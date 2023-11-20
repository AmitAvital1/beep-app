package beep.engine.ride.invitation;

import beep.engine.location.Location;
import beep.engine.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class RideInvitation {
    private UUID invitationID;
    private LocalDateTime dateTime;
    private User userSender;
    private User userReceiver;
    private Location sourceLocation;
    private InvitationStatus status = InvitationStatus.PENDING;

    public RideInvitation(User userSender, User userReceiver, Location sourceLocation) {
        this.invitationID = UUID.randomUUID();
        this.dateTime = LocalDateTime.now();
        this.userReceiver = userReceiver;
        this.userSender = userSender;
        this.sourceLocation = sourceLocation;
    }
    public void accept(){
        status = InvitationStatus.ACCEPTED;
    }

}
