package com.example.kidseat.fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kidseat.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.SimpleFormatter;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String NAME_KEY = "name";
    private static final String ADDRESS_KEY = "address";
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    public static final String DATE_KEY = "date";

    private static final String TAG = "MapFragment";

    private GoogleMap mMap;
    private float zoom;

    private FirebaseFirestore mFirestore;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);

    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private static boolean convertDateAndCompare(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        ParsePosition pos = new ParsePosition(0);
        Date currentTime = Calendar.getInstance().getTime();
        Date _date = dateFormat.parse(date, pos);
        assert _date != null;
        return _date.after(currentTime); // if true show event else do not
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        zoom = 15;

        // Add a markers for each event location
        mFirestore.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                HashMap<String, Double> latlng = (HashMap<String, Double>) document.get("latlng");
                                String name = document.getString(NAME_KEY);
                                String address = document.getString(ADDRESS_KEY);
                                String event_date = document.getString(DATE_KEY);
                                assert latlng != null;
                                Double lat = latlng.get(LATITUDE_KEY);
                                Double lng = latlng.get(LONGITUDE_KEY);
                                LatLng location = new LatLng(lat, lng);

                                if(convertDateAndCompare(event_date)){ // if date is greater than today show event else don't
                                    // Add marker to the map
                                    mMap.addMarker(new MarkerOptions().position(location).title(name).snippet(address));
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                                }
                                else{
                                    Log.d(TAG, "onComplete: Date was before today");
                                }

                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}
