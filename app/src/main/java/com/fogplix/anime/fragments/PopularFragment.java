package com.fogplix.anime.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.anime.R;
import com.fogplix.anime.adapters.VerticalItemsListAdapter;
import com.fogplix.anime.callbacks.AnimeScraperCallback;
import com.fogplix.anime.databinding.FragmentPopularBinding;
import com.fogplix.anime.helpers.CustomMethods;
import com.fogplix.anime.helpers.Scraper;

import org.json.JSONArray;
import org.json.JSONException;

public class PopularFragment extends Fragment {

    private FragmentPopularBinding binding;
    private Activity activity;
    private VerticalItemsListAdapter rvAdapter;
    private GridLayoutManager layoutManager;
    private boolean isScrolling = false;
    private int currentItems, totalItems, scrollOutItems;
    private final JSONArray allAnime = new JSONArray();
    private int page = 1;
    private boolean alreadyReachedLastPage = false;
    private boolean firstTimeSearch = true;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();

        if (activity != null) {

            rvAdapter = new VerticalItemsListAdapter(activity, allAnime, false);
            binding.recyclerView.setAdapter(rvAdapter);
            layoutManager = new GridLayoutManager(activity, 3);
            binding.recyclerView.setLayoutManager(layoutManager);

            loadAnime(page);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPopularBinding.inflate(inflater, container, false);

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                currentItems = layoutManager.getChildCount();
                totalItems = layoutManager.getItemCount();
                scrollOutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && (currentItems + scrollOutItems == totalItems)){

                    isScrolling = false;
                    page = page + 1;
                    loadAnime(page);
                }
            }
        });

        return binding.getRoot();
    }

//    ----------------------------------------------------------------------------------------------

    private void loadAnime(int page){

        String pageLink = activity.getString(R.string.gogoanime_url) + "/popular.html?page=" + page;

        if (!alreadyReachedLastPage){

            if (page > 1){
                binding.loaderProgressBottom.setVisibility(View.VISIBLE);
            }

            Scraper scraper = new Scraper(activity, new AnimeScraperCallback() {
                @Override
                public void onScrapeComplete(JSONArray resultAnime) {

                    binding.loaderProgressCenter.setVisibility(View.GONE);
                    binding.loaderProgressBottom.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    try {

                        if (resultAnime.length() <= 0){
                            alreadyReachedLastPage = true;
                        }

                        firstTimeSearch = false;

                        int startPosition = rvAdapter.getItemCount(); // Get the current item count

                        CustomMethods.mergeTwoJsonArray(allAnime, resultAnime);

                        int itemCount = rvAdapter.getItemCount() - startPosition; // Calculate the number of inserted items

                        rvAdapter.notifyItemRangeInserted(startPosition, itemCount);

                    } catch (JSONException e){
                        e.printStackTrace();
                        CustomMethods.errorAlert(activity, "Error", e.getMessage(), "OK", false);
                    }
                }

                @Override
                public void onScrapeFailed(String error) {
                    alreadyReachedLastPage = true;
                    binding.loaderProgressCenter.setVisibility(View.GONE);
                    binding.loaderProgressBottom.setVisibility(View.GONE);

                    if (firstTimeSearch){
                        CustomMethods.errorAlert(activity, "Error PL", error, "OK", false);
                    }
                }
            });

            scraper.scrapeAnime(pageLink);

        }
    }
}