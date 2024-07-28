package com.fogplix.anime.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.fogplix.anime.R;
import com.fogplix.anime.helpers.MyDatabaseHandler;
import com.fogplix.anime.dialogs.EpisodeDialogBuilder;

import org.json.JSONArray;

@UnstableApi
public class EpisodesButtonsAdapter extends RecyclerView.Adapter<EpisodesButtonsAdapter.MyCustomViewHolder> {

    Context context;
    JSONArray allEpisodesJArray;
    String animeId;
    String animeTitle;
    private final String TAG = "MADARA";

    public EpisodesButtonsAdapter(Context context, JSONArray allEpisodesJArray, String animeId, String animeTitle){
        this.context = context;
        this.allEpisodesJArray = allEpisodesJArray;
        this.animeId = animeId;
        this.animeTitle = animeTitle;
    }

    @NonNull
    @Override
    public MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.sample_episode_button_design, parent, false);
        return new EpisodesButtonsAdapter.MyCustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCustomViewHolder holder, int position) {

        try {

            String episodeId = allEpisodesJArray.getJSONObject(holder.getBindingAdapterPosition()).getString("episodeId");
            String episodeNum = allEpisodesJArray.getJSONObject(holder.getBindingAdapterPosition()).getString("episodeNum");

            holder.episodeButton.setText(episodeNum);

            holder.episodeButton.setOnClickListener(view -> {
                EpisodeDialogBuilder episodeDialogBuilder = new EpisodeDialogBuilder((Activity) context);
                episodeDialogBuilder.choosePlayServer(episodeId, animeTitle, animeId);
            });

            MyDatabaseHandler databaseHandler = new MyDatabaseHandler(context);

            if (episodeId.equals(databaseHandler.getLastWatchedEpisodeId(animeId))){
                holder.episodeButton.setTextColor(context.getColor(R.color.white));
                holder.episodeButton.setBackgroundColor(context.getColor(R.color.teal_500));
            } else{
                holder.episodeButton.setTextColor(context.getColor(R.color.teal_500));
                holder.episodeButton.setBackgroundColor(context.getColor(R.color.black));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }
    }

    @Override
    public int getItemCount() {
        return allEpisodesJArray.length();
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder{

        Button episodeButton;

        public MyCustomViewHolder(View itemView) {
            super(itemView);
            episodeButton = itemView.findViewById(R.id.episodeButton);
        }
    }
}
