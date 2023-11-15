package beep.app.controller;

import beep.app.constants.Constants;
import beep.app.data.dao.LoginCodeRepository;
import beep.app.data.dao.UserRepository;
import beep.app.data.entities.UserEntity;
import beep.app.data.login.LoginCode;
import beep.app.service.LoginCodeService;
import beep.app.utils.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import login.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    private UserRepository userRepository;
    private LoginCodeService loginCodeService;

    @Autowired
    public LoginController(UserRepository userRepository, LoginCodeService loginCodeService){
        this.userRepository = userRepository;
        this.loginCodeService = loginCodeService;
    }
    @PutMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request,@RequestBody UserDTO dto){
        Integer verificationCode = loginCodeService.generateVerificationCode();
        LoginCode loginCode = new LoginCode(verificationCode,dto.getPhoneNumber());
        loginCodeService.saveLoginCode(loginCode);
        return ResponseEntity.status(200).body(dto);
    }
    @PostMapping("/code/{code}")
    public ResponseEntity<?> codeAuthentication(@PathVariable Integer code, @RequestBody UserDTO dto, HttpServletRequest request){
        UserEntity userEntity = userRepository.findByPhoneNumber(dto.getPhoneNumber());
        Integer dbCode = loginCodeService.findMostRecentCodeByPhoneNumber(dto.getPhoneNumber());
        UserDTO userDTO;
        if(!code.equals(dbCode))
            return ResponseEntity.badRequest().body("Invalid Code. Please try again");
        else{
            if(userEntity != null){//User exist
                userDTO = new UserDTO(userEntity.getUserId(),userEntity.getFirstName(),userEntity.getLastName(),userEntity.getPhoneAreaCode(),userEntity.getPhoneNumber());
                String userIdFromSession = SessionUtils.getUserId(request);
                if (userIdFromSession == null) {
                    request.getSession(true).setAttribute(Constants.USER_ID, userEntity.getUserId());
                }else {
                    if (!userIdFromSession.equals(userEntity.getUserId())) {
                        SessionUtils.clearSession(request);
                        request.getSession(true).setAttribute(Constants.USER_ID, userEntity.getUserId());
                    }
                }
                return ResponseEntity.status(200).body(userDTO);
            }else{//Need to register after authentication
                userDTO = dto;
                return ResponseEntity.status(201).body(userDTO);
            }
        }
    }
    @PutMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request,@RequestBody UserDTO dto){
        if(!isValidName(dto.getFirstName()))
            return ResponseEntity.status(400).body("Invalid first name");
        if(!isValidName(dto.getLastName()))
            return ResponseEntity.status(406).body("Invalid last name");

        if(userRepository.findByPhoneNumber(dto.getPhoneNumber()) == null) {
            UserEntity userEntity = new UserEntity(dto.getFirstName(), dto.getLastName(), dto.getPhoneAreaCode(), dto.getPhoneNumber());
            userRepository.save(userEntity);
            request.getSession(true).setAttribute(Constants.USER_ID, userEntity.getUserId());
            UserDTO userDTO = new UserDTO(userEntity.getUserId(), userEntity.getFirstName(), userEntity.getLastName(), userEntity.getPhoneAreaCode(), userEntity.getPhoneNumber());
            return ResponseEntity.status(200).body(userDTO);
        }else
            return ResponseEntity.status(500).body("Error creating exist account");
    }

    private boolean isValidName(String name) {
        // Check if the name contains only alphabetic characters
        return name.matches("^[a-zA-Z]{1,15}$");
    }
   /* @PutMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request,UserDTO dto){
        UserEntity userEntity = userRepository.findByPhoneNumber(dto.getPhoneNumber());
        if(userEntity != null){
            String userIdFromSession = SessionUtils.getUserId(request);
            if (userIdFromSession == null) {
                request.getSession(true).setAttribute(Constants.USER_ID, userEntity.getUserId());
            }else{
                if(!userIdFromSession.equals(userEntity.getUserId())){
                    SessionUtils.clearSession(request);
                    request.getSession(true).setAttribute(Constants.USER_ID, userEntity.getUserId());
                }
            }
            return ResponseEntity.status(200).body(userEntity);
        }else{
            return ResponseEntity.status(201).body(userEntity);
        }

    }*/


}
