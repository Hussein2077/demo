# Spring Boot Chat Backend — Phase 1

A clean, production-ready mock backend for a chat application built with Spring Boot, JPA/Hibernate, PostgreSQL, and WebSocket/STOMP.

---

## 1. Requirements

- **Java**: 21 or higher
- **Build Tool**: Maven (wrapper included: `mvnw.cmd` / `./mvnw`)

---

## 2. No Database Required (In-Memory Demo)

For this phase, the application runs entirely in-memory.
**No PostgreSQL or database installation is required.**
Data is stored using thread-safe collections and is re-initialized with static seed data every time the application starts.

---

## 3. How to Run

### **Windows**
```cmd
mvnw.cmd spring-boot:run
```

### **Linux / macOS**
```bash
chmod +x ./mvnw
./mvnw spring-boot:run
```

The application will start on **`http://localhost:8080`**.
On startup, the `DatabaseSeeder` will automatically and idempotently populate:
- 5 static users (`ahmed`, `mohamed`, `ali`, `sara`, `omar`)
- 1 static group (`System Chat`) with 100+ messages
- 3 private conversations (Ahmed ↔ Mohamed, Ahmed ↔ Ali, Ahmed ↔ Sara) with sample messages

---

## 4. Authentication Mechanism

This Phase 1 mock uses the **`X-User-Id`** HTTP and STOMP header to identify the current user:
- User 1: `ahmed`
- User 2: `mohamed`
- User 3: `ali`
- User 4: `sara`
- User 5: `omar`

Pass `X-User-Id: 1` in all requests requiring user context. Authorization checks ensure users can only access conversations they participate in; unauthorized requests receive `403 Forbidden`.

---

## 5. REST API Documentation & Examples

Swagger UI is available at: **`http://localhost:8080/swagger-ui.html`**

### **A. Search Users**
Find other users excluding the current user.
```http
GET /api/users/search?q=mohamed
X-User-Id: 1
```
**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "username": "mohamed",
      "displayName": "Mohamed Ali",
      "avatarUrl": null
    }
  ],
  "message": null
}
```

---

### **B. List All Chats for Current User**
Lists all conversations (groups and private chats) with the latest message and unread count, sorted by most recent activity.
```http
GET /api/chats
X-User-Id: 1
```
**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "GROUP",
      "name": "System Chat",
      "avatarUrl": null,
      "lastMessage": {
        "id": 110,
        "conversationId": 1,
        "sender": {
          "id": 5,
          "username": "omar",
          "displayName": "Omar Yasser"
        },
        "type": "TEXT",
        "content": "System Chat message #110",
        "attachments": [],
        "createdAt": "2026-09-23T19:00:00"
      },
      "unreadCount": 105,
      "updatedAt": "2026-09-23T19:00:00"
    }
  ],
  "message": null
}
```

---

### **C. Get or Create Private Chat**
Fetches existing private conversation between current user and target user, or creates one if it doesn't exist.
```http
POST /api/chats/private
Content-Type: application/json
X-User-Id: 1

{
  "userId": 2
}
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": 2,
    "type": "PRIVATE",
    "name": null,
    "avatarUrl": null,
    "createdAt": "2026-09-23T18:00:00",
    "updatedAt": "2026-09-23T18:00:00"
  },
  "message": null
}
```

---

### **D. Message History (Cursor-based Pagination)**
Retrieves message history sorted chronologically. Does not use offset pagination.
```http
GET /api/chats/1/messages?limit=50
X-User-Id: 1
```
Or with cursor for older messages:
```http
GET /api/chats/1/messages?limit=50&before=951
X-User-Id: 1
```
**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 901,
      "conversationId": 1,
      "sender": {
        "id": 2,
        "username": "mohamed",
        "displayName": "Mohamed Ali"
      },
      "type": "TEXT",
      "content": "Hello",
      "attachments": [],
      "createdAt": "2026-09-23T17:00:00"
    }
  ],
  "pagination": {
    "hasMore": true,
    "nextCursor": 901
  },
  "message": null
}
```

---

### **E. Mark Conversation as Read**
Updates `lastReadMessageId` for the current user in this chat.
```http
POST /api/chats/1/read
Content-Type: application/json
X-User-Id: 1

{
  "messageId": 950
}
```
**Response**:
```json
{
  "success": true,
  "data": null,
  "message": null
}
```

---

### **F. File Upload**
Uploads an attachment and creates a message of type `FILE`. Also broadcasts the message to the chat's WebSocket topic.
```http
POST /api/chats/1/messages/files
Content-Type: multipart/form-data
X-User-Id: 1

file: (binary file data)
content: "Check this document"
```
**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1050,
    "conversationId": 1,
    "sender": {
      "id": 1,
      "username": "ahmed",
      "displayName": "Ahmed Mohamed"
    },
    "type": "FILE",
    "content": "Check this document",
    "attachments": [
      {
        "id": 1,
        "fileName": "sample.pdf",
        "contentType": "application/pdf",
        "fileSize": 104857,
        "createdAt": "2026-09-23T20:00:00"
      }
    ],
    "createdAt": "2026-09-23T20:00:00"
  },
  "message": null
}
```

---

### **G. File Download**
Downloads an attachment. Verifies that the requesting user is a participant of the conversation.
```http
GET /api/files/1
X-User-Id: 1
```

---

## 6. WebSocket / STOMP Realtime Messaging

- **STOMP Endpoint**: `/ws`
- **Application Prefix**: `/app`
- **Broker Prefix**: `/topic`

### **Global Chat List Realtime Updates (IMPORTANT)**
Flutter should subscribe to this user-level destination once after login to update the Chat List in realtime.
```text
SUBSCRIBE
destination:/topic/users/{userId}/chats
```
Event Payload:
```json
{
  "eventType": "NEW_MESSAGE",
  "chatId": 2,
  "message": { ... },
  "unreadCount": 1,
  "chatUpdatedAt": "2026-09-24T10:00:00"
}
```

### **Chat-Level Subscription**
Subscribe to a specific chat room for new messages:
```text
SUBSCRIBE
destination:/topic/chats/{chatId}
```

### **Sending Messages**
Send a text message:
```text
SEND
destination:/app/chats/{chatId}/messages
X-User-Id:1
content-type:application/json

{
  "type": "TEXT",
  "content": "Hello team!"
}
```

### **Broadcast Payload**
When a message (TEXT or FILE) is sent, the full DTO is broadcast to `/topic/chats/{chatId}`:
```json
{
  "id": 1051,
  "conversationId": 1,
  "sender": {
    "id": 1,
    "username": "ahmed",
    "displayName": "Ahmed Mohamed",
    "avatarUrl": null
  },
  "type": "TEXT",
  "content": "Hello team!",
  "attachments": [],
  "createdAt": "2026-09-23T20:01:00",
  "updatedAt": "2026-09-23T20:01:00"
}
```

---

## 7. Storage Configuration

Files are stored locally in the directory configured in `application.yml`:
```yaml
chat:
  files:
    max-size: 10485760 # 10 MB limit
    storage-path: ./storage/chat
```
Physical files use UUIDs (`storageKey`) so original file names are never directly exposed to disk paths.
