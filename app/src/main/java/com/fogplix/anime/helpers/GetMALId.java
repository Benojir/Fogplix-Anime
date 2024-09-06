package com.fogplix.anime.helpers;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetMALId {

    private static final String TAG = "MADARA";

    public interface OnGetMALIdListener {
        void onComplete(String malId);
    }

    public static void getId(String animeTitle, OnGetMALIdListener listener) {

        animeTitle = animeTitle.toLowerCase().replace("(dub)", "").trim();

        String url = "https://myanimelist.net/search/prefix.json?type=anime&keyword=" + Uri.encode(animeTitle);

        Log.d(TAG, "getId url: " + url);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        String finalAnimeTitle = animeTitle;

        new Thread(() -> {

            String malID = "";

            try {
                Response response = client.newCall(request).execute();

                if (response.body() == null) {
                    Log.d(TAG, "getId: response is null");
                    return;
                }

                String responseBody = response.body().string();

                JSONObject responseJson = new JSONObject(responseBody);
                JSONArray items = responseJson.getJSONArray("categories").getJSONObject(0).getJSONArray("items");

                if (items.length() == 0) {
                    Log.d(TAG, "getId: items.length() == 0");
                    return;
                }

                for (int i = 0; i < items.length(); i++) {

                    try {
                        JSONObject item = items.getJSONObject(i);
                        int malId = item.getInt("id");
                        String name = item.getString("name").trim();
                        Log.d(TAG, "getId anime name: " + name);

                        if (name.equalsIgnoreCase(finalAnimeTitle)) {
                            malID = String.valueOf(malId);
                            Log.d(TAG, "getId: " + malId);
                            break;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "getId: ", e);
                        break;
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "getId: ", e);
            }

            String finalMalID = malID;
            new Handler(Looper.getMainLooper()).post(() -> listener.onComplete(finalMalID));

        }).start();
    }
}
