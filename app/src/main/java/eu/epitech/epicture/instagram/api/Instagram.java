package eu.epitech.epicture.instagram.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import eu.epitech.epicture.instagram.models.GalleryItem;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by lucien on 09/02/18.
 */

public class Instagram {

    public class InitResponse {
        public String code;
    }

    public static final String HOST = "api.instagram.com";
    public static final String API_VERSION = "v1";
    public static final String REDIRECT_URI = "http://epicture.eu/auth/instagram";

    private String clientId;
    private String clientSecret;
    private String code;
    private String token;
    private long userId;
    private String userName;
    private String fullName;
    private String profilePicture;

    private OkHttpClient client;

    public Instagram (String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.client = new OkHttpClient.Builder().build();
    }

    /**
     * Get instagram code required to do any api call
     * @param context
     */
    public void init (Context context) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", clientId)
                .addQueryParameter("redirect_uri", REDIRECT_URI)
                .addQueryParameter("response_type", "code")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        context.startActivity(intent);
    }

    public void initCallback (String uri) {
        InitResponse res = parseInitResponse(uri);
        this.code = res.code;
    }

    /**
     * Get instagram access token
     * @param context
     */
    public CompletionStage<String> authenticate (Context context) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment("oauth")
                .addPathSegment("access_token")
                .build();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("client_id", clientId)
                .addFormDataPart("client_secret", clientSecret)
                .addFormDataPart("grant_type", "authorization_code")
                .addFormDataPart("redirect_uri", REDIRECT_URI)
                .addFormDataPart("code", this.code)
                .build();

        return post(url, body)
                .thenApply(new Function<JSONObject, String>() {
                    @Override
                    public String apply(JSONObject json) {
                        String message = "";

                        try {
                            if (json.has("error_message")) {
                                message = json.getString("error_message");
                            } else {
                                JSONObject user = json.getJSONObject("user");
                                Instagram.this.token = json.getString("access_token");
                                Instagram.this.userId = user.getLong("id");
                                Instagram.this.userName = user.getString("username");
                                Instagram.this.fullName = user.getString("full_name");
                                Instagram.this.profilePicture = user.getString("profile_picture");
                                message = "success";
                            }
                        } catch (Exception e) {
                            Log.e("Instagram.authenticate", e.getMessage());
                        }

                        return message;
                    }
                });
    }

    public CompletionStage<ArrayList<GalleryItem>> getRecent () {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(HOST)
                .addPathSegment(API_VERSION)
                .addPathSegment("users")
                .addPathSegment("self")
                .addPathSegment("media")
                .addPathSegment("recent")
                .addQueryParameter("access_token", this.token)
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
                            Log.e("Instagram.getRecent", e.getMessage());
                        }

                        return gallery;
                    }
                });
    }

    private CompletableFuture<JSONObject> get (HttpUrl url) {
        Request req = new Request.Builder()
                .url(url)
                .get()
                .build();

        return call(req);
    }

    private CompletableFuture<JSONObject> post (HttpUrl url, RequestBody body) {
        Request req = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return call(req);
    }

    private CompletableFuture<JSONObject> call (Request req) {
        final CompletableFuture<JSONObject> future = new CompletableFuture<>();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("instagram.call.onFailure", e.getMessage());
                future.cancel(false);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    future.complete(json);
                } catch (JSONException e) {
                    Log.e("instagram.call.onResponse", e.getMessage());
                    future.complete(null);
                }
            }
        });

        // Method: throws InterruptedException
        return future;
    }


    private InitResponse parseInitResponse (String uri) {
        InitResponse res = new InitResponse();
        HttpUrl url = HttpUrl.parse(uri);

        res.code = url.queryParameter("code");

        return res;
    }

    public boolean isAuthenticated () {
        return this.token != null;
    }

}
