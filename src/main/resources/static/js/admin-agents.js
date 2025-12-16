// src/main/resources/static/js/admin-agents.js

let formToSubmit = null;

function openAgentModal() {
    const modal = document.getElementById("agentModal");
    if (!modal) return;

    const form = modal.querySelector("form");

    // 1️⃣ reset champs
    if (form) form.reset();

    // 2️⃣ supprimer styles d’erreur
    modal.querySelectorAll(".sc-input-error")
        .forEach(el => el.classList.remove("sc-input-error"));

    // 3️⃣ cacher messages d’erreur
    modal.querySelectorAll(".sc-error")
        .forEach(el => el.style.display = "none");

    // 4️⃣ réactiver bouton submit
    const submitBtn = modal.querySelector("button[type='submit']");
    if (submitBtn) submitBtn.disabled = false;

    // 5️⃣ afficher modal
    modal.style.display = "flex";
}


function closeAgentModal() {
    const modal = document.getElementById("agentModal");
    if (modal) modal.style.display = "none";
}

function openDisableModal() {
    const modal = document.getElementById("disableModal");
    if (modal) modal.style.display = "flex";
}

function closeDisableModal() {
    const modal = document.getElementById("disableModal");
    if (modal) modal.style.display = "none";
    formToSubmit = null;
}

function confirmDisable() {
    if (formToSubmit) formToSubmit.submit();
}

document.addEventListener("DOMContentLoaded", () => {
    // Lucide icons
    if (window.lucide) lucide.createIcons();

    // Click sur icone poubelle => ouvrir modal suppression
    document.addEventListener("click", function (e) {
        const btn = e.target.closest(".sc-trash-icon");
        if (!btn) return;

        formToSubmit = btn.closest("form");

        const agentName = btn.getAttribute("data-agent-name") || "cet agent";
        const txt = document.getElementById("disableText");
        if (txt) txt.textContent = `Voulez-vous vraiment supprimer ${agentName} ?`;

        openDisableModal();
    });

    // Fermer modal ajout si clic sur overlay
    document.addEventListener("click", function (e) {
        const modal = document.getElementById("agentModal");
        if (modal && modal.style.display === "flex" && e.target === modal) {
            closeAgentModal();
        }
    });

    // Fermer modal suppression si clic sur overlay
    document.addEventListener("click", function (e) {
        const modal = document.getElementById("disableModal");
        if (modal && modal.style.display === "flex" && e.target === modal) {
            closeDisableModal();
        }
    });

    // ESC ferme les modals
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            closeAgentModal();
            closeDisableModal();
        }
    });

    // Si le backend envoie showAgentModal=true => réouvrir le modal
    const show = document.body.getAttribute("data-show-agent-modal");
    if (show === "true") {
        openAgentModal();
    }
});
document.addEventListener("DOMContentLoaded", () => {

    const searchInput = document.getElementById("searchInput");
    const departmentFilter = document.getElementById("departmentFilter");
    const rows = document.querySelectorAll("tbody tr");

    function filterTable() {
        const searchValue = searchInput.value.toLowerCase().trim();
        const departmentValue = departmentFilter.value;

        rows.forEach(row => {

            // Ignore la ligne "Aucun agent trouvé"
            if (!row.dataset.nom) return;

            const nom = row.dataset.nom.toLowerCase();
            const prenom = row.dataset.prenom.toLowerCase();
            const departement = row.dataset.departement;

            const matchSearch =
                nom.includes(searchValue) ||
                prenom.includes(searchValue);

            const matchDepartment =
                !departmentValue || departement === departmentValue;

            row.style.display =
                matchSearch && matchDepartment ? "" : "none";
        });
    }

    searchInput.addEventListener("input", filterTable);
    departmentFilter.addEventListener("change", filterTable);
});
document.addEventListener("DOMContentLoaded", () => {

    const emailInput = document.getElementById("emailInput");
    const emailError = document.getElementById("emailError");

    if (!emailInput) return;

    let timeout = null;

    emailInput.addEventListener("input", () => {

        // reset visuel
        emailError.style.display = "none";
        emailInput.closest(".sc-input")
            .classList.remove("sc-input-error");

        const email = emailInput.value.trim();

        if (email.length < 5 || !email.includes("@")) return;

        // debounce (évite spam serveur)
        clearTimeout(timeout);
        timeout = setTimeout(() => {

            fetch(`/admin/users/check-email?email=${encodeURIComponent(email)}`)
                .then(res => res.json())
                .then(exists => {

                    if (exists) {
                        emailError.style.display = "block";
                        emailInput.closest(".sc-input")
                            .classList.add("sc-input-error");
                    }

                })
                .catch(() => {
                    console.error("Erreur vérification email");
                });

        }, 400);
    });
});
document.addEventListener("DOMContentLoaded", () => {

    const emailInput = document.getElementById("emailInput");
    const emailError = document.getElementById("emailError");
    const submitBtn = document.querySelector("button[type='submit']");

    if (!emailInput) return;

    let timeout = null;

    emailInput.addEventListener("input", () => {

        emailError.style.display = "none";
        emailInput.closest(".sc-input")
            .classList.remove("sc-input-error");

        submitBtn.disabled = false;

        const email = emailInput.value.trim();

        if (email.length < 5 || !email.includes("@")) return;

        clearTimeout(timeout);
        timeout = setTimeout(() => {

            fetch(`/admin/users/check-email?email=${encodeURIComponent(email)}`)
                .then(res => res.json())
                .then(exists => {

                    if (exists) {
                        emailError.style.display = "block";
                        emailInput.closest(".sc-input")
                            .classList.add("sc-input-error");
                        submitBtn.disabled = true; // ⛔ bloque submit
                    }

                });

        }, 400);
    });
});
