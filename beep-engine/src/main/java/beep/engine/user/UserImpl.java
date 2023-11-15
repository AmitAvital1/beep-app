package beep.engine.user;

import beep.engine.execution.RideExecution;
import beep.engine.location.Location;
import beep.engine.ride.invitation.RideInvitation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserImpl implements User{
    private UUID userID;
    private String firstName;
    private String lastName;
    private String phoneAreaCode;
    private String phoneNumber;
    private List<String> favoriteLocations = null;

    private final List<RideInvitation> sentInvitations = new ArrayList<>();
    private final List<RideInvitation> receivedInvitations = new ArrayList<>();

    public UserImpl(UUID userID, String firstName, String lastName, String phoneAreaCode, String phoneNumber){
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneAreaCode = phoneAreaCode;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public UUID userID() {
        return userID;
    }

    @Override
    public String getAreaCode() {
        return phoneAreaCode;
    }

    @Override
    public List<String> getFavoriteLocations() {
        return favoriteLocations;
    }

    @Override
    public List<RideExecution> getRides() {
        return null;
    }

    @Override
    public void sendInvitation(User userReceiver, Location location) {
        RideInvitation invitation = new RideInvitation(this, userReceiver,location);
        sentInvitations.add(invitation);
        userReceiver.addInvitation(invitation);
    }

    @Override
    public void addInvitation(RideInvitation rideInvitation) {
        receivedInvitations.add(rideInvitation);
    }
}
