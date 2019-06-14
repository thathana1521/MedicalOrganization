package com.example.medicalorganization.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.medicalorganization.AvailableEvents;
import com.example.medicalorganization.Holders.DoctorHolder;
import com.example.medicalorganization.Models.Doctor;
import com.example.medicalorganization.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;

import static android.app.PendingIntent.getActivity;

public class DoctorAdapter extends FirebaseRecyclerAdapter<Doctor, DoctorHolder> {

    private Context context;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public DoctorAdapter(@NonNull FirebaseRecyclerOptions<Doctor> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull DoctorHolder holder, final int position, final @NonNull Doctor model) {

        holder.setDoctorName(model.Name + " " + model.Surname);

        //setting up the onclick listener for recyclerview
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jump to available events of doctor. passing doctorId to the AvailableEventsActivity
                Intent intent = new Intent(context, AvailableEvents.class);
                String doctorId = getRef(position).getKey();
                intent.putExtra("doctorToken", model.Device_Token);
                intent.putExtra("doctorId", doctorId);
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public DoctorHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_doctor, viewGroup, false);
        DoctorHolder viewHolder = new DoctorHolder(view);
        return viewHolder;
    }
}
