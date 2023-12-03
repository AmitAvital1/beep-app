package beep.app.controller;

import beep.app.data.dao.UserRepository;
import beep.app.data.entities.RideEntity;
import beep.app.service.RideService;
import jakarta.servlet.http.HttpServletRequest;
import location.LocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class OnRideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/sender-ride/{ride_id}")
    public ResponseEntity<?> updateSenderRideLocation(@PathVariable String ride_id, HttpServletRequest request, @RequestBody LocationDTO locationDTO){
        RideEntity rideEntity = rideService.findById(ride_id).get();
        synchronized (this){
            rideEntity.setSenderCurrentLatitude(locationDTO.getLatitude());
            rideEntity.setSenderCurrentLongitude(locationDTO.getLongitude());
        }
        return ResponseEntity.ok().body("");
    }
    @PostMapping("/receiver-ride/{ride_id}")
    public ResponseEntity<?> updateReceiverRideLocation(@PathVariable String ride_id, HttpServletRequest request, @RequestBody LocationDTO locationDTO){
        RideEntity rideEntity = rideService.findById(ride_id).get();
        synchronized (this){
            rideEntity.setReceiverCurrentLatitude(locationDTO.getLatitude());
            rideEntity.setReceiverCurrentLongitude(locationDTO.getLongitude());
        }
        return ResponseEntity.ok().body("");
    }
}
