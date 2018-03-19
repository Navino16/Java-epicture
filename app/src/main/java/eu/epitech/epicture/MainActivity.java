package eu.epitech.epicture;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.function.Consumer;

import eu.epitech.epicture.flickr.GalleryItemAdapter;
import eu.epitech.epicture.flickr.api.Flickr;
import eu.epitech.epicture.imgur.api.Imgur;
import eu.epitech.epicture.instagram.api.Instagram;

public class MainActivity extends AppCompatActivity {

    private Imgur imgurApi;
    private Flickr flickrApi;
    private Instagram instagramApi;

    private ProgressBar loading;
    private SwipeRefreshLayout swipe;
    private ListView images;
    private FloatingActionButton fab;

    private int currentTab;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigation
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        currentTab = item.getItemId();
                        reset();
                        setup();
                        return true;
                    }
                });

        // Views
        loading = findViewById(R.id.main_bar_loading);
        swipe = findViewById(R.id.main_swipe_images);
        images = findViewById(R.id.main_list_images);
        fab = findViewById(R.id.main_fab_upload);

        // Init
        imgurApi = new Imgur(
                eu.epitech.epicture.imgur.api.Constants.CLIENT_ID,
                eu.epitech.epicture.imgur.api.Constants.CLIENT_SECRET
        );
        flickrApi = new Flickr(
                eu.epitech.epicture.flickr.api.Constants.CLIENT_ID,
                eu.epitech.epicture.flickr.api.Constants.CLIENT_SECRET
        );
        instagramApi = new Instagram(
                eu.epitech.epicture.instagram.api.Constants.CLIENT_ID,
                eu.epitech.epicture.instagram.api.Constants.CLIENT_SECRET
        );
        currentTab = R.id.nav_main_imgur;
        res = getResources();

        // Listeners
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshImages();
            }
        });
        images.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                Intent intent = new Intent();

                switch (currentTab) {
                    // Click on imgur gallery item
                    case R.id.nav_main_imgur: {
                        eu.epitech.epicture.imgur.models.GalleryItem image =
                                (eu.epitech.epicture.imgur.models.GalleryItem) adapter.getItemAtPosition(position);
                        intent.setClass(MainActivity.this, image.album
                                ? eu.epitech.epicture.imgur.AlbumActivity.class
                                : eu.epitech.epicture.imgur.ImageActivity.class);
                        intent.putExtra("id", image.id);
                        break;
                    }
                    // Click on flickr photo
                    case R.id.nav_main_flickr: {
                        eu.epitech.epicture.flickr.models.GalleryItem image =
                                (eu.epitech.epicture.flickr.models.GalleryItem) adapter.getItemAtPosition(position);
                        intent.setClass(MainActivity.this,
                                eu.epitech.epicture.flickr.ImageActivity.class);
                        intent.putExtra("id", image.id);
                        break;
                    }
                    // Click on instagram photo
                    case R.id.nav_main_instagram: {
                        eu.epitech.epicture.instagram.models.GalleryItem image =
                                (eu.epitech.epicture.instagram.models.GalleryItem) adapter.getItemAtPosition(position);
                        intent.setAction(Intent.ACTION_VIEW);
                        if (image.type == eu.epitech.epicture.instagram.models.GalleryItem.Type.VIDEO){
                            intent.setDataAndType(Uri.parse(image.video), "video/*");
                        } else {
                            intent.setDataAndType(Uri.parse(image.image), "image/*");
                        }

                        break;
                    }
                }
                startActivity(intent);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prompt user to select a file
                FileService.showFileChooser(MainActivity.this);
            }
        });

        setup();
        // Fab
    }

    /**
     * Create and init toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        // https://developer.android.com/training/search/setup.html#create-sc
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_main_action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName())
        );

        return true;
    }

    /**
     * React on click in toolbar item
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_action_authenticate:
                authenticate();
                return true;
            case R.id.menu_main_action_refresh:
                swipe.setRefreshing(true);
                refreshImages();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * React to intent
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_SEARCH:
                actionSearch(intent);
                break;
            case Intent.ACTION_VIEW:
                actionView(intent);
                break;
        }
    }

    /**
     * React to search
     */
    private void actionSearch (Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        reset();
        switch (currentTab) {
            case R.id.nav_main_imgur:
                imgurApi.search(1, query)
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.imgur.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.imgur.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new eu.epitech.epicture.imgur.GalleryItemAdapter(MainActivity.this, gallery));
                                    }
                                });
                            }
                        });
                break;
            case R.id.nav_main_flickr:
                flickrApi.search(1, query)
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.flickr.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.flickr.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new GalleryItemAdapter(MainActivity.this, gallery));
                                    }
                                });
                            }
                        });
                break;
            case R.id.nav_main_instagram:
                break;
        }
    }

    /**
     * Auth callback
     */
    private void actionView (Intent intent) {
        Uri uri = intent.getData();
        switch (currentTab) {
            case R.id.nav_main_imgur:
                imgurApi.authenticateCallback(uri.toString());
                Toast.makeText(MainActivity.this, res.getString(R.string.toast_auth_ok, res.getString(R.string.nav_imgur)), Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_main_instagram:
                instagramApi.initCallback(uri.toString());
                instagramApi.authenticate(this)
                        .thenAccept(new Consumer<String>() {
                            @Override
                            public void accept(final String message) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (message.equals("success")) {
                                            Toast.makeText(MainActivity.this, res.getString(R.string.toast_auth_ok, res.getString(R.string.nav_instagram)), Toast.LENGTH_LONG).show();
                                            reset();
                                            setup();
                                        } else {
                                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });
                break;
        }

    }

    /**
     * Gets called after user selected a file to upload
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imgurApi.upload("Title", "description", bmp)
                            .thenAccept(new Consumer<String>() {
                                @Override
                                public void accept(final String message) {
                                    Log.i("MA.onActivityresult", message);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                } catch (Exception e) {
                    Log.e("MA.onActivityResult", e.getMessage());
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                //showImage(uri);
            }
        }
    }


    /**
     * Authenticate depending on current api
     */
    private void authenticate () {
        switch (currentTab) {
            case R.id.nav_main_imgur:
                imgurApi.authenticate(this);
                break;
            case R.id.nav_main_flickr:
                Toast.makeText(this, res.getString(R.string.toast_auth_notimpl, res.getString(R.string.nav_flickr)), Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_main_instagram:
                instagramApi.init(this);
                break;
        }
    }

    /**
     * Reset views and start loading animation
     */
    private void reset () {
        images.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        if (currentTab != R.id.nav_main_imgur) {
            fab.setVisibility(View.GONE);
        }
    }

    /**
     * Set title to current nav
     * Update listview with corresponding data
     */
    private void setup () {
        switch (currentTab) {
            case R.id.nav_main_imgur:
                setTitle("Imgur");
                fab.setVisibility(View.VISIBLE);
                imgurApi.getGallery(1)
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.imgur.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.imgur.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new eu.epitech.epicture.imgur.GalleryItemAdapter(MainActivity.this, gallery));
                                    }
                                });
                            }
                        });
                break;
            case R.id.nav_main_flickr:
                setTitle("Flickr");
                flickrApi.getRecent(1)
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.flickr.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.flickr.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new eu.epitech.epicture.flickr.GalleryItemAdapter(MainActivity.this, gallery));
                                    }
                                });
                            }
                        });
                break;
            case R.id.nav_main_instagram:
                setTitle("Instagram");
                if (instagramApi.isAuthenticated()) {
                    instagramApi.getRecent()
                            .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.instagram.models.GalleryItem>>() {
                                @Override
                                public void accept(final ArrayList<eu.epitech.epicture.instagram.models.GalleryItem> gallery) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.this.update(new eu.epitech.epicture.instagram.GalleryItemAdapter(MainActivity.this, gallery));
                                        }
                                    });
                                }
                            });
                } else {
                    update(new eu.epitech.epicture.instagram.GalleryItemAdapter(this, new ArrayList<eu.epitech.epicture.instagram.models.GalleryItem>()));
                    Toast.makeText(this, res.getString(R.string.toast_please_auth, res.getString(R.string.nav_instagram)), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Update list data, then stop swipe layout animation
     */
    private void refreshImages () {
        switch (currentTab) {
            case R.id.nav_main_imgur:
                imgurApi.getGallery(1)
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.imgur.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.imgur.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new eu.epitech.epicture.imgur.GalleryItemAdapter(MainActivity.this, gallery));
                                        swipe.setRefreshing(false);
                                    }
                                });
                            }
                        });
                break;
            case R.id.nav_main_flickr:
                flickrApi.getRecent(1)
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.flickr.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.flickr.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new GalleryItemAdapter(MainActivity.this, gallery));
                                        swipe.setRefreshing(false);
                                    }
                                });
                            }
                        });
                break;
            case R.id.nav_main_instagram:
                instagramApi.getRecent()
                        .thenAccept(new Consumer<ArrayList<eu.epitech.epicture.instagram.models.GalleryItem>>() {
                            @Override
                            public void accept(final ArrayList<eu.epitech.epicture.instagram.models.GalleryItem> gallery) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.update(new eu.epitech.epicture.instagram.GalleryItemAdapter(MainActivity.this, gallery));
                                        swipe.setRefreshing(false);
                                    }
                                });
                            }
                        });
                break;
        }
    }

    private void update (ArrayAdapter adapter) {
        images.setAdapter(adapter);
        images.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }
}
