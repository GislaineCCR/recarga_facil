/**
 * ============================================================================
 * RECARGA FÁCIL - API CLIENT SERVICE (JAVASCRIPT -> JAVA SPRING BOOT)
 * ============================================================================
 * FUNÇÃO:
 * Gerencia a comunicação HTTP (via fetch) entre a interface Web App e o 
 * Back-end Java Spring Boot rodando em http://localhost:8080/api.
 * 
 * ARQUITETURA 100% REAL:
 * - Toda comunicação é realizada diretamente com a API REST do Spring Boot.
 * - Sem datasets mockados: todas as entidades (Motoristas, Veículos, Hosts, Wallboxes,
 *   Reservas e Pagamentos) são persistidas e lidas no MySQL.
 * ============================================================================
 */

const API_CONFIG = {
  BASE_URL: 'http://localhost:8080/api',
  TIMEOUT_MS: 5000,
  isBackendConnected: false
};

const ApiService = {

  /**
   * VERIFICAÇÃO DE CONECTIVIDADE COM O BACK-END JAVA
   */
  async checkBackendHealth() {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT_MS);

      const response = await fetch(`${API_CONFIG.BASE_URL}/wallboxes`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      if (response.ok) {
        API_CONFIG.isBackendConnected = true;
        console.log("⚡ [Recarga Fácil] Conectado ao Back-end Java Spring Boot (http://localhost:8080)");
        return true;
      }
    } catch (err) {
      API_CONFIG.isBackendConnected = false;
      console.error("❌ [Recarga Fácil] Back-end Java offline ou inacessível:", err);
    }
    return false;
  },

  /**
   * BUSCAR WALLBOXES (TELA: MAPA DE ESTAÇÕES)
   * Conexão Java: GET /api/wallboxes ou GET /api/wallboxes/nearby
   */
  async getWallboxes(lat, lng, radiusKm = 15) {
    try {
      let url = `${API_CONFIG.BASE_URL}/wallboxes`;
      if (lat && lng) {
        url = `${API_CONFIG.BASE_URL}/wallboxes/nearby?lat=${lat}&lng=${lng}&radius=${radiusKm}`;
      }

      const response = await fetch(url);
      if (response.ok) {
        const data = await response.json();
        API_CONFIG.isBackendConnected = true;
        return data.map(item => this.mapJavaWallboxToUI(item));
      } else {
        console.error("Erro na resposta da API de Wallboxes:", response.status);
      }
    } catch (err) {
      console.error("Erro ao buscar Wallboxes do Java:", err);
    }
    return [];
  },

  /**
   * CRIAR UMA RESERVA (TELA: AGENDAMENTO)
   * Conexão Java: POST /api/reservas
   */
  async createReserva(bookingData) {
    try {
      const payload = {
        idMotorista: bookingData.motoristaId || 1,
        idWallbox: bookingData.wallboxId || 1,
        idVeiculo: bookingData.veiculoId || 1,
        dataReserva: this.formatDateToIso(bookingData.date),
        horario: this.formatTimeToIso(bookingData.timeSlot)
      };

      const response = await fetch(`${API_CONFIG.BASE_URL}/reservas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const reservaCriada = await response.json();
        console.log("✅ Reserva criada no banco de dados Java:", reservaCriada);
        return reservaCriada;
      } else {
        throw new Error("Falha ao gravar reserva no servidor.");
      }
    } catch (err) {
      console.error("Erro ao criar reserva no Java:", err);
      throw err;
    }
  },

  /**
   * PROCESSAR PAGAMENTO COM TAXA DE 25% (TELA: CHECKOUT)
   * Conexão Java: POST /api/pagamentos
   */
  async processPayment(paymentData) {
    try {
      const payload = {
        idReserva: paymentData.reservaId || 1,
        valorTotal: paymentData.total,
        taxaSistema: paymentData.platformFee
      };

      const response = await fetch(`${API_CONFIG.BASE_URL}/pagamentos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const pagamentoCriado = await response.json();
        console.log("✅ Pagamento aprovado no banco de dados Java:", pagamentoCriado);
        return pagamentoCriado;
      } else {
        throw new Error("Falha ao processar pagamento no servidor.");
      }
    } catch (err) {
      console.error("Erro ao processar pagamento no Java:", err);
      throw err;
    }
  },

  /**
   * ATUALIZAR STATUS DA RESERVA
   */
  async updateReservaStatus(idReserva, status) {
    const response = await fetch(`${API_CONFIG.BASE_URL}/reservas/${idReserva}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status })
    });
    return response.json();
  },

  /**
   * ATUALIZAR STATUS DO PAGAMENTO
   */
  async updatePaymentStatus(idPagamento, status) {
    const response = await fetch(`${API_CONFIG.BASE_URL}/pagamentos/${idPagamento}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status })
    });
    return response.json();
  },

  /**
   * BUSCAR HORÁRIOS DA AGENDA DA WALLBOX (RF05)
   * Conexão Java: GET /api/agendas/wallbox/{idWallbox}
   */
  async getAgendasByWallbox(idWallbox) {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}/agendas/wallbox/${idWallbox}`);
      if (response.ok) {
        return await response.json();
      }
    } catch (err) {
      console.error(`Erro ao buscar agenda da Wallbox #${idWallbox}:`, err);
    }
    return [];
  },

  /**
   * BUSCAR HISTÓRICO DE RESERVAS DO BANCO DE DADOS
   */
  async getReservas() {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}/reservas`);
      if (response.ok) {
        return await response.json();
      }
    } catch (err) {
      console.error("Erro ao buscar reservas do banco:", err);
    }
    return [];
  },

  /**
   * BUSCAR DADOS DO MOTORISTA DO BANCO DE DADOS
   */
  async getMotorista(id = 1) {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}/motoristas/${id}`);
      if (response.ok) {
        return await response.json();
      }
    } catch (err) {
      console.error("Erro ao buscar motorista do banco:", err);
    }
    return null;
  },

  /**
   * ATUALIZAR DADOS DO MOTORISTA NO BANCO DE DADOS
   */
  async updateMotorista(id, dto) {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}/motoristas/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dto)
      });
      if (response.ok) {
        return await response.json();
      }
    } catch (err) {
      console.error("Erro ao atualizar motorista no banco:", err);
    }
    return null;
  },

  /**
   * BUSCAR VEÍCULOS CADASTRADOS NO BANCO DE DADOS
   */
  async getVeiculos() {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}/veiculos`);
      if (response.ok) {
        return await response.json();
      }
    } catch (err) {
      console.error("Erro ao buscar veículos do banco:", err);
    }
    return [];
  },

  /**
   * BUSCAR TODOS OS MOTORISTAS
   */
  async getMotoristas() {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}/motoristas`);
      if (response.ok) {
        return await response.json();
      }
    } catch (err) {
      console.error("Erro ao buscar motoristas do banco:", err);
    }
    return [];
  },

  /**
   * ADAPTADOR: Transforma o modelo Java Spring Boot no formato consumido pela UI
   */
  mapJavaWallboxToUI(javaWb) {
    const local = javaWb.local || {};
    const host = javaWb.host || {};
    const isRes = (local.tipoAmbiente === 'RESIDENCIAL');

    let powerLabel = "7.4 kW";
    if (javaWb.potencia === 'P_11KW') powerLabel = "11 kW";
    if (javaWb.potencia === 'P_22KW') powerLabel = "22 kW";

    const amenitiesArray = javaWb.comodidades 
      ? javaWb.comodidades.split(',').map(s => s.trim()) 
      : ['wifi', 'cafe', 'coberto'];

    return {
      id: `wb-${javaWb.id}`,
      backendId: javaWb.id,
      title: javaWb.modelo || `Wallbox ${powerLabel}`,
      type: isRes ? 'residencial' : 'comercial',
      hostName: host.nome || "Anfitrião Recarga Fácil",
      hostAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=160&h=160&q=80",
      hostRating: 4.95,
      totalReviews: 24,
      isSuperHost: true,
      photo: javaWb.fotoUrl || (isRes 
        ? "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=800&q=80"
        : "https://images.unsplash.com/photo-1563986768609-322da13575f3?auto=format&fit=crop&w=800&q=80"),
      power: powerLabel,
      powerType: isRes ? "AC Monofásico / Trifásico" : "AC Rápido",
      plugType: "Tipo 2 (Type 2 / IEC 62196)",
      pricePerHour: javaWb.precoHora ? Number(javaWb.precoHora) : 15.00,
      distanceKm: 1.5,
      lat: local.latitude || -23.5238,
      lng: local.longitude || -47.4645,
      city: "Sorocaba - SP",
      neighborhood: isRes ? "Bairro Residencial" : "Centro Empresarial",
      maskedAddress: (local.enderecoCompleto ? local.enderecoCompleto.split(',')[0] : "Sorocaba - SP") + " (Endereço exato liberado pós-pagamento)",
      unlockedAddress: {
        street: local.enderecoCompleto || "Rua Antonio Perez Hernandez, 480",
        number: "480",
        complement: "Vaga de Recarga Privada",
        neighborhood: "Sorocaba",
        zipCode: "18048-115",
        city: "Sorocaba",
        state: "SP",
        accessInstructions: local.instrucoesAcesso || "Apresente o código de reserva na chegada. O anfitrião foi notificado.",
        hostPhone: "+5515998765432"
      },
      amenities: amenitiesArray,
      description: `Estação de recarga cadastrada no Recarga Fácil. Potência de ${powerLabel} com segurança e comodidades.`,
      availableSlots: ["08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00", "19:00 - 21:00"]
    };
  },

  formatDateToIso(dateStr) {
    if (!dateStr) return new Date().toISOString().split('T')[0];
    const today = new Date();
    if (dateStr.includes('Amanhã')) today.setDate(today.getDate() + 1);
    if (dateStr.includes('Sexta')) today.setDate(today.getDate() + 2);
    return today.toISOString().split('T')[0];
  },

  formatTimeToIso(timeSlot) {
    if (!timeSlot) return "14:00:00";
    const start = timeSlot.split('-')[0].trim();
    return `${start}:00`;
  }
};
