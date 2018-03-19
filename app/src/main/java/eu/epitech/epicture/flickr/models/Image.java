package eu.epitech.epicture.flickr.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by lucien on 07/02/18.
 */

public class Image {

    public long id;
    public String link;
    public int server;
    public long farm;
    public String secret;
    public String title;
    public String owner;
    public String description;
    public Date date;
    public int views;

    public static Image fromJSON (JSONObject json) throws JSONException {
        Image image = new Image();

        image.id = json.getLong("id");
        image.farm = json.getLong("farm");
        image.server = json.getInt("server");
        image.secret = json.getString("secret");
        image.link = String.format("https://farm%d.staticflickr.com/%d/%d_%s.jpg",
                image.farm, image.server, image.id, image.secret);
        image.title = json.getJSONObject("title").getString("_content");
        image.owner = json.getJSONObject("owner").getString("username");
        image.description = json.getJSONObject("description").getString("_content");
        image.date = new Date(json.getLong("dateuploaded") * 1000);
        image.views = json.getInt("views");

        return image;
    }

}
