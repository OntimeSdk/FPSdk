package com.ontimesdk;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class FpInstance {
    private FpSdk mFpSdk = null;
    private Activity mContext;
    private FpSdk.IFpSdk mfpSdk;

    private static FpInstance instance;

    public static FpInstance getInstance() {
        if (null == instance) {
            instance = new FpInstance();
        }
        return instance;
    }

    public void SetContext(Activity activityContext, FpSdk.IFpSdk fpSdk) {
        mfpSdk = fpSdk;
        mContext = activityContext;
        if (mFpSdk == null) {
            mFpSdk = new FpSdk(mContext, mfpSdk);
            intiFpSet();
        } else {
            mFpSdk.SetContext(mContext, mfpSdk);
        }
    }

    public FpSdk GetFpSdk() {
        if (mFpSdk == null) {
            mFpSdk = new FpSdk(mContext, mfpSdk);
            intiFpSet();
        }
        return mFpSdk;
    }


    /////////////////////// AZLAAN CODED to ha/////////////////////////////
    public void intiFpSet() {
        mStartSdkHandler = new Handler();
        mStartSdkunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("CancelIssue", String.valueOf("isClosed && mFpSdk = " + isClosed + " && " + mFpSdk != "null"));
                if (isClosed && mFpSdk != null) {
                    mFpSdk.cancel();
                    mFpSdk.onResume();
                    mFpSdk.openSdk();
                } else {
                    mfpSdk.onDeviceOpen();
                }

            }
        };

        mStopSdkHandler = new Handler();
        mStopSdkunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("CancelIssue", String.valueOf("isOpened && mFpSdk = " + isOpened + " && " + mFpSdk != "null"));
                if (isOpened && mFpSdk != null) {
                    mFpSdk.cancel();
                    mFpSdk.onPause();
                    mFpSdk.closeSdk();
                }
            }
        };
    }

    private Handler mStartSdkHandler;
    private Runnable mStartSdkunnable;

    private Handler mStopSdkHandler;
    private Runnable mStopSdkunnable;

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    private boolean isClosed = true;
    private boolean isOpened = false;

    public void startSdkHandler() {
        if (mStopSdkHandler != null & mStopSdkHandler != null) {
            mStopSdkHandler.removeCallbacks(mStopSdkunnable);
        }

        mStartSdkHandler.postDelayed(mStartSdkunnable, 1000);
    }

    public void stopSdkHandler() {
        if (mStartSdkHandler != null & mStartSdkHandler != null) {
            mStartSdkHandler.removeCallbacks(mStartSdkunnable);
        }
        mStopSdkHandler.postDelayed(mStopSdkunnable, 1000);
    }

    public void restartSdk() {
        GetFpSdk().cancel();

    }
}
