import fs from 'fs/promises';
import path from 'path';
import { v4 as uuidv4 } from 'uuid';
import StorageProvider from './StorageProvider.js';
import { storageConfig } from '../config/storage.js';

class LocalStorageProvider extends StorageProvider {
    constructor() {
        super();
        this.uploadDir = storageConfig.local.uploadDir;
        this.ensureDirectoryExists();
    }

    async ensureDirectoryExists() {
        try {
            await fs.mkdir(this.uploadDir, { recursive: true });
        } catch (err) {
            console.error("Failed to create local upload directory", err);
        }
    }

    async put(file) {
        const fileExtension = path.extname(file.originalname);
        const storageKey = `${uuidv4()}${fileExtension}`;
        const targetPath = path.join(this.uploadDir, storageKey);

        if (file.buffer) {
            await fs.writeFile(targetPath, file.buffer);
        } else if (file.path) {
            await fs.rename(file.path, targetPath);
        } else {
            throw new Error("Invalid file object format provided to LocalStorageProvider");
        }

        return storageKey;
    }

    async remove(storageKey) {
        const filePath = await this.resolvePath(storageKey);
        try {
            await fs.unlink(filePath);
        } catch (error) {
            if (error.code !== 'ENOENT') throw error;
        }
    }

    async resolvePath(storageKey) {
        return path.join(this.uploadDir, storageKey);
    }
}

export default LocalStorageProvider;