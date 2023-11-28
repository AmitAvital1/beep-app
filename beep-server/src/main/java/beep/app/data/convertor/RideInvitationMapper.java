package beep.app.data.convertor;

import beep.app.data.entities.RideInvitationEntity;
import beep.app.data.entities.UserEntity;
import beep.engine.ride.invitation.RideInvitation;
import beep.engine.user.User;

public class RideInvitationMapper {
    public static RideInvitationEntity toJpaEntity(RideInvitation rideInvitation){
        RideInvitationEntity rideInvitationEntity = new RideInvitationEntity(rideInvitation.getInvitationID(), rideInvitation.getDateTime(), UserMapper.toJpaEntityNoInvitations(rideInvitation.getUserSender()), UserMapper.toJpaEntityNoInvitations(rideInvitation.getUserReceiver()), rideInvitation.getLatitude(),rideInvitation.getLongitude(),rideInvitation.getStatus().toString());
        return rideInvitationEntity;
    }
}
