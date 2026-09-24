const API_BASE = 'http://localhost:8080/api';
let listaEnvios = [];
let bitacoraActual = [];

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    const roles = JSON.parse(sessionStorage.getItem('roles') || '[]');
    document.getElementById('usuarioActual').textContent = sessionStorage.getItem('username') || '';

    if (roles.includes('ROLE_CONDUCTOR')) {
        document.getElementById('seccionRegistro').style.display = 'none';
    }
    if (!roles.includes('ROLE_ADMIN')) {
        document.getElementById('panelAdmin').style.display = 'none';
    }

    cargarEnvios();

    document.getElementById('formEnvio').addEventListener('submit', registrarEnvio);
    document.getElementById('formVehiculo').addEventListener('submit', registrarVehiculo);
});

function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

function mostrarErrores(problema) {
    const caja = document.getElementById('alertaErrores');
    let mensajes = [problema.detail || 'Ocurrió un error'];
    if (problema.errors) {
        mensajes = mensajes.concat(Object.values(problema.errors));
    }
    caja.innerHTML = mensajes.map(m => `<p>${m}</p>`).join('');
    setTimeout(() => caja.innerHTML = '', 6000);
}

async function fetchWithAuth(url, options = {}) {
    const token = sessionStorage.getItem('jwt_token');
    const headers = { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };
    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error('Sesión expirada o sin permisos');
    }
    return response;
}

async function cargarEnvios() {
    try {
        const respuesta = await fetchWithAuth(`${API_BASE}/envios/optimizados`);
        if (!respuesta.ok) throw new Error('Error al cargar');
        listaEnvios = await respuesta.json();
        renderizarEnvios(listaEnvios);
        actualizarKPIs(listaEnvios);
    } catch (error) {
        console.error('Fallo la conexión:', error);
    }
}

function actualizarKPIs(envios) {
    document.getElementById('kpiTotal').textContent = envios.length;
    document.getElementById('kpiEntregados').textContent =
        envios.filter(e => e.estadoEnvio === 'ENTREGADO').length;
    const placas = new Set(envios.map(e => e.vehiculo?.placa).filter(Boolean));
    document.getElementById('kpiVehiculos').textContent = placas.size;
}

function renderizarEnvios(envios) {
    const contenedor = document.getElementById('enviosGrid');
    contenedor.innerHTML = '';
    const roles = JSON.parse(sessionStorage.getItem('roles') || '[]');

    envios.forEach(envio => {
        const article = document.createElement('article');
        let colorPill = 'bg-pendiente';
        if (envio.estadoEnvio === 'ENTREGADO') colorPill = 'bg-entregado';
        else if (envio.estadoEnvio === 'EN_TRANSITO') colorPill = 'bg-transito';
        else if (envio.estadoEnvio === 'CANCELADO') colorPill = 'bg-cancelado';

        let botones = '';
        if (envio.estadoEnvio !== 'EN_TRANSITO' && envio.estadoEnvio !== 'ENTREGADO') {
            botones += `<button onclick="actualizarEstado(${envio.id}, 'EN_TRANSITO')">🚀 Marcar en Tránsito</button>`;
        }
        if (envio.estadoEnvio === 'EN_TRANSITO') {
            botones += `<button onclick="actualizarEstado(${envio.id}, 'ENTREGADO')">✅ Marcar Entregado</button>`;
        }
        if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_OPERADOR')) {
            botones += `<button onclick="abrirBitacora(${envio.id})">Ver Bitácora</button>`;
        }

        article.innerHTML = `
            <h3>📦 ${envio.codigoRastreo}</h3>
            <span class="pill-status ${colorPill}">${envio.estadoEnvio}</span>
            <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
            <p><strong>Peso:</strong> ${envio.pesoKg}kg | <strong>Costo:</strong> ₡${envio.costo}</p>
            <div class="botones-accion">${botones}</div>
        `;
        contenedor.appendChild(article);
    });
}

