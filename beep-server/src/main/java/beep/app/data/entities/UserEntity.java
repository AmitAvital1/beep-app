package beep.app.data.entities;

import beep.engine.ride.invitation.InvitationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.*;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "sender", cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private List<RideInvitationEntity> sentInvitations;

    @OneToMany(mappedBy = "receiver", cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private List<RideInvitationEntity> receivedInvitations;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "on_ride")
    private RideInvitationEntity onRide;

    public UserEntity(){

    }

    public UserEntity(String firstName, String lastName, String phoneAreaCode, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
        this.onRide = null;
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

    public void addSentInvitation(RideInvitationEntity invitation){
        if(sentInvitations == null)
            sentInvitations = new ArrayList<>();
        sentInvitations.add(invitation);
    }
    public void addReceiveInvitation(RideInvitationEntity invitation){
        if(receivedInvitations == null)
            receivedInvitations = new ArrayList<>();
        receivedInvitations.add(invitation);
    }

    public List<RideInvitationEntity> getSentInvitations() {
        return sentInvitations;
    }

    public void setSentInvitations(List<RideInvitationEntity> sentInvitations) {
        this.sentInvitations = sentInvitations;
    }

    public List<RideInvitationEntity> getReceivedInvitations() {
        return receivedInvitations;
    }

    public void setReceivedInvitations(List<RideInvitationEntity> receivedInvitations) {
        this.receivedInvitations = receivedInvitations;
    }
    public String getFullName(){ return firstName + " " + lastName; }

    public RideInvitationEntity getOnRide() {
        return onRide;
    }

    public void setOnRide(RideInvitationEntity onRide) {
        this.onRide = onRide;
    }
    public List<RideInvitationEntity> getAllInvitationsByMostRecent(){
        List<RideInvitationEntity> allInvitations = new ArrayList<>();
        allInvitations.addAll(getReceivedInvitations());
        allInvitations.addAll(getSentInvitations());
        Collections.sort(allInvitations, Comparator.comparing(RideInvitationEntity::getDateTime).reversed());
        return allInvitations;
    }
    public List<RideEntity> getRidesByMostRecent(){
        List<RideInvitationEntity> allInvitationsMostRecent = getAllInvitationsByMostRecent();
        allInvitationsMostRecent.removeIf(invitation -> !invitation.getInvitationStatus().equals(InvitationStatus.ACCEPTED.toString()));
        return allInvitationsMostRecent.stream().map(RideInvitationEntity::getRideEntity).collect(Collectors.toList());
    }
}
