/**
 * RECARGA FÁCIL - APPLICATION CONTROLLER (VANILLA JS)
 * Gerencia a navegação SPA de todas as telas (Mapa, Detalhes, Agendamento,
 * Checkout com 25%, Sucesso, Favoritos, Minhas Recargas e Perfil).
 * Integrado com a API REST em Java Spring Boot (via js/api.js).
 */

// Configuração de ícones e rótulos de comodidades (UI)
const AMENITY_CONFIG = {
  wifi: { label: "Wi-Fi Grátis", icon: "ri-wifi-line", desc: "Internet de alta velocidade" },
  cafe: { label: "Café / Lounge", icon: "ri-cup-line", desc: "Espaço com café e bebidas" },
  coberto: { label: "Vaga Coberta", icon: "ri-umbrella-line", desc: "Proteção contra sol e chuva" },
  banheiro: { label: "Banheiro", icon: "ri-rest-time-line", desc: "Acesso a sanitário limpo" },
  cameras: { label: "Segurança 24h", icon: "ri-shield-check-line", desc: "Câmeras e monitoramento" },
  trabalho: { label: "Espaço p/ Notebook", icon: "ri-laptop-line", desc: "Mesa com tomadas p/ trabalho" },
  conveniencia: { label: "Conveniência", icon: "ri-store-2-line", desc: "Loja com lanches e compras" }
};

// Estado Global da Aplicação
const AppState = {
  currentScreen: 'screen-search',
  previousScreen: null,
  selectedWallbox: null,
  currentMotorista: null,
  currentMotoristaId: null,
  currentVeiculo: null,
  veiculosDoMotorista: [],
  activeTypeFilter: 'todos', // 'todos' | 'residencial' | 'comercial'
  favoritesFilter: 'todos',
  selectedAmenities: new Set(), // ['wifi', 'cafe', 'coberto', ...]
  favorites: new Set(['wb-001', 'wb-003']), // IDs favoritados por padrão
  searchQuery: '',
  wallboxesList: [],
  backendOnline: false,
  userLocation: {
    lat: -23.5100,
    lng: -47.4600,
    address: 'Sorocaba - SP'
  },
  userMarker: null,
  booking: {
    wallboxId: 1,
    reservaId: 1,
    date: 'Hoje, 26 Ago',
    selectedStartTime: '08:00',
    timeSlot: '08:00 - 10:00',
    durationHours: 2,
    pricePerHour: 15.00,
    subtotal: 30.00,
    platformFeeRate: 0.25, // 25% de taxa de intermediação
    platformFee: 7.50,
    total: 37.50,
    paymentMethod: 'pix',
    reservationCode: 'REC-84920',
    veiculoSelecionado: null
  },
  userBookings: [],
  map: null,
  markers: []
};

// ==========================================
// CONFIGURAÇÃO DINÂMICA DA REDE (ADMIN SANDBOX)
// ==========================================
function getPlatformConfig() {
  const saved = localStorage.getItem('recarga_facil_config');
  if (saved) {
    try { return JSON.parse(saved); } catch(e) {}
  }
  return {
    tarifaBaseHora: 15.00,
    taxaPlataforma: 25.0,
    toleranciaMinutos: 15,
    multaNoShow: 10.0
  };
}

// Inicialização após carregamento do DOM
document.addEventListener('DOMContentLoaded', () => {
  loadSavedFavorites();
  initApp();
});

async function initApp() {
  const cfg = getPlatformConfig();
  AppState.booking.pricePerHour = cfg.tarifaBaseHora || 15.00;
  AppState.booking.platformFeeRate = (cfg.taxaPlataforma || 25) / 100;

  initTimeStatusBar();
  initAmenitiesFilters();
  initMap();
  bindNavigationEvents();
  bindFilterEvents();
  bindDesktopToggle();
  carregarSessaoMotorista();

  // Conexão com Back-end Java Spring Boot
  await loadWallboxData();

  if (AppState.map) {
    setTimeout(() => {
      AppState.map.invalidateSize();
      renderMapMarkers();
    }, 150);
    setTimeout(() => {
      if (AppState.map) AppState.map.invalidateSize();
    }, 500);
  }

  // Se a URL contiver hash #perfil, navega direto para o perfil do motorista
  if (window.location.hash === '#perfil') {
    navigateTo('screen-profile');
  }
}

function loadSavedFavorites() {
  try {
    const saved = localStorage.getItem('recarga_facil_favorites');
    if (saved) {
      AppState.favorites = new Set(JSON.parse(saved));
    }
  } catch (e) {
    console.log("Local storage fallback.");
  }
}

function saveFavorites() {
  try {
    localStorage.setItem('recarga_facil_favorites', JSON.stringify(Array.from(AppState.favorites)));
  } catch (e) {}
}

/**
 * RN02: CÁLCULO DE DISTÂNCIAS GEODÉSICAS (LEAFLET OPEN SOURCE)
 * Utiliza L.latLng().distanceTo() para computar com alta precisão geodésica
 * a distância em km entre o condutor do veículo elétrico e cada ponto de recarga.
 * Nesta fase inicial, a busca por raio é filtrada a nível de banco de dados (Spring Boot / SQL Haversine).
 */
