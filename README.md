RROMS - Restaurant Reservation & Order Management System

Hệ thống quản lý đặt bàn và gọi món cho nhà hàng: đặt bàn online, gọi món tại bàn, hiển thị bếp (KDS) realtime, thanh toán và báo cáo doanh thu.

Công nghệ
Java 21, Spring Boot 3.5.x, Maven
SQL Server, Spring Data JPA
Thymeleaf, HTML/CSS/JS
Spring Security (RBAC: Admin, Manager, Receptionist, Waiter, Chef, Customer)

Google OAuth2 / OpenID Connect

1. Tạo OAuth 2.0 Client ID loại Web application trong Google Cloud Console.
2. Thêm Authorized redirect URI:
   `http://localhost:8085/login/oauth2/code/google`
3. Cấu hình biến môi trường `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` theo `.env.example`.
4. Nếu email Google đã tồn tại, hệ thống giữ nguyên role và yêu cầu tài khoản có trạng thái `ACTIVE`.
5. Nếu email chưa tồn tại, hệ thống tự tạo tài khoản `ACTIVE` với role `CUSTOMER` và lấy họ, tên từ Google.

Google chỉ xác minh danh tính. Role của tài khoản hiện có được tải từ bảng `roles` và được lưu trong Spring Security session. OAuth không thay đổi role hiện có; chỉ tài khoản đăng ký mới được gán `CUSTOMER`.

Tài khoản được tạo bằng Google có `password_hash = NULL`. Đăng ký bằng biểu mẫu thông thường vẫn bắt buộc mật khẩu qua DTO và lưu BCrypt hash.
