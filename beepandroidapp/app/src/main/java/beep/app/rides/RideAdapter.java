package beep.app.rides;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import beep.app.R;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder>{
    private List<RideItem> rideList;

    public RideAdapter(List<RideItem> rideList) {
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        RideItem ride = rideList.get(position);

        holder.endLocationTextView.setText("Beep target: " + ride.getTargetLocation());
        holder.titleTextView.setText("Beep from " + ride.getSenderName() + " to " + ride.getReceiverName());
        holder.statusRideTextView.setText(ride.getRideStatus() + " " + ride.getEndRideTime());
        holder.startingTimeTextView.setText("Start time: " + ride.getStartRideTime());
        holder.durationTextView.setText("Duration: " + ride.getDuration());

    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView statusRideTextView;
        TextView startingTimeTextView;
        TextView durationTextView;
        TextView endLocationTextView;

        RideViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            statusRideTextView = itemView.findViewById(R.id.statusRide);
            startingTimeTextView = itemView.findViewById(R.id.startingTime);
            durationTextView = itemView.findViewById(R.id.duration);
            endLocationTextView = itemView.findViewById(R.id.endLocation);
        }
    }
    // Method to update the data in the adapter
    public void updateData(List<RideItem> newRideList) {
        rideList = newRideList;
        notifyDataSetChanged();
    }

    // Method to check if the data is empty
    public boolean isEmpty() {
        return rideList.isEmpty();
    }
}