function calculateDistanceKm(lat1, lng1, lat2, lng2) {
  if (typeof L !== 'undefined' && L.latLng) {
    const point1 = L.latLng(lat1, lng1);
    const point2 = L.latLng(lat2, lng2);
    return point1.distanceTo(point2) / 1000; // Converte metros para quilômetros
  }
  // Fallback de Haversine matemático caso Leaflet não esteja instanciado
  const R = 6371;
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLng = (lng2 - lng1) * (Math.PI / 180);
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * (Math.PI / 180)) * Math.cos(lat2 * (Math.PI / 180)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function updateDistancesFromUserLocation() {
  const list = AppState.wallboxesList || [];

  list.forEach(wb => {
    if (wb.lat && wb.lng && AppState.userLocation) {
      const dist = calculateDistanceKm(
        AppState.userLocation.lat,
        AppState.userLocation.lng,
        wb.lat,
        wb.lng
      );
      wb.distanceKm = Number(dist.toFixed(1));
    }
  });
}

/**
 * Carrega pontos de recarga e reservas diretamente da API Java Spring Boot
 */
async function loadWallboxData() {
  const isHealthy = await ApiService.checkBackendHealth();
  AppState.backendOnline = isHealthy;
  updateBackendStatusBadge(isHealthy);

  // Consulta pontos no backend (filtrando por raio de proximidade no MySQL)
  AppState.wallboxesList = await ApiService.getWallboxes(
    AppState.userLocation.lat,
    AppState.userLocation.lng,
    15
  );

  if (AppState.wallboxesList.length > 0) {
    AppState.selectedWallbox = AppState.wallboxesList[0];
  }

  updateDistancesFromUserLocation();
  atualizarBannerMaisProxima();
  renderMapMarkers();
  renderUserLocationMarker(AppState.userLocation.lat, AppState.userLocation.lng);

  await loadReservasData();
}

async function loadReservasData() {
  try {
    const rawReservas = await ApiService.getReservas();
    if (rawReservas && rawReservas.length > 0) {
      AppState.userBookings = rawReservas.map(r => {
        const wb = r.wallbox || {};
        const local = wb.local || {};
        const host = wb.host || {};
        const precoHora = wb.precoHora ? Number(wb.precoHora) : 15.0;
        const subtotal = precoHora * 2;
        const taxa = subtotal * 0.25;
        const total = subtotal + taxa;
        return {
          id: `res-${r.id}`,
          code: `REC-${1000 + r.id}`,
          wallboxTitle: wb.modelo || "Wallbox Recarga Fácil",
          wallboxPhoto: wb.fotoUrl || "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80",
          power: wb.potencia ? wb.potencia.replace('P_', '').replace('KW', ' kW') : "7.4 kW",
          date: r.dataReserva || "Data confirmada",
          time: r.horario ? `${r.horario.substring(0, 5)} (2h)` : "14:00 (2h)",
          total: total,
          fee25: taxa,
          status: r.statusReserva || "CONFIRMADA",
          address: local.enderecoCompleto || "Endereço liberado",
          accessNotes: local.instrucoesAcesso || `Anfitrião ${host.nome || 'avisado'}.`
        };
      });
    }
  } catch (e) {
    console.error("Erro ao carregar reservas:", e);
  }
}

/**
 * GOOGLE MAPS: Identifica a estação mais próxima do motorista e gera rota direta
 */
function atualizarBannerMaisProxima() {
  const banner = document.getElementById('nearest-station-banner');
  if (!banner) return;

  const list = AppState.wallboxesList || [];
  if (list.length === 0) {
    banner.style.display = 'none';
    return;
  }

  // Ordena pela menor distância em km
  const ordenada = [...list].sort((a, b) => (a.distanceKm || 999) - (b.distanceKm || 999));
  const maisProxima = ordenada[0];

  if (maisProxima) {
    document.getElementById('nearest-station-title').innerText = maisProxima.title;
    document.getElementById('nearest-station-desc').innerText = `Apenas ${maisProxima.distanceKm} km · ${maisProxima.neighborhood || 'Sorocaba - SP'}`;
    const gmapsBtn = document.getElementById('nearest-station-gmaps-btn');
    if (gmapsBtn) {
      gmapsBtn.href = `https://www.google.com/maps/dir/?api=1&destination=${maisProxima.lat},${maisProxima.lng}`;
    }
    banner.style.display = 'flex';
  }
}

function selecionarMaisProximaBanner() {
  if (!AppState.wallboxesList || AppState.wallboxesList.length === 0) return;
  const ordenada = [...AppState.wallboxesList].sort((a, b) => (a.distanceKm || 999) - (b.distanceKm || 999));
  const maisProxima = ordenada[0];
  if (maisProxima) {
    AppState.selectedWallbox = maisProxima;
    showBottomSheetPreview(maisProxima);
    if (AppState.map) {
      AppState.map.setView([maisProxima.lat, maisProxima.lng], 15);
    }
  }
}

/* ==========================================================================
   AUTENTICAÇÃO & SESSÃO DO MOTORISTA
   ========================================================================== */
function abrirModalLoginMotorista(abaPadrao = 'login') {
  const modal = document.getElementById('modal-login-motorista');
  if (modal) {
    modal.style.display = 'flex';
    alternarAbaAuthApp(abaPadrao);
  }
}

function fecharModalLoginMotorista() {
  const modal = document.getElementById('modal-login-motorista');
  if (modal) modal.style.display = 'none';
}

function alternarAbaAuthApp(aba) {
  const painelLogin = document.getElementById('app-painel-login');
  const painelCadastro = document.getElementById('app-painel-cadastro');
  const tabLogin = document.getElementById('app-tab-login');
  const tabCadastro = document.getElementById('app-tab-cadastro');

  if (!painelLogin || !painelCadastro) return;

  if (aba === 'cadastro') {
    painelLogin.style.display = 'none';
    painelCadastro.style.display = 'block';
    if (tabCadastro) { tabCadastro.style.background = 'var(--primary)'; tabCadastro.style.color = '#fff'; }
    if (tabLogin) { tabLogin.style.background = 'transparent'; tabLogin.style.color = '#64748B'; }
  } else {
    painelLogin.style.display = 'block';
    painelCadastro.style.display = 'none';
    if (tabLogin) { tabLogin.style.background = 'var(--primary)'; tabLogin.style.color = '#fff'; }
    if (tabCadastro) { tabCadastro.style.background = 'transparent'; tabCadastro.style.color = '#64748B'; }
  }
}

function preencherLoginMotorista(email, senha) {
  document.getElementById('motorista-login-email').value = email;
  document.getElementById('motorista-login-senha').value = senha;
}

async function realizarLoginMotorista(event) {
  event.preventDefault();
  const email = document.getElementById('motorista-login-email').value.trim();
  const senha = document.getElementById('motorista-login-senha').value.trim();
  const btn = document.getElementById('btn-submit-motorista');

  btn.disabled = true;
  btn.innerHTML = '<i class="ri-loader-4-line ri-spin"></i> Autenticando...';

  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}/auth/login-motorista`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, senha })
    });

    if (response.ok) {
      const motoristaData = await response.json();
      sessionStorage.setItem('recarga_facil_motorista', JSON.stringify(motoristaData));
      AppState.currentMotorista = motoristaData;
      AppState.currentMotoristaId = motoristaData.id;
      atualizarBarraMotorista();
      fecharModalLoginMotorista();
      showToast(`Bem-vindo, ${motoristaData.nome}!`);
      navigateTo('screen-profile');
    } else {
      const err = await response.json();
      alert(err.error || 'Credenciais inválidas.');
    }
  } catch (e) {
    console.error(e);
    alert('Erro ao conectar ao servidor Java Spring Boot.');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="ri-login-box-line"></i> Entrar como Motorista';
  }
}

async function realizarCadastroMotoristaApp(event) {
  event.preventDefault();
  const nome = document.getElementById('app-cad-nome').value.trim();
  const cpf = document.getElementById('app-cad-cpf').value.trim();
  const email = document.getElementById('app-cad-email').value.trim();
  const telefone = document.getElementById('app-cad-telefone').value.trim();
  const senha = document.getElementById('app-cad-senha').value.trim();
  const marca = document.getElementById('app-cad-marca').value.trim();
  const modelo = document.getElementById('app-cad-modelo').value.trim();
  const placa = document.getElementById('app-cad-placa').value.trim().toUpperCase();
  const renavan = document.getElementById('app-cad-renavam').value.trim();
  const tipoConector = document.getElementById('app-cad-conector').value;

  const btn = document.getElementById('app-btn-cadastrar');
  btn.disabled = true;
  btn.innerHTML = '<i class="ri-loader-4-line ri-spin"></i> Cadastrando...';

  try {
    const response = await fetch(`${API_CONFIG.BASE_URL}/auth/cadastro-motorista`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nome, cpf, email, telefone, senha,
        marca, modelo, placa, renavan, tipoConector
      })
    });

    if (response.ok) {
      const motoristaData = await response.json();
      sessionStorage.setItem('recarga_facil_motorista', JSON.stringify(motoristaData));
      AppState.currentMotorista = motoristaData;
      AppState.currentMotoristaId = motoristaData.id;
      atualizarBarraMotorista();
      fecharModalLoginMotorista();
      showToast(`Cadastro realizado! Bem-vindo, ${motoristaData.nome}!`);
      navigateTo('screen-profile');
    } else {
      const err = await response.json();
      alert(err.error || 'Erro ao realizar cadastro.');
    }
  } catch (e) {
    console.error(e);
    alert('Erro ao conectar ao servidor Java Spring Boot.');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<i class="ri-user-add-line"></i> Cadastrar e Acessar';
  }
}

function atualizarBarraMotorista() {
  const nomeBar = document.getElementById('motorista-nome-bar');
  const btnAuth = document.getElementById('btn-motorista-auth');
  if (!nomeBar || !btnAuth) return;

  if (AppState.currentMotorista) {
    const primeiroNome = AppState.currentMotorista.nome ? AppState.currentMotorista.nome.split(' ')[0] : 'Motorista';
    nomeBar.innerText = `${primeiroNome} (Sair)`;
    btnAuth.title = "Clique para encerrar a sessão e voltar à Página Inicial";
    btnAuth.onclick = fazerLogoutMotorista;
  } else {
    window.location.replace('index.html?login=motorista');
  }
}

function fazerLogoutMotorista() {
  if (confirm("Deseja realmente sair da sua conta?")) {
    sessionStorage.removeItem('recarga_facil_motorista');
    window.location.replace('index.html');
  }
}

function carregarSessaoMotorista() {
  const salvo = sessionStorage.getItem('recarga_facil_motorista');
  if (salvo) {
    try {
      AppState.currentMotorista = JSON.parse(salvo);
      AppState.currentMotoristaId = AppState.currentMotorista.id;
    } catch (e) {}
  }
  atualizarBarraMotorista();
}

function updateBackendStatusBadge(isOnline) {
  const badges = document.querySelectorAll('.backend-status-badge, #backend-status-indicator');
  badges.forEach(badgeEl => {
    if (isOnline) {
      badgeEl.innerHTML = `<span style="display:inline-block;width:7px;height:7px;border-radius:50%;background:#00D084;margin-right:6px;"></span> Java API Online (Porta 8080)`;
      badgeEl.style.color = '#047857';
      badgeEl.style.background = '#ECFDF5';
    } else {
      badgeEl.innerHTML = `<span style="display:inline-block;width:7px;height:7px;border-radius:50%;background:#F59E0B;margin-right:6px;"></span> Modo Standalone`;
      badgeEl.style.color = '#B45309';
      badgeEl.style.background = '#FEF3C7';
    }
  });
}

/* ==========================================================================
   1. MAPA INTERATIVO E MARCADORES (LEAFLET + OPENSTREETMAP OPEN SOURCE)
   ========================================================================== */
function initMap() {
  const mapElement = document.getElementById('map-container');
  if (!mapElement) return;

  const defaultCenter = [AppState.userLocation.lat, AppState.userLocation.lng];

  try {
    AppState.map = L.map('map-container', {
      zoomControl: false,
      attributionControl: true
    }).setView(defaultCenter, 13);

    // Tiles oficiais OpenSource do OpenStreetMap (100% livres, sem restrição de API Key)
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(AppState.map);

    renderUserLocationMarker(AppState.userLocation.lat, AppState.userLocation.lng);
    renderMapMarkers();
  } catch (e) {
    console.warn("Leaflet fallback ativo.", e);
  }
}

function renderUserLocationMarker(lat, lng) {
  if (!AppState.map) return;
  if (AppState.userMarker) {
    AppState.map.removeLayer(AppState.userMarker);
  }

  const userIcon = L.divIcon({
    className: 'leaflet-user-gps-icon',
    html: `
      <div style="position: relative; width: 28px; height: 28px;">
        <div class="pin-pulse" style="background: rgba(0, 102, 204, 0.4);"></div>
        <div style="position: absolute; width: 16px; height: 16px; top: 6px; left: 6px; border-radius: 50%; background: #0066CC; border: 2.5px solid #FFFFFF; box-shadow: 0 2px 8px rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center;">
          <div style="width: 4px; height: 4px; border-radius: 50%; background: white;"></div>
        </div>
      </div>
    `,
    iconSize: [28, 28],
    iconAnchor: [14, 14]
  });

  AppState.userMarker = L.marker([lat, lng], { icon: userIcon, zIndexOffset: 1000 }).addTo(AppState.map);
}

function handleGpsLocate() {
  showToast("Obtendo localização do veículo...");

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        AppState.userLocation.lat = lat;
        AppState.userLocation.lng = lng;

        if (AppState.map) {
          AppState.map.setView([lat, lng], 14);
          renderUserLocationMarker(lat, lng);
        }

        // RN02: Busca no banco de dados por proximidade
        if (AppState.backendOnline) {
          AppState.wallboxesList = await ApiService.getWallboxes(lat, lng, 15);
        }

        // RN02: Recalcula distâncias de todos os pontos no frontend usando Leaflet
        updateDistancesFromUserLocation();
        renderMapMarkers();
        showToast("Distâncias recalculadas com Leaflet (RN02)");
      },
      (error) => {
        console.warn("Geolocalização não autorizada, centralizando em Sorocaba/SP:", error);
        if (AppState.map) {
          AppState.map.setView([AppState.userLocation.lat, AppState.userLocation.lng], 13);
          renderUserLocationMarker(AppState.userLocation.lat, AppState.userLocation.lng);
        }
        updateDistancesFromUserLocation();
        renderMapMarkers();
        showToast("Mapa centralizado em Sorocaba - SP");
      },
      { enableHighAccuracy: true, timeout: 5000 }
    );
  } else {
    if (AppState.map) {
      AppState.map.setView([AppState.userLocation.lat, AppState.userLocation.lng], 13);
      renderUserLocationMarker(AppState.userLocation.lat, AppState.userLocation.lng);
    }
    updateDistancesFromUserLocation();
    renderMapMarkers();
    showToast("Mapa centralizado em Sorocaba - SP");
  }
}

function renderMapMarkers() {
  if (!AppState.map) return;

  AppState.markers.forEach(m => AppState.map.removeLayer(m));
  AppState.markers = [];

  const filteredWallboxes = getFilteredWallboxes();
  updateResultsCount(filteredWallboxes.length);

  filteredWallboxes.forEach(wb => {
    const isCommercial = wb.type === 'comercial';
    const pinClass = isCommercial ? 'commercial-pin' : '';
    const iconName = isCommercial ? 'ri-building-2-fill' : 'ri-home-4-fill';

    const customIcon = L.divIcon({
      className: 'leaflet-custom-div-icon',
      html: `
        <div class="custom-map-pin ${pinClass}" onclick="handleMarkerClick('${wb.id}')">
          <div class="pin-pulse"></div>
          <i class="${iconName}"></i>
          <span>R$ ${wb.pricePerHour.toFixed(0)}/h</span>
        </div>
      `,
      iconSize: [80, 30],
      iconAnchor: [40, 15]
    });

    const marker = L.marker([wb.lat, wb.lng], { icon: customIcon }).addTo(AppState.map);
    AppState.markers.push(marker);
  });
}

function handleMarkerClick(wallboxId) {
  const wallbox = AppState.wallboxesList && AppState.wallboxesList.find(w => w.id === wallboxId);
  if (!wallbox) return;

  AppState.selectedWallbox = wallbox;
  showBottomSheetPreview(wallbox);
}

function showBottomSheetPreview(wallbox) {
  const bottomSheet = document.getElementById('map-bottom-sheet');
  if (!bottomSheet) return;

  const isRes = wallbox.type === 'residencial';
  const typeBadge = isRes 
    ? `<span class="badge badge-residential"><i class="ri-home-4-line"></i> Residencial</span>` 
    : `<span class="badge badge-commercial"><i class="ri-building-2-line"></i> Comercial</span>`;

  bottomSheet.innerHTML = `
    <div class="preview-card-content">
      <img src="${wallbox.photo}" alt="${wallbox.title}" class="preview-thumbnail" style="cursor: pointer;" onclick="navigateTo('screen-details')">
      <div class="preview-details">
        <div style="cursor: pointer;" onclick="navigateTo('screen-details')">
          <div class="d-flex justify-between align-center mb-1">
            ${typeBadge}
            <span class="badge badge-power"><i class="ri-flashlight-line"></i> ${wallbox.power}</span>
          </div>
          <h3 class="preview-title">${wallbox.title}</h3>
          <p class="text-muted" style="font-size: 12px; margin-bottom: 2px;"><i class="ri-map-pin-2-line"></i> ${wallbox.distanceKm} km • ${wallbox.neighborhood}</p>
          <div style="font-size: 11px; color: #059669; font-weight: 600; display: flex; align-items: center; gap: 4px;">
            <i class="ri-shield-keyhole-line"></i> Endereço exato liberado após pagamento
          </div>
        </div>
        <div class="d-flex justify-between align-center mt-2" style="gap: 8px;">
          <div class="preview-price">R$ ${wallbox.pricePerHour.toFixed(2).replace('.', ',')}<small>/h</small></div>
          <div style="display: flex; gap: 6px;">
            <button class="btn btn-secondary" style="padding: 7px 12px; font-size: 12px; font-weight: 600;" onclick="navigateTo('screen-details')" title="Ver Detalhes do Ponto">
              <i class="ri-information-line"></i> Detalhes
            </button>
            <button class="btn btn-primary" style="padding: 7px 14px; font-size: 12px; font-weight: 700;" onclick="iniciarAgendamentoWallbox('${wallbox.id}')">
              <i class="ri-calendar-check-line"></i> Fazer Agendamento
            </button>
          </div>
        </div>
      </div>
    </div>
  `;

  bottomSheet.classList.add('show');
}

function iniciarAgendamentoWallbox(wallboxId) {
  const wb = AppState.wallboxesList && AppState.wallboxesList.find(w => w.id === wallboxId);
  if (wb) {
    AppState.selectedWallbox = wb;
  }
  navigateTo('screen-booking');
}

function filtrarPorMaisProximo(event) {
  if (event) {
    document.querySelectorAll('.filter-chip[data-type]').forEach(c => c.classList.remove('active'));
    event.currentTarget.classList.add('active');
  }
  if (!AppState.wallboxesList || AppState.wallboxesList.length === 0) return;
  const ordenada = [...AppState.wallboxesList].sort((a, b) => (a.distanceKm || 999) - (b.distanceKm || 999));
  const maisProxima = ordenada[0];
  if (maisProxima) {
    AppState.selectedWallbox = maisProxima;
    if (AppState.map) {
      AppState.map.setView([maisProxima.lat, maisProxima.lng], 15);
    }
    showBottomSheetPreview(maisProxima);
    showToast(`Ponto mais próximo: ${maisProxima.title} (${maisProxima.distanceKm} km)`);
  }
}

/* ==========================================================================
   2. FILTROS DE COMODIDADES E TIPO
   ========================================================================== */
function initAmenitiesFilters() {
  const container = document.getElementById('amenities-filter-container');
  if (!container) return;

  container.innerHTML = Object.entries(AMENITY_CONFIG).map(([key, config]) => `
    <button class="amenity-chip" data-amenity="${key}" onclick="toggleAmenityFilter('${key}', this)">
      <i class="${config.icon}"></i> ${config.label}
    </button>
  `).join('');
}

function toggleAmenityFilter(amenityKey, element) {
  if (AppState.selectedAmenities.has(amenityKey)) {
    AppState.selectedAmenities.delete(amenityKey);
    element.classList.remove('selected');
  } else {
    AppState.selectedAmenities.add(amenityKey);
    element.classList.add('selected');
  }

  renderMapMarkers();
  showToast(AppState.selectedAmenities.size > 0 
    ? `Filtro de comodidades atualizado (${AppState.selectedAmenities.size} ativas)`
    : "Mostrando todos os pontos"
  );
}

function getFilteredWallboxes() {
  const sourceList = AppState.wallboxesList || [];

  return sourceList.filter(wb => {
    if (AppState.activeTypeFilter !== 'todos' && wb.type !== AppState.activeTypeFilter) {
      return false;
    }

    if (AppState.selectedAmenities.size > 0) {
      for (const amenity of AppState.selectedAmenities) {
        if (!wb.amenities || !wb.amenities.includes(amenity)) {
          return false;
        }
      }
    }

    if (AppState.searchQuery.trim() !== '') {
      const q = AppState.searchQuery.toLowerCase();
      const matchTitle = wb.title.toLowerCase().includes(q);
      const matchNeigh = wb.neighborhood ? wb.neighborhood.toLowerCase().includes(q) : false;
      const matchCity = wb.city ? wb.city.toLowerCase().includes(q) : false;
      if (!matchTitle && !matchNeigh && !matchCity) return false;
    }

    return true;
  });
}

function updateResultsCount(count) {
  const counterEl = document.getElementById('results-counter');
  if (counterEl) {
    counterEl.innerHTML = `<i class="ri-charging-pile-2-fill"></i> ${count} wallbox${count === 1 ? '' : 'es'} disponíve${count === 1 ? 'l' : 'is'}`;
  }
}

/* ==========================================================================
   3. NAVEGAÇÃO ENTRE TODAS AS TELAS (SPA CONTROLLER)
   ========================================================================== */
function navigateTo(targetScreenId) {
  const targetScreen = document.getElementById(targetScreenId);
  if (!targetScreen || targetScreenId === AppState.currentScreen) return;

  AppState.previousScreen = AppState.currentScreen;
  AppState.currentScreen = targetScreenId;

  // Renderiza telas dinâmicas
  if (targetScreenId === 'screen-search') {
    if (AppState.map) {
      setTimeout(() => {
        AppState.map.invalidateSize();
        if (AppState.userLocation) {
          AppState.map.setView([AppState.userLocation.lat, AppState.userLocation.lng]);
        }
        renderMapMarkers();
        renderUserLocationMarker(AppState.userLocation.lat, AppState.userLocation.lng);
      }, 50);
      setTimeout(() => {
        if (AppState.map) AppState.map.invalidateSize();
      }, 250);
    }
  }
  if (targetScreenId === 'screen-details') renderScreenDetails();
  if (targetScreenId === 'screen-booking') renderScreenBooking();
  if (targetScreenId === 'screen-checkout') renderScreenCheckout();
  if (targetScreenId === 'screen-success') renderScreenSuccess();
  if (targetScreenId === 'screen-favorites') renderScreenFavorites();
  if (targetScreenId === 'screen-bookings-list') renderScreenBookingsList();
  if (targetScreenId === 'screen-profile') renderScreenProfile();

  document.querySelectorAll('.app-screen').forEach(screen => {
    screen.classList.remove('active');
  });
  targetScreen.classList.add('active');

  // Atualiza active do bottom navigation
  document.querySelectorAll('.nav-item').forEach(item => {
    if (item.getAttribute('data-target') === targetScreenId) {
      item.classList.add('active');
    } else {
      item.classList.remove('active');
    }
  });

  // Oculta bottom-nav apenas em telas de fluxo linear (detalhes, agendamento, checkout)
  const bottomNav = document.getElementById('bottom-nav-bar');
  if (bottomNav) {
    const isMainTab = ['screen-search', 'screen-favorites', 'screen-bookings-list', 'screen-profile'].includes(targetScreenId);
    bottomNav.style.display = isMainTab ? 'flex' : 'none';
  }

  targetScreen.scrollTop = 0;
}

function goBack() {
  if (AppState.currentScreen === 'screen-details') {
    navigateTo('screen-search');
  } else if (AppState.currentScreen === 'screen-booking') {
    navigateTo('screen-details');
  } else if (AppState.currentScreen === 'screen-checkout') {
    navigateTo('screen-booking');
  } else if (AppState.currentScreen === 'screen-success') {
    navigateTo('screen-bookings-list');
  } else {
    navigateTo('screen-search');
  }
}

/* ==========================================================================
   4. TELA DE FAVORITOS (SCREEN FAVORITES)
   ========================================================================== */
function toggleFavorite(wallboxId, event) {
  if (event) event.stopPropagation();

  if (AppState.favorites.has(wallboxId)) {
    AppState.favorites.delete(wallboxId);
    showToast("Removido dos favoritos");
  } else {
    AppState.favorites.add(wallboxId);
    showToast("Adicionado aos favoritos!");
  }

  saveFavorites();

  // Re-renderiza a tela de favoritos se estiver nela
  if (AppState.currentScreen === 'screen-favorites') {
    renderScreenFavorites();
  }
  // Re-renderiza detalhes se estiver nela
  if (AppState.currentScreen === 'screen-details') {
    renderScreenDetails();
  }
}

function renderScreenFavorites() {
  const container = document.getElementById('favorites-content-placeholder');
  if (!container) return;

  const allWallboxes = AppState.wallboxesList || [];

  // Filtra as wallboxes favoritadas
  const favList = allWallboxes.filter(wb => AppState.favorites.has(wb.id));

  // Aplica filtro por tipo na tela de favoritos
  const filteredFavs = favList.filter(wb => {
    if (AppState.favoritesFilter !== 'todos' && wb.type !== AppState.favoritesFilter) {
      return false;
    }
    return true;
  });

  const cardsHtml = filteredFavs.map(wb => {
    const isRes = wb.type === 'residencial';
    const typeBadge = isRes 
      ? `<span class="badge badge-residential"><i class="ri-home-4-line"></i> Residencial</span>` 
      : `<span class="badge badge-commercial"><i class="ri-building-2-line"></i> Comercial</span>`;

    const amenitiesIcons = (wb.amenities || []).slice(0, 3).map(am => {
      const cfg = AMENITY_CONFIG[am];
      return cfg ? `<span class="badge" style="background:#F1F5F9;color:#475569;"><i class="${cfg.icon}"></i> ${cfg.label.split(' ')[0]}</span>` : '';
    }).join(' ');

    return `
      <div class="favorite-card" onclick="openFavoriteDetails('${wb.id}')">
        <div class="favorite-card-media">
          <img src="${wb.photo}" alt="${wb.title}">
          <button class="btn-favorite-remove" onclick="toggleFavorite('${wb.id}', event)" title="Remover dos Favoritos">
            <i class="ri-heart-3-fill"></i>
          </button>
        </div>
        <div class="favorite-card-body">
          <div class="d-flex justify-between align-center">
            ${typeBadge}
            <span class="badge badge-power"><i class="ri-flashlight-line"></i> ${wb.power}</span>
          </div>
          <h3 style="font-size: 15px; font-weight: 700; color: var(--text-main); margin-top: 2px;">${wb.title}</h3>
          <p style="font-size: 12px; color: var(--text-muted);"><i class="ri-map-pin-2-line"></i> ${wb.distanceKm} km • ${wb.neighborhood || wb.city}</p>
          
          <div class="d-flex gap-1 mt-1" style="flex-wrap: wrap;">
            ${amenitiesIcons}
          </div>

          <div class="d-flex justify-between align-center mt-2" style="border-top: 1px solid var(--border-light); padding-top: 8px;">
            <div style="font-size: 16px; font-weight: 800; color: var(--secondary);">
              R$ ${wb.pricePerHour.toFixed(2).replace('.', ',')}<small style="font-size: 11px; font-weight: 500; color: var(--text-muted);">/h</small>
            </div>
            <button class="btn btn-primary" style="padding: 6px 14px; font-size: 12px;" onclick="quickBookFavorite('${wb.id}', event)">
              <i class="ri-calendar-check-line"></i> Agendar
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = `
    <div class="screen-scroll-body" style="padding: 16px 16px calc(var(--bottom-nav-height) + 20px) 16px;">
      <div class="favorites-header">
        <h1>Meus Favoritos</h1>
        <p>${favList.length} ponto${favList.length === 1 ? '' : 's'} de recarga salvo${favList.length === 1 ? '' : 's'}</p>
      </div>

      <!-- Filtros de Tipo nos Favoritos -->
      <div class="filter-pills-row mb-3">
        <button class="filter-chip ${AppState.favoritesFilter === 'todos' ? 'active' : ''}" onclick="setFavoritesFilter('todos')">
          Todos (${favList.length})
        </button>
        <button class="filter-chip ${AppState.favoritesFilter === 'residencial' ? 'active' : ''}" onclick="setFavoritesFilter('residencial')">
          Residenciais
        </button>
        <button class="filter-chip ${AppState.favoritesFilter === 'comercial' ? 'active' : ''}" onclick="setFavoritesFilter('comercial')">
          Comerciais
        </button>
      </div>

      <!-- Lista de Cards ou Estado Vazio -->
      ${favList.length === 0 ? `
        <div class="empty-state-box">
          <div class="empty-state-icon"><i class="ri-heart-3-line"></i></div>
          <h3 style="font-size: 16px; font-weight: 700;">Nenhum favorito salvo ainda</h3>
          <p style="font-size: 13px; color: var(--text-muted); max-width: 260px;">
            Toque no ícone de coração nos pontos do mapa para salvá-los aqui e agendar recargas com rapidez.
          </p>
          <button class="btn btn-primary mt-2" onclick="navigateTo('screen-search')">
            <i class="ri-map-pin-2-line"></i> Explorar no Mapa
          </button>
        </div>
      ` : cardsHtml}
    </div>
  `;
}

