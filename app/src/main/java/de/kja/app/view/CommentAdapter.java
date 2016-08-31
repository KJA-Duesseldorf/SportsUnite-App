package de.kja.app.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import de.kja.app.R;
import de.kja.app.model.Comment;
import de.kja.app.util.TimeUtil;

@EBean
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> comments;

    @RootContext
    protected Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        TextView usernameView = (TextView) holder.view.findViewById(R.id.commentUsername);
        TextView textView = (TextView) holder.view.findViewById(R.id.commentText);
        TextView timeView = (TextView) holder.view.findViewById(R.id.commentTime);

        usernameView.setText(comment.getUsername());
        textView.setText(comment.getText());
        timeView.setText(TimeUtil.getTimeAgo(comment.getTimestamp(), context));
    }

    @Override
    public int getItemCount() {
        if(comments == null) {
            return 0;
        }
        return comments.size();
    }

    public void show(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }
}
