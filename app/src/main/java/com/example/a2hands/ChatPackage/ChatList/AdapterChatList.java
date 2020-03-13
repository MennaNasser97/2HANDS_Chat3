package com.example.a2hands.ChatPackage.ChatList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a2hands.ChatPackage.ChatActivity;
import com.example.a2hands.R;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.PostsPackage.MyPostRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<User> usersList;
    private HashMap<String,String> lastMessageMap;
    private HashMap<String,String> SeenOfTheLastMessage;
    String hisImage;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    public AdapterChatList(Context context, List<User> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMessageMap = new HashMap<>();
        SeenOfTheLastMessage =new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        //get data
        final String Uid =usersList.get(position).Uid;
        db.collection("users/").document(Uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                loadPhotos(holder.profileIv,"Profile_Pics/"+Uid+"/"+user.profile_pic );
                holder.nameTv.setText(user.first_name+" "+user.last_name);
            }
        });
        String lastMessage = lastMessageMap.get(Uid);
        //set data
           if (Objects.equals(SeenOfTheLastMessage.get(Uid), "NewMessage")){
               holder.lastMessageTv.setVisibility(View.VISIBLE);
               holder.lastMessageTv.setText(lastMessage);
               SpannableString ss = new SpannableString(holder.lastMessageTv.getText());
               StyleSpan bold = new StyleSpan(Typeface.BOLD);
               ss.setSpan(bold,0,holder.lastMessageTv.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
               holder.lastMessageTv.setText(ss);
               holder.lastMessageTv.setTextSize(20f);
               holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
           }
           else {
               holder.lastMessageTv.setVisibility(View.VISIBLE);
               holder.lastMessageTv.setText(lastMessage);
           }
        //set online status of other users in chatlist
        /*if (usersList.get(position).onlineStatus.equals("online")){
            //online

        }else {
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }*/

        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",Uid);
                context.startActivity(intent);
            }
        });

    }
    public void setLastMessageAndSeenMap(String userId,String lastMessage,String IsSeen){
        lastMessageMap.put(userId,lastMessage);
        SeenOfTheLastMessage.put(userId,IsSeen);
    }

    void loadPhotos(final ImageView imgV , String path){
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                hisImage=uri.toString();
                Picasso.get().load(uri.toString()).into(imgV);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv,onlineStatusIv;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv =itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
