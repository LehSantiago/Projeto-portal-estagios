'use strict';
/*
 * Interface do Portal de Estágios (HTML + JavaScript puro).
 * Tudo que aparece na tela vem da API REST. Dois cuidados de segurança:
 *  1) CSRF: antes de qualquer POST/PUT/PATCH/DELETE buscamos o token em /api/auth/csrf
 *     e o enviamos no cabeçalho indicado pelo servidor (X-XSRF-TOKEN).
 *  2) XSS: os dados são sempre inseridos como TEXTO (textContent), nunca com innerHTML.
 */

const $ = (id) => document.getElementById(id);
let csrf = null;     // { header, token }
let usuario = null;  // { nome, email, perfil } ou null (visitante)

// ---------------------------------------------------------------- utilitários

/** Cria um elemento HTML. Filhos que são texto viram nós de texto (seguro contra XSS). */
function el(tag, atributos = {}, ...filhos) {
  const e = document.createElement(tag);
  for (const [chave, valor] of Object.entries(atributos)) {
    if (chave === 'class') e.className = valor;
    else e.setAttribute(chave, valor);
  }
  for (const filho of filhos.flat()) {
    e.append(filho instanceof Node ? filho : String(filho ?? ''));
  }
  return e;
}

function botao(texto, aoClicar, classe = '') {
  const b = el('button', { type: 'button', class: classe }, texto);
  b.addEventListener('click', aoClicar);
  return b;
}

function aviso(texto, ehErro = false) {
  const caixa = $('aviso');
  caixa.textContent = texto || '';
  caixa.className = ehErro ? 'erro' : '';
  caixa.hidden = !texto;
}

const formatarData = (iso) => (iso ? iso.split('-').reverse().join('/') : '');
const ROTULO_MODALIDADE = { PRESENCIAL: 'Presencial', REMOTA: 'Remota', HIBRIDA: 'Híbrida' };

/** Executa uma ação e mostra o erro (se houver) na caixa de aviso. */
async function tentar(acao) {
  try {
    await acao();
  } catch (e) {
    aviso(e.mensagem || 'Não foi possível concluir a operação.', true);
  }
}

// ---------------------------------------------------------------- acesso à API

async function obterCsrf() {
  const resposta = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
  csrf = await resposta.json();
}

function mensagemDeErro(dados, status) {
  if (dados && dados.erro) {
    const detalhes = Array.isArray(dados.detalhes) ? ' ' + dados.detalhes.join('; ') : '';
    return dados.erro + detalhes;
  }
  return 'Erro ' + status + '.';
}

/** Chama a API. Em caso de erro lança { status, mensagem }. */
async function api(url, metodo = 'GET', corpo) {
  const opcoes = { method: metodo, headers: {}, credentials: 'same-origin' };
  if (metodo !== 'GET') {
    if (!csrf) await obterCsrf();
    opcoes.headers[csrf.header] = csrf.token;
  }
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json';
    opcoes.body = JSON.stringify(corpo);
  }
  const resposta = await fetch(url, opcoes);
  const texto = await resposta.text();
  let dados = null;
  try { dados = texto ? JSON.parse(texto) : null; } catch { dados = null; }

  if (!resposta.ok) {
    if (resposta.status === 401 && url !== '/api/auth/login' && url !== '/api/auth/me') {
      usuario = null; // sessão expirou
      montarMenu();
    }
    throw { status: resposta.status, mensagem: mensagemDeErro(dados, resposta.status) };
  }
  return dados;
}

// ---------------------------------------------------------------- navegação

const TELAS = ['vagas', 'login', 'cadastro', 'candidaturas', 'minhas-vagas', 'form-vaga'];

function mostrar(nome) {
  TELAS.forEach((t) => { $('view-' + t).hidden = (t !== nome); });
  aviso('');
  if (nome === 'vagas') tentar(carregarVagas);
  if (nome === 'candidaturas') tentar(carregarCandidaturas);
  if (nome === 'minhas-vagas') tentar(carregarMinhasVagas);
}

function montarMenu() {
  const menu = $('menu');
  menu.replaceChildren(botao('Vagas', () => mostrar('vagas')));
  if (!usuario) {
    menu.append(botao('Entrar', () => mostrar('login')), botao('Cadastrar-se', () => mostrar('cadastro')));
    $('usuario').textContent = '';
    return;
  }
  if (usuario.perfil === 'ESTUDANTE') {
    menu.append(botao('Minhas candidaturas', () => mostrar('candidaturas')));
  }
  if (usuario.perfil === 'EMPRESA') {
    menu.append(botao('Minhas vagas', () => mostrar('minhas-vagas')), botao('Publicar vaga', novaVaga));
  }
  menu.append(botao('Sair', sair));
  $('usuario').textContent = usuario.nome + ' (' + usuario.perfil.toLowerCase() + ')';
}

function irParaInicio() {
  mostrar(usuario && usuario.perfil === 'EMPRESA' ? 'minhas-vagas' : 'vagas');
}

// ---------------------------------------------------------------- autenticação

