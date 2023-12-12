package beep.app.data.entities;

import beep.engine.location.Location;
import beep.engine.ride.RideStatus;
import beep.engine.ride.invitation.InvitationStatus;
import beep.engine.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static beep.engine.ride.RideStatus.ON_RIDE;

@Entity
@Table(name = "rides")
public class RideEntity {

    private static final double RADIUS_KM = 0.1;
    private static final int EARTH_RADIUS_KM = 6371;

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

    @Column(name="ride_end_time")
    private LocalDateTime rideEndTime;

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

    @Column(name="sender_current_bearing")
    private Float senderCurrentBearing;

    @Column(name="receiver_current_latitude")
    private Double receiverCurrentLatitude;

    @Column(name="receiver_current_longitude")
    private Double receiverCurrentLongitude;

    @Column(name="receiver_current_bearing")
    private Float receiverCurrentBearing;

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

    public LocalDateTime getRideEndTime() {
        return rideEndTime;
    }

    public void setRideEndTime(LocalDateTime rideEndTime) {
        this.rideEndTime = rideEndTime;
    }

    public Float getSenderCurrentBearing() {
        return senderCurrentBearing;
    }

    public void setSenderCurrentBearing(Float senderCurrentBearing) {
        this.senderCurrentBearing = senderCurrentBearing;
    }

    public Float getReceiverCurrentBearing() {
        return receiverCurrentBearing;
    }

    public void setReceiverCurrentBearing(Float receiverCurrentBearing) {
        this.receiverCurrentBearing = receiverCurrentBearing;
    }

    public boolean isSenderArrived(){
        double distance = calculateDistance(this.senderCurrentLatitude, this.senderCurrentLongitude, this.receiverCurrentLatitude, this.receiverCurrentLongitude);
        return distance <= RADIUS_KM;
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Transactional
    public void setRideCompleted(){
        rideStatus = RideStatus.COMPLETED.toString();
        rideEndTime = LocalDateTime.now();
        UserEntity rideSender = this.getSender();
        UserEntity rideReceiver = this.getReceiver();
        rideSender.setOnRide(null);
        rideReceiver.setOnRide(null);
    }

    @Transactional
    public void setRideCanceled() {
        rideStatus = RideStatus.CANCELED.toString();
        rideEndTime = LocalDateTime.now();
        UserEntity rideSender = this.getSender();
        UserEntity rideReceiver = this.getReceiver();
        rideSender.setOnRide(null);
        rideReceiver.setOnRide(null);
    }
    public String getDurationOnString(){
        if(rideStartTime == null || rideEndTime == null)
            return "N/A";

        Duration duration = Duration.between(rideStartTime, rideEndTime);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();

        if (hours < 1) {
            return String.format("%d Min", minutes);
        } else {
            return String.format("%d:%02d Hours", hours, minutes % 60);
        }
    }
    public String getStartTimeForDTO(){
        if(rideStartTime == null)
            return "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return rideStartTime.format(formatter);
    }
    public String getEndTimeForDTO(){
        if(rideEndTime == null)
            return "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return rideEndTime.format(formatter);
    }
}
