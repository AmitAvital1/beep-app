package beep.app.data.entities;

import beep.engine.location.Location;
import beep.engine.ride.invitation.InvitationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rides_invitations")
public class RideInvitationEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "invitation_id", updatable = false, nullable = false)
    private UUID invitationID;

    @Column(name="invitation_date_time")
    private LocalDateTime dateTime;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;

    @Column(name="source_latitude")
    private Double sourceLatitude;

    @Column(name="source_longitude")
    private Double sourceLongitude;

    @Column(name="invitation_status")
    private String invitationStatus;

    @OneToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "ride_id")
    private RideEntity rideEntity;

    public RideInvitationEntity() {
    }

    public RideInvitationEntity(UUID invitationID, LocalDateTime dateTime, UserEntity sender, UserEntity receiver, Double sourceLatitude, Double sourceLongitude, String invitationStatus) {
        this.invitationID = invitationID;
        this.dateTime = dateTime;
        this.sender = sender;
        this.receiver = receiver;
        this.sourceLatitude = sourceLatitude;
        this.sourceLongitude = sourceLongitude;
        this.invitationStatus = invitationStatus;
    }

    public RideInvitationEntity(UserEntity sender, UserEntity receiver, Double sourceLatitude, Double sourceLongitude) {
        this.dateTime = LocalDateTime.now();
        this.sender = sender;
        this.receiver = receiver;
        this.sourceLatitude = sourceLatitude;
        this.sourceLongitude = sourceLongitude;
        this.invitationStatus = InvitationStatus.PENDING.toString();
    }

    public UUID getInvitationID() {
        return invitationID;
    }

    public void setInvitationID(UUID invitationID) {
        this.invitationID = invitationID;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public Double getSourceLatitude() {
        return sourceLatitude;
    }

    public void setSourceLatitude(Double sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public Double getSourceLongitude() {
        return sourceLongitude;
    }

    public void setSourceLongitude(Double sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public String getInvitationStatus() {
        return invitationStatus;
    }

    public void setInvitationStatus(String invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public RideEntity getRideEntity() {
        return rideEntity;
    }

    public void setRideEntity(RideEntity rideEntity) {
        this.rideEntity = rideEntity;
    }
}
