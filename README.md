# 📚 BookNest — Personal Book Catalog & AI Assistant

**BookNest** is a web application for managing a personal book catalog, built on **Spring Boot 4 / Java 17**, **Thymeleaf**, **Spring Security**, and **Microsoft SQL Server**.

The project follows an *Academic Catalog* design style (clean, elegant, focused on book content) and integrates modern technologies: **Google OAuth2 login**, **Online Book Search (Google Books / Open Library)**, and an **AI Assistant (Groq / OpenAI)** with a **RAG Toggle** (Retrieval-Augmented Generation).

---

## 🌟 Key Features

1. **Personal Catalog Management**:
   - Add, edit, delete, and view detailed information about the books in your collection.
   - Multi-user data security: each user can only view and manage their own books (prevents IDOR vulnerabilities).
2. **Role-Based Access Control (RBAC)**:
   - **`ADMIN`**: Manages all categories, and views and manages every user's books in the system, with an Owner column.
   - **`USER`**: Manages personal books, sets up a reading profile, and chats with the AI.
   - The account name and an `ADMIN` badge are displayed in the Navbar next to the Sign out button.
3. **Multiple Authentication Methods**:
   - Traditional username/password login (BCrypt encrypted).
   - One-click login via **Google OAuth2 / OpenID Connect**.
4. **Automatic Online Book Search (External Book Search)**:
   - Quickly search for books via the **Google Books API**, with automatic **fallback to the Open Library API** if Google fails.
   - Automatically fills in book information (title, author, ISBN, publication year, cover image) in the Add Book form.
5. **Intelligent AI Assistant with Two Modes (AI Assistant with RAG Toggle)**:
   - **📚 Catalog Mode (RAG ON)**: The AI searches and answers questions based only on the books in your personal catalog.
   - **🌐 General Knowledge (RAG OFF)**: The AI chats more broadly about world literature, summarizes works, and discusses famous authors.
   - Supports both a dedicated chat page (`/ai/chat`) and a floating AI widget (`ai-widget`) in the corner of the screen.
6. **Reading Profile & AI Recommendations**:
   - Set your preferred genres, topics, languages, and reading goals.
   - The AI combines your profile with book search results to suggest the next suitable book, along with detailed reasoning.

---

## 🛠️ Tech Stack

- **Language**: Java 17+ (also compatible with JDK 21).
- **Backend**: Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA / Hibernate.
- **Security**: Spring Security 6 (Form Login, BCrypt, OAuth2 Client / OIDC).
- **Database**: Microsoft SQL Server (connected via `mssql-jdbc` and HikariCP).
- **Frontend**: Thymeleaf, Bootstrap 5.3.3, Literata & Source Sans 3 typography, custom CSS.
- **Third-party integrations**:
  - Google Identity Services (OAuth2).
  - Google Books API & Open Library API.
  - Groq Cloud / OpenAI API (Chat Completions).

---

## 🏗️ Application Architecture

The project follows a strict Layered Architecture:

```text
com.huypham.huymelon.spring3/
├── config/                  # System configuration (SecurityFilterChain, PasswordEncoder, DataInitializer)
├── security/                # Account authentication services (CustomUserDetailsService, OAuth2, OIDC)
├── controller/              # Request routing and Thymeleaf view responses
├── service/                 # Business logic, RAG context retrieval, RestClient for external API calls
├── repository/              # Database access via Spring Data JPA
└── entity/                  # Database entities (Book, Category, User)
```

---

## 🚀 Installation & Running Locally (Step-by-Step)

For any team member cloning the project to their own machine:

### 1. Prerequisites
- **JDK 17** or later installed (JDK 17 or JDK 21 recommended).
- **Microsoft SQL Server** installed (Developer or Express edition).
- Recommended IDE: **IntelliJ IDEA** (preferred), VS Code, or Eclipse.

---

### 2. Database Setup
1. Open **SQL Server Management Studio (SSMS)** or **Azure Data Studio**.
2. Run the following statement to create the database:
   ```sql
   CREATE DATABASE LibraryDB;
   ```
3. Open [`src/main/resources/application.properties`](src/main/resources/application.properties) and adjust the SQL Server credentials to match your machine (if your password is not `12345`):
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=LibraryDB;encrypt=true;trustServerCertificate=true
   spring.datasource.username=sa
   spring.datasource.password=your_sql_server_password
   ```
   *(Hibernate will automatically create the tables on the first launch thanks to `ddl-auto=update`.)*

---

### 3. Environment Variables (`.env`)
Create a file named `.env` in the project root (next to `pom.xml`) and copy the contents of `.env.example` into it:

```properties
# 1. AI Chatbot configuration (get a free API key at https://console.groq.com)
BOOKNEST_AI_API_KEY=gsk_your_groq_api_key_here
BOOKNEST_AI_ENDPOINT=https://api.groq.com/openai/v1/chat/completions
BOOKNEST_AI_MODEL=openai/gpt-oss-20b

# 2. Google OAuth2 Login configuration (optional: only needed if you want the Google login button)
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here
```

> **Tips:**
> - If you don't have an AI key yet: the application still runs and manages books normally (only the AI chat feature will say a key is required). You can create a Groq key completely free in about 30 seconds at [console.groq.com](https://console.groq.com).
> - If you haven't configured Google yet: you can still register a regular account or use the default Admin account below.

---

### 4. Run the Application

#### Option 1: Run with IntelliJ IDEA (simplest)
1. Open the project folder in IntelliJ IDEA.
2. Let IntelliJ sync Maven: right-click `pom.xml` > select **Maven** > **Reload project**.
3. Open `src/main/java/com/huypham/huymelon/spring3/Spring3Application.java`.
4. Click the **Run** button (the green triangle ▶️).

#### Option 2: Run from the terminal
- On **Windows**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
  *(Or, if Maven is installed: `mvn spring-boot:run`)*
- On **macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

---

### 5. Access & Try It Out
Once the terminal prints `Started Spring3Application in ... seconds`:
1. Open your browser and go to: **[http://localhost:8080](http://localhost:8080)**.
2. **Default Administrator account** (automatically created by the system on first run):
   - **Username**: `admin`
   - **Password**: `admin123`
3. **Regular accounts**:
   - Click **"Create an account"** to register a new account.
   - Or click **"Continue with Google"** to sign in with your Google account.

---

## ❓ Troubleshooting

- **SQL Server connection error (`The connection to the host localhost, named instance failed`)**:
  - Open *SQL Server Configuration Manager* and check that the `SQL Server (MSSQLSERVER)` service is **Running**.
  - Under *Protocols for MSSQLSERVER*, make sure **TCP/IP** is **Enabled** and the port is `1433`.
- **Google OAuth2 Redirect URI error**:
  - When creating Credentials in Google Cloud Console, the **Authorized redirect URIs** field must be exactly:
    `http://localhost:8080/login/oauth2/code/google`
- **IntelliJ shows the `security.oauth2` package in red**:
  - Go to **Settings** (`Ctrl + Alt + S`) > **Build Tools** > **Maven** > **Runner** > check **`Delegate IDE build/run actions to Maven`**.

---

## 📄 License & Notes
This project was built for learning purposes in the HSF302 course and to explore modern Spring Boot architecture.
For any questions or contributions, please contact the author via the GitHub repository!
