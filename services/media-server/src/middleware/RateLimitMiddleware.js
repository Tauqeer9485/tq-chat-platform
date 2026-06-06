import rateLimit from 'express-rate-limit';

/**
 * Rate limiting middleware for the media server. 
 * Limits the number of requests from a single client IP to prevent abuse and ensure fair usage of resources.
 */
export const apiRateLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 100,
    standardHeaders: true,
    legacyHeaders: false, 
    message: {
        error: 'Too many requests from this client IP, please try again later.'
    }
});