#!/bin/bash

echo "Setting up tq-chat-platform..."

# Create uploads directory
mkdir -p services/media-server/uploads

# Install dependencies for media-server
echo "Installing media-server dependencies..."
cd services/media-server
npm install
cd ../..

# Install dependencies for rtc-server
echo "Installing rtc-server dependencies..."
cd services/rtc-server
npm install
cd ../..

echo "Setup complete!"
echo "Run 'docker-compose up' to start all services"
