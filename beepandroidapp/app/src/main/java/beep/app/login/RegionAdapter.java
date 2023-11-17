package beep.app.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import beep.app.R;

public class RegionAdapter extends ArrayAdapter<RegionItem> {

    public RegionAdapter(@NonNull Context context, @NonNull List<RegionItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_region_item, parent, false);
        }

        ImageView imageViewFlag = convertView.findViewById(R.id.imageViewFlag);
        TextView textViewPrefix = convertView.findViewById(R.id.textViewPrefix);

        RegionItem currentItem = getItem(position);

        if (currentItem != null) {
            imageViewFlag.setImageResource(currentItem.getFlagResourceId());
            textViewPrefix.setText(currentItem.getPrefix());
        }

        return convertView;
    }
}
