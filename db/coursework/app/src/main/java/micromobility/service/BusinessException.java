package micromobility.service;

/** A business rule refused the operation; the message is shown to the user (Ukrainian). The transaction rolls back. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
