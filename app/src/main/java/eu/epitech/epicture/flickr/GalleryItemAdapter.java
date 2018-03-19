package eu.epitech.epicture.flickr;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import eu.epitech.epicture.R;
import eu.epitech.epicture.flickr.models.GalleryItem;

/**
 * Created by lucien on 07/02/18.
 */

public class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {

    private final SimpleDateFormat format = new SimpleDateFormat();
    private final Resources res;

    public GalleryItemAdapter(Context context, ArrayList<GalleryItem> objects) {
        super(context, 0, objects);
        res = context.getResources();
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        final GalleryItem item = getItem(position);

        // Transform
        item.title = (item.title.equals("null") ? res.getString(R.string.txt_untitled) : item.title);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.flickr_gallery_item_card, parent, false);
        }

        // Get views
        final ImageView image = convertView.findViewById(R.id.photo_card_img_image);
        final TextView title = convertView.findViewById(R.id.photo_card_txt_title);

        // Fill views
        Glide.with(getContext()).load(item.link).into(image); // Get image
        title.setText(item.title);

        return convertView;
    }

}
