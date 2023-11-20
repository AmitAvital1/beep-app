package beep.app.controller;

import beep.app.constants.Constants;
import beep.app.data.dao.UserRepository;
import beep.app.data.entities.UserEntity;
import beep.app.service.LoginCodeService;
import beep.app.utils.SessionUtils;
import beep.engine.ride.invitation.RideInvitation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import login.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static beep.app.controller.SearchController.cleanPhoneNumber;

@RestController
public class RideInvitationController {
    private UserRepository userRepository;

    @Autowired
    public RideInvitationController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @PutMapping("/invite-beep/{phone}")
    public ResponseEntity<?> register(@PathVariable String phone, HttpServletRequest request){
        String userIdFromSession = SessionUtils.getUserId(request);
        String cleanedNumber = cleanPhoneNumber(phone);
        if (userIdFromSession == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Error user not authorized");
        }else{
            Optional<UserEntity> userEntityOptional = userRepository.findById(UUID.fromString(userIdFromSession));
            if(!userEntityOptional.isPresent())
                return ResponseEntity.status(HttpServletResponse.SC_BAD_GATEWAY).body("User not exist");
            UserEntity userEntity = userEntityOptional.get();
            //RideInvitation rideInvitation = new RideInvitation()
            return ResponseEntity.ok().body(userEntity.getFirstName() + " " + userEntity.getLastName());
        }


    }
}
