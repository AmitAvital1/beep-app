package location;

public class LocationDTO {
    private String locationName;
    private Double latitude;
    private Double longitude;
    private Float bearing;

    public LocationDTO() {
    }

    public LocationDTO(String locationName, Double latitude, Double longitude, Float bearing) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getBearing() {
        return bearing;
    }

    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }
}
