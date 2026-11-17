/**
 * Прихований режим тестування: вмикається адресою ?test=1 або сімома дотиками по рядку версії в Профілі,
 * зберігається в localStorage (ключ emob.test). Показує службові елементи, яких немає в звичайному інтерфейсі.
 */
const KEY = 'emob.test';
let on = false;

function readFlag() { try { return localStorage.getItem(KEY) === '1'; } catch { return false; } }

/** Ініціалізація з адреси й сховища (викликається до першого малювання). */
export function init() {
  let q = null;
  try { q = new URLSearchParams(window.location.search).get('test'); } catch { /* без параметрів */ }
  if (q === '1') set(true);
  else if (q === '0') set(false);
  else on = readFlag();
}

export const isTest = () => on;

export function set(v) {
  on = !!v;
  try { if (on) localStorage.setItem(KEY, '1'); else localStorage.removeItem(KEY); } catch { /* лишається в пам'яті */ }
}
