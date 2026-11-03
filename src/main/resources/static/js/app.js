let csrfToken = null;
let csrfHeader = null;

const authSection = document.getElementById("authSection");
const appContent = document.getElementById("appContent");
const authMessage = document.getElementById("authMessage");

async function actualizarCsrf() {
    const respuesta = await fetch("/api/auth/csrf");

    if (!respuesta.ok) {
        throw new Error("No se pudo obtener el token de seguridad");
    }

    const datos = await respuesta.json();

    csrfToken = datos.token;
    csrfHeader = datos.headerName;
}

function encabezadosCsrf() {
    if (!csrfToken || !csrfHeader) {
        throw new Error("Token de seguridad no disponible");
    }

    return { [csrfHeader]: csrfToken };
}

function mostrarAplicacion() {
    authSection.hidden = true;
    appContent.hidden = false;
}

function mostrarAcceso() {
    authSection.hidden = false;
    appContent.hidden = true;
}

function mostrarMensaje(mensaje) {
    authMessage.textContent = mensaje;
}

async function comprobarSesion() {
    try {
        await actualizarCsrf();

        const respuesta = await fetch("/api/auth/me", {
            credentials: "same-origin"
        });

        if (respuesta.status === 401) {
            mostrarAcceso();
            return;
        }

        if (!respuesta.ok) {
            throw new Error("No se pudo comprobar la sesión");
        }

        const usuario = await respuesta.json();

        mostrarAplicacion();

    } catch (error) {
        mostrarAcceso();
        mostrarMensaje(error.message);
    }
}
// Cambiar entre inicio de sesión y registro

document.getElementById("showRegister")
    .addEventListener("click", () => {
        document.getElementById("loginView").hidden = true;
        document.getElementById("registerView").hidden = false;
        mostrarMensaje("");
    });

document.getElementById("showLogin")
    .addEventListener("click", () => {
        document.getElementById("registerView").hidden = true;
        document.getElementById("loginView").hidden = false;
        mostrarMensaje("");
    });


// Registro de usuario

document.getElementById("registerForm")
    .addEventListener("submit", async function(e) {
        e.preventDefault();

        try {
            await actualizarCsrf();

            const datos = {
                nombre: document.getElementById("registerName").value,
                email: document.getElementById("registerEmail").value,
                password: document.getElementById("registerPassword").value
            };

            const respuesta = await fetch("/api/auth/registro", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...encabezadosCsrf()
                },
                body: JSON.stringify(datos)
            });

            const resultado = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(resultado.error || "No se pudo crear la cuenta");
            }

            document.getElementById("registerForm").reset();
            document.getElementById("registerView").hidden = true;
            document.getElementById("loginView").hidden = false;

            mostrarMensaje("Cuenta creada. Ahora puedes iniciar sesión.");

        } catch (error) {
            mostrarMensaje(error.message);
        }
    });

// Inicio de sesión

document.getElementById("loginForm")
    .addEventListener("submit", async function(e) {
        e.preventDefault();

        try {
            await actualizarCsrf();

            const datos = new URLSearchParams();

            datos.append(
                "email",
                document.getElementById("loginEmail").value
            );

            datos.append(
                "password",
                document.getElementById("loginPassword").value
            );

            const respuesta = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    ...encabezadosCsrf()
                },
                body: datos.toString()
            });

            if (!respuesta.ok) {
                throw new Error("Correo o contraseña incorrectos");
            }

            await actualizarCsrf();
            mostrarMensaje("");
            mostrarAplicacion();

        } catch (error) {
            mostrarMensaje(error.message);
        }
    });

// Cerrar sesión

async function cerrarSesion() {
    try {
        await actualizarCsrf();

        const respuesta = await fetch("/api/auth/logout", {
            method: "POST",
            headers: encabezadosCsrf()
        });

        if (!respuesta.ok) {
            throw new Error("No se pudo cerrar la sesión");
        }

        mostrarAcceso();

    } catch (error) {
        alert(error.message);
    }
}

// Calculadora DE FITCONTROL

const form = document.getElementById("fitnessForm");

form.addEventListener("submit", async function(e) {
    e.preventDefault();

    const peso = document.getElementById("peso").value;
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
            `/api/fitness/calcular?${params}`
        );

        const data = await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(data.message || "No se pudo calcular");
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

    } catch(error) {
        alert(error.message);
    }
});

// Comprobar sesión al cargar la página

comprobarSesion();
