package beep.app.rides;

import fetch.RideMenuDTO;

public class RideItem {
    private String senderName;
    private String receiverName;
    private String rideStatus;
    private String startRideTime;
    private String endRideTime;
    private String duration;
    private String targetLocation;

    public RideItem() {
    }

    public RideItem(RideMenuDTO rideMenuDTO){
        this.senderName = rideMenuDTO.getSenderName();
        this.receiverName = rideMenuDTO.getReceiverName();
        this.rideStatus = rideMenuDTO.getRideStatus();
        this.startRideTime = rideMenuDTO.getStartRideTime();
        this.endRideTime = rideMenuDTO.getEndRideTime();
        this.duration = rideMenuDTO.getDuration();
        this.targetLocation = rideMenuDTO.getTargetLocation();
    }
    public RideItem(String senderName, String receiverName, String rideStatus, String startRideTime, String endRideTime, String duration, String targetLocation) {
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.rideStatus = rideStatus;
        this.startRideTime = startRideTime;
        this.endRideTime = endRideTime;
        this.duration = duration;
        this.targetLocation = targetLocation;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }

    public String getStartRideTime() {
        return startRideTime;
    }

    public void setStartRideTime(String startRideTime) {
        this.startRideTime = startRideTime;
    }

    public String getEndRideTime() {
        return endRideTime;
    }

    public void setEndRideTime(String endRideTime) {
        this.endRideTime = endRideTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }
}
