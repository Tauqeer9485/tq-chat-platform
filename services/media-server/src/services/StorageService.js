import { storageConfig } from '../config/storage.js';
import LocalStorageProvider from '../storage/LocalStorageProvider.js';
// import S3StorageProvider from '../storage/S3StorageProvider.js'; // Future import

class StorageService {
    constructor() {
        this.provider = this.resolveProvider();
    }

    /**
     * Resolves the appropriate storage provider based on the configuration. Currently supports 'local'.
     */
    resolveProvider() {
        switch (storageConfig.driver) {
            case 'local':
                return new LocalStorageProvider();
            default:
                throw new Error(`Unsupported storage driver config: ${storageConfig.driver}`);
        }
    }

    async store(file) {
        return await this.provider.put(file);
    }

    async delete(storageKey) {
        return await this.provider.remove(storageKey);
    }

    async getPath(storageKey) {
        return await this.provider.resolvePath(storageKey);
    }
}

export default new StorageService();