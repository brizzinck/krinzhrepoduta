/**
 * Обгортка Telegram Mini Apps. Екрани не звертаються до `Telegram` напряму.
 * У браузері (initData порожній) головна кнопка, кнопка «Назад» і спливне вікно малюються в HTML
 * з тим самим виглядом. Усі виклики Telegram іде через safe() і перевірку версії.
 */
import { CFG } from './config.js';

const W = () => (window.Telegram && window.Telegram.WebApp) || null;
const safe = (fn, fallback) => { try { return fn(); } catch { return fallback; } };
const atLeast = (v) => !!safe(() => W().isVersionAtLeast(v), false);

/** Чи застосунок відкрито всередині Telegram. */
export const inTelegram = () => !!safe(() => W().initData, false);

const cssVar = (n) => getComputedStyle(document.documentElement).getPropertyValue(n).trim();

/** Користувач із initDataUnsafe або тестовий гість. */
export function user() {
  const u = safe(() => W().initDataUnsafe.user, null);
  if (inTelegram() && u && u.id) return { id: u.id, firstName: u.first_name || 'Користувач', username: u.username || null, isGuest: false };
  return { id: 0, firstName: 'Гість', username: null, isGuest: true };
}

/** Параметр запуску (?startapp=s3) для переходу на екран: initData, потім адреса сторінки. */
export function startParam() {
  const p = safe(() => W().initDataUnsafe.start_param, null);
  if (p) return p;
  try {
    const q = new URLSearchParams(window.location.search);
    return q.get('startapp') || q.get('tgWebAppStartParam') || null;
  } catch { return null; }
}

const isDark = () => (inTelegram() ? safe(() => W().colorScheme, 'light') === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches);

/** Тема: data-theme з colorScheme (Telegram) або prefers-color-scheme (браузер), кольори оболонки з CSS-змінних. */
export function applyTheme() {
  document.documentElement.dataset.theme = isDark() ? 'dark' : 'light';
  const w = W();
  if (!w) return;
  const bg = cssVar('--bg'), surface = cssVar('--surface');
  if (atLeast('6.9')) safe(() => w.setHeaderColor(bg));
  if (atLeast('6.1')) safe(() => w.setBackgroundColor(bg));
  if (atLeast('7.10')) safe(() => w.setBottomBarColor(surface));
  paintMain();
}

let started = false;
/** Запуск: ready, expand, вимкнення вертикальних свайпів, тема. */
export function init() {
  if (started) return;
  started = true;
  const w = W();
  if (w) {
    safe(() => w.ready());
    safe(() => w.expand());
    if (atLeast('7.7')) safe(() => w.disableVerticalSwipes());
    safe(() => w.onEvent('themeChanged', applyTheme));
    safe(() => w.onEvent('viewportChanged', syncViewport));
  }
  safe(() => matchMedia('(prefers-color-scheme: dark)').addEventListener('change', applyTheme));
  applyTheme();
  syncViewport();
  if (inTelegram() && window.location.origin !== CFG.PROD_ORIGIN) {
    console.warn(`Mini App відкрито з ${window.location.origin}; у @BotFather зареєстровано лише ${CFG.PROD_ORIGIN}.`);
  }
  document.documentElement.dataset.shim = inTelegram() ? 'off' : 'on';
}

function syncViewport() {
  const h = safe(() => W().viewportStableHeight, 0);
  if (inTelegram() && h) document.documentElement.style.setProperty('--vh', `${h}px`);
}

/** Закріпити застосунок із підтвердженням закриття (лише під час поїздки). */
let closingState = null;
export function closing(on) {
  if (!atLeast('6.2') || closingState === !!on) return;
  closingState = !!on;
  safe(() => (on ? W().enableClosingConfirmation() : W().disableClosingConfirmation()));
}

/* ---------- головна кнопка ---------- */

let mainCfg = { text: '', active: false, visible: false, progress: false, onClick: null };
const mainStack = [];
let nativeHandler = null;

