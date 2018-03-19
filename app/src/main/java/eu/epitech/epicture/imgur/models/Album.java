package eu.epitech.epicture.imgur.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lucien on 05/02/18.
 * Reference: https://api.imgur.com/models/album
 */

public class Album {

    public String id;
    public String title;
    public String description;
    public int views;
    public Date date;
    public ArrayList<Image> images;
    public int imagesCount;

    public static Album fromJSON(JSONObject json) throws JSONException {
        Album album = new Album();

        album.id = json.getString("id");
        album.title = json.getString("title");
        album.description = json.getString("description");
        album.images = new ArrayList<>();
        album.views = json.getInt("views");
        album.date = new Date(json.getLong("datetime") * 1000);
        JSONArray images = json.getJSONArray("images");
        for (int i = 0; i < images.length(); i++) {
            album.images.add(Image.fromJSON(images.getJSONObject(i)));
        }
        album.imagesCount = json.getInt("images_count");

        return album;
    }

}
