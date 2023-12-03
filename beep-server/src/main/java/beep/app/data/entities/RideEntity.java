package beep.app.data.entities;

import beep.engine.location.Location;
import beep.engine.ride.RideStatus;
import beep.engine.ride.invitation.InvitationStatus;
import beep.engine.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

import static beep.engine.ride.RideStatus.ON_RIDE;

@Entity
@Table(name = "rides")
public class RideEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "ride_id", updatable = false, nullable = false)
    private UUID rideID;

    @Column(name="ride_start_time")
    private LocalDateTime rideStartTime;

    @OneToOne(mappedBy = "rideEntity",cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private RideInvitationEntity rideInvitationEntity;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;

    @Column(name="sender_current_latitude")
    private Double senderCurrentLatitude;

    @Column(name="sender_current_longitude")
    private Double senderCurrentLongitude;

    @Column(name="receiver_current_latitude")
    private Double receiverCurrentLatitude;

    @Column(name="receiver_current_longitude")
    private Double receiverCurrentLongitude;

    @Column(name="ride_status")
    private String rideStatus;

    public RideEntity() {
    }

    public RideEntity(UserEntity sender, UserEntity receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.rideStartTime = LocalDateTime.now();
        this.rideStatus = ON_RIDE.toString();
    }

    public UUID getRideID() {
        return rideID;
    }

    public LocalDateTime getRideStartTime() {
        return rideStartTime;
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

    public Double getSenderCurrentLatitude() {
        return senderCurrentLatitude;
    }

    public void setSenderCurrentLatitude(Double senderCurrentLatitude) {
        this.senderCurrentLatitude = senderCurrentLatitude;
    }

    public Double getSenderCurrentLongitude() {
        return senderCurrentLongitude;
    }

    public void setSenderCurrentLongitude(Double senderCurrentLongitude) {
        this.senderCurrentLongitude = senderCurrentLongitude;
    }

    public Double getReceiverCurrentLatitude() {
        return receiverCurrentLatitude;
    }

    public void setReceiverCurrentLatitude(Double receiverCurrentLatitude) {
        this.receiverCurrentLatitude = receiverCurrentLatitude;
    }

    public Double getReceiverCurrentLongitude() {
        return receiverCurrentLongitude;
    }

    public void setReceiverCurrentLongitude(Double receiverCurrentLongitude) {
        this.receiverCurrentLongitude = receiverCurrentLongitude;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }

    public RideInvitationEntity getRideInvitationEntity() {
        return rideInvitationEntity;
    }

    public void setRideInvitationEntity(RideInvitationEntity rideInvitationEntity) {
        this.rideInvitationEntity = rideInvitationEntity;
    }
}
