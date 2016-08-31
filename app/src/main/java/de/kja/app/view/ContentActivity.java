package de.kja.app.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;

import java.util.List;

import de.kja.app.R;
import de.kja.app.client.Authenticator;
import de.kja.app.client.ClientErrorHandler;
import de.kja.app.client.ContentClient;
import de.kja.app.client.ImageClient;
import de.kja.app.model.Comment;

@EActivity
public class ContentActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_IMAGE = "image";

    public static final String TAG = "ContentActivity";

    @ViewById(R.id.image_wrapper)
    protected FrameLayout imageWrapper;

    @ViewById(R.id.imageView)
    protected ImageView imageView;

    @ViewById(R.id.progressBarImage)
    protected ProgressBar progressBar;

    @ViewById(R.id.title)
    protected TextView titleView;

    @ViewById(R.id.text)
    protected TextView textView;

    @ViewById(R.id.commentList)
    protected RecyclerView commentList;

    @ViewById(R.id.textViewNoComments)
    protected TextView noComments;

    @ViewById(R.id.editTextComment)
    protected EditText editTextComment;

    @RestService
    protected ContentClient contentClient;

    @Bean
    protected ClientErrorHandler clientErrorHandler;

    @Bean
    protected CommentAdapter commentAdapter;

    @Bean
    protected Authenticator authenticator;

    protected boolean isDestroyed;

    private boolean updating = false;

    private long contentId = -1l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        contentClient.setRestErrorHandler(clientErrorHandler);

        Intent intent = getIntent();
        titleView.setText(intent.getStringExtra(EXTRA_TITLE));
        textView.setText(intent.getStringExtra(EXTRA_TEXT));

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

        final ContentActivity thizz = this;
        if(intent.hasExtra(EXTRA_IMAGE)) {
            imageWrapper.setVisibility(View.VISIBLE);
            ListenableFuture<ImageClient.TaggedBitmap> future = ImageClient.getImageAsync(this, intent.getStringExtra(EXTRA_IMAGE));
            Futures.addCallback(future, new FutureCallback<ImageClient.TaggedBitmap>() {
                @Override
                public void onSuccess(final ImageClient.TaggedBitmap result) {
                    if(!thizz.isDestroyed) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(result.bitmap);
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                progressBar.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Could not load image!", t);
                    if(!thizz.isDestroyed) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!thizz.isDestroyed) {
                                    imageView.setImageResource(R.drawable.ic_error_black_24dp);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                    progressBar.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            });
        }

        commentList.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        commentList.setAdapter(commentAdapter);
        contentId = intent.getLongExtra(EXTRA_ID, -1l);
        update();

    }

    @UiThread
    public void update() {
        if(updating) {
            return;
        }
        updating = true;
        updateBackground();
    }

    @Background
    protected void updateBackground() {
        List<Comment> comments = contentClient.getComments(contentId);
        show(comments);
    }

    @UiThread
    public void show(List<Comment> comments) {
        if(comments != null) {
            if(!comments.isEmpty()) {
                noComments.setVisibility(View.GONE);
                commentList.setVisibility(View.VISIBLE);
            } else {
                noComments.setVisibility(View.VISIBLE);
                commentList.setVisibility(View.GONE);
            }
            commentAdapter.show(comments);
        }
        updating = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    @Click(R.id.buttonSend)
    protected void buttonSend() {
        String comment = editTextComment.getText().toString().trim();
        if(comment.isEmpty()) {
            return;
        }
        editTextComment.getText().clear();
        postComment(comment);
    }

    @Background
    protected void postComment(String comment) {
        contentClient.postComment(contentId, comment);
        update();
    }
}
