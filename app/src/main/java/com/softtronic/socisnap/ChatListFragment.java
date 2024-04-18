package com.softtronic.socisnap;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    ArrayList<ModelChatList> chatListList;
    Set<UserModel> usersList;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    GifImageView gifImageView;
    ProgressBar progressBar;
    AdapterChatList adapterChatList;
    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        FloatingActionButton users = view.findViewById(R.id.selectUser);
        GifImageView noChats = view.findViewById(R.id.noChats);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Users");
        users.setOnClickListener(v->{
            UsersFragment fragment = new UsersFragment();
            FragmentManager fm = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, "");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        firebaseAuth = FirebaseAuth.getInstance();

        // getting current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.chatlistrecycle);
        progressBar = view.findViewById(R.id.pbLoadChats);
        chatListList = new ArrayList<>();

        if(firebaseUser != null){
            reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
            progressBar.setVisibility(View.VISIBLE);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatListList.clear();
                    if(dataSnapshot.exists()){
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        noChats.setVisibility(View.GONE);
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelChatList modelChatList = ds.getValue(ModelChatList.class);
                            chatListList.add(modelChatList);
                        }
                        loadChats();
                    }
                    else{
                        Toast.makeText(getContext(), "No Recent Chats", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        noChats.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    noChats.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        return view;
    }

    // loading the user chat layout using chat node
    private void loadChats() {
        usersList = new HashSet<>();
        for(ModelChatList modelChatList: chatListList){
            reference = FirebaseDatabase.getInstance().getReference("Users").child(modelChatList.getId());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        Objects.requireNonNull(user).setImageUri(dataSnapshot.child("image").getValue(String.class));
                        Objects.requireNonNull(user).setId(dataSnapshot.child("uid").getValue(String.class));
                        user.setOnlineStatus(dataSnapshot.child("onlineStatus").getValue(String.class));
                        usersList.add(user);
                        lastMessage(user.getId());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        adapterChatList = new AdapterChatList( getActivity(),usersList);
        recyclerView.setAdapter(adapterChatList);
        adapterChatList.clear();
        adapterChatList.addAll(usersList);
    }

    private void lastMessage(final String uid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastMsg = "default";
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelChat chat = dataSnapshot1.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(uid) ||
                            chat.getReceiver().equals(uid) && chat.getSender().equals(firebaseUser.getUid())) {
                        if (chat.getType().equals("images")) {
                            lastMsg = "Sent a Photo";
                        }
                        else {
                            lastMsg = chat.getMessage();
                        }
                    }
                    adapterChatList.setLastMessageMap(uid, lastMsg);
                }
                adapterChatList.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

}
