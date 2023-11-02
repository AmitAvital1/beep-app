package beep.engine.user;

import beep.engine.execution.RideExecution;
import beep.engine.location.Location;
import beep.engine.ride.invitation.RideInvitation;

import java.util.List;

public interface User {
    String getFirstName();
    String getLastName();
    String getPhoneNumber();
    List<String> getFavoriteLocations();
    List<RideExecution> getRides();
    void sendInvitation(User userReceiver, Location location);
    void addInvitation(RideInvitation rideInvitation);
}