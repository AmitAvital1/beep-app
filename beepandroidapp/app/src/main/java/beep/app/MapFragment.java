package beep.app;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean firstFetch = true;
    private boolean recenterOnLocationChange = false;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CameraPosition prevCameraPosition;


    private GoogleMap mMap;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                boolean allPermissionsGranted = true;
                for (Boolean granted : isGranted.values()) {
                    if (!granted) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    getLastLocation();
                    System.out.println("!!!!!!!!!!!!!!!Here!!!!!!!!!!!!!!!!!");
                } else {
                    // Permission is denied, handle it
                }
            });



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        // FrameLayout mapContainer = rootView.findViewById(R.id.mapContainer);
       // mapContainer.addView(super.onCreateView(inflater, container, savedInstanceState));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        supportMapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 200, 0, 0);
        requestPermissions();
    }
    private void requestPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            // Request permissions
            requestPermissionLauncher.launch(permissions);
        }
    }
    @SuppressLint("MissingPermission")
    public void getLastLocation(){
        mMap.setMyLocationEnabled(true);
        fusedLocationProviderClient.requestLocationUpdates(
                new LocationRequest()
                        .setInterval(1000) // Update every second
                        .setFastestInterval(500), // Update as often as possible
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        currentLocation = locationResult.getLastLocation();
                        if (currentLocation != null) {
                            LatLng myLocation = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                            if(firstFetch) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 50));
                                prevCameraPosition = mMap.getCameraPosition();
                                firstFetch = false;
                            }else{
                                if(prevCameraPosition.equals(mMap.getCameraPosition())){
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 50));
                                    prevCameraPosition = mMap.getCameraPosition();
                                }
                            }
                        }
                    }
                }, Looper.myLooper()
        );
        // Set a click listener for the "My Location" button
        mMap.setOnMyLocationButtonClickListener(() -> {
            if (currentLocation != null) {
                LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 50));
            }
            prevCameraPosition = mMap.getCameraPosition();
            return true; // Indicates that the listener has consumed the event
        });
    }
    public Location getCurrentLocation() {
        return currentLocation;
    }
}