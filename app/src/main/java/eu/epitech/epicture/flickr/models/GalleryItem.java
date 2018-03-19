package eu.epitech.epicture.flickr.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lucien on 07/02/18.
 */

public class GalleryItem {

    public long id;
    public String link;
    public int server;
    public long farm;
    public String secret;
    public String title;

    public static GalleryItem fromJSON (JSONObject json) throws JSONException {
        GalleryItem item = new GalleryItem();

        item.id = json.getLong("id");
        item.farm = json.getLong("farm");
        item.server = json.getInt("server");
        item.secret = json.getString("secret");
        item.link = String.format("https://farm%d.staticflickr.com/%d/%d_%s.jpg",
                item.farm, item.server, item.id, item.secret);
        item.title = json.getString("title");

        return item;
    }

}
