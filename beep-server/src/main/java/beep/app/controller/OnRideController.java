package beep.app.controller;

import beep.app.data.dao.UserRepository;
import beep.app.data.entities.RideEntity;
import beep.app.google.api.DistanceMatrixResponse;
import beep.app.service.DistanceMatrixService;
import beep.app.service.RideService;
import jakarta.servlet.http.HttpServletRequest;
import location.LocationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ride.OnRideRefresherDTO;

import java.util.Optional;

@RestController
public class OnRideController {

    @Autowired
    private RideService rideService;

    @Autowired
    private DistanceMatrixService distanceMatrixService;

    @PostMapping("/sender-ride/{ride_id}")
    public ResponseEntity<?> updateSenderRideLocation(@PathVariable String ride_id, HttpServletRequest request, @RequestBody LocationDTO locationDTO){
        RideEntity rideEntity;
        synchronized (this){
            rideEntity = rideService.findById(ride_id).get();
            rideEntity.setSenderCurrentLatitude(locationDTO.getLatitude());
            rideEntity.setSenderCurrentLongitude(locationDTO.getLongitude());
            rideService.save(rideEntity);
        }
        DistanceMatrixResponse distanceMatrixResponse = distanceMatrixService.getDistanceMatrix(locationDTO.getLatitude(),locationDTO.getLongitude(),rideEntity.getReceiverCurrentLatitude(), rideEntity.getReceiverCurrentLongitude());
        String distanceText = distanceMatrixService.getDistanceTextFromDistanceMatrixResponse(distanceMatrixResponse);
        String durationText = distanceMatrixService.getDurationTextFromDistanceMatrixResponse(distanceMatrixResponse);
        OnRideRefresherDTO onRideRefresherDTO = new OnRideRefresherDTO(rideEntity.getRideStatus(),locationDTO,new LocationDTO(null,rideEntity.getReceiverCurrentLatitude(), rideEntity.getReceiverCurrentLongitude()),distanceText,durationText);
        return ResponseEntity.ok().body(onRideRefresherDTO);
    }
    @PostMapping("/receiver-ride/{ride_id}")
    public ResponseEntity<?> updateReceiverRideLocation(@PathVariable String ride_id, HttpServletRequest request, @RequestBody LocationDTO locationDTO){
        RideEntity rideEntity;
        synchronized (this){
            rideEntity = rideService.findById(ride_id).get();
            rideEntity.setReceiverCurrentLatitude(locationDTO.getLatitude());
            rideEntity.setReceiverCurrentLongitude(locationDTO.getLongitude());
            rideService.save(rideEntity);
        }
        DistanceMatrixResponse distanceMatrixResponse = distanceMatrixService.getDistanceMatrix(rideEntity.getSenderCurrentLatitude(), rideEntity.getSenderCurrentLongitude(),locationDTO.getLatitude(),locationDTO.getLongitude());
        String distanceText = distanceMatrixService.getDistanceTextFromDistanceMatrixResponse(distanceMatrixResponse);
        String durationText = distanceMatrixService.getDurationTextFromDistanceMatrixResponse(distanceMatrixResponse);
        OnRideRefresherDTO onRideRefresherDTO = new OnRideRefresherDTO(rideEntity.getRideStatus(),new LocationDTO(null,rideEntity.getSenderCurrentLatitude(), rideEntity.getSenderCurrentLongitude()),locationDTO,distanceText,durationText);
        return ResponseEntity.ok().body(onRideRefresherDTO);
    }
}
