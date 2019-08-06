package biz.biztaker.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import biz.biztaker.R;
import biz.biztaker.commonClasses.BizTakerApp;
import biz.biztaker.entity.Person;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * @author Anand Jakhaniya
 */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private final String TAG = this.getClass().getSimpleName();
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;
    private Context context;
    private FloatingActionButton mAddBtn;
    private Toolbar toolbar;
    private View mProgressView;

    //data fields
    private Person person = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;

        person = (Person) getIntent().getSerializableExtra("person");

        initComponent();
        setData();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        if (!mayRequestContacts()) {
            //return;
            Log.d(TAG,"");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BizTakerApp.getInstance().currentActivity = this;
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS};
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,permissions[0])
                && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,permissions[1])
                && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,permissions[2])) {
            Snackbar.make(mProgressView.getRootView(), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(permissions, REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    }).show();
        } else {
            requestPermissions(permissions, REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressView = findViewById(R.id.login_progress);
        mAddBtn = findViewById(R.id.add_floating_btn);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LinearLayout headerView = (LinearLayout) navigationView.getHeaderView(0);
        TextView userName =  headerView.findViewById(R.id.user_name);
        userName.setText(person.firstName + " " + person.lastName);

        TextView emailView = headerView.findViewById(R.id.user_email);
        emailView.setText(person.email);
    }

    private void setData() {

    }

    public void addBtnClickEvent(View view) {
        String fcmToken = BizTakerApp.sharedPreferences.getString("fcmToken", null);
        Snackbar.make(mProgressView.getRootView(),fcmToken, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_logout){
           logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        BizTakerApp.getInstance().logout();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && this.checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED
                    && this.checkSelfPermission(permissions[1]) == PackageManager.PERMISSION_GRANTED
                    && this.checkSelfPermission(permissions[2]) == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mProgressView.getRootView(),context.getResources().getString(R.string.permission_granted), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(mProgressView.getRootView(), R.string.permission_with_storage_message, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
