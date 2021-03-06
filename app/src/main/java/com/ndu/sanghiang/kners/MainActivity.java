package com.ndu.sanghiang.kners;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.makeramen.roundedimageview.RoundedImageView;
import com.ndu.sanghiang.kners.firebase.SigninActivity;
import com.ndu.sanghiang.kners.indevelopment.GridMenuActivity;
import com.ndu.sanghiang.kners.indevelopment.HistoryActivity;
import com.ndu.sanghiang.kners.indevelopment.ProdukKnowledgeActivity;
import com.ndu.sanghiang.kners.ocr.OcrCaptureActivity;
import com.ndu.sanghiang.kners.projecttrackerfi.ProjectTrackerActivity;
import com.ndu.sanghiang.kners.service.ConnectivityReceiver;
import com.ndu.sanghiang.kners.service.MyApplication;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ConnectivityReceiver.ConnectivityReceiverListener {

    Button buttonProdukKnowledge, buttonBrowser, buttonCodeMatch, buttonGridMenu,
            buttonOcrCapture, buttonProjectTracker, buttonHistory, buttonUnmountOtg, buttonProfile,
            buttonSmartQap;
    TextView navEmail, navUserName, navDept;
    RoundedImageView navPhoto;
    //Firebase
    FirebaseAuth.AuthStateListener mAuthListner;
    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;
    FirebaseUser userEmail = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseUser userName = FirebaseAuth.getInstance().getCurrentUser();
    public static final String CHANNEL_ID = "kners_app";
    private static final String CHANNEL_NAME = "KNers App";
    private static final String CHANNEL_DESCRIPTION = "KNers App Description";

    private DrawerLayout drawer;
    private Handler handler;
    private String versName;
    int versCode;

    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPrefs;
    private String TAG;
    private DatabaseReference tokenRef;
    private String token;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.getCurrentUser();
        updateUI();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        TAG = "Nandur93";
        View headerView = navigationView.getHeaderView(0);
        navEmail = headerView.findViewById(R.id.text_view_nav_email);
        navPhoto = headerView.findViewById(R.id.image_view_email_photo);
        navDept = headerView.findViewById(R.id.text_view_nav_dept);
        navUserName = headerView.findViewById(R.id.text_view_nav_username);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Hallo " + navUserName.getText().toString(), Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Sample AdMob app ID: ca-app-pub-4368595636314473~7130779124
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~6300978111");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        handler = new Handler();
        buttonProdukKnowledge = findViewById(R.id.button_produk_knowledge);
        buttonBrowser = findViewById(R.id.button_browser);
        buttonCodeMatch = findViewById(R.id.button_code_match_activity);
        buttonGridMenu = findViewById(R.id.button_grid_menu);
        buttonHistory = findViewById(R.id.button_history);
        buttonOcrCapture = findViewById(R.id.button_ocr_activity);
        buttonProjectTracker = findViewById(R.id.button_project_tracker);
        buttonUnmountOtg = findViewById(R.id.button_unmount_otg);
        buttonProfile = findViewById(R.id.button_profile);
        buttonSmartQap = findViewById(R.id.button_smartqap);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        // get current userId
        String userId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("profile");
        tokenRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Permission Marshmelo
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.INTERNET},
                1);

        //developer menu menampilkan semua menu
        /* hide
        buttonProdukKnowledge.setVisibility(View.GONE); //dev
        buttonBrowser.setVisibility(View.GONE); //dev
        buttonInventoryManager.setVisibility(View.GONE); //dev
        //buttonCodeMatch.setOnClickListener(v -> goToCodematch()); //qa
        buttonHistory.setVisibility(View.GONE); //dev
        //buttonOcrCapture.setOnClickListener(v -> goToOcrCapture()); //qa
        buttonProjectTracker.setVisibility(View.GONE); //fi
        buttonGridMenu.setVisibility(View.GONE); //fi
        //buttonUnmountOtg.setOnClickListener(v -> goToUnmountOTG()); //dev
        buttonProfile.setVisibility(View.GONE); //dev */

        //fi menu hanya menampilkan menu fi
        /* hide
        buttonProdukKnowledge.setVisibility(View.GONE); //dev
        buttonBrowser.setVisibility(View.GONE); //dev
        buttonInventoryManager.setVisibility(View.GONE); //dev
        //buttonCodeMatch.setOnClickListener(v -> goToCodematch()); //qa
        buttonHistory.setVisibility(View.GONE); //dev
        //buttonOcrCapture.setOnClickListener(v -> goToOcrCapture()); //qa
        buttonProjectTracker.setVisibility(View.GONE); //fi
        buttonGridMenu.setVisibility(View.GONE); //fi
        //buttonUnmountOtg.setOnClickListener(v -> goToUnmountOTG()); //dev
        buttonProfile.setVisibility(View.GONE); //dev */

        //qa menu hanya menampilkan menu qa
        // hide
