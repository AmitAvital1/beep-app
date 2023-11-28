package beep.app;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static beep.app.util.Constants.FETCH_ON_RIDE;
import static beep.app.util.Constants.LOGIN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
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
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import beep.app.util.http.HttpClientUtil;
import fetch.UserOnRideDTO;
import login.UserDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean firstFetch = true;
    private boolean recenterOnLocationChange = false;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CameraPosition prevCameraPosition;
    private static final long FETCH_INTERVAL = 4000; // 4 seconds
    private Handler handler = new Handler(Looper.myLooper());
    private Runnable dataFetchRunnable;
    private Dialog dialog = null;

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
                    startDataFetch();
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

        dataFetchRunnable = new Runnable() {
            @Override
            public void run() {
                // Fetch data from the server
                fetchDataFromServer();

                // Schedule the next fetch after the interval
                handler.postDelayed(this, FETCH_INTERVAL);
            }
        };

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
            startDataFetch();
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

    private void startDataFetch() {
        handler.postDelayed(dataFetchRunnable, FETCH_INTERVAL);
    }
    private void fetchDataFromServer() {
        String finalUrl = HttpUrl
                .parse(FETCH_ON_RIDE)
                .newBuilder()
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .get()
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        UserOnRideDTO userOnRideDTO = gson.fromJson(responseBody, UserOnRideDTO.class);
                        if(userOnRideDTO.isOnRide()){
                            if(userOnRideDTO.isSender()){
                                showDialog("Invitation sent to " + userOnRideDTO.getRideDTO().getUserReceiver().getFirstName()
                                        + userOnRideDTO.getRideDTO().getUserReceiver().getLastName() + "Waiting for approval", true, userOnRideDTO);
                            }else{
                                if(dialog == null)
                                    showDialog("You have new invitation from " + userOnRideDTO.getRideDTO().getUserSender().getFirstName()
                                         + userOnRideDTO.getRideDTO().getUserSender().getLastName(), false, userOnRideDTO);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(dataFetchRunnable);
        if(dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();

    }
    private void showDialog(String text, boolean isSender,UserOnRideDTO userOnRideDTO ) {

        dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_invitation_dialog);

        Button acceptButton = dialog.findViewById(R.id.acceptButton);
        Button rejectButton = dialog.findViewById(R.id.rejectButton);
        TextView textView = dialog.findViewById(R.id.invitation_text);

        textView.setText(text);

        if(isSender) {
            acceptButton.setVisibility(View.INVISIBLE);
            rejectButton.setVisibility(View.INVISIBLE);
        }
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(requireContext(),"Edit is Clicked",Toast.LENGTH_SHORT).show();

            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                Toast.makeText(requireContext(),"Share is Clicked",Toast.LENGTH_SHORT).show();

            }
        });



        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
}