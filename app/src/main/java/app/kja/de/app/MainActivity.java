package app.kja.de.app;

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

@EActivity
@OptionsMenu(R.menu.menu)
public class MainActivity extends AppCompatActivity {

    @ViewById(R.id.swiperefresh)
    protected SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.listview)
    protected RecyclerView listview;

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
    }

    @OptionsItem(R.id.menu_refresh)
    protected void menurefreshSelected() {
        update();
    }

    @Background
    protected void update() {
        Log.i("MainActivity", "update");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        showUpdate();
    }

    @UiThread
    protected void showUpdate() {
        swipeRefreshLayout.setRefreshing(false);
    }

}
