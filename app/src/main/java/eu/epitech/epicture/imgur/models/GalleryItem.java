package eu.epitech.epicture.imgur.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by lucien on 06/02/18.
 */

public class GalleryItem {

    public String id;
    public String title;
    public String description;
    public Date date;
    public String link;
    public boolean album;
    public int views;

    public static GalleryItem fromJSON(JSONObject json) throws JSONException {
        GalleryItem item = new GalleryItem();

        item.id = json.getString("id");
        item.title = json.getString("title");
        item.description = json.getString("description");
        item.date = new Date(json.getLong("datetime") * 1000);
        item.link = json.getString("link");
        item.album = json.getBoolean("is_album");
        item.views = json.getInt("views");

        // If item is an album, set link to its cover image
        if (item.album) {
            JSONArray images = json.getJSONArray("images");
            item.link = images.getJSONObject(0).getString("link");
        }

        return item;
    }

}
