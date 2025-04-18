document.addEventListener('DOMContentLoaded', function() {
    // Cache DOM elements
    const sidebar = document.getElementById('sidebar');
    const appHeader = document.getElementById('appHeader');
    const mainContent = document.getElementById('mainContent');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mobileToggle = document.getElementById('mobileToggle');
    const sidebarOverlay = document.getElementById('sidebarOverlay');

    // Toggle sidebar on desktop
    const toggleSidebar = () => {
        sidebar.classList.toggle('collapsed');
        appHeader.classList.toggle('expanded');
        mainContent.classList.toggle('expanded');
    };

    // Toggle sidebar on mobile
    const toggleMobileSidebar = () => {
        sidebar.classList.toggle('mobile-visible');
        sidebarOverlay.classList.toggle('visible');
    };

    // Event listeners
    sidebarToggle?.addEventListener('click', toggleSidebar);
    mobileToggle?.addEventListener('click', toggleMobileSidebar);
    sidebarOverlay?.addEventListener('click', toggleMobileSidebar);

    // Handle panel visibility
    const updateActivePanel = () => {
        const path = window.location.pathname;
        const panel = path.split('/').pop() || 'dashboard';
        
        document.querySelectorAll('.content-panel').forEach(p => {
            p.style.display = 'none';
        });

        const activePanel = document.querySelector(`[th-fragment="${panel}"]`);
        if (activePanel) {
            activePanel.style.display = 'block';
        }
    };

    // Initialize
    updateActivePanel();
    
    // Handle navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            const panel = e.currentTarget.getAttribute('href').split('/').pop();
            history.pushState({}, '', `/candidats/${panel}`);
            updateActivePanel();
        });
    });
});
