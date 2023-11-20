package beep.app.controller;

import beep.app.data.dao.UserRepository;
import beep.app.data.entities.UserEntity;
import beep.app.service.LoginCodeService;
import jakarta.servlet.http.HttpServletRequest;
import login.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import search.UserPhoneExistDTO;

import java.util.List;

@RestController
public class SearchController {
    private UserRepository userRepository;

    @Autowired
    public SearchController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @PostMapping("/has-user/list")
    public ResponseEntity<?> checkUsers(HttpServletRequest request, @RequestBody List<UserPhoneExistDTO> listUserPhoneExistDTO) {
        for (UserPhoneExistDTO userPhoneExistDTO : listUserPhoneExistDTO) {
            String cleanedNumber = cleanPhoneNumber(new String(userPhoneExistDTO.getPhoneNumber()));
            UserEntity userEntity = userRepository.findByPhoneNumber(cleanedNumber);
            if (userEntity == null) {
                userPhoneExistDTO.setHasUser(false);
            }else{
                userPhoneExistDTO.setUserID(userEntity.getUserId());
                userPhoneExistDTO.setHasUser(true);
            }
        }
        return ResponseEntity.status(200).body(listUserPhoneExistDTO);
    }
    public static String cleanPhoneNumber(String phoneNumber) {
        // Remove spaces and dashes using regex
        return phoneNumber.replaceAll("[\\s-]+", "");
    }
}
