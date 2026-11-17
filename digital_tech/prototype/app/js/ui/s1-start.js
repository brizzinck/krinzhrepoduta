/** S1. Вхід: привітання, згода з умовами, імітація Дія / BankID. */
import { DISCLAIMER } from '../config.js';
import * as store from '../store.js';
import * as api from '../api.js';
import * as tg from '../tg.js';
import { esc, fullTime } from '../format.js';
import { icon, toast } from './components.js';

const METHOD = { DIIA: 'Дія', BANKID: 'BankID' };

export function mount(root, ctx) {
  const s = store.get();
  const u = s.user;
  let busy = false;
  let agreed = !!u.consentAt;

  const render = () => {
    const st = store.get().user;
    const consent = !!st.consentAt;
    const id = st.identity;
    root.innerHTML = `<div class="screen s1" id="s1">
      <div class="hero">
        <div class="logo" aria-hidden="true">${icon('scooter-electric')}</div>
        <h1>Вітаємо, ${esc(st.firstName)}</h1>
        <p class="lead">Самокати й велосипеди кількох операторів на одній карті. Найближчий і найдешевший за кілька дотиків.</p>
      </div>
      <ul class="intro">
        <li>${icon('map')}<span>Транспорт Bolt, Jet і Zelektra на одній карті</span></li>
        <li>${icon('wallet')}<span>Порівняння за ціною, відстанню й зарядом</span></li>
        <li>${icon('lock-open')}<span>Розблокування, поїздка й чек в одному місці</span></li>
      </ul>
      <div class="card consent">
        <label class="check"><input type="checkbox" id="consent" ${agreed ? 'checked' : ''} ${consent ? 'disabled' : ''}>
          <span>Погоджуюсь з <a href="#terms">Умовами використання</a> та <a href="#privacy">Політикою конфіденційності</a></span></label>
        ${consent ? `<p class="muted sm note">Згоду надано ${esc(fullTime(st.consentAt))}</p>` : ''}
      </div>
      <h2 class="sec">Підтвердження особи, імітація</h2>
      
      <div class="idbtns" ${id ? "hidden" : ""}>
        <button type="button" class="btn secondary" data-m="DIIA" ${busy ? 'disabled' : ''}>${busy === 'DIIA' ? '<span class="spin on"></span>' : icon('id')}<span>Підтвердити особу через Дію</span></button>
        <button type="button" class="btn secondary" data-m="BANKID" ${busy ? 'disabled' : ''}>${busy === 'BANKID' ? '<span class="spin on"></span>' : icon('shield-check')}<span>Підтвердити через BankID</span></button>
      </div>
      <div class="idstatus" aria-live="polite">${id ? `<span class="badge success lg">${icon('check')}<span>Особу підтверджено (${METHOD[id.method]})</span></span><span class="muted sm">${esc(fullTime(id.at))}</span>` : ''}</div>
      <p class="disclaimer">${esc(DISCLAIMER)}</p>
    </div>`;
    updateMain();
  };

  const checked = () => agreed;
  function updateMain() {
    tg.main.set({ text: 'Перейти до карти', active: checked(), visible: true, onClick: go });
  }
  async function go() {
    if (!checked()) return;
    await api.acceptTerms();
    tg.haptic.ok();
    ctx.nav('#s3');
  }

  root.addEventListener('change', (e) => { if (e.target.id === 'consent') { agreed = e.target.checked; tg.haptic.select(); updateMain(); } });
  root.addEventListener('click', async (e) => {
    const b = e.target.closest('button[data-m]');
    if (!b || busy) return;
    busy = b.dataset.m;
    render();
    const keepFocus = () => { const n = root.querySelector(`button[data-m="${b.dataset.m}"]`); if (n) n.focus(); };
    try {
      await api.confirmIdentity(b.dataset.m);
      tg.haptic.ok();
    } catch (err) { toast(err.message); tg.haptic.err(); }
    busy = false;
    render();
    keepFocus();
  });
  render();
  tg.back.set(null);
  return () => {};
}
