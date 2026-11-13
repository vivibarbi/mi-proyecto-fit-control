let graficoPeso = null;

// Obtener token CSRF
async function obtenerCsrf() {

    const respuesta = await fetch("/api/auth/csrf", {
        method: "GET",
        credentials: "same-origin",
        headers: {
            "Accept": "application/json"
        }
    });

    if (!respuesta.ok) {
        throw new Error("No se pudo obtener el token CSRF");
    }

    return await respuesta.json();
}


// Comprobar autenticación
async function comprobarUsuario() {

    const respuesta = await fetch("/api/auth/me", {
        method: "GET",
        credentials: "same-origin",
        headers: {
            "Accept": "application/json"
        }
    });

    if (respuesta.status === 401) {
        window.location.href = "/index.html";
        return false;
    }

    if (!respuesta.ok) {
        throw new Error("No se pudo verificar tu sesión");
    }

    const usuario = await respuesta.json();

        // Obtener nombre y correo
    const nombre = usuario.nombre ||
                       usuario.name ||
                       usuario.username ||
                       usuario.email ||
                       "Usuario";

    // Mostrar nombre en bienvenida
    document.getElementById("nombreUsuario").textContent =
            nombre.toUpperCase();


    return usuario;
}


// Mostrar fecha actual
function mostrarFecha() {

    const hoy = new Date();

    document.getElementById("fechaActual").textContent =
        hoy.toLocaleDateString("es-BO");

}


// Consultar registros
async function cargarRegistros() {

    const respuesta = await fetch("/api/progreso", {
        method: "GET",
        credentials: "same-origin",
        headers: {
            "Accept": "application/json"
        }
    });

    if (respuesta.status === 401) {
        window.location.href = "/index.html";
        return;
    }

    if (!respuesta.ok) {
        throw new Error("No se pudo consultar el historial");
    }

    const registros = await respuesta.json();

    // Comprobar si ya registró su peso hoy
    const hoy = new Date().toLocaleDateString("en-CA");

    const registroHoy = registros.some(registro => {

        const fecha = registro.fecha;
            return fecha === hoy ||
               fecha === new Date().toLocaleDateString("es-BO");
        });

        // Ocultar formulario si ya registró hoy
    document.getElementById("pesoForm").hidden = registroHoy;

        // Mostrar mensaje
    document.getElementById("pesoRegistradoHoy").hidden = !registroHoy;

    mostrarHistorial(registros);

    mostrarGrafico(registros);
}


// Mostrar historial
function mostrarHistorial(registros) {

    const tabla = document.getElementById("tablaRegistros");

    tabla.replaceChildren();

    if (registros.length === 0) {

        const fila = document.createElement("tr");
        const celda = document.createElement("td");

        celda.colSpan = 2;
        celda.textContent = "No tienes registros todavía.";

        fila.appendChild(celda);
        tabla.appendChild(fila);

        return;
    }

    // Mostrar los registros más recientes primero
    const ordenados = [...registros].reverse();

    ordenados.forEach(registro => {

        const fila = document.createElement("tr");

        const fecha = document.createElement("td");
        fecha.textContent = registro.fecha;

        const peso = document.createElement("td");
        peso.textContent = Number(registro.peso).toFixed(2) + " kg";

        fila.appendChild(fecha);
        fila.appendChild(peso);

        tabla.appendChild(fila);

    });

}


// Crear o actualizar gráfico
function mostrarGrafico(registros) {

    const canvas = document.getElementById("graficoPeso");

    const sinRegistros = document.getElementById("sinRegistros");

    sinRegistros.hidden = registros.length !== 0;

    if (graficoPeso) {
        graficoPeso.destroy();
    }

    if (registros.length === 0) {
        graficoPeso = null;
        return;
    }

    const fechas = registros.map(registro => registro.fecha);

    const pesos = registros.map(registro =>
        Number(registro.peso)
    );

    graficoPeso = new Chart(canvas, {

        type: "line",

        data: {

            labels: fechas,

            datasets: [{
                label: "Mi peso (kg)",
                data: pesos,
                borderColor: "#2563eb",
                backgroundColor: "rgba(37, 99, 235, 0.15)",
                borderWidth: 3,
                fill: true,
                tension: 0.3,
                pointRadius: 5,
                pointBackgroundColor: "#2563eb"

            }]

        },

        options: {

            responsive: true,

            maintainAspectRatio: false,

            plugins: {

                legend: {
                    display: true
                }

            },

            scales: {

                y: {

                    beginAtZero: false,

                    title: {
                        display: true,
                        text: "Peso (kg)"
                    }

                },

                x: {

                    title: {
                        display: true,
                        text: "Fecha"
                    }

                }

            }

        }

    });

}


// Guardar peso
function configurarFormulario() {

    const formulario = document.getElementById("pesoForm");

    formulario.addEventListener("submit", async function(e) {

        e.preventDefault();

        const boton = document.getElementById("btnGuardar");

        const mensaje = document.getElementById("mensaje");

        const peso = Number(document.getElementById("peso").value);

        if (!Number.isFinite(peso) || peso <= 0 || peso > 1000) {

            mensaje.textContent = "Introduce un peso válido.";
            return;

        }

        boton.disabled = true;

        mensaje.textContent = "Guardando registro...";

        try {

            const csrf = await obtenerCsrf();

            const respuesta = await fetch("/api/progreso", {

                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json",
                    [csrf.headerName]: csrf.token
                },

                body: JSON.stringify({
                    peso: peso
                })

            });

            const resultado = await respuesta.json();

            if (respuesta.status === 401) {
                window.location.href = "/index.html";
                return;
            }

            if (respuesta.status === 409) {
                throw new Error(
                    resultado.error ||
                    "Ya registraste tu peso hoy."
                );

            }

            if (!respuesta.ok) {

                throw new Error(
                    resultado.error ||
                    "No se pudo guardar el peso."
                );

            }

            mensaje.textContent = "¡Peso registrado correctamente!";

            mensaje.className = "text-success";

            formulario.reset();

            // Actualizar tabla y gráfico
            await cargarRegistros();

        } catch (error) {

            console.error(error);

            mensaje.textContent = error.message;

            mensaje.className = "text-danger";

        } finally {
            boton.disabled = false;
        }

    });

}


// Cerrar sesión
async function cerrarSesion() {

    try {

        const csrf = await obtenerCsrf();

        const respuesta = await fetch("/api/auth/logout", {

            method: "POST",
            credentials: "same-origin",
            headers: {
                [csrf.headerName]: csrf.token
            }

        });

        if (!respuesta.ok) {
            throw new Error("No se pudo cerrar sesión");
        }

        window.location.href = "/index.html";

    } catch (error) {

        alert(error.message);

    }

}

window.cerrarSesion = cerrarSesion;


// Inicializar página
document.addEventListener("DOMContentLoaded", async () => {

    try {

        const usuario = await comprobarUsuario();

        if (!usuario) return;

        mostrarFecha();

        configurarFormulario();

        await cargarRegistros();

    } catch (error) {

        console.error(error);

        alert("No se pudo cargar tu módulo de progreso.");

    }

});
