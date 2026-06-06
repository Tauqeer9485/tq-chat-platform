/**
 * Abstract class acting as an interface for all storage engine drivers.
 */
class StorageProvider {
    async put(file) {
        throw new Error("Method 'put()' must be implemented.");
    }

    async remove(storageKey) {
        throw new Error("Method 'remove()' must be implemented.");
    }

    async resolvePath(storageKey) {
        throw new Error("Method 'resolvePath()' must be implemented.");
    }
}

export default StorageProvider;