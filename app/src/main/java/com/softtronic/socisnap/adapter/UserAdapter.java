package com.softtronic.socisnap.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.softtronic.socisnap.ChatActivity;
import com.softtronic.socisnap.R;
import com.softtronic.socisnap.UserModel;
import com.softtronic.socisnap.ViewProfilePic;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final ArrayList<UserModel> userArrayList;
    private final Context context;

    public UserAdapter(ArrayList<UserModel> userArrayList, Context context) {
        this.userArrayList = userArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_details_view, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel model = userArrayList.get(position);
        final String UserId = userArrayList.get(position).getId();
        holder.userName.setText(model.getName());
        //holder.txtEmail.setText(model.getEmail());
        holder.txtEmail.setText((model.getBio().isEmpty() ? model.getEmail():model.getBio()));
        try {
                Glide.with(context).load(model.getImageUri()).into(holder.userImageView);
        }catch (Exception e){
            Picasso.get().load(R.drawable.no_user).into(holder.userImageView);
        }

        holder.nameBioLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("SENDER_UID_KEY", model.getId());
            intent.putExtra("SENDER_NAME", model.getName());
            intent.putExtra("uid", UserId);
            intent.putExtra("SENDER_IMAGE_URL_KEY", model.getImageUri());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
        holder.userImageView.setOnClickListener(view -> {
            AlertDialog alertDialog = ViewProfilePic.createAlertDialog(view.getContext(), model.getName(), model.getImageUri());
            alertDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        CircleImageView userImageView;
        TextView userName, txtEmail;
        CardView cardView;
        LinearLayout nameBioLayout;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userImageView = itemView.findViewById(R.id.user_image_view);
            userName = itemView.findViewById(R.id.text_view_user_name);
            txtEmail = itemView.findViewById(R.id.user_email);
            cardView = itemView.findViewById(R.id.cardView);
            nameBioLayout = itemView.findViewById(R.id.nameBioEmail);
        }
    }
}
