package ride;

import location.LocationDTO;

public class OnRideRefresherDTO {
    private String rideStatus;
    private LocationDTO senderCurrentLocation;
    private LocationDTO receiverCurrentLocation;
    private String distanceText;
    private String durationTime;

    public OnRideRefresherDTO() {
    }

    public OnRideRefresherDTO(String rideStatus, LocationDTO senderCurrentLocation, LocationDTO receiverCurrentLocation, String distanceText, String durationTime) {
        this.rideStatus = rideStatus;
        this.senderCurrentLocation = senderCurrentLocation;
        this.receiverCurrentLocation = receiverCurrentLocation;
        this.distanceText = distanceText;
        this.durationTime = durationTime;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }

    public LocationDTO getSenderCurrentLocation() {
        return senderCurrentLocation;
    }

    public void setSenderCurrentLocation(LocationDTO senderCurrentLocation) {
        this.senderCurrentLocation = senderCurrentLocation;
    }

    public LocationDTO getReceiverCurrentLocation() {
        return receiverCurrentLocation;
    }

    public void setReceiverCurrentLocation(LocationDTO receiverCurrentLocation) {
        this.receiverCurrentLocation = receiverCurrentLocation;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public String getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(String durationTime) {
        this.durationTime = durationTime;
    }
}
