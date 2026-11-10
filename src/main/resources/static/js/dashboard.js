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
            return;
        }

        if (!respuesta.ok) {
            throw new Error("No se pudo obtener la información del usuario");
        }

        const usuario = await respuesta.json();

        // Obtener nombre y correo
        const nombre = usuario.nombre || usuario.name ||
                       usuario.username || usuario.email || "Usuario";

        const email = usuario.email || usuario.username || "";

        // Mostrar nombre en la portada
        document.getElementById("nombreUsuario").textContent = nombre;

        // Mostrar nombre en el título
        document.getElementById("nombreBienvenida").textContent =
            nombre.toUpperCase();

        // Mostrar datos del perfil
        document.getElementById("perfilNombre").textContent = nombre;

        document.getElementById("perfilEmail").textContent = email;

    } catch (error) {

        console.error("Error al cargar el usuario:", error);

        alert("No se pudo cargar tu información.");

    }
}


// Obtener token CSRF
async function actualizarCsrf() {

    const respuesta = await fetch("/api/auth/csrf");

    if (!respuesta.ok) {
        throw new Error("No se pudo obtener el token CSRF");
    }

    return await respuesta.json();
}


// Cerrar sesión
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

        alert(error.message);

    }
}


// Ejecutar al cargar el dashboard
document.addEventListener("DOMContentLoaded", comprobarUsuario);
