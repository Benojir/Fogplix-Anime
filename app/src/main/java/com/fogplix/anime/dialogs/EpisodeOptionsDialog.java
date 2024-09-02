package com.fogplix.anime.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.fogplix.anime.R;
import com.fogplix.anime.activities.PlayerActivity;
import com.fogplix.anime.helpers.CustomMethods;
import com.fogplix.anime.helpers.GenerateDirectLink;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

@OptIn(markerClass = UnstableApi.class)
public class EpisodeOptionsDialog {

    public EpisodeOptionsDialog(Activity activity, String episodeId) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_play_or_download);

        // Set dialog width to match screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        dialog.show();

        Button playBtn = dialog.findViewById(R.id.play_option_btn);
        Button downloadBtn = dialog.findViewById(R.id.download_option_btn);

        playBtn.setOnClickListener(v -> {
            Intent intent = new Intent(activity, PlayerActivity.class);
            intent.putExtra("episodeId", episodeId);
            activity.startActivity(intent);
            dialog.dismiss();
        });

        downloadBtn.setOnClickListener(v -> {

            MyProgressDialog mpd = new MyProgressDialog(activity);
            mpd.setMessage("Generating download links...");
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
                        CustomMethods.chooseDownloadOptions(activity, refererUrl, videoHLSUrl);
                    } catch (JSONException e) {
                        Toast.makeText(activity, "Something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed(String error) {
                    mpd.dismiss();
                    dialog.dismiss();
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                    Log.d("MADARA", "onFailed: ");
                }
            });
        });
    }
}
