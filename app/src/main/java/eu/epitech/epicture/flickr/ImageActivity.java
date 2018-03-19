package eu.epitech.epicture.flickr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.function.Consumer;

import eu.epitech.epicture.R;
import eu.epitech.epicture.flickr.api.Flickr;
import eu.epitech.epicture.flickr.api.Constants;
import eu.epitech.epicture.flickr.models.Image;

/**
 * Created by lucien on 07/02/18.
 */

public class ImageActivity extends AppCompatActivity {

    private Flickr api;

    private ImageView _image;
    private TextView title;
    private TextView owner;
    private TextView description;
    private TextView views;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flickr_activity_image);

        // Get views
        _image = findViewById(R.id.flickr_image_img_image);
        title = findViewById(R.id.flickr_image_txt_title);
        owner = findViewById(R.id.flickr_image_txt_owner);
        description = findViewById(R.id.flickr_image_txt_description);
        views = findViewById(R.id.flickr_image_txt_views);
        loading = findViewById(R.id.flickr_image_bar_loading);

        // Get the photo
        long id = getIntent().getLongExtra("id", -1);

        api = new Flickr(Constants.CLIENT_ID, Constants.CLIENT_SECRET);

        setTitle("Image");

        setup(id);
    }

    /**
     * Asynchronously set up UI according to image
     * @param id Image ID
     */
    private void setup (long id) {
        api.getPhoto(id).thenAccept(new Consumer<Image>() {
            @Override
            public void accept(final Image image) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Transform
                        image.title = (image.title.equals("null") ? getResources().getString(R.string.txt_untitled) : image.title);
                        image.description = (image.description.equals("null") ? getResources().getString(R.string.txt_no_description) : image.description);

                        Glide.with(ImageActivity.this)
                                .load(image.link)
                                .into(ImageActivity.this._image);
                        title.setText(image.title);
                        owner.setText(getResources().getString(R.string.txt_by, image.owner));
                        description.setText(image.description);
                        views.setText(getResources().getString(R.string.txt_views, image.views));

                        _image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(image.link), "image/jpeg");
                                startActivity(intent);
                            }
                        });
                        loading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

}
