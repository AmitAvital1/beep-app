package beep.app.data.dao;

import beep.app.data.entities.RideEntity;
import beep.app.data.entities.RideInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RideInvitationRepository extends JpaRepository<RideInvitationEntity, UUID> {

}
