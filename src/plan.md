# Spring Boot Chat Backend — Phase 1

This plan details the implementation of a complete Spring Boot backend mock for a chat application, satisfying all requirements including PostgreSQL, WebSockets, file uploads, and pagination.

## User Review Required

> [!IMPORTANT]
> The current workspace (`f:\demo`) contains a Gradle project using an H2 database created in the previous steps. The new requirements specify using **Maven** and **PostgreSQL**.
> 
> **Question:** Should I delete the existing Gradle project in `f:\demo` and initialize a fresh Maven project in the same directory, or would you prefer I create it in a different directory? (I will proceed with clearing `f:\demo` and initializing Maven there if approved).
> 
> **Question:** Do you already have a local PostgreSQL instance running on `localhost:5432` with the credentials `postgres/postgres`, or will you set that up before I run the application?

## Open Questions

None at this moment. The requirements provided are very detailed and clear.

## Proposed Changes

We will implement the system following the feature-based package organization requested. The implementation will proceed in the specified exact order (Phases 1-7).

### Phase 1 — Project Initialization
- [NEW] Clear `f:\demo` and initialize a Spring Boot 3.x project using Java 21, Maven, and dependencies (Web, Data JPA, PostgreSQL, WebSocket, Validation).
- [NEW] `application.yml` with PostgreSQL configuration, file storage paths, and hibernate `ddl-auto: update`.

### Phase 2 — Database Models (Entities & Repositories)
- [NEW] `user/entity/User.java`, `user/repository/UserRepository.java`
- [NEW] `conversation/entity/Conversation.java`, `ConversationParticipant.java`, `conversation/repository/ConversationRepository.java`, `ConversationParticipantRepository.java`
- [NEW] `message/entity/Message.java`, `message/repository/MessageRepository.java`
- [NEW] `attachment/entity/MessageAttachment.java`, `attachment/repository/MessageAttachmentRepository.java`

### Phase 3 — Seed Data
- [NEW] `config/DatabaseSeeder.java` (Implements `CommandLineRunner` or `@EventListener(ApplicationReadyEvent.class)`)
- Will idempotently create the 5 static users, the static "System Chat" group, sample private chats (Ahmed ↔ Mohamed, Ahmed ↔ Ali, Ahmed ↔ Sara), and seed messages to test pagination.

### Phase 4 — REST API & Services
- [NEW] `common/response/ApiResponse.java`, `common/response/PaginatedResponse.java`
- [NEW] `common/exception/GlobalExceptionHandler.java`
- [NEW] `config/CurrentUserProvider.java` (Extracts `X-User-Id` header)
- [NEW] `user/controller/UserController.java`, `user/service/UserService.java` (Search users)
- [NEW] `conversation/controller/ChatController.java`, `conversation/service/ChatService.java` (List chats, Create/Get Private Chat, Mark Read)
- [NEW] `message/controller/MessageController.java`, `message/service/MessageService.java` (Cursor-based message history)

### Phase 5 — File Storage
- [NEW] `attachment/storage/FileStorageService.java` (Interface)
- [NEW] `attachment/storage/LocalFileStorageService.java` (Saves to `./storage/chat/`)
- [NEW] `attachment/controller/FileController.java`, `attachment/service/AttachmentService.java` (Upload/Download REST endpoints with validation)

### Phase 6 — WebSockets
- [NEW] `websocket/config/WebSocketConfig.java` (STOMP broker at `/topic`, app destination `/app`)
- [NEW] `websocket/controller/ChatWebSocketController.java` (Handles `/app/chats/{chatId}/messages`)
- [NEW] Update `MessageService.java` and `AttachmentService.java` to broadcast messages and file events via `SimpMessagingTemplate`.

### Phase 7 — Testing & Documentation
- [NEW] JUnit 5 & `@SpringBootTest` classes covering Users, Conversations, Messages, Unread counts, Files, and WebSockets.
- [NEW] `README.md` containing run instructions, DB setup, API examples, and WebSocket details.

## Verification Plan

### Automated Tests
- Run `mvnw test` to execute all unit and integration tests covering the required test cases (search, create chat, pagination, auth, file validation, WS broadcast).

### Manual Verification
- Verify database tables and seed data using pgAdmin or `psql`.
- Use Postman / `curl` to manually test REST APIs using `X-User-Id: 1`.
- Verify file storage creates files in `./storage/chat/`.
- Compile and run the application to ensure it boots without errors.
