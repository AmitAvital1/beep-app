package beep.engine.user;

import beep.engine.execution.RideExecution;
import beep.engine.location.Location;
import beep.engine.ride.invitation.RideInvitation;

import java.util.List;
import java.util.UUID;

public interface User {
    UUID userID();
    String getFirstName();
    String getLastName();
    String getPhoneNumber();
    String getAreaCode();
    List<String> getFavoriteLocations();
    List<RideExecution> getRides();
    void sendInvitation(User userReceiver, Location location);
    void addInvitation(RideInvitation rideInvitation);
}