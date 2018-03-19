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
import eu.epitech.epicture.imgur.models.Image;

/**
 * Created by lucien on 05/02/18.
 */

public class ImageAdapter extends ArrayAdapter<Image> {

    private final SimpleDateFormat format = new SimpleDateFormat();
    private final Resources res;

    public ImageAdapter(Context context, ArrayList<Image> objects) {
        super(context, 0, objects);
        res = context.getResources();
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        final Image item = getItem(position);

        // Transform
        item.title = (item.title.equals("null") ? res.getString(R.string.txt_untitled) : item.title);
        item.description = (item.description.equals("null") ? res.getString(R.string.txt_no_description) : item.description);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.imgur_image_card, parent, false);
        }

        // Get views
        ImageView image = convertView.findViewById(R.id.imgur_image_card_img_image);
        final TextView title = convertView.findViewById(R.id.imgur_image_card_txt_title);
        final TextView description = convertView.findViewById(R.id.imgur_image_card_txt_description);
        final TextView views = convertView.findViewById(R.id.imgur_image_card_txt_views);
        final TextView date = convertView.findViewById(R.id.imgur_image_card_txt_date);

        // Fill views
        Glide.with(getContext()).load(item.link).into(image);
        title.setText(item.title);
        description.setText(item.description);
        views.setText(res.getString(R.string.txt_views, item.views));
        date.setText(res.getString(R.string.txt_submitted, format.format(item.date)));

        return convertView;
    }

}
