let grafico = null;

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

// Gráfico de peso

async function cargarPesos() {

    try {

        const respuesta = await fetch("/api/peso");

        if (!respuesta.ok) {
            throw new Error("Error al cargar pesos");
        }

        const datos = await respuesta.json();

        const fechas = datos.map(x => x.fecha);
        const pesos = datos.map(x => x.peso);

        const ctx = document.getElementById("graficoPeso");

        if (grafico) {
            grafico.destroy();
        }

        grafico = new Chart(ctx, {
            type: "line",

            data: {
                labels: fechas,

                datasets: [{
                    label: "Peso corporal (kg)",
                    data: pesos,
                    borderWidth: 3,
                    tension: 0.3
                }]
            },

            options: {
                responsive: true,
                scales: {
                    y: {
                        title: {
                            display: true,
                            text: "Peso en kg"
                        }
                    }
                }
            }
        });

    } catch(error) {
        console.error(error);
    }
}

// Guardar nuevo peso

document.getElementById("pesoForm")
.addEventListener("submit", async function(e) {

    e.preventDefault();

    const peso = Number(
        document.getElementById("nuevoPeso").value
    );

    try {

        const respuesta = await fetch("/api/peso", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ peso })
        });

        if (!respuesta.ok) {
            throw new Error("No se pudo guardar el peso");
        }

        document.getElementById("nuevoPeso").value = "";

        await cargarPesos();

        alert("Peso registrado correctamente");

    } catch(error) {
        alert(error.message);
    }
});

cargarPesos();