function paintMain() {
  const c = mainCfg;
  const shim = document.getElementById('shim-main');
  if (shim && !inTelegram()) {
    shim.hidden = !c.visible;
    const b = shim.firstElementChild;
    if (b) {
      b.disabled = !c.active || c.progress;
      b.setAttribute('aria-busy', c.progress ? 'true' : 'false');
      b.classList.toggle('busy', !!c.progress);
      b.querySelector('.t').textContent = c.text;
    }
  }
  const w = W();
  if (!w || !inTelegram()) return;
  if (!c.visible || !c.text) {            // SDK не приймає порожній текст: лише ховаємо кнопку
    safe(() => { w.MainButton.hideProgress(); w.MainButton.hide(); });
    return;
  }
  safe(() => {
    w.MainButton.setParams({
      text: c.text,
      is_active: c.active && !c.progress,
      is_visible: c.visible,
      color: cssVar(c.active ? '--primary' : '--disabled'),
      text_color: cssVar('--on-primary'),
    });
    if (c.progress) w.MainButton.showProgress(false); else w.MainButton.hideProgress();
  });
}

function bindMainClick() {
  const w = W();
  if (w && inTelegram()) {
    if (nativeHandler) safe(() => w.MainButton.offClick(nativeHandler));
    nativeHandler = () => { if (mainCfg.onClick && mainCfg.active && !mainCfg.progress) mainCfg.onClick(); };
    safe(() => w.MainButton.onClick(nativeHandler));
  }
}

export const main = {
  /** Задати параметри головної кнопки (єдина основна дія екрана). */
  set({ text, active = true, visible = true, onClick = null, progress = false }) {
    mainCfg = { text, active, visible, onClick, progress };
    bindMainClick();
    paintMain();
  },
  hide() { mainCfg = { ...mainCfg, visible: false, onClick: null }; bindMainClick(); paintMain(); },
  /** Показати індикатор виконання на кнопці. */
  busy(on) { mainCfg = { ...mainCfg, progress: !!on }; paintMain(); },
  /** Запам'ятати поточну кнопку (перед тимчасовим аркушем) і відновити її після. */
  push() { mainStack.push(mainCfg); },
  pop() { const c = mainStack.pop(); if (c) { mainCfg = c; bindMainClick(); paintMain(); } },
  current: () => mainCfg,
  /** Підключення HTML-кнопки в режимі браузера. */
  mountShim() {
    const el = document.getElementById('shim-main');
    el.innerHTML = '<button type="button" class="main-btn"><span class="spin" aria-hidden="true"></span><span class="t"></span></button>';
    el.firstElementChild.addEventListener('click', () => { if (mainCfg.onClick && mainCfg.active && !mainCfg.progress) mainCfg.onClick(); });
  },
};

/* ---------- кнопка «Назад» ---------- */

let backHandler = null;
let nativeBack = null;
export const back = {
  /** Показати «Назад» з обробником або сховати (null). */
  set(handler) {
    backHandler = handler;
    const w = W();
    if (w && inTelegram()) {
      safe(() => { if (nativeBack) w.BackButton.offClick(nativeBack); });
      if (handler) { nativeBack = () => backHandler && backHandler(); safe(() => w.BackButton.onClick(nativeBack)); safe(() => w.BackButton.show()); }
      else safe(() => w.BackButton.hide());
    }
    const bar = document.getElementById('shim-back');
    if (bar && !inTelegram()) bar.hidden = !handler;
  },
  mountShim() {
    const bar = document.getElementById('shim-back');
    bar.innerHTML = '<button type="button" class="back-btn"><svg class="ic" aria-hidden="true"><use href="vendor/tabler/sprite.svg#chevron-left"/></svg><span>Назад</span></button>';
    bar.firstElementChild.addEventListener('click', () => backHandler && backHandler());
  },
};

/* ---------- спливне вікно ---------- */

/**
 * Показати вікно з кнопками; повертає id натиснутої кнопки або null (закрито).
 * @param {{title?:string, message:string, buttons:{id:string,type?:string,text?:string}[]}} o
 */
