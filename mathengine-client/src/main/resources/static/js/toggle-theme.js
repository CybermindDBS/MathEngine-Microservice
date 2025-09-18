const htmlElement = document.documentElement;

// Apply saved theme on load
const savedTheme = localStorage.getItem("theme");
if (savedTheme) {
    htmlElement.classList.remove("theme-light", "theme-dark");
    htmlElement.classList.add(savedTheme);
}

function toggleTheme() {
    if (htmlElement.classList.contains("theme-light")) {
        htmlElement.classList.remove("theme-light");
        htmlElement.classList.add("theme-dark");
        localStorage.setItem("theme", "theme-dark");
    } else {
        htmlElement.classList.remove("theme-dark");
        htmlElement.classList.add("theme-light");
        localStorage.setItem("theme", "theme-light");
    }
}

document.getElementById("toggle-theme-btn").addEventListener("click", toggleTheme);