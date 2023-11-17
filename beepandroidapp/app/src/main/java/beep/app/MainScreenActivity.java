package beep.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import login.UserDTO;

public class MainScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout main_screen_layout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private UserDTO userDTO;
    private TextView fullName;
    private TextView ridesNum;
    private int lastSelectedItem = R.id.nav_home;
    private MapFragment mapFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mapFragment = new MapFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit();

        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");

        main_screen_layout=findViewById(R.id.main_screen);
        navigationView=findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        fullName = navigationView.getHeaderView(0).findViewById(R.id.name_text_view);
        ridesNum = navigationView.getHeaderView(0).findViewById(R.id.num_rides_text_view);
        fullName.setText(userDTO.getFirstName() + " " + userDTO.getLastName());


        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,main_screen_layout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        main_screen_layout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        //Todo set timer to fetch user details


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if(lastSelectedItem == itemId) {
            main_screen_layout.closeDrawer(GravityCompat.START);
            return true;
        }
        lastSelectedItem = itemId;
        if (itemId == R.id.nav_home) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, mapFragment)
                    .commit();
            Toast.makeText(this, "Home", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.nav_locations) {
            Toast.makeText(this, "My Location", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.nav_rides) {
            Toast.makeText(this, "My Rides", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.nav_profile) {
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, new ProfileFragment())
                .commit();
            Toast.makeText(this, "My Profile", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, "Logout", Toast.LENGTH_LONG).show();
        }
        main_screen_layout.closeDrawer(GravityCompat.START); return true;
    }
}