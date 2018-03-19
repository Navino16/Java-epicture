package eu.epitech.epicture.instagram;

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
import eu.epitech.epicture.instagram.models.GalleryItem;

/**
 * Created by lucien on 09/02/18.
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

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.instagram_gallery_item_card, parent, false);
        }

        // Get views
        final ImageView image = convertView.findViewById(R.id.instagram_gallery_item_card_img_image);
        final ImageView type = convertView.findViewById(R.id.instagram_gallery_item_card_img_type);
        final TextView caption = convertView.findViewById(R.id.instagram_gallery_item_card_txt_caption);
        final TextView likes = convertView.findViewById(R.id.instagram_gallery_item_card_txt_likes);
        final TextView date = convertView.findViewById(R.id.instagram_gallery_item_card_txt_date);

        // Fill views
        Glide.with(getContext()).load(item.image).into(image); // Get image
        caption.setText(item.caption);
        type.setImageResource(item.type == GalleryItem.Type.VIDEO ? R.drawable.ic_videocam_black_24dp : R.drawable.ic_photo_black_24dp);
        likes.setText(res.getString(R.string.txt_likes, item.likes));
        date.setText(res.getString(R.string.txt_submitted, format.format(item.date)));

        return convertView;
    }


}