function setFavoritesFilter(filterType) {
  AppState.favoritesFilter = filterType;
  renderScreenFavorites();
}

function openFavoriteDetails(wbId) {
  const allWallboxes = AppState.wallboxesList || [];
  const wb = allWallboxes.find(w => w.id === wbId);
  if (wb) {
    AppState.selectedWallbox = wb;
    navigateTo('screen-details');
  }
}

function quickBookFavorite(wbId, event) {
  if (event) event.stopPropagation();
  openFavoriteDetails(wbId);
  navigateTo('screen-booking');
}

/* ==========================================================================
   5. TELA DE MINHAS RECARGAS / HISTÓRICO (SCREEN BOOKINGS LIST)
   ========================================================================== */
async function renderScreenBookingsList() {
  const container = document.getElementById('bookings-list-content-placeholder');
  if (!container) return;

  await loadReservasData();

  if (!AppState.userBookings || AppState.userBookings.length === 0) {
    container.innerHTML = `
      <div class="screen-scroll-body" style="padding: 16px 16px calc(var(--bottom-nav-height) + 20px) 16px;">
        <div class="favorites-header">
          <h1>Minhas Recargas</h1>
          <p>Histórico e sessões agendadas</p>
        </div>
        <div style="text-align: center; padding: 48px 20px; color: var(--text-muted);">
          <i class="ri-history-line" style="font-size: 36px; opacity: 0.5;"></i>
          <p class="mt-2">Nenhuma recarga encontrada no banco de dados.</p>
        </div>
      </div>
    `;
    return;
  }

  const bookingsHtml = AppState.userBookings.map(item => `
    <div class="booking-item-card">
      <div class="d-flex justify-between align-center mb-2">
        <span class="booking-status-tag completed"><i class="ri-checkbox-circle-fill"></i> ${item.status}</span>
        <span style="font-family: monospace; font-size: 12px; font-weight: 700; color: var(--secondary);">${item.code}</span>
      </div>

      <div class="d-flex gap-3 align-center mb-3">
        <img src="${item.wallboxPhoto}" style="width: 54px; height: 54px; border-radius: var(--radius-md); object-fit: cover;">
        <div>
          <h4 style="font-size: 14px; font-weight: 700; color: var(--text-main);">${item.wallboxTitle}</h4>
          <p style="font-size: 12px; color: var(--text-muted);">${item.date} • ${item.time}</p>
        </div>
      </div>

      <div class="access-instructions-box mb-2" style="font-size: 11px;">
        <strong><i class="ri-lock-unlock-line text-primary"></i> Endereço Liberado:</strong><br>
        ${item.address}<br>
        <span style="color: var(--text-muted);">${item.accessNotes}</span>
      </div>

      <div class="d-flex justify-between align-center pt-2" style="border-top: 1px solid var(--border-light);">
        <div>
          <span style="font-size: 11px; color: var(--text-muted);">Total Pago (Taxa 25% inclusa)</span>
          <div style="font-size: 15px; font-weight: 800; color: var(--secondary);">R$ ${item.total.toFixed(2).replace('.', ',')}</div>
        </div>
        <button class="btn btn-outline-primary" style="padding: 6px 12px; font-size: 12px;" onclick="showToast('Comprovante enviado para seu e-mail')">
          <i class="ri-receipt-line"></i> Comprovante
        </button>
      </div>
    </div>
  `).join('');

  container.innerHTML = `
    <div class="screen-scroll-body" style="padding: 16px 16px calc(var(--bottom-nav-height) + 20px) 16px;">
      <div class="favorites-header">
        <h1>Minhas Recargas</h1>
        <p>Histórico e sessões agendadas</p>
      </div>

      ${bookingsHtml}
    </div>
  `;
}

/* ==========================================================================
   6. TELA DE PERFIL (SCREEN PROFILE)
   ========================================================================== */
