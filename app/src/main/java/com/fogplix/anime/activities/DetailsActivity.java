package com.fogplix.anime.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fogplix.anime.R;
import com.fogplix.anime.adapters.EpisodesButtonsAdapter;
import com.fogplix.anime.callbacks.DetailsScraperCallback;
import com.fogplix.anime.databinding.ActivityDetailsBinding;
import com.fogplix.anime.helpers.CustomMethods;
import com.fogplix.anime.helpers.GetMALId;
import com.fogplix.anime.helpers.Scraper;

import org.json.JSONArray;
import org.json.JSONObject;

@UnstableApi
public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = "MADARA";
    private ActivityDetailsBinding binding;
    private EpisodesButtonsAdapter episodesButtonsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.getRoot());

//        ------------------------------------------------------------------------------------------

        Intent intent = getIntent();

        String animeId = intent.getStringExtra("animeId");

        String episodeId = null;

        if (intent.hasExtra("episodeId")) {
            episodeId = intent.getStringExtra("episodeId");
        }

        Scraper scraper = new Scraper(DetailsActivity.this, new DetailsScraperCallback() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onScrapingComplete(JSONObject animeDetails) {

                try {
                    binding.progressBarContainer.setVisibility(View.GONE);
                    binding.detailsContainerScrollView.setVisibility(View.VISIBLE);

                    Glide.with(DetailsActivity.this)
                            .load(animeDetails.getString("animeImg"))
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.drawable.preload_thumb)
                            .into(binding.thumbnailImageView);

                    String animeTitle = animeDetails.getString("animeTitle");
                    String animeStatus = animeDetails.getString("status");
                    String animeType = animeDetails.getString("type");
                    String animeReleasedDate = animeDetails.getString("releasedDate");
                    String animeTotalEpisodes = animeDetails.getString("totalEpisodes");

                    binding.animeTitleTV.setText(animeTitle);
                    binding.statusTV.setText(CustomMethods.capitalize(animeStatus));
                    binding.animeTypeTV.setText(animeType);
                    binding.releaseDateTV.setText(animeReleasedDate);
                    binding.totalEpisodesTV.setText(animeTotalEpisodes);

                    StringBuilder genres = new StringBuilder();

                    JSONArray genresArray = animeDetails.getJSONArray("genres");

                    for (int i = 0; i < genresArray.length(); i++) {
                        genres.append(", ").append(CustomMethods.capitalize(genresArray.getString(i)));
                    }
                    binding.genresTV.setText(genres.substring(1).trim());

                    //------------------------------------------------------------------------------

                    JSONArray episodesListArray = animeDetails.getJSONArray("episodesList");

                    GetMALId.getId(animeTitle, malID -> {

                        binding.loadingEpisodesContainer.setVisibility(View.GONE);
                        binding.episodesBtnRecyclerView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "malID: " + malID);

                        episodesButtonsAdapter = new EpisodesButtonsAdapter(DetailsActivity.this, episodesListArray, animeId, malID);
                        binding.episodesBtnRecyclerView.setAdapter(episodesButtonsAdapter);

                        GridLayoutManager layoutManager = new GridLayoutManager(DetailsActivity.this, 3);
                        binding.episodesBtnRecyclerView.setLayoutManager(layoutManager);
                    });

                    //------------------------------------------------------------------------------

                    binding.showDescriptionBtn.setOnClickListener(view -> {

                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                        builder.setTitle("Synopsis");
                        try {
                            builder.setMessage(animeDetails.getString("synopsis"));
                        } catch (Exception e) {
                            Log.e(TAG, "onScrapingComplete: ", e);
                            builder.setTitle("Error");
                            builder.setMessage(e.getMessage());
                        }
                        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
                        builder.create();
                        builder.show();
                    });

                } catch (Exception e) {
                    Log.e(TAG, "onScrapingComplete: ", e);
                    CustomMethods.errorAlert(DetailsActivity.this, "Error (Json DT)", e.getMessage(), "OK", true);
                }
            }

            @Override
            public void onScrapingFailed(String error) {
                CustomMethods.errorAlert(DetailsActivity.this, "Error (Json DT)", error, "OK", true);
            }
        });

        scraper.scrapeDetails(animeId, episodeId);

        binding.backBtn.setOnClickListener(view -> onBackPressed());
        binding.searchBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, SearchActivity.class));
            finish();
        });
    }

//    ----------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();

        if (episodesButtonsAdapter != null) {
            episodesButtonsAdapter.notifyDataSetChanged();
        }
    }
}