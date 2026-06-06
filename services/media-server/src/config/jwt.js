/**
 * JWT configuration for the media server, including JWKS URI, validation options, and caching settings.
 * This configuration is used by the JWT middleware to validate incoming tokens against the messaging server's JWKS endpoint.
 */
export const jwtConfig = {
    jwksUri: process.env.JWKS_URI || 'http://messaging-server:8081/api/auth/.well-known/jwks.json',
    
    options: {
        algorithms: ['RS256'],
        audience: process.env.JWT_AUDIENCE || 'media-service',
        issuer: process.env.JWT_ISSUER || 'tq-chat-platform'
    },

    cache: {
        cache: true,
        rateLimit: true, 
        jwksRequestsPerMinute: 10,
        cacheMaxEntries: 5,
        cacheMaxAge: 600000 // 10 minutes
    }
};