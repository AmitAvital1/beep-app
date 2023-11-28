package beep.engine.ride.invitation;

import beep.engine.location.Location;
import beep.engine.location.LocationImpl;
import beep.engine.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class RideInvitation {
    private UUID invitationID;
    private LocalDateTime dateTime;
    private User userSender;
    private User userReceiver;
    private Location sourceLocation;
    private InvitationStatus status = InvitationStatus.PENDING;

    public RideInvitation(User userSender, User userReceiver, Location sourceLocation) {
        this.invitationID = UUID.randomUUID();
        this.dateTime = LocalDateTime.now();
        this.userReceiver = userReceiver;
        this.userSender = userSender;
        this.sourceLocation = sourceLocation;
    }
    public RideInvitation(UUID invitationID,LocalDateTime dateTime,User userSender, User userReceiver, Double sourceLatitude,Double sourceLongitude, String status) {
        this.invitationID = invitationID;
        this.dateTime = dateTime;
        this.userReceiver = userReceiver;
        this.userSender = userSender;
        this.sourceLocation = new LocationImpl(null,sourceLatitude,sourceLongitude);
        this.status = InvitationStatus.valueOf(status);
    }
    public void accept(){
        status = InvitationStatus.ACCEPTED;
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

    public User getUserSender() {
        return userSender;
    }

    public void setUserSender(User userSender) {
        this.userSender = userSender;
    }

    public User getUserReceiver() {
        return userReceiver;
    }

    public void setUserReceiver(User userReceiver) {
        this.userReceiver = userReceiver;
    }

    public Location getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(Location sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }
    public Double getLatitude(){
        return sourceLocation.getLatitude();
    }
    public Double getLongitude(){
        return sourceLocation.getLongitude();
    }

}
