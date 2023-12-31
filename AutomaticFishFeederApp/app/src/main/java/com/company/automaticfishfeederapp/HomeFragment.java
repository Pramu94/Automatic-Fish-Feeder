package com.company.automaticfishfeederapp;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String userId,firstName,lastName,profilePicture,phValue,deviceId,temp,waterLevel;
    private TextView txt_fullName,txt_phValue,txt_waterLevel;
    private CircleImageView img_profilePicture;
    private DatabaseReference databaseReference;
    private ProgressBar progressbar_waterLevel;
    private CustomProgressBar progressbar_temperature;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        txt_fullName = (TextView) view.findViewById(R.id.userFullName);
        img_profilePicture = (CircleImageView) view.findViewById(R.id.userProfilePicture);

        txt_phValue = (TextView) view.findViewById(R.id.textViewPH);
        txt_waterLevel = (TextView) view.findViewById(R.id.textViewHomeWaterLevel);
        progressbar_temperature = (CustomProgressBar) view.findViewById(R.id.progressTemperature);
        progressbar_waterLevel = (ProgressBar) view.findViewById(R.id.progressHomeWaterLevel);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("SensorData");

        LoginSession sessionManagement =new LoginSession(getContext());
        HashMap<String, String> user = sessionManagement.readLoginSession();
        deviceId = user.get(LoginSession.KEY_DEVICEID);
        userId = user.get(LoginSession.KEY_USERID);
        firstName = user.get(LoginSession.KEY_FIRSTNAME);
        lastName = user.get(LoginSession.KEY_LASTNAME);
        profilePicture = user.get(LoginSession.KEY_PROFILEPICTURE);

        txt_fullName.setText(firstName+" "+lastName);
        Picasso.get().load(profilePicture).into(img_profilePicture);

        progressbar_waterLevel.setMax(100);
        progressbar_waterLevel.setMin(0);

        ShowSensorData(deviceId);
        return view;

    }

    private void ShowSensorData(String deviceId)
    {
        databaseReference.orderByChild("deviceId").equalTo(deviceId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    for (DataSnapshot ds:dataSnapshot.getChildren()) {

                        waterLevel = ds.child("waterLevel").getValue().toString();
                        temp = ds.child("temp").getValue().toString();
                        phValue = ds.child("phValue").getValue().toString();

                        progressbar_waterLevel.setProgress(Integer.parseInt(waterLevel));
                        txt_waterLevel.setText(waterLevel+"%");

                        progressbar_temperature.setProgress(Integer.parseInt(temp));

                        txt_phValue.setText(phValue);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseErrorUser) {

            }
        });


    }
}