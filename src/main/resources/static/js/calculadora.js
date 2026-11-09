let csrfToken = null;
let csrfHeader = null;
let graficoPeso = null;

// Obtener token CSRF
async function actualizarCsrf() {

    const respuesta = await fetch("/api/auth/csrf");

    if (!respuesta.ok) {
        throw new Error("No se pudo obtener el token CSRF");
    }

    const datos = await respuesta.json();

    csrfToken = datos.token;
    csrfHeader = datos.headerName;
}

function encabezadosCsrf() {

    if (!csrfToken || !csrfHeader) {
        throw new Error("Token CSRF no disponible");
    }

    return {
        [csrfHeader]: csrfToken
    };
}

// Comprobar usuario
async function cargarUsuario() {

    const respuesta = await fetch("/api/auth/me", {
        credentials: "same-origin"
    });

    if (respuesta.status === 401) {
        window.location.href = "/";
        return;
    }

    if (!respuesta.ok) {
        throw new Error("No se pudo obtener el usuario");
    }

    const usuario = await respuesta.json();

    document.getElementById("nombreUsuario").textContent =
        usuario.nombre;

    document.getElementById("saludoUsuario").textContent =
        "¡Hola, " + usuario.nombre + "!";

}

// CERRAR SESIÓN
document.getElementById("btnCerrarSesion")
    .addEventListener("click", async () => {

        try {

            await actualizarCsrf();

            const respuesta = await fetch("/api/auth/logout", {
                method: "POST",
                headers: encabezadosCsrf(),
                credentials: "same-origin"
            });

            if (!respuesta.ok) {
                throw new Error("No se pudo cerrar sesión");
            }

            window.location.href = "/";

        } catch (error) {
            alert(error.message);
        }
    });


// CALCULADORA FITNESS
document.getElementById("fitnessForm")
    .addEventListener("submit", async function(e) {

        e.preventDefault();

        const peso = Number(document.getElementById("peso").value);
        const altura = document.getElementById("altura").value;
        const edad = document.getElementById("edad").value;
        const sexo = document.getElementById("sexo").value;
        const actividad = document.getElementById("actividad").value;
        const objetivo = document.getElementById("objetivo").value;

        const params = new URLSearchParams({
            peso, altura, edad, sexo, actividad, objetivo
        });

        try {

            const respuesta = await fetch(
                `/api/fitness/calcular?${params}`,
                { credentials: "same-origin" }
            );

            const data = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(data.message || "Error al calcular");
            }

            document.getElementById("tmb").textContent = data.tmb;
            document.getElementById("tdee").textContent = data.tdee;
            document.getElementById("calorias").textContent =
                data.caloriasObjetivo;

            document.getElementById("imc").textContent = data.imc;

            document.getElementById("proteinas").textContent =
                data.proteinas + " g";

            document.getElementById("carbohidratos").textContent =
                data.carbohidratos + " g";

            document.getElementById("grasas").textContent =
                data.grasas + " g";

            document.getElementById("resultados")
                .classList.remove("hidden");

            document.getElementById("macros")
                .classList.remove("hidden");

            // Guardar el peso del usuario
            await guardarPeso(peso);

            // Actualizar historial
            await cargarHistorial();

        } catch (error) {
            alert(error.message);
        }
    });


// GUARDAR PESO
async function guardarPeso(peso) {

    await actualizarCsrf();

    const respuesta = await fetch("/api/peso/guardar", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...encabezadosCsrf()
        },
        credentials: "same-origin",
        body: JSON.stringify({ peso: peso })
    });

    if (!respuesta.ok) {
        throw new Error("No se pudo guardar el registro de peso");
    }

    document.getElementById("mensajePeso").textContent =
        "¡Tu peso se guardó correctamente!";
}


// CONSULTAR HISTORIAL
async function cargarHistorial() {

    const respuesta = await fetch("/api/peso/historial", {
        credentials: "same-origin"
    });

    if (!respuesta.ok) {
        throw new Error("No se pudo cargar el historial");
    }

    const registros = await respuesta.json();

    const tabla = document.getElementById("tablaPesos");

    tabla.innerHTML = "";

    registros.forEach(registro => {

        const fila = document.createElement("tr");

        const fecha = document.createElement("td");
        fecha.textContent = registro.fecha;

        const peso = document.createElement("td");
        peso.textContent = registro.peso + " kg";

        fila.appendChild(fecha);
        fila.appendChild(peso);

        tabla.appendChild(fila);
    });

    crearGrafico(registros);
}


// GRÁFICO DE EVOLUCIÓN
function crearGrafico(registros) {

    const contexto =
        document.getElementById("graficoPeso").getContext("2d");

    if (graficoPeso) {
        graficoPeso.destroy();
    }

    graficoPeso = new Chart(contexto, {

        type: "line",

        data: {
            labels: registros.map(r => r.fecha),

            datasets: [{
                label: "Mi peso (kg)",
                data: registros.map(r => r.peso),
                borderColor: "#e50914",
                backgroundColor: "rgba(229,9,20,0.15)",
                borderWidth: 3,
                tension: 0.3,
                fill: true
            }]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false,

            scales: {
                y: {
                    beginAtZero: false
                }
            }
        }
    });
}


// INICIAR CALCULADORA
async function iniciarCalculadora() {

    try {

        await actualizarCsrf();
        await cargarUsuario();
        await cargarHistorial();

    } catch (error) {

        console.error(error);
        window.location.href = "/";
    }
}

iniciarCalculadora();