package app.kja.de.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {

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
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContentAdapter.ViewHolder holder, int position) {
        TextView title = (TextView) holder.view.findViewById(R.id.contentTitle);
        TextView shortText = (TextView) holder.view.findViewById(R.id.contentShortText);
        title.setText("Test title at " + position);
        shortText.setText("Short test text at " + position);
    }

    @Override
    public int getItemCount() {
        return 10;
    }

}
