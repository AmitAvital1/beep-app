package search;

public class UserPhoneExistDTO {
    private String phoneNumber;
    private boolean hasUser;

    public UserPhoneExistDTO() {
    }

    public UserPhoneExistDTO(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isHasUser() {
        return hasUser;
    }

    public void setHasUser(boolean hasUser) {
        this.hasUser = hasUser;
    }
}
