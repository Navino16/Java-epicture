package eu.epitech.epicture.imgur.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import eu.epitech.epicture.imgur.models.Album;
import eu.epitech.epicture.imgur.models.GalleryItem;
import eu.epitech.epicture.imgur.models.Image;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lucien on 05/02/18.
 * http://www.baeldung.com/java-completablefuture
 * http://www.deadcoderising.com/java8-writing-asynchronous-code-with-completablefuture/
 */

public class Imgur {

    public class CallbackResponse {
        public String accessToken;
        public long expiresIn;
        public String tokenType;
        public String refreshToken;
        public String accountUsername;
        public long accountId; // check
    }

    public static final String HOST = "api.imgur.com";
    public static final String API_VERSION = "3";

    public static final String SECTION_HOT = "hot";
    public static final String SECTION_TOP = "top";
    public static final String SECTION_USER = "user";

    public static final String SORT_TIME = "time";
    public static final String SORT_VIRAL = "viral";
    public static final String SORT_TOP = "top";

    public static final String WINDOW_DAY = "day";
    public static final String WINDOW_WEEK = "week";
    public static final String WINDOW_MONTH = "month";
    public static final String WINDOW_YEAR = "year";
    public static final String WINDOW_ALL = "all";

    private String clientId;
    private String clientSecret;
    private String token;
    private String accountUsername;

    private OkHttpClient client;

    public Imgur (String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.client = new OkHttpClient.Builder().build();
    }

    public void authenticate (Context context) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment("oauth2")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", clientId)
                .addQueryParameter("response_type", "token")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        context.startActivity(intent);
    }

    public void authenticateCallback (String uri) {
        CallbackResponse res = parseResponse(uri);
        this.token = res.accessToken;
        this.accountUsername = res.accountUsername;
    }

    public CompletionStage<Album> getAlbum (String id) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment(API_VERSION)
                .addPathSegment("album")
                .addPathSegment(id)
                .build();

        return get(url)
                .thenApply(new Function<JSONObject, Album>() {
                    @Override
                    public Album apply(JSONObject json) {
                        Album album = new Album();

                        try {
                            JSONObject data = json.getJSONObject("data");
                            album = Album.fromJSON(data);
                        } catch (Exception e) {
                            Log.e("Imgur.getAlbum", e.getMessage());
                        }

                        return album;
                    }
                });
    }

    public CompletionStage<ArrayList<GalleryItem>> getGallery (int page) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment(API_VERSION)
                .addPathSegment("gallery")
                .addPathSegment(SECTION_HOT)
                .addPathSegment(SORT_VIRAL)
                .addPathSegment(WINDOW_DAY)
                .addPathSegment(String.valueOf(page))
                .build();

        return get(url)
                .thenApply(new Function<JSONObject, ArrayList<GalleryItem>>() {
                    @Override
                    public ArrayList<GalleryItem> apply(JSONObject json) {
                        ArrayList<GalleryItem> gallery = new ArrayList<>();

                        try {
                            JSONArray items = json.getJSONArray("data");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                gallery.add(GalleryItem.fromJSON(obj));
                            }
                        } catch (Exception e) {
                            Log.e("Imgur.getGallery", e.getMessage());
                        }

                        return gallery;
                    }
                });
    }

    public CompletionStage<ArrayList<GalleryItem>> search (int page, String query) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment(API_VERSION)
                .addPathSegment("gallery")
                .addPathSegment("search")
                .addPathSegment(SORT_VIRAL)
                .addPathSegment(WINDOW_ALL)
                .addPathSegment(String.valueOf(page))
                .addQueryParameter("q", query)
                .build();

        return get(url)
                .thenApply(new Function<JSONObject, ArrayList<GalleryItem>>() {
                    @Override
                    public ArrayList<GalleryItem> apply(JSONObject json) {
                        ArrayList<GalleryItem> gallery = new ArrayList<>();

                        try {
                            JSONArray items = json.getJSONArray("data");
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                gallery.add(GalleryItem.fromJSON(obj));
                            }
                        } catch (Exception e) {
                            Log.e("Imgur.search", e.getMessage());
                        }

                        return gallery;
                    }
                });
    }

    public CompletionStage<Image> getImage (String id) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment(API_VERSION)
                .addPathSegment("image")
                .addPathSegment(id)
                .build();

        return get(url)
                .thenApply(new Function<JSONObject, Image>() {
                    @Override
                    public Image apply(JSONObject json) {
                        Image image = new Image();

                        try {
                            JSONObject data = json.getJSONObject("data");
                            image = Image.fromJSON(data);
                        } catch (Exception e) {
                            Log.e("Imgur.getImage", e.getMessage());
                        }

                        return image;
                    }
                });
    }

    public CompletionStage<String> upload (String title, String description, Bitmap image) throws UnsupportedOperationException {
        if (token == null) {
            throw new UnsupportedOperationException("No access token. You need to authenticate first");
        }
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment(API_VERSION)
                .addPathSegment("image")
                .build();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", bitmapToBase64(image))
                .addFormDataPart("title", title)
                .addFormDataPart("description", description)
                .addFormDataPart("name", "truc.png")
                .build();

        return post(url, body)
                .thenApply(new Function<JSONObject, String>() {
                    @Override
                    public String apply(JSONObject json) {
                        String message = "";

                        try {
                            if (json.getBoolean("success")) {
                                message = "Image uploaded successfully";
                            } else {
                                JSONObject data = json.getJSONObject("data");
                                if (data.has("error")) {
                                    message = data.getString("error");
                                } else {
                                    message = "Unknown error";
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Imgur.upload", e.getMessage());
                        }

                        return message;
                    }
                });
    }

    private CompletableFuture<JSONObject> get (HttpUrl url) {
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Client-ID " + this.clientId)
                .header("User-Agent", "Epicture")
                .get()
                .build();

        return call(req);
    }

    private CompletableFuture<JSONObject> post (HttpUrl url, RequestBody body) {
        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Client-ID " + this.clientId)
                .header("Authorization", "Bearer " + this.token)
                .header("User-Agent", "Epicture")
                .post(body)
                .build();

        return call(req);
    }

    /**
     * Make a general call to imgur API
     * @param req
     * @return json response
     */
    private CompletableFuture<JSONObject> call (Request req) {
        final CompletableFuture<JSONObject> future = new CompletableFuture<>();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Imgur.call.onFailure", e.getMessage());
                future.cancel(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    if (json.getBoolean("success")) {
                        future.complete(json);
                    } else {
                        future.cancel(false);
                    }
                } catch (JSONException e) {
                    Log.e("Imgur.call.onResponse", e.getMessage());
                    future.complete(null);
                }
            }
        });

        // Method: throws InterruptedException
        return future;
    }

    private CallbackResponse parseResponse (String uri) {
        CallbackResponse res = new CallbackResponse();
        String[] parameters = uri.split("#")[1].split("&");

        res.accessToken = parameters[0].split("=")[1];
        res.expiresIn = Long.valueOf(parameters[1].split("=")[1]);
        res.tokenType = parameters[2].split("=")[1];
        res.refreshToken = parameters[3].split("=")[1];
        res.accountUsername = parameters[4].split("=")[1];
        res.accountId = Long.valueOf(parameters[5].split("=")[1]);

        return res;
    }

    private String bitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes;

        bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        bytes = out.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
