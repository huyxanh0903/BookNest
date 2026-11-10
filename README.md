# 📚 BookNest — Personal Book Catalog & AI Assistant

**BookNest** là một ứng dụng web quản lý danh mục sách cá nhân được xây dựng trên nền tảng **Spring Boot 4 / Java 17**, **Thymeleaf**, **Spring Security** và **Microsoft SQL Server**.

Dự án được thiết kế theo phong cách giao diện *Academic Catalog* (gọn gàng, thanh lịch, tập trung vào nội dung sách) và tích hợp các công nghệ hiện đại: **Đăng nhập Google OAuth2**, **Tìm kiếm sách trực tuyến (Google Books / Open Library)** và **Trợ lý AI (Groq / OpenAI)** với cơ chế **Bật/Tắt RAG** (Retrieval-Augmented Generation).

---

## 🌟 Các tính năng nổi bật (Features)

1. **Quản lý danh mục sách cá nhân (Personal Catalog)**:
   - Thêm, sửa, xóa, xem chi tiết sách trong bộ sưu tập.
   - Bảo mật dữ liệu nhiều người dùng: Mỗi người dùng chỉ xem và quản lý sách của chính mình (chống lỗ hổng IDOR).
2. **Phân quyền người dùng (Role-Based Access Control - RBAC)**:
   - **`ADMIN`**: Quản lý toàn bộ danh mục thể loại (Categories), xem và quản lý tất cả sách của toàn bộ người dùng trong hệ thống với cột hiển thị người sở hữu (Owner).
   - **`USER`**: Quản lý sách cá nhân, thiết lập hồ sơ đọc sách, trò chuyện cùng AI.
   - Hiển thị tên tài khoản và huy hiệu `ADMIN` trên thanh Navbar kế bên nút Sign out.
3. **Đa dạng hình thức Đăng nhập (Authentication)**:
   - Đăng nhập bằng tài khoản / mật khẩu truyền thống (Mã hóa BCrypt).
   - Đăng nhập một chạm bằng **Google OAuth2 / OpenID Connect**.
4. **Tìm kiếm sách trực tuyến tự động (External Book Search)**:
   - Tìm sách nhanh qua **Google Books API**, tự động dự phòng (**fallback sang Open Library API**) nếu Google gặp sự cố.
   - Tự động điền thông tin (tiêu đề, tác giả, ISBN, năm xuất bản, ảnh bìa) vào form thêm sách.
5. **Trợ lý AI thông minh 2 chế độ (AI Assistant with RAG Toggle)**:
   - **📚 Catalog Mode (RAG BẬT)**: AI chỉ tìm kiếm và trả lời câu hỏi dựa trên các cuốn sách đang có trong tủ sách cá nhân của bạn.
   - **🌐 General Knowledge (RAG TẮT)**: AI trò chuyện mở rộng ra bên ngoài về văn học thế giới, tóm tắt tác phẩm, tác giả nổi tiếng.
   - Hỗ trợ cả trang chat chuyên biệt (`/ai/chat`) lẫn Widget AI nổi ở góc phải màn hình (`ai-widget`).
6. **Hồ sơ đọc sách & Gợi ý (Reading Profile & AI Recommendations)**:
   - Thiết lập sở thích thể loại, chủ đề, ngôn ngữ và mục tiêu đọc sách.
   - AI kết hợp hồ sơ của bạn với kết quả tìm kiếm sách để đề xuất cuốn sách phù hợp tiếp theo kèm lý do chi tiết.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

- **Ngôn ngữ**: Java 17+ (tương thích cả JDK 21).
- **Backend**: Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA / Hibernate.
- **Bảo mật**: Spring Security 6 (Form Login, BCrypt, OAuth2 Client / OIDC).
- **Cơ sở dữ liệu**: Microsoft SQL Server (kết nối qua `mssql-jdbc` và HikariCP).
- **Frontend**: Thymeleaf, Bootstrap 5.3.3, Typography Literata & Source Sans 3, CSS tùy chỉnh.
- **Tích hợp bên thứ ba**:
  - Google Identity Services (OAuth2).
  - Google Books API & Open Library API.
  - Groq Cloud / OpenAI API (Chat Completions).

---

## 🏗️ Kiến trúc thư mục (Application Architecture)

Dự án được phân tầng nghiêm ngặt theo chuẩn Layered Architecture:

```text
com.huypham.huymelon.spring3/
├── config/                  # Cấu hình hệ thống (SecurityFilterChain, PasswordEncoder, DataInitializer)
├── security/                # Dịch vụ xác thực tài khoản (CustomUserDetailsService, OAuth2, OIDC)
├── controller/              # Điều hướng request và trả về giao diện Thymeleaf
├── service/                 # Logic nghiệp vụ, RAG context retrieval, RestClient gọi API bên ngoài
├── repository/              # Tương tác với CSDL qua Spring Data JPA
└── entity/                  # Các thực thể CSDL (Book, Category, User)
```

---

## 🚀 Hướng dẫn Cài đặt & Chạy ứng dụng trên Localhost (Step-by-Step)

