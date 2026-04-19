package lk.jiat.luxara.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import lk.jiat.luxara.R;
import lk.jiat.luxara.fragment.CartFragment;
import lk.jiat.luxara.fragment.CategoryFragment;
import lk.jiat.luxara.fragment.HomeFragment;
import lk.jiat.luxara.fragment.LoginFragment;
import lk.jiat.luxara.fragment.OffersPromotionsFragment;
import lk.jiat.luxara.fragment.ProfileFragment;
import lk.jiat.luxara.fragment.SettingFragment;
import lk.jiat.luxara.fragment.WishlistFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.side_navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);


        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();


        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());

        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (isNavigating) {
            return true;
        }
        isNavigating = true;

        try {
            int itemId = item.getItemId();
            Menu navMenu = navigationView.getMenu();
            Menu bottomNavMenu = bottomNavigationView.getMenu();


            for (int i = 0; i < navMenu.size(); i++){
                navMenu.getItem(i).setChecked(false);
            }

            for (int i = 0; i < bottomNavMenu.size(); i++){
                bottomNavMenu.getItem(i).setChecked(false);
            }

            if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
                loadFragment(new HomeFragment());
                navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
                bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
            } else if (itemId == R.id.bottom_nav_profile) {
                loadFragment(new ProfileFragment());
            } else if (itemId == R.id.side_nav_wishlist || itemId == R.id.bottom_nav_wishlist) {
                loadFragment(new WishlistFragment());
                navigationView.getMenu().findItem(R.id.side_nav_wishlist).setChecked(true);
                bottomNavigationView.getMenu().findItem(R.id.bottom_nav_wishlist).setChecked(true);
            } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
                loadFragment(new CartFragment());
                navigationView.getMenu().findItem(R.id.side_nav_cart).setChecked(true);
                bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);
            } else if (itemId == R.id.side_nav_settings) {
                loadFragment(new SettingFragment());
                navigationView.getMenu().findItem(R.id.side_nav_settings).setChecked(true);
            } else if (itemId == R.id.bottom_nav_categories) {
                loadFragment(new CategoryFragment());
                navigationView.getMenu().findItem(R.id.side_nav_categories).setChecked(true);
            } else if (itemId == R.id.side_nav_offers) {
                loadFragment(new OffersPromotionsFragment());
                navigationView.getMenu().findItem(R.id.side_nav_offers).setChecked(true);
            } else if (itemId == R.id.side_nav_login) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.side_nav_logout) {
                Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
            }

            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        } finally {
            isNavigating = false;
        }

        return true;
    }


    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
