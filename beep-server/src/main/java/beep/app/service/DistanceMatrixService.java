package beep.app.service;

import beep.app.google.api.DistanceMatrixResponse;
import beep.app.google.api.Leg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DistanceMatrixService {
    @Value("${google.maps.api.key}")
    private String apiKey;

    public DistanceMatrixResponse getDistanceMatrix(double originLat, double originLng, double destinationLat, double destinationLng) {
        String apiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                "?destinations=" + destinationLat + "," + destinationLng +
                "&origins=" + originLat + "," + originLng +
                "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(apiUrl, DistanceMatrixResponse.class);
    }
    public String getDistanceTextFromDistanceMatrixResponse(DistanceMatrixResponse matrixResponse) {
        if (matrixResponse != null && !matrixResponse.getRoutes().isEmpty()) {
            Leg firstLeg = matrixResponse.getRoutes().get(0).getLegs().get(0);
            return firstLeg.getDistance().getText();
        } else
            return "N/A";
    }
    public String getDurationTextFromDistanceMatrixResponse(DistanceMatrixResponse matrixResponse) {
        if (matrixResponse != null && !matrixResponse.getRoutes().isEmpty()) {
            Leg firstLeg = matrixResponse.getRoutes().get(0).getLegs().get(0);
           return firstLeg.getDuration().getText();
        } else
            return "N/A";
    }
}
