#!/usr/bin/env python3
"""Аналог sys.dm_db_index_physical_stats (режим DETAILED) для InnoDB.

Читає файл табличного простору .ibd (його копію, зроблену після FLUSH TABLES ... FOR EXPORT)
і по сторінках типу INDEX рахує для кожного індексу й рівня дерева:
  depth, level, pages, records (index_depth, index_level, page_count, record_count),
  used_pct: частка зайнятого місця на сторінці, heap_top - garbage + слоти каталогу + трейлер (avg_page_space_used_in_percent),
  frag_pct: частка сторінок рівня, чия наступна сторінка в логічному ланцюжку (FIL_PAGE_NEXT)
            не є фізично наступною, page_no + 1 (avg_fragmentation_in_percent).
Використання: ibdstat.py файл.ibd назва_індексу=index_id@коренева_сторінка [...]   (беруться з information_schema.INNODB_INDEXES: INDEX_ID, PAGE_NO)
Розмір сторінки 16384. Формат: заголовок FIL — 38 байт, далі заголовок сторінки індексу (MySQL 8.4).
"""
import struct
import sys
from collections import defaultdict

PAGE = 16384
FIL_PAGE_INDEX = 17855


def parse(path, names, roots):
    data = open(path, "rb").read()
    levels = defaultdict(list)  # (index_id, level) -> [(page_no, next, n_recs, used)]
    for off in range(0, len(data) - PAGE + 1, PAGE):
        p = data[off:off + PAGE]
        page_no, prev, nxt = struct.unpack(">III", p[4:16])
        ptype = struct.unpack(">H", p[24:26])[0]
        if ptype != FIL_PAGE_INDEX:
            continue
        n_dir, heap_top, n_heap, free, garbage = struct.unpack(">5H", p[38:48])
        n_recs = struct.unpack(">H", p[54:56])[0]
        level = struct.unpack(">H", p[64:66])[0]
        index_id = struct.unpack(">Q", p[66:74])[0]
        used = heap_top - garbage + 2 * n_dir + 8
        levels[(index_id, level)].append((page_no, nxt, n_recs, min(used, PAGE)))
    # рівень кореня беремо з кореневої сторінки (номер у INNODB_INDEXES.PAGE_NO); зайві копії сторінок верхніх рівнів,
    # що лишаються у файлі після масового завантаження індексу, відкидаємо
    root_level = {}
    for (iid, lvl), pages in levels.items():
        for x in pages:
            if x[0] == roots.get(iid):
                root_level[iid] = lvl
    rows = []
    for (iid, lvl), pages in sorted(levels.items()):
        if iid in root_level:
            if lvl > root_level[iid]:
                continue
            if lvl == root_level[iid]:
                pages = [x for x in pages if x[0] == roots[iid]]
        depth = 1 + root_level.get(iid, max(l for (i, l) in levels if i == iid))
        name = names.get(iid, f"id{iid}")
        pc = len(pages)
        rec = sum(x[2] for x in pages)
        space = 100.0 * sum(x[3] for x in pages) / (pc * PAGE)
        # зовнішня фрагментація: частка сторінок, за якими логічно йде не наступна фізична
        nums = {x[0] for x in pages}
        bad = sum(1 for x in pages if x[1] in nums and x[1] != x[0] + 1)
        frag = 100.0 * bad / pc
        rows.append((name, depth, lvl, pc, rec, space, frag))
    return rows


def main():
    path = sys.argv[1]
    names, roots = {}, {}
    for a in sys.argv[2:]:
        n, i = a.rsplit("=", 1)
        i, _, r = i.partition("@")
        names[int(i)] = n
        if r:
            roots[int(i)] = int(r)
    rows = parse(path, names, roots)
    hdr = ("index_name", "depth", "level", "pages", "records", "used_pct", "frag_pct")
    out = [hdr] + [(n, str(d), str(l), str(pc), str(rc), f"{sp:.2f}", f"{fr:.2f}") for n, d, l, pc, rc, sp, fr in rows]
    w = [max(len(r[i]) for r in out) for i in range(len(hdr))]
    sep = "+" + "+".join("-" * (x + 2) for x in w) + "+"
    print(sep)
    print("| " + " | ".join(h.ljust(w[i]) for i, h in enumerate(hdr)) + " |")
    print(sep)
    for r in out[1:]:
        print("| " + " | ".join((r[i].ljust(w[i]) if i == 0 else r[i].rjust(w[i])) for i in range(len(hdr))) + " |")
    print(sep)


if __name__ == "__main__":
    main()