async function renderScreenProfile() {
  const container = document.getElementById('profile-content-placeholder');
  if (!container) return;

  container.innerHTML = `
    <div style="padding: 60px 20px; text-align: center; color: var(--text-muted);">
      <i class="ri-loader-4-line ri-spin" style="font-size: 28px; color: var(--primary);"></i>
      <p style="margin-top: 12px; font-size: 13px;">Carregando perfil do banco de dados MySQL...</p>
    </div>
  `;

  try {
    const motoristaId = AppState.currentMotoristaId || 1;
    const motorista = await ApiService.getMotorista(motoristaId) || {
      id: 1,
      nome: "Motorista Demo EV",
      email: "motorista@email.com",
      telefone: "(15) 99876-5432",
      cpf: "123.456.789-00"
    };
    AppState.currentMotorista = motorista;

    // Busca todos os veículos vinculados a este motorista
    let veiculosDoMotorista = [];
    try {
      veiculosDoMotorista = await ApiService.getVeiculosPorMotorista(motorista.id);
    } catch (e) {
      console.warn("Fallback ao buscar veículos:", e);
    }
    if (!veiculosDoMotorista || veiculosDoMotorista.length === 0) {
      const todosVeiculos = await ApiService.getVeiculos();
      veiculosDoMotorista = (todosVeiculos || []).filter(v => v.motorista && v.motorista.id === motorista.id);
    }
    AppState.veiculosDoMotorista = veiculosDoMotorista;
    AppState.currentVeiculo = veiculosDoMotorista.length > 0 ? veiculosDoMotorista[0] : null;
    if (veiculosDoMotorista.length > 0 && !AppState.booking.veiculoSelecionado) {
      AppState.booking.veiculoSelecionado = veiculosDoMotorista[0];
    }

    // Métricas reais calculadas a partir das reservas no banco
    const sessoes = AppState.userBookings.length;
    const kwhTotal = sessoes * 35; // estimativa de kWh por sessão
    const co2Evitado = (kwhTotal * 0.42).toFixed(1);

    const formatarConector = (conector) => {
      const mapa = {
        'TIPO_2': 'Tipo 2 / Mennekes',
        'CCS2': 'CCS 2 Combo',
        'GB_T': 'GB/T (BYD/China)',
        'TIPO_1': 'Tipo 1 (J1772)'
      };
      return mapa[conector] || conector || 'Tipo 2';
    };

    const veiculosCardsHtml = (veiculosDoMotorista && veiculosDoMotorista.length > 0) ? veiculosDoMotorista.map((v, idx) => `
      <div class="card mb-2" style="border: 1px solid #E2E8F0; padding: 14px; border-radius: 12px; background: #ffffff; box-shadow: 0 1px 3px rgba(0,0,0,0.03);">
        <div class="d-flex justify-between align-center mb-2">
          <div style="display: flex; align-items: center; gap: 6px;">
            <span style="font-size: 10px; font-weight: 800; color: #059669; background: #ECFDF5; padding: 2px 8px; border-radius: 6px; letter-spacing: 0.5px; text-transform: uppercase;">
              ${idx === 0 ? '★ Carro Principal' : `Carro #${idx + 1}`}
            </span>
          </div>
          <span class="badge badge-power" style="font-size: 11px;"><i class="ri-plug-line"></i> ${formatarConector(v.tipoConector)}</span>
        </div>
        <div class="d-flex gap-3 align-center">
          <div style="width: 44px; height: 44px; border-radius: 10px; background: #F0FDF4; display: flex; align-items: center; justify-content: center; font-size: 22px; color: #059669; flex-shrink: 0; border: 1px solid #DCFCE7;">
            <i class="ri-car-fill"></i>
          </div>
          <div style="flex: 1;">
            <h4 style="font-size: 14px; font-weight: 800; margin: 0; color: #0F172A;">
              ${v.marca ? v.marca + ' ' : ''}${v.modelo || 'Veículo Elétrico'}
            </h4>
            <div style="font-size: 11px; color: #64748B; margin-top: 4px; display: flex; flex-wrap: wrap; gap: 8px; align-items: center;">
              <span>Placa: <strong style="color: #1E293B; background: #F1F5F9; padding: 1px 6px; border-radius: 4px; font-family: monospace;">${v.placa || 'Sem placa'}</strong></span>
              <span>•</span>
              <span>RENAVAM: <strong style="color: #475569;">${v.renavan || 'N/D'}</strong></span>
            </div>
          </div>
        </div>
      </div>
    `).join('') : `
      <div class="card mb-3 text-center" style="padding: 24px 16px; border: 1.5px dashed #CBD5E1; background: #F8FAFC; border-radius: 12px;">
        <div style="width: 48px; height: 48px; border-radius: 50%; background: #E2E8F0; color: #64748B; display: flex; align-items: center; justify-content: center; font-size: 24px; margin: 0 auto 10px auto;">
          <i class="ri-car-line"></i>
        </div>
        <h4 style="font-size: 14px; font-weight: 700; color: #334155; margin-bottom: 4px;">Nenhum veículo cadastrado</h4>
        <p style="font-size: 12px; color: #64748B; margin-bottom: 12px;">Cadastre seu carro elétrico para liberar agendamentos e recargas na rede.</p>
        <button class="btn btn-primary" style="font-size: 12px; padding: 8px 16px; margin: 0 auto; display: inline-flex; align-items: center; gap: 6px;" onclick="abrirModalCadastrarVeiculo()">
          <i class="ri-add-line"></i> Cadastrar Meu Primeiro Carro
        </button>
      </div>
    `;

    const secaoVeiculosHtml = `
      <div style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; flex-wrap: wrap; gap: 8px;">
          <div>
            <h3 style="font-size: 15px; font-weight: 800; color: #1E293B; margin: 0; display: flex; align-items: center; gap: 6px;">
              <i class="ri-car-fill" style="color: var(--primary);"></i> Meus Veículos Elétricos
              <span style="font-size: 11px; font-weight: 700; background: #ECFDF5; color: #047857; padding: 2px 8px; border-radius: 99px; border: 1px solid #A7F3D0;">
                ${veiculosDoMotorista.length} ${veiculosDoMotorista.length === 1 ? 'carro cadastrado' : 'carros cadastrados'}
              </span>
            </h3>
            <span style="font-size: 11px; color: #64748B;">Cadastre e alterne entre seus carros elétricos</span>
          </div>
          <button class="btn btn-primary" style="font-size: 12px; padding: 6px 14px; font-weight: 700; display: inline-flex; align-items: center; gap: 4px; border-radius: 8px;" onclick="abrirModalCadastrarVeiculo()" title="Cadastrar outro veículo elétrico">
            <i class="ri-add-line"></i> + Adicionar Carro
          </button>
        </div>
        
        ${veiculosCardsHtml}
      </div>
    `;

    const prefPayment = sessionStorage.getItem('recarga_facil_preferred_payment') || 'Pix Instantâneo';
    const fotoSalva = sessionStorage.getItem('recarga_facil_motorista_foto') || 
                      (AppState.currentMotorista && AppState.currentMotorista.fotoUrl) ||
                      'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&h=160&q=80';

    container.innerHTML = `
      <div class="screen-scroll-body" style="padding: 16px 16px calc(var(--bottom-nav-height) + 20px) 16px;">
        <!-- Card do Usuário -->
        <div class="profile-hero-card">
          <div class="profile-avatar-wrapper" onclick="abrirModalFotoPerfil()" title="Clique para cadastrar ou alterar foto de perfil">
            <img src="${fotoSalva}" alt="Avatar" class="profile-avatar-large" id="profile-avatar-img">
            <div class="avatar-edit-badge" title="Cadastrar / Alterar foto">
              <i class="ri-camera-fill"></i>
            </div>
          </div>
          <div>
            <h2 style="font-size: 17px; font-weight: 800;">${motorista.nome}</h2>
            <p style="font-size: 12px; color: #94A3B8;">${motorista.email}</p>
            <p style="font-size: 11px; color: #94A3B8; margin-top: 2px;">Tel: ${motorista.telefone || 'N/A'} • CPF: ${motorista.cpf || 'N/A'}</p>
            <div style="margin-top: 5px;">
              <span style="font-size: 11px; color: var(--primary); cursor: pointer; display: inline-flex; align-items: center; gap: 4px; font-weight: 700;" onclick="abrirModalFotoPerfil()">
                <i class="ri-image-edit-line"></i> Alterar foto de perfil
              </span>
            </div>
          </div>
        </div>

        <!-- Veículos Elétricos Cadastrados (Multi-veículos) -->
        ${secaoVeiculosHtml}

        <!-- Dashboard de Impacto Sustentável -->
        <h3 style="font-size: 14px; font-weight: 700; margin-bottom: 8px;">Seu Impacto Sustentável</h3>
        <div class="impact-dashboard-grid">
          <div class="impact-metric-box">
            <i class="ri-leaf-line" style="color: var(--primary); font-size: 18px;"></i>
            <div class="metric-val">${co2Evitado} kg</div>
            <div class="metric-label">CO₂ Evitado</div>
          </div>
          <div class="impact-metric-box">
            <i class="ri-flashlight-line" style="color: #F59E0B; font-size: 18px;"></i>
            <div class="metric-val">${kwhTotal} kWh</div>
            <div class="metric-label">Energia Limpa</div>
          </div>
          <div class="impact-metric-box">
            <i class="ri-charging-pile-line" style="color: #3B82F6; font-size: 18px;"></i>
            <div class="metric-val">${sessoes}</div>
            <div class="metric-label">Sessões</div>
          </div>
        </div>

        <!-- Menu de Opções -->
        <div class="profile-menu-list mb-3">
          <!-- OPÇÃO DEDICADA: CADASTRAR OUTRO VEÍCULO -->
          <div class="profile-menu-item" onclick="abrirModalCadastrarVeiculo()" style="border-left: 3px solid var(--primary); background: #F8FAFC;">
            <div class="menu-left">
              <div class="menu-icon" style="color: #059669; background: #ECFDF5;"><i class="ri-car-line"></i></div>
              <div>
                <span style="display: block; font-weight: 800; color: #065F46;">Cadastrar Outro Veículo Elétrico</span>
                <span style="display: block; font-size: 11px; color: #64748B;">Adicione mais um carro elétrico (BYD, Volvo, Tesla)</span>
              </div>
            </div>
            <i class="ri-add-line" style="color: var(--primary); font-size: 20px; font-weight: 800;"></i>
          </div>

          <div class="profile-menu-item" onclick="abrirModalAtualizarCadastro()">
            <div class="menu-left">
              <div class="menu-icon" style="color: #0284C7; background: #E0F2FE;"><i class="ri-user-settings-line"></i></div>
              <div>
                <span style="display: block; font-weight: 700;">Atualizar Cadastro</span>
                <span style="display: block; font-size: 11px; color: #94A3B8;">Telefone, WhatsApp e Forma de Pagamento</span>
              </div>
            </div>
            <i class="ri-arrow-right-s-line text-muted"></i>
          </div>
          <div class="profile-menu-item" onclick="abrirModalAtualizarCadastro('pagamento')">
            <div class="menu-left">
              <div class="menu-icon"><i class="ri-bank-card-line"></i></div>
              <span>Formas de Pagamento (${prefPayment})</span>
            </div>
            <i class="ri-arrow-right-s-line text-muted"></i>
          </div>
          <div class="profile-menu-item" onclick="showToast('Notificações push ativadas')">
            <div class="menu-left">
              <div class="menu-icon"><i class="ri-notification-3-line"></i></div>
              <span>Notificações</span>
            </div>
            <i class="ri-arrow-right-s-line text-muted"></i>
          </div>
          <div class="profile-menu-item" onclick="fazerLogoutMotorista()">
            <div class="menu-left">
              <div class="menu-icon" style="color: #EF4444; background: #FEE2E2;"><i class="ri-logout-box-r-line"></i></div>
              <span style="color: #EF4444; font-weight: 700;">Sair da Conta</span>
            </div>
            <i class="ri-arrow-right-s-line text-muted"></i>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    console.error("Erro ao renderizar tela de perfil:", err);
  }
}

window.abrirModalAtualizarCadastro = function(aba = 'geral') {
  const modal = document.getElementById('modal-atualizar-cadastro');
  if (!modal) return;

  const motorista = AppState.currentMotorista || {};
  const nomeInput = document.getElementById('edit-motorista-nome');
  const telInput = document.getElementById('edit-motorista-telefone');
  const cpfInput = document.getElementById('edit-motorista-cpf');
  const emailInput = document.getElementById('edit-motorista-email');
  const senhaInput = document.getElementById('edit-motorista-senha');

  if (nomeInput) nomeInput.value = motorista.nome || '';
  if (telInput) telInput.value = motorista.telefone || '(11) 99269-6670';
  if (cpfInput) cpfInput.value = motorista.cpf || '36616646633';
  if (emailInput) emailInput.value = motorista.email || '';
  if (senhaInput) senhaInput.value = '';

  const savedPayment = sessionStorage.getItem('recarga_facil_preferred_payment') || 'Pix Instantâneo';
  const radioPix = document.getElementById('radio-pag-pix');
  const radioCard = document.getElementById('radio-pag-card');
  const cardFinal = document.getElementById('edit-card-final');

  if (savedPayment.includes('Cartão') || savedPayment.includes('Card') || aba === 'pagamento') {
    if (radioCard) radioCard.checked = true;
    if (cardFinal) cardFinal.value = sessionStorage.getItem('recarga_facil_card_label') || 'Mastercard final 8821';
  } else {
    if (radioPix) radioPix.checked = true;
  }
  toggleEditPaymentFields();

  modal.style.display = 'flex';
};

window.fecharModalAtualizarCadastro = function() {
  const modal = document.getElementById('modal-atualizar-cadastro');
  if (modal) modal.style.display = 'none';
};

window.toggleEditPaymentFields = function() {
  const radioCard = document.getElementById('radio-pag-card');
  const cardFields = document.getElementById('edit-card-fields');
  if (cardFields) {
    cardFields.style.display = (radioCard && radioCard.checked) ? 'block' : 'none';
  }
};

window.salvarAtualizacaoCadastroMotorista = async function(event) {
  event.preventDefault();
  const motorista = AppState.currentMotorista;
  if (!motorista || !motorista.id) {
    showToast("Erro: motorista não autenticado.");
    return;
  }

  const nome = document.getElementById('edit-motorista-nome').value.trim();
  const telefone = document.getElementById('edit-motorista-telefone').value.trim();
  const email = document.getElementById('edit-motorista-email').value.trim();
  const senha = document.getElementById('edit-motorista-senha').value.trim();
  const radioCard = document.getElementById('radio-pag-card');
  const cardFinal = document.getElementById('edit-card-final');

  let paymentLabel = 'Pix Instantâneo';
  if (radioCard && radioCard.checked) {
    paymentLabel = cardFinal && cardFinal.value.trim() ? cardFinal.value.trim() : 'Cartão de Crédito';
    sessionStorage.setItem('recarga_facil_card_label', paymentLabel);
    AppState.booking.paymentMethod = 'card';
  } else {
    AppState.booking.paymentMethod = 'pix';
  }
  sessionStorage.setItem('recarga_facil_preferred_payment', paymentLabel);

  const btn = document.getElementById('btn-save-motorista-edit');
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="ri-loader-4-line ri-spin"></i> Salvando...';
  }

  try {
    const dto = {
      nome: nome || motorista.nome,
      telefone: telefone || motorista.telefone,
      email: email || motorista.email,
      cpf: motorista.cpf
    };
    if (senha) dto.senha = senha;

    const atualizado = await ApiService.updateMotorista(motorista.id, dto);
    if (atualizado) {
      AppState.currentMotorista = { ...AppState.currentMotorista, ...atualizado };
    } else {
      AppState.currentMotorista = { ...AppState.currentMotorista, nome, telefone, email };
    }

    sessionStorage.setItem('recarga_facil_motorista', JSON.stringify(AppState.currentMotorista));
    atualizarBarraMotorista();
    fecharModalAtualizarCadastro();
    showToast("Cadastro atualizado com sucesso!");
    renderScreenProfile();
  } catch (err) {
    console.error("Erro ao salvar atualização de motorista:", err);
    showToast("Erro ao salvar alterações no servidor.");
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = '<i class="ri-check-line"></i> Salvar Alterações';
    }
  }
};

