package beep.engine.location;

public interface Location {
    String getLocationName();
    Double getLatitude();
    Double getLongitude();
    void setLatitude(Double latitude);
    void setLongitude(Double longitude);


}
