package eu.epitech.epicture.instagram.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by lucien on 09/02/18.
 */

public class GalleryItem {

    public enum Type {
        IMAGE,
        VIDEO
    }

    public String id;
    public String caption;
    public int likes;
    public String image;
    public String video;
    public Type type;
    public Date date;

    public static GalleryItem fromJSON(JSONObject json) throws JSONException {
        GalleryItem item = new GalleryItem();

        item.id = json.getString("id");
        item.caption = (json.get("caption") != JSONObject.NULL)
                ? json.getJSONObject("caption").getString("text")
                : "";
        item.likes = json.getJSONObject("likes").getInt("count");
        item.image = json.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
        item.type = Type.valueOf(json.getString("type").toUpperCase());
        item.date = new Date(json.getLong("created_time") * 1000);

        if (item.type == Type.VIDEO) {
            item.video = json.getJSONObject("videos").getJSONObject("standard_resolution").getString("url");
        } else {
            item.video = null;
        }

        return item;
    }

}
