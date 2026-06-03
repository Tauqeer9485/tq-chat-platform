## API Status Convention

Each API is marked with one of:

- `IMPLEMENTED` → fully working in current codebase
- `IN_PROGRESS` → partially implemented / unstable
- `PLANNED` → designed but not implemented yet

# API Contracts

## Messaging Server (Port 8081)

### Authentication (IMPLEMENTED)
```
POST /api/auth/login
{
  "username": "user01",
  "password": "password123"
}
Response: { "token": "jwt-token", "user": { ... } }
```

```
POST /api/auth/signup
{
  "username": "user01",
  "email": "user@example.com",
  "password": "password123",
  "username": "username"
}
Response: { "token": "jwt-token", "user": { ... } }
```

``` 
POST /api/auth/verify
{
  "token": "jwt-token"
}
Response: { "success": true, "message": "Token valid", "token": "jwt-token", "user": { ... } }
```

### Conversation (IMPLEMENTED)
```
POST /api/conversations/direct
Creates or returns a direct conversation between authenticated user and target user.

**Authentication required**

Headers:
Authorization: Bearer <JWT_TOKEN>

Request:
```json
{
  "targetUserId": "e5238285-0bdd-4840-8781-eaa8e94f2696"
}
Response: { "conversationId": "dm_396dc8a3-1c6b-40a4-b076-3ebe095df89f_e5238285-0bdd-4840-8781-eaa8e94f2696" }

```

### WebSocket (IN_PROGRESS)
Connect to: `ws://localhost:8080/ws/`
Messages:
- `CHAT_MESSAGE`: New message received
- `TYPING_INDICATOR`: User typing indicator
- `USER_ONLINE`: User came online

---

## Media Server (Port 3001) 

### Upload (PLANNED)
```
POST /api/media/upload
Content-Type: multipart/form-data
file: <binary>

Response: { "id": "filename", "url": "/api/v1/media/filename", "filename": "original.jpg" }
```

### Download (PLANNED)
```
GET /api/media/{fileId}
Response: <binary file>
```

### Delete (PLANNED)
```
DELETE /api/media/{fileId}
Response: { "success": true }
```

---

## RTC Server (Port 3002)

### WebSocket (Port 3002/ws) (PLANNED)
Connection for WebRTC signaling:

```
{
  "type": "call-initiate",
  "callerId": "user-id",
  "recipientId": "recipient-id",
  "offer": { ... }
}
```

```
{
  "type": "answer",
  "callerId": "user-id",
  "recipientId": "recipient-id",
  "answer": { ... }
}
```

```
{
  "type": "ice-candidate",
  "callerId": "user-id",
  "recipientId": "recipient-id",
  "candidate": { ... }
}
```

### REST Endpoints (PLANNED)
```
POST /api/v1/rtc/initiate-call
{
  "callerId": "user-id",
  "recipientId": "recipient-id"
}
Response: { "callId": "..." }
```

```
POST /api/v1/rtc/end-call
{
  "callId": "..."
}
Response: { "success": true }
```

---

## Inter-Service Communication (PLANNED)

### Messaging Server → Media Server
When uploading files in messages:
```
POST http://media-server:3001/api/v1/media/upload
```

### Messaging Server → RTC Server
When initiating calls:
```
POST http://rtc-server:3002/api/v1/rtc/initiate-call
```
