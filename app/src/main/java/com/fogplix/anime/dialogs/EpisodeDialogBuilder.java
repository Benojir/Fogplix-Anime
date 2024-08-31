package com.fogplix.anime.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.media3.common.util.UnstableApi;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.fogplix.anime.R;
import com.fogplix.anime.activities.PlayerActivity;
import com.fogplix.anime.helpers.CustomMethods;
import com.fogplix.anime.helpers.GenerateDirectLink;
import com.fogplix.anime.helpers.HPSharedPreference;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Objects;

@UnstableApi
public class EpisodeDialogBuilder {

    private static final String TAG = "MADARA";
    private final Activity activity;

    public EpisodeDialogBuilder(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("SetTextI18n")
    public void choosePlayServer(String episodeId, String animeTitle, String animeId) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_play_server_selector);

        // Set dialog width to match screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        ((TextView) dialog.findViewById(R.id.episodeNumTV)).setText("Episode " + CustomMethods.extractEpisodeNumberFromId(episodeId));

        Button server_1_btn = dialog.findViewById(R.id.server_1_btn);
        Button server_2_btn = dialog.findViewById(R.id.server_2_btn);

//        ------------------------------------------------------------------------------------------

        server_1_btn.setOnClickListener(view -> {

            HPSharedPreference hpSharedPreference = new HPSharedPreference(activity);

            boolean serverStatus = hpSharedPreference.getPlayableServersStatus("server_1");

            if (serverStatus) {

                MyProgressDialog mpd = new MyProgressDialog(activity);
                mpd.setMessage("Generating playable link...");
                mpd.setCancelable(false);
                mpd.show();

                GenerateDirectLink generateDirectLink = new GenerateDirectLink(activity);

                generateDirectLink.generate(episodeId, new GenerateDirectLink.OnGenerateDirectLink() {
                    @Override
                    public void onComplete(JSONObject object) {

                        mpd.dismiss();
                        dialog.dismiss();

                        try {
                            String refererUrl = object.getString("referer");
                            String videoHLSUrl = object.getString("videoHLSUrl");
                            String videoHLSUrl2 = object.getString("videoHLSUrl2");

                            choosePlayOrDownload(animeId, animeTitle, episodeId, refererUrl, videoHLSUrl, videoHLSUrl2);

                        } catch (JSONException e) {
                            Toast.makeText(activity, "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailed(String error) {
                        mpd.dismiss();
                        dialog.dismiss();
                        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailed: ");
                    }
                });

            } else {
                Toast.makeText(activity, "This server is not working currently. Try another server.", Toast.LENGTH_SHORT).show();
            }
        });

//        ------------------------------------------------------------------------------------------

        server_2_btn.setOnClickListener(v -> {

            HPSharedPreference hpSharedPreference = new HPSharedPreference(activity);

            boolean serverStatus = hpSharedPreference.getPlayableServersStatus("server_2");

            if (serverStatus) {

                String vidcdn_api = activity.getString(R.string.VIDCDN_API_URL) + episodeId;

                MyProgressDialog pd = new MyProgressDialog(activity);
                pd.setMessage("Generating playable link...");
                pd.setCancelable(false);
                pd.show();

                StringRequest objectRequest1 = new StringRequest(Request.Method.GET, vidcdn_api, response -> {

                    try {

                        JSONObject object = new JSONObject(response);

                        if (object.has("error")) {
                            pd.dismiss();
                            CustomMethods.errorAlert(activity, "Error", object.getString("error"), "OK", true);
                        } else {

                            String refererUrl = object.getString("linkiframe");
                            String videoHLSUrl = object.getJSONArray("source").getJSONObject(0).getString("file");
                            String videoHLSUrl2 = object.getJSONArray("source_bk").getJSONObject(0).getString("file");

                            pd.dismiss();
                            dialog.dismiss();

                            choosePlayOrDownload(animeId, animeTitle, episodeId, refererUrl, videoHLSUrl, videoHLSUrl2);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "choosePlayServer: ", e);
                        Toast.makeText(activity, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        CustomMethods.errorAlert(activity, "Error (Try another server)", e.getMessage() + "\n", "OK", true);
                    }
                }, error -> {
                    pd.dismiss();
                    CustomMethods.errorAlert(activity, "Error (Try another server.)", error.getMessage(), "OK", true);
                });

                Cache cache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024); // 1MB cap
                Network network = new BasicNetwork(new HurlStack());


                RequestQueue queue = new RequestQueue(cache, network);
                objectRequest1.setRetryPolicy((new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
                queue.add(objectRequest1);
                queue.start();

                dialog.dismiss();
            } else {
                Toast.makeText(activity, "This server is not working currently. Try another server.", Toast.LENGTH_SHORT).show();
            }
        });

//        ------------------------------------------------------------------------------------------

        dialog.show();
    }

//    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void choosePlayOrDownload(String animeId, String animeTitle, String episodeId, String refererUrl, String videoHLSUrl, String videoHLSUrl2) {

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_play_or_download);

        // Set dialog width to match screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        Button playOptionBtn = dialog.findViewById(R.id.play_option_btn);
        Button downloadOptionBtn = dialog.findViewById(R.id.download_option_btn);

        playOptionBtn.setOnClickListener(view -> {

            Intent intent = new Intent(activity, PlayerActivity.class);
            intent.putExtra("episodeId", episodeId);
            intent.putExtra("animeId", animeId);
            intent.putExtra("animeTitle", animeTitle);
            intent.putExtra("refererUrl", refererUrl);
            intent.putExtra("videoHLSUrl", videoHLSUrl);
            intent.putExtra("videoHLSUrl2", videoHLSUrl2);
            activity.startActivity(intent);

            dialog.dismiss();
        });


        downloadOptionBtn.setOnClickListener(view -> {

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetDialog);
            bottomSheetDialog.setCancelable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetDialog.setContentView(R.layout.sample_download_option_bottomsheet_layout);

            CardView option1 = bottomSheetDialog.findViewById(R.id.download_option_1);
            CardView option2 = bottomSheetDialog.findViewById(R.id.download_option_2);

            if (option1 != null) {

                option1.setOnClickListener(view1 -> {

                    if (!refererUrl.equals("")) {

                        try {
                            URL url = new URL(refererUrl);

                            String protocol = url.getProtocol();
                            String host = url.getHost();
                            String newPath = "/download";
                            String query = url.getQuery();

                            String downloadUrl = protocol + "://" + host + newPath + "?" + query;

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                            activity.startActivity(intent);

                            bottomSheetDialog.dismiss();
                        } catch (Exception e) {
                            Log.e(TAG, "choosePlayOrDownload: ", e);
                            Toast.makeText(activity, "Cannot parse download url. Please choose option 2.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, "Option 1 will not work. Try option 2", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //======================================================================================

            if (option2 != null) {

                option2.setOnClickListener(view1 -> {

                    String idmPackageName = "idm.internet.download.manager";

                    if (CustomMethods.isAppInstalledOrNot(activity, idmPackageName)) {

                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoHLSUrl2));
                            //intent.setClassName("idm.internet.download.manager", "idm.internet.download.manager.MainActivity");
                            intent.setPackage("idm.internet.download.manager");
                            activity.startActivity(intent);

                        } catch (Exception e) {
                            Log.e(TAG, "choosePlayOrDownload: ", e);
                            CustomMethods.errorAlert(activity, "Error", e.getMessage(), "OK", false);
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("1DM required");
                        builder.setMessage("1DM is not installed in your device. Install 1DM first to download this episode.");
                        builder.setPositiveButton("Install", (dialog1, which) -> activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=idm.internet.download.manager"))));
                        builder.create().show();
                    }

                    bottomSheetDialog.dismiss();
                });
            }

            bottomSheetDialog.show();

        });

        dialog.show();
    }

//    ----------------------------------------------------------------------------------------------
}
