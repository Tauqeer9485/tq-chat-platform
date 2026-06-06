import jwt from 'jsonwebtoken';
import jwksClient from 'jwks-rsa';
import { jwtConfig } from '../config/jwt.js';

const client = jwksClient({
    jwksUri: jwtConfig.jwksUri,
    ...jwtConfig.cache
});

function getKey(header, callback) {
    client.getSigningKey(header.kid, (err, key) => {
        if (err) return callback(err, null);
        callback(null, key.getPublicKey());
    });
}

/**
 * JWT authentication middleware for the media server. 
 * Validates incoming JWT tokens against the messaging server's JWKS endpoint and attaches user information to the request object.
 */
export const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ error: 'Access token missing' });
    }

    jwt.verify(token, getKey, jwtConfig.options, (err, decoded) => {
        if (err) {
            return res.status(403).json({ error: 'Invalid or expired token' });
        }

        req.user = {
            id: decoded.sub || decoded.userId,
            scopes: decoded.scopes || []
        };
        next();
    });
};