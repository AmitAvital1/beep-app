package beep.app;

import static beep.app.util.Constants.FETCH_ON_RIDE;
import static beep.app.util.Constants.FETCH_RIDES;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import beep.app.rides.RideAdapter;
import beep.app.rides.RideItem;
import beep.app.util.http.HttpClientUtil;
import fetch.RideMenuDTO;
import fetch.UserOnRideDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import search.UserPhoneExistDTO;

public class RidesFragment extends Fragment {

    private RecyclerView ridesRecyclerView;
    private RideAdapter rideAdapter;
    private TextView noRidesTextView;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rides,container,false);

        ridesRecyclerView = rootView.findViewById(R.id.ridesRecyclerView);
        noRidesTextView = rootView.findViewById(R.id.noRidesTextView);
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(ridesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        ridesRecyclerView.addItemDecoration(dividerItemDecoration);
        handler = new Handler(Looper.myLooper());

        fetchRides();

        return rootView;
    }

    private void fetchRides() {
        String finalUrl = HttpUrl
                .parse(FETCH_RIDES)
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
                        TypeToken<ArrayList<RideMenuDTO>> typeToken = new TypeToken<ArrayList<RideMenuDTO>>() {};
                        List<RideMenuDTO> rideMenuDTO = gson.fromJson(responseBody, typeToken.getType());
                        List<RideItem> rideList = rideMenuDTO.stream().map(dto -> { return new RideItem(dto); }).collect(Collectors.toList()); // Populate the list with ride data
                        rideAdapter = new RideAdapter(rideList);
                        ridesRecyclerView.setAdapter(rideAdapter);
                        if (rideAdapter.isEmpty()) {
                            noRidesTextView.setVisibility(View.VISIBLE);
                            ridesRecyclerView.setVisibility(View.GONE);
                        } else {
                            noRidesTextView.setVisibility(View.GONE);
                            ridesRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }
}

