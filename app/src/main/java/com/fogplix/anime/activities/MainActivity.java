package com.fogplix.anime.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.fogplix.anime.BuildConfig;
import com.fogplix.anime.R;
import com.fogplix.anime.adapters.FragmentAdapter;
import com.fogplix.anime.dialogs.JoinTelegramDialog;
import com.fogplix.anime.helpers.AdBlockerDetector;
import com.fogplix.anime.helpers.CustomMethods;
import com.fogplix.anime.helpers.HPSharedPreference;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 3153;
    private static final String TAG = "MADARA";
    private DrawerLayout drawerLayout;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TextView importantNoticeTV;
    private NavigationView navigationView;
    private boolean keepSplashScreen = true;
    private boolean doubleBackPressed = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> keepSplashScreen);
        new Handler().postDelayed(() -> keepSplashScreen = false, 3000);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        initVars();

        checkAndRequestNotificationPermission();

        ////////////////////////////////////////////////////////////////////////////////////////////

        View ownToolbar = findViewById(R.id.ownToolbar);

        ImageButton navLeftBtn = ownToolbar.findViewById(R.id.navbarLeftBtn);

        navLeftBtn.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.apps_24));
        navLeftBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ownToolbar.findViewById(R.id.navbarRightBtn).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));

        ////////////////////////////////////////////////////////////////////////////////////////////

        CustomMethods.checkForUpdateOnStartApp(this);
        CustomMethods.checkNewNotice(this, importantNoticeTV);
        CustomMethods.checkPlayableServersStatus(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        AdBlockerDetector.detectAdBlocker(isAdBlockerDetected -> {
            if (isAdBlockerDetected) {
                if (preferences.getBoolean("use_proxy", false)) {
                    CustomMethods.warningAlert(MainActivity.this, "Warning", getString(R.string.adblock_detected), "Ignore", false);
                } else {
                    Toast.makeText(this, "Ad-blocker detected! Don't worry, it's ads free.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////

        JoinTelegramDialog joinTelegramDialog = new JoinTelegramDialog(MainActivity.this);

        if (!new HPSharedPreference(MainActivity.this).doNotShowTGDialog()) {
            joinTelegramDialog.showJoinTelegramDialog();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        View headView = navigationView.getHeaderView(0);

        ((TextView) headView.findViewById(R.id.header_layout_version_tv)).setText("Version: " + BuildConfig.VERSION_NAME);

        navigationViewItemClickedActions(navigationView);

        ////////////////////////////////////////////////////////////////////////////////////////////

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentManager, getLifecycle(), tabLayout.getTabCount());
        viewPager2.setAdapter(fragmentAdapter);
        viewPager2.setUserInputEnabled(true);

        //for changing the first tab icon color
        Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(0)).getIcon()).setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());

                Objects.requireNonNull(tab.getIcon()).setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(tab.getIcon()).setColorFilter(getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }


//    ----------------------------------------------------------------------------------------------

    private void navigationViewItemClickedActions(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.favorite_anime_menu_item) {

                startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));

                new Handler().postDelayed(() -> {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }, 500);
            } else if (item.getItemId() == R.id.settings_menu_item) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));

                new Handler().postDelayed(() -> {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }, 500);
            } else if (item.getItemId() == R.id.report_bug_action) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_group))));
            } else if (item.getItemId() == R.id.share_action) {
                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_sharing_message) + "\n" + getString(R.string.official_website));
                startActivity(Intent.createChooser(intent1, "Share via"));
            } else if (item.getItemId() == R.id.more_apps_action) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_apps))));
            } else if (item.getItemId() == R.id.visit_website) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_website))));
            } else if (item.getItemId() == R.id.visit_telegram) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_channel))));
            }
            return false;
        });
    }

//    ----------------------------------------------------------------------------------------------

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackPressed) {
                super.onBackPressed();
            } else {
                this.doubleBackPressed = true;
                Snackbar.make(drawerLayout, "Double press to exit!", Snackbar.LENGTH_LONG).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackPressed = false, 2000);
            }
        }
    }
//    ----------------------------------------------------------------------------------------------

    private void initVars() {
        drawerLayout = findViewById(R.id.home_page_drawerlayout);
        viewPager2 = findViewById(R.id.fragmentContainerViewPager2Main);
        tabLayout = findViewById(R.id.tabLayout);
        navigationView = findViewById(R.id.navigation_drawer);
        importantNoticeTV = findViewById(R.id.important_notice_tv);
    }

//    ----------------------------------------------------------------------------------------------

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            String NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS;

            if (ContextCompat.checkSelfPermission(this, NOTIFICATION_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this,
                        new String[]{NOTIFICATION_PERMISSION},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "onRequestPermissionsResult: notification permission granted.");
            } else {
                // Permission denied
                Log.d(TAG, "onRequestPermissionsResult: notification permission denied.");
            }
        }
    }
}