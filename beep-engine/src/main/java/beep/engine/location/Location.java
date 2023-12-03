package beep.engine.location;

public interface Location {
    String getLocationName();
    Double getLatitude();
    Double getLongitude();
    void setLocationName(String locationName);
    void setLatitude(Double latitude);
    void setLongitude(Double longitude);
    boolean isLocationWithinRadius(Location otherLocation);


}
