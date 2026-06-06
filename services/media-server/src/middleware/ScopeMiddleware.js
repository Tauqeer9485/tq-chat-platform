/**
 * Scope-based authorization middleware for the media server. 
 * Ensures that the authenticated user has the required permission scope before allowing access to a route.
 */
export const requireScope = (requiredScope) => {
    return (req, res, next) => {
        if (!req.user || !req.user.scopes || !req.user.scopes.includes(requiredScope)) {
            return res.status(403).json({ 
                error: `Forbidden: Missing required permission scope [${requiredScope}]` 
            });
        }
        next();
    };
};