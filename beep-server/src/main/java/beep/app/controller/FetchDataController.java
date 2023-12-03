package beep.app.controller;

import beep.app.data.dao.UserRepository;
import beep.app.data.entities.RideInvitationEntity;
import beep.app.data.entities.UserEntity;
import beep.app.utils.SessionUtils;
import fetch.UserOnRideDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import location.LocationDTO;
import login.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ride.RideDTO;

import java.util.Optional;
import java.util.UUID;

import static beep.app.controller.SearchController.cleanPhoneNumber;

@RestController
public class FetchDataController {

    private UserRepository userRepository;

    @Autowired
    public FetchDataController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping("/on_ride_status")
    public ResponseEntity<?> register(HttpServletRequest request){
        String userIdFromSession = SessionUtils.getUserId(request);
        if (userIdFromSession == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Error user not authorized");
        }else{
            Optional<UserEntity> userSenderEntityOptional = userRepository.findById(UUID.fromString(userIdFromSession));
            if(!userSenderEntityOptional.isPresent())
                return ResponseEntity.status(HttpServletResponse.SC_BAD_GATEWAY).body("User not exist");

            UserEntity userEntity = userSenderEntityOptional.get();
            RideInvitationEntity rideInvitationEntity = userEntity.getOnRide();
            UserOnRideDTO userOnRideDTO = new UserOnRideDTO();
            if(rideInvitationEntity == null){
                userOnRideDTO.setOnRide(false);
            }else{
                userOnRideDTO.setOnRide(true);
                if(rideInvitationEntity.getSender().getUserId().equals(userEntity.getUserId()))
                    userOnRideDTO.setSender(true);
                else
                    userOnRideDTO.setSender(false);
                UserEntity sender = rideInvitationEntity.getSender();
                UserEntity receiver = rideInvitationEntity.getReceiver();

                UserDTO senderDTO = new UserDTO(sender.getUserId(),sender.getFirstName(),sender.getLastName(),sender.getPhoneAreaCode(),sender.getPhoneNumber());
                UserDTO receiverDTO = new UserDTO(receiver.getUserId(),receiver.getFirstName(),receiver.getLastName(),receiver.getPhoneAreaCode(),receiver.getPhoneNumber());

                RideDTO rideDTO = new RideDTO(rideInvitationEntity.getInvitationID().toString(),senderDTO,receiverDTO,rideInvitationEntity.getDateTime().toString(),new LocationDTO(null,rideInvitationEntity.getSourceLatitude(),rideInvitationEntity.getSourceLongitude()),null,rideInvitationEntity.getInvitationStatus());

                if(rideInvitationEntity.getRideEntity() != null)
                    rideDTO.setRideID(rideInvitationEntity.getRideEntity().getRideID().toString());

                userOnRideDTO.setRideDTO(rideDTO);
            }
            return ResponseEntity.ok().body(userOnRideDTO);
        }
    }
}
