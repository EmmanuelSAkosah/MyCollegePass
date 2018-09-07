package com.mycollegepass.mycollegepass;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.mycollegepass.mycollegepass.model.FeedItem;

public class BusinessPage extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FeedItem feedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_page);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        //get feed/item passed to activity
        Gson gson = new Gson();
        String strObj = getIntent().getStringExtra("feedItem");
        feedItem = gson.fromJson(strObj, FeedItem.class);

        /***get reference to UI elements***/
        TextView businessName_tv = findViewById(R.id.business_name_BP);
        final TextView website = findViewById(R.id.business_website_BP);
        TextView type = findViewById(R.id.business_type_BP);
        TextView rank = findViewById(R.id.business_rank_BP);

        /***set text on UI elements***/
        businessName_tv.setText(feedItem.getBusiness_name());
        type.setText(feedItem.getType());
        website.setText(feedItem.getWebsite());
        rank.setText("#"+feedItem.getRank());

        /***set onclickListerners***/
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = website.getText().toString();
                if (!url.startsWith("http"))
                    url = "http://" + url;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //business coordinates
        LatLng latLng = new LatLng(feedItem.getLatitude(), feedItem.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("BusinessName"));
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                latLng, 18);
        mMap.animateCamera(location);
    }
}

