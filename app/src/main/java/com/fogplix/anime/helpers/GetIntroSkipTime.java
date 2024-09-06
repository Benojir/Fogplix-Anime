package com.fogplix.anime.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetIntroSkipTime {

    private static final String TAG = "MADARA";

    public interface OnGetIntroSkipTimeListener {
        void onComplete(boolean success, JSONObject interval);
    }

    public static void onGetIntroSkipTime(String malID, OnGetIntroSkipTimeListener listener) {

        String url = "https://api.aniskip.com/v1/skip-times/" + malID + "/1?types=op";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        new Thread(() -> {

            try (Response response = client.newCall(request).execute()) {

                if (response.body() == null) {
                    Log.d(TAG, "onGetIntroSkipTime: response is null");
                    new Handler(Looper.getMainLooper()).post(() -> listener.onComplete(false, null));
                    return;
                }

                String responseBody = response.body().string();

                Log.d(TAG, "onGetIntroSkipTime responseBody: " + responseBody);

                JSONObject responseJson = new JSONObject(responseBody);

                if (responseJson.getBoolean("found")) {
                    JSONObject interval = responseJson.getJSONArray("results").getJSONObject(0).getJSONObject("interval");

                    JSONObject result = new JSONObject();
                    result.put("start_time", interval.getDouble("start_time"));
                    result.put("end_time", interval.getDouble("end_time"));

                    new Handler(Looper.getMainLooper()).post(() -> listener.onComplete(true, result));
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onComplete(false, null));
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onComplete(false, null));
                Log.e(TAG, "getId: ", e);
            }
        }).start();
    }
}
