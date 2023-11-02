package beep.engine.ride.invitation;

import beep.engine.ride.Ride;
import beep.engine.user.User;

import java.util.UUID;

public class RideImpl implements Ride {
    private UUID rideID;

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public User getUserTarget() {
        return null;
    }

    @Override
    public void executeRide() {

    }
}