# tq-chat-platform

Core chat platform with multiple microservices.

## Services

- **messaging-server** - Java Spring Boot backend (messaging, auth, user management)
- **media-server** - Node.js server (file uploads/downloads)
- **rtc-server** - Node.js WebRTC signaling server (voice/video calls)
- **web-client** - Web application
- **android-client** - Android mobile app

## Quick Start

```bash
# Start all services
docker-compose up

# Or build first
docker-compose up --build
```

Services run at:
- Messaging Server: http://localhost:8081 && ws://localhost:8080/ws
- Media Server: http://localhost:3001
- RTC Server: http://localhost:3002
- Database: localhost:5432

## Development

Each service can be run independently:

```bash
# Messaging Server
cd services/messaging-server
./gradlew bootRun

# Media Server
cd services/media-server
npm install && npm run dev

# RTC Server
cd services/rtc-server
npm install && npm run dev
```

## Structure

```
services/
├── messaging-server/  - Java Spring Boot/Netty 
├── media-server/      - Node.js
└── rtc-server/        - Node.js

clients/
├── web-client/
└── android-client/

infrastructure/
└── docker/            - Docker configs

docs/                  - Documentation
```

## Contributing

See [docs/](./docs) for architecture and API details.
