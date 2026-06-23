import mediaService from '../services/MediaService.js';
import { UploadMediaDto } from '../dtos/UploadMediaDto.js';
import { MediaResponseDto } from '../dtos/MediaResponseDto.js';

/**
 * Handles incoming HTTP requests related to media operations (upload, get, delete) and delegates business logic to MediaService.
 */
class MediaController {

    /**
     * Handles media upload requests. Expects a multipart/form-data request with a file and conversationId in the body. Validates the request, constructs an UploadMediaDto, and calls MediaService to process the upload. Returns the created media record or appropriate error responses.
     * @route POST /api/media/upload
     * @access Protected (requires authentication and 'media:write' scope)
     * @returns {Object} 201 - Created media record
     * @returns {Object} 400 - Validation errors
     * @returns {Object} 500 - Internal server error
     */
    async upload(req, res) {
        try {
            const dtoPayload = {
                conversationId: req.body.conversationId, 
                uploaderId: req.user.id
            };
            
            const uploadDto = new UploadMediaDto(dtoPayload, req.file);
            const record = await mediaService.uploadMedia(uploadDto);
            const response = new MediaResponseDto(record);
            
            return res.status(201).json(response);
        } catch (error) {
            if (error.statusCode === 400) {
                return res.status(400).json({ error: error.message, details: error.details });
            }
            return res.status(500).json({ error: error.message });
        }
    }

    /**
     * Handles requests to retrieve media by ID. Streams the media file back to the client.
     * @route GET /api/media/:id
     * @access Protected (requires authentication and 'media:read' scope)
     * @returns {File} 200 - Media file stream
     * @returns {Object} 404 - Media not found
     * @returns {Object} 500 - Internal server error
     */
    async get(req, res) {
        try {
            const { id } = req.params;
            const { media, absolutePath } = await mediaService.getMedia(id);
            
            res.setHeader('Content-Type', media.mime_type);
            res.setHeader('Content-Disposition', `inline; filename="${media.file_name}"`);
            return res.sendFile(absolutePath);
        } catch (error) {
            if (error.message === 'Media not found') {
                return res.status(404).json({ error: error.message });
            }
            return res.status(500).json({ error: error.message });
        }
    }

    /**
     * Handles requests to delete media by ID.
     * @route DELETE /api/media/:id
     * @access Protected (requires authentication and 'media:delete' scope)
     * @returns {Object} 200 - Success message
     * @returns {Object} 404 - Media not found or already deleted
     * @returns {Object} 500 - Internal server error
     */
    async delete(req, res) {
        try {
            const { id } = req.params;
            await mediaService.deleteMedia(id);
            return res.status(200).json({ message: 'Media deleted successfully' });
        } catch (error) {
            if (error.message === 'Media not found or already deleted') {
                return res.status(404).json({ error: error.message });
            }
            return res.status(500).json({ error: error.message });
        }
    }
}

export default new MediaController();