/** Профіль: дані, згода, налаштування демо, журнал подій, про карту. */
import { DISCLAIMER, FIX_MAP_URL, CFG } from '../config.js';
import * as store from '../store.js';
import * as api from '../api.js';
import * as tg from '../tg.js';
import { fullTime, esc, money } from '../format.js';
import { icon } from './components.js';

const METHOD = { DIIA: 'Дія', BANKID: 'BankID' };
const EVT = {
  CONSENT: 'Згода з умовами', ID_VERIFIED: 'Особу підтверджено', BONUS: 'Стартовий бонус', TOPUP: 'Поповнення', UNLOCK: 'Розблокування',
  UNLOCK_REFUSED: 'Відмова в розблокуванні', LOW_BALANCE: 'Попередження про залишок', RIDE_FINISH: 'Завершення поїздки', CHARGE: 'Списання',
  FLEET_GENERATED: 'Автопарк сформовано', RESET: 'Скидання демо-даних',
};

export function mount(root, ctx) {
  let open = { tools: false, log: false };

  const render = () => {
    const s = store.get();
    const u = s.user;
    const log = [...s.events].reverse().slice(0, 30);
    root.innerHTML = `<div class="screen profile">
      <h1>Профіль</h1>
      <section class="card who">
        <div class="avatar" aria-hidden="true">${esc(u.firstName.slice(0, 1).toUpperCase())}</div>
        <div><div class="strong lg">${esc(u.firstName)}</div>
        <div class="muted sm">${u.username ? '@' + esc(u.username) : 'Ім\'я користувача не вказано'}</div>
        <div class="muted sm">${u.isGuest ? 'Тестовий користувач' : 'Telegram id ' + u.tgId}</div></div>
      </section>
      <ul class="list kvlist">
        <li><span class="muted">Підтвердження особи</span><span>${u.identity ? `${icon('check', 'okic')} ${METHOD[u.identity.method]}, ${esc(fullTime(u.identity.at))}` : 'Не підтверджено'}</span></li>
        <li><span class="muted">Згода з умовами</span><span>${u.consentAt ? `${icon('check', 'okic')} ${esc(fullTime(u.consentAt))}` : 'Не надано'}</span></li>
        ${u.identity ? '' : '<li><a class="link" href="#s1">Підтвердити особу</a></li>'}
        <li><a class="link row" href="#privacy"><span>Політика конфіденційності</span>${icon('chevron-right')}</a></li>
        <li><a class="link row" href="#terms"><span>Умови використання</span>${icon('chevron-right')}</a></li>
      </ul>
      <h2 class="sec">Демо</h2>
      <div class="card switchrow">
        <div><div class="strong">Демо-час ×60</div><div class="muted sm">Одна секунда поїздки рахується як хвилина</div></div>
        <button type="button" role="switch" class="switch" aria-checked="${s.settings.timeFactor === 60}" id="tf" aria-label="Демо-час ×60"><span></span></button>
      </div>
      <details class="card acc" id="tools" ${open.tools ? 'open' : ''}><summary>Демо-інструменти</summary>
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
      <button type="button" class="btn ghost danger-b" id="reset">${icon('trash')}<span>Скинути демо-дані</span></button>
      <h2 class="sec">Про карту</h2>
      <p class="muted sm">Карта © учасники OpenStreetMap, дані доступні за ліцензією ODbL. Позиція користувача не зберігається; на пристрої лишаються округлений центр автопарку й приблизне положення транспорту.</p>
      <p class="sm"><a class="link" href="${FIX_MAP_URL}" data-ext>Виправити карту</a></p>
      <p class="muted sm">Режим: ${tg.inTelegram() ? 'Telegram' : 'браузер, тестовий користувач'}${store.isMemoryOnly() ? '. Сховище браузера недоступне, дані живуть лише до закриття сторінки.' : ''}</p>
      <p class="disclaimer">${esc(DISCLAIMER)}</p>
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
      const b = await tg.popup({ title: 'Скинути демо-дані?', message: 'Буде видалено згоду, підтвердження особи, гаманець, поїздки й журнал. Застосунок почне спочатку.', buttons: [{ id: 'ok', type: 'destructive', text: 'Скинути' }, { id: 'no', type: 'cancel' }] });
      if (b === 'ok') { api.resetAll(tg.user()); tg.haptic.ok(); ctx.nav('#s1', { toast: 'Демо-дані скинуто' }); }
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
