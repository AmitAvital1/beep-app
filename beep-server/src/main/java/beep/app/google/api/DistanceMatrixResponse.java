package beep.app.google.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistanceMatrixResponse {
    @JsonProperty("rows")
    private List<Route> routes;
}

