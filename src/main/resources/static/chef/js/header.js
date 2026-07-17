document.addEventListener("DOMContentLoaded", function () {
    const profileTrigger = document.getElementById("header-profile-trigger");
    const accountDropdown = document.getElementById("accountDropdown");

    // Click vào Avatar hoặc vùng chứa thông tin -> Bật / Tắt menu
    profileTrigger.addEventListener("click", function (event) {
        // Ngăn sự kiện nổi bọt để tránh kích hoạt sự kiện click out ngay lập tức
        event.stopPropagation();
        accountDropdown.classList.toggle("show");
    });

    // Bấm ra ngoài vùng menu -> Tự động đóng menu lại cho tinh tế
    document.addEventListener("click", function (event) {
        if (!profileTrigger.contains(event.target)) {
            accountDropdown.classList.remove("show");
        }
    });
});