Dành cho bất kỳ thành viên nào clone dự án về máy cá nhân:

### 1. Yêu cầu môi trường (Prerequisites)
- Đã cài đặt **JDK 17** trở lên (khuyên dùng JDK 17 hoặc JDK 21).
- Đã cài đặt **Microsoft SQL Server** (bản Developer hoặc Express).
- IDE khuyên dùng: **IntelliJ IDEA** (khuyên dùng), VS Code hoặc Eclipse.

---

### 2. Thiết lập Cơ sở dữ liệu (Database Setup)
1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Chạy câu lệnh tạo database:
   ```sql
   CREATE DATABASE LibraryDB;
   ```
3. Mở file [`src/main/resources/application.properties`](src/main/resources/application.properties) và điều chỉnh thông tin đăng nhập SQL Server cho khớp với máy của bạn (nếu mật khẩu của bạn khác `12345`):
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=LibraryDB;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=mật_khẩu_sql_server_của_bạn
   ```
   *(Hibernate sẽ tự động tạo bảng khi ứng dụng khởi chạy lần đầu nhờ `ddl-auto=update`)*.

---

### 3. Cấu hình biến môi trường (`.env`)
Tạo một file mang tên `.env` tại thư mục gốc của dự án (ngang hàng với `pom.xml`), sao chép nội dung từ file `.env.example` vào:

```properties
# 1. Cấu hình AI Chatbot (Lấy API Key miễn phí tại https://console.groq.com)
BOOKNEST_AI_API_KEY=gsk_your_groq_api_key_here
BOOKNEST_AI_ENDPOINT=https://api.groq.com/openai/v1/chat/completions
BOOKNEST_AI_MODEL=openai/gpt-oss-20b

# 2. Cấu hình Google OAuth2 Login (Tùy chọn: chỉ cần nếu muốn dùng nút đăng nhập Google)
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here
```

> **Mẹo:**
> - Nếu chưa có Key AI: Ứng dụng vẫn chạy và quản lý sách bình thường (chỉ tính năng chat AI sẽ báo cần key). Bạn có thể tạo Key Groq hoàn toàn miễn phí chỉ mất 30 giây tại [console.groq.com](https://console.groq.com).
> - Nếu chưa cấu hình Google: Bạn vẫn có thể đăng ký tài khoản thường hoặc dùng tài khoản Admin mặc định bên dưới.

---

### 4. Khởi chạy ứng dụng (Run Application)

#### Cách 1: Chạy bằng IntelliJ IDEA (Đơn giản nhất)
1. Mở thư mục dự án trong IntelliJ IDEA.
2. Để IntelliJ đồng bộ Maven: Chuột phải vào file `pom.xml` > chọn **Maven** > **Reload project**.
3. Mở file `src/main/java/com/huypham/huymelon/spring3/Spring3Application.java`.
4. Bấm nút **Run** (biểu tượng tam giác màu xanh ▶️).

#### Cách 2: Chạy bằng dòng lệnh Terminal
- Trên **Windows**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
  *(Hoặc nếu đã cài Maven: `mvn spring-boot:run`)*
- Trên **macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

---

### 5. Truy cập & Trải nghiệm
Sau khi terminal báo `Started Spring3Application in ... seconds`:
1. Mở trình duyệt và truy cập: **[http://localhost:8080](http://localhost:8080)**.
2. **Tài khoản Administrator mặc định** (hệ thống tự động khởi tạo khi chạy lần đầu):
   - **Username**: `admin`
   - **Password**: `admin123`
3. **Tài khoản thông thường**:
   - Bấm **"Create an account"** để đăng ký tài khoản mới.
   - Hoặc bấm **"Continue with Google"** để đăng nhập bằng tài khoản Google.

---

## ❓ Xử lý lỗi thường gặp (Troubleshooting)

- **Lỗi kết nối SQL Server (`The connection to the host localhost, named instance failed`)**:
  - Mở *SQL Server Configuration Manager* > kiểm tra dịch vụ `SQL Server (MSSQLSERVER)` đã ở trạng thái **Running**.
  - Kiểm tra mục *Protocols for MSSQLSERVER* > đảm bảo **TCP/IP** đã được **Enabled** và port là `1433`.
- **Lỗi Google OAuth2 Redirect URI**:
  - Khi tạo Credentials trên Google Cloud Console, mục **Authorized redirect URIs** bắt buộc phải điền chính xác:
    `http://localhost:8080/login/oauth2/code/google`
- **Lỗi IntelliJ báo đỏ package `security.oauth2`**:
  - Vào **Settings** (`Ctrl + Alt + S`) > **Build Tools** > **Maven** > **Runner** > Tích chọn ô **`Delegate IDE build/run actions to Maven`**.

---

## 📄 License & Notes
Dự án được xây dựng phục vụ cho mục đích học tập môn học HSF302 và tìm hiểu kiến trúc Spring Boot hiện đại.
Mọi thắc mắc và đóng góp xin vui lòng liên hệ tác giả qua kho lưu trữ GitHub!
