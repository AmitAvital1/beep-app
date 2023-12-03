package beep.app.service;

import beep.app.data.dao.RideRepository;
import beep.app.data.entities.RideEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    public void save(RideEntity rideEntity){
        rideRepository.save(rideEntity);
    }
    public Optional<RideEntity> findById(String rideID){
        return rideRepository.findById(UUID.fromString(rideID));
    }
}
