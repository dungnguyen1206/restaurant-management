document.addEventListener("DOMContentLoaded", function () {
    const accountMenu = document.getElementById("header-account-menu");
    const profileTrigger = document.getElementById("header-profile-trigger");
    const accountDropdown = document.getElementById("accountDropdown");

    if (!accountMenu || !profileTrigger || !accountDropdown) {
        return;
    }

    function closeDropdown() {
        accountDropdown.classList.remove("show");
        profileTrigger.setAttribute("aria-expanded", "false");
    }

    profileTrigger.addEventListener("click", function (event) {
        event.stopPropagation();
        const isOpen = accountDropdown.classList.toggle("show");
        profileTrigger.setAttribute("aria-expanded", String(isOpen));
    });

    document.addEventListener("click", function (event) {
        if (!accountMenu.contains(event.target)) {
            closeDropdown();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeDropdown();
            profileTrigger.focus();
        }
    });
});
