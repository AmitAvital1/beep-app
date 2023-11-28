package beep.app.data.dao;

import beep.app.data.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.sentInvitations WHERE u.userId = :userId")
    UserEntity findUserWithSentInvitations(@Param("userId") UUID userId);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.receivedInvitations WHERE u.userId = :userId")
    UserEntity findUserWithReceiveInvitations(@Param("userId") UUID userId);

    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.sentInvitations " +
            "LEFT JOIN FETCH u.receivedInvitations " +
            "WHERE u.userId = :userId")
    UserEntity findUserWithSentAndReceivedInvitations(@Param("userId") UUID userId);
}
