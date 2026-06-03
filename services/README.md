# Services

Backend microservices for tq-chat-platform.

## messaging-server

**Java Spring Boot application** - Core chat and messaging platform.

- User authentication (JWT)
- Message management
- Conversation/group management
- User sessions
- WebSocket for real-time updates

**Stack**: Java, Spring Boot, Netty, PostgreSQL, Redis

**Ports**: 
- `8081` (Spring HTTP REST API)
- `8080` (Netty WebSocket Server)

**Run**: 
```bash
cd services/messaging-server
./gradlew bootRun
```

---

## media-server

**Node.js Express application** - File upload and download service.

- File uploads (images, videos, documents)
- File download/streaming
- File deletion
- Storage management

**Stack**: Node.js, Express, Multer

**Port**: 3001

**Run**:
```bash
cd services/media-server
npm install && npm run dev
```

---

## rtc-server

**Node.js Express + WebSocket** - Real-time communication (voice/video).

- WebRTC signaling
- Call initiation/termination
- ICE candidate exchange
- Call state management

**Stack**: Node.js, Express, ws (WebSocket)

**Port**: 3002

**Run**:
```bash
cd services/rtc-server
npm install && npm run dev
```

---

## Inter-Service Communication

All services communicate at runtime:

```
messaging-server <→> media-server (REST calls)
messaging-server <→> rtc-server (REST calls)
```

Service URLs:
- Messaging API: `http://messaging-server:8081` (Docker) or `http://localhost:8081` (local)
- Messaging WebSocket: `ws://messaging-server:8080/ws` (Docker) or `ws://localhost:8080/ws` (local)
- Media: `http://media-server:3001` (Docker) or `http://localhost:3001` (local)
- RTC: `http://rtc-server:3002` (Docker) or `http://localhost:3002` (local)

See [API_CONTRACTS.md](../docs/API_CONTRACTS.md) for detailed endpoints.