package com.mycollegepass.mycollegepass.adapters;

import android.net.NetworkInfo;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.mycollegepass.mycollegepass.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterWrapper {

    private View base;
    private CheckBox likeButton;
    private TextView businessNameTextview;
    private NetworkImageView discountImageView;

    public AdapterWrapper(View base)
    {
        this.base = base;
    }

    public CheckBox getLikeButton()
    {
        if (likeButton == null)
        {
            likeButton = base.findViewById(R.id.like);
        }
        return (likeButton);
    }

    public TextView getBusinessNameTextView()
    {
        if (businessNameTextview == null)
        {
            businessNameTextview = base.findViewById(R.id.business_name);
        }
        return businessNameTextview;
    }

    public NetworkImageView getDiscountImageView()
    {
        if (discountImageView == null)
        {
            discountImageView = base.findViewById(R.id.discount_image);
        }
        return discountImageView;
    }


}
