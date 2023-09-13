package com.example.chat.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.Adapter.MessagesAdapter;
import com.example.chat.ModelClass.Messages;
import com.example.chat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {


    String ReciverImage,ReciverUID,ReciverName, SenderUID;
    CircleImageView profileimage;
    TextView reciverName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String sImage;
    public static String rImage;

    CardView sendBtn;
    EditText edtMessage;

    String senderRoom,reciverRoom;

    RecyclerView messageAdater;
    ArrayList<Messages> messagesArrayList;

    MessagesAdapter adater;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = FirebaseDatabase.getInstance("https://chatapp-ff14d-default-rtdb.firebaseio.com/");
        firebaseAuth = FirebaseAuth.getInstance();

        ReciverName = getIntent().getStringExtra("name");
        ReciverImage = getIntent().getStringExtra("ReciverImage");
        ReciverUID = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        profileimage = findViewById(R.id.profile_image);
        reciverName = findViewById(R.id.reciverName);

        messageAdater = findViewById(R.id.messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdater.setLayoutManager(linearLayoutManager);
        adater = new MessagesAdapter(ChatActivity.this , messagesArrayList);
        messageAdater.setAdapter(adater);

        sendBtn = findViewById(R.id.sendBtn);
        edtMessage = findViewById(R.id.edtMessage);

        Picasso.get().load(ReciverImage).into(profileimage);
        reciverName.setText(""+ReciverName);

        SenderUID = firebaseAuth.getUid();

        senderRoom = SenderUID+ReciverUID;
        reciverRoom = ReciverUID+SenderUID;


        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messges");

        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {

                messagesArrayList.clear();

                for(DataSnapshot dataSnapshot:Snapshot.getChildren()){
                    Messages messges = dataSnapshot.getValue(Messages.class);
                    messagesArrayList.add(messges);
                }
                adater.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError seError) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {


                sImage = Snapshot.child("imageUri").getValue().toString();
                rImage = ReciverImage;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError seError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edtMessage.getText().toString();
                if(message.isEmpty()){
                    Toast.makeText(ChatActivity.this, "please enter message", Toast.LENGTH_SHORT).show();
                    return;
                }
                edtMessage.setText("");
                Date date = new Date();
                Messages messages = new Messages(message,SenderUID,date.getTime());

                database = FirebaseDatabase.getInstance("https://chatapp-ff14d-default-rtdb.firebaseio.com/");
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messges")
                        .push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.getReference().child("chats")
                                .child(reciverRoom)
                                .child("messges")
                                .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });
            }
        });
    }
}