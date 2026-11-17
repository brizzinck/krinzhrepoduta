/** S3. Карта й порівняння: Leaflet, маркери операторів, фільтр, сортування, нижній аркуш, розблокування. */
import { CFG, TILES, TYPES } from '../config.js';
import * as store from '../store.js';
import * as api from '../api.js';
import * as geo from '../geo.js';
import * as tg from '../tg.js';
import { money, dist, esc } from '../format.js';
import { icon, markerSvg, batteryChip, toast } from './components.js';

const L = window.L;

// Вибір користувача живе між відвідуваннями екрана в межах сеансу.
const ui = { filter: 'ALL', sort: 'price', selected: null, sheet: 'peek' };

const FILTERS = [['ALL', 'Усі'], ['E_SCOOTER', TYPES.E_SCOOTER.name], ['E_BIKE', TYPES.E_BIKE.name]];
const SORTS = [['price', 'Ціна'], ['distance', 'Відстань'], ['battery', 'Заряд']];

export function mount(root, ctx) {
  let map = null, userMarker = null, accCircle = null, offers = [], pos = null, alive = true, loading = true;
  const markers = new Map();
  let offMove = null;

  root.innerHTML = `<div class="screen s3 nopad">
    <h1 class="sr-only">Карта й порівняння транспорту</h1>
    <div class="mapwrap"><div id="map" class="map" role="application" aria-label="Карта з транспортом"></div></div>
    <div class="maptop"><div class="chips scroll" role="radiogroup" aria-label="Тип транспорту" id="filters"></div></div>
    <div class="fabs" id="fabs">
      <button type="button" class="fab" id="zin" aria-label="Збільшити">${icon('plus')}</button>
      <button type="button" class="fab" id="zout" aria-label="Зменшити"><svg class="ic" aria-hidden="true"><use href="vendor/tabler/sprite.svg#minus"/></svg></button>
      <button type="button" class="fab pri" id="recenter" aria-label="Моя позиція">${icon('current-location')}</button>
    </div>
    <section class="sheet" id="sheet" data-state="${ui.sheet}" aria-label="Транспорт поруч">
      <button type="button" class="sheet-head" id="sheetHead" aria-expanded="${ui.sheet === 'full'}" aria-controls="offers">
        <span class="handle" aria-hidden="true"></span>
        <span class="sheet-title"><span id="countTxt" aria-live="polite">Шукаємо транспорт поруч</span><span class="exp">${icon('chevron-up')}</span></span>
      </button>
      <div class="sortrow" role="radiogroup" aria-label="Сортування"><span class="muted sm">Сортувати</span><div class="seg" id="sorts"></div></div>
      <ul class="offers" id="offers"></ul>
    </section>
  </div>`;
  const $ = (id) => root.querySelector(`#${id}`);

  /* ---------- фільтри й сортування ---------- */
  const renderFilters = () => {
    $('filters').innerHTML = FILTERS.map(([k, name]) => `<button type="button" role="radio" aria-checked="${ui.filter === k}" class="chip${ui.filter === k ? ' on' : ''}" data-f="${k}">${name}</button>`).join('');
    $('sorts').innerHTML = SORTS.map(([k, name]) => `<button type="button" role="radio" aria-checked="${ui.sort === k}" class="seg-b${ui.sort === k ? ' on' : ''}" data-s="${k}">${name}</button>`).join('');
  };

  /* ---------- список ---------- */
  const offerHtml = (o) => `<li><button type="button" class="offer${ui.selected === o.id ? ' sel' : ''}" data-id="${o.id}" aria-pressed="${ui.selected === o.id}">
      <span class="o-head">
        <span class="o-mark">${markerSvg({ op: o.op, type: o.type, battery: o.battery, size: 36 })}</span>
        <span class="o-title"><b>${esc(o.opName)}</b><span class="muted">${esc(o.typeName)}</span></span>
        ${batteryChip(o.battery)}
      </span>
      <span class="o-left">
        <span class="o-walk">${icon('walk')}<span>~${o.walk} хв пішки, ${dist(o.distance)}</span></span>
        <span class="o-tariff muted">${money(o.unlock, { compact: true })} + ${money(o.perMin)}/хв</span>
      </span>
      <span class="o-est num">10 хв ≈ ${money(o.est10, { compact: true })}</span>
    </button></li>`;

  function renderList() {
    $('countTxt').textContent = loading ? 'Шукаємо транспорт поруч' : offers.length ? `Поруч: ${offers.length}` : 'Поруч немає транспорту';
    const ul = $('offers');
    if (loading) {
      ul.innerHTML = '<li class="skel" aria-hidden="true"></li><li class="skel" aria-hidden="true"></li><li class="skel" aria-hidden="true"></li>';
    } else if (!offers.length) {
      ul.innerHTML = `<li class="empty">${icon('map-pin')}<p>У цьому районі немає вільного транспорту вибраного типу.</p><p class="muted sm">Змініть фільтр або перемістіть карту.</p></li>`;
    } else {
      ul.innerHTML = offers.map(offerHtml).join('');
    }
  }

  /* ---------- маркери ---------- */
  const mkIcon = (o) => L.divIcon({ className: `mk${ui.selected === o.id ? ' sel' : ''}`, html: markerSvg({ op: o.op, type: o.type, battery: o.battery }), iconSize: [36, 36], iconAnchor: [18, 18] });

  function renderMarkers() {
    if (!map) return;
    const ids = new Set(offers.map((o) => o.id));
    for (const [id, m] of markers) if (!ids.has(id)) { map.removeLayer(m); markers.delete(id); }
    for (const o of offers) {
      let m = markers.get(o.id);
      if (!m) {
        m = L.marker([o.lat, o.lng], { icon: mkIcon(o), keyboard: true, title: `${o.opName}, ${o.typeName.toLowerCase()}, заряд ${o.battery} %`, alt: `${o.opName}, ${o.typeName}`, riseOnHover: false }).addTo(map);
        m.on('click', (e) => { if (e.originalEvent) L.DomEvent.stopPropagation(e); tg.haptic.tap(); select(o.id, { pan: true, scroll: true }); });
        markers.set(o.id, m);
      } else if (m._sel !== (ui.selected === o.id)) m.setIcon(mkIcon(o));
      m._sel = ui.selected === o.id;
      m.setZIndexOffset(ui.selected === o.id ? 1000 : 0);
    }
  }

  /* ---------- вибір ---------- */
  function sheetPx() { return $('sheet').offsetHeight; }
  function panToVisible(o) {
    const size = map.getSize();
    const visibleH = size.y - sheetPx();
    const pt = map.latLngToContainerPoint([o.lat, o.lng]);
    map.panBy([pt.x - size.x / 2, pt.y - Math.max(visibleH / 2, 60)], { animate: true, duration: 0.3 });
  }
  function select(id, { pan = false, scroll = false } = {}) {
    ui.selected = id;
    const o = offers.find((x) => x.id === id);
    renderMarkers();
    root.querySelectorAll('.offer').forEach((b) => { const on = b.dataset.id === id; b.classList.toggle('sel', on); b.setAttribute('aria-pressed', String(on)); });
    if (!o) { tg.main.hide(); return; }
    if (ui.sheet === 'full') setSheet('peek');
    if (pan) setTimeout(() => alive && panToVisible(o), ui.sheet === 'peek' ? 0 : 280);
    if (scroll) { const c = root.querySelector(`.offer[data-id="${id}"]`); if (c) c.scrollIntoView({ block: 'nearest', behavior: 'smooth' }); }
    tg.main.set({ text: `Розблокувати · ${o.opName}`, active: true, visible: true, onClick: () => unlock(o) });
  }

  /* ---------- розблокування ---------- */
  async function unlock(o) {
    const btn = await tg.popup({
      title: `Розблокувати ${o.opName}?`,
      message: `${o.typeName}, ${o.id}. Розблокування ${money(o.unlock, { compact: true })}, далі ${money(o.perMin)} за хвилину. Сума спишеться з гаманця після завершення.`,
      buttons: [{ id: 'go', type: 'default', text: 'Розблокувати' }, { id: 'cancel', type: 'cancel' }],
    });
    if (btn !== 'go') return;
    tg.main.busy(true);
    try {
      await api.startRide(o.id);
      tg.haptic.ok();
      ctx.nav('#s4');
    } catch (e) {
      tg.main.busy(false);
      tg.haptic.err();
      if (e.code === 'NO_IDENTITY' || e.code === 'NO_CONSENT') { toast(e.message, 4200); ctx.nav('#s1'); return; }
      if (e.code === 'TAKEN') { ui.selected = null; tg.main.hide(); await refresh(); toast(e.message, 4200); return; }
      if (e.code === 'DEBT' || e.code === 'LOW_BALANCE') { toast(e.message, 4200); setTimeout(() => ctx.nav('#s2'), 900); return; }
      toast(e.message, 4200);
    }
  }

  /* ---------- шторка ---------- */
  function setSheet(state) {
    ui.sheet = state;
    $('sheet').dataset.state = state;
    $('sheetHead').setAttribute('aria-expanded', String(state === 'full'));
    $('fabs').classList.toggle('hide', state === 'full');
  }

  /* ---------- дані ---------- */
  let seq = 0;
  async function refresh() {
    const my = ++seq;
    try {
      const res = await api.getOffers({ pos, type: ui.filter, sort: ui.sort });
      if (!alive || my !== seq) return;
      offers = res;
    } catch { offers = []; }
    loading = false;
    if (ui.selected && !offers.some((o) => o.id === ui.selected)) { ui.selected = null; tg.main.hide(); }
    renderList(); renderMarkers();
  }

  function drawUser() {
    if (!map || !pos) return;
    const ll = [pos.lat, pos.lng];
    if (!userMarker) {
      userMarker = L.marker(ll, { icon: L.divIcon({ className: 'me', html: '<span class="me-dot"></span>', iconSize: [22, 22], iconAnchor: [11, 11] }), interactive: false, keyboard: false, zIndexOffset: -500 }).addTo(map);
      accCircle = L.circle(ll, { radius: Math.max(pos.accuracy || 0, 20), className: 'acc', interactive: false }).addTo(map);
    } else { userMarker.setLatLng(ll); accCircle.setLatLng(ll); accCircle.setRadius(Math.max(pos.accuracy || 0, 20)); }
  }

  function centerOnUser(animate = false) {
    const size = map.getSize();
    const shift = Math.max((sheetPx() / 2) - 20, 0);
    map.setView([pos.lat, pos.lng], map.getZoom() || CFG.MAP_ZOOM, { animate });
    map.panBy([0, shift], { animate: false });
  }

  async function boot() {
    renderFilters(); renderList();
    pos = geo.current() || await geo.getPosition();
    if (!alive) return;
    map = L.map($('map'), { zoomControl: false, attributionControl: false, minZoom: 12, maxZoom: 19, zoomSnap: 1, tap: false })
      .setView([pos.lat, pos.lng], CFG.MAP_ZOOM);
    L.control.attribution({ position: 'topright', prefix: false }).addAttribution(TILES.ATTRIBUTION).addTo(map);
    L.tileLayer(TILES.URL, { maxZoom: 19, referrerPolicy: 'strict-origin-when-cross-origin' }).addTo(map);
    map.on('click', () => { if (ui.selected) { select(null); } });
    map.whenReady(() => { map.invalidateSize(); centerOnUser(false); drawUser(); });
    if (geo.isFallback() && !pos.notified) { pos.notified = true; toast('Геолокацію не надано: показано район ДУІКТ', 4200); }
    geo.watch();
    offMove = geo.onMove((p) => { pos = p; drawUser(); refresh(); });
    await refresh();
    if (ui.selected) select(ui.selected);
  }

  /* ---------- події ---------- */
  root.addEventListener('click', async (e) => {
    const f = e.target.closest('[data-f]');
    if (f) { ui.filter = f.dataset.f; tg.haptic.select(); renderFilters(); root.querySelector(`[data-f="${ui.filter}"]`).focus(); await refresh(); return; }
    const sr = e.target.closest('[data-s]');
    if (sr) { ui.sort = sr.dataset.s; tg.haptic.select(); renderFilters(); root.querySelector(`[data-s="${ui.sort}"]`).focus(); await refresh(); return; }
    const card = e.target.closest('.offer');
    if (card) { tg.haptic.tap(); const id = card.dataset.id; select(ui.selected === id ? null : id, { pan: ui.selected !== id }); return; }
    if (e.target.closest('#sheetHead')) { setSheet(ui.sheet === 'peek' ? 'full' : 'peek'); return; }
    if (e.target.closest('#zin')) { map && map.zoomIn(); return; }
    if (e.target.closest('#zout')) { map && map.zoomOut(); return; }
    if (e.target.closest('#recenter')) await recenter();
  });

  async function recenter() {
    if (!map) return;
    if (geo.isFallback()) {
      const p = await geo.getPosition();
      if (p.source === 'default') {
        if (geo.canOpenSettings()) {
          const b = await tg.popup({ title: 'Геолокація вимкнена', message: 'Дозвольте доступ до позиції в налаштуваннях Telegram, щоб бачити транспорт поруч.', buttons: [{ id: 'set', type: 'default', text: 'Дозволити в налаштуваннях' }, { id: 'no', type: 'cancel' }] });
          if (b === 'set') tg.geoTg.openSettings();
        } else toast('Геолокацію не надано: показано район ДУІКТ', 4200);
      } else { pos = p; geo.watch(); drawUser(); await refresh(); }
    }
    centerOnUser(true);
  }

  if (ui.sheet === 'full') $('fabs').classList.add('hide');
  tg.back.set(null);
  tg.main.hide();
  boot();
  return () => {
    alive = false;
    geo.unwatch();
    if (offMove) offMove();
    if (map) { map.remove(); map = null; }
  };
}
