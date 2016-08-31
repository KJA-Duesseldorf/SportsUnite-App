package de.kja.app.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import de.kja.app.R;
import de.kja.app.client.ImageClient;
import de.kja.app.model.Content;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private static final String TAG = "ContentAdapter";

    private List<Content> contents = new ArrayList<Content>();
    private Context context;
    private OnClickListener onClickListener;
    private FutureCallback<ImageClient.TaggedBitmap> onImageArrived;

    public ContentAdapter(Context context, OnClickListener onClickListener, FutureCallback<ImageClient.TaggedBitmap> onImageArrived) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.onImageArrived = onImageArrived;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        String awaitingImage = null;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    @Override
    public ContentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_view, parent, false);
        v.setOnClickListener(onClickListener);
        ((ProgressBar)v.findViewById(R.id.progressBarPreview)).getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark),
                        PorterDuff.Mode.MULTIPLY);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContentAdapter.ViewHolder holder, int position) {
        final Content content = contents.get(position);
        TextView title = (TextView) holder.view.findViewById(R.id.commentUsername);
        TextView shortText = (TextView) holder.view.findViewById(R.id.contentShortText);
        FrameLayout wrapper = (FrameLayout) holder.view.findViewById(R.id.previewWrapper);
        ImageView imageView = (ImageView) holder.view.findViewById(R.id.imagePreview);
        ProgressBar progressBar = (ProgressBar) holder.view.findViewById(R.id.progressBarPreview);

        title.setText(content.getTitle());
        shortText.setText(content.getShortText());

        final String imageId = content.getImage();
        if(imageId != null && !imageId.isEmpty()) {
            wrapper.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            holder.awaitingImage = content.getImage();
            ListenableFuture<ImageClient.TaggedBitmap> future = ImageClient.getImageAsync(context, imageId);
            Futures.addCallback(future, onImageArrived);
        } else {
            wrapper.setVisibility(View.GONE);
            holder.awaitingImage = null;
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.awaitingImage = null;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.awaitingImage = null;
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
        this.notifyDataSetChanged();
    }

    public Content getContent(int position) {
        return contents.get(position);
    }
}
