package com.mycollegepass.mycollegepass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.mycollegepass.mycollegepass.adapters.FeedListAdapter;
import com.mycollegepass.mycollegepass.model.FeedItem;
import com.mycollegepass.mycollegepass.model.Preferences;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    //TODO move all database functions to Database Operations

    private String TAG = "MainActivity";
    private ListView listView;
    private FeedListAdapter mlistAdapter;
    private ArrayList<FeedItem> feedItems;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private Preferences mPrefs;
    private DatabaseReference myRef;
    private ActionMenuView amvMenu;
    private View searchLayout;
    private View normalToolbar;
    private ImageButton backButton;
    private ImageView search_btn;
    private ImageView search_btn_SL;
    private EditText query_ET;
    private CheckBox discount_filter,views_filter ;
    private static Location currLocation;
    private enum filterType {discount ,rank, views}
    private boolean orderedByDiscountDescOrder = false, orderedByRankDescOrder = false,
            orderedByViewsDescOrder = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    private TextView verify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*** Customize app bar***/
        Toolbar toolbar = findViewById(R.id.main_toolbar);

        amvMenu =  toolbar.findViewById(R.id.amvMenu);
        amvMenu.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onOptionsItemSelected(menuItem);
            }
        });
        /***Manage Toolbar***/
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        //hide search toolbar
        normalToolbar = findViewById(R.id.normal_toolbar);
        searchLayout = findViewById(R.id.search_layout);
        searchLayout.setVisibility(View.GONE);



        /***necessary initializations***/
        mPrefs = new Preferences(this);
        feedItems = new ArrayList<FeedItem>();
        //writeQRCodesToDB();
        //writeTermsAndConditionsToDB();
       // writeInfo();


        /***app behaviour management***/
        //delete verify warning if user verified
        if (mPrefs.getUserLoggedInStatus()) {
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user.isEmailVerified()) {
                deleteVerifStatusLayout();
            }
        }

        //start open page if not logged in
        if (!mPrefs.getUserLoggedInStatus()){
            Intent intent = new Intent(MainActivity.this, OpenActivity.class);
            startActivity(intent);
            finish();
        }

        /***Database-related operations***/
        // Create firebase storage reference
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        mAuth = FirebaseAuth.getInstance();

        /*** fill feedItems with feed and show in UI***/
        //get reference to listview to contain feed
        listView = findViewById(R.id.feed_list_view);
        // Wire listener to listView
        listView.setOnItemClickListener(mListener);
        populateFeedWithDataBase();


        /***get reference to UI elements***/
        verify = findViewById(R.id.verify);
        search_btn = findViewById(R.id.search_NL);
        backButton = findViewById(R.id.backButton_SL);
        discount_filter = findViewById(R.id.discount_filter);
        views_filter = findViewById(R.id.views_filter);
        query_ET = findViewById(R.id.query_SL);
        search_btn_SL = findViewById(R.id.search_SL);




        /***Set up onclick listeners***/
        //verify account
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyEmail();
            }
        });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide normal tool bar layout and show search layout
                normalToolbar.setVisibility(View.GONE);
                amvMenu.setVisibility(View.GONE);
                searchLayout.setVisibility(View.VISIBLE);

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide search and show layout normal layout
                searchLayout.setVisibility(View.GONE);
                amvMenu.setVisibility(View.VISIBLE);
                normalToolbar.setVisibility(View.VISIBLE);
            }
        });
        search_btn_SL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFeedFor(query_ET.getText().toString());
            }
        });
        discount_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterFeedBy(filterType.discount);
            }
        });

        views_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterFeedBy(filterType.views);
            }
        });

        /***Location : get users distance to businesses***/
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermissions();
    }

    /***feed on click listener***/
    AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {

            FeedItem feedItem = feedItems.get(position);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, amvMenu.getMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.settings:
                Toast.makeText(MainActivity.this,
                        "No Settings yet \n Donut worry :)",
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


   public void deleteVerifStatusLayout(){
       findViewById(R.id.unverified_layout).setVisibility(View.GONE);
   }

   public void showFeed(){
       mlistAdapter = new FeedListAdapter(this, R.layout.feed_item, feedItems);
       listView.setAdapter(mlistAdapter);
   }

   private void populateFeedWithDataBase(){
       DatabaseReference myRef = database.getReference("Discounts");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get feed object and use the values to update the UI
                GenericTypeIndicator<ArrayList<FeedItem>> t = new
                        GenericTypeIndicator<ArrayList<FeedItem>>() {};

                feedItems = dataSnapshot.getValue(t);
                //remove first null item
                feedItems.remove(0);

                //set IDs of FeedItemIDs;
                int i = 0;
                for (DataSnapshot dataS : dataSnapshot.getChildren()){
                    feedItems.get(i).setDiscountID(dataS.getKey());
                    i++;
                    }
                showFeed();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }


    private void setUpDBStructure(){
        int i = 0;
        for (FeedItem feedItem: feedItems) {
            //set up ID
            DatabaseReference discountRef = database.getReference("Discounts").
                    child(String.valueOf(i+1));

            //set name
            discountRef.child("business_name").setValue(feedItem.getBusiness_name());

            //set description
            discountRef.child("description").setValue(feedItem.getDescription());

            //set redemption_alert
            discountRef.child("redemption_alert").setValue(feedItem.getDiscount());

            //set business_name

            //set business_ID
            i++;
        }


        //set imageURLs
        myRef = database.getReference("Discounts").child("1").child("image_url");
        myRef.setValue("https://firebasestorage.googleapis.com/v0/b/mycollegepass-fa5e1.appspot.com/o/images%2F2131165305?alt=media&token=38d5b6ee-91bc-4041-b06b-fbaaea92ec8c");

        myRef = database.getReference("Discounts").child("2").child("image_url");
        myRef.setValue("https://firebasestorage.googleapis.com/v0/b/mycollegepass-fa5e1.appspot.com/o/images%2F2131165327?alt=media&token=1745e80d-3166-419b-8e1b-bc2897338275");

        myRef = database.getReference("Discounts").child("3").child("image_url");
        myRef.setValue("https://firebasestorage.googleapis.com/v0/b/mycollegepass-fa5e1.appspot.com/o/images%2F2131165277?alt=media&token=09dea5f0-a6ba-4090-a42e-f085a8fb8643");

        Toast.makeText(MainActivity.this, "DataBase Update successful",
                Toast.LENGTH_LONG).show();

        //set locations



    }

   private void writeLocationToDB(){
       Location[] locations = new Location[3];
       locations[0] = new Location("");
       locations[0].setLongitude(-72.2922034);
       locations[0].setLatitude(43.7009775);
       locations[1] = new Location("");
       locations[1].setLongitude(-72.2913192);
       locations[1].setLatitude(43.7019352);
       locations[2] = new Location("");
       locations[2].setLongitude(-72.2923237);
       locations[2].setLatitude(43.7022387);


        for( int j = 0; j<3;j++ ) {
            myRef = database.getReference("Discounts").child("" + (j+1)).child("longitude");
            myRef.setValue(locations[j].getLongitude());
            myRef = database.getReference("Discounts").child("" + (j+1)).child("latitude");
            myRef.setValue(locations[j].getLatitude());
        }
   }

   public void writeQRCodesToDB(){
        String[] codes = new String[]{
                "14FL785K",
                "56ZI544M",
                "21JL629K"};

       for( int j = 0; j<3;j++ ) {
           myRef = database.getReference("Discounts").child("" + (j + 1)).child("redeemCode");
           myRef.setValue(codes[j]);
       }
   }

    public void writeTermsAndConditionsToDB(){
        String[] terms = new String[]{
                "Discount only applies to meals on our regular menu",
                "Applies to all apparel",
                "Can be applied to only recommended textbooks. Notebooks and stationery are excluded"};
        for( int j = 0; j<3;j++ ) {
            myRef = database.getReference("Discounts").child("" + (j + 1)).child("terms");
            myRef.setValue(terms[j]);
        }
    }

    public void writeInfo(){

        String[] types = new String[]{
                "Restaurant",
                "Clothing",
                "Books and stationery"};
        //Type
        for( int j = 0; j<3;j++ ) {
            myRef = database.getReference("Discounts").child("" + (j + 1)).child("type");
            myRef.setValue(types[j]);
        }
        String[] websites = new String[]{
                "www.mollysrestaurant.com",
                "stores.jcrew.com/en/hanover",
                "http://www.wheelockbooks.com/"};
        //websites
        for( int j = 0; j<3;j++ ) {
            myRef = database.getReference("Discounts").child("" + (j + 1)).child("website");
            myRef.setValue(websites[j]);
        }


        //rank
        long[] ranks = new long[]{
                2,1,3};
        for( int j = 0; j<3;j++ ) {
            myRef = database.getReference("Discounts").child("" + (j + 1)).child("rank");
            myRef.setValue(ranks[j]);
        }


    }


    private void imageTasks(){
        //get image from drawables
        Uri file = Uri.parse("android.resource://"+R.class.getPackage().getName()+"/"
                    +R.drawable.books);

        StorageReference imageRef = storageRef.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(file);

        // Register observers to listen for when the upload is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(MainActivity.this, "failed to upload image",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Toast.makeText(MainActivity.this, "Upload successful"+taskSnapshot.getMetadata(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void verifyEmail() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.reload();  //refresh status


        //check current status
        if(user.isEmailVerified()){
            Toast.makeText(MainActivity.this,
                    "You are now verified! :)",
                    Toast.LENGTH_SHORT).show();
                deleteVerifStatusLayout();
        }else {

            //else send verification email
            user.sendEmailVerification()
                    .addOnCompleteListener(this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,
                                        "Verification email sent to" + user.getEmail(),
                                        Toast.LENGTH_SHORT).show();
                                //update Database user as verified

                            } else {
                                Log.e(TAG, "sendEmailVerification", task.getException());
                                Toast.makeText(MainActivity.this,
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    public void filterFeedBy(final filterType filter){
        //TODO use limitToLast() to limit returned results to be
        //reversed in batches
        String filterString;
        if (filter == filter.views)
           filterString = "clickCount";
        else
          filterString = filter.toString();

        Query topDiscountQuery = database.getReference().child("Discounts").
                orderByChild(filterString);
        topDiscountQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get feed object and use the values to update the UI
                GenericTypeIndicator<ArrayList<FeedItem>> t = new
                        GenericTypeIndicator<ArrayList<FeedItem>>() {
                        };

                ArrayList<FeedItem> orderedFeedItems = dataSnapshot.getValue(t);
                //remove first null item
                orderedFeedItems.remove(0);

                //set IDs of FeedItemIDs;
                int i = 0;
                for (DataSnapshot dataS : dataSnapshot.getChildren()) {
                    orderedFeedItems.get(i).setDiscountID(dataS.getKey());
                    i++;
                }

                //reverse feedItems to present feed items in descending order,
                //if already in descending order, present in ascending order
                switch (filter) {
                    case discount:
                        if (orderedByDiscountDescOrder)
                            orderedByDiscountDescOrder = false;
                        else {
                            Collections.reverse(orderedFeedItems);
                            orderedByDiscountDescOrder = true;
                        }
                        break;
                    case rank:
                        if (orderedByRankDescOrder)
                            orderedByRankDescOrder = false;
                        else {
                            Collections.reverse(orderedFeedItems);
                            orderedByRankDescOrder = true;
                        }
                        break;
                    case views:
                        if (orderedByViewsDescOrder)
                            orderedByViewsDescOrder = false;
                        else {
                            Collections.reverse(orderedFeedItems);
                            orderedByViewsDescOrder = true;
                        }
                        break;
                }
                feedItems = orderedFeedItems;
                showFeed();
            }

            @Override
            public void onCancelled(DatabaseError databaseError){
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    public void searchFeedFor(String query){
        Query topDiscountQuery = database.getReference().child("Discounts").
                orderByChild("business_name").equalTo(query.trim());
        topDiscountQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get feed object and use the values to update the UI
              //  GenericTypeIndicator<ArrayList<FeedItem>> t = new
                //        GenericTypeIndicator<ArrayList<FeedItem>>() {};

                ArrayList<FeedItem> matchedFeedItems = new ArrayList<FeedItem>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    matchedFeedItems.add(child.getValue(FeedItem.class));
                }

                if (matchedFeedItems.size() != 0) {

                    //set IDs of FeedItemIDs;
                    int i = 0;
                    for (DataSnapshot dataS : dataSnapshot.getChildren()) {
                        matchedFeedItems.get(i).setDiscountID(dataS.getKey());
                        i++;
                    }
                    feedItems = matchedFeedItems;
                    showFeed();
                }else{
                    //show no results message
                    Toast.makeText(MainActivity.this, "Sorry, couldn't find anything. Alter query",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void checkPermissions() {

        if(Build.VERSION.SDK_INT < 23)
            return;
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }else{
            //get user's last location
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location.
                    if (location != null) {
                        currLocation = location;
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //get user's last location
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location.
                        if (location != null) {
                            currLocation = location;
                        }
                    }
                });
            }
        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is important for the app.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                            }
                        }
                    });
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
            }
        }
    }

   public static Location getCurrentLocation(){
        if (currLocation != null)
            return currLocation;
        else {
            Location dartmouthLoc = new Location("");
            dartmouthLoc.setLongitude(-72.2886799);
            dartmouthLoc.setLatitude(43.7044465);
            return dartmouthLoc;
        }
   }


}
