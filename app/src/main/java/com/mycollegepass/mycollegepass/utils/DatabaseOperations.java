package com.mycollegepass.mycollegepass.utils;


import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mycollegepass.mycollegepass.MainActivity;
import com.mycollegepass.mycollegepass.model.FeedItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class DatabaseOperations {

    String TAG = "DatabaseOperations";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference discountsRef;
    DatabaseReference usersRef;
    DatabaseReference mRef;

    public DatabaseOperations(){

        //get refs to discounts, businesses and users
        discountsRef = database.getReference("Discounts");
        usersRef = database.getReference("Users");
    }

    public void addLike(String discountID, String userID){
        //get reference to the redemption_alert post liked, and add the user to its like list
        DatabaseReference likesRef = discountsRef.child(""+discountID).child("Likes");
        likesRef.push().setValue(userID);

        //add redemption_alert feeditem to user's list of likes
        usersRef.child(""+userID).child("Likes").push().setValue(discountID);
    }

    public void removeLike(final String discountID, final String userID){
        //remove the user from the redemption_alert's like list
        final DatabaseReference likesRef = discountsRef.child(discountID).child("Likes");
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataS: dataSnapshot.getChildren()){
                    if (dataS.getValue().equals(userID)) {
                        //remove user from redemption_alert's like list
                        likesRef.child(dataS.getKey()).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Couldn't check if user exists in redemption_alert's like list",
                        databaseError.toException());
                //toast couldn't connect
            }
        });


        //remove the redemption_alert from user's list of likes
        final DatabaseReference userLikesRef = usersRef.child(userID).child("Likes");
        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataS: dataSnapshot.getChildren()){
                    if (dataS.getValue().equals(discountID)) {
                        //remove redemption_alert from user's like list
                        userLikesRef.child(dataS.getKey()).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Couldn't check if redemption_alert exists in user's like list",
                        databaseError.toException());
                //toast couldn't connect
            }
        });
    }

    public void LikeORUnlike(final CheckBox likeButton, final String DiscountID,
                             final boolean previousCheckState ){

        //check if discountID not in user's likes
        usersRef.child(""+getUserID()).child("Likes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //check if redemption_alert is in user's list
                boolean exists = false;

                for(DataSnapshot dataS: dataSnapshot.getChildren()){
                    if (dataS.getValue().equals(DiscountID)) {
                        exists = true;
                        //remove from user's like list
                        removeLike(DiscountID, getUserID());
                    }
                }
                if (!exists) {
                    addLike(DiscountID, getUserID());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Couldn't check if like exists in user's list",
                        databaseError.toException());
                //revert button to its original state
                likeButton.setChecked(previousCheckState);
                //toast couldn't connect
            }
        });

    }

    public void colorIfLiked(final String discountID, final CheckBox like_icon, final Context context){
        //check if discountID not in user's likes
        usersRef.child(""+getUserID()).child("Likes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //check if redemption_alert is in user's list
                        for(DataSnapshot dataS: dataSnapshot.getChildren()){
                            if (dataS.getValue().equals(discountID)) {
                                //exists = true;
                               like_icon.setChecked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "Couldn't check if like exists in user's list",
                                databaseError.toException());
                        //toast couldn't connect
                    }
                });

    }


    public void setNumOfLikesOfDiscount(String DiscountId, final TextView likes_tv, final Context context){
        //get number of likes of a redemption_alert feeditem with its id
        discountsRef.child(""+DiscountId).child("Likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get list of likes, then its count
                final int numLikes = (int)dataSnapshot.getChildrenCount();
                likes_tv.setText(String.valueOf(numLikes));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    public void setNumOfClicksOfDiscount(String DiscountId, final TextView views_tv, final Context context){
        //get number of likes of a redemption_alert feeditem with its id
        discountsRef.child(""+DiscountId).child("clickCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 long clickCount = (long) dataSnapshot.getValue();
                 views_tv.setText(Long.valueOf(clickCount).toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    public void setLocation(String discountId,final TextView location_tv ){
        //get number of likes of a redemption_alert feeditem with its id
        Location businessLocation = new Location("");
        discountsRef.child(discountId).child("longitude").addValueEventListener(
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               for(DataSnapshot dataS : dataSnapshot.getChildren()) {
                   double lng, lat;
                   if (dataS.getKey().equals("longitude")) {
                       lng = (double) dataS.getValue();
                   }
               }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getLocation:onCancelled", databaseError.toException());
            }
        });

        discountsRef.child(discountId).child("latitude").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataS : dataSnapshot.getChildren()) {
                            double lng, lat;
                            if (dataS.getKey().equals("longitude")) {
                                lng = (double) dataS.getValue();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getLocation:onCancelled", databaseError.toException());
                    }
        });

        /* if (dataS.getKey().equals("latitude")) {
                       lat = (double) dataS.getValue();
                   }

                }

                businessLocation.setLatitude(lat);
                businessLocation.setLongitude(lng);

                DecimalFormat df = new DecimalFormat("##.#");
                df.setRoundingMode(RoundingMode.UP);
                location_tv.setText(df.format(getDistanceTo(businessLocation))+"mi");*/
    }

    public void addClickCount(String DiscountId){
        discountsRef.child(DiscountId).child("clickCount").addListenerForSingleValueEvent(new
                        ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               // get feed and notifydatachanged

                long clickCount =(long)dataSnapshot.getValue();
                dataSnapshot.getRef().setValue(clickCount+1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getLocation:onCancelled", databaseError.toException());
            }
        });
    }

    public String getUserID(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void StoreUserIDInDB(){
        usersRef.push().setValue(getUserID());
    }

}
