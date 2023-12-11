package beep.app;

import static beep.app.util.Constants.ACCEPT_INVITATION;
import static beep.app.util.Constants.FETCH_ON_RIDE;
import static beep.app.util.Constants.RECEIVER_ON_RIDE;
import static beep.app.util.Constants.REJECT_INVITATION;
import static beep.app.util.Constants.SENDER_ON_RIDE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import beep.app.util.http.HttpClientUtil;
import fetch.UserOnRideDTO;
import location.LocationDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ride.OnRideRefresherDTO;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean firstFetch = true;
    private boolean recenterOnLocationChange = false;
    private Location currentLocation;
    private Location lastLocation;
    private float lastBearing = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CameraPosition prevCameraPosition;
    private static final long FETCH_INTERVAL = 4000; // 4 seconds
    private Handler handler = new Handler(Looper.myLooper());
    private Runnable dataFetchRunnable;
    private Runnable onRideDataFetch;
    private Dialog dialog = null;
    private Dialog dialogComplete = null;
    private Marker otherMarker = null;
    private boolean showOtherFocusOnMap = false;
    private ImageButton iconButton;


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
                    startDataFetch(dataFetchRunnable);
                } else {
                    // Permission is denied, handle it
                }
            });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        supportMapFragment.getMapAsync(this);
        firstFetch = true;

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
            startDataFetch(dataFetchRunnable);
        } else {
            // Request permissions
            requestPermissionLauncher.launch(permissions);
        }
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        mMap.setMyLocationEnabled(true);
        fusedLocationProviderClient.requestLocationUpdates(
                new LocationRequest()
                        .setInterval(1000) // Update every second
                        .setFastestInterval(500), // Update as often as possible
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        lastLocation = currentLocation;
                        currentLocation = locationResult.getLastLocation();

                        if(currentLocation != null && lastLocation != null) {
                            lastBearing = lastLocation.bearingTo(currentLocation);
                        }

                        if (currentLocation != null && !showOtherFocusOnMap) {
                            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            if (firstFetch) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 50));
                                prevCameraPosition = mMap.getCameraPosition();
                                firstFetch = false;
                            } else {
                                if (prevCameraPosition.equals(mMap.getCameraPosition())) {
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
            showOtherFocusOnMap = false;
            return true; // Indicates that the listener has consumed the event
        });
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public float getLastBearing() {
        return lastBearing;
    }

    private void startDataFetch(Runnable runnable) {
        handler.postDelayed(runnable, FETCH_INTERVAL);
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
                        if (userOnRideDTO.isOnRide()) {//On ride mean have invitation or on ride
                            if ((dialog == null) && userOnRideDTO.getRideDTO().getInvitationStatus().equals("PENDING"))
                                callInvitationDialog(userOnRideDTO);
                            else if (userOnRideDTO.getRideDTO().getInvitationStatus().equals("ACCEPTED")) {
                                setOnRideUi(userOnRideDTO);
                                if(dialog == null && otherMarker == null)
                                    createSenderMarker(userOnRideDTO.isSender(),userOnRideDTO);
                            }
                        }else{
                            removeDialog(dialog);
                            if(otherMarker != null)
                                otherMarker.remove();
                        }
                    }
                });
            }
        });
    }

    private void setOnRideUi(UserOnRideDTO userOnRideDTO) {
        handler.removeCallbacks(dataFetchRunnable);
        removeDialog(dialog);
        removeCompleteDialog(dialogComplete);
        onRideDataFetch = new Runnable() {
            @Override
            public void run() {
                // Fetch data from the server
                onRideApi(userOnRideDTO, userOnRideDTO.getRideDTO().getRideID(), userOnRideDTO.isSender());

                // Schedule the next fetch after the interval
                handler.postDelayed(this, FETCH_INTERVAL);
            }
        };
        startDataFetch(onRideDataFetch);
    }

    private void removeCompleteDialog(Dialog dialogComplete) {
        if (dialogComplete != null) {
            dialogComplete.dismiss();
            this.dialogComplete = null;
        }
    }

    private void removeDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            this.dialog = null;
        }
    }

    private void callInvitationDialog(UserOnRideDTO userOnRideDTO) {
        if (userOnRideDTO.isSender()) {
            showInvitationRideDialog("Invitation sent to " + userOnRideDTO.getRideDTO().getUserReceiver().getFirstName() + " "
                    + userOnRideDTO.getRideDTO().getUserReceiver().getLastName() + "Waiting for approval", true, userOnRideDTO);
        } else {
            showInvitationRideDialog("You have new invitation from " + userOnRideDTO.getRideDTO().getUserSender().getFirstName() + " "
                    + userOnRideDTO.getRideDTO().getUserSender().getLastName(), false, userOnRideDTO);
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(dataFetchRunnable);
        removeDialog(dialog);
        removeCompleteDialog(dialogComplete);
        otherMarker = null;
        showOtherFocusOnMap = false;
        super.onDestroy();

    }

    private void showInvitationRideDialog(String text, boolean isSender, UserOnRideDTO userOnRideDTO) {
        removeCompleteDialog(dialogComplete);
        dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_invitation_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        Button acceptButton = dialog.findViewById(R.id.acceptButton);
        Button rejectButton = dialog.findViewById(R.id.rejectButton);
        TextView textView = dialog.findViewById(R.id.invitation_text);

        textView.setText(text);

        if (isSender) {
            acceptButton.setVisibility(View.INVISIBLE);
            rejectButton.setVisibility(View.INVISIBLE);
        }

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

        createSenderMarker(isSender, userOnRideDTO);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                acceptInvitation(userOnRideDTO);

            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectInvitation(userOnRideDTO);
                dialog.dismiss();
            }
        });

    }

    private void createSenderMarker(boolean isSender, UserOnRideDTO userOnRideDTO) {
        if(!isSender){
            BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.car, getContext().getTheme());
            Bitmap b = bitmap.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 78, 78, false);

            LatLng senderLatLng = new LatLng(userOnRideDTO.getRideDTO().getSenderLocation().getLatitude(), userOnRideDTO.getRideDTO().getSenderLocation().getLongitude());
            otherMarker = mMap.addMarker(new MarkerOptions().position(senderLatLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).rotation(userOnRideDTO.getRideDTO().getSenderLocation().getBearing()));
            showOtherFocusOnMap = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(senderLatLng, 30));
            prevCameraPosition = mMap.getCameraPosition();
        }
    }

    private void acceptInvitation(UserOnRideDTO userOnRideDTO) {
        String finalUrl = HttpUrl
                .parse(ACCEPT_INVITATION + userOnRideDTO.getRideDTO().getInvitationID())
                .newBuilder()
                .build()
                .toString();

        Gson gson = new Gson();
        String json = gson.toJson(new LocationDTO(null, getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude(),lastBearing));
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();


        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                showOtherFocusOnMap = true;
            }
        });
    }
    private void rejectInvitation(UserOnRideDTO userOnRideDTO) {
        String finalUrl = HttpUrl
                .parse(REJECT_INVITATION + userOnRideDTO.getRideDTO().getInvitationID())
                .newBuilder()
                .build()
                .toString();

        RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();


        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    private void onRideApi(UserOnRideDTO userOnRideDTO, String rideID, boolean sender) {

        if (currentLocation == null || lastLocation == null)
            return;


        String finalUrl;
        if (sender) {
            finalUrl = HttpUrl
                    .parse(SENDER_ON_RIDE + rideID)
                    .newBuilder()
                    .build()
                    .toString();
        } else if (currentLocation.getLongitude() == lastLocation.getLongitude() && currentLocation.getLatitude() == lastLocation.getLatitude()) {
            //Todo Is the receiver and location not changed - not call api only fetch data.
            finalUrl = HttpUrl
                    .parse(RECEIVER_ON_RIDE + rideID)//Todo Change the endpoint
                    .newBuilder()
                    .build()
                    .toString();
        } else {
            finalUrl = HttpUrl
                    .parse(RECEIVER_ON_RIDE + rideID)
                    .newBuilder()
                    .build()
                    .toString();
        }

        Gson gson = new Gson();
        String json = gson.toJson(new LocationDTO(null, getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude(),lastBearing));
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        OnRideRefresherDTO rideRefresherDTO = gson.fromJson(responseBody, OnRideRefresherDTO.class);
                        if (rideRefresherDTO.getRideStatus().equals("ON_RIDE")) {
                            if ((dialog == null)) {
                                createRideDialog(rideRefresherDTO, userOnRideDTO, sender);
                                if (sender)
                                    createReceiverMarker(rideRefresherDTO);
                            } else {
                                updateRideDialog(rideRefresherDTO);
                                updateMapCameraToOtherAndMarker(rideRefresherDTO, sender);
                            }

                        }else if(rideRefresherDTO.getRideStatus().equals("COMPLETED")){
                            createRideCompleteDialog(rideRefresherDTO,sender,userOnRideDTO);
                            handler.removeCallbacks(onRideDataFetch);
                            startDataFetch(dataFetchRunnable);
                        }
                    }
                });
            }
        });
    }

    private void createRideCompleteDialog(OnRideRefresherDTO rideRefresherDTO, boolean sender, UserOnRideDTO userOnRideDTO) {
        removeDialog(dialog);
        dialogComplete = new Dialog(requireContext());
        dialogComplete.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogComplete.setContentView(R.layout.bottom_complete_ride_dialog);
        dialogComplete.setCanceledOnTouchOutside(false);
        dialogComplete.setCancelable(false);

        Button okButton = dialogComplete.findViewById(R.id.okButton);
        TextView textView = dialogComplete.findViewById(R.id.complete_text);
        if(!sender)
            textView.setText(userOnRideDTO.getRideDTO().getUserSender().getFirstName() + " " + userOnRideDTO.getRideDTO().getUserSender().getLastName() + " is arrived. Beep Completed");
        else
            textView.setText("You arrived to " + userOnRideDTO.getRideDTO().getUserReceiver().getFirstName() + " " +userOnRideDTO.getRideDTO().getUserReceiver().getLastName() + ". Beep Completed");
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogComplete.dismiss();
                dialogComplete = null;
                otherMarker.remove();
            }
        });

        dialogComplete.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialogComplete.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogComplete.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogComplete.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialogComplete.getWindow().setGravity(Gravity.BOTTOM);
        dialogComplete.show();
    }

    private void createReceiverMarker(OnRideRefresherDTO rideRefresherDTO) {
        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.blue_dot, getContext().getTheme());
        Bitmap b = bitmap.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 58, 58, false);

        LatLng recieverLatLng = new LatLng(rideRefresherDTO.getReceiverCurrentLocation().getLatitude(), rideRefresherDTO.getReceiverCurrentLocation().getLongitude());
        otherMarker = mMap.addMarker(new MarkerOptions().position(recieverLatLng).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).rotation(rideRefresherDTO.getReceiverCurrentLocation().getBearing()));
        showOtherFocusOnMap = true;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(recieverLatLng, 30));
    }

    private void updateMapCameraToOtherAndMarker(OnRideRefresherDTO rideRefresherDTO, boolean sender) {
        LatLng senderLatLng = new LatLng(rideRefresherDTO.getSenderCurrentLocation().getLatitude(), rideRefresherDTO.getSenderCurrentLocation().getLongitude());
        LatLng recieverLatLng = new LatLng(rideRefresherDTO.getReceiverCurrentLocation().getLatitude(),rideRefresherDTO.getReceiverCurrentLocation().getLongitude());
        if(!sender){
            otherMarker.setPosition(senderLatLng);
            otherMarker.setRotation(rideRefresherDTO.getSenderCurrentLocation().getBearing());
            if(showOtherFocusOnMap && prevCameraPosition.equals(mMap.getCameraPosition())) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(senderLatLng, 30));
                prevCameraPosition = mMap.getCameraPosition();
            }
        }
        else {
            otherMarker.setPosition(recieverLatLng);
            otherMarker.setRotation(rideRefresherDTO.getReceiverCurrentLocation().getBearing());
            if(showOtherFocusOnMap && prevCameraPosition.equals(mMap.getCameraPosition())) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(recieverLatLng, 30));
                prevCameraPosition = mMap.getCameraPosition();
            }
        }
    }

    private void updateRideDialog(OnRideRefresherDTO rideRefresherDTO) {
        TextView duration = dialog.findViewById(R.id.durationText);
        TextView distance = dialog.findViewById(R.id.distanceText);


        duration.setText(rideRefresherDTO.getDurationTime());
        distance.setText(rideRefresherDTO.getDistanceText());
    }

    private void createRideDialog(OnRideRefresherDTO rideRefresherDTO, UserOnRideDTO onRideDTO, boolean sender) {
        dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_on_ride_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        iconButton = dialog.findViewById(R.id.showOtherIcon);

        if(sender)
            iconButton.setImageResource(R.drawable.blue_dot);

        iconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOtherFocusOnMap = true;
                prevCameraPosition = mMap.getCameraPosition();
            }
        });


        TextView title = dialog.findViewById(R.id.ride_text);
        if (sender)
            title.setText("Beep to: " + onRideDTO.getRideDTO().getUserReceiver().getFirstName() + " " + onRideDTO.getRideDTO().getUserReceiver().getLastName());
        else
            title.setText(onRideDTO.getRideDTO().getUserSender().getFirstName() + " " + onRideDTO.getRideDTO().getUserSender().getLastName() + " on the way to you");

        TextView duration = dialog.findViewById(R.id.durationText);
        TextView distance = dialog.findViewById(R.id.distanceText);

        duration.setText(rideRefresherDTO.getDurationTime());
        distance.setText(rideRefresherDTO.getDistanceText());

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

    }


}