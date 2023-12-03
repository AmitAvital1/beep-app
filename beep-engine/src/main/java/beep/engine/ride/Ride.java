package beep.engine.ride;

import beep.engine.user.User;

public interface Ride {
    int getID();
    User getUserTarget();
    void executeRide(Double senderLatitude, Double senderLongitude,Double receiverLatitude, Double receiverLongitude);
    //Ride status
    //
}
