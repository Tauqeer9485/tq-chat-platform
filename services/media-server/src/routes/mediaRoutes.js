import express from 'express';
import multer from 'multer';
import mediaController from '../controllers/MediaController.js';
import { authenticateToken } from '../middleware/JwtAuthMiddleware.js';
import { requireScope } from '../middleware/ScopeMiddleware.js';
import { validateSignedUrl } from '../services/SignedUrlService.js';

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

/**
 * UPLOAD FILE
 * Requires a valid short-lived token AND the explicit 'media:write' scope.
 */
router.post(
    '/upload', 
    authenticateToken, 
    requireScope('media:write'), 
    upload.single('file'), 
    (req, res) => mediaController.upload(req, res)
);

/**
 * DELETE FILE
 * Requires a valid short-lived token AND the explicit 'media:delete' scope.
 */
router.delete(
    '/:id', 
    authenticateToken, 
    requireScope('media:delete'), 
    (req, res) => mediaController.delete(req, res)
);

/**
 * DOWNLOAD / STREAM FILE (1-Week Expiration)
 * NO Header authentication required! Authenticated entirely by query-string signature.
 */
router.get(
    '/download/:id', 
    validateSignedUrl, 
    (req, res) => mediaController.get(req, res)
);

export default router;