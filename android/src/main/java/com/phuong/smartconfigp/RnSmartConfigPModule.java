package com.phuong.smartconfigp;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.EsptouchTask;
import android.content.Context;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;

import android.os.AsyncTask;

import java.util.List;

public class RnSmartConfigPModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private IEsptouchTask mEsptouchTask;
    private EsptouchAsyncTask esptouchAsyncTask;

    public RnSmartConfigPModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RnSmartConfigP";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void stop(){
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
    }

    @ReactMethod
    public void start(final ReadableMap options, final Promise promise) throws Exception {
        byte[] ssid = ByteUtil.getBytesByString(options.getString("ssid"));
        byte[] pass = ByteUtil.getBytesByString(options.getString("password"));
        byte[] bssid = TouchNetUtil.parseBssid2bytes(options.getString("bssid"));
        byte[] deviceCountData = String.valueOf(options.getInt("count")).getBytes();
        byte[] broadcast = {(byte) (options.getString("cast") == "broadcast" ? 1 : 0)};

        if (esptouchAsyncTask != null) {
            esptouchAsyncTask.cancelEsptouch();
        }

        esptouchAsyncTask = new EsptouchAsyncTask(result -> new Thread(() -> {
            try {
                IEsptouchResult firstResult = result.get(0);
                if (firstResult.isCancelled()) {
                    promise.reject("Timoutout or not Found");
                }
                if (!firstResult.isSuc()) {
                    promise.reject("Timoutout or not Found");
                }

                WritableArray ret = Arguments.createArray();
                for (IEsptouchResult touchResult : result) {
                    WritableMap map = Arguments.createMap();
                    map.putString("bssid", touchResult.getBssid());
                    map.putString("ipv4", touchResult.getInetAddress().getHostAddress());
                    ret.pushMap(map);
                }
                promise.resolve(ret);
            }
            catch (Exception e){
                promise.reject(e);
            }
        }).start());
        esptouchAsyncTask.execute(ssid, bssid, pass, deviceCountData, broadcast);
    }

    public interface TaskListener {
        public void onFinished(List<IEsptouchResult> result);
    }

    private class EsptouchAsyncTask extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {


        private final Object mLock = new Object();

        private final TaskListener taskListener;

        public EsptouchAsyncTask(TaskListener listener) {
            this.taskListener = listener;
        }

        void cancelEsptouch() {
            cancel(true);
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            int taskResultCount;
            synchronized (mLock) {
                byte[] apSsid = params[0];
                byte[] apBssid = params[1];
                byte[] apPassword = params[2];
                byte[] deviceCountData = params[3];
                byte[] broadcastData = params[4];
                taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
                Context context = getCurrentActivity().getApplicationContext();
                mEsptouchTask = (IEsptouchTask) new EsptouchTask(apSsid, apBssid, apPassword, context);
                mEsptouchTask.setPackageBroadcast(broadcastData[0] == 1);
                //mEsptouchTask.setEsptouchListener();
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {

            //IEsptouchResult firstResult = result.get(0);

            //if (!firstResult.isCancelled()) {
                if(this.taskListener != null) {
                    this.taskListener.onFinished(result);
                }
            //}
        }
    }
}
