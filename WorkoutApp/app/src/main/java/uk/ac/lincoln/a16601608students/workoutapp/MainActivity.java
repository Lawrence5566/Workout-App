package uk.ac.lincoln.a16601608students.workoutapp;

import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private StorageReference mStorageRef;
    private Uri downloadUrl;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SQLiteHelper mDbHelper = new SQLiteHelper(this);
        mDbHelper.createWorkoutsTable(); //create workouts table for later, if one doesn't exist

        //initialise firebase storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //curently using annon sign in for testing:
        signInAnonymously();

        //check for user: (for when we have them)
        /*
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            readDb();
        } else {
            signInAnonymously();
        }*/

        //set toolbar as action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        //ref & add navigation view to listen to click events on the drawer menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); //can pass main acitivty as it implements navItemSelected

        //handle opening drawer toggle, including adding "three lines" icon to toolbar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //set starting fragment
        //but if runtime config changed (like rotating device), onCreate will run again
        //therefore only load start fragment if savedInstance = null
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WorkoutsFragment()).commit();
            navigationView.setCheckedItem(R.id.viewWorkout);
        }

    }

    @Override
    protected void onUserLeaveHint() { //triggers when user leaves
        super.onUserLeaveHint();

        writeDb(); //upload db
        mAuth.signOut();
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d("annon sign", "onSuccess: ");
                user = authResult.getUser();

                String email = "test@test.com";
                String password = "password";

                AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener((new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("signed in", " ");
                        readDb();
                    }
                }));

            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
    }

    public void writeDb(){
        //reading = false;

        //try to write
        Uri file = Uri.fromFile(new File("/data/data/uk.ac.lincoln.a16601608students.workoutapp/databases/Workouts.db"));
        StorageReference dbRef = mStorageRef.child("Workouts/Workout.db");

        Log.d("WRITE", "writeDb: ");

        dbRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // unsuccessful upload
                    }
                });
    }

    public void readDb(){
        //try to find file
        mStorageRef.child("Workouts/Workout.db").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadUrl = uri; // Got the download URI

                //now replace db since we know we hav one online
                Uri file = Uri.fromFile(new File("/data/data/uk.ac.lincoln.a16601608students.workoutapp/databases/Workouts.db"));
                //try download the file
                mStorageRef.child("Workouts/Workout.db").getFile(file)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Successfully downloaded data to local file, need to update workouts fragment to reflect changes
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // failed download
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // File not found, so keep local database
            }
        });

    }

    //when an item selected in drawer, change fragments
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) { //pass menu item selected
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (menuItem.getItemId()){
            case R.id.viewWorkout: //view workout tapped
                //replace current frame layout in main, with new fragment layout
                fragmentTransaction.replace(R.id.fragment_container,
                    new WorkoutsFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.newWorkout:
                fragmentTransaction.replace(R.id.fragment_container,
                        new NewWorkoutsFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.benchmarks:
                fragmentTransaction.replace(R.id.fragment_container,
                        new FindNearestGymFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

            case R.id.setReminders:
                fragmentTransaction.replace(R.id.fragment_container,
                        new SetRemindersFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

        }

        drawer.closeDrawer(GravityCompat.START); //close drawer
        return true; //item has been clicked
    }

    public void onBackPressed(){ //make sure back button doesn't close drawer
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else{
            writeDb(); //upload db
            mAuth.signOut();
            super.onBackPressed(); //close activity as usual
        }
    }
}
