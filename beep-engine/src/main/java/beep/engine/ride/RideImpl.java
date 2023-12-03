package beep.engine.ride;

import beep.engine.location.Location;
import beep.engine.ride.Ride;
import beep.engine.ride.invitation.RideInvitation;
import beep.engine.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

import static beep.engine.ride.RideStatus.ON_RIDE;

public class RideImpl implements Ride {
    private UUID rideID;
    private LocalDateTime rideStartTime;
    private User userSender;
    private User userReceiver;
    private Location senderCurrentLocation;
    private Location receiverCurrentLocation;
    private RideStatus rideStatus = ON_RIDE;

    public RideImpl(RideInvitation rideInvitation){
        this.rideID = UUID.randomUUID();
        this.rideStartTime = LocalDateTime.now();
        this.userReceiver = rideInvitation.getUserReceiver();
        this.userSender = rideInvitation.getUserSender();
    }


    @Override
    public int getID() {
        return 0;
    }

    @Override
    public User getUserTarget() {
        return null;
    }

    @Override
    public void executeRide(Double senderLatitude, Double senderLongitude,Double receiverLatitude, Double receiverLongitude) {

    }
}