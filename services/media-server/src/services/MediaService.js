import mediaRepository from '../repositories/MediaRepository.js';
import storageService from './StorageService.js';
import { generateSignedUrl } from './SignedUrlService.js';
import path from 'path';

class MediaService {
    /**
     * Handles the media upload process:
     * 1. Saves the physical file using StorageService.
     * 2. Saves metadata to the database.
     * 3. Mints a secure, 1-week Signed URL for immediate frontend access.
     * @param {UploadMediaDto} dto 
     */
    async uploadMedia(dto) {
        const storageKey = await storageService.store(dto.file);

        const mediaData = {
            conversation_id: dto.conversationId,
            uploader_id: dto.uploaderId,
            file_name: dto.file.originalname,
            mime_type: dto.file.mimetype,
            size: dto.file.size,
            extension: path.extname(dto.file.originalname).replace('.', ''),
            storage_key: storageKey,
            status: 'active'
        };

        const savedMedia = await mediaRepository.save(mediaData);
        const secureDownloadUrl =  generateSignedUrl(savedMedia.id);

        return {
            ...savedMedia.toJSON ? savedMedia.toJSON() : savedMedia,
            downloadUrl: secureDownloadUrl
        };
    }

    /**
     * Retrieves media metadata from the database and gets the absolute file path from StorageService.
     * Throws an error if media is not found or is marked as deleted.
     * @param {string} mediaId 
     */
    async getMedia(mediaId) {
        const media = await mediaRepository.findById(mediaId);
        if (!media || media.status === 'deleted') {
            throw new Error('Media not found');
        }
        
        const absolutePath = await storageService.getPath(media.storage_key);
        return { media, absolutePath };
    }

    /**
     * Deletes media by:
     * 1. Removing the physical file from storage.
     * 2. Updating the media record in the database to mark it as deleted (soft delete).
     * @param {string} mediaId 
     */
    async deleteMedia(mediaId) {
        const media = await mediaRepository.findById(mediaId);
        if (!media || media.status === 'deleted') {
            throw new Error('Media not found or already deleted');
        }

        await storageService.delete(media.storage_key);

        media.status = 'deleted';
        await media.save();

        return media;
    }
}

export default new MediaService();
