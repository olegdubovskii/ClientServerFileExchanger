package exeptions;

public class SuchUserExistsException extends Exception {
    String message;

    public SuchUserExistsException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
