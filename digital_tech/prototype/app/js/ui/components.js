/** Спільні елементи інтерфейсу: піктограми, маркери, тости, нижній аркуш поповнення. */
import { TYPES, CFG, opByCode } from '../config.js';
import { esc, money } from '../format.js';
import * as tg from '../tg.js';
import * as api from '../api.js';
import * as store from '../store.js';

const SPRITE = 'vendor/tabler/sprite.svg';

/** Піктограма Tabler зі спрайта; колір задає currentColor. */
export const icon = (name, cls = '') => `<svg class="ic ${cls}" aria-hidden="true" focusable="false"><use href="${SPRITE}#${name}"/></svg>`;

const SHAPES = {
  circle: '<circle class="shape" cx="18" cy="18" r="13.5"/>',
  square: '<rect class="shape" x="4.5" y="4.5" width="27" height="27" rx="9"/>',
  hexagon: '<polygon class="shape" points="18,4 30.1,11 30.1,25 18,32 5.9,25 5.9,11" stroke-linejoin="round"/>',
};
const RINGS = {
  circle: '<circle class="ring" cx="18" cy="18" r="13.5"/>',
  square: '<rect class="ring" x="4.5" y="4.5" width="27" height="27" rx="9"/>',
  hexagon: '<polygon class="ring" points="18,4 30.1,11 30.1,25 18,32 5.9,25 5.9,11" stroke-linejoin="round"/>',
};

/** Маркер транспорту: колір оператора, форма й піктограма типу, крапка при низькому заряді. */
export function markerSvg({ op, type, battery, size = 36 }) {
  const t = TYPES[type];
  const color = opByCode(op).colorVar;
  const low = battery != null && battery < CFG.BATTERY_WARN;
  return `<svg class="mk-svg" viewBox="0 0 36 36" width="${size}" height="${size}" style="--c:var(${color})" aria-hidden="true">
    ${RINGS[t.shape]}${SHAPES[t.shape]}
    <svg class="glyph" x="9" y="9" width="18" height="18" viewBox="0 0 24 24"><use href="${SPRITE}#${t.icon}"/></svg>
    ${low ? '<circle class="lowdot" cx="30" cy="6" r="4.6"/>' : ''}</svg>`;
}

/** Фішка заряду: колір і піктограма залежать від рівня, а не лише колір. */
export function batteryChip(pct) {
  const kind = pct < CFG.BATTERY_REFUSE ? 'danger' : pct < CFG.BATTERY_WARN ? 'warning' : 'success';
  const ic = kind === 'success' ? 'battery-3' : 'battery-1';
  return `<span class="badge ${kind}">${icon(ic)}<span>${pct} %</span></span>`;
}

/* ---------- тости ---------- */

let toastTimer = null;
/** Короткий тост знизу; role=status. */
export function toast(text, ms = 3200) {
  const layer = document.getElementById('toasts');
  layer.innerHTML = `<div class="toast" role="status">${esc(text)}</div>`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { layer.innerHTML = ''; }, ms);
}

/* ---------- аркуш поповнення ---------- */

function setShimOffset() {
  const el = document.getElementById('shim-main');
  const h = el && !el.hidden ? el.offsetHeight : 0;
  document.documentElement.style.setProperty('--shim-main-h', `${h}px`);
}

/**
 * Нижній аркуш поповнення з імітованим платіжним шлюзом.
 * auto: одразу запускає оплату вибраної суми (кнопка «Поповнити» на екрані гаманця).
 * Повертає true, якщо оплату підтверджено.
 */
export function openTopUp({ amount = CFG.TOPUPS[0], auto = false } = {}) {
  return new Promise((resolve) => {
    const layer = document.getElementById('layer');
    const wrap = document.createElement('div');
    wrap.className = 'modal sheetm';
    let amt = amount, busy = false, paid = false;
    const prev = document.activeElement;
    tg.main.push();
    setShimOffset();
    const chips = () => CFG.TOPUPS.map((a) => `<button type="button" role="radio" aria-checked="${a === amt}" class="chip${a === amt ? ' on' : ''}" data-a="${a}">${money(a, { compact: true })}</button>`).join('');
    wrap.innerHTML = `<div class="panel" role="dialog" aria-modal="true" aria-labelledby="tu-t">
      <div class="grab" aria-hidden="true"></div>
      <h2 id="tu-t">Поповнення гаманця</h2>
      <div class="chips" role="radiogroup" aria-label="Сума поповнення">${chips()}</div>
      <div class="cardrow">${icon('credit-card')}<div><div class="strong">Картка ${CFG.CARD}</div><div class="muted sm">Імітація платіжного шлюзу, реальні гроші не списуються</div></div></div>
      <div class="pay-state" aria-live="polite"></div>
      <button type="button" class="btn ghost close">Закрити</button></div>`;
    const state = wrap.querySelector('.pay-state');
    const finish = () => {
      tg.main.pop(); wrap.remove(); document.removeEventListener('keydown', onKey);
      document.documentElement.style.setProperty('--shim-main-h', '0px');
      if (prev && prev.focus) prev.focus();
      resolve(paid);
    };
    const setMain = () => tg.main.set({ text: `Сплатити ${money(amt, { compact: true })}`, active: !busy && !paid, onClick: pay });
    async function pay() {
      if (busy || paid) return;
      busy = true;
      state.innerHTML = `<span class="spin on" aria-hidden="true"></span><span>Зв'язуємося з банком…</span>`;
      tg.main.busy(true);
      wrap.querySelectorAll('.chip,.close').forEach((b) => { b.disabled = true; });
      try {
        await api.topUp(amt);
        paid = true; busy = false;
        state.innerHTML = `${icon('check', 'okic')}<span>Оплату підтверджено</span>`;
        tg.haptic.ok();
        tg.main.busy(false); tg.main.set({ text: 'Готово', active: false, onClick: null });
        setTimeout(finish, 900);
      } catch (e) {
        busy = false;
        state.innerHTML = `${icon('alert-triangle')}<span>${esc(e.message)}</span>`;
        tg.haptic.err(); tg.main.busy(false); setMain();
        wrap.querySelectorAll('.chip,.close').forEach((b) => { b.disabled = false; });
      }
    }
    const onKey = (e) => { if (e.key === 'Escape' && !busy && !paid) finish(); };
    wrap.addEventListener('click', (e) => {
      const c = e.target.closest('.chip');
      if (c && !busy) { amt = Number(c.dataset.a); tg.haptic.select(); wrap.querySelector('.chips').innerHTML = chips(); setMain(); return; }
      if (e.target === wrap && !busy && !paid) finish();
      if (e.target.closest('.close') && !busy && !paid) finish();
    });
    document.addEventListener('keydown', onKey);
    layer.appendChild(wrap);
    setMain();
    if (auto) pay(); else (wrap.querySelector('.chip.on') || wrap.querySelector('.close')).focus();
  });
}

/** Підписати поточний стан (баланс) для екранів. */
export const balance = () => store.get().wallet.balance;