async function entrar(evento) {
  evento.preventDefault();
  await tentar(async () => {
    usuario = await api('/api/auth/login', 'POST', { email: $('login-email').value, senha: $('login-senha').value });
    $('form-login').reset();
    csrf = null; // pega um token novo para a sessão nova
    montarMenu();
    irParaInicio();
  });
}

async function sair() {
  await tentar(async () => {
    await api('/api/auth/logout', 'POST');
    usuario = null;
    csrf = null;
    montarMenu();
    mostrar('vagas');
    aviso('Você saiu da sua conta.');
  });
}

async function cadastrar(evento, url, corpo, formulario) {
  evento.preventDefault();
  await tentar(async () => {
    await api(url, 'POST', corpo);
    formulario.reset();
    mostrar('login');
    aviso('Cadastro realizado! Faça login para continuar.');
  });
}

// ---------------------------------------------------------------- vagas (público)

async function carregarAreas() {
  const areas = await api('/api/areas');
  for (const select of [$('filtro-area'), $('vaga-area')]) {
    const primeiro = select.id === 'filtro-area' ? [select.options[0]] : [];
    select.replaceChildren(...primeiro, ...areas.map((a) => el('option', { value: a.id }, a.nome)));
  }
}

async function carregarVagas() {
  const filtros = new URLSearchParams();
  if ($('filtro-area').value) filtros.set('areaId', $('filtro-area').value);
  if ($('filtro-modalidade').value) filtros.set('modalidade', $('filtro-modalidade').value);
  if ($('filtro-q').value.trim()) filtros.set('q', $('filtro-q').value.trim());

  const vagas = await api('/api/vagas' + (filtros.toString() ? '?' + filtros : ''));
  $('detalhe-vaga').hidden = true;
  $('lista-vagas').replaceChildren(...(vagas.length === 0
    ? [el('p', {}, 'Nenhuma vaga aberta encontrada.')]
    : vagas.map((v) => el('div', { class: 'cartao' },
        el('h3', {}, v.titulo),
        el('div', {},
          el('span', { class: 'etiqueta' }, v.area),
          el('span', { class: 'etiqueta' }, ROTULO_MODALIDADE[v.modalidade])),
        el('p', {}, v.empresa + ' · ' + v.localizacao + ' · ' + v.cargaHorariaSemanal + 'h/semana'),
        el('div', { class: 'acoes' }, botao('Ver detalhes', () => tentar(() => mostrarDetalhe(v.id))))))));
}

