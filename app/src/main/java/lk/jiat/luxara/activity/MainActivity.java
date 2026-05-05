package lk.jiat.luxara.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.jiat.luxara.R;
import lk.jiat.luxara.databinding.ActivityMainBinding;
import lk.jiat.luxara.databinding.SideNavHeaderBinding;
import lk.jiat.luxara.fragment.CartFragment;
import lk.jiat.luxara.fragment.CategoryFragment;
import lk.jiat.luxara.fragment.HomeFragment;
import lk.jiat.luxara.fragment.LoginFragment;
import lk.jiat.luxara.fragment.OffersPromotionsFragment;
import lk.jiat.luxara.fragment.ProfileFragment;
import lk.jiat.luxara.fragment.SettingFragment;
import lk.jiat.luxara.fragment.WishlistFragment;
import lk.jiat.luxara.model.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private SideNavHeaderBinding sideNavHeaderBinding;
    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private boolean isNavigating = false;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View headerView = binding.sideNavigationView.getHeaderView(0);
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

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

        // check and load user details
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        FirebaseUser currentuser = firebaseAuth.getCurrentUser();
        if (currentuser != null) {
            firebaseFirestore.collection("users").document(currentuser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user != null) {
                                sideNavHeaderBinding.headerUserName.setText(user.getName());
                                sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

                                Glide.with(MainActivity.this)
                                        .load(user.getProfilePicUrl())
                                        .circleCrop()
                                        .into(sideNavHeaderBinding.headerProfilePic);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Firestore", "Error: " + e.getMessage());
                    });
            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);

            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_offers).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(true);
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


            for (int i = 0; i < navMenu.size(); i++) {
                navMenu.getItem(i).setChecked(false);
            }

            for (int i = 0; i < bottomNavMenu.size(); i++) {
                bottomNavMenu.getItem(i).setChecked(false);
            }

            if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
                loadFragment(new HomeFragment());
                navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
                bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
            } else if (itemId == R.id.bottom_nav_profile) {
                if (firebaseAuth.getCurrentUser() == null){
                    Intent intent = new Intent(MainActivity.this , SignInActivity.class);
                    startActivity(intent);
                    finish();
                }
                loadFragment(new ProfileFragment());
                bottomNavigationView.getMenu().findItem(R.id.bottom_nav_profile).setChecked(true);
            } else if (itemId == R.id.side_nav_wishlist || itemId == R.id.bottom_nav_wishlist) {
                loadFragment(new WishlistFragment());
                navigationView.getMenu().findItem(R.id.side_nav_wishlist).setChecked(true);
                bottomNavigationView.getMenu().findItem(R.id.bottom_nav_wishlist).setChecked(true);
            } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
                if (firebaseAuth.getCurrentUser() == null){
                    Intent intent = new Intent(MainActivity.this , SignInActivity.class);
                    startActivity(intent);
                    finish();
                }
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
                firebaseAuth.signOut();
                loadFragment(new HomeFragment()) ;


                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.side_nav_menu);


                navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
                navigationView.inflateHeaderView(R.layout.side_nav_header);
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