/* ==========================================================================
   GERENCIAMENTO DE MÚLTIPLOS VEÍCULOS (MULTI-VEÍCULOS DO MOTORISTA)
   ========================================================================== */
window.abrirModalCadastrarVeiculo = function() {
  const modal = document.getElementById('modal-cadastrar-veiculo');
  if (!modal) return;
  const form = document.getElementById('form-cadastrar-veiculo');
  if (form) form.reset();
  modal.style.display = 'flex';
};

window.fecharModalCadastrarVeiculo = function() {
  const modal = document.getElementById('modal-cadastrar-veiculo');
  if (modal) modal.style.display = 'none';
};

window.handleSalvarNovoVeiculo = async function(event) {
  event.preventDefault();
  const motorista = AppState.currentMotorista;
  if (!motorista || !motorista.id) {
    alert("Por favor, faça login como motorista primeiro.");
    return;
  }

  const marca = document.getElementById('novo-veiculo-marca').value.trim();
  const modelo = document.getElementById('novo-veiculo-modelo').value.trim();
  const placa = document.getElementById('novo-veiculo-placa').value.trim().toUpperCase();
  const renavam = document.getElementById('novo-veiculo-renavam').value.trim();
  const tipoConector = document.getElementById('novo-veiculo-conector').value;

  const btn = document.getElementById('btn-salvar-novo-veiculo');
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<i class="ri-loader-4-line ri-spin"></i> Salvando veículo...';
  }

  try {
    const payload = {
      idMotorista: motorista.id,
      marca,
      modelo,
      placa,
      renavan: renavam,
      tipoConector
    };

    const novo = await ApiService.cadastrarVeiculo(payload);
    fecharModalCadastrarVeiculo();
    showToast(`Veículo ${marca} ${modelo} (${placa}) cadastrado com sucesso!`);

    if (!AppState.veiculosDoMotorista) AppState.veiculosDoMotorista = [];
    AppState.veiculosDoMotorista.push(novo);
    AppState.currentVeiculo = novo;
    AppState.booking.veiculoSelecionado = novo;

    await renderScreenProfile();
  } catch (err) {
    console.error("Erro ao cadastrar novo veículo:", err);
    alert(err.message || "Erro ao salvar veículo no servidor.");
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = '<i class="ri-add-line"></i> Salvar Veículo';
    }
  }
};

window.selecionarVeiculoParaAgendamento = function(veiculoId) {
  const v = (AppState.veiculosDoMotorista || []).find(item => item.id === veiculoId);
  if (v) {
    AppState.booking.veiculoSelecionado = v;
    showToast(`Veículo selecionado: ${v.marca} ${v.modelo}`);
    const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
    if (wb) renderScreenBookingContent(wb);
  }
};

/* ==========================================================================
   GERENCIAMENTO DE FOTO DE PERFIL DO MOTORISTA
   ========================================================================== */