async function mostrarDetalhe(id) {
  const v = await api('/api/vagas/' + id);
  const painel = $('detalhe-vaga');
  const acoes = el('div', { class: 'acoes' });

  if (v.situacao !== 'ABERTA') {
    acoes.append(el('span', { class: 'etiqueta encerrada' }, 'Vaga encerrada'));
  } else if (usuario && usuario.perfil === 'ESTUDANTE') {
    acoes.append(botao('Candidatar-me', () => tentar(() => candidatar(v.id))));
  } else if (!usuario) {
    acoes.append(el('em', {}, 'Entre como estudante para se candidatar.'));
  }
  acoes.append(botao('Fechar', () => { painel.hidden = true; }, 'secundario'));

  painel.replaceChildren(
    el('h3', {}, v.titulo),
    el('p', {}, el('strong', {}, 'Empresa: '), v.empresa),
    el('p', {}, el('strong', {}, 'Área: '), v.area),
    el('p', {}, el('strong', {}, 'Modalidade: '), ROTULO_MODALIDADE[v.modalidade] + ' · ' + v.localizacao),
    el('p', {}, el('strong', {}, 'Carga horária: '), v.cargaHorariaSemanal + ' horas por semana'),
    el('p', {}, v.descricao),
    acoes);
  painel.hidden = false;
  painel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

async function candidatar(vagaId) {
  await api('/api/vagas/' + vagaId + '/candidaturas', 'POST');
  aviso('Candidatura realizada com sucesso!');
}

// ---------------------------------------------------------------- estudante

async function carregarCandidaturas() {
  const lista = await api('/api/candidaturas/minhas');
  if (lista.length === 0) {
    $('lista-candidaturas').replaceChildren(el('p', {}, 'Você ainda não se candidatou a nenhuma vaga.'));
    return;
  }
  const linhas = lista.map((c) => el('tr', {},
    el('td', {}, c.vaga), el('td', {}, c.empresa), el('td', {}, ROTULO_MODALIDADE[c.modalidade]),
    el('td', {}, c.situacaoVaga === 'ABERTA' ? 'Aberta' : 'Encerrada'), el('td', {}, formatarData(c.dataInscricao))));
  $('lista-candidaturas').replaceChildren(el('table', {},
    el('thead', {}, el('tr', {}, ...['Vaga', 'Empresa', 'Modalidade', 'Situação da vaga', 'Inscrição'].map((t) => el('th', {}, t)))),
    el('tbody', {}, linhas)));
}

// ---------------------------------------------------------------- empresa

async function carregarMinhasVagas() {
  const vagas = await api('/api/vagas/minhas');
  $('painel-candidatos').hidden = true;
  $('lista-minhas-vagas').replaceChildren(...(vagas.length === 0
    ? [el('p', {}, 'Você ainda não publicou vagas.')]
    : vagas.map((v) => {
        const acoes = el('div', { class: 'acoes' });
        if (v.situacao === 'ABERTA') {
          acoes.append(botao('Editar', () => editarVaga(v)), botao('Encerrar', () => tentar(() => encerrarVaga(v.id)), 'perigo'));
        }
        acoes.append(botao('Ver candidatos', () => tentar(() => mostrarCandidatos(v))));
        return el('div', { class: 'cartao' },
          el('h3', {}, v.titulo),
          el('div', {},
            el('span', { class: 'etiqueta' + (v.situacao === 'ABERTA' ? '' : ' encerrada') }, v.situacao === 'ABERTA' ? 'Aberta' : 'Encerrada'),
            el('span', { class: 'etiqueta' }, v.area),
            el('span', { class: 'etiqueta' }, ROTULO_MODALIDADE[v.modalidade])),
          el('p', {}, v.localizacao + ' · ' + v.cargaHorariaSemanal + 'h/semana'),
          acoes);
      })));
}

function novaVaga() {
  $('form-vaga').reset();
  $('vaga-id').value = '';
  $('titulo-form-vaga').textContent = 'Publicar vaga';
  mostrar('form-vaga');
}

function editarVaga(v) {
  $('vaga-id').value = v.id;
  $('vaga-titulo').value = v.titulo;
  $('vaga-descricao').value = v.descricao;
  $('vaga-area').value = v.areaId;
  $('vaga-localizacao').value = v.localizacao;
  $('vaga-modalidade').value = v.modalidade;
  $('vaga-carga').value = v.cargaHorariaSemanal;
  $('titulo-form-vaga').textContent = 'Editar vaga';
  mostrar('form-vaga');
}

async function salvarVaga(evento) {
  evento.preventDefault();
  await tentar(async () => {
    const corpo = {
      titulo: $('vaga-titulo').value,
      descricao: $('vaga-descricao').value,
      areaId: Number($('vaga-area').value),
      localizacao: $('vaga-localizacao').value,
      modalidade: $('vaga-modalidade').value,
      cargaHorariaSemanal: Number($('vaga-carga').value),
    };
    const id = $('vaga-id').value;
    if (id) await api('/api/vagas/' + id, 'PUT', corpo);
    else await api('/api/vagas', 'POST', corpo);
    mostrar('minhas-vagas');
    aviso(id ? 'Vaga atualizada.' : 'Vaga publicada.');
  });
}

async function encerrarVaga(id) {
  if (!confirm('Encerrar esta vaga? Ela deixará de aceitar candidaturas.')) return;
  await api('/api/vagas/' + id + '/encerrar', 'PATCH');
  await carregarMinhasVagas();
  aviso('Vaga encerrada.');
}

async function mostrarCandidatos(vaga) {
  const candidatos = await api('/api/vagas/' + vaga.id + '/candidatos');
  const painel = $('painel-candidatos');
  const conteudo = candidatos.length === 0
    ? el('p', {}, 'Nenhum candidato até o momento.')
    : el('table', {},
        el('thead', {}, el('tr', {}, ...['Nome', 'E-mail', 'Curso', 'Inscrição'].map((t) => el('th', {}, t)))),
        el('tbody', {}, candidatos.map((c) => el('tr', {},
          el('td', {}, c.nome), el('td', {}, c.email), el('td', {}, c.curso), el('td', {}, formatarData(c.dataInscricao))))));
  painel.replaceChildren(el('h3', {}, 'Candidatos — ' + vaga.titulo), conteudo,
    el('div', { class: 'acoes' }, botao('Fechar', () => { painel.hidden = true; }, 'secundario')));
  painel.hidden = false;
}

// ---------------------------------------------------------------- inicialização

$('form-login').addEventListener('submit', entrar);
$('form-estudante').addEventListener('submit', (e) => cadastrar(e, '/api/auth/cadastro/estudante', {
  nome: $('est-nome').value, email: $('est-email').value, curso: $('est-curso').value, senha: $('est-senha').value,
}, $('form-estudante')));
$('form-empresa').addEventListener('submit', (e) => cadastrar(e, '/api/auth/cadastro/empresa', {
  nome: $('emp-nome').value, email: $('emp-email').value, nomeEmpresa: $('emp-empresa').value, senha: $('emp-senha').value,
}, $('form-empresa')));
$('form-filtros').addEventListener('submit', (e) => { e.preventDefault(); tentar(carregarVagas); });
$('btn-limpar').addEventListener('click', () => { $('form-filtros').reset(); tentar(carregarVagas); });
$('form-vaga').addEventListener('submit', salvarVaga);
$('btn-cancelar-vaga').addEventListener('click', () => mostrar('minhas-vagas'));

(async function iniciar() {
  await tentar(carregarAreas);
  try {
    usuario = await api('/api/auth/me'); // há sessão ativa?
  } catch {
    usuario = null;
  }
  montarMenu();
  irParaInicio();
})();
