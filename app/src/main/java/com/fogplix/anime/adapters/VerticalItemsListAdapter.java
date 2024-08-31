package com.fogplix.anime.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fogplix.anime.R;
import com.fogplix.anime.activities.DetailsActivity;
import com.fogplix.anime.helpers.CustomMethods;
import com.fogplix.anime.helpers.MyDatabaseHandler;
import com.fogplix.anime.model.AnimeFavoriteListModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;

public class VerticalItemsListAdapter extends RecyclerView.Adapter<VerticalItemsListAdapter.MyCustomViewHolder> {

    private final JSONArray allAnime;
    private final Activity activity;
    private final boolean isHorizontalView;
    private final String TAG = "MADARA";

    public VerticalItemsListAdapter(Activity activity, JSONArray allAnime, boolean isHorizontalView) {
        this.allAnime = allAnime;
        this.activity = activity;
        this.isHorizontalView = isHorizontalView;
    }

    @NonNull
    @Override
    public MyCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view;

        if (isHorizontalView) {
            view = layoutInflater.inflate(R.layout.sample_horizontal_item_card, parent, false);
        } else {
            view = layoutInflater.inflate(R.layout.sample_item_card_design, parent, false);
        }

        return new MyCustomViewHolder(view);
    }

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyCustomViewHolder holder, int position) {

        try {
            String animeTitle = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeTitle");

            holder.animeNameTV.setText(animeTitle);

            if (allAnime.getJSONObject(holder.getBindingAdapterPosition()).has("subOrDub")) {

                if (allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("subOrDub").equalsIgnoreCase("DUB")) {
                    holder.isAnimeSubDubTV.setText("dub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.green));
                } else {
                    holder.isAnimeSubDubTV.setText("sub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.fade_blue));
                }
            } else {
                if (animeTitle.toLowerCase().contains("(dub)")) {
                    holder.isAnimeSubDubTV.setText("dub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.green));
                } else {
                    holder.isAnimeSubDubTV.setText("sub");
                    holder.isAnimeSubDubTV.setBackgroundColor(activity.getColor(R.color.fade_blue));
                }
            }

            Glide.with(activity)
                    .load(allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeImg"))
                    .placeholder(R.drawable.preload_thumb)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.imageView);

            if (allAnime.getJSONObject(holder.getBindingAdapterPosition()).has("releasedDate")) {

                holder.releaseDateTV.setVisibility(View.VISIBLE);
                holder.releaseDateTV.setText(allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("releasedDate"));
            }
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
            Toast.makeText(activity, "JSON Error", Toast.LENGTH_SHORT).show();
            CustomMethods.errorAlert(activity, "Error", e.getMessage(), "OK", true);
        }

        String finalServer = "gogo";

        holder.itemView.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(activity, DetailsActivity.class);
                intent.putExtra("animeId", allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeId"));
                intent.putExtra("episodeId", allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("episodeId"));
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: ", e);
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        holder.itemView.setOnLongClickListener(view -> {

            try {

                String animeId = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeId");
                String animeTitle = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeTitle");
                String animeImageUrl = allAnime.getJSONObject(holder.getBindingAdapterPosition()).getString("animeImg");

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity, R.style.BottomSheetDialog);
                bottomSheetDialog.setCancelable(true);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetDialog.setContentView(R.layout.sample_add_favorite_bottomsheet_layout);

                TextView animeTvInBottomSheet = bottomSheetDialog.findViewById(R.id.anime_title_tv);
                CardView addToFavActionCV = bottomSheetDialog.findViewById(R.id.add_to_favorite_action_cv);
                CardView watchNowActionCV = bottomSheetDialog.findViewById(R.id.watch_now_action_cv);

                if (animeTvInBottomSheet != null && addToFavActionCV != null && watchNowActionCV != null) {

                    animeTvInBottomSheet.setText(animeTitle);

                    //------------------------------------------------------------------------------

                    addToFavActionCV.setOnClickListener(v -> {

                        MyDatabaseHandler handler = new MyDatabaseHandler(activity);

                        AnimeFavoriteListModel favoriteListModel = handler.getAnimeFromFavorite(animeId);

                        if (!favoriteListModel.getAnimeId().equalsIgnoreCase("")) {
                            Toast.makeText(activity, "Already added to favorite.", Toast.LENGTH_SHORT).show();
                        } else {
                            favoriteListModel.setAnimeId(animeId);
                            favoriteListModel.setAnimeName(animeTitle);
                            favoriteListModel.setAnimeImageUrl(animeImageUrl);
                            favoriteListModel.setAnimeServer(finalServer);

                            handler.addAnimeToFavorite(favoriteListModel);
                            Toast.makeText(activity, "Added to favorite list.", Toast.LENGTH_SHORT).show();
                        }

                        bottomSheetDialog.dismiss();
                    });

                    //------------------------------------------------------------------------------

                    watchNowActionCV.setOnClickListener(v -> {

                        Intent intent = new Intent(activity, DetailsActivity.class);
                        intent.putExtra("animeId", animeId);
                        intent.putExtra("server", finalServer);
                        activity.startActivity(intent);

                        bottomSheetDialog.dismiss();
                    });

                    bottomSheetDialog.show();
                }
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: ", e);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return allAnime.length();
    }

    public static class MyCustomViewHolder extends RecyclerView.ViewHolder {
        CardView rootView;
        ImageView imageView;
        TextView animeNameTV, isAnimeSubDubTV, releaseDateTV;

        public MyCustomViewHolder(View itemView) {
            super(itemView);

            rootView = itemView.findViewById(R.id.rootCardView);
            imageView = itemView.findViewById(R.id.thumb_img_view);
            animeNameTV = itemView.findViewById(R.id.anime_title_tv);
            isAnimeSubDubTV = itemView.findViewById(R.id.is_anime_sub_dub_tv);
            releaseDateTV = itemView.findViewById(R.id.releaseDateTV);
        }
    }
}
