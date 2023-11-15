package beep.app.data.login;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_code")
public class LoginCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="code")
    private int code;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name="identifier")
    private String identifier;

    @PrePersist
    private void onCreate() {
        this.createdOn = LocalDateTime.now();
    }

    public LoginCode() {
    }

    public LoginCode(int code, String identifier) {
        this.code = code;
        this.identifier = identifier;
        this.createdOn = LocalDateTime.now();
    }

    public LoginCode(LocalDateTime createdOn, String userID) {
        this.createdOn = createdOn;
        this.identifier = userID;
    }

    public LoginCode(int code, LocalDateTime createdOn, String identifier) {
        this.code = code;
        this.createdOn = createdOn;
        this.identifier = identifier;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getUserID() {
        return identifier;
    }

    public void setUserID(String userID) {
        this.identifier = userID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
