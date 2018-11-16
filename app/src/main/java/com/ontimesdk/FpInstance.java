package com.ontimesdk;

import android.app.Activity;
import android.os.Handler;

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
                if (mFpSdk != null) {
                    if (!mFpSdk.isSdkOpen()) {
                        mFpSdk.cancel();
                        mFpSdk.onResume();
                        mFpSdk.openSdk();
                    } else {
                        mfpSdk.onAlreadyOpen();
                    }
                } else {
                    mFpSdk = GetFpSdk();
                    mFpSdk.onResume();
                    mFpSdk.openSdk();
                }
                /*if (isClosed && mFpSdk != null) {
                    mFpSdk.cancel();
                    mFpSdk.onResume();
                    mFpSdk.openSdk();
                } else {
                    mfpSdk.onAlreadyOpen();
                }*/

            }
        };

        mStopSdkHandler = new Handler();
        mStopSdkunnable = new Runnable() {
            @Override
            public void run() {

                if (mFpSdk != null) {
                    if (mFpSdk.isSdkOpen()) {
                        mFpSdk.cancel();
                        mFpSdk.onPause();
                        mFpSdk.closeSdk();
                    } else {
                        //mfpSdk.onAlreadyOpen();
                    }
                } else {
                    mFpSdk = GetFpSdk();
                    mFpSdk.onPause();
                    mFpSdk.closeSdk();
                }
               /* if (isOpened && mFpSdk != null) {
                    mFpSdk.cancel();
                    mFpSdk.onPause();
                    mFpSdk.closeSdk();
                }*/
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
