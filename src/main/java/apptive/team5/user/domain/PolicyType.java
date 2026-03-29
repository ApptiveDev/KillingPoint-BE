package apptive.team5.user.domain;

public enum PolicyType {

    SERVICE_TERMS(true),
    PRIVACY(true);

    private final boolean required;

    PolicyType(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }
}
