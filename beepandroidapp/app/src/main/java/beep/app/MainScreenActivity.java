package beep.app;

import static java.security.AccessController.getContext;

import static beep.app.util.Constants.INVITE;
import static beep.app.util.Constants.IS_USERS;
import static beep.app.util.Constants.REGISTER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import beep.app.search.ContactItem;
import beep.app.search.ContactItemAdapter;
import beep.app.util.http.HttpClientUtil;
import location.LocationDTO;
import login.UserDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import search.UserPhoneExistDTO;

public class MainScreenActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 123;
    private static boolean CONTACT_HAS_FETCHED = false;
    private static boolean CONTACT_THREAD_HAS_CALLED = false;

    private DrawerLayout main_screen_layout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private UserDTO userDTO;
    private TextView fullName;
    private TextView ridesNum;
    private int lastSelectedItem = R.id.nav_home;
    private MapFragment mapFragment;
    private FragmentManager fragmentManager;


    private SearchView searchView;
    private RecyclerView contactRecyclerView;
    private ContactItemAdapter contactAdapter;
    private List<ContactItem> contactList;
    private final Map<String, Boolean> phoneNumberToHasUser = new HashMap<>();

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
        searchView = findViewById(R.id.searchView);
        contactRecyclerView = findViewById(R.id.contactRecyclerView);
        fullName = navigationView.getHeaderView(0).findViewById(R.id.name_text_view);
        ridesNum = navigationView.getHeaderView(0).findViewById(R.id.num_rides_text_view);

        fullName.setText(userDTO.getFirstName() + " " + userDTO.getLastName());

        setSupportActionBar(toolbar);

        contactRecyclerView.bringToFront();
        searchView.bringToFront();
        navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,main_screen_layout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        main_screen_layout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        contactList = new ArrayList<>();
        contactAdapter = new ContactItemAdapter(contactList, item -> {
            contactItemClicked(item);
        });
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setAdapter(contactAdapter);

        addSearchViewListeners();
    }

    private void requestContactsPermission() {
        // Check if the permission is not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // Permission already granted, proceed with contact retrieval
            updateContactList("");
        }
    }
    private void updateContactList(String query) {
        // Query contacts based on the search query
        contactList.clear();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                new String[]{"%" + query + "%"},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if(!CONTACT_HAS_FETCHED){
            if(!CONTACT_THREAD_HAS_CALLED)
                updateAndFetchFirstTime(query);
        }else {

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (cursor.moveToNext()) {
                    // Check if the column indices are valid
                    if (nameIndex >= 0 && phoneNumberIndex >= 0) {
                        String name = cursor.getString(nameIndex);
                        String phoneNumber = cursor.getString(phoneNumberIndex);
                        if(phoneNumberToHasUser.containsKey(phoneNumber))
                            contactList.add(new ContactItem(name,phoneNumber, phoneNumberToHasUser.get(phoneNumber)));
                    }
                }

                cursor.close();
            }

            // Update the RecyclerView with the new contact list
            contactAdapter.notifyDataSetChanged();
            if(searchView.hasFocus())
                contactRecyclerView.setVisibility(contactList.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }
    private void updateAndFetchFirstTime(String query) {
        Thread fetchItemsAndShow = new Thread(() -> {
            List<UserPhoneExistDTO> listPhoneUsersDTO = new ArrayList<>();

            buildListOfContacts(listPhoneUsersDTO);

            listPhoneUsersDTO = isUsersApiList(listPhoneUsersDTO);

            listPhoneUsersDTO.stream().forEach(dto -> phoneNumberToHasUser.put(dto.getPhoneNumber(),dto.isHasUser()));

            CONTACT_HAS_FETCHED = true;
            runOnUiThread(() -> {
                updateContactList(query);
            });
        });
        CONTACT_THREAD_HAS_CALLED = true;
        fetchItemsAndShow.start();
    }

    private void buildListOfContacts(List<UserPhoneExistDTO> listPhoneUsersDTO) {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                // Check if the column indices are valid
                if (nameIndex >= 0 && phoneNumberIndex >= 0) {
                    String phoneNumber = cursor.getString(phoneNumberIndex);
                    listPhoneUsersDTO.add(new UserPhoneExistDTO(phoneNumber));
                }
            }
            cursor.close();
        }
    }
    private static List<UserPhoneExistDTO> isUsersApiList(List<UserPhoneExistDTO> listPhoneUsersDTO) {
        String finalUrl = HttpUrl
                .parse(IS_USERS)
                .newBuilder()
                .build()
                .toString();

        Gson gson = new Gson();
        String json = gson.toJson(listPhoneUsersDTO);
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        try {
            Response response = HttpClientUtil.runSync(request);
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                TypeToken<ArrayList<UserPhoneExistDTO>> typeToken = new TypeToken<ArrayList<UserPhoneExistDTO>>() {};
                listPhoneUsersDTO = gson.fromJson(responseBody, typeToken.getType());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listPhoneUsersDTO;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateContactList("");
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                // You may want to explain why you need the permission before requesting again
            }
        }
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
        } else if (itemId == R.id.nav_locations) {
            Toast.makeText(this, "My Location", Toast.LENGTH_LONG).show();
        } else if (itemId == R.id.nav_rides) {
            RidesFragment ridesFragment = new RidesFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ridesFragment)
                    .commit();
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

    private void addSearchViewListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search query submission (if needed)
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle search query text change
                updateContactList(newText);
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v,hasFocus) -> {
            // Request permission to read contacts when the search view gains focus
            if (hasFocus) {
                requestContactsPermission();
            }
        });

        // Set a close listener for the search view
        searchView.setOnCloseListener(() -> {
            // Hide the contact list when the search view is closed
            contactRecyclerView.setVisibility(View.GONE);
            return false;
        });
    }

    private void contactItemClicked(ContactItem item) {
        String finalUrl = HttpUrl
                .parse(INVITE)
                .newBuilder()
                .build()
                .toString();

        Gson gson = new Gson();
        LocationDTO locationDTO;
        if(mapFragment.getCurrentLocation() != null)
            locationDTO = new LocationDTO(mapFragment.getShortAddress(getApplicationContext(),mapFragment.getCurrentLocation()), mapFragment.getCurrentLocation().getLatitude(),mapFragment.getCurrentLocation().getLongitude(),mapFragment.getLastBearing());
        else
            locationDTO = new LocationDTO(null, null,null,null);
        String json = gson.toJson(locationDTO);

        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(finalUrl + item.getPhoneNumber())
                .put(requestBody)
                .build();
        HttpClientUtil.runAsync(request, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainScreenActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    System.out.println(e.toString());
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                runOnUiThread(() -> {
                    Toast.makeText(MainScreenActivity.this, responseBody, Toast.LENGTH_LONG).show();
                });
                if (response.code() == 400) { //Invalid code
                }

            }
        });
    }
}