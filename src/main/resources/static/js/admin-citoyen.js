document.addEventListener("DOMContentLoaded", () => {

    if (window.lucide) lucide.createIcons();

    const searchInput = document.getElementById("searchInput");
    const dateFromInput = document.getElementById("dateFrom");
    const dateToInput = document.getElementById("dateTo");
    const rows = document.querySelectorAll("tbody tr");

    function filterTable() {
        const searchValue = searchInput.value.toLowerCase().trim();
        const dateFrom = dateFromInput.value;
        const dateTo = dateToInput.value;

        rows.forEach(row => {

            // Ignore la ligne "Aucun citoyen trouvé"
            if (!row.dataset.nom) return;

            const nom = row.dataset.nom.toLowerCase();
            const prenom = row.dataset.prenom.toLowerCase();
            const rowDate = row.dataset.date; // yyyy-MM-dd

            // Filtre nom / prénom
            const matchSearch =
                nom.includes(searchValue) ||
                prenom.includes(searchValue);

            // Filtre date
            let matchDate = true;

            if (dateFrom && rowDate < dateFrom) matchDate = false;
            if (dateTo && rowDate > dateTo) matchDate = false;

            row.style.display =
                matchSearch && matchDate ? "" : "none";
        });
    }

    searchInput.addEventListener("input", filterTable);
    dateFromInput.addEventListener("change", filterTable);
    dateToInput.addEventListener("change", filterTable);
});
