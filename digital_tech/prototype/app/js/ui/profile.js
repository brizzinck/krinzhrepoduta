/** Профіль: дані, згода, про застосунок; службові елементи лише в режимі тестування. */
import { DISCLAIMER, FIX_MAP_URL, CFG } from '../config.js';
import * as store from '../store.js';
import * as api from '../api.js';
import * as tg from '../tg.js';
import * as testmode from '../testmode.js';
import { fullTime, esc, money } from '../format.js';
import { icon } from './components.js';

const METHOD = { DIIA: 'Дія', BANKID: 'BankID' };
const EVT = {
  CONSENT: 'Згода з умовами', ID_VERIFIED: 'Особу підтверджено', BONUS: 'Стартовий бонус', TOPUP: 'Поповнення', UNLOCK: 'Розблокування',
  UNLOCK_REFUSED: 'Відмова в розблокуванні', LOW_BALANCE: 'Попередження про залишок', RIDE_FINISH: 'Завершення поїздки', CHARGE: 'Списання',
  FLEET_GENERATED: 'Автопарк сформовано', RESET: 'Видалення даних',
};

export function mount(root, ctx) {
  let open = { tools: false, log: false };
  let taps = 0, tapTimer = null;

  const render = () => {
    const s = store.get();
    const u = s.user;
    const log = [...s.events].reverse().slice(0, 30);
    root.innerHTML = `<div class="screen profile">
      <h1>Профіль</h1>
      <section class="card who">
        <div class="avatar" aria-hidden="true">${esc(u.firstName.slice(0, 1).toUpperCase())}</div>
        <div><div class="strong lg">${esc(u.firstName)}</div>
        <div class="muted sm">${u.username ? '@' + esc(u.username) : (u.isGuest ? 'Вхід через браузер' : 'Ім\'я користувача не вказано')}</div>
        ${u.isGuest ? '' : `<div class="muted sm">Telegram id ${u.tgId}</div>`}</div>
      </section>
      <ul class="list kvlist">
        <li><span class="muted">Підтвердження особи</span><span>${u.identity ? `${icon('check', 'okic')} ${METHOD[u.identity.method]}, ${esc(fullTime(u.identity.at))}` : 'Не підтверджено'}</span></li>
        <li><span class="muted">Згода з умовами</span><span>${u.consentAt ? `${icon('check', 'okic')} ${esc(fullTime(u.consentAt))}` : 'Не надано'}</span></li>
        ${u.identity ? '' : '<li><a class="link" href="#s1">Підтвердити особу</a></li>'}
        <li><a class="link row" href="#privacy"><span>Політика конфіденційності</span>${icon('chevron-right')}</a></li>
        <li><a class="link row" href="#terms"><span>Умови використання</span>${icon('chevron-right')}</a></li>
      </ul>
      ${testmode.isTest() ? `<h2 class="sec">Режим тестування</h2>
      <div class="card switchrow">
        <div><div class="strong">Прискорений час ×60</div><div class="muted sm">Одна секунда поїздки рахується як хвилина</div></div>
        <button type="button" role="switch" class="switch" aria-checked="${s.settings.timeFactor === 60}" id="tf" aria-label="Прискорений час ×60"><span></span></button>
      </div>
      <details class="card acc" id="tools" ${open.tools ? 'open' : ''}><summary>Інструменти тестування</summary>
        <div class="tools">
          <div class="muted sm">Баланс гаманця</div>
          <div class="chips">${[9900, 10000, 1500].map((b) => `<button type="button" class="chip" data-bal="${b}">${money(b, { compact: true })}</button>`).join('')}</div>
          <div class="muted sm">Наступне розблокування</div>
          <div class="chips" role="radiogroup" aria-label="Наступне розблокування">
            ${[[null, 'Як зазвичай'], ['REFUSED', 'Оператор відмовить'], ['TAKEN', 'Транспорт зайнято']].map(([v, t]) => `<button type="button" role="radio" aria-checked="${s.settings.failNext === v}" class="chip${s.settings.failNext === v ? ' on' : ''}" data-fail="${v || ''}">${t}</button>`).join('')}
          </div>
        </div>
      </details>
      <details class="card acc" id="log" ${open.log ? 'open' : ''}><summary>Журнал подій</summary>
        <ul class="evlog">${log.map((e) => `<li><span class="muted sm num">${esc(fullTime(e.at))}</span><span>${EVT[e.type] || e.type}</span></li>`).join('') || '<li class="muted">Подій ще немає</li>'}</ul>
      </details>
      <p class="muted sm note">Режим: ${tg.inTelegram() ? 'Telegram' : 'браузер'}. Центр автопарку: ${s.fleet ? esc(s.fleet.centerSource) : 'немає'}.</p>
      <button type="button" class="btn ghost" id="testoff">Вимкнути режим тестування</button>` : ''}
      <button type="button" class="btn ghost danger-b" id="reset">${icon('trash')}<span>Видалити мої дані</span></button>
      <h2 class="sec">Про застосунок</h2>
      <p class="muted sm">Карта © учасники OpenStreetMap, дані доступні за ліцензією ODbL. Позиція користувача не зберігається; на пристрої лишаються округлений центр автопарку й приблизне положення транспорту.</p>
      <p class="sm"><a class="link" href="${FIX_MAP_URL}" data-ext>Виправити карту</a></p>
      ${store.isMemoryOnly() ? '<p class="muted sm">Сховище браузера недоступне, дані зберігаються лише до закриття сторінки.</p>' : ''}
      <p class="disclaimer">${esc(DISCLAIMER)}</p>
      <p class="muted sm version" id="ver" role="button" tabindex="-1">Версія 1.0</p>
    </div>`;
  };

  root.addEventListener('toggle', (e) => { if (e.target.id === 'tools') open.tools = e.target.open; if (e.target.id === 'log') open.log = e.target.open; }, true);
  root.addEventListener('click', async (e) => {
    const t = e.target;
    if (t.closest('#tf')) { api.setTimeFactor(store.get().settings.timeFactor === 60 ? 1 : 60); tg.haptic.select(); render(); root.querySelector('#tf').focus(); return; }
    const bal = t.closest('[data-bal]');
    if (bal) { api.setBalance(Number(bal.dataset.bal)); tg.haptic.select(); ctx.toast(`Баланс: ${money(Number(bal.dataset.bal))}`); render(); return; }
    const fail = t.closest('[data-fail]');
    if (fail) { api.setFailNext(fail.dataset.fail || null); tg.haptic.select(); render(); return; }
    if (t.closest('#reset')) {
      const b = await tg.popup({ title: 'Видалити мої дані?', message: 'Буде видалено згоду, підтвердження особи, гаманець, поїздки й журнал на цьому пристрої. Застосунок почне спочатку.', buttons: [{ id: 'ok', type: 'destructive', text: 'Видалити' }, { id: 'no', type: 'cancel' }] });
      if (b === 'ok') { api.resetAll(tg.user()); tg.haptic.ok(); ctx.nav('#s1', { toast: 'Дані видалено' }); }
      return;
    }
    if (t.closest('#testoff')) { api.setTimeFactor(1); api.setFailNext(null); testmode.set(false); tg.haptic.select(); ctx.toast('Режим тестування вимкнено'); render(); return; }
    if (t.closest('#ver')) {
      taps++; clearTimeout(tapTimer); tapTimer = setTimeout(() => { taps = 0; }, 2500);
      if (taps >= 7 && !testmode.isTest()) { taps = 0; testmode.set(true); tg.haptic.ok(); ctx.toast('Режим тестування увімкнено'); render(); }
      return;
    }
    const ext = t.closest('[data-ext]');
    if (ext) { e.preventDefault(); tg.openLink(ext.href); }
  });
  render();
  tg.back.set(null);
  tg.main.hide();
  const off = store.subscribe(() => { if (!document.querySelector('.modal')) { const y = root.firstElementChild.scrollTop; render(); root.firstElementChild.scrollTop = y; } });
  return off;
}
