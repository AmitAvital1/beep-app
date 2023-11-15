package beep.app.data.dao;

import beep.app.data.entities.UserEntity;
import beep.app.data.login.LoginCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoginCodeRepository extends JpaRepository<LoginCode,Long> {
    @Query("SELECT code FROM LoginCode " +
            "WHERE identifier = :phoneNumber " +
            "ORDER BY createdOn DESC " +
            "LIMIT 1")
    Optional<Integer> findMostRecentCodeByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
