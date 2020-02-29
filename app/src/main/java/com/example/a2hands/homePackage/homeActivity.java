package com.example.a2hands.homePackage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2hands.CreatePost;
import com.example.a2hands.LoginActivity;
import com.example.a2hands.NotificationsPackage.NotificationFragment;
import com.example.a2hands.homePackage.PostsPackage.Post;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.example.a2hands.SavedActivity;
import com.example.a2hands.SearchFragment;
import com.example.a2hands.User;
import com.example.a2hands.homePackage.PostsPackage.PostFragment;
import com.example.a2hands.settingsPackage.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class homeActivity extends AppCompatActivity implements
        SearchFragment.OnFragmentInteractionListener,
        PostFragment.OnListFragmentInteractionListener,
        NotificationFragment.OnListFragmentInteractionListener{


    //drawer
    private DrawerLayout drawer;
    public NavigationView navigationView;

    //bottom navigation
    private BottomNavigationView nav;
    private int navItemId;
    private BadgeDrawable badge;

    MaterialSpinner catsSpinner;
    String[] catsStrings;
    CircleImageView profile_image ;
    SearchView searchView;
    TextView notificationsTitle;
    FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    String myUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //drawer
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        nav = findViewById(R.id.bottom_navigation);
        badge = nav.getOrCreateBadge(nav.getMenu().getItem(3).getItemId());
        badge.setVisible(false);

        searchView = findViewById(R.id.searchView);
        notificationsTitle = findViewById(R.id.notificationsTitle);

        //category spinner declaration
        catsStrings = getResources().getStringArray(R.array.categories);
        catsSpinner = findViewById(R.id.catsSpinner);
        profile_image = findViewById(R.id.profile_image);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        catsSpinner.setItems(getResources().getStringArray(R.array.categories));
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        myUid=firebaseAuth.getCurrentUser().getUid();


        //drawer header data
        View headerView =  navigationView.getHeaderView(0);
        final CircleImageView header_profilePic =headerView.findViewById(R.id.drawer_profileImage);
        final TextView header_fAndLName = headerView.findViewById(R.id.drawer_fAndLName);
        final TextView header_email = headerView.findViewById(R.id.drawer_userEmail);

        FirebaseFirestore.getInstance().collection("users/").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+uid+"/"+user.profile_pic).getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri.toString()).into(header_profilePic);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                header_fAndLName.setText(user.first_name+" "+user.last_name);
                header_email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });

        header_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(homeActivity.this, ProfileActivity.class));
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        //navigation drawer menu items
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        startActivity(new Intent(homeActivity.this , ProfileActivity.class));
                        break;
                    case R.id.nav_saved:
                        startActivity(new Intent(homeActivity.this , SavedActivity.class));
                        break;
                    case R.id.nav_settings:
                        startActivity(new Intent(homeActivity.this , SettingsActivity.class));
                        break;
                    case R.id.nav_signOut:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(homeActivity.this , LoginActivity.class));
                        break;
                    case R.id.nav_share:
                        Toast.makeText(homeActivity.this, "Share", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_send:
                        Toast.makeText(homeActivity.this, "Send", Toast.LENGTH_SHORT).show();
                        break;
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });




        /////////////////////////////////////
        /////////////////////////////////////
        //bottom navigation
        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int pos =0;
                navItemId = item.getItemId();
                for (int i = 0; i < nav.getMenu().size(); i++) {
                    if(navItemId == nav.getMenu().getItem(i).getItemId()){
                        pos = i;
                        break;
                    }
                }

                switch (pos) {
                    case 0: navigateHome();
                        break;
                    case 1:navigateSearch() ;
                        break;
                    case 2:navigateCreatePost() ;
                        break;
                    case 3: navigateNotification();
                        break;
                    case 4:

                        break;
                }
                return true;
            }
        });

        //load current user main pic in home top menu
        FirebaseFirestore.getInstance().collection("users/").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                User user = task.getResult().toObject(User.class);
                FirebaseStorage.getInstance().getReference().child("Profile_Pics/"+uid+"/"+user.profile_pic).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(profile_image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
        });


        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
                /*Intent intent = new Intent(homeActivity.this, ProfileActivity.class);
                startActivity(intent);*/
            }
        });

        //check notifications
        FirebaseDatabase.getInstance().getReference("notifications")
                .orderByChild("subscriber_id")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount() > 0){
                            badge.setVisible(true);
                            badge.setNumber(( (int)dataSnapshot.getChildrenCount()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

        catsSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                loadPosts(position);
            }
        });
        /*catsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPosts(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });*/

        navigateHome();

    }//////////////////////////////////end of onCreate method

    private void CheckOnlineStatus(String status) {
        db.collection("users/").document(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        db.collection("users/").document(myUid).update(hashMap);

    }




    @Override
    protected void onStart() {
        CheckOnlineStatus("online");
        super.onStart();
    }


    @Override
    protected void onResume() {
        CheckOnlineStatus("online");
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        Calendar cal =Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String dateTime =simpleDateFormat.format(cal.getTime());
        CheckOnlineStatus(dateTime);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Calendar cal =Calendar.getInstance(Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String dateTime =simpleDateFormat.format(cal.getTime());
        CheckOnlineStatus(dateTime);
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    void loadPosts(int pos){
        Fragment frg = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("category", catsStrings[pos]);
        bundle.putString("for","home");
        frg.setArguments(bundle);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,frg).addToBackStack("");
        ft.commit();
    }
    public void navigateHome(){
        catsSpinner.setVisibility(View.VISIBLE);
        catsSpinner.setTextSize(15);
        searchView.setVisibility(View.GONE);
        notificationsTitle.setVisibility(View.GONE);
        loadPosts(0);
    }
    public void navigateSearch(){
        searchView.setVisibility(View.VISIBLE);
        catsSpinner.setVisibility(View.GONE);
        notificationsTitle.setVisibility(View.GONE);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,new SearchFragment()).addToBackStack(null);
        ft.commit();
    }

    public void navigateCreatePost(){
        Intent intent = new Intent(this, CreatePost.class);
        intent.putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(intent);
    }
    void navigateNotification(){
        notificationsTitle.setVisibility(View.VISIBLE);
        notificationsTitle.setTextColor(getResources().getColor(R.color.colorWhiteGray));
        catsSpinner.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.homeFrag,new NotificationFragment()).addToBackStack(null);
        ft.commit();
        badge.clearNumber();
        badge.setVisible(false);

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(Post item) {

    }

    @Override
    public void onListFragmentInteraction(int x) {

    }

    /*//@Override
    public void onOptionClicked(int index) {

    }*/
}