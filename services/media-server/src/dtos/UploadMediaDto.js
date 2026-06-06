/**
 * Data Transfer Object (DTO) for validating and structuring incoming data for media uploads.
 */
export class UploadMediaDto {
    
    constructor(body, file) {
        this.conversationId = body.conversationId?.trim();
        this.uploaderId = body.uploaderId?.trim();
        this.file = file;

        this.validate();
    }

    validate() {
        const errors = [];

        if (!this.conversationId) {
            errors.push('conversationId is required and cannot be empty.');
        }

        if (!this.uploaderId) {
            errors.push('uploaderId is required.');
        } else {
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
            if (!uuidRegex.test(this.uploaderId)) {
                errors.push('uploaderId must be a valid UUID.');
            }
        }

        if (!this.file) {
            errors.push('A binary file must be provided under the form-data key "file".');
        }

        if (errors.length > 0) {
            const error = new Error('Validation Failed');
            error.statusCode = 400;
            error.details = errors;
            throw error;
        }
    }
}