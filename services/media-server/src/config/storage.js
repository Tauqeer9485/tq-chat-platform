import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * Storage configuration for the media server, supporting multiple drivers (local filesystem, S3, MinIO).
 */
export const storageConfig = {
    driver: process.env.STORAGE_DRIVER || 'local', // 'local', 's3', 'minio'
    
    local: {
        uploadDir: process.env.LOCAL_UPLOAD_DIR || path.join(__dirname, '../../uploads'),
    },

    s3: {
        bucket: process.env.S3_BUCKET,
        endpoint: process.env.S3_ENDPOINT,
        accessKeyId: process.env.S3_ACCESS_KEY,
        secretAccessKey: process.env.S3_SECRET_KEY,
    }
};