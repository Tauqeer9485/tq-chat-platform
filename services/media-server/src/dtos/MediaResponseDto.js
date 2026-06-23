/**
 * Data Transfer Object (DTO) for structuring outgoing media upload responses.
 * Screens out internal database fields like storage keys, status, and timestamps.
 */
export class MediaResponseDto {
    constructor(record) {
        this.id = record.id;
        this.file_name = record.file_name;
        this.mime_type = record.mime_type;
        this.size = String(record.size); 
        this.extension = record.extension;
        this.downloadUrl = record.downloadUrl; 
    }
}