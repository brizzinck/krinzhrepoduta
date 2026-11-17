/** S5. Історія поїздок усіх операторів і чек поїздки. */
import { CFG, TYPES, opByCode, DISCLAIMER } from '../config.js';
import * as api from '../api.js';
import * as tg from '../tg.js';
import { money, dateTime, fullTime, esc, minWord, timeOnly } from '../format.js';
import { icon, markerSvg } from './components.js';

/** Вкладка «Історія». */
export function mountHistory(root, ctx) {
  root.innerHTML = '<div class="screen s5"><h1>Історія</h1><div id="h"></div></div>';
  api.getHistory().then((rides) => {
    const h = root.querySelector('#h');
    if (!rides.length) {
      h.innerHTML = `<div class="empty big">${icon('receipt')}<p class="strong">Поїздок ще не було</p><p class="muted">Після першої поїздки тут з'являться чеки всіх операторів.</p></div>`;
      return;
    }
    h.innerHTML = `<ul class="list rides">${rides.map((r) => `<li><a class="ride-row" href="#receipt/${r.id}">
      <span class="o-mark">${markerSvg({ op: r.op, type: r.type, battery: null, size: 40 })}</span>
      <span class="grow"><span class="strong">${esc(opByCode(r.op).name)}</span> <span class="muted">${TYPES[r.type].name}</span><br><span class="muted sm">${dateTime(r.startedAt)}, ${r.minutes} ${minWord(r.minutes)}</span></span>
      <span class="strong num">${money(r.price)}</span></a></li>`).join('')}</ul>`;
  });
  tg.back.set(null);
  tg.main.hide();
  return () => {};
}

/** Чек поїздки. */
export function mountReceipt(root, ctx, id) {
  root.innerHTML = '<div class="screen s5r"><div id="r"></div></div>';
  api.getReceipt(id).then((r) => {
    const op = opByCode(r.op);
    root.querySelector('#r').innerHTML = `
      <div class="receipt-head"><span class="okmark">${icon('check')}</span><h1>Поїздку завершено</h1><p class="muted">Чек сформовано ${esc(fullTime(r.issuedAt || r.endedAt))}</p></div>
      <section class="card receipt" aria-label="Чек поїздки">
        <div class="rno">Чек № <span class="num">${esc(r.receiptNo)}</span></div>
        <div class="rows">
          <div class="kv"><span class="muted">Оператор</span><span>${esc(op.name)}</span></div>
          <div class="kv"><span class="muted">Транспорт</span><span>${TYPES[r.type].name}, ${esc(r.vehicleId)}</span></div>
          <div class="kv"><span class="muted">Початок</span><span class="num">${esc(fullTime(r.startedAt))}</span></div>
          <div class="kv"><span class="muted">Завершення</span><span class="num">${esc(fullTime(r.endedAt))}</span></div>
          <div class="kv"><span class="muted">Тривалість</span><span class="num">${r.minutes} ${minWord(r.minutes)}</span></div>
          <div class="kv"><span class="muted">Тариф</span><span class="num">${money(r.unlock, { compact: true })} + ${money(r.perMin)}/хв</span></div>
        </div>
        <div class="total"><span>До сплати</span><span class="num">${money(r.price)}</span></div>
        <div class="kv after"><span class="muted">Баланс після поїздки</span><span class="num ${r.balanceAfter < 0 ? 'danger-t' : ''}">${money(r.balanceAfter)}</span></div>
      </section>
      ${r.balanceAfter < 0 ? `<div class="banner danger" role="alert">${icon('alert-triangle')}<div><div class="strong">Є борг ${money(-r.balanceAfter)}</div><div class="sm">Його буде погашено першим поповненням. Нова поїздка заблокована до погашення.</div></div></div>` : ''}
      ${tg.inTelegram() ? '' : `<button type="button" class="btn ghost" id="print">${icon('file-text')}<span>Зберегти чек у PDF (друк)</span></button>`}
      <p class="disclaimer">${esc(DISCLAIMER)}</p>`;
    const p = root.querySelector('#print'); if (p) p.addEventListener('click', () => window.print());
  }).catch(() => { root.querySelector('#r').innerHTML = `<div class="empty big">${icon('receipt')}<p class="strong">Чек не знайдено</p></div>`; });
  tg.back.set(() => ctx.back('#s5'));
  tg.main.set({ text: 'Готово', active: true, visible: true, onClick: () => ctx.nav('#s3') });
  return () => {};
}
