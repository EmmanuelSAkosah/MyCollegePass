package com.mycollegepass.mycollegepass.utils;

import android.app.Application;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
public class ImageManager  extends Application{


        public static final String TAG = ImageManager.class.getSimpleName();

        private RequestQueue mRequestQueue;
        private ImageLoader mImageLoader;

        private static ImageManager mInstance;

        @Override
        public void onCreate() {
            super.onCreate();
            mInstance = this;
        }

        public static synchronized ImageManager getInstance() {
            return mInstance;
        }

        public RequestQueue getRequestQueue() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }

            return mRequestQueue;
        }

        public ImageLoader getImageLoader() {
            getRequestQueue();
            if (mImageLoader == null) {
                mImageLoader = new ImageLoader(this.mRequestQueue,
                        new LruBitmapCache());
            }
            return this.mImageLoader;
        }
}