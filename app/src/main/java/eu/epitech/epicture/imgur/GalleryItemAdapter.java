package eu.epitech.epicture.imgur;

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
import eu.epitech.epicture.imgur.models.GalleryItem;

/**
 * Created by lucien on 05/02/18.
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
        item.description = (item.description.equals("null") ? res.getString(R.string.txt_no_description) : item.description);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.imgur_gallery_item_card, parent, false);
        }

        // Get views
        final ImageView image = convertView.findViewById(R.id.imgur_gallery_item_card_img_image);
        final ImageView type = convertView.findViewById(R.id.imgur_gallery_item_card_txt_album);
        final TextView title = convertView.findViewById(R.id.imgur_gallery_item_card_txt_title);
        final TextView description = convertView.findViewById(R.id.imgur_gallery_item_card_txt_description);
        final TextView views = convertView.findViewById(R.id.imgur_gallery_item_card_txt_views);
        final TextView date = convertView.findViewById(R.id.imgur_gallery_item_card_txt_date);

        // Fill views
        Glide.with(getContext()).load(item.link).into(image); // Get image
        title.setText(item.title);
        description.setText(item.description);
        type.setImageResource(item.album ? R.drawable.ic_collections_black_24dp : R.drawable.ic_photo_black_24dp);
        views.setText(res.getString(R.string.txt_views, item.views));
        date.setText(res.getString(R.string.txt_submitted, format.format(item.date)));

        return convertView;
    }

}
