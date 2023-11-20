package beep.app.data.dao;

import beep.app.data.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByPhoneNumber(String phoneNumber);
}
