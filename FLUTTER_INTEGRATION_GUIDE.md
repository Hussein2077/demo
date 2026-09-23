# Flutter Web / Mobile Integration Guide for Chat Backend

This document details how to integrate your Flutter app (Web, Android, iOS, Desktop) with the Spring Boot Chat Backend, covering REST endpoints, file uploads/downloads, and real-time STOMP WebSockets.

---

## 1. Flutter Dependencies

Add the following packages to your Flutter app's `pubspec.yaml`:

```yaml
dependencies:
  flutter:
    sdk: flutter

  # Networking & HTTP
  dio: ^5.7.0

  # Real-time WebSocket with STOMP
  stomp_dart_client: ^1.0.1

  # File Picker for attachments (supports Web, Android, iOS, Desktop)
  file_picker: ^8.1.4

  # Formatting & Date utilities
  intl: ^0.19.0
```

Run:
```bash
flutter pub get
```

---

## 2. Configuration & Base URLs

Create `lib/core/constants/api_constants.dart`:

```dart
class ApiConstants {
  // Use http://localhost:8080 for Web and Desktop.
  // For Android Emulator, use http://10.0.2.2:8080.
  // For Cloud deployment, use https://your-app.onrender.com.
  static const String baseUrl = 'http://localhost:8080';

  // WebSocket URL
  // Use ws://localhost:8080/ws for local Web/Desktop.
  // Use wss://your-app.onrender.com/ws for cloud.
  static const String wsUrl = 'ws://localhost:8080/ws/websocket';
}
```

---

## 3. Mock Authentication & User State

In this Phase 1 mock, every request uses the `X-User-Id` header.
Create a simple user session manager `lib/core/auth/user_session.dart`:

```dart
class MockUser {
  final int id;
  final String username;
  final String displayName;

  const MockUser(this.id, this.username, this.displayName);
}

class UserSession {
  static const List<MockUser> staticUsers = [
    MockUser(1, 'ahmed', 'Ahmed Mohamed'),
    MockUser(2, 'mohamed', 'Mohamed Ali'),
    MockUser(3, 'ali', 'Ali Hassan'),
    MockUser(4, 'sara', 'Sara Ahmed'),
    MockUser(5, 'omar', 'Omar Yasser'),
  ];

  // Default active user (can be switched in UI via dropdown for testing)
  static MockUser currentUser = staticUsers[0]; // Ahmed (id = 1)

  static void switchUser(MockUser user) {
    currentUser = user;
  }
}
```

---

## 4. Dart Data Models

Create `lib/models/chat_models.dart`:

```dart
class UserModel {
  final int id;
  final String username;
  final String displayName;
  final String? avatarUrl;

  UserModel({
    required this.id,
    required this.username,
    required this.displayName,
    this.avatarUrl,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'],
      username: json['username'],
      displayName: json['displayName'] ?? json['username'],
      avatarUrl: json['avatarUrl'],
    );
  }
}

class AttachmentModel {
  final int id;
  final String fileName;
  final String contentType;
  final int fileSize;
  final DateTime createdAt;

  AttachmentModel({
    required this.id,
    required this.fileName,
    required this.contentType,
    required this.fileSize,
    required this.createdAt,
  });

  factory AttachmentModel.fromJson(Map<String, dynamic> json) {
    return AttachmentModel(
      id: json['id'],
      fileName: json['fileName'],
      contentType: json['contentType'],
      fileSize: json['fileSize'],
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class MessageModel {
  final int id;
  final int conversationId;
  final UserModel sender;
  final String type; // TEXT, FILE, SYSTEM
  final String? content;
  final List<AttachmentModel> attachments;
  final DateTime createdAt;

  MessageModel({
    required this.id,
    required this.conversationId,
    required this.sender,
    required this.type,
    this.content,
    required this.attachments,
    required this.createdAt,
  });

  factory MessageModel.fromJson(Map<String, dynamic> json) {
    return MessageModel(
      id: json['id'],
      conversationId: json['conversationId'],
      sender: UserModel.fromJson(json['sender']),
      type: json['type'],
      content: json['content'],
      attachments: (json['attachments'] as List? ?? [])
          .map((a) => AttachmentModel.fromJson(a))
          .toList(),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}

class ChatSummaryModel {
  final int id;
  final String type; // PRIVATE, GROUP
  final String? name;
  final String? avatarUrl;
  final MessageModel? lastMessage;
  final int unreadCount;
  final DateTime updatedAt;

  ChatSummaryModel({
    required this.id,
    required this.type,
    this.name,
    this.avatarUrl,
    this.lastMessage,
    required this.unreadCount,
    required this.updatedAt,
  });

  factory ChatSummaryModel.fromJson(Map<String, dynamic> json) {
    return ChatSummaryModel(
      id: json['id'],
      type: json['type'],
      name: json['name'],
      avatarUrl: json['avatarUrl'],
      lastMessage: json['lastMessage'] != null
          ? MessageModel.fromJson(json['lastMessage'])
          : null,
      unreadCount: json['unreadCount'] ?? 0,
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }
}
```

