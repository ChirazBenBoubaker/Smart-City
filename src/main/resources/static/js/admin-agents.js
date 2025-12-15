// src/main/resources/static/js/admin-agents.js

let formToSubmit = null;

function openAgentModal() {
    const modal = document.getElementById("agentModal");
    if (modal) modal.style.display = "flex";
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
        if (txt) txt.textContent = `Voulez-vous vraiment désactiver ${agentName} ?`;

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
