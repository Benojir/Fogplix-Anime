package com.fogplix.anime.callbacks;

import org.json.JSONObject;

public interface DetailsScraperCallback {
    void onScrapingComplete(JSONObject animeDetails);
    void onScrapingFailed(String error);
}