---

## 5. REST API Service Implementation

Create `lib/services/chat_api_service.dart`:

```dart
import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import '../core/constants/api_constants.dart';
import '../core/auth/user_session.dart';
import '../models/chat_models.dart';

class ChatApiService {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: ApiConstants.baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
  ));

  ChatApiService() {
    // Interceptor to inject active X-User-Id header dynamically
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) {
        options.headers['X-User-Id'] = UserSession.currentUser.id.toString();
        return handler.next(options);
      },
    ));
  }

  // 1. Search Users
  Future<List<UserModel>> searchUsers(String query) async {
    final response = await _dio.get('/api/users/search', queryParameters: {'q': query});
    final List list = response.data['data'];
    return list.map((json) => UserModel.fromJson(json)).toList();
  }

  // 2. List All Chats for Current User
  Future<List<ChatSummaryModel>> listChats() async {
    final response = await _dio.get('/api/chats');
    final List list = response.data['data'];
    return list.map((json) => ChatSummaryModel.fromJson(json)).toList();
  }

  // 3. Get or Create Private Chat
  Future<Map<String, dynamic>> getOrCreatePrivateChat(int targetUserId) async {
    final response = await _dio.post('/api/chats/private', data: {
      'userId': targetUserId,
    });
    return response.data['data'];
  }

  // 4. Message History (Cursor Pagination)
  Future<Map<String, dynamic>> getMessages(int chatId, {int limit = 50, int? before}) async {
    final Map<String, dynamic> params = {'limit': limit};
    if (before != null) params['before'] = before;

    final response = await _dio.get(
      '/api/chats/$chatId/messages',
      queryParameters: params,
    );

    final List list = response.data['data'];
    final messages = list.map((json) => MessageModel.fromJson(json)).toList();
    final pagination = response.data['pagination'];

    return {
      'messages': messages,
      'hasMore': pagination['hasMore'] as bool,
      'nextCursor': pagination['nextCursor'] as int?,
    };
  }

  // 5. Mark Conversation as Read
  Future<void> markRead(int chatId, int messageId) async {
    await _dio.post('/api/chats/$chatId/read', data: {
      'messageId': messageId,
    });
  }

  // 6. Upload File Message (Multipart)
  Future<MessageModel> uploadFileMessage({
    required int chatId,
    required PlatformFile file,
    String? content,
  }) async {
    MultipartFile multipartFile;

    if (file.bytes != null) {
      // In Flutter Web, files are available in bytes
      multipartFile = MultipartFile.fromBytes(
        file.bytes!,
        filename: file.name,
      );
    } else if (file.path != null) {
      // Mobile / Desktop native file paths
      multipartFile = await MultipartFile.fromFile(
        file.path!,
        filename: file.name,
      );
    } else {
      throw Exception('Invalid file data');
    }

    final formData = FormData.fromMap({
      'file': multipartFile,
      if (content != null && content.isNotEmpty) 'content': content,
    });

    final response = await _dio.post(
      '/api/chats/$chatId/messages/files',
      data: formData,
    );

    return MessageModel.fromJson(response.data['data']);
  }

  // 7. Download File URL
  String getFileDownloadUrl(int attachmentId) {
    return '${ApiConstants.baseUrl}/api/files/$attachmentId';
  }
}
```

