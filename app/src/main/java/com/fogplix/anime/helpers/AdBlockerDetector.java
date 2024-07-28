package com.fogplix.anime.helpers;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdBlockerDetector {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AdBlockerCallback {
        void onResult(boolean isAdBlockerDetected);
    }

    public static void detectAdBlocker(final AdBlockerCallback callback) {
        executor.execute(() -> {
            final boolean isBlocked = checkAdBlocker();
            mainHandler.post(() -> callback.onResult(isBlocked));
        });
    }

    private static boolean checkAdBlocker() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://googleads.g.doubleclick.net");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true); // Automatically follow redirects

            int responseCode = connection.getResponseCode();

            // Check for redirect responses
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308) {

                String redirectUrl = connection.getHeaderField("Location");
                if (redirectUrl != null && !redirectUrl.isEmpty()) {
                    // If redirected to a different domain, consider it blocked
                    URL redirectedUrl = new URL(redirectUrl);
                    return !redirectedUrl.getHost().equals(url.getHost());
                }
                return true; // Consider it blocked if there's a redirect without a valid location
            }

            // Check if the response is either 200 (OK) or 404 (Not Found)
            return responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NOT_FOUND && responseCode != HttpURLConnection.HTTP_FORBIDDEN;

        } catch (IOException e) {
            return true; // Connection failed, assume ad blocker is active
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}