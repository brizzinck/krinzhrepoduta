package micromobility.web;

import micromobility.service.BusinessException;
import micromobility.service.DbErrors;
import org.springframework.dao.DataIntegrityViolationException;

/** The text for the user from a refused operation: a business rule message or the name of a violated constraint. */
final class Errors {

    private Errors() {
    }

    static String text(RuntimeException e) {
        if (e instanceof BusinessException) {
            return e.getMessage();
        }
        if (e instanceof DataIntegrityViolationException dive) {
            return DbErrors.message(dive);
        }
        if (e instanceof java.util.NoSuchElementException) {
            return "Запис не знайдено.";
        }
        if (e instanceof org.springframework.dao.PessimisticLockingFailureException) {
            return "Операцію не виконано, спробуйте ще раз.";
        }
        throw e;
    }
}
