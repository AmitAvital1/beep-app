package beep.app.service;

import beep.app.data.dao.LoginCodeRepository;
import beep.app.data.login.LoginCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class LoginCodeService {

    @Autowired
    private LoginCodeRepository loginCodeRepository;

    public Integer generateVerificationCode() {
        // Implement code generation logic (e.g., random 4-digit code)
        return new Random().nextInt(1000,9999);
    }

    public void saveLoginCode(LoginCode loginCode){
        loginCodeRepository.save(loginCode);
    }

    public Integer findMostRecentCodeByPhoneNumber(String phoneNumber) {
        return loginCodeRepository.findMostRecentCodeByPhoneNumber(phoneNumber).isPresent() ? loginCodeRepository.findMostRecentCodeByPhoneNumber(phoneNumber).get() : null ;
    }
}
