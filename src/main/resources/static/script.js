/**
 * Fursati - Script principal
 * Gère les interactions utilisateur et les animations
 */

document.addEventListener('DOMContentLoaded', function() {
    // Gestion des favoris (bookmark)
    initBookmarkSystem();

    // Animation des compteurs statistiques
    initCounterAnimation();

    // Animation des éléments au défilement
    initScrollAnimations();

    // Amélioration de l'accessibilité pour les messages d'erreur des formulaires
    initFormValidation();

    // Initialisation des tooltips Bootstrap (si nécessaire)
    initBootstrapComponents();
});

/**
 * Initialise le système de bookmark des offres d'emploi
 */
function initBookmarkSystem() {
    const bookmarkBtns = document.querySelectorAll('.bookmark');

    bookmarkBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const icon = this.querySelector('i');

            if (icon.classList.contains('far')) {
                icon.classList.remove('far');
                icon.classList.add('fas');
                // Ajouter une classe temporaire pour l'animation
                icon.classList.add('bookmark-animation');

                // Afficher un message de confirmation (optionnel)
                showToast('Offre ajoutée aux favoris');
            } else {
                icon.classList.remove('fas');
                icon.classList.add('far');

                // Afficher un message de confirmation (optionnel)
                showToast('Offre retirée des favoris');
            }

            // Supprimer la classe d'animation après son exécution
            setTimeout(() => {
                icon.classList.remove('bookmark-animation');
            }, 300);
        });
    });
}

/**
 * Affiche un message toast
 * @param {string} message - Le message à afficher
 */
function showToast(message) {
    // Vérifier si l'élément toast existe déjà
    let toastContainer = document.querySelector('.toast-container');

    // Si le conteneur n'existe pas, le créer
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }

    // Créer le toast
    const toastEl = document.createElement('div');
    toastEl.className = 'toast align-items-center bg-primary text-white border-0';
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');

    toastEl.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Fermer"></button>
        </div>
    `;

    toastContainer.appendChild(toastEl);

    // Initialiser et afficher le toast avec Bootstrap
    const toast = new bootstrap.Toast(toastEl, {
        autohide: true,
        delay: 3000
    });

    toast.show();

    // Nettoyer le DOM après la fermeture du toast
    toastEl.addEventListener('hidden.bs.toast', () => {
        toastEl.remove();
    });
}

/**
 * Initialise l'animation des compteurs statistiques
 */
function initCounterAnimation() {
    const stats = document.querySelectorAll('.stat .counter');

    if (stats.length > 0) {
        const animateValue = (element, start, end, duration) => {
            let startTimestamp = null;
            const step = (timestamp) => {
                if (!startTimestamp) startTimestamp = timestamp;
                const progress = Math.min((timestamp - startTimestamp) / duration, 1);
                const value = Math.floor(progress * (end - start) + start);

                // Formater le nombre avec des séparateurs de milliers
                element.textContent = value.toLocaleString() + (element.dataset.suffix || '+');

                if (progress < 1) {
                    window.requestAnimationFrame(step);
                }
            };
            window.requestAnimationFrame(step);
        };

        // Utiliser IntersectionObserver pour détecter quand les éléments sont visibles
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const target = entry.target;
                    const endValue = parseInt(target.getAttribute('data-count') || 0);

                    // Animer de 0 jusqu'à la valeur cible
                    animateValue(target, 0, endValue, 2000);

                    // Ne plus observer cet élément
                    observer.unobserve(target);
                }
            });
        }, { threshold: 0.1 });

        // Observer chaque élément de statistique
        stats.forEach(stat => {
            observer.observe(stat);
        });
    }
}

/**
 * Initialise les animations au défilement de la page
 */
function initScrollAnimations() {
    // Ajouter la classe .animated-element aux éléments à animer
    const elementsToAnimate = [
        '.hero h1', '.hero p', '.hero .search-container',
        '.hero .row .stat', 'section h2', 'section .card',
        '.col-lg-6 h2', '.col-lg-6 .lead', '.col-lg-6 ul',
        '.testimonial-card'
    ];

    // Ajouter la classe aux éléments concernés s'ils ne l'ont pas déjà
    elementsToAnimate.forEach(selector => {
        document.querySelectorAll(selector).forEach(el => {
            if (!el.classList.contains('animated-element')) {
                el.classList.add('animated-element');
            }
        });
    });

    // Observer les éléments avec animation
    const animatedElements = document.querySelectorAll('.animated-element');

    if (animatedElements.length > 0) {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');

                    // Pour les animations qui ne doivent se produire qu'une fois
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        animatedElements.forEach(el => {
            observer.observe(el);
        });
    }
}

/**
 * Initialise la validation des formulaires avec retour utilisateur amélioré
 */
function initFormValidation() {
    const forms = document.querySelectorAll('form');

    forms.forEach(form => {
        // Ajouter un écouteur d'événement pour la soumission du formulaire
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();

                // Identifier les champs invalides et ajouter des messages d'erreur accessibles
                const invalidFields = form.querySelectorAll(':invalid');

                invalidFields.forEach(field => {
                    const feedbackId = `${field.id}-feedback`;

                    // Créer un message d'erreur si nécessaire
                    if (!document.getElementById(feedbackId)) {
                        const feedbackElement = document.createElement('div');
                        feedbackElement.id = feedbackId;
                        feedbackElement.className = 'invalid-feedback';
                        feedbackElement.textContent = field.validationMessage;

                        // Ajouter l'attribut aria pour l'accessibilité
                        field.setAttribute('aria-describedby', feedbackId);

                        // Insérer après le champ
                        field.parentNode.appendChild(feedbackElement);
                    }
                });
            }

            form.classList.add('was-validated');
        }, false);
    });
}

/**
 * Initialise les composants Bootstrap qui nécessitent une initialisation JavaScript
 */
function initBootstrapComponents() {
    // Initialiser tous les tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialiser tous les popovers (si utilisés)
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
}