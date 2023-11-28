package ride;

import location.LocationDTO;
import login.UserDTO;

import java.time.LocalDateTime;

public class RideDTO {
    private String invitationID;
    private UserDTO userSender;
    private UserDTO userReceiver;
    private String time;
    private LocationDTO senderLocation;
    private LocationDTO receiverLocation;
    private String invitationStatus;

    public RideDTO() {
    }

    public RideDTO(String invitationID, UserDTO userSender, UserDTO userReceiver, String time, LocationDTO senderLocation, LocationDTO receiverLocation, String invitationStatus) {
        this.invitationID = invitationID;
        this.userSender = userSender;
        this.userReceiver = userReceiver;
        this.time = time;
        this.senderLocation = senderLocation;
        this.receiverLocation = receiverLocation;
        this.invitationStatus = invitationStatus;
    }

    public String getInvitationID() {
        return invitationID;
    }

    public void setInvitationID(String invitationID) {
        this.invitationID = invitationID;
    }


    public LocationDTO getSenderLocation() {
        return senderLocation;
    }

    public void setSenderLocation(LocationDTO senderLocation) {
        this.senderLocation = senderLocation;
    }

    public LocationDTO getReceiverLocation() {
        return receiverLocation;
    }

    public void setReceiverLocation(LocationDTO receiverLocation) {
        this.receiverLocation = receiverLocation;
    }

    public String getInvitationStatus() {
        return invitationStatus;
    }

    public void setInvitationStatus(String invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public UserDTO getUserSender() {
        return userSender;
    }

    public void setUserSender(UserDTO userSender) {
        this.userSender = userSender;
    }

    public UserDTO getUserReceiver() {
        return userReceiver;
    }

    public void setUserReceiver(UserDTO userReceiver) {
        this.userReceiver = userReceiver;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