export function popup(o) {
  const w = W();
  if (w && inTelegram() && atLeast('6.2')) {
    return new Promise((resolve) => {
      try { w.showPopup(o, (id) => resolve(id || null)); } catch { shimPopup(o).then(resolve); }
    });
  }
  return shimPopup(o);
}

function shimPopup(o) {
  return new Promise((resolve) => {
    const layer = document.getElementById('layer');
    const wrap = document.createElement('div');
    wrap.className = 'modal';
    const defText = { cancel: 'Скасувати', close: 'Закрити', ok: 'Гаразд' };
    wrap.innerHTML = `<div class="dialog" role="dialog" aria-modal="true" aria-labelledby="dlg-t">
      <h2 id="dlg-t">${o.title ? escHtml(o.title) : ''}</h2><p>${escHtml(o.message)}</p>
      <div class="dlg-btns">${o.buttons.map((b) => `<button type="button" class="btn ${b.type === 'destructive' ? 'danger' : b.type === 'cancel' || b.type === 'close' ? 'ghost' : 'primary'}" data-id="${escHtml(b.id)}">${escHtml(b.text || defText[b.type] || 'Гаразд')}</button>`).join('')}</div></div>`;
    if (!o.title) wrap.querySelector('h2').remove();
    const prev = document.activeElement;
    const done = (id) => { wrap.remove(); document.removeEventListener('keydown', onKey); if (prev && prev.focus) prev.focus(); resolve(id); };
    const onKey = (e) => { if (e.key === 'Escape') done(null); };
    wrap.addEventListener('click', (e) => { const b = e.target.closest('button[data-id]'); if (b) done(b.dataset.id); else if (e.target === wrap) done(null); });
    document.addEventListener('keydown', onKey);
    layer.appendChild(wrap);
    const first = wrap.querySelector('button.primary, button.danger') || wrap.querySelector('button');
    if (first) first.focus();
  });
}
const escHtml = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

/* ---------- вібровідгук ---------- */

const hf = () => (atLeast('6.1') ? safe(() => W().HapticFeedback, null) : null);
export const haptic = {
  select() { safe(() => hf().selectionChanged()); },
  tap() { safe(() => hf().impactOccurred('light')); },
  ok() { safe(() => hf().notificationOccurred('success')); },
  err() { safe(() => hf().notificationOccurred('error')); },
  warn() { safe(() => hf().notificationOccurred('warning')); },
};

/* ---------- геолокація Telegram (Bot API 8.0) ---------- */

export const geoTg = {
  supported: () => inTelegram() && atLeast('8.0') && !!safe(() => W().LocationManager, null),
  init() {
    return new Promise((resolve) => {
      const lm = safe(() => W().LocationManager, null);
      if (!lm) return resolve(false);
      if (lm.isInited) return resolve(true);
      const t = setTimeout(() => resolve(false), 4000);
      try { lm.init(() => { clearTimeout(t); resolve(true); }); } catch { clearTimeout(t); resolve(false); }
    });
  },
  state() {
    const lm = safe(() => W().LocationManager, {});
    return { available: !!lm.isLocationAvailable, requested: !!lm.isAccessRequested, granted: !!lm.isAccessGranted };
  },
  /** Отримати позицію; null, якщо доступ заборонено. */
  get() {
    return new Promise((resolve) => {
      const lm = safe(() => W().LocationManager, null);
      if (!lm) return resolve(null);
      const t = setTimeout(() => resolve(null), 8000);
      try { lm.getLocation((d) => { clearTimeout(t); resolve(d || null); }); } catch { clearTimeout(t); resolve(null); }
    });
  },
  openSettings() { safe(() => W().LocationManager.openSettings()); },
};

/** Відкрити зовнішнє посилання: у Telegram через openLink, у браузері нова вкладка. */
export function openLink(url) {
  if (inTelegram()) { try { W().openLink(url); return; } catch { /* нижче */ } }
  window.open(url, '_blank', 'noopener');
}

/** Подія повернення в застосунок (Bot API 8.0): перерахувати таймер. */
export function onActivated(cb) { if (atLeast('8.0')) safe(() => W().onEvent('activated', cb)); }
