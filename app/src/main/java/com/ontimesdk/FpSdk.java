package com.ontimesdk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.fgtit.device.Constants;
import com.fgtit.device.FPModule;
import com.fgtit.fpcore.FPMatch;

import java.util.ArrayList;


public class FpSdk {


    private FPModule fpm = new FPModule();
    private byte bmpdata[] = new byte[Constants.RESBMP_SIZE];
    private int worktype = 0;

    private IFpSdk mFpSdk;
    private Handler mHandler;
    private Handler delayRestartHandler;
    private Runnable delayRestartRunnable;

    private int RESTART_DELAY = 5000;

    private Intent intent;

    /**
     * Call at onCreate.
     */
    public FpSdk(Activity activityContext, IFpSdk fpSdk) {
        mFpSdk = fpSdk;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        initHandler();
        //mFpSdk.deviceType(String.valueOf(fpm.getDeviceType()));

        fpm.InitMatch();
        fpm.SetContextHandler(activityContext, mHandler);
        fpm.SetTimeOut(Constants.TIMEOUT_LONG);
        fpm.SetLastCheckLift(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activityContext);
        fpm.SetImageOrientation(sp.getInt("ImageOrientation", 0));
        // fpm.Cancle();//This is inbuilt method
        intent = new Intent();
    }

    public void SetContext(Activity activityContext, IFpSdk fpSdk) {
        mFpSdk = fpSdk;
        //fpm.SetContextHandler(activityContext, mHandler);
    }

    /**
     * Call at onResume.
     */
    public void onResume() {
        fpm.ResumeRegister();
    }

    /**
     * Call at onPause.
     */
    public void onPause() {
        try {
            fpm.PauseUnRegister();
            if (delayRestartHandler != null && delayRestartRunnable != null)
                delayRestartHandler.removeCallbacks(delayRestartRunnable);
        } catch (IllegalArgumentException e) {
            mFpSdk.onStatusChange(e.toString());
        }
    }

    /**
     * Always call after {@link #onResume()}
     */
    public void openSdk() {
        fpm.OpenDevice();
    }

    /**
     * Always call after {@link #onPause()}
     */
    public void closeSdk() {
        //fpm.PauseUnRegister();
        fpm.CloseDevice();
    }

    /**
     * Call at onDestroy.
     */
    public void release() {
        mHandler = null;
    }

    public void requestPermission() {
        fpm.requestPermission();
    }

    public void PowerControl(boolean bOn) {
        fpm.PowerControl(bOn);
    }

    /**
     * Call to match fingerprints
     *
     * @param fp1   fingerprint one.
     * @param fp2   fingerprint two.
     * @param score score of matching.
     * @return true or false.
     */
    public boolean matchFP(byte[] fp1, byte[] fp2, int score) {
        return fpm.MatchTemplate(fp1, fp1.length, fp2, fp2.length, score);
    }

    /**
     * Cancels fp reading.
     */
    public void cancel() {
        fpm.Cancle();
        mFpSdk.onStatusChange("Cancel");
    }

    /**
     * Ready to listen 1 fingerprint.
     */
    public void generateTemplate1() {
        cancel();
        if (fpm.GenerateTemplate(1)) worktype = 0;
        else {
            mFpSdk.onStatusChange("Please wait...");
            mFpSdk.onBusy();
        }
    }

    /**
     * Ready to listen 4 fingerprints.
     */
    public void generateTemplate4() {
        cancel();
        if (fpm.GenerateTemplate(4)) worktype = 1;
        else {
            mFpSdk.onStatusChange("Please wait...");
            mFpSdk.onBusy();
        }
    }

    private void initHandler() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FPM_DEVICE:
                        switch (msg.arg1) {
                            case Constants.DEV_OK:
                                mFpSdk.onDeviceOpen();
                                break;
                            case Constants.DEV_FAIL:
                                mFpSdk.onDeviceFail("Open Device Fail");
                                break;
                            case Constants.DEV_ATTACHED:
                                mFpSdk.onDeviceFail("USB Device Attached");
                                break;
                            case Constants.DEV_DETACHED:
                                mFpSdk.onDeviceFail("USB Device Detached");
                                break;
                            case Constants.DEV_CLOSE:
                                mFpSdk.onDeviceClose("Device Close");
                                break;
                        }
                        break;
                    case Constants.FPM_PLACE:
                        mFpSdk.showPlaceFinger();
                        break;
                    case Constants.FPM_LIFT:
                        mFpSdk.onStatusChange("Lift Finger");
                        break;
                    case Constants.FPM_GENCHAR: {
                        if (msg.arg1 == 1) {
                            if (worktype == 0) {
                                byte matdata[] = new byte[Constants.TEMPLATESIZE];
                                fpm.GetTemplateByGen(matdata);
                                mFpSdk.onFpDetected(matdata);
                            } else {
                                byte refdata[] = new byte[Constants.TEMPLATESIZE * 4];
                                fpm.GetTemplateByGen(refdata);
                                mFpSdk.onFpDetected(refdata);
                            }
                        } else {
                            mFpSdk.onStatusChange("Generate Template Fail");
                            mFpSdk.onFingerGenerateFail();
                            if (worktype == 0) {
                                if (delayRestartRunnable == null)
                                    delayRestartRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            generateTemplate1();
                                        }
                                    };
                                if (delayRestartHandler == null)
                                    delayRestartHandler = new Handler();
                                delayRestartHandler.postDelayed(delayRestartRunnable, RESTART_DELAY);
                            }
                        }
                    }
                    break;
                    case Constants.FPM_NEWIMAGE: {
                        fpm.GetBmpImage(bmpdata);
                        Bitmap bmp = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.length);
                        mFpSdk.showLiftFinger(bmp);
                    }
                    break;
                    case Constants.FPM_TIMEOUT:
                        mFpSdk.onStatusChange("Time Out");
                        break;
                }
                return true;
            }
        });


    }

    public int matchFingure(ArrayList<FingureModel> fingureList, byte matdata[]) {
        int score = 0;
        int index = -1;
        for (int i = 0; i < fingureList.size(); i++) {
            int sc = fpm.MatchTemplate(fingureList.get(i).getFingerInBytes(), fingureList.get(i).getFingerInBytes().length, matdata, matdata.length);
            if (sc > 60) {
                if (sc > score) {
                    index = i;
                    score = sc;
                }
            }
        }
        return index;
    }

    public int MatchTemplate(byte[] fingerInBytes, int length, byte[] matdata, int length1) {
        return fpm.MatchTemplate(fingerInBytes, length, matdata, length1);
    }

    public boolean matchTemplate(byte[] fingerInBytes, int length, byte[] matdata, int length1, int score) {
        return fpm.MatchTemplate(fingerInBytes,length, matdata,length1,score);
    }

    public FPModule getFpModule() {
        return fpm;
    }

    public boolean MatchTemplateFull(byte[] piEnl, byte[] piMat, int tempMatchScore) {
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
  /*  public boolean isSdkOpen(){
        fpm.
    }
*/

    public interface IFpSdk {

        void onStatusChange(String status);

        void onFpDetected(byte[] metadata);

        void showPlaceFinger();

        void showLiftFinger(Bitmap bmp);

        void onDeviceOpen();

        void onDeviceFail(String error);

        void onDeviceClose(String s);

        void onFingerGenerateFail();

        void onBusy();
        //void deviceType(String deviceType);

    }

}
