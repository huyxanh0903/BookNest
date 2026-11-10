# BookNest

BookNest is a small personal learning project for exploring **Spring Boot**, **Thymeleaf**, and a layered application structure. It provides a simple web application for managing a personal book collection, with optional AI features connected to an OpenAI-compatible chat API.

> This project is intended for learning and local development. It is not a production-ready library or authentication system.

## What I am learning

- Building server-rendered web pages with Spring MVC and Thymeleaf.
- Organizing application code into controller, service, and repository layers.
- Persisting entities with Spring Data JPA and Microsoft SQL Server.
- Adding login and role-based access with Spring Security.
- Calling external book catalog APIs and an AI chat API from a Spring service.
- Keeping provider settings and API keys in local configuration.

## Features

- Register and sign in to a personal account.
- Add, edit, browse, and organize books by category.
- Search external catalogs using Google Books, with Open Library as a fallback.
- Save a reading profile and view AI-assisted recommendations based on external book metadata.
- Chat with an AI assistant using the signed-in user's book catalog as context.
- Restrict category management to users with the administrator role.

## Technology

- Java 17
- Spring Boot 4.1.2
- Spring MVC and Thymeleaf
- Spring Security
- Spring Data JPA / Hibernate
- Microsoft SQL Server
- Maven Wrapper
- Groq-compatible Chat Completions API by default (configurable)

## Application structure

The application follows a three-layer flow. A request moves from the web layer through business logic to data access, then the result returns to a Thymeleaf view.

```text
Browser
  -> Controller       Handles routes, form input, and view models
  -> Service          Applies business rules and coordinates operations
  -> Repository       Reads and writes entities using Spring Data JPA
  -> SQL Server

Controller -> Thymeleaf template -> HTML response
Service -> ExternalBookSearchService / AiChatService -> External APIs
```

Main packages:

| Package | Responsibility |
| --- | --- |
| `controller` | Spring MVC routes, form handling, and security configuration |
| `service` | Book, category, user, external search, and AI logic |
| `repository` | Spring Data JPA database access |
| `entity` | JPA models for books, categories, and users |
| `config` | Application startup data initialization |
| `resources/templates` | Thymeleaf HTML pages |
| `resources/static` | CSS and other static assets |

## Run locally

### Requirements

- JDK 17 or later
- SQL Server running locally or reachable from your machine
- A SQL Server database named `LibraryDB` (or update the datasource settings)
- An AI provider API key only if you want to use chat and AI recommendations

### Configure the database

The current datasource connection is configured in `src/main/resources/application.properties`. Update the URL, database name, username, and password to match your local SQL Server. Do not commit real passwords or other credentials to GitHub.

The application uses Hibernate schema update (`spring.jpa.hibernate.ddl-auto=update`) for local learning. Review the setting before using a database with important data.

### Configure the optional AI features

The app imports an optional `.env` file from the project root. Create or update `.env` with your provider settings:

```properties
BOOKNEST_AI_API_KEY=your-api-key
BOOKNEST_AI_ENDPOINT=https://api.groq.com/openai/v1/chat/completions
BOOKNEST_AI_MODEL=openai/gpt-oss-20b
```

The endpoint and model shown are defaults for this project and can be changed to match a compatible provider. Keep `.env` private; it is excluded by `.gitignore`. The app can start without an AI key, but AI chat and recommendations will not work until one is configured.

### Start the application

From the project directory, run on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080).

On first startup, the app seeds default book categories and creates a local administrator account if one does not already exist:

```text
Username: admin
Password: admin123
```

This default is for local learning only. Change or remove it before exposing the application to other users.

## Learning notes

- **Controller:** receives a request, validates or binds form data, calls a service, and selects a Thymeleaf template.
- **Service:** contains application behavior so controllers do not need to implement business rules or database queries directly.
- **Repository:** provides persistence operations for entities through Spring Data JPA.
- **Thymeleaf:** renders server-side HTML from templates in `src/main/resources/templates` using data added to the MVC model.
- **AI integration:** `AiChatService` sends chat messages to a configurable OpenAI-compatible endpoint. The key is read from configuration rather than embedded in Java source.

## Project status

This is a small, evolving study project. Some configuration and implementation choices are intentionally simple to make the framework concepts easier to explore. Review authentication defaults, database configuration, and error handling before adapting it for a real deployment.
