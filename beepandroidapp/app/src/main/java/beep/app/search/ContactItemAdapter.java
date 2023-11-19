package beep.app.search;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import beep.app.R;

public class ContactItemAdapter extends RecyclerView.Adapter<ContactItemAdapter.ContactViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(ContactItem item);
    }

    private List<ContactItem> contactList;
    private final OnItemClickListener listener;

    public ContactItemAdapter(List<ContactItem> contactList, OnItemClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactItem contact = contactList.get(position);
        holder.contactNameTextView.setText(contact.getContactName());
        if(!contact.isHasUser()){
            holder.contactNameTextView.setTextColor(Color.GRAY);
            holder.contactNameTextView.setTypeface(null, Typeface.NORMAL);
            holder.imageView.setImageResource(R.drawable.invite_icon);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactNameTextView;
        ImageView imageView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameTextView = itemView.findViewById(R.id.contactNameTextView);
            imageView = itemView.findViewById(R.id.invitationImg);
        }
    }
}
