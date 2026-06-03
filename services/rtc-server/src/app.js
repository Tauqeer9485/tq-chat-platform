const express = require('express');
const cors = require('cors');
const WebSocket = require('ws');
const http = require('http');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3002;
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Middleware
app.use(cors());
app.use(express.json());

// Store active connections
const connections = new Map();

// WebSocket connection handler
wss.on('connection', (ws) => {
  console.log('New WebSocket connection');

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      console.log('Message received:', data.type);

      switch (data.type) {
        case 'call-initiate':
          // Route to recipient
          if (connections.has(data.recipientId)) {
            connections.get(data.recipientId).send(JSON.stringify(data));
          }
          break;
        case 'answer':
          // Route back to caller
          if (connections.has(data.callerId)) {
            connections.get(data.callerId).send(JSON.stringify(data));
          }
          break;
        case 'ice-candidate':
          // Route ICE candidate
          const targetId = data.recipientId || data.callerId;
          if (connections.has(targetId)) {
            connections.get(targetId).send(JSON.stringify(data));
          }
          break;
        default:
          console.log('Unknown message type:', data.type);
      }
    } catch (error) {
      console.error('Error processing message:', error);
    }
  });

  ws.on('close', () => {
    console.log('WebSocket connection closed');
    connections.delete(ws);
  });
});

// REST endpoints
app.post('/api/v1/rtc/initiate-call', (req, res) => {
  // TODO: Implement call initiation logic
  res.json({ success: true });
});

app.post('/api/v1/rtc/end-call', (req, res) => {
  // TODO: Implement call termination logic
  res.json({ success: true });
});

server.listen(PORT, () => {
  console.log(`RTC Server running on port ${PORT}`);
});
