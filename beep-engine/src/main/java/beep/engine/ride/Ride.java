package beep.engine.ride;

import beep.engine.user.User;

public interface Ride {
    int getID();
    User getUserTarget();
    void executeRide();
    //Ride status
    //
}
