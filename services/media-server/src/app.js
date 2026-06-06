import express from 'express';
import cors from 'cors';
import { apiRateLimiter } from './middleware/RateLimitMiddleware.js';
import { globalErrorHandler } from './middleware/ErrorMiddleware.js';
import 'dotenv/config';

// Import the media routes
import mediaRoutes from './routes/mediaRoutes.js';

const app = express();

// Global Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routed Endpoints
app.use('/api', apiRateLimiter);
app.use('/api/media', mediaRoutes);

// Global Error Handler 
app.use(globalErrorHandler);

export default app;