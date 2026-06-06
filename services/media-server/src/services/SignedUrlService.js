import crypto from 'crypto';
import dotenv from 'dotenv';

dotenv.config();

const URL_SIGNING_SECRET = process.env.URL_SIGNING_SECRET || 'super-secure-long-dev-secret-key-change-in-prod';

/**
 * Generate a cryptographically signed URL valid for 7 days
 * 
 * @param {string} mediaId - The ID of the media file to be accessed
 * @returns {string} - A signed URL that can be used to download the file
 */
export const generateSignedUrl = (mediaId) => {
    const baseUrl = process.env.MEDIA_SERVER_URL || 'http://localhost:3001';
    
    const ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000;
    const expires = Date.now() + ONE_WEEK_MS;

    const payload = `/api/media/download/${mediaId}:${expires}`;
    const signature = crypto
        .createHmac('sha256', URL_SIGNING_SECRET)
        .update(payload)
        .digest('hex');

    return `${baseUrl}/api/media/download/${mediaId}?expires=${expires}&signature=${signature}`;
};

/**
 * Express Middleware to validate incoming Signed URLs
 */
export const validateSignedUrl = (req, res, next) => {
    const { id } = req.params;
    const { expires, signature } = req.query;

    if (!expires || !signature) {
        return res.status(403).json({ error: 'Access Denied: Missing signing parameters' });
    }

    if (Date.now() > parseInt(expires, 10)) {
        return res.status(403).json({ error: 'Access Denied: This download link has expired' });
    }

    const payload = `/api/media/download/${id}:${expires}`;
    const expectedSignature = crypto
        .createHmac('sha256', URL_SIGNING_SECRET)
        .update(payload)
        .digest('hex');

    if (signature !== expectedSignature) {
        return res.status(403).json({ error: 'Access Denied: Invalid URL signature' });
    }

    next();
};