const AVATAR_PRESETS = [
  { id: 'av1', url: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&h=160&q=80', label: 'Foto Atual' },
  { id: 'av2', url: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=160&h=160&q=80', label: 'Executiva' },
  { id: 'av3', url: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=160&h=160&q=80', label: 'Condutor' },
  { id: 'av4', url: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=160&h=160&q=80', label: 'Jovem EV' },
  { id: 'av5', url: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=160&h=160&q=80', label: 'Avatar Tech' },
  { id: 'av6', url: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=160&h=160&q=80', label: 'Avatar Minimalista' }
];

window.abrirModalFotoPerfil = function() {
  const modal = document.getElementById('modal-foto-perfil');
  if (!modal) return;

  const fotoAtual = sessionStorage.getItem('recarga_facil_motorista_foto') || 
                    (AppState.currentMotorista && AppState.currentMotorista.fotoUrl) ||
                    AVATAR_PRESETS[0].url;

  const previewEl = document.getElementById('modal-avatar-preview');
  if (previewEl) previewEl.src = fotoAtual;

  const container = document.getElementById('avatar-presets-container');
  if (container) {
    container.innerHTML = AVATAR_PRESETS.map(av => `
      <img src="${av.url}" alt="${av.label}" class="avatar-preset-item ${av.url === fotoAtual ? 'active' : ''}" 
           onclick="selecionarPresetFotoPerfil('${av.url}')" title="${av.label}">
    `).join('');
  }

  modal.style.display = 'flex';
};

window.fecharModalFotoPerfil = function() {
  const modal = document.getElementById('modal-foto-perfil');
  if (modal) modal.style.display = 'none';
};

window.selecionarPresetFotoPerfil = function(url) {
  atualizarFotoPerfil(url);
};

window.handleFotoUploadChange = function(event) {
  const file = event.target.files && event.target.files[0];
  if (!file) return;

  if (file.size > 5 * 1024 * 1024) {
    alert("Por favor, selecione uma imagem com menos de 5MB.");
    return;
  }

  const reader = new FileReader();
  reader.onload = function(e) {
    const dataUrl = e.target.result;
    atualizarFotoPerfil(dataUrl);
  };
  reader.readAsDataURL(file);
};

window.atualizarFotoPerfil = function(url) {
  sessionStorage.setItem('recarga_facil_motorista_foto', url);
  if (AppState.currentMotorista) {
    AppState.currentMotorista.fotoUrl = url;
    sessionStorage.setItem('recarga_facil_motorista', JSON.stringify(AppState.currentMotorista));
  }

  const previewEl = document.getElementById('modal-avatar-preview');
  if (previewEl) previewEl.src = url;

  const avatarImg = document.getElementById('profile-avatar-img');
  if (avatarImg) avatarImg.src = url;

  document.querySelectorAll('.avatar-preset-item').forEach(el => {
    if (el.src === url) {
      el.classList.add('active');
    } else {
      el.classList.remove('active');
    }
  });

  showToast("Foto de perfil atualizada com sucesso!");
};

async function alternarMotoristaBanco() {
  try {
    const motoristas = await ApiService.getMotoristas();
    if (!motoristas || motoristas.length <= 1) {
      showToast("Apenas 1 motorista cadastrado no banco.");
      return;
    }
    const atualId = AppState.currentMotoristaId || 1;
    const outro = motoristas.find(m => m.id !== atualId) || motoristas[0];
    AppState.currentMotoristaId = outro.id;
    showToast(`Alternado para motorista: ${outro.nome}`);
    renderScreenProfile();
  } catch (e) {
    console.error(e);
  }
}

/* ==========================================================================
   7. RENDERIZAÇÃO DAS TELAS DE DETALHES, AGENDAMENTO E CHECKOUT
   ========================================================================== */

// Tela 2: Detalhes da Wallbox
function renderScreenDetails() {
  const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
  if (!wb) return;
  const container = document.getElementById('details-content-placeholder');
  if (!container) return;

  const isRes = wb.type === 'residencial';
  const typeBadge = isRes 
    ? `<span class="badge badge-residential"><i class="ri-home-4-line"></i> Residencial</span>` 
    : `<span class="badge badge-commercial"><i class="ri-building-2-line"></i> Comercial</span>`;

  const superHostBadge = wb.isSuperHost 
    ? `<span class="badge badge-superhost"><i class="ri-medal-fill"></i> Super Anfitrião</span>` 
    : '';

  const isFav = AppState.favorites.has(wb.id);

  const amenitiesHtml = (wb.amenities || []).map(amKey => {
    const config = AMENITY_CONFIG[amKey];
    if (!config) return '';
    return `
      <div class="amenity-feature-item">
        <i class="${config.icon}"></i>
        <span>${config.label}</span>
      </div>
    `;
  }).join('');

  container.innerHTML = `
    <div class="screen-scroll-body" style="padding-bottom: 85px;">
      <!-- Hero Image -->
      <div class="details-hero">
        <img src="${wb.photo}" alt="${wb.title}" class="details-hero-img">
        <div class="hero-overlay-actions">
          <button class="btn-icon" onclick="goBack()"><i class="ri-arrow-left-line"></i></button>
          <button class="btn-icon" onclick="toggleFavorite('${wb.id}')" title="Favoritar">
            <i class="${isFav ? 'ri-heart-3-fill' : 'ri-heart-3-line'}" style="${isFav ? 'color: #EF4444;' : ''}"></i>
          </button>
        </div>
        <div class="hero-gradient"></div>
        <div class="hero-badge-pill">
          ${typeBadge}
        </div>
      </div>

      <!-- Conteúdo dos Detalhes -->
      <div class="details-content-body">
        <div class="details-title-row">
          <h1>${wb.title}</h1>
          <div class="location-text">
            <i class="ri-map-pin-2-fill text-primary"></i> ${wb.maskedAddress}
          </div>
        </div>

        <!-- Grid de Especificações Técnicas -->
        <div class="specs-grid">
          <div class="spec-box">
            <i class="ri-flashlight-fill"></i>
            <span class="spec-label">Potência</span>
            <span class="spec-value">${wb.power}</span>
          </div>
          <div class="spec-box">
            <i class="ri-plug-2-fill"></i>
            <span class="spec-label">Plugue</span>
            <span class="spec-value">${wb.plugType ? wb.plugType.split(' ')[0] : 'Tipo 2'}</span>
          </div>
          <div class="spec-box">
            <i class="ri-money-dollar-circle-fill"></i>
            <span class="spec-label">Preço/Hora</span>
            <span class="spec-value text-secondary">R$ ${wb.pricePerHour.toFixed(2).replace('.', ',')}</span>
          </div>
        </div>

        <!-- Card do Anfitrião -->
        <div class="host-card">
          <img src="${wb.hostAvatar}" alt="${wb.hostName}" class="host-avatar">
          <div class="host-meta">
            <div class="host-name">${wb.hostName}</div>
            <div class="host-badge-line">
              <span style="color: var(--accent-gold); font-weight: 700;">★ ${wb.hostRating ? wb.hostRating.toFixed(2) : '4.95'}</span>
              <span>(${wb.totalReviews || 24} avaliações)</span>
              ${superHostBadge}
            </div>
          </div>
        </div>

        <!-- Descrição do Espaço -->
        <div class="card">
          <h3 style="font-size: 14px; font-weight: 700; margin-bottom: 6px;">Sobre este ponto</h3>
          <p style="font-size: 13px; color: var(--text-muted); line-height: 1.5;">${wb.description}</p>
        </div>

        <!-- Comodidades Oferecidas -->
        <div class="amenities-section">
          <h3>Comodidades disponíveis</h3>
          <div class="amenities-list-grid">
            ${amenitiesHtml}
          </div>
        </div>

        <!-- Regra de Segurança: Endereço Mascarado -->
        <div class="security-alert-box" style="background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.25); border-radius: 12px; padding: 14px; margin: 16px 0; display: flex; gap: 12px; align-items: flex-start;">
          <i class="ri-shield-keyhole-fill" style="font-size: 24px; color: #059669; flex-shrink: 0; margin-top: 2px;"></i>
          <div class="alert-text">
            <strong style="color: #065F46; font-size: 13px; display: block; margin-bottom: 2px;">Regra de Segurança e Privacidade do Anfitrião:</strong>
            <span style="font-size: 12px; color: #047857; line-height: 1.45; display: block;">
              Por segurança e privacidade, o endereço completo (rua, número e complemento) e as instruções de acesso ao imóvel/vaga só são <strong>desbloqueados após a confirmação do pagamento</strong> da reserva.
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Barra Fixa Inferior de Agendamento (Sempre Ancorada no Rodapé) -->
    <div class="sticky-bottom-action-bar">
      <div class="action-price-preview">
        <div class="price-amount">R$ ${wb.pricePerHour.toFixed(2).replace('.', ',')}<small style="font-size: 13px; font-weight: 500; color: var(--text-muted);">/hora</small></div>
        <div class="price-label">Cancelamento grátis até 1h antes</div>
      </div>
      <button class="btn btn-primary" onclick="navigateTo('screen-booking')">
        <i class="ri-calendar-check-line"></i> Fazer Agendamento
      </button>
    </div>
  `;
}

// ==========================================================================
// 3. TELA DE AGENDAMENTO (INTEGRADA COM AGENDA: diaSemana, horarioInicio, horarioFim, estaAtivo)
// ==========================================================================
async function renderScreenBooking() {
  const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
  if (!wb) return;
  const container = document.getElementById('booking-content-placeholder');
  if (!container) return;

  AppState.booking.wallboxId = wb.backendId || 1;
  AppState.booking.pricePerHour = wb.pricePerHour;
  if (!AppState.booking.durationHours) {
    AppState.booking.durationHours = 2;
  }
  updateBookingCalculations();

  container.innerHTML = `
    <div class="screen-header-nav">
      <button class="btn-icon" onclick="goBack()"><i class="ri-arrow-left-line"></i></button>
      <span class="header-title">Fazer Agendamento</span>
      <div style="width: 40px;"></div>
    </div>
    <div class="screen-scroll-body" style="padding: 40px 16px; text-align: center;">
      <i class="ri-loader-4-line ri-spin" style="font-size: 32px; color: var(--primary);"></i>
      <p style="margin-top: 12px; color: var(--text-muted); font-size: 13px;">Consultando horários e disponibilidade da agenda...</p>
    </div>
  `;

  // Consulta agendas no banco de dados Java Spring Boot
  const agendas = await ApiService.getAgendasByWallbox(wb.backendId || 1);
  AppState.currentWallboxAgendas = agendas || [];

  const DIAS_CONFIG = [
    { codigo: 'DOMINGO', nome: 'Domingo', abrev: 'Dom' },
    { codigo: 'SEGUNDA', nome: 'Segunda-feira', abrev: 'Seg' },
    { codigo: 'TERCA',   nome: 'Terça-feira', abrev: 'Ter' },
    { codigo: 'QUARTA',  nome: 'Quarta-feira', abrev: 'Qua' },
    { codigo: 'QUINTA',  nome: 'Quinta-feira', abrev: 'Qui' },
    { codigo: 'SEXTA',   nome: 'Sexta-feira', abrev: 'Sex' },
    { codigo: 'SABADO',  nome: 'Sábado', abrev: 'Sáb' }
  ];

  // Gera os próximos 7 dias a partir de hoje
  const hoje = new Date();
  const diasSemanaList = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(hoje);
    d.setDate(hoje.getDate() + i);
    const dayIndex = d.getDay();
    const conf = DIAS_CONFIG[dayIndex];

    const agendaBD = (agendas || []).find(a => a.diaSemana === conf.codigo);
    const estaAtivo = agendaBD ? (agendaBD.estaAtivo === true || agendaBD.estaAtivo === 1) : false;
    const inicioStr = agendaBD && agendaBD.horarioInicio ? agendaBD.horarioInicio.substring(0, 5) : '08:00';
    const fimStr = agendaBD && agendaBD.horarioFim ? agendaBD.horarioFim.substring(0, 5) : '20:00';

    const isoDate = d.toISOString().split('T')[0];
    const rotuloPrincipal = i === 0 ? 'Hoje' : (i === 1 ? 'Amanhã' : conf.abrev);
    const dataDisplay = `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`;

    diasSemanaList.push({
      index: i,
      isoDate: isoDate,
      rotuloPrincipal: rotuloPrincipal,
      nomeCompleto: conf.nome,
      dataDisplay: dataDisplay,
      codigoEnum: conf.codigo,
      estaAtivo: estaAtivo,
      horarioInicio: inicioStr,
      horarioFim: fimStr
    });
  }

  AppState.diasSemanaDisponiveis = diasSemanaList;

  // Seleciona o primeiro dia ativo, ou o índice 0
  if (AppState.booking.selectedDayIndex === undefined || AppState.booking.selectedDayIndex === null) {
    const primeiroAtivoIndex = diasSemanaList.findIndex(d => d.estaAtivo);
    AppState.booking.selectedDayIndex = primeiroAtivoIndex !== -1 ? primeiroAtivoIndex : 0;
  }

  const diaSelecionado = diasSemanaList[AppState.booking.selectedDayIndex] || diasSemanaList[0];
  AppState.booking.date = `${diaSelecionado.rotuloPrincipal}, ${diaSelecionado.dataDisplay}`;
  AppState.booking.selectedDateIso = diaSelecionado.isoDate;
  AppState.booking.currentDayAgenda = diaSelecionado;

  // Garante carregamento dos veículos do motorista para seleção na reserva
  if (!AppState.veiculosDoMotorista || AppState.veiculosDoMotorista.length === 0) {
    const motoristaId = AppState.currentMotoristaId || (AppState.currentMotorista && AppState.currentMotorista.id) || 1;
    try {
      let veics = await ApiService.getVeiculosPorMotorista(motoristaId);
      if (!veics || veics.length === 0) {
        const todos = await ApiService.getVeiculos();
        veics = (todos || []).filter(v => v.motorista && v.motorista.id === motoristaId);
      }
      AppState.veiculosDoMotorista = veics || [];
    } catch (e) {
      console.warn("Erro ao carregar veículos para agendamento:", e);
    }
  }

  if (AppState.veiculosDoMotorista && AppState.veiculosDoMotorista.length > 0 && !AppState.booking.veiculoSelecionado) {
    AppState.booking.veiculoSelecionado = AppState.veiculosDoMotorista[0];
  }

  renderScreenBookingContent(wb);
}

function renderScreenBookingContent(wb) {
  const container = document.getElementById('booking-content-placeholder');
  if (!container) return;

  const cfg = getPlatformConfig();
  wb.pricePerHour = cfg.tarifaBaseHora || wb.pricePerHour || 15.00;
  AppState.booking.pricePerHour = wb.pricePerHour;
  AppState.booking.platformFeeRate = (cfg.taxaPlataforma || 25) / 100;
  updateBookingCalculations();

  const diasSemanaList = AppState.diasSemanaDisponiveis || [];
  const selectedIndex = AppState.booking.selectedDayIndex || 0;
  const diaSelecionado = diasSemanaList[selectedIndex] || diasSemanaList[0];

  const datePillsHtml = diasSemanaList.map((d, idx) => {
    const isSelected = idx === selectedIndex;
    const statusDot = d.estaAtivo 
      ? `<span style="display:inline-block;width:6px;height:6px;border-radius:50%;background:#10B981;"></span>`
      : `<span style="display:inline-block;width:6px;height:6px;border-radius:50%;background:#EF4444;"></span>`;
    
    return `
      <div class="date-pill ${isSelected ? 'selected' : ''}" 
           style="cursor: ${d.estaAtivo ? 'pointer' : 'not-allowed'}; opacity: ${d.estaAtivo ? '1' : '0.45'}; min-width: 78px; flex-shrink: 0; text-align: center;"
           onclick="selectBookingDayFromAgenda(${idx})">
        <div class="day-name" style="display: flex; align-items: center; justify-content: center; gap: 4px; font-size: 11px;">
          ${statusDot} ${d.rotuloPrincipal}
        </div>
        <div class="day-num" style="font-size: 14px; font-weight: 800;">${d.dataDisplay}</div>
        <div style="font-size: 10px; margin-top: 2px; color: ${d.estaAtivo ? '#059669' : '#EF4444'}; font-weight: 700;">
          ${d.estaAtivo ? `${d.horarioInicio}` : 'Inativo'}
        </div>
      </div>
    `;
  }).join('');

  const veiculos = AppState.veiculosDoMotorista || [];
  let seletorVeiculoHtml = '';
  if (veiculos.length > 0) {
    if (!AppState.booking.veiculoSelecionado) {
      AppState.booking.veiculoSelecionado = veiculos[0];
    }
    seletorVeiculoHtml = `
      <!-- SELEÇÃO DO VEÍCULO PARA A RECARGA (MULTI-VEÍCULOS) -->
      <div class="booking-section-card" style="margin-bottom: 16px;">
        <div class="section-label" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <span><i class="ri-car-fill text-primary"></i> Veículo para esta Recarga</span>
          <button type="button" onclick="abrirModalCadastrarVeiculo()" style="background: none; border: none; font-size: 11px; color: var(--primary); font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 3px; padding: 0;">
            <i class="ri-add-line"></i> + Outro Carro
          </button>
        </div>
        <div style="display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px;">
          ${veiculos.map(v => {
            const isSel = AppState.booking.veiculoSelecionado && AppState.booking.veiculoSelecionado.id === v.id;
            return `
              <div onclick="selecionarVeiculoParaAgendamento(${v.id})" 
                   style="flex: 1; min-width: 155px; padding: 10px 12px; border-radius: 10px; border: 2px solid ${isSel ? 'var(--primary)' : '#CBD5E1'}; background: ${isSel ? '#ECFDF5' : '#FFFFFF'}; cursor: pointer; transition: all 0.2s;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <strong style="font-size: 13px; color: ${isSel ? '#065F46' : '#1E293B'};">${v.marca} ${v.modelo}</strong>
                  ${isSel ? '<i class="ri-checkbox-circle-fill" style="color: var(--primary); font-size: 16px;"></i>' : '<i class="ri-checkbox-blank-circle-line" style="color: #94A3B8; font-size: 16px;"></i>'}
                </div>
                <div style="font-size: 11px; color: #64748B; margin-top: 4px;">
                  Placa: <strong style="color: #334155;">${v.placa}</strong> • <span class="badge badge-power" style="font-size: 9px; padding: 2px 5px;">${v.tipoConector || 'Tipo 2'}</span>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>
    `;
  }

  container.innerHTML = `
    <div class="screen-header-nav">
      <button class="btn-icon" onclick="goBack()"><i class="ri-arrow-left-line"></i></button>
      <span class="header-title">Fazer Agendamento</span>
      <div style="width: 40px;"></div>
    </div>

    <div class="screen-scroll-body" style="padding: 16px 16px 95px 16px;">
      <!-- Resumo do Ponto Selecionado -->
      <div class="card mb-3 d-flex gap-3 align-center">
        <img src="${wb.photo}" style="width: 60px; height: 60px; border-radius: var(--radius-md); object-fit: cover;">
        <div style="flex: 1;">
          <h4 style="font-size: 14px; font-weight: 700; margin-bottom: 2px;">${wb.title}</h4>
          <div style="font-size: 12px; color: var(--text-muted);">${wb.power} • R$ ${wb.pricePerHour.toFixed(2).replace('.', ',')}/h</div>
          <div style="font-size: 11px; color: #059669; font-weight: 600; margin-top: 2px;">
            <i class="ri-map-pin-2-fill"></i> ${wb.neighborhood}, ${wb.city}
          </div>
        </div>
      </div>

      <!-- REGRA DE NEGÓCIO DE SEGURANÇA: ENDEREÇO PROTEGIDO -->
      <div style="background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.3); border-radius: 12px; padding: 12px 14px; margin-bottom: 16px; display: flex; gap: 10px; align-items: center;">
        <i class="ri-shield-keyhole-fill" style="font-size: 22px; color: #059669; flex-shrink: 0;"></i>
        <div style="font-size: 12px; color: #065F46; line-height: 1.4;">
          <strong>Regra de Segurança:</strong> O endereço completo (rua, número) e as instruções de acesso ao ponto de recarga só serão <strong>liberados após a confirmação do pagamento</strong> por segurança do anfitrião.
        </div>
      </div>

      <!-- SELETOR DE VEÍCULO ELÉTRICO -->
      ${seletorVeiculoHtml}

      <!-- 1. SELETOR DE DATA / DIA DA SEMANA -->
      <div class="booking-section-card">
        <div class="section-label">
          <i class="ri-calendar-event-line text-primary"></i> 1. Escolha a Data da Recarga
        </div>
        <div class="date-selector-row" style="display: flex; gap: 8px; overflow-x: auto; padding-bottom: 6px;">
          ${datePillsHtml}
        </div>
      </div>

      <!-- STATUS DA AGENDA DA ESTAÇÃO PARA O DIA SELECIONADO -->
      <div style="background: ${diaSelecionado.estaAtivo ? '#F0FDF4' : '#FEF2F2'}; border: 1px solid ${diaSelecionado.estaAtivo ? '#BBF7D0' : '#FECACA'}; border-radius: 12px; padding: 12px 14px; margin-bottom: 16px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-size: 11px; font-weight: 800; color: ${diaSelecionado.estaAtivo ? '#166534' : '#991B1B'}; text-transform: uppercase;">
              <i class="ri-calendar-check-line"></i> Agenda: ${diaSelecionado.nomeCompleto} (${diaSelecionado.codigoEnum})
            </div>
            <div style="font-size: 13px; font-weight: 700; color: #1E293B; margin-top: 3px;">
              ${diaSelecionado.estaAtivo 
                ? `Horário de Funcionamento: <span style="color: #059669;">${diaSelecionado.horarioInicio} às ${diaSelecionado.horarioFim}</span>`
                : `<span style="color: #DC2626;">Esta estação não aceita agendamentos neste dia da semana.</span>`}
            </div>
          </div>
          <span style="padding: 4px 10px; border-radius: 99px; font-size: 11px; font-weight: 800; background: ${diaSelecionado.estaAtivo ? '#DCFCE7' : '#FEE2E2'}; color: ${diaSelecionado.estaAtivo ? '#15803D' : '#B91C1C'};">
            ${diaSelecionado.estaAtivo ? '🟢 Aberto' : '🔴 Inativo'}
          </span>
        </div>
      </div>

      <!-- 2. DURAÇÃO DA RECARGA -->
      <div class="booking-section-card">
        <div class="section-label"><i class="ri-time-line text-primary"></i> 2. Tempo de Recarga Desejado</div>
        <div class="duration-selector-row">
          <div class="duration-chip ${AppState.booking.durationHours === 1 ? 'selected' : ''}" onclick="selectBookingDuration(1, this)">1 Hora</div>
          <div class="duration-chip ${AppState.booking.durationHours === 2 ? 'selected' : ''}" onclick="selectBookingDuration(2, this)">2 Horas</div>
          <div class="duration-chip ${AppState.booking.durationHours === 3 ? 'selected' : ''}" onclick="selectBookingDuration(3, this)">3 Horas</div>
          <div class="duration-chip ${AppState.booking.durationHours === 4 ? 'selected' : ''}" onclick="selectBookingDuration(4, this)">4 Horas</div>
        </div>
      </div>

      <!-- 3. GRADE DE HORÁRIOS DENTRO DO INTERVALO DA AGENDA -->
      <div class="booking-section-card">
        <div class="section-label"><i class="ri-sun-cloudy-line text-primary"></i> 3. Selecione o Horário de Início</div>
        <div class="time-slots-grid" id="booking-time-slots-grid">
          <!-- Renderizado dinamicamente -->
        </div>
      </div>

      <!-- Estimativa de Valores em Tempo Real -->
      <div class="card" style="background: var(--primary-light); border-color: var(--primary);">
        <div class="d-flex justify-between align-center">
          <div>
            <div style="font-size: 11px; font-weight: 700; color: var(--secondary); text-transform: uppercase;">Subtotal da Sessão</div>
            <div id="booking-subtotal-preview" style="font-size: 18px; font-weight: 800; color: var(--secondary);">
              R$ ${AppState.booking.subtotal.toFixed(2).replace('.', ',')}
            </div>
          </div>
          <div style="text-align: right; font-size: 12px; color: var(--text-muted);">
            <span id="booking-hours-preview">${AppState.booking.durationHours}h</span> de recarga a ${wb.power}
          </div>
        </div>
      </div>

      <!-- AVISO DE TOLERÂNCIA DE HORÁRIO E MULTA -->
      <div style="background: rgba(245, 158, 11, 0.08); border: 1px solid rgba(245, 158, 11, 0.3); border-radius: 12px; padding: 12px 14px; margin-top: 14px; display: flex; gap: 10px; align-items: flex-start;">
        <i class="ri-time-line" style="font-size: 20px; color: #D97706; flex-shrink: 0; margin-top: 2px;"></i>
        <div id="booking-multa-box" style="font-size: 12px; color: #92400E; line-height: 1.45;">
          <strong>Tolerância de Chegada:</strong> Você tem até <strong>${cfg.toleranciaMinutos} minutos de tolerância</strong> após o horário agendado para conectar o veículo. Em caso de não comparecimento (No-Show), a vaga é liberada e incide <strong>multa compensatória de ${cfg.multaNoShow}% (R$ ${((AppState.booking.total * cfg.multaNoShow) / 100).toFixed(2).replace('.', ',')})</strong> para o anfitrião.
        </div>
      </div>
    </div>

    <!-- Barra Fixa Inferior de Checkout -->
    <div class="sticky-bottom-action-bar">
      <div class="action-price-preview">
        <div id="booking-action-total" class="price-amount">R$ ${AppState.booking.total.toFixed(2).replace('.', ',')}</div>
        <div class="price-label">Inclui taxa de ${cfg.taxaPlataforma}% da plataforma</div>
      </div>
      <button class="btn btn-primary" id="btn-booking-submit" ${!diaSelecionado.estaAtivo ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''} onclick="handleConfirmBooking()">
        Ir para Pagamento <i class="ri-arrow-right-line"></i>
      </button>
    </div>
  `;

  renderAgendaTimeSlots(diaSelecionado);
}

function selectBookingDayFromAgenda(dayIndex) {
  const diasSemanaList = AppState.diasSemanaDisponiveis || [];
  const dia = diasSemanaList[dayIndex];
  if (!dia) return;

  if (!dia.estaAtivo) {
    showToast(`Estação inativa em ${dia.nomeCompleto}. Por favor, escolha um dia ativo.`);
    return;
  }

  AppState.booking.selectedDayIndex = dayIndex;
  AppState.booking.date = `${dia.rotuloPrincipal}, ${dia.dataDisplay}`;
  AppState.booking.selectedDateIso = dia.isoDate;
  AppState.booking.currentDayAgenda = dia;

  const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
  renderScreenBookingContent(wb);
}

function renderAgendaTimeSlots(diaSelecionado) {
  const gridContainer = document.getElementById('booking-time-slots-grid');
  if (!gridContainer) return;

  if (!diaSelecionado || !diaSelecionado.estaAtivo) {
    gridContainer.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 16px; text-align: center; color: #991B1B; background: #FEF2F2; border-radius: 10px; font-size: 13px;">
        <i class="ri-error-warning-line"></i> Esta estação está inativa neste dia da semana. Selecione outro dia com horário ativo.
      </div>
    `;
    return;
  }

  const duration = AppState.booking.durationHours || 2;
  const startParts = (diaSelecionado.horarioInicio || '08:00').split(':');
  const endParts = (diaSelecionado.horarioFim || '20:00').split(':');

  const startH = parseInt(startParts[0], 10);
  const startM = parseInt(startParts[1] || '0', 10);
  const endH = parseInt(endParts[0], 10);

  const slots = [];
  for (let h = startH; h <= endH - duration; h += (duration <= 2 ? 1 : 2)) {
    const sFormatted = `${String(h).padStart(2, '0')}:${String(startM).padStart(2, '0')}`;
    const eH = h + duration;
    const eFormatted = `${String(eH).padStart(2, '0')}:${String(startM).padStart(2, '0')}`;
    slots.push({
      startTime: sFormatted,
      label: `${sFormatted} - ${eFormatted}`
    });
  }

  if (slots.length === 0) {
    gridContainer.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 14px; text-align: center; color: #92400E; background: #FEF3C7; border-radius: 10px; font-size: 13px;">
        <i class="ri-time-line"></i> A duração (${duration}h) excede o horário de funcionamento deste dia (${diaSelecionado.horarioInicio} às ${diaSelecionado.horarioFim}). Tente reduzir para 1h.
      </div>
    `;
    return;
  }

  let currentStart = AppState.booking.selectedStartTime;
  if (!currentStart || !slots.some(s => s.startTime === currentStart)) {
    currentStart = slots[0].startTime;
  }
  AppState.booking.selectedStartTime = currentStart;
  const currentSlot = slots.find(s => s.startTime === currentStart) || slots[0];
  AppState.booking.timeSlot = currentSlot.label;

  gridContainer.innerHTML = slots.map(slot => {
    const isSelected = slot.startTime === currentStart;
    return `
      <button class="time-slot-btn ${isSelected ? 'selected' : ''}" onclick="selectTimeSlot('${slot.label}', '${slot.startTime}', this)">
        <i class="ri-time-line"></i> ${slot.label}
      </button>
    `;
  }).join('');
}

function selectBookingDuration(hours, element) {
  AppState.booking.durationHours = hours;
  document.querySelectorAll('.duration-chip').forEach(el => el.classList.remove('selected'));
  if (element) {
    element.classList.add('selected');
  }
  updateBookingCalculations();
  const diasSemanaList = AppState.diasSemanaDisponiveis || [];
  const selectedIndex = AppState.booking.selectedDayIndex || 0;
  const diaSelecionado = diasSemanaList[selectedIndex];
  renderAgendaTimeSlots(diaSelecionado);
}

function selectTimeSlot(slotText, startTime, element) {
  AppState.booking.timeSlot = slotText;
  if (startTime) {
    AppState.booking.selectedStartTime = startTime;
  } else {
    AppState.booking.selectedStartTime = slotText.split('-')[0].trim();
  }
  document.querySelectorAll('.time-slot-btn').forEach(el => el.classList.remove('selected'));
  if (element) {
    element.classList.add('selected');
  }
}

async function handleConfirmBooking() {
  if (AppState.currentMotorista) {
    AppState.booking.motoristaId = AppState.currentMotorista.id;
  } else {
    AppState.booking.motoristaId = AppState.currentMotoristaId || 1;
  }
  const reservaResult = await ApiService.createReserva(AppState.booking);
  if (reservaResult && reservaResult.id) {
    AppState.booking.reservaId = reservaResult.id;
  }
  navigateTo('screen-checkout');
}

function updateBookingCalculations() {
  const subtotal = AppState.booking.pricePerHour * AppState.booking.durationHours;
  const fee = subtotal * AppState.booking.platformFeeRate;
  const total = subtotal + fee;

  AppState.booking.subtotal = subtotal;
  AppState.booking.platformFee = fee;
  AppState.booking.total = total;

  const subtotalEl = document.getElementById('booking-subtotal-preview');
  const actionTotalEl = document.getElementById('booking-action-total');
  const hoursPreviewEl = document.getElementById('booking-hours-preview');

  if (subtotalEl) subtotalEl.textContent = `R$ ${subtotal.toFixed(2).replace('.', ',')}`;
  if (actionTotalEl) actionTotalEl.textContent = `R$ ${total.toFixed(2).replace('.', ',')}`;
  if (hoursPreviewEl) hoursPreviewEl.textContent = `${AppState.booking.durationHours}h`;

  // Atualiza o valor em reais da multa dinamicamente conforme a duração e configuração do Admin
  const cfg = getPlatformConfig();
  const multaValor = ((total * cfg.multaNoShow) / 100).toFixed(2).replace('.', ',');
  const multaBoxEl = document.getElementById('booking-multa-box');
  if (multaBoxEl) {
    multaBoxEl.innerHTML = `<strong>Tolerância de Chegada:</strong> Você tem até <strong>${cfg.toleranciaMinutos} minutos de tolerância</strong> após o horário agendado para conectar o veículo. Em caso de não comparecimento (No-Show), a vaga é liberada e incide <strong>multa compensatória de ${cfg.multaNoShow}% (R$ ${multaValor})</strong> para o anfitrião.`;
  }
}

// Tela 4: Pagamento e Checkout
function renderScreenCheckout() {
  const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
  if (!wb) return;
  const cfg = getPlatformConfig();
  const container = document.getElementById('checkout-content-placeholder');
  if (!container) return;

  container.innerHTML = `
    <div class="screen-header-nav">
      <button class="btn-icon" onclick="goBack()"><i class="ri-arrow-left-line"></i></button>
      <span class="header-title">Pagamento & Checkout</span>
      <div style="width: 40px;"></div>
    </div>

    <div class="screen-scroll-body" style="padding: 16px 16px 95px 16px;">
      <!-- Resumo da Reserva -->
      <div class="checkout-summary-card">
        <h3 style="font-size: 15px; font-weight: 700; margin-bottom: 10px;">Resumo do Agendamento</h3>
        <div class="receipt-row">
          <span>Ponto de Recarga:</span>
          <strong class="text-main">${wb.title}</strong>
        </div>
        ${AppState.booking.veiculoSelecionado ? `
        <div class="receipt-row">
          <span>Veículo Agendado:</span>
          <strong class="text-main">${AppState.booking.veiculoSelecionado.marca} ${AppState.booking.veiculoSelecionado.modelo} (${AppState.booking.veiculoSelecionado.placa})</strong>
        </div>` : ''}
        <div class="receipt-row">
          <span>Data e Horário:</span>
          <span>${AppState.booking.date} • ${AppState.booking.timeSlot}</span>
        </div>
        <div class="receipt-row">
          <span>Duração da sessão:</span>
          <span>${AppState.booking.durationHours} horas (${wb.power})</span>
        </div>

        <div class="receipt-divider"></div>

        <!-- Discriminação de Valores com Taxa -->
        <div class="receipt-row">
          <span>Subtotal (${AppState.booking.durationHours}h × R$ ${wb.pricePerHour.toFixed(2).replace('.', ',')}):</span>
          <span>R$ ${AppState.booking.subtotal.toFixed(2).replace('.', ',')}</span>
        </div>
        
        <div class="receipt-row highlight-fee">
          <span><i class="ri-shield-check-line"></i> Taxa da Plataforma (${cfg.taxaPlataforma}%):</span>
          <span>+ R$ ${AppState.booking.platformFee.toFixed(2).replace('.', ',')}</span>
        </div>

        <div class="receipt-divider"></div>

        <div class="receipt-total">
          <span>Total a pagar:</span>
          <span class="total-amount">R$ ${AppState.booking.total.toFixed(2).replace('.', ',')}</span>
        </div>

        <div style="background: rgba(16, 185, 129, 0.08); border: 1px solid rgba(16, 185, 129, 0.25); border-radius: 10px; padding: 10px 12px; margin-top: 12px; font-size: 11px; color: #065F46; display: flex; gap: 8px; align-items: center;">
          <i class="ri-shield-keyhole-fill" style="font-size: 18px; color: #059669; flex-shrink: 0;"></i>
          <span><strong>Regra de Segurança:</strong> O endereço exato com número e as instruções de acesso ao imóvel serão <strong>desbloqueados imediatamente</strong> na tela de confirmação após a aprovação do pagamento.</span>
        </div>

        <div style="background: rgba(245, 158, 11, 0.08); border: 1px solid rgba(245, 158, 11, 0.25); border-radius: 10px; padding: 10px 12px; margin-top: 8px; font-size: 11px; color: #92400E; display: flex; gap: 8px; align-items: center;">
          <i class="ri-time-line" style="font-size: 18px; color: #D97706; flex-shrink: 0;"></i>
          <span><strong>Política de Chegada & Multa:</strong> Tolerância de até <strong>${cfg.toleranciaMinutos} minutos</strong> para conectar o veículo. Em caso de não comparecimento no local (No-Show), a vaga é reaberta e incide <strong>multa compensatória de ${cfg.multaNoShow}% (R$ ${((AppState.booking.total * cfg.multaNoShow) / 100).toFixed(2).replace('.', ',')})</strong> sobre o valor agendado.</span>
        </div>
      </div>

      <!-- Seletor de Forma de Pagamento -->
      <div class="payment-tabs">
        <button class="payment-tab-btn ${AppState.booking.paymentMethod === 'pix' ? 'active' : ''}" onclick="switchPaymentTab('pix')">
          <i class="ri-qr-code-line"></i> Pix Instantâneo
        </button>
        <button class="payment-tab-btn ${AppState.booking.paymentMethod === 'card' ? 'active' : ''}" onclick="switchPaymentTab('card')">
          <i class="ri-bank-card-line"></i> Cartão de Crédito
        </button>
      </div>

      <!-- Conteúdo Pix -->
      <div id="pix-payment-box" class="pix-content-box" style="${AppState.booking.paymentMethod === 'pix' ? '' : 'display: none;'}">
        <div class="badge badge-verified"><i class="ri-flashlight-line"></i> Aprovação Imediata</div>
        
        <div class="qr-code-wrapper">
          <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=00020126580014BR.GOV.BCB.PIX0136recargafacil-checkout-pix5204000053039865405${AppState.booking.total.toFixed(2)}5802BR" class="qr-code-img" alt="QR Code Pix">
        </div>

        <p style="font-size: 12px; color: var(--text-muted);">Escaneie o QR Code no app do seu banco ou use o Copia e Cola:</p>

        <div class="pix-copia-cola-box">
          <input type="text" readonly value="00020126580014BR.GOV.BCB.PIX0136recargafacil-checkout-pix5204000053039865405${AppState.booking.total.toFixed(2)}5802BR" class="pix-input" id="pix-copy-field">
          <button class="btn-copy-pix" onclick="copyPixCode()"><i class="ri-file-copy-line"></i> Copiar</button>
        </div>

        <div style="font-size: 11px; color: var(--text-light); display: flex; align-items: center; gap: 4px;">
          <i class="ri-time-line"></i> Este código expira em 14:59 minutos
        </div>
      </div>

      <!-- Conteúdo Cartão de Crédito -->
      <div id="card-payment-box" class="credit-card-form" style="${AppState.booking.paymentMethod === 'card' ? '' : 'display: none;'}">
        <div class="form-group">
          <label>Número do Cartão</label>
          <input type="text" class="form-input" placeholder="0000 0000 0000 0000" maxlength="19">
        </div>
        <div class="form-group">
          <label>Nome Impresso no Cartão</label>
          <input type="text" class="form-input" placeholder="Ex: CARLOS E MENDES">
        </div>
        <div class="form-row-2">
          <div class="form-group">
            <label>Validade</label>
            <input type="text" class="form-input" placeholder="MM/AA" maxlength="5">
          </div>
          <div class="form-group">
            <label>CVV</label>
            <input type="password" class="form-input" placeholder="123" maxlength="4">
          </div>
        </div>
      </div>

      <!-- Selo de Garantia -->
      <div class="d-flex align-center justify-between mt-3 text-muted" style="font-size: 12px; padding: 0 4px;">
        <span><i class="ri-lock-2-line text-primary"></i> Pagamento 100% Criptografado</span>
        <span><i class="ri-shield-check-line text-primary"></i> Garantia Recarga Fácil</span>
      </div>
    </div>

    <!-- Barra Fixa Inferior de Confirmação (Sempre Ancorada no Rodapé) -->
    <div class="sticky-bottom-action-bar">
      <button id="btn-confirm-payment" class="btn btn-primary btn-block" onclick="handleProcessPayment()">
        <i class="ri-check-double-line"></i> Confirmar e Pagar R$ ${AppState.booking.total.toFixed(2).replace('.', ',')}
      </button>
    </div>
  `;
}

function switchPaymentTab(method) {
  AppState.booking.paymentMethod = method;
  renderScreenCheckout();
}

function copyPixCode() {
  const input = document.getElementById('pix-copy-field');
  if (input) {
    input.select();
    navigator.clipboard.writeText(input.value);
    showToast("Código Pix copiado com sucesso!");
  }
}

async function handleProcessPayment() {
  const btn = document.getElementById('btn-confirm-payment');
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = `<i class="ri-loader-4-line ri-spin"></i> Processando pagamento seguro...`;
  }

  await ApiService.processPayment({
    reservaId: AppState.booking.reservaId || 1,
    total: AppState.booking.total,
    platformFee: AppState.booking.platformFee
  });

  // Registra no histórico do usuário
  const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
  if (wb) {
    AppState.userBookings.unshift({
      id: `res-${Date.now()}`,
      code: AppState.booking.reservationCode,
      wallboxTitle: wb.title,
      wallboxPhoto: wb.photo,
      power: wb.power,
      date: AppState.booking.date,
      time: `${AppState.booking.timeSlot} (${AppState.booking.durationHours}h)`,
      total: AppState.booking.total,
      fee25: AppState.booking.platformFee,
      status: "CONCLUIDA",
      address: wb.unlockedAddress ? `${wb.unlockedAddress.street}, ${wb.unlockedAddress.number} - ${wb.unlockedAddress.neighborhood}` : "Endereço liberado",
      accessNotes: wb.unlockedAddress ? wb.unlockedAddress.accessInstructions : "Acesso liberado"
    });
  }

  setTimeout(() => {
    navigateTo('screen-success');
    showToast("Pagamento aprovado! Endereço desbloqueado.");
  }, 1000);
}

// Tela 5: Sucesso & Desbloqueio do Endereço Completo e Instruções de Acesso
function renderScreenSuccess() {
  const wb = AppState.selectedWallbox || (AppState.wallboxesList && AppState.wallboxesList[0]);
  if (!wb) return;
  const cfg = getPlatformConfig();
  const addr = wb.unlockedAddress || {
    street: "Rua Antonio Perez Hernandez",
    number: "480",
    complement: "Condomínio Residencial Terra Nova - Casa 34",
    neighborhood: "Parque Campolim",
    zipCode: "18048-115",
    city: "Sorocaba",
    state: "SP",
    accessInstructions: "Identifique-se na portaria com o código de reserva. Ao entrar, vire na 2ª à esquerda, Casa 34. A Wallbox fica na garagem da direita. O anfitrião Carlos já foi notificado.",
    hostPhone: "+5515998765432"
  };
  const container = document.getElementById('success-content-placeholder');
  if (!container) return;

  container.innerHTML = `
    <div class="screen-scroll-body" style="padding: 20px 16px 30px 16px;">
      <!-- Header de Sucesso -->
      <div class="success-header-banner">
        <div class="success-icon-circle">
          <i class="ri-check-line"></i>
        </div>
        <h2>Recarga Confirmada!</h2>
        <p>Sua vaga está garantida. O anfitrião foi avisado.</p>
      </div>

      <!-- CARD DE ENDEREÇO TOTALMENTE DESBLOQUEADO -->
      <div class="unlocked-address-card">
        <div class="unlocked-header-badge">
          <i class="ri-lock-unlock-fill"></i> Endereço e Acesso Desbloqueados
        </div>
        
        <div class="full-address-text">
          ${addr.street}, nº ${addr.number}
        </div>
        <div class="complement-text">
          ${addr.complement} • Bairro ${addr.neighborhood}<br>
          CEP: ${addr.zipCode} — ${addr.city}/${addr.state}
        </div>

        <!-- Instruções Exclusivas de Acesso ao Imóvel -->
        <div class="access-instructions-box">
          <strong><i class="ri-information-line"></i> Instruções do Anfitrião:</strong><br>
          ${addr.accessInstructions}
        </div>

        <!-- Ações de Rota (Waze / Google Maps) -->
        <div class="route-actions-row">
          <a href="https://waze.com/ul?ll=${wb.lat},${wb.lng}&navigate=yes" target="_blank" class="btn btn-waze">
            <i class="ri-navigation-line"></i> Abrir no Waze
          </a>
          <a href="https://www.google.com/maps/dir/?api=1&destination=${wb.lat},${wb.lng}" target="_blank" class="btn btn-gmaps">
            <i class="ri-map-pin-line"></i> Google Maps
          </a>
        </div>

        <!-- Contato Direto com Anfitrião -->
        <a href="https://api.whatsapp.com/send?phone=${addr.hostPhone}&text=Ol%C3%A1%20${wb.hostName},%20acabei%20de%20reservar%20a%20Wallbox%20pelo%20Recarga%20F%C3%A1cil!" target="_blank" class="btn btn-whatsapp">
          <i class="ri-whatsapp-line"></i> Chamar ${wb.hostName ? wb.hostName.split(' ')[0] : 'Anfitrião'} no WhatsApp
        </a>
      </div>

      <!-- Voucher / Código de Reserva -->
      <div class="checkin-voucher">
        <div>
          <div style="font-size: 11px; color: var(--text-muted); font-weight: 600;">CÓDIGO DE IDENTIFICAÇÃO</div>
          <div class="voucher-code">${AppState.booking.reservationCode}</div>
        </div>
        <div style="text-align: right; font-size: 12px;">
          <strong>${AppState.booking.date}</strong><br>
          <span class="text-muted">${AppState.booking.timeSlot}</span>
        </div>
      </div>

      <!-- Lembrete de Tolerância de Chegada e Multa -->
      <div style="background: rgba(245, 158, 11, 0.08); border: 1px solid rgba(245, 158, 11, 0.25); border-radius: 10px; padding: 10px 14px; margin-top: 14px; font-size: 12px; color: #92400E; display: flex; align-items: flex-start; gap: 10px;">
        <i class="ri-time-line" style="font-size: 20px; color: #D97706; flex-shrink: 0; margin-top: 2px;"></i>
        <div style="line-height: 1.45;">
          <strong>Lembrete de Chegada & Tolerância:</strong> Apresente-se no local em até <strong>${cfg.toleranciaMinutos} minutos</strong> do horário marcado (${AppState.booking.timeSlot}). Em caso de não comparecimento (No-Show), a vaga é reaberta com incidência de <strong>multa compensatória de ${cfg.multaNoShow}% (R$ ${((AppState.booking.total * cfg.multaNoShow) / 100).toFixed(2).replace('.', ',')})</strong> destinada ao anfitrião.
        </div>
      </div>

      <!-- Botões de Ação -->
      <div class="d-flex gap-2 mt-4">
        <button class="btn btn-secondary" style="flex: 1;" onclick="navigateTo('screen-bookings-list')">
          <i class="ri-calendar-todo-line"></i> Ver Recargas
        </button>
        <button class="btn btn-primary" style="flex: 1;" onclick="navigateTo('screen-search')">
          <i class="ri-map-pin-2-line"></i> Ir para Mapa
        </button>
      </div>
    </div>
  `;
}

/* ==========================================================================
   8. BINDING DE EVENTOS
   ========================================================================== */
function bindNavigationEvents() {
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const target = item.getAttribute('data-target');
      if (target) {
        navigateTo(target);
      }
    });
  });
}

function bindFilterEvents() {
  document.querySelectorAll('.filter-chip[data-type]').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip[data-type]').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      AppState.activeTypeFilter = chip.getAttribute('data-type');
      renderMapMarkers();
      showToast(`Exibindo: ${chip.textContent.trim()}`);
    });
  });

  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      AppState.searchQuery = e.target.value;
      renderMapMarkers();
    });
  }
}

function showToast(message) {
  const toast = document.getElementById('app-toast');
  const msgEl = document.getElementById('toast-message');
  if (!toast || !msgEl) return;

  msgEl.textContent = message;
  toast.classList.add('show');

  setTimeout(() => {
    toast.classList.remove('show');
  }, 2800);
}

function initTimeStatusBar() {
  const timeEl = document.getElementById('status-bar-time');
  if (!timeEl) return;

  const updateTime = () => {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    timeEl.textContent = `${hours}:${minutes}`;
  };

  updateTime();
  setInterval(updateTime, 30000);
}

window.alternarModoVisualizacao = function(mode) {
  const container = document.querySelector('.app-container');
  if (!container) return;
  const btnMobile = document.getElementById('btn-view-mobile');
  const btnFullscreen = document.getElementById('btn-view-fullscreen');

  if (mode === 'mobile') {
    container.classList.remove('fullscreen-mode');
    container.classList.add('phone-frame');
    if (btnMobile) btnMobile.classList.add('active');
    if (btnFullscreen) btnFullscreen.classList.remove('active');
  } else {
    container.classList.remove('phone-frame');
    container.classList.add('fullscreen-mode');
    if (btnFullscreen) btnFullscreen.classList.add('active');
    if (btnMobile) btnMobile.classList.remove('active');
  }

  if (AppState.map) {
    setTimeout(() => AppState.map.invalidateSize(), 300);
  }
};

function bindDesktopToggle() {
  const btnMobile = document.getElementById('btn-view-mobile');
  const btnFullscreen = document.getElementById('btn-view-fullscreen');

  if (btnMobile) {
    btnMobile.addEventListener('click', () => alternarModoVisualizacao('mobile'));
  }
  if (btnFullscreen) {
    btnFullscreen.addEventListener('click', () => alternarModoVisualizacao('fullscreen'));
  }

  const legacyButtons = document.querySelectorAll('#toggle-frame-btn, .btn-view-toggle');
  legacyButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const container = document.querySelector('.app-container');
      const isCurrentlyFrame = container && container.classList.contains('phone-frame');
      alternarModoVisualizacao(isCurrentlyFrame ? 'fullscreen' : 'mobile');
    });
  });
}

// Sincronização em tempo real com alterações de parâmetros salvas no painel do Administrador
window.addEventListener('storage', (e) => {
  if (e.key === 'recarga_facil_config') {
    const cfg = getPlatformConfig();
    AppState.booking.platformFeeRate = cfg.taxaPlataforma / 100;
    if (document.getElementById('screen-booking')?.classList.contains('active')) {
      renderScreenBooking();
    } else if (document.getElementById('screen-checkout')?.classList.contains('active')) {
      renderScreenCheckout();
    }
  }
});
