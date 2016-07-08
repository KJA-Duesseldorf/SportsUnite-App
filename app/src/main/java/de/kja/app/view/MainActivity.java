package de.kja.app.view;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;

import java.util.List;

import de.kja.app.R;
import de.kja.app.client.ContentClient;
import de.kja.app.model.Content;

@EActivity
@OptionsMenu(R.menu.menu)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.swiperefresh)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.listview)
    protected RecyclerView listview;

    @RestService
    protected ContentClient contentClient;

    protected ContentAdapter contentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

        listview.setHasFixedSize(true);
        listview.setLayoutManager(new LinearLayoutManager(this));

        contentAdapter = new ContentAdapter();
        listview.setAdapter(contentAdapter);

        update();
    }

    @OptionsItem(R.id.menu_refresh)
    protected void menuRefreshSelected() {
        update();
    }

    @Background
    protected void update() {
        Log.i("MainActivity", "update");
        List<Content> contents = contentClient.getContents();
        showUpdate(contents);
    }

    @UiThread
    protected void showUpdate(List<Content> contents) {
        contentAdapter.setContents(contents);
        swipeRefreshLayout.setRefreshing(false);
    }

}
