package com.example.urbanroots.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbanroots.domain.models.Comment;
import com.example.urbanroots.domain.models.Post;
import com.example.urbanroots.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import android.widget.LinearLayout;
import java.util.HashMap;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onEditPost(Post post);
        void onDeletePost(Post post);
    }

    public PostAdapter(List<Post> postList, Context context, OnPostInteractionListener listener) {
        this.postList = postList;
        this.context = context;
        this.listener = listener;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.userName.setText(post.getUserName());
        holder.description.setText(post.getDescription());
        holder.timestamp.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm").format(post.getTimestamp()));
        holder.likeCount.setText(post.getLikeCount() + " likes");
        holder.commentCount.setText(post.getCommentCount() + " comments");

        // Check if the current user is the post owner or admin
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        if (post.getUserId().equals(currentUserId)) {
            holder.postActionsLayout.setVisibility(View.VISIBLE);
        } else {
            holder.postActionsLayout.setVisibility(View.GONE);
        }

        // Like button
        holder.likeButton.setOnClickListener(v -> {
            String likeId = currentUserId + "_" + post.getPostId();
            db.collection("posts").document(post.getPostId()).collection("likes").document(likeId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Unlike
                            db.collection("posts").document(post.getPostId()).collection("likes").document(likeId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        db.collection("posts").document(post.getPostId())
                                                .update("LikeCount", post.getLikeCount() - 1);
                                        post.setLikeCount(post.getLikeCount() - 1);
                                        holder.likeCount.setText(post.getLikeCount() + " likes");
                                    });
                        } else {
                            // Like
                            db.collection("posts").document(post.getPostId()).collection("likes").document(likeId)
                                    .set(new HashMap<String, Object>() {{
                                        put("LikeId", likeId);
                                        put("UserId", currentUserId);
                                    }})
                                    .addOnSuccessListener(aVoid -> {
                                        db.collection("posts").document(post.getPostId())
                                                .update("LikeCount", post.getLikeCount() + 1);
                                        post.setLikeCount(post.getLikeCount() + 1);
                                        holder.likeCount.setText(post.getLikeCount() + " likes");
                                    });
                        }
                    });
        });

        // Comment button
        holder.commentButton.setOnClickListener(v -> {
            holder.commentInputLayout.setVisibility(View.VISIBLE);
            holder.submitCommentButton.setVisibility(View.VISIBLE);
        });

        // Submit comment
        holder.submitCommentButton.setOnClickListener(v -> {
            String commentText = holder.commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                String commentId = UUID.randomUUID().toString();
                db.collection("farmers").document(currentUserId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String userName = documentSnapshot.getString("Name");
                            Comment comment = new Comment(commentId, post.getPostId(), currentUserId, userName, commentText, new Date());
                            db.collection("posts").document(post.getPostId()).collection("comments").document(commentId)
                                    .set(comment)
                                    .addOnSuccessListener(aVoid -> {
                                        db.collection("posts").document(post.getPostId())
                                                .update("CommentCount", post.getCommentCount() + 1);
                                        post.setCommentCount(post.getCommentCount() + 1);
                                        holder.commentCount.setText(post.getCommentCount() + " comments");
                                        holder.commentEditText.setText("");
                                        holder.commentInputLayout.setVisibility(View.GONE);
                                        holder.submitCommentButton.setVisibility(View.GONE);
                                    });
                        });
            }
        });

        // Edit and Delete buttons
        holder.editButton.setOnClickListener(v -> listener.onEditPost(post));
        holder.deleteButton.setOnClickListener(v -> listener.onDeletePost(post));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView userName, description, timestamp, likeCount, commentCount;
        MaterialButton likeButton, commentButton, editButton, deleteButton, submitCommentButton;
        TextInputLayout commentInputLayout;
        TextInputEditText commentEditText;
        LinearLayout postActionsLayout;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.post_user_name);
            description = itemView.findViewById(R.id.post_description);
            timestamp = itemView.findViewById(R.id.post_timestamp);
            likeCount = itemView.findViewById(R.id.like_count);
            commentCount = itemView.findViewById(R.id.comment_count);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            editButton = itemView.findViewById(R.id.edit_post_button);
            deleteButton = itemView.findViewById(R.id.delete_post_button);
            commentInputLayout = itemView.findViewById(R.id.comment_input_layout);
            commentEditText = itemView.findViewById(R.id.comment_edit_text);
            submitCommentButton = itemView.findViewById(R.id.submit_comment_button);
            postActionsLayout = itemView.findViewById(R.id.post_actions_layout);
        }
    }
}