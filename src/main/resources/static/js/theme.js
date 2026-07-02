(function () {
    "use strict";

    const storageKey = "tsubuyaki.theme";
    const darkTheme = "dark";
    const lightTheme = "light";

    function selectedTheme() {
        return localStorage.getItem(storageKey) === darkTheme ? darkTheme : lightTheme;
    }

    function applyTheme(theme) {
        const darkMode = theme === darkTheme;
        document.documentElement.classList.toggle("theme-dark", darkMode);
        document.documentElement.classList.toggle("theme-light", !darkMode);
        document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
            button.setAttribute("aria-pressed", String(darkMode));
            button.textContent = darkMode ? "ライトモード" : "ダークモード";
        });
    }

    function toggleTheme() {
        const nextTheme = selectedTheme() === darkTheme ? lightTheme : darkTheme;
        localStorage.setItem(storageKey, nextTheme);
        applyTheme(nextTheme);
    }

    document.addEventListener("DOMContentLoaded", () => {
        applyTheme(selectedTheme());
        document.querySelectorAll("[data-theme-toggle]").forEach((button) => {
            button.addEventListener("click", toggleTheme);
        });
    });
}());
