package de.kja.app.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.kja.app.R;
import de.kja.app.model.Content;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

    private List<Content> contents = new ArrayList<Content>();
    private OnClickListener onClickListener;

    public ContentAdapter(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View view;

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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContentAdapter.ViewHolder holder, int position) {
        Content content = contents.get(position);
        TextView title = (TextView) holder.view.findViewById(R.id.contentTitle);
        TextView shortText = (TextView) holder.view.findViewById(R.id.contentShortText);
        title.setText(content.getTitle());
        shortText.setText(content.getShortText());
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
