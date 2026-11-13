'use strict';
/* ZeladoriaCidadã · front-end (JavaScript puro, sem dependências externas). Consome a API REST em /api. */
(function () {
  const API = '/api';
  const ABAS = ['painel', 'ocorrencias', 'registrar', 'cadastros'];
  const ROT = { ABERTA: 'Aberta', EM_ANDAMENTO: 'Em andamento', RESOLVIDA: 'Resolvida', BAIXA: 'Baixa', MEDIA: 'Média', ALTA: 'Alta' };
  const PESO = { ALTA: 3, MEDIA: 2, BAIXA: 1 };
  const NS = 'http://www.w3.org/2000/svg';
  const estado = { categorias: [], regioes: [], ocorrencias: [], carregado: false, aba: 'painel', visao: 'grade',
    filtro: { busca: '', status: '', cat: '', reg: '', ordem: 'recentes' }, cad: 'categorias', enviando: false };
  let uid = 0;

  /* ---------- utilidades ---------- */
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => Array.from(r.querySelectorAll(s));
  function el(tag, attrs, ...filhos) {
    const e = document.createElement(tag);
    Object.entries(attrs || {}).forEach(([k, v]) => { if (v === true) e.setAttribute(k, ''); else if (v !== false && v !== null && v !== undefined) e.setAttribute(k, v); });
    filhos.flat().forEach(f => { if (f !== null && f !== undefined && f !== false) e.append(f.nodeType ? f : document.createTextNode(String(f))); });
    return e;
  }
  // replaceChildren converte null em texto "null"; este auxiliar descarta valores vazios.
  function montar(alvo, ...filhos) { alvo.replaceChildren(...filhos.flat().filter(f => f !== null && f !== undefined && f !== false)); }
  function svg(tag, attrs) { const e = document.createElementNS(NS, tag); Object.entries(attrs || {}).forEach(([k, v]) => e.setAttribute(k, v)); return e; }

  const ICONES = {
    pin: '<path d="M12 21s7-6.2 7-11a7 7 0 1 0-14 0c0 4.8 7 11 7 11z"/><circle cx="12" cy="10" r="2.5"/>',
    road: '<path d="M8 3 4 21M16 3l4 18M12 4v3M12 11v3M12 18v3"/>',
    bulb: '<path d="M9 18h6M10 21h4M12 3a6 6 0 0 0-3.5 10.9c.6.5 1 1.3 1 2.1h5c0-.8.4-1.6 1-2.1A6 6 0 0 0 12 3z"/>',
    trash: '<path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3"/>',
    access: '<circle cx="12" cy="4.5" r="1.8"/><path d="M12 7.5v6h5l2 5M12 10.5H7M10 13.5a4.5 4.5 0 1 0 5 6"/>',
    tree: '<path d="M12 21v-6M12 3 6 11h3l-4 5h14l-4-5h3z"/>',
    sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>',
    moon: '<path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
    refresh: '<path d="M20 11a8 8 0 0 0-14.5-4M4 4v4h4M4 13a8 8 0 0 0 14.5 4M20 20v-4h-4"/>',
    download: '<path d="M12 3v12M7 11l5 5 5-5M5 21h14"/>',
    close: '<path d="M6 6l12 12M18 6 6 18"/>',
    check: '<path d="m5 12 5 5L20 7"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
    grid: '<rect x="4" y="4" width="7" height="7" rx="1"/><rect x="13" y="4" width="7" height="7" rx="1"/><rect x="4" y="13" width="7" height="7" rx="1"/><rect x="13" y="13" width="7" height="7" rx="1"/>',
    list: '<path d="M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    edit: '<path d="M4 20h4L19 9l-4-4L4 16v4zM13.5 6.5l4 4"/>',
    alert: '<path d="M12 3 2 20h20L12 3zM12 10v4M12 17h.01"/>',
    activity: '<path d="M3 12h4l3-8 4 16 3-8h4"/>',
    layers: '<path d="m12 3 9 5-9 5-9-5 9-5zM3 13l9 5 9-5"/>',
    shield: '<path d="M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6l-8-3z"/><path d="m9 12 2 2 4-4"/>',
    'a-menos': '<path d="M4 19 9 6l5 13M6 15h6M17 8h5"/>',
    'a-mais': '<path d="M3 19 8 6l5 13M5 15h6M18 5v6M15 8h6"/>',
    contraste: '<circle cx="12" cy="12" r="9"/><path d="M12 3a9 9 0 0 1 0 18z" fill="currentColor"/>',
  };
  function icone(nome, tam) {
    const s = svg('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round', 'aria-hidden': 'true', class: 'ic' });
    if (tam) { s.setAttribute('width', tam); s.setAttribute('height', tam); }
    s.innerHTML = ICONES[nome] || ICONES.pin; // somente constantes internas, nunca dados do usuário
    return s;
  }
  function iconeCategoria(nome) {
    const n = norm(nome);
    if (n.includes('buraco')) return 'road';
    if (n.includes('ilumin')) return 'bulb';
    if (n.includes('lixo') || n.includes('coleta')) return 'trash';
    if (n.includes('calcad') || n.includes('acessib')) return 'access';
    if (n.includes('poda') || n.includes('arvore')) return 'tree';
    return 'pin';
  }
  function aplicarIcones() { $$('[data-ic]').forEach(n => { if (!n.querySelector(':scope > svg.ic')) n.prepend(icone(n.dataset.ic)); }); }

  const norm = s => String(s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  const fmtData = iso => iso ? new Date(iso).toLocaleDateString('pt-BR') : '–';
  const fmtDataHora = iso => iso ? new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '–';
  const dias = (a, b) => (new Date(b) - new Date(a)) / 86400000;
  const fmtNum = (v, d) => v === null || v === undefined ? '–' : Number(v).toLocaleString('pt-BR', { minimumFractionDigits: d || 0, maximumFractionDigits: d || 0 });
  const fmtDias = d => d < 1 ? Math.max(1, Math.round(d * 24)) + ' h' : fmtNum(d, 1) + ' d';
  const protocolo = id => 'ZC-' + String(id).padStart(5, '0');
  const reduzMovimento = () => window.matchMedia && matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------- preferências (tema, texto, contraste) ---------- */
  const ESCALAS = [0.9, 1, 1.12, 1.25];
  let prefs = { tema: 'dark', escala: 1, contraste: false };
  try { prefs = Object.assign(prefs, JSON.parse(localStorage.getItem('zc:prefs') || '{}')); } catch (e) { /* sem armazenamento */ }
  function aplicarPrefs() {
    const r = document.documentElement;
    r.dataset.tema = prefs.tema;
    r.dataset.contraste = prefs.contraste ? 'alto' : '';
    r.style.setProperty('--escala', prefs.escala);
    $('#b-contraste').setAttribute('aria-pressed', String(prefs.contraste));
    const bt = $('#b-tema'); bt.replaceChildren(icone(prefs.tema === 'dark' ? 'sun' : 'moon'));
    bt.setAttribute('aria-label', prefs.tema === 'dark' ? 'Mudar para o tema claro' : 'Mudar para o tema escuro');
    try { localStorage.setItem('zc:prefs', JSON.stringify(prefs)); } catch (e) { /* ignora */ }
  }
  function mudarEscala(passo) {
    let i = ESCALAS.indexOf(prefs.escala); if (i < 0) i = 1;
    prefs.escala = ESCALAS[Math.max(0, Math.min(ESCALAS.length - 1, i + passo))]; aplicarPrefs();
  }

  /* ---------- API, avisos e status ---------- */
  function erroApi(m, d, s) { const e = new Error(m); e.detalhes = d || []; e.status = s || 0; return e; }
  async function api(caminho, opcoes) {
    let resp;
    try { resp = await fetch(API + caminho, Object.assign({ headers: { 'Content-Type': 'application/json' } }, opcoes)); }
    catch (e) { throw erroApi('Sem conexão com o servidor. Verifique sua internet e tente novamente.'); }
    if (resp.status === 204) return null;
    let corpo = null;
    try { corpo = await resp.json(); } catch (e) { /* sem corpo */ }
    if (!resp.ok) throw erroApi((corpo && corpo.mensagem) || 'Não foi possível concluir a operação.',
      ((corpo && corpo.detalhes) || []).map(d => d.replace(/^[^:]+: /, '')), resp.status);
    return corpo;
  }
  function toast(tipo, msg, detalhes) {
    const fechar = el('button', { type: 'button', 'aria-label': 'Fechar notificação' }, icone('close', 16));
    const t = el('div', { class: 'toast ' + tipo, role: tipo === 'erro' ? 'alert' : 'status' },
      icone(tipo === 'ok' ? 'check' : tipo === 'erro' ? 'alert' : 'activity'),
      el('div', {}, el('b', {}, msg), (detalhes && detalhes.length) ? el('ul', {}, detalhes.map(d => el('li', {}, d))) : null), fechar);
    const rm = () => t.remove(); fechar.addEventListener('click', rm);
    $('#toasts').append(t); setTimeout(rm, tipo === 'erro' ? 9000 : 5000);
  }
  async function pingApi() {
    const ini = performance.now(), pill = $('#api'), txt = $('#api-txt');
    try { const r = await fetch(API + '/categorias', { cache: 'no-store' }); if (!r.ok) throw new Error('status'); pill.dataset.s = 'on'; txt.textContent = 'API online · ' + Math.round(performance.now() - ini) + ' ms'; }
    catch (e) { pill.dataset.s = 'off'; txt.textContent = 'API offline'; }
  }

  /* ---------- prazo (SLA) e componentes visuais ---------- */
  const catDe = o => estado.categorias.find(c => c.id === o.idCategoria);
  const prazoDe = o => { const c = catDe(o); return c ? c.prazoDias : null; };
  function infoPrazo(o) {
    const prazo = prazoDe(o), resolvida = o.status === 'RESOLVIDA';
    const usado = dias(o.dataAbertura, resolvida && o.dataResolucao ? o.dataResolucao : new Date());
    if (prazo === null) return { prazo, usado, pct: 0, tom: 'info', texto: '', atrasada: false };
    const pct = Math.min(100, Math.round(usado / prazo * 100));
    let tom, texto;
    if (resolvida) { tom = usado <= prazo ? 'ok' : 'bad'; texto = 'Resolvida em ' + fmtDias(usado) + ' (prazo ' + prazo + ' d)'; }
    else if (usado > prazo) { tom = 'bad'; texto = 'Atrasada há ' + Math.max(1, Math.ceil(usado - prazo)) + ' d'; }
    else { tom = pct >= 75 ? 'warn' : 'info'; texto = Math.max(0, Math.ceil(prazo - usado)) + ' d restantes de ' + prazo + ' d'; }
    return { prazo, usado, pct, tom, texto, atrasada: !resolvida && usado > prazo };
  }
  const chipStatus = s => el('span', { class: 'chip ' + s }, ROT[s]);
  const chipPrio = p => el('span', { class: 'chip ' + p }, 'Urgência ' + ROT[p].toLowerCase());
  function barraPrazo(o) {
    const ip = infoPrazo(o); if (ip.prazo === null) return null;
    const i = el('i', {}); i.style.width = ip.pct + '%';
    return el('div', { class: 'prazo', 'data-t': ip.tom }, el('small', {}, el('span', {}, 'Prazo da categoria'), el('span', {}, ip.texto)), el('div', { class: 'trilho' }, i));
  }

  /* ---------- painel ---------- */
  function estatisticas() {
    const L = estado.ocorrencias, res = L.filter(o => o.status === 'RESOLVIDA' && o.dataResolucao);
    const dentro = res.filter(o => { const p = prazoDe(o); return p === null || dias(o.dataAbertura, o.dataResolucao) <= p; }).length;
    return { total: L.length, ab: L.filter(o => o.status === 'ABERTA').length, an: L.filter(o => o.status === 'EM_ANDAMENTO').length,
      rs: L.filter(o => o.status === 'RESOLVIDA').length, atrasadas: L.filter(o => infoPrazo(o).atrasada).length,
      media: res.length ? res.reduce((s, o) => s + dias(o.dataAbertura, o.dataResolucao), 0) / res.length : null,
      sla: res.length ? Math.round(dentro / res.length * 100) : null };
  }
  function contar(no, alvo, dec) {
    if (alvo === null || reduzMovimento()) { no.textContent = fmtNum(alvo, dec); return; }
    const t0 = performance.now(), dur = 700;
    (function passo(t) { const k = Math.min(1, (t - t0) / dur); no.textContent = fmtNum(alvo * (1 - Math.pow(1 - k, 3)), dec); if (k < 1) requestAnimationFrame(passo); })(t0);
  }
  function kpi(rotulo, valor, sub, tom, dec) {
    const n = el('strong', {}, '0'); contar(n, valor, dec);
    return el('div', { class: 'kpi painel-vidro', 'data-t': tom }, el('div', { class: 'rotulo' }, rotulo), n, el('span', { class: 'sub' }, sub));
  }
  function donut(partes, total) {
    const R = 52, C = 2 * Math.PI * R, s = svg('svg', { viewBox: '0 0 140 140', class: 'donut', role: 'img', 'aria-label': 'Distribuição das ocorrências por situação' });
    s.append(svg('circle', { cx: 70, cy: 70, r: R, style: 'stroke: var(--surf2)' }));
    let acc = 0;
    partes.forEach(p => {
      const len = total ? p.valor / total * C : 0, c = svg('circle', { cx: 70, cy: 70, r: R, transform: 'rotate(-90 70 70)', 'stroke-dasharray': '0 ' + C, 'stroke-dashoffset': -acc, 'stroke-linecap': 'butt' });
      c.style.stroke = p.cor; s.append(c); acc += len;
      requestAnimationFrame(() => requestAnimationFrame(() => c.setAttribute('stroke-dasharray', len + ' ' + (C - len))));
    });
    const t1 = svg('text', { x: 70, y: 74, 'text-anchor': 'middle', 'font-size': 30 }); t1.textContent = total;
    const t2 = svg('text', { x: 70, y: 94, 'text-anchor': 'middle', 'font-size': 10, style: 'fill: var(--mut); font-weight: 600' }); t2.textContent = 'ocorrências';
    s.append(t1, t2); return s;
  }
  function anel(pct) {
    const R = 52, C = 2 * Math.PI * R, s = svg('svg', { viewBox: '0 0 140 140', class: 'donut', role: 'img', 'aria-label': pct === null ? 'Sem ocorrências resolvidas' : pct + '% resolvidas dentro do prazo' });
    s.append(svg('circle', { cx: 70, cy: 70, r: R, style: 'stroke: var(--surf2)' }));
    const c = svg('circle', { cx: 70, cy: 70, r: R, transform: 'rotate(-90 70 70)', 'stroke-dasharray': '0 ' + C, 'stroke-linecap': 'round' }); c.style.stroke = 'var(--ok)'; s.append(c);
    const len = (pct || 0) / 100 * C; requestAnimationFrame(() => requestAnimationFrame(() => c.setAttribute('stroke-dasharray', len + ' ' + (C - len))));
    const t = svg('text', { x: 70, y: 78, 'text-anchor': 'middle', 'font-size': 28 }); t.textContent = pct === null ? '–' : pct + '%'; s.append(t); return s;
  }
  function barras(dados) {
    const max = Math.max(1, ...dados.map(d => d.valor));
    return el('div', { class: 'barras' }, dados.map(d => {
      const i = el('i', {}); requestAnimationFrame(() => requestAnimationFrame(() => { i.style.width = (d.valor / max * 100) + '%'; }));
      return el('div', { class: 'barra-l' }, el('span', { title: d.rotulo }, d.rotulo), el('div', { class: 'trilho' }, i), el('b', {}, d.valor));
    }));
  }
  function renderPainel() {
    const e = estatisticas();
    montar($('#kpis'),
      kpi('Total registrado', e.total, 'ocorrências no bairro', 'info'),
      kpi('Abertas', e.ab, 'aguardando atendimento', 'warn'),
      kpi('Em andamento', e.an, 'equipe atuando', 'info'),
      kpi('Resolvidas', e.rs, 'problemas solucionados', 'ok'));
    const cores = { ABERTA: 'var(--warn)', EM_ANDAMENTO: 'var(--info)', RESOLVIDA: 'var(--ok)' };
    const partes = ['ABERTA', 'EM_ANDAMENTO', 'RESOLVIDA'].map(s => ({ rotulo: ROT[s], cor: cores[s], valor: estado.ocorrencias.filter(o => o.status === s).length }));
    montar($('#graficos-1'),
      el('div', { class: 'card pad' }, el('div', { class: 'rotulo' }, 'Situação das ocorrências'),
        el('div', { class: 'donut-wrap', style: 'margin-top:14px' }, donut(partes, e.total),
          el('ul', { class: 'leg' }, partes.map(p => { const i = el('i', {}); i.style.background = p.cor; return el('li', {}, i, p.rotulo, el('b', {}, p.valor)); })))),
      el('div', { class: 'card pad' }, el('div', { class: 'rotulo' }, 'Indicadores de impacto'),
        el('div', { class: 'donut-wrap', style: 'margin-top:14px' }, anel(e.sla),
          el('div', {}, el('div', { class: 'rotulo' }, 'Dentro do prazo'), el('p', { style: 'margin:2px 0 12px;color:var(--mut);font-size:.88rem' }, 'Percentual das resolvidas que cumpriram o prazo da categoria.'),
            el('div', { class: 'rotulo' }, 'Tempo médio de resolução'), el('strong', { style: 'font-size:1.6rem' }, e.media === null ? '–' : fmtDias(e.media)),
            el('div', { style: 'margin-top:8px' }, el('span', { class: 'chip ' + (e.atrasadas ? 'bad' : 'ok') }, e.atrasadas ? e.atrasadas + ' em atraso' : 'Nenhuma em atraso'))))));
    const porCat = estado.categorias.map(c => ({ rotulo: c.nome, valor: estado.ocorrencias.filter(o => o.idCategoria === c.id).length })).sort((a, b) => b.valor - a.valor);
    const porReg = estado.regioes.map(r => ({ rotulo: r.nome, valor: estado.ocorrencias.filter(o => o.idRegiao === r.id).length })).sort((a, b) => b.valor - a.valor);
    montar($('#graficos-2'),
      el('div', { class: 'card pad' }, el('div', { class: 'rotulo' }, 'Ocorrências por tipo de problema'), barras(porCat)),
      el('div', { class: 'card pad' }, el('div', { class: 'rotulo' }, 'Ocorrências por local'), barras(porReg)));
    const urg = estado.ocorrencias.filter(o => o.status !== 'RESOLVIDA')
      .sort((a, b) => (PESO[b.prioridade] - PESO[a.prioridade]) || (new Date(a.dataAbertura) - new Date(b.dataAbertura))).slice(0, 5);
    montar($('#graficos-3'), el('div', { class: 'card pad' }, el('div', { class: 'rotulo' }, 'Prioridades em aberto'),
      urg.length ? el('ul', { class: 'lista-urg' }, urg.map(o => {
        const li = el('li', {}, chipPrio(o.prioridade), el('span', { class: 't' }, o.titulo), el('span', { class: 'proto' }, protocolo(o.id)), chipStatus(o.status));
        li.addEventListener('click', () => { location.hash = '#ocorrencia/' + o.id; }); return li;
      })) : el('p', { class: 'vazio', style: 'margin-top:10px' }, 'Nenhuma ocorrência em aberto. Bom trabalho!')));
  }

  /* ---------- lista de ocorrências ---------- */
  const nomePublico = nome => { const p = String(nome || '').trim().split(/\s+/); return p.length > 1 ? p[0] + ' ' + p[p.length - 1][0] + '.' : (p[0] || '–'); };
  const restante = o => { if (o.status === 'RESOLVIDA') return Infinity; const p = prazoDe(o); return p === null ? Infinity : p - dias(o.dataAbertura, new Date()); };
  function filtradas() {
    const f = estado.filtro, b = norm(f.busca);
    const L = estado.ocorrencias.filter(o => (!f.status || o.status === f.status) && (!f.cat || String(o.idCategoria) === f.cat) && (!f.reg || String(o.idRegiao) === f.reg)
      && (!b || norm([o.titulo, o.descricao, o.nomeCategoria, o.nomeRegiao, protocolo(o.id)].join(' ')).includes(b)));
    const por = {
      recentes: (a, c) => new Date(c.dataAbertura) - new Date(a.dataAbertura),
      antigas: (a, c) => new Date(a.dataAbertura) - new Date(c.dataAbertura),
      prioridade: (a, c) => (PESO[c.prioridade] - PESO[a.prioridade]) || (new Date(a.dataAbertura) - new Date(c.dataAbertura)),
      prazo: (a, c) => { const x = restante(a), y = restante(c); return x === y ? 0 : (x < y ? -1 : 1); },
    };
    return L.sort(por[f.ordem] || por.recentes);
  }
  const abrir = id => { location.hash = '#ocorrencia/' + id; };
  function cartao(o) {
    const ip = infoPrazo(o);
    const ver = el('button', { type: 'button', class: 'btn peq', 'aria-label': 'Ver detalhes de ' + o.titulo }, 'Ver detalhes');
    ver.addEventListener('click', ev => { ev.stopPropagation(); abrir(o.id); });
    const a = el('article', { class: 'card oc', 'data-prio': o.prioridade },
      el('div', { style: 'display:flex;justify-content:space-between;gap:8px;align-items:center' }, el('span', { class: 'proto' }, protocolo(o.id)), chipStatus(o.status)),
      el('h3', {}, o.titulo),
      el('div', { class: 'meta' }, el('span', {}, icone(iconeCategoria(o.nomeCategoria)), o.nomeCategoria), el('span', {}, icone('pin'), o.nomeRegiao), el('span', {}, icone('clock'), fmtData(o.dataAbertura))),
      el('div', { class: 'chips' }, chipPrio(o.prioridade), ip.atrasada ? el('span', { class: 'chip bad' }, 'Fora do prazo') : null),
      barraPrazo(o), el('div', { style: 'margin-top:8px' }, ver));
    a.addEventListener('click', () => abrir(o.id)); return a;
  }
  function tabela(L) {
    const cab = ['Protocolo', 'Título', 'Tipo', 'Local', 'Situação', 'Urgência', 'Prazo', 'Aberta em', ''];
    const linhas = L.map(o => {
      const ip = infoPrazo(o), b = el('button', { type: 'button', class: 'btn peq', 'aria-label': 'Abrir ' + o.titulo }, 'Abrir');
      b.addEventListener('click', ev => { ev.stopPropagation(); abrir(o.id); });
      const tr = el('tr', { class: 'clic' }, el('td', {}, el('span', { class: 'proto' }, protocolo(o.id))), el('td', { style: 'font-weight:600;min-width:180px' }, o.titulo), el('td', {}, o.nomeCategoria),
        el('td', {}, o.nomeRegiao), el('td', {}, chipStatus(o.status)), el('td', {}, chipPrio(o.prioridade)),
        el('td', {}, ip.prazo === null ? '–' : el('span', { class: 'chip ' + ip.tom }, ip.texto)), el('td', {}, fmtData(o.dataAbertura)), el('td', {}, b));
      tr.addEventListener('click', () => abrir(o.id)); return tr;
    });
    return el('div', { class: 'tabela-wrap' }, el('table', { class: 'tb' }, el('thead', {}, el('tr', {}, cab.map(c => el('th', { scope: 'col' }, c)))), el('tbody', {}, linhas)));
  }
  function renderLista() {
    const cont = s => estado.ocorrencias.filter(o => !s || o.status === s).length;
    montar($('#chips-status'), [['', 'Todas'], ['ABERTA', 'Abertas'], ['EM_ANDAMENTO', 'Em andamento'], ['RESOLVIDA', 'Resolvidas']].map(([v, t]) => {
      const b = el('button', { type: 'button', 'aria-pressed': String(estado.filtro.status === v) }, t + ' (' + cont(v) + ')');
      b.addEventListener('click', () => { estado.filtro.status = v; renderLista(); }); return b;
    }));
    const L = filtradas();
    $('#contagem').textContent = L.length === 1 ? '1 ocorrência encontrada' : L.length + ' ocorrências encontradas';
    $('#v-grade').setAttribute('aria-pressed', String(estado.visao === 'grade')); $('#v-tabela').setAttribute('aria-pressed', String(estado.visao === 'tabela'));
    if (!L.length) { montar($('#lista'), el('div', { class: 'vazio' }, 'Nenhuma ocorrência encontrada com esses filtros.')); return; }
    montar($('#lista'), estado.visao === 'grade' ? el('div', { class: 'grade g2' }, L.map(cartao)) : tabela(L));
  }
  function preencherFiltros() {
    const op = (v, t) => el('option', { value: v }, t);
    montar($('#f-cat'), op('', 'Todos os tipos'), estado.categorias.map(c => op(c.id, c.nome)));
    montar($('#f-reg'), op('', 'Todos os locais'), estado.regioes.map(r => op(r.id, r.nome)));
    $('#f-cat').value = estado.filtro.cat; $('#f-reg').value = estado.filtro.reg;
  }
  function exportarCSV() {
    const L = filtradas(), cab = ['Protocolo', 'Título', 'Tipo', 'Local', 'Situação', 'Urgência', 'Aberta em', 'Resolvida em', 'Registrada por'];
    const seguro = v => { let s = String(v === null || v === undefined ? '' : v); if (/^[=+\-@]/.test(s)) s = "'" + s; return '"' + s.replace(/"/g, '""') + '"'; };
    const linhas = L.map(o => [protocolo(o.id), o.titulo, o.nomeCategoria, o.nomeRegiao, ROT[o.status], ROT[o.prioridade], fmtDataHora(o.dataAbertura), o.dataResolucao ? fmtDataHora(o.dataResolucao) : '', nomePublico(o.nomeCidadao)]);
    const csv = '\ufeff' + [cab].concat(linhas).map(l => l.map(seguro).join(';')).join('\r\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
    const a = el('a', { href: url, download: 'ocorrencias-santana-' + new Date().toISOString().slice(0, 10) + '.csv' });
    document.body.append(a); a.click(); a.remove(); setTimeout(() => URL.revokeObjectURL(url), 1500);
    toast('ok', L.length + (L.length === 1 ? ' ocorrência exportada.' : ' ocorrências exportadas.'));
  }

  /* ---------- registrar ocorrência ---------- */
  const maskTel = v => { const d = v.replace(/\D/g, '').slice(0, 11); if (!d) return ''; if (d.length <= 2) return '(' + d; if (d.length <= 6) return '(' + d.slice(0, 2) + ') ' + d.slice(2);
    return d.length <= 10 ? '(' + d.slice(0, 2) + ') ' + d.slice(2, 6) + '-' + d.slice(6) : '(' + d.slice(0, 2) + ') ' + d.slice(2, 7) + '-' + d.slice(7); };
  const maskCep = v => { const d = v.replace(/\D/g, '').slice(0, 8); return d.length > 5 ? d.slice(0, 5) + '-' + d.slice(5) : d; };
  const valorRadio = nome => { const r = $('input[name="' + nome + '"]:checked'); return r ? r.value : ''; };
  function montarFormRegistro() {
    montar($('#r-cats'), estado.categorias.map(c => el('label', { class: 'cat' },
      el('input', { type: 'radio', name: 'cat', value: String(c.id) }),
      el('div', {}, icone(iconeCategoria(c.nome)), el('b', {}, c.nome), el('small', {}, 'Prazo: ' + c.prazoDias + ' dias')))));
    $$('#r-cats input').forEach(i => i.addEventListener('change', () => { $('#e-cat').textContent = ''; atualizarPrevia(); }));
    montar($('#r-reg'), el('option', { value: '' }, 'Selecione o local…'), estado.regioes.map(r => el('option', { value: r.id }, r.nome)));
    try { const m = JSON.parse(localStorage.getItem('zc:id') || 'null'); if (m) { $('#r-nome').value = m.nome || ''; $('#r-email').value = m.email || ''; $('#r-tel').value = m.tel || ''; $('#r-lembrar').checked = true; } } catch (e) { /* ignora */ }
    atualizarPrevia();
  }
  function atualizarPrevia() {
    const catId = valorRadio('cat'), c = estado.categorias.find(x => String(x.id) === catId), r = estado.regioes.find(x => String(x.id) === $('#r-reg').value), urg = valorRadio('urg') || 'MEDIA';
    $('#n-titulo').textContent = $('#r-titulo').value.length + '/120'; $('#n-desc').textContent = $('#r-desc').value.length + '/500';
    montar($('#previa'), el('span', { class: 'proto' }, 'ZC-NOVO'), el('h3', { style: 'font-size:1.05rem' }, $('#r-titulo').value.trim() || 'O resumo do problema aparece aqui'),
      el('div', { class: 'meta', style: 'display:flex;flex-wrap:wrap;gap:4px 14px;color:var(--mut);font-size:.84rem;margin:8px 0' },
        el('span', {}, icone(c ? iconeCategoria(c.nome) : 'pin'), c ? c.nome : 'Tipo não escolhido'), el('span', {}, icone('pin'), r ? r.nome : 'Local não escolhido')),
      el('div', { class: 'chips', style: 'display:flex;gap:6px;flex-wrap:wrap' }, chipStatus('ABERTA'), chipPrio(urg)),
      c ? el('p', { style: 'color:var(--mut);font-size:.84rem;margin:10px 0 0' }, 'Prazo padrão de atendimento: ' + c.prazoDias + ' dias.') : null);
  }
  function erroCampo(id, msg) { const e = $('#' + id); e.textContent = msg || ''; const campo = e.closest('.campo'); const inp = campo && $('input, select, textarea', campo); if (inp) inp.setAttribute('aria-invalid', msg ? 'true' : 'false'); return !!msg; }
  function validarRegistro() {
    const nome = $('#r-nome').value.trim(), email = $('#r-email').value.trim(), tel = $('#r-tel').value.replace(/\D/g, '');
    const falhas = [
      erroCampo('e-nome', nome.length < 3 ? 'Informe seu nome (mínimo 3 letras).' : ''),
      erroCampo('e-email', !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email) ? 'Informe um e-mail válido, como voce@exemplo.com.' : ''),
      erroCampo('e-tel', tel && tel.length < 10 ? 'Informe DDD + número (10 ou 11 dígitos) ou deixe em branco.' : ''),
      (() => { const f = !valorRadio('cat'); $('#e-cat').textContent = f ? 'Escolha o tipo de problema.' : ''; return f; })(),
      erroCampo('e-reg', !$('#r-reg').value ? 'Escolha onde fica o problema.' : ''),
      erroCampo('e-titulo', !$('#r-titulo').value.trim() ? 'Escreva um resumo do problema.' : ''),
    ];
    if (falhas.some(Boolean)) { const p = $('[aria-invalid="true"]', $('#form-reg')); if (p) p.focus(); else $('#r-cats input').focus(); return null; }
    return { nome, email, tel };
  }
  function mostrarSucesso(o) {
    const nova = el('button', { type: 'button', class: 'btn' }, 'Registrar outra');
    const ver = el('button', { type: 'button', class: 'btn pri' }, 'Acompanhar ocorrência');
    ver.addEventListener('click', () => abrir(o.id));
    nova.addEventListener('click', () => { $('#sucesso').hidden = true; $('#form-reg').hidden = false; atualizarPrevia(); $('#r-titulo').focus(); });
    montar($('#sucesso'), el('div', { class: 'sel-ok' }, icone('check')), el('h2', {}, 'Ocorrência registrada!'),
      el('p', { style: 'color:var(--mut)' }, 'Guarde o protocolo para acompanhar o andamento:'), el('div', { class: 'proto' }, protocolo(o.id)),
      el('p', { style: 'color:var(--mut);font-size:.9rem' }, o.titulo + ' · ' + o.nomeCategoria + ' · ' + o.nomeRegiao), el('div', { class: 'acoes' }, ver, nova));
    $('#form-reg').hidden = true; $('#sucesso').hidden = false; window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  async function enviarRegistro(ev) {
    ev.preventDefault(); if (estado.enviando) return;
    const v = validarRegistro(); if (!v) return;
    const btn = $('#r-enviar'); estado.enviando = true; btn.disabled = true; montar(btn, el('span', { class: 'spin' }), ' Enviando…');
    try {
      const cid = await api('/cidadaos/identificar', { method: 'POST', body: JSON.stringify({ nome: v.nome, email: v.email, telefone: v.tel, tipo: 'MORADOR' }) });
      const oc = await api('/ocorrencias', { method: 'POST', body: JSON.stringify({ titulo: $('#r-titulo').value.trim(), descricao: $('#r-desc').value.trim(), prioridade: valorRadio('urg') || 'MEDIA',
        idCidadao: cid.id, idCategoria: Number(valorRadio('cat')), idRegiao: Number($('#r-reg').value) }) });
      try { if ($('#r-lembrar').checked) localStorage.setItem('zc:id', JSON.stringify({ nome: v.nome, email: v.email, tel: $('#r-tel').value })); else localStorage.removeItem('zc:id'); } catch (e) { /* ignora */ }
      ['r-titulo', 'r-desc'].forEach(i => { $('#' + i).value = ''; }); $$('input[name="cat"]').forEach(i => { i.checked = false; }); $('#r-reg').value = ''; $('input[name="urg"][value="MEDIA"]').checked = true;
      if (!$('#r-lembrar').checked) { ['r-nome', 'r-email', 'r-tel'].forEach(i => { $('#' + i).value = ''; }); }
      mostrarSucesso(oc); toast('ok', 'Ocorrência ' + protocolo(oc.id) + ' registrada.'); await carregarOcorrencias(true);
    } catch (e) { toast('erro', e.message, e.detalhes); }
    finally { estado.enviando = false; btn.disabled = false; montar(btn, 'Enviar ocorrência'); }
  }

  /* ---------- componentes de formulário (drawer e cadastros) ---------- */
  function campo(o) {
    const id = 'c' + (++uid);
    const ent = o.tipo === 'textarea' ? el('textarea', { id, rows: o.linhas || 3 }) : o.tipo === 'select' ? el('select', { id }) : el('input', { id, type: o.tipo || 'text' });
    if (o.max) ent.setAttribute('maxlength', o.max); if (o.min !== undefined) ent.setAttribute('min', o.min); if (o.maxn !== undefined) ent.setAttribute('max', o.maxn);
    if (o.ph) ent.setAttribute('placeholder', o.ph);
    const erro = el('div', { class: 'erro-campo', id: id + '-e', role: 'alert' }); ent.setAttribute('aria-describedby', id + '-e');
    const no = el('div', { class: 'campo' }, el('label', { for: id }, o.rotulo, o.req ? el('span', { class: 'obrig', 'aria-hidden': 'true' }, ' *') : null), ent, erro);
    const marcar = m => { erro.textContent = m || ''; ent.setAttribute('aria-invalid', m ? 'true' : 'false'); return !!m; };
    ['input', 'change'].forEach(ev => ent.addEventListener(ev, () => marcar('')));
    return { no, ent, erro: marcar };
  }
  const opcoes = (lista, rot, valor) => lista.map(x => el('option', { value: x.id }, rot(x)));

  /* ---------- detalhe da ocorrência (painel lateral) ---------- */
  function conteudoDetalhe(o, hist) {
    const dlg = $('#drawer'), ip = infoPrazo(o), resolvida = o.status === 'RESOLVIDA';
    const fechar = el('button', { type: 'button', class: 'ib', 'aria-label': 'Fechar detalhes' }, icone('close')); fechar.addEventListener('click', () => dlg.close());
    const linha = el('ol', { class: 'linha' }, hist.length ? hist.map(h => el('li', {}, chipStatus(h.status), el('time', {}, fmtDataHora(h.dataRegistro)), el('p', {}, h.comentario))) : [el('li', {}, 'Ainda sem registros de andamento.')]);

    const fa = { st: campo({ tipo: 'select', rotulo: 'Nova situação' }), tx: campo({ tipo: 'textarea', rotulo: 'O que aconteceu?', max: 300, req: true, ph: 'Descreva a ação realizada ou o andamento.' }) };
    const lista = resolvida ? ['RESOLVIDA'] : ['ABERTA', 'EM_ANDAMENTO', 'RESOLVIDA'];
    montar(fa.st.ent, lista.map(s => el('option', { value: s }, ROT[s]))); fa.st.ent.value = o.status;
    const bAnd = el('button', { type: 'submit', class: 'btn pri' }, 'Registrar andamento');
    const formAnd = el('form', { novalidate: true }, fa.st.no, fa.tx.no, bAnd);
    formAnd.addEventListener('submit', async ev => {
      ev.preventDefault(); if (fa.tx.erro(!fa.tx.ent.value.trim() ? 'Descreva o que aconteceu.' : '')) { fa.tx.ent.focus(); return; }
      bAnd.disabled = true;
      try { await api('/ocorrencias/' + o.id + '/acompanhamentos', { method: 'POST', body: JSON.stringify({ comentario: fa.tx.ent.value.trim(), status: fa.st.ent.value }) });
        toast('ok', 'Andamento registrado.'); await carregarOcorrencias(true); await abrirDetalhe(o.id); }
      catch (e) { toast('erro', e.message, e.detalhes); bAnd.disabled = false; }
    });

    const fe = { t: campo({ rotulo: 'Resumo', max: 120, req: true }), d: campo({ tipo: 'textarea', rotulo: 'Detalhes', max: 500 }), p: campo({ tipo: 'select', rotulo: 'Urgência' }),
      c: campo({ tipo: 'select', rotulo: 'Tipo de problema' }), r: campo({ tipo: 'select', rotulo: 'Local' }) };
    fe.t.ent.value = o.titulo; fe.d.ent.value = o.descricao || '';
    montar(fe.p.ent, ['BAIXA', 'MEDIA', 'ALTA'].map(p => el('option', { value: p }, ROT[p]))); fe.p.ent.value = o.prioridade;
    montar(fe.c.ent, opcoes(estado.categorias, c => c.nome)); fe.c.ent.value = String(o.idCategoria);
    montar(fe.r.ent, opcoes(estado.regioes, r => r.nome)); fe.r.ent.value = String(o.idRegiao);
    const bSalvar = el('button', { type: 'submit', class: 'btn pri' }, 'Salvar alterações'), bCancel = el('button', { type: 'button', class: 'btn' }, 'Cancelar');
    const formEd = el('form', { novalidate: true, hidden: true, class: 'card pad', style: 'margin-top:12px' }, fe.t.no, fe.d.no, el('div', { class: 'linha2' }, fe.p.no, fe.c.no), fe.r.no, el('div', { class: 'acoes' }, bSalvar, bCancel));
    const bEditar = el('button', { type: 'button', class: 'btn peq' }, icone('edit'), 'Editar dados');
    bEditar.addEventListener('click', () => { formEd.hidden = !formEd.hidden; }); bCancel.addEventListener('click', () => { formEd.hidden = true; });
    formEd.addEventListener('submit', async ev => {
      ev.preventDefault(); if (fe.t.erro(!fe.t.ent.value.trim() ? 'O resumo é obrigatório.' : '')) { fe.t.ent.focus(); return; }
      bSalvar.disabled = true;
      try { await api('/ocorrencias/' + o.id, { method: 'PUT', body: JSON.stringify({ titulo: fe.t.ent.value.trim(), descricao: fe.d.ent.value.trim(), prioridade: fe.p.ent.value, status: o.status,
          idCidadao: o.idCidadao, idCategoria: Number(fe.c.ent.value), idRegiao: Number(fe.r.ent.value) }) });
        toast('ok', 'Ocorrência atualizada.'); await carregarOcorrencias(true); await abrirDetalhe(o.id); }
      catch (e) { toast('erro', e.message, e.detalhes); bSalvar.disabled = false; }
    });

    const bExc = el('button', { type: 'button', class: 'btn peq perigo' }, icone('alert'), 'Excluir ocorrência'), zona = el('div', { class: 'acoes' }, bExc);
    bExc.addEventListener('click', () => {
      const ok = el('button', { type: 'button', class: 'btn peq perigo' }, 'Confirmar exclusão'), nao = el('button', { type: 'button', class: 'btn peq' }, 'Cancelar');
      montar(zona, el('span', { style: 'color:var(--mut);font-size:.88rem;align-self:center' }, 'Isso apaga também o histórico.'), ok, nao); ok.focus();
      nao.addEventListener('click', () => montar(zona, bExc));
      ok.addEventListener('click', async () => { ok.disabled = true;
        try { await api('/ocorrencias/' + o.id, { method: 'DELETE' }); dlg.close(); toast('ok', 'Ocorrência excluída.'); await carregarOcorrencias(true); }
        catch (e) { toast('erro', e.message, e.detalhes); montar(zona, bExc); } });
    });

    return el('div', { class: 'dr-in' },
      el('div', { class: 'dr-topo' }, el('div', {}, el('span', { class: 'proto' }, protocolo(o.id)), el('h2', { id: 'dr-titulo' }, o.titulo)), fechar),
      el('div', { class: 'chips', style: 'display:flex;gap:6px;flex-wrap:wrap;margin-top:10px' }, chipStatus(o.status), chipPrio(o.prioridade)), barraPrazo(o),
      el('div', { class: 'info-g' },
        el('div', {}, el('small', {}, 'Tipo'), el('span', {}, o.nomeCategoria)), el('div', {}, el('small', {}, 'Local'), el('span', {}, o.nomeRegiao)),
        el('div', {}, el('small', {}, 'Registrada por'), el('span', {}, nomePublico(o.nomeCidadao))), el('div', {}, el('small', {}, 'Aberta em'), el('span', {}, fmtDataHora(o.dataAbertura))),
        resolvida ? el('div', {}, el('small', {}, 'Resolvida em'), el('span', {}, fmtDataHora(o.dataResolucao))) : null,
        ip.prazo !== null ? el('div', {}, el('small', {}, 'Prazo padrão'), el('span', {}, ip.prazo + ' dias')) : null),
      o.descricao ? el('p', {}, o.descricao) : null,
      el('div', { class: 'sec' }, el('h3', {}, icone('clock'), 'Histórico'), linha),
      el('div', { class: 'sec' }, el('h3', {}, icone('activity'), 'Registrar andamento'), resolvida ? el('p', { style: 'color:var(--mut);font-size:.88rem' }, 'Esta ocorrência foi resolvida e não pode ser reaberta; ainda é possível registrar comentários.') : null, formAnd),
      el('div', { class: 'sec' }, el('h3', {}, icone('edit'), 'Gerenciar'), el('div', { class: 'acoes', style: 'margin-top:0' }, bEditar), formEd),
      el('div', { class: 'sec' }, zona));
  }
  async function abrirDetalhe(id) {
    const dlg = $('#drawer');
    try {
      const [o, hist] = await Promise.all([api('/ocorrencias/' + id), api('/ocorrencias/' + id + '/acompanhamentos')]);
      montar(dlg, conteudoDetalhe(o, hist)); if (!dlg.open) dlg.showModal();
    } catch (e) { toast('erro', e.message, e.detalhes); if (location.hash.indexOf('#ocorrencia/') === 0) history.replaceState(null, '', '#' + estado.aba); }
  }

  /* ---------- cadastros (categorias e regiões) ---------- */
  const CAD = {
    categorias: { rota: '/categorias', nome: 'categoria', novo: 'Nova categoria',
      cols: [['Tipo', c => c.nome], ['Descrição', c => c.descricao || '–'], ['Prazo padrão', c => c.prazoDias + ' dias']],
      campos: [{ k: 'nome', rotulo: 'Nome', max: 60, req: true }, { k: 'descricao', rotulo: 'Descrição', max: 200 }, { k: 'prazoDias', rotulo: 'Prazo padrão (dias)', tipo: 'number', min: 1, maxn: 365, req: true }] },
    regioes: { rota: '/regioes', nome: 'região', novo: 'Nova região',
      cols: [['Local', r => r.nome], ['CEP', r => r.cep]],
      campos: [{ k: 'nome', rotulo: 'Nome do local', max: 80, req: true }, { k: 'cep', rotulo: 'CEP', max: 9, req: true, ph: '00000-000', mascara: 'cep' }] },
  };
  function renderCadastros() {
    const cfg = CAD[estado.cad], dados = estado[estado.cad];
    $('#cad-categorias').setAttribute('aria-pressed', String(estado.cad === 'categorias')); $('#cad-regioes').setAttribute('aria-pressed', String(estado.cad === 'regioes'));
    $('#t-novo-cad').textContent = cfg.novo;
    const linhas = dados.map(item => {
      const cel = el('td', { style: 'white-space:nowrap' }), bE = el('button', { type: 'button', class: 'btn peq', 'aria-label': 'Editar ' + item.nome }, icone('edit'), 'Editar'), bX = el('button', { type: 'button', class: 'btn peq perigo', 'aria-label': 'Excluir ' + item.nome }, 'Excluir');
      const padrao = () => montar(cel, bE, ' ', bX);
      bE.addEventListener('click', () => formCadastro(item));
      bX.addEventListener('click', () => { const ok = el('button', { type: 'button', class: 'btn peq perigo' }, 'Confirmar'), nao = el('button', { type: 'button', class: 'btn peq' }, 'Cancelar');
        montar(cel, ok, ' ', nao); ok.focus(); nao.addEventListener('click', padrao);
        ok.addEventListener('click', async () => { ok.disabled = true;
          try { await api(cfg.rota + '/' + item.id, { method: 'DELETE' }); toast('ok', 'Cadastro excluído.'); await carregarReferencias(); renderAtual(); }
          catch (e) { toast('erro', e.message, e.detalhes); padrao(); } }); });
      padrao(); return el('tr', {}, cfg.cols.map(c => el('td', {}, c[1](item))), cel);
    });
    montar($('#cad-lista'), dados.length ? el('div', { class: 'tabela-wrap' }, el('table', { class: 'tb' },
      el('thead', {}, el('tr', {}, cfg.cols.map(c => el('th', { scope: 'col' }, c[0])), el('th', { scope: 'col' }, 'Ações'))), el('tbody', {}, linhas))) : el('div', { class: 'vazio' }, 'Nenhum cadastro ainda.'));
  }
  function formCadastro(item) {
    const cfg = CAD[estado.cad], modal = $('#modal'), cs = cfg.campos.map(c => Object.assign({ c }, campo(c)));
    cs.forEach(x => { if (item) x.ent.value = item[x.c.k] === null || item[x.c.k] === undefined ? '' : item[x.c.k]; if (x.c.mascara === 'cep') x.ent.addEventListener('input', () => { x.ent.value = maskCep(x.ent.value); }); });
    const salvar = el('button', { type: 'submit', class: 'btn pri' }, item ? 'Salvar alterações' : 'Cadastrar'), cancelar = el('button', { type: 'button', class: 'btn' }, 'Cancelar');
    const form = el('form', { novalidate: true }, el('h2', { id: 'md-titulo' }, item ? 'Editar ' + cfg.nome : cfg.novo), cs.map(x => x.no), el('div', { class: 'acoes' }, salvar, cancelar));
    cancelar.addEventListener('click', () => modal.close());
    form.addEventListener('submit', async ev => {
      ev.preventDefault();
      const falhas = cs.map(x => x.erro(x.c.req && !String(x.ent.value).trim() ? 'Campo obrigatório.' : (x.c.tipo === 'number' && x.ent.value && (Number(x.ent.value) < x.c.min || Number(x.ent.value) > x.c.maxn) ? 'Informe um valor entre ' + x.c.min + ' e ' + x.c.maxn + '.' : '')));
      if (falhas.some(Boolean)) { cs[falhas.indexOf(true)].ent.focus(); return; }
      const corpo = {}; cs.forEach(x => { corpo[x.c.k] = x.c.tipo === 'number' ? Number(x.ent.value) : x.ent.value.trim(); });
      salvar.disabled = true;
      try { await api(cfg.rota + (item ? '/' + item.id : ''), { method: item ? 'PUT' : 'POST', body: JSON.stringify(corpo) });
        modal.close(); toast('ok', item ? 'Cadastro atualizado.' : 'Cadastro criado.'); await carregarReferencias(); renderAtual(); }
      catch (e) { toast('erro', e.message, e.detalhes); salvar.disabled = false; }
    });
    montar(modal, form); modal.showModal(); cs[0].ent.focus();
  }

  /* ---------- navegação e inicialização ---------- */
  function renderAtual() {
    if (!estado.carregado) return;
    if (estado.aba === 'painel') renderPainel(); else if (estado.aba === 'ocorrencias') renderLista(); else if (estado.aba === 'registrar') atualizarPrevia(); else if (estado.aba === 'cadastros') renderCadastros();
  }
  function mostrarAba(aba) {
    estado.aba = aba; ABAS.forEach(a => { $('#aba-' + a).hidden = a !== aba; });
    $$('#nav a').forEach(a => { if (a.dataset.aba === aba) a.setAttribute('aria-current', 'page'); else a.removeAttribute('aria-current'); });
    const nomes = { painel: 'Painel', ocorrencias: 'Ocorrências', registrar: 'Registrar', cadastros: 'Cadastros' };
    document.title = nomes[aba] + ' · ZeladoriaCidadã · Santana, São Paulo'; renderAtual(); window.scrollTo({ top: 0 });
  }
  function rota() {
    const h = location.hash.slice(1), m = h.match(/^ocorrencia\/(\d+)$/);
    if (m) { if (estado.carregado) abrirDetalhe(Number(m[1])); return; }
    mostrarAba(ABAS.indexOf(h) >= 0 ? h : estado.aba);
  }
  async function carregarReferencias() {
    const r = await Promise.all([api('/categorias'), api('/regioes')]); estado.categorias = r[0]; estado.regioes = r[1];
    preencherFiltros(); montarFormRegistro();
  }
  async function carregarOcorrencias(silencioso) {
    try { estado.ocorrencias = await api('/ocorrencias'); estado.carregado = true; renderAtual(); }
    catch (e) { if (!silencioso) toast('erro', e.message, e.detalhes); }
  }
  function ligarEventos() {
    $('#b-tema').addEventListener('click', () => { prefs.tema = prefs.tema === 'dark' ? 'light' : 'dark'; aplicarPrefs(); });
    $('#b-contraste').addEventListener('click', () => { prefs.contraste = !prefs.contraste; aplicarPrefs(); });
    $('#b-mais').addEventListener('click', () => mudarEscala(1)); $('#b-menos').addEventListener('click', () => mudarEscala(-1));
    const atualizar = async () => { await carregarOcorrencias(); toast('info', 'Dados atualizados.'); };
    $('#b-atualizar').addEventListener('click', atualizar); $('#b-atualizar-p').addEventListener('click', atualizar);
    let t; $('#f-busca').addEventListener('input', ev => { clearTimeout(t); t = setTimeout(() => { estado.filtro.busca = ev.target.value; renderLista(); }, 150); });
    $('#f-cat').addEventListener('change', ev => { estado.filtro.cat = ev.target.value; renderLista(); }); $('#f-reg').addEventListener('change', ev => { estado.filtro.reg = ev.target.value; renderLista(); });
    $('#f-ordem').addEventListener('change', ev => { estado.filtro.ordem = ev.target.value; renderLista(); });
    $('#v-grade').addEventListener('click', () => { estado.visao = 'grade'; renderLista(); }); $('#v-tabela').addEventListener('click', () => { estado.visao = 'tabela'; renderLista(); });
    $('#b-csv').addEventListener('click', exportarCSV);
    $('#form-reg').addEventListener('submit', enviarRegistro);
    ['r-titulo', 'r-desc'].forEach(i => $('#' + i).addEventListener('input', atualizarPrevia)); $('#r-reg').addEventListener('change', atualizarPrevia);
    $$('input[name="urg"]').forEach(i => i.addEventListener('change', atualizarPrevia));
    $('#r-tel').addEventListener('input', ev => { ev.target.value = maskTel(ev.target.value); });
    [['r-nome', 'e-nome'], ['r-email', 'e-email'], ['r-tel', 'e-tel'], ['r-reg', 'e-reg'], ['r-titulo', 'e-titulo']].forEach(([i, e]) => {
      ['input', 'change'].forEach(ev => $('#' + i).addEventListener(ev, () => erroCampo(e, '')));
    });
    $('#cad-categorias').addEventListener('click', () => { estado.cad = 'categorias'; renderCadastros(); }); $('#cad-regioes').addEventListener('click', () => { estado.cad = 'regioes'; renderCadastros(); });
    $('#b-novo-cad').addEventListener('click', () => formCadastro(null));
    ['drawer', 'modal'].forEach(id => { const d = $('#' + id); d.addEventListener('click', ev => { if (ev.target === d) d.close(); }); });
    $('#drawer').addEventListener('close', () => { if (location.hash.indexOf('#ocorrencia/') === 0) history.replaceState(null, '', '#' + estado.aba); });
    window.addEventListener('hashchange', rota);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) { pingApi(); carregarOcorrencias(true); } });
  }
  async function iniciar() {
    aplicarPrefs(); aplicarIcones(); ligarEventos();
    const h = location.hash.slice(1); estado.aba = ABAS.indexOf(h) >= 0 ? h : 'painel'; mostrarAba(estado.aba);
    montar($('#kpis'), [1, 2, 3, 4].map(() => el('div', { class: 'skel' }))); montar($('#lista'), el('div', { class: 'grade g2' }, [1, 2, 3, 4].map(() => el('div', { class: 'skel' }))));
    pingApi(); setInterval(pingApi, 30000); setInterval(() => { if (!document.hidden) carregarOcorrencias(true); }, 60000);
    try { await carregarReferencias(); await carregarOcorrencias(); }
    catch (e) { toast('erro', 'Não foi possível carregar os dados.', [e.message]); montar($('#kpis'), el('div', { class: 'vazio' }, 'Servidor indisponível no momento.')); return; }
    rota();
  }
  iniciar();
})();
