package lk.jiat.luxara.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import lk.jiat.luxara.R;
import lk.jiat.luxara.databinding.ActivityMainBinding;
import lk.jiat.luxara.databinding.SideNavHeaderBinding;
import lk.jiat.luxara.fragment.CartFragment;
import lk.jiat.luxara.fragment.CategoryFragment;
import lk.jiat.luxara.fragment.HomeFragment;
import lk.jiat.luxara.fragment.OffersPromotionsFragment;
import lk.jiat.luxara.fragment.ProfileFragment;
import lk.jiat.luxara.fragment.SettingFragment;
import lk.jiat.luxara.fragment.WishlistFragment;
import lk.jiat.luxara.model.User;
import lk.jiat.luxara.util.CartHelper;

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
    private FirebaseAuth.AuthStateListener authStateListener;
    private ListenerRegistration userSnapshotListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sp = getSharedPreferences("luxara", MODE_PRIVATE);
        if (!sp.getBoolean("attrs_seeded", false)) {
            CartHelper.seedProductAttributes(() -> sp.edit().putBoolean("attrs_seeded", true).apply());
        }

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        // Initialize Header Binding
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            headerView = navigationView.inflateHeaderView(R.layout.side_nav_header);
        }
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

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

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Professional way to handle user state changes
        setupAuthStateListener();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateCheckedItems(R.id.side_nav_home);
        }
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                startObservingUser(user.getUid());
                updateMenuVisibility(true);
            } else {
                stopObservingUser();
                resetHeaderToGuest();
                updateMenuVisibility(false);
            }
        };
    }

    private void startObservingUser(String uid) {
        stopObservingUser(); // Remove any existing listener
        
        userSnapshotListener = firebaseFirestore.collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error: " + error.getMessage());
                        if (error.getMessage().contains("PERMISSION_DENIED")) {
                            Toast.makeText(this, "Fix Firestore Rules in Firebase Console!", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        User userModel = snapshot.toObject(User.class);
                        if (userModel != null) {
                            updateHeaderUI(userModel);
                        }
                    }
                });
    }

    private void stopObservingUser() {
        if (userSnapshotListener != null) {
            userSnapshotListener.remove();
            userSnapshotListener = null;
        }
    }

    private void updateHeaderUI(User user) {
        if (sideNavHeaderBinding == null) return;
        
        sideNavHeaderBinding.headerUserName.setText(user.getName());
        sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

        Glide.with(this)
                .load(user.getProfilePicUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(sideNavHeaderBinding.headerProfilePic);
    }

    private void resetHeaderToGuest() {
        if (sideNavHeaderBinding == null) return;
        sideNavHeaderBinding.headerUserName.setText("Guest User");
        sideNavHeaderBinding.headerUserEmail.setText("Sign in to your account");
        sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.ic_default_avatar);
    }

    private void updateMenuVisibility(boolean isLoggedIn) {
        Menu menu = navigationView.getMenu();
        setMenuItemVisible(menu, R.id.side_nav_login, !isLoggedIn);
        setMenuItemVisible(menu, R.id.side_nav_logout, isLoggedIn);
        setMenuItemVisible(menu, R.id.side_nav_wishlist, isLoggedIn);
        setMenuItemVisible(menu, R.id.side_nav_cart, isLoggedIn);
        setMenuItemVisible(menu, R.id.side_nav_offers, isLoggedIn);
    }

    private void setMenuItemVisible(Menu menu, int id, boolean visible) {
        MenuItem item = menu.findItem(id);
        if (item != null) item.setVisible(visible);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        stopObservingUser();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (isNavigating) return true;
        isNavigating = true;

        int itemId = item.getItemId();
        updateCheckedItems(itemId);

        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
        } else if (itemId == R.id.bottom_nav_profile) {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(this, SignInActivity.class));
            } else {
                loadFragment(new ProfileFragment());
            }
        } else if (itemId == R.id.side_nav_logout) {
            firebaseAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            loadFragment(new HomeFragment());
        } else if (itemId == R.id.side_nav_login) {
            startActivity(new Intent(this, SignInActivity.class));
        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            loadFragment(new CartFragment());
        } else if (itemId == R.id.side_nav_wishlist || itemId == R.id.bottom_nav_wishlist) {
            loadFragment(new WishlistFragment());
        } else if (itemId == R.id.side_nav_categories || itemId == R.id.bottom_nav_categories) {
            loadFragment(new CategoryFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        isNavigating = false;
        return true;
    }

    private void updateCheckedItems(int id) {
        Menu navMenu = navigationView.getMenu();
        Menu bottomMenu = bottomNavigationView.getMenu();
        
        // Clear all
        for (int i = 0; i < navMenu.size(); i++) navMenu.getItem(i).setChecked(false);
        for (int i = 0; i < bottomMenu.size(); i++) bottomMenu.getItem(i).setChecked(false);

        // Check selected
        MenuItem navItem = navMenu.findItem(id);
        if (navItem != null) navItem.setChecked(true);
        
        MenuItem bottomItem = bottomMenu.findItem(id);
        if (bottomItem != null) bottomItem.setChecked(true);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
