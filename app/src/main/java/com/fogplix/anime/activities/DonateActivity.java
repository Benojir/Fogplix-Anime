package com.fogplix.anime.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.NestedScrollView;

import com.fogplix.anime.R;
import com.fogplix.anime.databinding.ActivityDonateBinding;
import com.fogplix.anime.databinding.OwnToolbarBinding;

public class DonateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDonateBinding binding = ActivityDonateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        OwnToolbarBinding ownToolbarBinding = binding.ownToolbar;
        ownToolbarBinding.ownToolbarTV.setVisibility(View.VISIBLE);
        ownToolbarBinding.imageViewMiddle.setVisibility(View.GONE);
        ownToolbarBinding.navbarRightBtn.setVisibility(View.GONE);

        String donateUs = "Donate Us";
        ownToolbarBinding.ownToolbarTV.setText(donateUs);
        ownToolbarBinding.navbarLeftBtn.setOnClickListener(view -> onBackPressed());

        binding.nestedScrollView.postDelayed(() -> {
            binding.nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
            // Delay for 1 second then scroll back to the top
            new Handler().postDelayed(() -> binding.nestedScrollView.fullScroll(NestedScrollView.FOCUS_UP), 1000); // 1000 milliseconds = 1 second
        }, 1000); // Delay to ensure the layout is complete

        binding.sendViaBMAC.setOnClickListener(v -> new AlertDialog.Builder(DonateActivity.this)
                .setTitle("Donate Us \uD83E\uDD7A")
                .setMessage("Thank you for your support. We need your help to continue our project.")
                .setIcon(AppCompatResources.getDrawable(this, R.drawable.donate_icon))
                .setPositiveButton("Continue", (dialog, which) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bmc_donation_link))));
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "We need your help.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .create()
                .show());

        binding.sendViaUPI.setOnClickListener(v -> new AlertDialog.Builder(DonateActivity.this)
                .setTitle("Donate Us \uD83E\uDD7A")
                .setMessage("Thank you for your support. We need your help to continue our project.\n" +
                        "You can give us a very small donation through UPI.\n" +
                        "Our UPI Id: nura57764@axl")
                .setIcon(AppCompatResources.getDrawable(this, R.drawable.donate_icon))
                .setPositiveButton("Copy UPI", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", "nura57764@axl");
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "We need your help.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .create()
                .show());
    }
}