/**
 * Global error handling middleware for the media server. Catches unhandled errors and returns a standardized JSON response.
 */
export const globalErrorHandler = (err, req, res, next) => {
    console.error('Unhandled System Error:', err.stack);

    const statusCode = err.statusCode || 500;
    const message = statusCode === 500 ? 'Internal Server Error' : err.message;

    return res.status(statusCode).json({
        error: message,
        ...(process.env.NODE_ENV === 'development' && { details: err.details || err.stack })
    });
};