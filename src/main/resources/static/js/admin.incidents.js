document.addEventListener("DOMContentLoaded", () => {

    const categoryFilter = document.getElementById("categoryFilter");
    const statusFilter = document.getElementById("statusFilter");
    const priorityFilter = document.getElementById("priorityFilter");

    // sécurité (comme pour Agents)
    if (!categoryFilter || !statusFilter || !priorityFilter) return;

    const rows = document.querySelectorAll("tbody tr");

    function filterIncidents() {
        const categoryValue = categoryFilter.value;
        const statusValue = statusFilter.value;
        const priorityValue = priorityFilter.value;

        rows.forEach(row => {

            // 🚫 ignorer la ligne "Aucun incident trouvé"
            if (!row.dataset.categorie) return;

            const rowCategory = row.dataset.categorie;
            const rowStatus = row.dataset.statut;
            const rowPriority = row.dataset.priorite;

            const matchCategory =
                !categoryValue || rowCategory === categoryValue;

            const matchStatus =
                !statusValue || rowStatus === statusValue;

            const matchPriority =
                !priorityValue || rowPriority === priorityValue;

            row.style.display =
                matchCategory && matchStatus && matchPriority ? "" : "none";
        });
    }

    categoryFilter.addEventListener("change", filterIncidents);
    statusFilter.addEventListener("change", filterIncidents);
    priorityFilter.addEventListener("change", filterIncidents);

    console.log("✅ Filtres incidents actifs");
});
