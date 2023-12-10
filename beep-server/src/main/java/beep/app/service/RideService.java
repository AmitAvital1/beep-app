package beep.app.service;

import beep.app.data.dao.RideRepository;
import beep.app.data.entities.RideEntity;
import beep.engine.ride.RideStatus;
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
    public boolean isRideComplete(RideEntity rideEntity){
        return rideEntity.isSenderArrived();
    }
    public void setRideComplete(RideEntity rideEntity){
        rideEntity.setRideCompleted();
    }
}
