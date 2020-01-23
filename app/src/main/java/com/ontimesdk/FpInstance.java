package com.ontimesdk;

import android.app.Activity;
import android.os.Handler;

import com.fgtit.fpcore.FPMatch;

import java.nio.ByteBuffer;

public class FpInstance {
    private FpSdk mFpSdk = null;
    private Activity mContext;
    private FpSdk.IFpSdk iFpCallbacks;

    private static FpInstance instance;

    public static FpInstance getInstance() {
        if (null == instance) {
            instance = new FpInstance();
        }
        return instance;
    }

    public void SetContext(Activity activityContext, FpSdk.IFpSdk fpCallbacks) {
        iFpCallbacks = fpCallbacks;
        mContext = activityContext;
        if (mFpSdk == null) {
            mFpSdk = new FpSdk(mContext, iFpCallbacks);
            intiFpSet();
        } else {
            mFpSdk.setCallbacks(iFpCallbacks);
        }
    }

    public FpSdk GetFpSdk() {
        if (mFpSdk == null) {
            mFpSdk = new FpSdk(mContext, iFpCallbacks);
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
                        iFpCallbacks.onAlreadyOpen();
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

    public void enrollFinger() {
        mFpSdk.generateTemplate4();
    }

    public void scanFinger() {
        mFpSdk.generateTemplate1();
    }

    public boolean matchFinger(byte[] dbFinger1, byte[] dbFinger2, byte[] detectedFinger, int matchScore) {
        byte[] dbFinger;
        if (dbFinger1 != null && dbFinger2 != null) {
            byte[] tempByte = new byte[2048];
            ByteBuffer buff = ByteBuffer.wrap(tempByte);
            buff.put(dbFinger1);
            buff.put(dbFinger2);
            dbFinger = buff.array();
        } else {
            if (dbFinger1 != null) {
                dbFinger = dbFinger1;
            } else {
                dbFinger = dbFinger2;
            }
        }

        if (dbFinger == null || dbFinger.length == 0) {
            return false;
        }
        int n = dbFinger.length / 256;
        int m = detectedFinger.length / 256;
        byte[] tmpEnl = new byte[256];
        byte[] tmpMat = new byte[256];

        for (int j = 0; j < m; j++) {
            System.arraycopy(detectedFinger, j * 256, tmpMat, 0, 256);
            for (int i = 0; i < n; i++) {
                System.arraycopy(dbFinger, i * 256, tmpEnl, 0, 256);
                if (FPMatch.getInstance().MatchTemplate(tmpEnl, tmpMat) >= matchScore) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchTemplateFull(byte[] piEnl, byte[] piMat, int tempMatchScore) {
        if (piEnl == null || piEnl.length == 0) {
            return false;
        }
        int n = piEnl.length / 256;
        int m = piMat.length / 256;
        byte[] tmpEnl = new byte[256];
        byte[] tmpMat = new byte[256];

        for (int j = 0; j < m; j++) {
            System.arraycopy(piMat, j * 256, tmpMat, 0, 256);
            for (int i = 0; i < n; i++) {
                System.arraycopy(piEnl, i * 256, tmpEnl, 0, 256);
                if (FPMatch.getInstance().MatchTemplate(tmpEnl, tmpMat) >= tempMatchScore) {
                    return true;
                }
            }
        }
        return false;
    }
}
