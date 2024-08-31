package com.fogplix.anime.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fogplix.anime.adapters.FavoriteAnimeAdapter;
import com.fogplix.anime.databinding.ActivityFavoriteBinding;
import com.fogplix.anime.databinding.OwnToolbarBinding;
import com.fogplix.anime.helpers.MyDatabaseHandler;

public class FavoriteActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFavoriteBinding binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        OwnToolbarBinding ownToolbarBinding = binding.ownToolbar;

        ownToolbarBinding.imageViewMiddle.setVisibility(View.GONE);
        ownToolbarBinding.navbarRightBtn.setVisibility(View.GONE);
        ownToolbarBinding.ownToolbarTV.setVisibility(View.VISIBLE);
        ownToolbarBinding.navbarLeftBtn.setOnClickListener(v -> onBackPressed());

        MyDatabaseHandler handler = new MyDatabaseHandler(this);

        handler.getAllFavoriteAnime(favoriteLists -> {

            if (favoriteLists.size() > 0) {
                FavoriteAnimeAdapter adapter = new FavoriteAnimeAdapter(this, favoriteLists);
                binding.recyclerView.setAdapter(adapter);

                GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
                binding.recyclerView.setLayoutManager(layoutManager);

                ownToolbarBinding.ownToolbarTV.setText("Favorites (" + favoriteLists.size() + ")");

            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.noAnimeContainer.setVisibility(View.VISIBLE);
            }
        });
    }
}