/*        buttonProdukKnowledge.setVisibility(View.GONE); //dev
        buttonBrowser.setVisibility(View.GONE); //dev
        //buttonCodeMatch.setOnClickListener(v -> goToCodematch()); //qa
        buttonHistory.setVisibility(View.GONE); //dev
        //buttonOcrCapture.setOnClickListener(v -> goToOcrCapture()); //qa
        buttonProjectTracker.setVisibility(View.GONE); //fi
        buttonGridMenu.setVisibility(View.GONE); //fi
        //buttonUnmountOtg.setOnClickListener(v -> goToUnmountOTG()); //dev
        buttonProfile.setVisibility(View.GONE); //dev //
        buttonUnmountOtg.setVisibility(View.GONE);*/

        //button listener
        buttonProdukKnowledge.setOnClickListener(v -> goToProdukKnowledge());
        buttonBrowser.setOnClickListener(v -> goToBrowser());
        buttonCodeMatch.setOnClickListener(v -> goToCodematch());
        buttonHistory.setOnClickListener(v -> {
            //goToHistory();
            //displayNotification();
        });
        buttonOcrCapture.setOnClickListener(v -> goToOcrCapture());
        buttonSmartQap.setOnClickListener(v -> goToSmartQap());
        buttonProjectTracker.setOnClickListener(v -> goToProjectTracker());
        buttonGridMenu.setOnClickListener(v -> goToGridMenu());
        buttonUnmountOtg.setOnClickListener(v -> goToUnmountOTG());
        buttonProfile.setOnClickListener(v -> goToProfile());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token = task.getResult().getToken();
                        Log.i(TAG, token);
                        saveToken();
                    } else {
                        Log.i(TAG, task.getException().getMessage());
                    }
                });

        navUserName.setOnClickListener(v -> goToProfile());
        navPhoto.setOnClickListener(v -> goToProfile());
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        mAuthListner = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(MainActivity.this, SigninActivity.class));
            }
        };

        // Load data langsung dari firebase
        // Load nik dan email
        checkConnection();
        loadImageFromPicasso();

        if (userEmail != null) {
            String userEmail = this.userEmail.getEmail();
            navEmail.setText(userEmail);
        } else {
            // No userEmail is signed in
            navEmail.setText("Signed Out");
        }
        /*if (userPhoto != null){
            String url = Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl()).toString();
            new DownloadImage(navPhoto).execute(url);
        } else {
            loadImageFromPicasso();
            //navPhoto = headerView.findViewById(R.id.image_view_email_photo);
        }*/

        String userDept = sharedPrefs.getString("user_dept", "Please Update Your Profile");
        if (userDept != null) {
            navDept.setText(userDept);
        } else {
            navDept.setText(R.string.dept_hint);
        }

        if (userName != null) {
            String shNik = sharedPrefs.getString("user_nik", "UNREGISTERED");
            navUserName.setText(userName.getDisplayName() + " " + "(" + shNik + ")");
        } else {
            for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                if (userInfo.getProviderId().equals("password")) {
                    loadNoImage();
                    navUserName.setText("KNers");
                }
            }
            navUserName.setText("KNers");
        }
        // Load nama
        navUserName.setText(userName.getDisplayName());
        String shNik = sharedPrefs.getString("user_nik", "UNREGISTERED");
        String shName = sharedPrefs.getString("user_name", "KNers");
        for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                Toast.makeText(this, "password", Toast.LENGTH_SHORT).show();
                if (userName != null) {
                    navUserName.setText(shNik);
                } else {
                    navUserName.setText(shName + " " + "(" + shNik + ")");
                }

            } else {
                navUserName.setText(userName.getDisplayName() + " " + "(" + shNik + ")");
                Toast.makeText(this, "google", Toast.LENGTH_SHORT).show();
            }
        }

        //getVersionName
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versName = pInfo.versionName;
            versCode = pInfo.versionCode;
            editor.putString("version_name", versName);
            editor.putInt("version_code", versCode);
            editor.apply();
            Log.d("MyApp", "Version Name : " + versName + "\n Version Code : " + versCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d("MyApp", "PackageManager Catch : " + e.toString());
        }
        // get menu from navigationView
        Menu menu = navigationView.getMenu();
        // find MenuItem you want to change
        MenuItem nav_appversion = menu.findItem(R.id.nav_version_name);
        // set new title to the MenuItem
        nav_appversion.setTitle(versName);


    }


    private void saveToken() {
        if (tokenRef.child("token").getKey() != null) {
            tokenRef.child("token").setValue(token);
        }
    }

    private void loadNoImage() {
        Uri uri = Uri.parse("https://lh3.googleusercontent.com/5Lfd3sDr2_KkGw2TV5ccn57xnZyTbGQHHJUxipkHf64W8ryfGHqncL5xDh2Yxwo8hKL81fPXeMYqab_khBRnQggmTsVEmTutWUf_sQkxLwBxiOTBcTohNAJeTJfXv9Pmzjyh_cuJYuU3yO1Pgm5SuSSjO-o_ivPgTQndfJR9AvMOPIC8GxWvzTnqom7uPNdFh7U-4ebQz4sAJOsccPZcLSzBMZIVD31KZJSxZIu9XKJksJuRCyFpDQ3j1PhlyP8fYCjM7fWa0pjLOFHS_ofrGIUFUZImqa9LA5bXfZOwB0mChOmficpYy4ET2patEAK9HHNXlRTwo7aSPfryg3lY5Cwz8X5ptf6gy_2UkKOIc1N8ACKfVj1cOPiR40gAHPl7saNjbqYCdk6UjqTcPDn1xkk7-M5bt8dOYbBFuunEV8zEZq1RU1QPelJ4GIHfzpgcbnNljsxK4bRjh79LFOnTnnHB8A9tgj9n4GLL6ekfF3Q1n6wWLN4W4wZN_SrY9raKn8jhl5E2fXczWNNKj4gSkY2qTW69y1M-iSwqtqdricbiwdMbEDVTM_2whSzkNOxBIg3opJiaddrEhDowlGuF-LvJeQ_JjXOOKkA2punukporIxHXTg8SX1fK8LI2EvP-NtK9WOWmlmtRQFfSLseTv3ACJTTdyeo=s72-no");
        Picasso.get()
                .load(uri)
                .placeholder(R.drawable.ic_launcher_round)
                .error(R.drawable.ic_launcher_round)
                .into(navPhoto);
    }

    private void loadImageFromPicasso() {
        //picasso
        Uri userPhoto = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl();

        if (userPhoto != null) {
            String originalPieceOfUrl = "s96-c/photo.jpg";
            // Variable holding the new String portion of the url that does the replacing, to improve image quality
            String newPieceOfUrlToAdd = "s400-c/photo.jpg";
            String hdUserPhoto = userPhoto.toString();
            String hdFinalUser = hdUserPhoto.replace(originalPieceOfUrl, newPieceOfUrlToAdd);
            Picasso.get()
                    .load(hdFinalUser)
                    .placeholder(R.drawable.ic_launcher_round)
                    .error(R.drawable.ic_launcher_round)
                    .into(navPhoto);
        } else {
            loadNoImage();
        }

    }

    // Method to manually check connection status
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        String message;
        int color;
        //color = Color.WHITE;
        //message = "";
        if (isConnected) {
            message = "Loading...";
            color = Color.WHITE;
            loadFirebaseUserData();
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
            loadOfflineData();
        }

        Snackbar snackbar = Snackbar
                .make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void loadOfflineData() {
        // ketika di load, dapatkan value dari shared pref langsung
        String shDept = sharedPrefs.getString("user_dept", "");
        navDept.setText(shDept);

        Toast.makeText(MainActivity.this, "Loaded from offline data", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(MainActivity.this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private void loadFirebaseUserData() {
        // Load nama
        if (userName != null) {
            String userName = this.userName.getDisplayName();
            navUserName.setText(userName);
        }
        navUserName.setText(userName.getDisplayName());
        // Load email
        if (userEmail != null) {
            String userEmail = this.userEmail.getEmail();
            navUserName.setText(userEmail);
        }
        // Configuration
        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String myName = dataSnapshot.child("name").getValue().toString();
                    String myNik = dataSnapshot.child("nik").getValue().toString();
                    String myEmail = dataSnapshot.child("email").getValue().toString();
                    String myDept = dataSnapshot.child("dept").getValue().toString();
                    // Load nama
                    navUserName.setText(userName.getDisplayName());
                    String shNik = sharedPrefs.getString("user_nik", "UNREGISTERED");
                    String shName = sharedPrefs.getString("user_name", "KNers");
                    String shEmail = sharedPrefs.getString("user_email", "");
                    String shDept = sharedPrefs.getString("user_dept", "UNREGISTERED");
                    for (UserInfo userInfo : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                        if (userInfo.getProviderId().equals("password")) {
                            if (userName != null) {
                                navUserName.setText(String.format("%s (%s)", myName, myNik));
                            } else {
                                navUserName.setText(String.format("%s (%s)", shName, shNik));
                                navDept.setText(shDept);
                            }
                        } else {
                            navUserName.setText(String.format("%s (%s)", userName.getDisplayName(), myNik));
                            navDept.setText(myDept);
                        }
                    }
                    Toast.makeText(MainActivity.this, "Data loaded from firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToSmartQap() {
        Intent goToSmartQap = new
                Intent(MainActivity.this, SmartQapActivity.class);
        startActivity(goToSmartQap);
    }

    private void goToProfile() {
        Intent goToProfile = new
                Intent(MainActivity.this, ProfileActivity.class);
        startActivity(goToProfile);
    }

    private void goToGridMenu() {
        Intent gridMenu = new
                Intent(MainActivity.this, GridMenuActivity.class);
        startActivity(gridMenu);
    }

    private void goToCodematch() {
        Intent codeMatchIntent = new
                Intent(MainActivity.this, CodeMatchActivity.class);
        startActivity(codeMatchIntent);
    }

    private void goToProdukKnowledge() {
        Intent produkIntent = new
                Intent(MainActivity.this, ProdukKnowledgeActivity.class);
        startActivity(produkIntent);
    }

    private void goToProjectTracker() {
        Intent projectTracker = new
                Intent(MainActivity.this, ProjectTrackerActivity.class);
        startActivity(projectTracker);
    }

    public void aboutActivity() {
        Intent aboutIntent = new
                Intent(MainActivity.this, AboutActivity.class);
        startActivity(aboutIntent);
    }

    public void goToBrowser() {
        Intent browserIntent = new
                Intent(MainActivity.this, WebViewActivity.class);
        startActivity(browserIntent);
    }

    public void goToHistory() {
        Intent historyIntent = new
                Intent(MainActivity.this, HistoryActivity.class);
        startActivity(historyIntent);
    }

    public void goToOcrCapture() {
        Intent ocrIntent = new
                Intent(MainActivity.this, OcrCaptureActivity.class);
        startActivity(ocrIntent);
    }

    public void goToUnmountOTG() {
        Intent i = new Intent(android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS);
        startActivity(i);
    }
    //get the current version number and name


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    //.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Keluar Dari Aplikasi")
                    .setMessage("Semua data dan progress Anda akan tetap tersimpan")
                    .setPositiveButton("Yes", (dialog, which) -> closeActivity())
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void closeActivity() {
        super.onBackPressed();
        moveTaskToBack(true);
        finish();
    }

    public void onInfoVersionName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_version);
        builder.setMessage("Verison Name: " + versName + "\n" + "Version Code: " + versCode);
        builder.setIcon(R.drawable.ic_info_outline_black_24dp);
        AlertDialog diag = builder.create();
        //Display the message!
        diag.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            settingsActivity();
            return true;
        }

        if (id == R.id.action_update) {
            updateApp();
            return true;
        }

        if (id == R.id.action_about) {
            aboutActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateApp() {
        new AppUpdater(MainActivity.this)
                //.setUpdateFrom(UpdateFrom.GITHUB)
                //.setGitHubUserAndRepo("javiersantos", "AppUpdater")
                .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML("https://raw.githubusercontent.com/nandur93/KNers/master/update-changelog.xml")
                .setDisplay(Display.DIALOG)
                .setButtonDoNotShowAgain(null)
                .showAppUpdated(true)
                .start();
    }

    private void settingsActivity() {
        Intent settingsIntent = new
                Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            closeDrawer();
            handler.postDelayed(this::goToProfile, 250);
            // Handle the camera action
        } else if (id == R.id.nav_share) {
            closeDrawer();
            handler.postDelayed(this::shareApp, 250);

        } else if (id == R.id.nav_settings) {
            closeDrawer();
            handler.postDelayed(this::settingsActivity, 250);
        } else if (id == R.id.nav_signout) {
            closeDrawer();
            handler.postDelayed(this::signOutAlert, 250);
        } else if (id == R.id.nav_about) {
            closeDrawer();
            // Do something after 5s = 5000ms
            handler.postDelayed(this::aboutActivity, 250);
        } else if (id == R.id.nav_version_name) {
            closeDrawer();
            handler.postDelayed(this::onInfoVersionName, 250);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareSubject = getResources().getString(R.string.app_name);
        String shareBody = getResources().getString(R.string.share_body);
        String shareVia = getResources().getString(R.string.share_via);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject + " Versi " + versName + " Build " + versCode);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, shareVia));
    }

    private void signOutAlert() {
        new AlertDialog.Builder(this)
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Keluar?")
                .setMessage("Semua sesi Anda akan tersimpan")
                .setPositiveButton("Keluar", (dialog, which) -> signOut())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> updateUI());

        // Clear sharedpref
        clearSharedPref();
    }

    private void clearSharedPref() {
        sharedPrefs.edit()
                .remove("user_name")
                .remove("user_email")
                .remove("user_nik")
                .remove("user_dept")
                .apply();
    }

    private void updateUI() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Permission Marshmelo
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the textViewResult arrays are empty.
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                //Toast.makeText(MainActivity.this, "Internet diijinkan", Toast.LENGTH_SHORT).show();
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(MainActivity.this, "Ijin untuk mengakses internet ditolak", Toast.LENGTH_SHORT).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

    public void closeDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }


    /*@SuppressLint("StaticFieldLeak")
    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d("Error", Arrays.toString(e.getStackTrace()));

            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }*/

}
