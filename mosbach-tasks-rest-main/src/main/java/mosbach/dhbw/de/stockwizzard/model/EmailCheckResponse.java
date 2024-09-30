package mosbach.dhbw.de.stockwizzard.model;

public class EmailCheckResponse {
    private boolean isRegistered;
    private String message;

    public EmailCheckResponse(boolean isRegistered, String message) {
        this.isRegistered = isRegistered;
        this.message = message;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public String getMessage() {
        return message;
    }
}
