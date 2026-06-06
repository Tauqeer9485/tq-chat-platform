import Media from '../models/Media.js';

/**
 * Repository class for managing media records in the database.
 */
class MediaRepository {
    async save(mediaData) {
        return Media.create(mediaData);
    }

    async findById(id) {
        return Media.findByPk(id);
    }

    async findByConversationId(conversationId) {
        return Media.findAll({
            where: { conversation_id: conversationId }
        });
    }
}

export default new MediaRepository();