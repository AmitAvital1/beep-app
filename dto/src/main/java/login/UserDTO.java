package login;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private String userID;
    private String firstName;
    private String lastName;
    private String phoneAreaCode;
    private String phoneNumber;

    public UserDTO() {
    }

    public UserDTO(String userID, String firstName, String lastName, String phoneAreaCode, String phoneNumber) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
    }

    public UserDTO(String phoneAreaCode, String phoneNumber) {
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneAreaCode() {
        return phoneAreaCode;
    }

    public void setPhoneAreaCode(String phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
