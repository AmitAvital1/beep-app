package fetch;

import ride.RideDTO;

public class UserOnRideDTO {
    private boolean onRide;
    private boolean sender;
    private RideDTO rideDTO;
    private Integer currentRides;

    public UserOnRideDTO() {
    }

    public UserOnRideDTO(boolean onRide, boolean sender, RideDTO rideDTO) {
        this.onRide = onRide;
        this.sender = sender;
        this.rideDTO = rideDTO;
    }

    public boolean isOnRide() {
        return onRide;
    }

    public void setOnRide(boolean onRide) {
        this.onRide = onRide;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public RideDTO getRideDTO() {
        return rideDTO;
    }

    public void setRideDTO(RideDTO rideDTO) {
        this.rideDTO = rideDTO;
    }

    public Integer getCurrentRides() {
        return currentRides;
    }

    public void setCurrentRides(Integer currentRides) {
        this.currentRides = currentRides;
    }
}
