package com.fogplix.anime.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import com.fogplix.anime.R;
import com.fogplix.anime.helpers.HPSharedPreference;

import java.util.Objects;

public class JoinTelegramDialog {

    private final Activity activity;

    public JoinTelegramDialog(Activity activity){
        this.activity = activity;
    }

    public void showJoinTelegramDialog(){

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_join_telegram);

        // Set dialog width to match screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        CheckBox checkBox = dialog.findViewById(R.id.doNotShowTGCheckBox);

        Button positiveBtn = dialog.findViewById(R.id.joinTGBtn);
        Button negativeBtn = dialog.findViewById(R.id.cancelJoinTGBtn);

        positiveBtn.setOnClickListener(view -> {
            new HPSharedPreference(activity).saveDoNotShowTGDialog(checkBox.isChecked());
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.official_telegram_channel))));
            dialog.dismiss();
        });

        negativeBtn.setOnClickListener(view -> {
            new HPSharedPreference(activity).saveDoNotShowTGDialog(checkBox.isChecked());
            dialog.dismiss();
        });

        dialog.show();
    }
}
