const API_URL = 'http://localhost:8080/api/envios';
let listaEnvios = [];
let bitacoraActual = [];

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const roles = JSON.parse(localStorage.getItem('roles') || '[]');
    if (roles.includes('ROLE_CONDUCTOR')) {
        document.getElementById('seccionRegistro').style.display = 'none';
    }

    cargarEnvios();
    
    const form = document.getElementById('formEnvio');
    if(form) form.addEventListener('submit', registrarEnvio);
});

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('roles');
    window.location.href = 'login.html';
}

async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('jwt_token');
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };

    const response = await fetch(url, { ...options, headers });
    
    if (response.status === 401 || response.status === 403) {
        logout();
        throw new Error("Sesión expirada o sin permisos");
    }
    return response;
}

async function cargarEnvios() {
    try {
        const respuesta = await fetchWithAuth(`${API_URL}/optimizados`);
        if (!respuesta.ok) throw new Error("Error al cargar");
        listaEnvios = await respuesta.json();
        renderizarEnvios(listaEnvios);
    } catch (error) {
        console.error("Fallo la conexión:", error);
    }
}

function renderizarEnvios(envios) {
    const contenedor = document.getElementById('contenedorEnvios');
    contenedor.innerHTML = '';
    const roles = JSON.parse(localStorage.getItem('roles') || '[]');

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
            botones += `<button onclick="abrirBitacora(${envio.id})" style="margin-left:5px;">Ver Bitácora</button>`;
        }

        article.innerHTML = `
            <h3 style="margin:0;">📦 ${envio.codigoRastreo}</h3>
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
        const filtrados = listaEnvios.filter(e => e.estadoEnvio === estado);
        renderizarEnvios(filtrados);
    }
}

async function registrarEnvio(evento) {
    evento.preventDefault();
    const payload = {
        codigoRastreo: document.getElementById('codigoRastreo').value,
        direccionDestino: document.getElementById('direccionDestino').value,
        pesoKg: parseFloat(document.getElementById('pesoKg').value),
        costo: parseFloat(document.getElementById('costo').value),
        estadoEnvio: 'PENDIENTE',
        vehiculo: { id: parseInt(document.getElementById('vehiculoId').value) },
        conductor: { id: parseInt(document.getElementById('conductorId').value) }
    };

    try {
        const respuesta = await fetchWithAuth(API_URL, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        if (respuesta.ok) {
            alert('Envío registrado exitosamente!');
            document.getElementById('formEnvio').reset();
            cargarEnvios();
        } else {
            alert(`Error del Backend al registrar`);
        }
    } catch (error) {
        console.error(error);
    }
}

async function actualizarEstado(idEnvio, nuevoEstado) {
    try {
        const respuesta = await fetchWithAuth(`${API_URL}/${idEnvio}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado: nuevoEstado })
        });

        if (respuesta.ok) {
            cargarEnvios();
        } else {
            const error = await respuesta.json();
            alert(`Error: ${error.detail || error.mensaje || 'Transición inválida'}`);
        }
    } catch (error) {
        console.error("Error al actualizar:", error);
    }
}

async function abrirBitacora(idEnvio) {
    try {
        const respuesta = await fetchWithAuth(`${API_URL}/${idEnvio}/bitacora`);
        if (respuesta.ok) {
            bitacoraActual = await respuesta.json();
            renderizarTablaBitacora(bitacoraActual);
            document.getElementById('modalBitacora').style.display = 'block';
        }
    } catch (error) {
        console.error("Error al cargar bitácora:", error);
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
