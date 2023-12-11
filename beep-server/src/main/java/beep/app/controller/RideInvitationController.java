package beep.app.controller;

import beep.app.constants.Constants;
import beep.app.data.dao.RideInvitationRepository;
import beep.app.data.dao.UserRepository;
import beep.app.data.entities.RideEntity;
import beep.app.data.entities.RideInvitationEntity;
import beep.app.data.entities.UserEntity;
import beep.app.service.LoginCodeService;
import beep.app.utils.SessionUtils;
import beep.engine.ride.invitation.InvitationStatus;
import beep.engine.ride.invitation.RideInvitation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import location.LocationDTO;
import login.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static beep.app.controller.SearchController.cleanPhoneNumber;

@RestController
public class RideInvitationController {
    private UserRepository userRepository;
    private RideInvitationRepository rideInvitationRepository;

    @Autowired
    public RideInvitationController(UserRepository userRepository,RideInvitationRepository rideInvitationRepository){
        this.userRepository = userRepository;
        this.rideInvitationRepository = rideInvitationRepository;
    }

    @PutMapping("/invite-beep/{phone}")
    public ResponseEntity<?> createInvitation(@PathVariable String phone, HttpServletRequest request, @RequestBody LocationDTO locationDTO){
        String userIdFromSession = SessionUtils.getUserId(request);
        String cleanedPhoneNumber = cleanPhoneNumber(phone);
        if (userIdFromSession == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Error user not authorized");
        }else{
            Optional<UserEntity> userSenderEntityOptional = userRepository.findById(UUID.fromString(userIdFromSession));
            if(!userSenderEntityOptional.isPresent())
                return ResponseEntity.status(HttpServletResponse.SC_BAD_GATEWAY).body("User not exist");
            UserEntity userSenderEntity = userSenderEntityOptional.get();
            UserEntity userReceiverEntity = userRepository.findByPhoneNumber(cleanedPhoneNumber);
            if(userReceiverEntity == null)
                return ResponseEntity.status(HttpServletResponse.SC_BAD_GATEWAY).body("Please invite to using beep!");

            if(userSenderEntity.getOnRide() != null ||userReceiverEntity.getOnRide() != null )
                return ResponseEntity.status(HttpServletResponse.SC_BAD_GATEWAY).body("Cannot send invitation while you / target on a ride.");

            RideInvitationEntity newInvitation = new RideInvitationEntity(userSenderEntity,userReceiverEntity,locationDTO.getLatitude(),locationDTO.getLongitude());
            userSenderEntity.addSentInvitation(newInvitation);
            userReceiverEntity.addReceiveInvitation(newInvitation);

            userSenderEntity.setOnRide(newInvitation);
            userReceiverEntity.setOnRide(newInvitation);

            userRepository.save(userSenderEntity);
            userRepository.save(userReceiverEntity);

            return ResponseEntity.ok().body(userSenderEntity.getFirstName() + " " + userSenderEntity.getLastName());
        }
    }
    @PostMapping("/accept-invitation/{invitation_id}")
    public ResponseEntity<?> acceptInvitation(@PathVariable String invitation_id, HttpServletRequest request, @RequestBody LocationDTO locationDTO ){
        synchronized (this) {
            Optional<RideInvitationEntity> optionalRideInvitationEntity = rideInvitationRepository.findById(UUID.fromString(invitation_id));
            RideInvitationEntity rideInvitationEntity = optionalRideInvitationEntity.get();
            if(rideInvitationEntity.getInvitationStatus().equals(InvitationStatus.PENDING.toString())) {
                rideInvitationEntity.setInvitationStatus(InvitationStatus.ACCEPTED.toString());
                RideEntity rideEntity = new RideEntity(rideInvitationEntity.getSender(), rideInvitationEntity.getReceiver());
                rideEntity.setSenderCurrentLatitude(rideInvitationEntity.getSourceLatitude());
                rideEntity.setSenderCurrentLongitude(rideInvitationEntity.getSourceLongitude());
                rideEntity.setReceiverCurrentLatitude(locationDTO.getLatitude());
                rideEntity.setReceiverCurrentLongitude(locationDTO.getLongitude());
                rideEntity.setSenderCurrentBearing(Float.valueOf(0));
                rideEntity.setReceiverCurrentBearing(locationDTO.getBearing());
                rideInvitationEntity.setRideEntity(rideEntity);

                rideInvitationRepository.save(rideInvitationEntity);
                return ResponseEntity.ok().body("Invitation accepted");
            }else
                return ResponseEntity.status(HttpServletResponse.SC_REQUEST_TIMEOUT).body("Invitation has been canceled");
        }
    }
    @PostMapping("/reject-invitation/{invitation_id}")
    public ResponseEntity<?> acceptInvitation(@PathVariable String invitation_id, HttpServletRequest request){
        synchronized (this) {
            Optional<RideInvitationEntity> optionalRideInvitationEntity = rideInvitationRepository.findById(UUID.fromString(invitation_id));
            RideInvitationEntity rideInvitationEntity = optionalRideInvitationEntity.get();
            if(rideInvitationEntity.getInvitationStatus().equals(InvitationStatus.PENDING.toString())) {
                rideInvitationEntity.rejectInvitation();
                rideInvitationRepository.save(rideInvitationEntity);
                return ResponseEntity.ok().body("Invitation rejected");
            }else{
                return ResponseEntity.status(HttpServletResponse.SC_REQUEST_TIMEOUT).body("Invitation has been canceled");
            }
        }
    }
    @PostMapping("/cancel-invitation/{invitation_id}")
    public ResponseEntity<?> cancelInvitation(@PathVariable String invitation_id, HttpServletRequest request){
        synchronized(this){
            Optional<RideInvitationEntity> optionalRideInvitationEntity = rideInvitationRepository.findById(UUID.fromString(invitation_id));
            RideInvitationEntity rideInvitationEntity = optionalRideInvitationEntity.get();
            if(rideInvitationEntity.getInvitationStatus().equals(InvitationStatus.PENDING.toString())) {
                rideInvitationEntity.cancelInvitation();
                rideInvitationRepository.save(rideInvitationEntity);
                return ResponseEntity.ok().body("Invitation canceled");
            }else{
                return ResponseEntity.status(HttpServletResponse.SC_REQUEST_TIMEOUT).body("Invitation already has been answered by the receiver");
            }
        }
    }
}