function filtrarEnvios(estado) {
    if (estado === 'TODOS') {
        renderizarEnvios(listaEnvios);
    } else {
        renderizarEnvios(listaEnvios.filter(e => e.estadoEnvio === estado));
    }
}

async function registrarEnvio(evento) {
    evento.preventDefault();
    const payload = {
        codigoRastreo: document.getElementById('codigoRastreo').value,
        destinatario: document.getElementById('destinatario').value,
        direccionDestino: document.getElementById('direccionDestino').value,
        pesoKg: parseFloat(document.getElementById('pesoKg').value),
        costo: parseFloat(document.getElementById('costo').value),
        estadoEnvio: 'PENDIENTE',
        vehiculo: { id: parseInt(document.getElementById('vehiculoId').value) },
        conductor: { id: parseInt(document.getElementById('conductorId').value) }
    };

    try {
        const respuesta = await fetchWithAuth(`${API_BASE}/envios`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        if (respuesta.ok) {
            document.getElementById('formEnvio').reset();
            cargarEnvios();
        } else {
            mostrarErrores(await respuesta.json());
        }
    } catch (error) {
        console.error(error);
    }
}

async function registrarVehiculo(evento) {
    evento.preventDefault();
    const payload = {
        placa: document.getElementById('placaVehiculo').value,
        capacidadKg: parseFloat(document.getElementById('capacidadVehiculo').value),
        estado: 'DISPONIBLE',
        empresa: { id: parseInt(document.getElementById('empresaVehiculo').value) }
    };

    try {
        const respuesta = await fetchWithAuth(`${API_BASE}/vehiculos`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        if (respuesta.ok) {
            document.getElementById('formVehiculo').reset();
        } else {
            mostrarErrores(await respuesta.json());
        }
    } catch (error) {
        console.error(error);
    }
}

async function actualizarEstado(idEnvio, nuevoEstado) {
    try {
        const respuesta = await fetchWithAuth(`${API_BASE}/envios/${idEnvio}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado })
        });

        if (respuesta.ok) {
            cargarEnvios();
        } else {
            mostrarErrores(await respuesta.json());
        }
    } catch (error) {
        console.error('Error al actualizar:', error);
    }
}

async function abrirBitacora(idEnvio) {
    try {
        const respuesta = await fetchWithAuth(`${API_BASE}/envios/${idEnvio}/bitacora`);
        if (respuesta.ok) {
            bitacoraActual = await respuesta.json();
            renderizarTablaBitacora(bitacoraActual);
            document.getElementById('modalBitacora').style.display = 'block';
        }
    } catch (error) {
        console.error('Error al cargar bitácora:', error);
    }
}

function cerrarModal() {
    document.getElementById('modalBitacora').style.display = 'none';
    document.getElementById('fechaInicio').value = '';
    document.getElementById('fechaFin').value = '';
}

function renderizarTablaBitacora(registros) {
    const tbody = document.getElementById('bodyBitacora');
    tbody.innerHTML = '';
    registros.forEach(reg => {
        const fechaStr = new Date(reg.fechaCambio).toLocaleString();
        tbody.innerHTML += `
            <tr>
                <td>${fechaStr}</td>
                <td>${reg.usuario}</td>
                <td>${reg.estadoAnterior}</td>
                <td>${reg.estadoNuevo}</td>
            </tr>
        `;
    });
}

function filtrarBitacora() {
    const inicio = document.getElementById('fechaInicio').value;
    const fin = document.getElementById('fechaFin').value;

    if (!inicio || !fin) {
        renderizarTablaBitacora(bitacoraActual);
        return;
    }

    const filtrados = bitacoraActual.filter(reg => {
        const fechaReg = new Date(reg.fechaCambio).toISOString().split('T')[0];
        return fechaReg >= inicio && fechaReg <= fin;
    });

    renderizarTablaBitacora(filtrados);
}
