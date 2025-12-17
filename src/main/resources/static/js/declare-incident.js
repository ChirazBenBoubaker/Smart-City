 // Initialize Lucide icons
    lucide.createIcons();

    // Variables pour la carte
    let lat = 36.8065, lng = 10.1815;
    const map = L.map('map').setView([lat, lng], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

    const marker = L.marker([lat, lng], { draggable: true }).addTo(map);

    marker.on('dragend', e => {
    const position = e.target.getLatLng();
    lat = position.lat;
    lng = position.lng;
});

    // Variables pour le rate limiting
    let isRateLimited = false;
    let rateLimitTimer = null;
    let countdownInterval = null;

    function getCsrfToken() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'XSRF-TOKEN') {
    return decodeURIComponent(value);
}
}
    return null;
}

    function showError(message, details = []) {
    const errorAlert = document.getElementById('errorAlert');
    const errorList = document.getElementById('errorList');

    document.getElementById('successAlert').style.display = 'none';
    document.getElementById('rateLimitAlert').style.display = 'none';

    if (details.length > 0) {
    errorList.innerHTML = '<ul>' + details.map(d => `<li>${d}</li>`).join('') + '</ul>';
} else {
    errorList.innerHTML = `<p>${message}</p>`;
}

    errorAlert.style.display = 'flex';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

    function showSuccess() {
    document.getElementById('successAlert').style.display = 'flex';
    document.getElementById('errorAlert').style.display = 'none';
    document.getElementById('rateLimitAlert').style.display = 'none';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

    function showRateLimit(retryAfterSeconds = 60) {
    isRateLimited = true;
    const rateLimitAlert = document.getElementById('rateLimitAlert');
    const retryTimer = document.getElementById('retryTimer');
    const countdownElement = document.getElementById('countdown');
    const submitBtn = document.getElementById('submitBtn');

    // Masquer les autres alertes
    document.getElementById('successAlert').style.display = 'none';
    document.getElementById('errorAlert').style.display = 'none';

    // Afficher l'alerte de rate limit
    rateLimitAlert.style.display = 'flex';
    retryTimer.style.display = 'flex';

    // Désactiver le bouton
    submitBtn.disabled = true;
    submitBtn.innerHTML = `
            <i data-lucide="lock"></i>
            Temporairement désactivé
        `;
    lucide.createIcons();

    // Démarrer le compte à rebours
    let remainingSeconds = retryAfterSeconds;
    countdownElement.textContent = remainingSeconds;

    if (countdownInterval) clearInterval(countdownInterval);

    countdownInterval = setInterval(() => {
    remainingSeconds--;
    countdownElement.textContent = remainingSeconds;

    if (remainingSeconds <= 0) {
    clearInterval(countdownInterval);
    resetRateLimit();
}
}, 1000);

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

    function resetRateLimit() {
    isRateLimited = false;
    const rateLimitAlert = document.getElementById('rateLimitAlert');
    const retryTimer = document.getElementById('retryTimer');

    rateLimitAlert.style.display = 'none';
    retryTimer.style.display = 'none';

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = false;
    submitBtn.innerHTML = `
            <i data-lucide="send"></i>
            Envoyer la déclaration
        `;
    lucide.createIcons();

    if (countdownInterval) clearInterval(countdownInterval);
}

    function setLoading(isLoading) {
    const btn = document.getElementById('submitBtn');
    if (isLoading) {
    btn.disabled = true;
    btn.innerHTML = `
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10" opacity="0.25"/>
                    <path d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" opacity="0.75"/>
                </svg>
                Envoi en cours...
            `;
} else {
    btn.disabled = false;
    btn.innerHTML = `
                <i data-lucide="send"></i>
                Envoyer la déclaration
            `;
    lucide.createIcons();
}
}

    async function envoyer() {
    if (isRateLimited) return;

    const titre = document.getElementById('titre').value;
    const description = document.getElementById('description').value;
    const categorie = document.getElementById('categorie').value;
    const priorite = document.getElementById('priorite').value;
    const photos = document.getElementById('photos').files;

    // Validation côté client
    const errors = [];
    if (!titre.trim()) errors.push("Le titre est obligatoire");
    else if (titre.length < 5) errors.push("Le titre doit contenir au moins 5 caractères");
    else if (titre.length > 100) errors.push("Le titre ne peut pas dépasser 100 caractères");

    if (!description.trim()) errors.push("La description est obligatoire");
    else if (description.length < 10) errors.push("La description doit contenir au moins 10 caractères");
    else if (description.length > 1000) errors.push("La description ne peut pas dépasser 1000 caractères");

    if (photos.length > 5) errors.push("Vous ne pouvez télécharger que 5 photos maximum");

    if (errors.length > 0) {
    showError("Veuillez corriger les erreurs suivantes :", errors);
    return;
}

    const fd = new FormData();
    fd.append("titre", titre);
    fd.append("description", description);
    fd.append("categorie", categorie);
    fd.append("priorite", priorite);
    fd.append("latitude", lat);
    fd.append("longitude", lng);

    for (let i = 0; i < photos.length; i++) {
    fd.append("photos", photos[i]);
}

    setLoading(true);

    try {
    const csrfToken = getCsrfToken();

    const response = await fetch("/api/incidents", {
    method: "POST",
    headers: { 'X-XSRF-TOKEN': csrfToken },
    body: fd,
    credentials: 'same-origin'
});

    if (response.status === 429) {
    let retryAfter = 60;
    const retryAfterHeader = response.headers.get('Retry-After');
    if (retryAfterHeader) retryAfter = parseInt(retryAfterHeader);

    try {
    const data = await response.json();
    if (data.retryAfter) retryAfter = data.retryAfter;
} catch (e) {}

    showRateLimit(retryAfter);
    setLoading(false);
    return;
}

    const data = await response.json();

    if (!response.ok) {
    if (data.details && data.details.length > 0) {
    showError(data.message, data.details);
} else {
    showError(data.message || "Une erreur s'est produite");
}
    setLoading(false);
    return;
}

    showSuccess();

    const btn = document.getElementById('submitBtn');
    btn.innerHTML = '<i data-lucide="check-circle"></i> Incident déclaré !';
    btn.style.background = 'linear-gradient(135deg, #10b981, #059669)';
    lucide.createIcons();

    setTimeout(() => {
    window.location.href = '/citoyen/dashboard';
}, 2000);

} catch (error) {
    console.error("Erreur:", error);
    showError("Erreur de connexion. Veuillez vérifier votre connexion internet et réessayer.");
    setLoading(false);
}
}