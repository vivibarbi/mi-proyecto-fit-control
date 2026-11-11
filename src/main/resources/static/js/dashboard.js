async function comprobarUsuario() {

    try {

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
            throw new Error(
                "No se pudo obtener la información del usuario"
            );
        }

        const usuario = await respuesta.json();

        // Obtener nombre y correo
        const nombre = usuario.nombre ||
                       usuario.name ||
                       usuario.username ||
                       usuario.email ||
                       "Usuario";

        const email = usuario.email ||
                      usuario.username ||
                      "";

        // Mostrar nombre en la portada
        document.getElementById("nombreUsuario").textContent =
            nombre;

        // Mostrar nombre en bienvenida
        document.getElementById("nombreBienvenida").textContent =
            nombre.toUpperCase();

        // Mostrar datos del perfil de cuenta
        document.getElementById("perfilNombre").textContent =
            nombre;

        document.getElementById("perfilEmail").textContent =
            email;

        return true;

    } catch (error) {

        console.error("Error al cargar el usuario:", error);

        alert("No se pudo cargar tu información.");

        return false;
    }
}

//OBTENER TOKEN CSRF
async function actualizarCsrf() {

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

// CERRAR SESIÓN
async function cerrarSesion() {

    try {

        const csrf = await actualizarCsrf();

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

        console.error(error);

        alert(error.message);
    }
}

// Permitir que el HTML utilice onclick="cerrarSesion()"
window.cerrarSesion = cerrarSesion;


//  CONSULTAR INFORMACIÓN FÍSICA
async function cargarPerfil() {

    const formulario = document.getElementById("formularioPerfil");
    const datosPerfil = document.getElementById("datosPerfil");
    const mensaje = document.getElementById("mensajePerfil");

    try {

        const respuesta = await fetch("/api/perfil", {
            method: "GET",
            credentials: "same-origin",
            headers: {
                "Accept": "application/json"
            }
        });

        // Si la sesión expiró
        if (respuesta.status === 401) {
            window.location.href = "/index.html";
            return;
        }

        // Si todavía no tiene información
        if (respuesta.status === 404) {

            formulario.hidden = false;
            datosPerfil.hidden = true;

            return;
        }

        if (!respuesta.ok) {
            throw new Error("Error al consultar tu información");
        }

        const perfil = await respuesta.json();

        // Mostrar información registrada
        document.getElementById("verEdad").textContent =
            perfil.edad;

        document.getElementById("verPeso").textContent =
            perfil.peso;

        document.getElementById("verSexo").textContent =
            perfil.sexo;

        document.getElementById("verAltura").textContent =
            perfil.altura;

        document.getElementById("verPais").textContent =
            perfil.pais;

        document.getElementById("verPreferencia").textContent =
            perfil.preferencia;

        // Ocultar formulario y mostrar datos
        formulario.hidden = true;
        datosPerfil.hidden = false;

    } catch (error) {

        console.error("Error al cargar perfil:", error);

        if (mensaje) {
            mensaje.textContent =
                "No se pudo cargar tu información. Intenta nuevamente.";
        }
    }
}


// GUARDAR INFORMACIÓN FÍSICA
async function configurarFormularioPerfil() {

    const perfilForm = document.getElementById("perfilForm");
    const mensaje = document.getElementById("mensajePerfil");

    perfilForm.addEventListener("submit", async (e) => {

        e.preventDefault();

        mensaje.textContent = "";

        const boton = perfilForm.querySelector(
            'button[type="submit"]'
        );

        boton.disabled = true;

        try {

            // Obtener datos del formulario
            const datos = Object.fromEntries(
                new FormData(perfilForm).entries()
            );

            datos.edad = Number(datos.edad);
            datos.peso = Number(datos.peso);
            datos.altura = Number(datos.altura);

            // Obtener token CSRF
            const csrf = await actualizarCsrf();

            // Enviar información al servidor
            const respuesta = await fetch("/api/perfil", {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json",
                    [csrf.headerName]: csrf.token
                },
                body: JSON.stringify(datos)

            });

            if (respuesta.status === 401) {

                window.location.href = "/index.html";
                return;
            }

            const resultado = await respuesta.json();

            if (!respuesta.ok) {

                throw new Error(
                    resultado.mensaje ||
                    resultado.error ||
                    "No se pudo guardar la información"
                );
            }

            alert("¡Información guardada correctamente!");

            perfilForm.reset();

            // Volver a consultar los datos guardados
            await cargarPerfil();

        } catch (error) {

            console.error("Error al guardar perfil:", error);

            mensaje.textContent =
                error.message ||
                "Ocurrió un error al guardar tu información.";

        } finally {

            boton.disabled = false;
        }

    });
}


//  INICIALIZAR DASHBOARD
document.addEventListener("DOMContentLoaded", async () => {

    // Primero comprobar que el usuario esté autenticado
    const autenticado = await comprobarUsuario();

    // Si no está autenticado, no continuar
    if (!autenticado) {
        return;
    }

    // Configurar formulario
    await configurarFormularioPerfil();

    // Consultar información física
    await cargarPerfil();

});
