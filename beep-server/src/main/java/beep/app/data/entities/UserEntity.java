package beep.app.data.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="phone_area_code")
    private String phoneAreaCode;

    @Column(name="phone_number")
    private String phoneNumber;

    public UserEntity(){

    }

    public UserEntity(String firstName, String lastName, String phoneAreaCode, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
    }

    public UserEntity(UUID userId, String firstName, String lastName, String phoneAreaCode, String phoneNumber) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId.toString();
    }
    public UUID getUUID() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneAreaCode() {
        return phoneAreaCode;
    }

    public void setPhoneAreaCode(String phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }
}
