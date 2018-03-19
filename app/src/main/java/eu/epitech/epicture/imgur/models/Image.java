package eu.epitech.epicture.imgur.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by lucien on 05/02/18.
 * Reference: https://api.imgur.com/models/image
 */

public class Image {

    public String id;
    public String title;
    public String description;
    public Date date;
    public String type;
    public boolean animated;
    public int width;
    public int height;
    public int size;
    public int views;
    public String section;
    public String link;
    public boolean favorite;
    public boolean nsfw;

    public static Image fromJSON(JSONObject json) throws JSONException {
        Image image = new Image();

        Log.d("Image.fromJSON", json.toString());
        image.id = json.getString("id");
        image.title = json.getString("title");
        image.description = json.getString("description");
        image.date = new Date(json.getLong("datetime") * 1000);
        image.type = json.getString("type");
        image.animated = json.getBoolean("animated");
        image.width = json.getInt("width");
        image.height = json.getInt("height");
        image.size = json.getInt("size");
        image.views = json.getInt("views");
        image.section = json.getString("section");
        image.link = json.getString("link");
        // image.favorite = json.getString("favorite");
        image.nsfw = !(json.getString("nsfw").equals("null"));

        return image;
    }

}
