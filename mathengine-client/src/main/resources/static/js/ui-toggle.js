document.addEventListener('DOMContentLoaded', () => {
    const editBtn = document.getElementById('editFunctionsBtn');
    const editPanel = document.querySelector('.edit-functions');
    const mainTextarea = document.querySelector('.bot-section > textarea');
    const closeEditBtn = document.getElementById('closeEditBtn');

    const overlay = document.querySelector('.blur-overlay');
    const signInCard = document.getElementById('sign-in');
    const signUpCard = document.getElementById('sign-up');
    const helpCard = document.getElementById('help');
    const helpBtn = document.getElementById('helpBtn');
    const toggleThemeBtn = document.getElementById('toggle-theme-btn')

    const calcForm = document.querySelector('form[action="/calculate"]');
    const spinner = document.querySelector(".loading-spinner");

    const show = el => el && (el.style.display = '');
    const hide = el => el && (el.style.display = 'none');

    // Edit functions panel toggle
    editBtn?.addEventListener('click', () => {
        show(editPanel);
        hide(mainTextarea);
    });

    closeEditBtn?.addEventListener('click', () => {
        hide(editPanel);
        show(mainTextarea);
    });

    // Help button
    helpBtn?.addEventListener('click', () => {
        show(overlay);
        show(helpCard);
        hide(signInCard);
        hide(signUpCard);
    });

    if (calcForm) {
        calcForm.addEventListener("submit", () => {
            spinner.style.display = "flex";
        });
    }

    // Show login/register popup on page load based on URL
    if (window.location.pathname === '/login') {
        show(overlay);
        show(signInCard);
        hide(signUpCard);
        hide(helpCard);
    }

    if (window.location.pathname === '/register') {
        show(overlay);
        show(signUpCard);
        hide(signInCard);
        hide(helpCard);
    }

    // Global click handler to hide overlay if clicked outside popups
    document.addEventListener('click', (e) => {
        const isOverlayVisible = getComputedStyle(overlay).display !== 'none';

        const clickedInsidePopup = [signInCard, signUpCard, helpCard].some(card =>
            card && getComputedStyle(card).display !== 'none' && card.contains(e.target)
        );

        const clickedToggleTheme = toggleThemeBtn && toggleThemeBtn.contains(e.target);
        const clickedHelpBtn = helpBtn && helpBtn.contains(e.target);

        if (isOverlayVisible && !clickedInsidePopup && !clickedToggleTheme && !clickedHelpBtn) {
            if (window.location.pathname === '/login') {
                // Redirect to home when outside click happens on /login
                window.location.href = '/';
                return;
            }

            // Preserve existing functionality for other pages
            hide(overlay);
            hide(signInCard);
            hide(signUpCard);
            hide(helpCard);

            // Remove /register from the URL if needed
            const path = window.location.pathname;
            if (path === '/register') {
                history.replaceState(null, '', '/');
            }
        }
    });
});
