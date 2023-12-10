package beep.engine.location;

import java.util.Objects;

public class LocationImpl implements Location{
    private static final double RADIUS_KM = 1;
    private static final int EARTH_RADIUS_KM = 6371;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private Float bearing;

    public LocationImpl(String locationName, Double latitude, Double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    @Override
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public Double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public Float getBearing() {
        return bearing;
    }

    @Override
    public void setBearing(Float bearing) {
        this.bearing = bearing;
    }

    @Override
    public boolean isLocationWithinRadius(Location otherLocation){
        double distance = calculateDistance(this.latitude, this.longitude, otherLocation.getLatitude(), otherLocation.getLongitude());
        return distance <= RADIUS_KM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationImpl location = (LocationImpl) o;
        return latitude.equals(location.latitude) && longitude.equals(location.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
