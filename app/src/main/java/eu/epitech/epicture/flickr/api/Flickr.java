package eu.epitech.epicture.flickr.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import eu.epitech.epicture.flickr.models.GalleryItem;
import eu.epitech.epicture.flickr.models.Image;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lucien on 07/02/18.
 */

public class Flickr {

    public static final String HOST = "api.flickr.com";

    private String clientId;
    private String clientSecret;

    private OkHttpClient client;

    public Flickr (String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.client = new OkHttpClient.Builder().build();
    }

    public CompletionStage<ArrayList<GalleryItem>> getRecent (int page) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegments("services/rest")
                .addQueryParameter("method", "flickr.photos.getRecent")
                .addQueryParameter("api_key", this.clientId)
                .addQueryParameter("format", "json")
                .addQueryParameter("nojsoncallback", "1")
                .build();

        return call(url)
                .thenApply(new Function<JSONObject, ArrayList<GalleryItem>>() {
                    @Override
                    public ArrayList<GalleryItem> apply(JSONObject json) {
                        ArrayList<GalleryItem> gallery = new ArrayList<>();

                        try {
                            JSONObject data = json.getJSONObject("photos");
                            JSONArray items = data.getJSONArray("photo");

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                gallery.add(GalleryItem.fromJSON(obj));
                            }
                        } catch (Exception e) {
                            Log.e("Flickr.getRecent", e.getMessage());
                        }

                        return gallery;
                    }
                })
                // If an error occurs, return an empty list of photos
                .exceptionally(new Function<Throwable, ArrayList<GalleryItem>>() {
                    @Override
                    public ArrayList<GalleryItem> apply(Throwable throwable) {
                        Log.e("Flickr.getRecent.exceptionnaly", throwable.getMessage());
                        return new ArrayList<>();
                    }
                });
    }

    public CompletionStage<ArrayList<GalleryItem>> search (int page, String query) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegments("services/rest")
                .addQueryParameter("method", "flickr.photos.search")
                .addQueryParameter("page", String.valueOf(page))
                .addQueryParameter("text", query)
                .addQueryParameter("api_key", this.clientId)
                .addQueryParameter("format", "json")
                .addQueryParameter("nojsoncallback", "1")
                .build();

        return call(url)
                .thenApply(new Function<JSONObject, ArrayList<GalleryItem>>() {
                    @Override
                    public ArrayList<GalleryItem> apply(JSONObject json) {
                        ArrayList<GalleryItem> gallery = new ArrayList<>();

                        try {
                            JSONObject data = json.getJSONObject("photos");
                            JSONArray items = data.getJSONArray("photo");

                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                gallery.add(GalleryItem.fromJSON(obj));
                            }
                        } catch (Exception e) {
                            Log.e("Flickr.search", e.getMessage());
                        }

                        return gallery;
                    }
                })
                // If an error occurs, return an empty list of photos
                .exceptionally(new Function<Throwable, ArrayList<GalleryItem>>() {
                    @Override
                    public ArrayList<GalleryItem> apply(Throwable throwable) {
                        Log.e("Flickr.search.exceptionnaly", throwable.getMessage());
                        return new ArrayList<>();
                    }
                });
    }

    public CompletionStage<Image> getPhoto (long id) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegments("services/rest")
                .addQueryParameter("method", "flickr.photos.getInfo")
                .addQueryParameter("photo_id", String.valueOf(id))
                .addQueryParameter("api_key", this.clientId)
                .addQueryParameter("format", "json")
                .addQueryParameter("nojsoncallback", "1")
                .build();

        return call(url)
                .thenApply(new Function<JSONObject, Image>() {
                    @Override
                    public Image apply(JSONObject json) {
                        Image image = new Image();

                        try {
                            JSONObject data = json.getJSONObject("photo");

                            image = Image.fromJSON(data);
                        } catch (Exception e) {
                            Log.e("Flickr.getRecent", e.getMessage());
                        }

                        return image;
                    }
                });
    }

    private CompletableFuture<JSONObject> call (HttpUrl url) {
        final CompletableFuture<JSONObject> future = new CompletableFuture<>();
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", "Epicture")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Flickr.call.onFailure", e.getMessage());
                future.cancel(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    Log.d("Flickr.call.onResponse", json.toString());
                    if (json.getString("stat").equals("ok")) {
                        future.complete(json);
                    } else {
                        future.cancel(false);
                    }
                } catch (JSONException e) {
                    Log.e("Flickr.call.onResponse", e.getMessage());
                    future.cancel(false);
                }
            }
        });

        // Method: throws InterruptedException
        return future;
    }

}
