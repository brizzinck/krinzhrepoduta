/** Запуск застосунку: тема, стан, маршрути за hash, вкладки, нагляд за поїздкою. */
import * as tg from './tg.js';
import * as testmode from './testmode.js';
import * as store from './store.js';
import * as api from './api.js';
import { CFG } from './config.js';
import { money } from './format.js';
import { toast, openTopUp } from './ui/components.js';
import * as s1 from './ui/s1-start.js';
import * as s2 from './ui/s2-wallet.js';
import * as s3 from './ui/s3-map.js';
import * as s4 from './ui/s4-ride.js';
import * as s5 from './ui/s5-history.js';
import * as profile from './ui/profile.js';
import * as legal from './ui/legal.js';

const view = document.getElementById('view');
const tabs = document.getElementById('tabs');
let cleanup = null;
let depth = 0;       // скільки переходів зроблено всередині застосунку (для «Назад»)
let ready = false;

const TAB = { s3: 'map', s4: 'map', s2: 'wallet', s5: 'history', receipt: 'history', profile: 'profile', privacy: 'profile', terms: 'profile' };
const START = { s1: '#s1', s2: '#s2', s3: '#s3', s4: '#s4', s5: '#s5', profile: '#profile' };
const KNOWN = new Set(['s1', 's2', 's3', 's4', 's5', 'receipt', 'profile', 'privacy', 'terms']);

function nav(hash, opts = {}) {
  if (opts.toast) toast(opts.toast);
  depth++;
  if (location.hash === hash) route(); else location.hash = hash;
}
function back(parent) {
  if (depth > 0) { depth--; history.back(); } else if (location.hash !== parent) location.hash = parent;
}
/** Замінити поточний запис історії (без нового кроку «Назад»). */
function replace(hash) {
  history.replaceState(null, '', hash);
  route();
}
const ctx = { nav, back, replace, toast };

/** Підібрати екран із урахуванням згоди й активної поїздки. */
function resolve(raw) {
  const s = store.get();
  let [name, arg] = raw.replace(/^#/, '').split('/');
  if (!name || !KNOWN.has(name)) name = s.user.consentAt ? 's3' : 's1';
  const open = ['privacy', 'terms'].includes(name);
  if (!s.user.consentAt && !open) name = 's1';
  else if (s.activeRide && ['s3', 's1'].includes(name)) name = 's4';
  else if (!s.activeRide && name === 's4') { name = 's3'; if (booting) pendingToast = 'Активної поїздки немає'; }
  return [name, arg];
}
let pendingToast = null;
let booting = true;   // тост про відсутню поїздку лише для відкритого посилання, не для кроку «Назад»

function route() {
  if (!ready) return;
  const [name, arg] = resolve(location.hash);
  const canon = arg ? `#${name}/${arg}` : `#${name}`;
  if (location.hash !== canon) { history.replaceState(null, '', canon); }
  tg.closePopups();   // вікно попереднього екрана не переходить на наступний (напр. на чек)
  if (cleanup) { try { cleanup(); } catch { /* перехід важливіший за прибирання */ } cleanup = null; }
  view.textContent = '';
  const root = document.createElement('div');
  root.className = 'route';
  view.appendChild(root);
  tg.main.hide();
  tg.back.set(null);
  tabs.hidden = name === 's1';
  tabs.querySelectorAll('a').forEach((a) => { const on = a.dataset.tab === TAB[name]; a.classList.toggle('on', on); if (on) a.setAttribute('aria-current', 'page'); else a.removeAttribute('aria-current'); });
  document.documentElement.dataset.route = name;
  const mod = { s1: () => s1.mount(root, ctx), s2: () => s2.mount(root, ctx), s3: () => s3.mount(root, ctx), s4: () => s4.mount(root, ctx),
    s5: () => s5.mountHistory(root, ctx), receipt: () => s5.mountReceipt(root, ctx, arg), profile: () => profile.mount(root, ctx),
    privacy: () => legal.mount(root, ctx, 'privacy'), terms: () => legal.mount(root, ctx, 'terms') }[name];
  cleanup = (mod || (() => s1.mount(root, ctx)))();
  view.scrollTop = 0;
  if (pendingToast) { toast(pendingToast); pendingToast = null; }
}

/* ---------- нагляд за поїздкою: попередження про залишок, підтвердження закриття ---------- */
let warnBusy = false;
async function watchRide() {
  const s = store.get();
  const r = s.activeRide;
  tg.closing(!!r);
  if (!r || r.warned || warnBusy) return;
  const left = s.wallet.balance - api.runningOf(r).price;
  if (left > CFG.LOW_LEFT) return;
  warnBusy = true;
  api.markWarned();
  tg.haptic.warn();
  const b = await tg.popup({
    title: 'Мало коштів на балансі',
    message: 'На балансі лишилося 20 грн або менше. Поїздка триває, а недостача стане боргом, який спишуть під час наступного поповнення.',
    buttons: [{ id: 'top', type: 'default', text: 'Поповнити' }, { id: 'ok', type: 'cancel', text: 'Закрити' }],
  });
  warnBusy = false;
  if (b === 'top') await openTopUp({ amount: CFG.TOPUPS[0] });
}

function boot() {
  testmode.init();
  tg.init();
  tg.main.mountShim();
  tg.back.mountShim();
  store.init(tg.user());
  const n = store.takeNotice(); if (n) setTimeout(() => toast(n), 300);
  // Telegram дописує в hash параметри запуску (tgWebAppData=...): забираємо їх, лишаємо лише маршрут
  const rawHash = location.hash.slice(1);
  const hashRoute = rawHash.split(/[&?]/)[0];
  const hashSp = new URLSearchParams(rawHash).get('tgWebAppStartParam');
  if (/tgWebApp/.test(rawHash)) {
    history.replaceState(null, '', window.location.pathname + window.location.search + (hashRoute && !hashRoute.includes('=') ? '#' + hashRoute : ''));
  }
  // параметр запуску (?startapp=s3 або start_param) діє, коли в адресі немає власного маршруту
  const sp = tg.startParam() || hashSp;
  if (sp && /^[A-Za-z0-9_-]+$/.test(sp) && START[sp] && !location.hash) history.replaceState(null, '', START[sp]);
  ready = true;
  window.addEventListener('hashchange', route);
  setInterval(watchRide, 1000);
  store.subscribe(watchRide);
  tg.onActivated(watchRide);
  document.addEventListener('visibilitychange', () => { if (!document.hidden) watchRide(); });
  route();
  booting = false;
  watchRide();
}

boot();
