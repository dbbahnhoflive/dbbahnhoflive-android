package de.deutschebahn.bahnhoflive.backend.ris.model;

public class Message {

    private String id;
    private String validFrom;
    private String validTo;
    private String type;
    private String code;
    private String displayMessage;
    private boolean deleted;
    private boolean revoked;

    public void setId(String id) {
        this.id = id;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidTo(String validTo) {
        this.validTo = validTo;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getId() {
        return id;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", validFrom='" + validFrom + '\'' +
                ", validTo='" + validTo + '\'' +
                ", type='" + type + '\'' +
                ", code='" + code + '\'' +
                ", displayMessage='" + displayMessage + '\'' +
                ", deleted=" + deleted +
                ", revoked=" + revoked +
                '}';
    }
}