---

## 6. Real-time STOMP WebSocket Service

Create `lib/services/chat_websocket_service.dart`:

```dart
import 'dart:convert';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import '../core/constants/api_constants.dart';
import '../core/auth/user_session.dart';
import '../models/chat_models.dart';

class ChatWebSocketService {
  StompClient? _client;
  final Map<int, StompUnsubscribeFn> _subscriptions = {};

  void connect({required Function() onConnected}) {
    _client = StompClient(
      config: StompConfig(
        url: ApiConstants.wsUrl,
        onConnect: (StompFrame frame) {
          onConnected();
        },
        onWebSocketError: (dynamic error) => print('WebSocket Error: $error'),
        onStompError: (StompFrame frame) => print('STOMP Error: ${frame.body}'),
        stompConnectHeaders: {
          'X-User-Id': UserSession.currentUser.id.toString(),
        },
      ),
    );

    _client?.activate();
  }

  // Subscribe to a specific chat room: /topic/chats/{chatId}
  void subscribeToChat(int chatId, Function(MessageModel message) onMessageReceived) {
    if (_client == null || !_client!.connected) return;

    final unsubscribeFn = _client!.subscribe(
      destination: '/topic/chats/$chatId',
      callback: (StompFrame frame) {
        if (frame.body != null) {
          final json = jsonDecode(frame.body!);
          final message = MessageModel.fromJson(json);
          onMessageReceived(message);
        }
      },
    );

    _subscriptions[chatId] = unsubscribeFn;
  }

  // Unsubscribe when leaving the chat screen
  void unsubscribeFromChat(int chatId) {
    _subscriptions[chatId]?.call();
    _subscriptions.remove(chatId);
  }

  // Send message to: /app/chats/{chatId}/messages
  void sendMessage(int chatId, String text) {
    if (_client == null || !_client!.connected) return;

    _client!.send(
      destination: '/app/chats/$chatId/messages',
      headers: {
        'X-User-Id': UserSession.currentUser.id.toString(),
        'content-type': 'application/json',
      },
      body: jsonEncode({
        'type': 'TEXT',
        'content': text,
      }),
    );
  }

  void disconnect() {
    _client?.deactivate();
  }
}
```

---

## 7. How to Run the Flutter Demo Locally

1. Create a new Flutter project (if not already created):
   ```bash
   flutter create chat_flutter_app
   ```
2. Place the above models, services, and UI screens into `lib/`.
3. Ensure your Spring Boot backend is running on `http://localhost:8080`.
4. Run Flutter Web on fixed port `3000`:
   ```bash
   flutter run -d chrome --web-port=3000
   ```

---

## 8. Live Demonstration Procedure (2 Users in Real Time)

To demo real-time bidirectional chatting between two users:

1. **Window A (Normal Chrome)**:
   - Go to `http://localhost:3000`.
   - Set active user to **Ahmed (id = 1)**.
   - Click on the private chat with **Mohamed** or the **System Chat**.

2. **Window B (Incognito Chrome or Edge)**:
   - Go to `http://localhost:3000`.
   - Set active user to **Mohamed (id = 2)**.
   - Open the same conversation.

3. **Verify Features**:
   - Type a message in Window A and hit Send $\rightarrow$ Window B instantly displays the message via WebSocket broadcast.
   - Click the attachment icon in Window A $\rightarrow$ select any file (image/PDF) $\rightarrow$ Window B receives the file message card in real time.
   - Click the file card in Window B $\rightarrow$ downloads directly from `GET /api/files/{attachmentId}`.
   - Observe the unread badges count incrementing in Window B when messages are sent while looking at the chat list, and resetting to 0 when opening the chat.
