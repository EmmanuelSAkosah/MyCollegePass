package com.mycollegepass.mycollegepass.adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.mycollegepass.mycollegepass.BusinessPage;
import com.mycollegepass.mycollegepass.MainActivity;
import com.mycollegepass.mycollegepass.R;
import com.mycollegepass.mycollegepass.model.FeedItem;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.android.volley.toolbox.ImageLoader;
import com.mycollegepass.mycollegepass.utils.Constants;
import com.mycollegepass.mycollegepass.utils.DatabaseOperations;
import com.mycollegepass.mycollegepass.utils.ImageManager;

import de.hdodenhof.circleimageview.CircleImageView;


public class FeedListAdapter extends ArrayAdapter{

    private Context mContext;
    private DatabaseOperations dBOps = new DatabaseOperations();
    private List<FeedItem> feedList = new ArrayList<>();
    ImageLoader imageLoader = ImageManager.getInstance().getImageLoader();

    public FeedListAdapter(Context context,int layoutID, List<FeedItem> list) {
        super(context, layoutID);
        mContext = context;
        feedList = list;
    }

    @Override
    public int getCount() {
        return feedList.size();
    }

    @Override
    public FeedItem getItem(int position) {
        return feedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        AdapterWrapper wrapper = null;


        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.feed_item, parent, false);
            wrapper = new AdapterWrapper(listItem);
            listItem.setTag(wrapper);
        }else
            wrapper = (AdapterWrapper) listItem.getTag();
        if (imageLoader == null)
            imageLoader = ImageManager.getInstance().getImageLoader();
        final NetworkImageView networkImageView = listItem.
                findViewById(R.id.discount_image);
        final CheckBox likeButton = wrapper.getLikeButton();
        final TextView businessNameTextView = wrapper.getBusinessNameTextView();
        final NetworkImageView discountImageView = wrapper.getDiscountImageView();

        final FeedItem feedItem = getItem(position);



        if (feedItem != null) {
            /*** descriptors: name, description image***/
            TextView business_name_tv = listItem.findViewById(R.id.business_name);
            business_name_tv.setText(feedItem.getBusiness_name());

            DecimalFormat df = new DecimalFormat("##.#");
            df.setRoundingMode(RoundingMode.DOWN);
            TextView discount_tv = listItem.findViewById(R.id.discount);
            discount_tv.setText(df.format(feedItem.getDiscount())+"%");

            TextView description_tv = listItem.findViewById(R.id.description);
            description_tv.setText(feedItem.getDescription());

            final String imageUrl = feedItem.getImage_url();
            networkImageView.setImageUrl(imageUrl, imageLoader);


            /***Feed stats: likes, views, distance to***/
            TextView likes_tv = listItem.findViewById(R.id.likes_count);
            TextView distance_to_tv = listItem.findViewById(R.id.distance_to);
            TextView views_tv = listItem.findViewById(R.id.views_count);

            dBOps.setNumOfLikesOfDiscount(feedItem.getDiscountID(),likes_tv, mContext);
            dBOps.setNumOfClicksOfDiscount(feedItem.getDiscountID(),views_tv, mContext);
            //set distance to business location
            Location businessLocation = new Location("");
            businessLocation.setLongitude(feedItem.getLongitude());
            businessLocation.setLatitude(feedItem.getLatitude());
            distance_to_tv.setText(df.format(getDistanceTo(businessLocation))+"mi");

            //colour like button if user has liked feed item
            dBOps.colorIfLiked(feedItem.getDiscountID(),likeButton, mContext);
        }else{
            Log.d("Adapter","feedItem is empty");
        }


        /*** click listeners***/
        //perform 'like database operation' when likebutton is clicked
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //keep original state of button to revert back to if like/unlike op fails
                final boolean previousCheckState;
                if(likeButton.isChecked())
                    previousCheckState = false;
                else
                    previousCheckState = true;

                dBOps.LikeORUnlike(likeButton,feedItem.getDiscountID(),previousCheckState);
            }
        });

        //open business page when business name is clicked
        businessNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext.getApplicationContext(), BusinessPage.class);
                Gson gson = new Gson();
                String feedItemString = gson.toJson(feedItem, FeedItem.class);
                intent.putExtra("feedItem",feedItemString);
                mContext.startActivity(intent);
            }
        });

        //increment count click when feeditem is clicked
        discountImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 dBOps.addClickCount(feedItem.getDiscountID());
                //open redemption_alert code and details
                openDiscountRedemptionAlert(feedItem);
            }
        });
        return listItem ;
    }

    private void openDiscountRedemptionAlert(FeedItem feedItem){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE );
        View customView = inflater.inflate(R.layout.redemption_alert, null);

        /***set discount redemption details ***/
        TextView terms = customView.findViewById(R.id.terms_and_conditions);
        terms.setText(feedItem.getTerms());

        TextView redeemCode = customView.findViewById(R.id.redeem_code);
        redeemCode.setText(feedItem.getRedeemCode());

        builder.setView(customView);
        builder.setTitle("")
                .setCancelable(false)
                .setPositiveButton("Redeem", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //EditText textBox = (EditText) findViewById(R.id.textbox);
                        //doStuff();
                        Toast.makeText(mContext,
                                "Redeemed! Yay!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

         AlertDialog alert = builder.create();
         alert.show();
    }

    public float getDistanceTo(Location businessLocation){
        return (float) Constants.METERS_TO_MILES* MainActivity.getCurrentLocation()
                .distanceTo(businessLocation);
    }
}
