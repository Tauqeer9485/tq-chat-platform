# System Architecture

## Overview

tq-chat-platform is a microservice-based real-time communication system designed for messaging, media sharing, and WebRTC-based calls.
The system is built around a central messaging engine with supporting services for media and RTC signaling.

---


## High-Level Design

Clients (Web / Android)
|
v
Messaging Server (Spring Boot + WebSocket)
|
|--> PostgreSQL (persistent data)
|--> Redis (cache / session / future pub-sub)
|
|--> Media Server (file handling)
|
|--> RTC Server (WebRTC signaling)


---


## Core Design Principles

- Stateless services where possible
- WebSocket-based real-time communication
- Custom packet protocol for extensibility
- JWT-based authentication
- Service separation by responsibility

---


## Messaging Server (Core System)

### Responsibilities

- User authentication (JWT)
- Session management
- WebSocket connection handling
- Packet routing and dispatching
- Conversation and message processing

### Internal Architecture

- `PacketDispatcher`
  - Routes incoming packets to correct handlers

- `PacketHandler`
  - Modular handlers for different packet types:
    - LoginPacketHandler
    - MessagePacketHandler
    - PingPacketHandler

- `SessionManager`
  - Tracks active WebSocket sessions per user

- `JwtAuthenticationFilter`
  - Secures HTTP + WebSocket handshake

---


## Communication Protocol

All real-time communication happens via **custom packets**:

- BasePacket
- PacketType enum defines message categories
- Serialized using PacketSerializer

Example flow:
1. Client connects via WebSocket
2. Sends LoginPacket with JWT
3. Server validates and creates session
4. MessagePacket is routed via dispatcher

---


## Media Server (PLANNED)

- Node.js service
- Handles file uploads/downloads
- Stores files in persistent Docker volume
- Stateless design (no session memory)

---


## RTC Server (Media Engine - mediasoup) (PLANNED)

The RTC server is planned to be built using mediasoup and will act as a Selective Forwarding Unit (SFU) for real-time audio and video communication.

---

### Responsibilities (Planned)

- Media pipeline management (audio/video streams)
- WebRTC transport creation using mediasoup
- RTP forwarding via mediasoup routers
- Handling multiple participants in a room
- Managing producers and consumers

---

### NOT handled here

- Signaling (handled by Messaging Server)
- Authentication (handled by Messaging Server)
- User/session management (handled by Messaging Server)

---

### Core Concepts (Planned Design)

- **Worker** → OS-level mediasoup process that handles media processing and enables scaling across CPU cores
- **Router** → Created inside a worker; defines codec capabilities and manages media flow for a room
- **Transport** → WebRTC connection channel responsible for ICE, DTLS, and RTP/RTCP communication
- **Producer** → Represents an outgoing media stream from a client (audio/video tracks sent to SFU)
- **Consumer** → Represents an incoming media stream delivered to a participant from the SFU

---

### Media Flow (Planned Architecture)

1. Client requests call via Messaging Server
2. Messaging Server handles signaling exchange (SDP/ICE)
3. Messaging Server instructs RTC Server to create/join a room
4. RTC Server establishes mediasoup router + transports
5. Clients connect directly to RTC Server for media streaming
6. Audio/video flows through SFU (not peer-to-peer mesh)

---

### Why mediasoup (Design Choice)

- Scales better than mesh P2P calls
- Supports group calls efficiently
- Reduces client CPU usage
- Centralized media routing via SFU model

---

### Future Improvements

- Recording support (via RTP dump or external worker)
- Load balancing across multiple RTC nodes
- Room-based scaling strategy
- Adaptive bitrate handling (ABR)


## Data Layer 

### PostgreSQL (IN_PROGRESS)
- Users
- Conversations
- Messages

### Redis (PLANNED)
- Session caching (planned/partial)
- Rate limiting (future)
- Pub/Sub (future scaling improvement)

---

## Deployment (IMPLEMENTED)

All services are containerized using Docker Compose:

- Internal network: `chat-network`
- Each service communicates via service name (DNS resolution inside Docker)

---

## Scaling Strategy (Future)

- Add Kafka / Redis PubSub for message fanout
- Separate read/write DB scaling
- Introduce API Gateway
- Horizontal scaling of messaging-server