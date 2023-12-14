package beep.app;

import static beep.app.util.Constants.ACCEPT_INVITATION;
import static beep.app.util.Constants.CANCEL_INVITATION;
import static beep.app.util.Constants.CANCEL_RIDE;
import static beep.app.util.Constants.FETCH_ON_RIDE;
import static beep.app.util.Constants.RECEIVER_ON_RIDE;
import static beep.app.util.Constants.REJECT_INVITATION;
import static beep.app.util.Constants.SENDER_ON_RIDE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.util.List;
import java.util.Locale;

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
    private static final long FETCH_IF_HAS_RIDE = 4000; // 4 seconds
    private static final long FETCH_IN_RIDE = 300; // 300ms
    private Handler handler = new Handler(Looper.myLooper());
    private Runnable onRideDataFetch;
    private Dialog dialog = null;
    private Dialog dialogComplete = null;
    private Marker otherMarker = null;
    private boolean showOtherFocusOnMap = false;
    private boolean focusThisFragment = true;
    private ImageButton focusOnOtherButton;
    private LocationCallback currentLocationCallback;


    private GoogleMap mMap;

    private Runnable dataFetchRunnable = new Runnable() {
        @Override
        public void run() {
            // Fetch data from the server
            fetchDataFromServer();

            // Schedule the next fetch after the interval
            handler.postDelayed(this, FETCH_IF_HAS_RIDE);
        }
    };

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
                    startDataFetch(dataFetchRunnable,FETCH_IF_HAS_RIDE);
                } else {
                    // Permission is denied, handle it
                }
            });
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        supportMapFragment.getMapAsync(this);
        firstFetch = true;
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
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            startDataFetch(dataFetchRunnable, FETCH_IF_HAS_RIDE);
        } else {
            // Request permissions
            requestPermissionLauncher.launch(permissions);
        }
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        mMap.setMyLocationEnabled(true);
        currentLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = currentLocation;
                currentLocation = locationResult.getLastLocation();

                if (currentLocation != null && lastLocation != null) {
                    lastBearing = lastLocation.bearingTo(currentLocation);
                    System.out.println(lastBearing);
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
        };
        fusedLocationProviderClient.requestLocationUpdates(
                new LocationRequest()
                        .setInterval(1000) // Update every second
                        .setFastestInterval(500), // Update as often as possible
                currentLocationCallback
                , Looper.myLooper()
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

    private void startDataFetch(Runnable runnable, long fetchTime) {
        handler.postDelayed(runnable, fetchTime);
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
                        ((MainScreenActivity) getActivity()).setCurrentRidesNum(userOnRideDTO.getCurrentRides());
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
                handler.postDelayed(this, FETCH_IN_RIDE);
            }
        };
        startDataFetch(onRideDataFetch,FETCH_IN_RIDE);
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
                    + userOnRideDTO.getRideDTO().getUserReceiver().getLastName() + " Waiting for approval", true, userOnRideDTO);
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
        fusedLocationProviderClient.removeLocationUpdates(currentLocationCallback);
      if(dialog != null)
          dialog.dismiss();
      if(dialogComplete != null)
          dialogComplete.dismiss();
       super.onDestroy();
    }


    private void showInvitationRideDialog(String text, boolean isSender, UserOnRideDTO userOnRideDTO) {
        removeCompleteDialog(dialogComplete);
        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_invitation_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        Button acceptButton = dialog.findViewById(R.id.acceptButton);
        Button rejectButton = dialog.findViewById(R.id.rejectButton);
        TextView textView = dialog.findViewById(R.id.invitation_text);
        ImageButton cancelInvitation = dialog.findViewById(R.id.cancelInvitation);

        textView.setText(text);

        if (isSender) {
            acceptButton.setVisibility(View.INVISIBLE);
            rejectButton.setVisibility(View.INVISIBLE);
        }else{
            cancelInvitation.setVisibility(View.INVISIBLE);
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
                acceptButton.setEnabled(false);
                acceptInvitation(userOnRideDTO,acceptButton);

            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectButton.setEnabled(false);
                rejectInvitation(userOnRideDTO,rejectButton);
                removeDialog(dialog);
                if(otherMarker != null)
                    otherMarker.remove();
                showOtherFocusOnMap = false;
            }
        });
        cancelInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelInvitation.setEnabled(false);
                cancelInvitation(userOnRideDTO,cancelInvitation);
                removeDialog(dialog);
                if(otherMarker != null)
                    otherMarker.remove();
                showOtherFocusOnMap = false;
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

    private void acceptInvitation(UserOnRideDTO userOnRideDTO, Button acceptButton) {
        String finalUrl = HttpUrl
                .parse(ACCEPT_INVITATION + userOnRideDTO.getRideDTO().getInvitationID())
                .newBuilder()
                .build()
                .toString();

        Gson gson = new Gson();
        String json = gson.toJson(new LocationDTO(getShortAddress(requireContext(),currentLocation), getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude(),lastBearing));
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();


        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handler.post(() -> acceptButton.setEnabled(true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() == 408){
                    //Request timeout - canceled by the sender.
                }else{
                    showOtherFocusOnMap = true;
                }
                handler.post(() -> acceptButton.setEnabled(true));
            }
        });
    }
    private void rejectInvitation(UserOnRideDTO userOnRideDTO, Button rejectButton) {
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
                handler.post(() -> rejectButton.setEnabled(true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() == 408){
                    //Request timeout - canceled by the sender.
                }else{

                }
                handler.post(() -> rejectButton.setEnabled(true));
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
                            handler.removeCallbacks(onRideDataFetch);
                                createRideCompleteDialog(rideRefresherDTO,sender,userOnRideDTO,false);
                            startDataFetch(dataFetchRunnable,FETCH_IF_HAS_RIDE);
                        }
                        else if(rideRefresherDTO.getRideStatus().equals("CANCELED")){
                            handler.removeCallbacks(onRideDataFetch);
                            createRideCompleteDialog(rideRefresherDTO,sender,userOnRideDTO,true);
                            startDataFetch(dataFetchRunnable,FETCH_IF_HAS_RIDE);
                        }
                    }
                });
            }
        });
    }

    private void createRideCompleteDialog(OnRideRefresherDTO rideRefresherDTO, boolean sender, UserOnRideDTO userOnRideDTO, boolean isCanceled) {
        removeDialog(dialog);
        dialogComplete = new Dialog(requireActivity());
        dialogComplete.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogComplete.setContentView(R.layout.bottom_complete_ride_dialog);
        dialogComplete.setCanceledOnTouchOutside(false);
        dialogComplete.setCancelable(false);

        Button okButton = dialogComplete.findViewById(R.id.okButton);
        TextView textView = dialogComplete.findViewById(R.id.complete_text);
        if(!isCanceled) {
            if (!sender)
                textView.setText(userOnRideDTO.getRideDTO().getUserSender().getFirstName() + " " + userOnRideDTO.getRideDTO().getUserSender().getLastName() + " is arrived. Beep Completed");
            else
                textView.setText("You arrived to " + userOnRideDTO.getRideDTO().getUserReceiver().getFirstName() + " " + userOnRideDTO.getRideDTO().getUserReceiver().getLastName() + ". Beep Completed");
        }else{
            textView.setText("Ride have been canceled and stopped");
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCompleteDialog(dialogComplete);
                if(otherMarker != null)
                    otherMarker.remove();
                showOtherFocusOnMap = false;
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
        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.user, getContext().getTheme());
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
        GoogleMap.CancelableCallback cancelableCallback =  new GoogleMap.CancelableCallback() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onFinish() {
                prevCameraPosition = mMap.getCameraPosition();
            }
        };
        if(!sender){
            otherMarker.setPosition(senderLatLng);
            otherMarker.setRotation(rideRefresherDTO.getSenderCurrentLocation().getBearing());
            if(showOtherFocusOnMap && prevCameraPosition.equals(mMap.getCameraPosition())) {
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(senderLatLng, 30));
                //prevCameraPosition = mMap.getCameraPosition();
                mMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(senderLatLng).zoom(17).bearing(0).build())
                        ,cancelableCallback);
            }
        }
        else {
            otherMarker.setPosition(recieverLatLng);
            otherMarker.setRotation(rideRefresherDTO.getReceiverCurrentLocation().getBearing());
            if(showOtherFocusOnMap && prevCameraPosition.equals(mMap.getCameraPosition())) {
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(recieverLatLng, 30));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(recieverLatLng, 30),cancelableCallback);
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
        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_on_ride_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        focusOnOtherButton = dialog.findViewById(R.id.showOtherIcon);
        ImageButton cancelRideButton = dialog.findViewById(R.id.cancelRide);

        if(sender)
            focusOnOtherButton.setImageResource(R.drawable.user);

        focusOnOtherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOtherFocusOnMap = true;
                prevCameraPosition = mMap.getCameraPosition();
            }
        });

        cancelRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRideButton.setEnabled(true);
                cancelRide(onRideDTO,cancelRideButton);
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

    private void cancelRide(UserOnRideDTO onRideDTO, ImageButton cancelRideButton) {
        String finalUrl = HttpUrl
                .parse(CANCEL_RIDE + onRideDTO.getRideDTO().getRideID())
                .newBuilder()
                .build()
                .toString();
        Gson gson = new Gson();
        RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handler.post(() -> cancelRideButton.setEnabled(true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                handler.post(() -> cancelRideButton.setEnabled(true));
            }
        });
    }
    private void cancelInvitation(UserOnRideDTO onRideDTO, ImageButton cancelInvitation) {

        String finalUrl = HttpUrl
                .parse(CANCEL_INVITATION + onRideDTO.getRideDTO().getInvitationID())
                .newBuilder()
                .build()
                .toString();
        Gson gson = new Gson();
        RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handler.post(() -> cancelInvitation.setEnabled(true));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                handler.post(() -> cancelInvitation.setEnabled(true));
            }
        });
    }
    public String getShortAddress(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String shortAddress = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);

                if (address.getThoroughfare() != null) {
                    shortAddress += address.getThoroughfare() + ", ";
                }
                if (address.getLocality() != null) {
                    shortAddress += address.getLocality();
                }
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting address", e);
        }

        return shortAddress;
    }


}