package beep.app.data.dao;

import beep.app.data.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,String> {
    UserEntity findByPhoneNumber(String phoneNumber);
}
