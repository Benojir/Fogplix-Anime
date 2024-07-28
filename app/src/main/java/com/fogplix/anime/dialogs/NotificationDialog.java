package com.fogplix.anime.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fogplix.anime.R;

import java.util.Objects;

public class NotificationDialog {

    private String heading = "Heading";
    private String message = "Message body";
    private String buttonText = "Okay";
    private Drawable image = null;
    private int backgroundColor;
    private int buttonColor;
    private int buttonTextColor;
    private int headingTextColor;
    private int messageTextColor;

    private final Dialog dialog;
    private final LinearLayout mainLayout;
    private final ImageView imageView;
    private final TextView headingTV;
    private final TextView messageTV;
    private final Button okButton;

    private OnActionListener listener;

    public NotificationDialog(Activity activity) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notification);

        // Set dialog width to match screen width
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(layoutParams);

        mainLayout = dialog.findViewById(R.id.main);
        imageView = dialog.findViewById(R.id.dialog_imageview);
        headingTV = dialog.findViewById(R.id.dialog_heading_tv);
        messageTV = dialog.findViewById(R.id.dialog_message_tv);
        okButton = dialog.findViewById(R.id.dialog_ok_btn);
    }

    public void setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
    }

    public void setButtonTextColor(int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    public void setHeading(@NonNull String heading) {
        this.heading = heading;
    }

    public void setHeadingTextColor(int headingTextColor) {
        this.headingTextColor = headingTextColor;
    }

    public void setMessageTextColor(int messageTextColor) {
        this.messageTextColor = messageTextColor;
    }

    public void setMessage(@NonNull String message) {
        this.message = message;
    }

    public void setButtonText(@NonNull String buttonText) {
        this.buttonText = buttonText;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
    }

    public void setActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    public void show() {

        if (image == null) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setImageDrawable(image);
        }

        headingTV.setText(heading);
        messageTV.setText(message);
        okButton.setText(buttonText);

        if (backgroundColor != 0) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        if (buttonColor != 0) {
            okButton.setBackgroundColor(buttonColor);
        }

        if (buttonTextColor != 0) {
            okButton.setTextColor(buttonTextColor);
        }

        if (headingTextColor != 0) {
            headingTV.setTextColor(headingTextColor);
        }

        if (messageTextColor != 0) {
            messageTV.setTextColor(messageTextColor);
        }

        okButton.setOnClickListener(v -> listener.onActionTaken(okButton, dialog));

        dialog.show();
    }

    public interface OnActionListener {
        void onActionTaken(Button button, Dialog dialog);
    }
}
