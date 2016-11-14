package de.kjaduesseldorf.sportsunite.app.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

import java.util.List;
import java.util.Locale;

import de.kjaduesseldorf.sportsunite.app.R;
import de.kjaduesseldorf.sportsunite.app.client.ContentClient;
import de.kjaduesseldorf.sportsunite.app.client.ImageClient;
import de.kjaduesseldorf.sportsunite.app.model.Content;

@EActivity
@OptionsMenu(R.menu.menu)
public class MainActivity extends AppCompatActivity implements RestErrorHandler, View.OnClickListener, FutureCallback<ImageClient.TaggedBitmap> {

    public static String PREFERENCE_FILE_KEY = "de.kja.app.PREFERENCE_FILE_KEY";
    public static String PREFERENCE_DISTRICT_KEY = "district";

    private static final String TAG = "MainActivity";

    public static boolean requestingLocation = false;

    @ViewById(R.id.swiperefresh)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.listview)
    protected RecyclerView listview;

    @RestService
    public static ContentClient contentClient;

    protected ContentAdapter contentAdapter;

    private boolean updating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contentClient.setRestErrorHandler(this);

        ImageClient.cleanupCache(this);

        SharedPreferences preferences = getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE);
        if(!preferences.contains(PREFERENCE_DISTRICT_KEY)) {
            openLocationActivity();
        }

        setContentView(R.layout.activity_main);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update(true);
            }
        });

        listview.setHasFixedSize(true);
        listview.setLayoutManager(new LinearLayoutManager(this));

        contentAdapter = new ContentAdapter(this, this, this);
        listview.setAdapter(contentAdapter);

        update(true);
    }

    private void openLocationActivity() {
        if(requestingLocation) {
            return;
        }
        requestingLocation = true;
        Intent intent = new Intent(this, LocationActivity_.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE);
        if(!preferences.contains(PREFERENCE_DISTRICT_KEY)) {
            openLocationActivity();
        }
        update(false);
    }

    @OptionsItem(R.id.menu_refresh)
    protected void menuRefreshSelected() {
        update(true);
    }

    @OptionsItem(R.id.menu_location)
    protected void menuLocationSelected() {
        openLocationActivity();
    }

    @Background
    protected void update(boolean showUpdate) {
        if(updating) {
            return;
        }
        updating = true;
        if(showUpdate) {
            showRefresh();
        }
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE);
        List<Content> contents = contentClient.getContents(preferences.getString(PREFERENCE_DISTRICT_KEY, "unknown"), Locale.getDefault().getLanguage());
        if(contents != null) {
            for(Content content : contents) {
                if(content.getImage() != null && !content.getImage().isEmpty()) {
                    ImageClient.getImageAsync(this, content.getImage());
                }
            }
        }
        showUpdate(contents);
        updating = false;
    }

    @UiThread
    protected void showRefresh() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @UiThread
    protected void showUpdate(List<Content> contents) {
        if(contents != null) {
            contentAdapter.setContents(contents);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    @UiThread
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        Log.e(TAG, "REST client error!", e);
        new AlertDialog.Builder(this)
                .setTitle(R.string.connectionerror)
                .setMessage(R.string.tryagain)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();

    }

    @Override
    public void onClick(View v) {
        int position = listview.getChildLayoutPosition(v);
        Content content = contentAdapter.getContent(position);

        Intent intent = new Intent(this, ContentActivity_.class);
        intent.putExtra(ContentActivity.EXTRA_TITLE, content.getTitle());
        intent.putExtra(ContentActivity.EXTRA_TEXT, content.getText());
        if(content.getImage() != null && !content.getImage().isEmpty()) {
            intent.putExtra(ContentActivity.EXTRA_IMAGE, content.getImage());
        }
        startActivity(intent);
    }

    protected ContentAdapter.ViewHolder findViewHolder(String image) {
        for(int i = 0; i < listview.getChildCount(); i++) {
            ContentAdapter.ViewHolder viewHolder =
                    (ContentAdapter.ViewHolder) listview.getChildViewHolder(listview.getChildAt(i));
            if(image.equals(viewHolder.awaitingImage)) {
                return viewHolder;
            }
        }
        return null;
    }

    @Override
    @UiThread
    public void onSuccess(ImageClient.TaggedBitmap result) {
        ContentAdapter.ViewHolder viewHolder = findViewHolder(result.id);
        if(viewHolder == null) {
            return;
        }
        View view = viewHolder.view;
        ImageView imagePreview = (ImageView) view.findViewById(R.id.imagePreview);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBarPreview);

        imagePreview.setImageBitmap(result.bitmap);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        progressBar.setVisibility(View.GONE);
        imagePreview.setVisibility(View.VISIBLE);
    }
    @Override
    @UiThread
    public void onFailure(Throwable t) {
        Log.e(TAG, "Could not load image!", t);

        ContentAdapter.ViewHolder viewHolder = findViewHolder(t.getMessage());
        if(viewHolder == null) {
            return;
        }
        View view = viewHolder.view;
        ImageView imagePreview = (ImageView) view.findViewById(R.id.imagePreview);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBarPreview);

        imagePreview.setImageResource(R.drawable.ic_error_black_24dp);
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        progressBar.setVisibility(View.GONE);
        imagePreview.setVisibility(View.VISIBLE);
    }
}
