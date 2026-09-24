const API_ENVIOS = 'http://localhost:8080/api/v1/envios';
const filtros = document.getElementById('filtros');
const procedimiento = document.getElementById('procedimiento');
const indicador = document.getElementById('indicador');
const errorBox = document.getElementById('error');
const botones = ['primera', 'anterior', 'siguiente', 'ultima'].map(id => document.getElementById(id));
let currentPage = 0;
let totalPages = 0;
let cargando = false;

function salir() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}

document.getElementById('salir').addEventListener('click', salir);
document.getElementById('usuarioActual').textContent = sessionStorage.getItem('username') || '';
filtros.addEventListener('submit', evento => {
    evento.preventDefault();
    cargarPagina(0);
});
filtros.addEventListener('change', evento => {
    if (evento.target.tagName === 'SELECT') cargarPagina(0);
});
botones[0].addEventListener('click', () => cargarPagina(0));
botones[1].addEventListener('click', () => cargarPagina(currentPage - 1));
botones[2].addEventListener('click', () => cargarPagina(currentPage + 1));
botones[3].addEventListener('click', () => cargarPagina(totalPages - 1));

function renderizarFilas(envios) {
    const tbody = document.getElementById('enviosBody');
    tbody.replaceChildren();
    for (const envio of envios) {
        const fila = document.createElement('tr');
        const valores = [envio.codigoRastreo, envio.destinatario || 'Sin registrar', envio.direccionDestino,
            Number(envio.montoFlete).toLocaleString('es-CR', { style: 'currency', currency: 'CRC' }), envio.estado];
        for (const valor of valores) {
            const celda = document.createElement('td');
            celda.textContent = valor;
            fila.appendChild(celda);
        }
        tbody.appendChild(fila);
    }
    if (envios.length === 0) {
        const celda = document.createElement('td');
        celda.colSpan = 5;
        celda.textContent = 'No hay envíos para esta consulta.';
        const fila = document.createElement('tr');
        fila.appendChild(celda);
        tbody.appendChild(fila);
    }
}

async function cargarPagina(page) {
    if (cargando) return;
    const token = sessionStorage.getItem('jwt_token');
    if (!token) {
        salir();
        return;
    }
    cargando = true;
    errorBox.textContent = '';
    indicador.textContent = 'Cargando envíos...';
    botones.forEach(boton => boton.disabled = true);
    Array.from(filtros.elements).forEach(control => control.disabled = true);
    const estadoSP = procedimiento.value;
    const parametros = new URLSearchParams({ page });
    for (const campo of ['size', 'sortBy', 'direction', 'busqueda', 'estado']) {
        parametros.set(campo, document.getElementById(campo).value);
    }
    const url = estadoSP ? `${API_ENVIOS}/procedimiento/${estadoSP}` : `${API_ENVIOS}?${parametros}`;
    try {
        const respuesta = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
        if (respuesta.status === 401) {
            salir();
            return;
        }
        if (!respuesta.ok) {
            const problema = await respuesta.json().catch(() => ({}));
            throw new Error(problema.detail || (respuesta.status === 403
                ? 'No tiene permisos para consultar los envíos. Inicie sesión de nuevo si el token venció.'
                : 'No se pudo completar la consulta.'));
        }
        const data = await respuesta.json();
        if (estadoSP) {
            renderizarFilas(data);
            indicador.textContent = `SP_OBTENER_ENVIOS_POR_ESTADO: ${estadoSP} (Total: ${data.length} envíos). Orden: fecha de creación descendente.`;
        } else {
            currentPage = data.number;
            totalPages = data.totalPages;
            renderizarFilas(data.content);
            indicador.textContent = `Página ${totalPages === 0 ? 0 : currentPage + 1} de ${totalPages} (Total: ${data.totalElements} envíos)`;
            botones[0].disabled = botones[1].disabled = data.first || totalPages === 0;
            botones[2].disabled = botones[3].disabled = data.last || totalPages === 0;
        }
    } catch (error) {
        renderizarFilas([]);
        errorBox.textContent = error.message;
        indicador.textContent = 'Consulta no disponible.';
    } finally {
        cargando = false;
        Array.from(filtros.elements).forEach(control => control.disabled = false);
        for (const campo of ['size', 'sortBy', 'direction', 'busqueda', 'estado']) {
            document.getElementById(campo).disabled = Boolean(estadoSP);
        }
    }
}

cargarPagina(0);
