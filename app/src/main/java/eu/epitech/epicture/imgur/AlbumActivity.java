package eu.epitech.epicture.imgur;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.function.Consumer;

import eu.epitech.epicture.R;
import eu.epitech.epicture.imgur.api.Constants;
import eu.epitech.epicture.imgur.api.Imgur;
import eu.epitech.epicture.imgur.models.Album;
import eu.epitech.epicture.imgur.models.Image;

public class AlbumActivity extends AppCompatActivity {

    private Imgur api;

    private TextView title;
    private ListView images;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imgur_activity_album);

        title = findViewById(R.id.imgur_album_txt_title);
        images = findViewById(R.id.imgur_album_list_images);
        loading = findViewById(R.id.imgur_album_bar_loading);

        String id = getIntent().getStringExtra("id");

        api = new Imgur(Constants.CLIENT_ID, Constants.CLIENT_SECRET);

        setTitle("Album");

        // Listeners
        images.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Image image = (Image) images.getItemAtPosition(i);
                Intent intent = new Intent(AlbumActivity.this, ImageActivity.class);
                intent.putExtra("id", image.id);
                AlbumActivity.this.startActivity(intent);
            }
        });

        setup(id);
    }

    /**
     * Asynchronously set up UI according to album
     * @param id Album ID
     */
    private void setup (final String id) {
        api.getAlbum(id)
                .thenAccept(new Consumer<Album>() {
                    @Override
                    public void accept(final Album album) {
                        Log.d("AlbumA.setup", "Setup album");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Transform
                                album.title = (album.title.equals("null") ? getResources().getString(R.string.txt_untitled) : album.title);
                                album.description = (album.description.equals("null") ? getResources().getString(R.string.txt_no_description) : album.description);

                                title.setText(album.title);
                                images.setAdapter(new ImageAdapter(AlbumActivity.this, album.images));
                                loading.setVisibility(View.GONE);
                            }
                        });
                    }
                });
    }

}
