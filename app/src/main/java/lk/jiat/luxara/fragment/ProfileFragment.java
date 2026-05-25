package lk.jiat.luxara.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import lk.jiat.luxara.R;
import lk.jiat.luxara.databinding.FragmentProfileBinding;
import lk.jiat.luxara.model.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ListenerRegistration userListener;
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            observeUserData(user.getUid());
        }

        binding.btnUpdateProfile.setOnClickListener(v -> updateProfile());
        
        // TODO: Implement image picker for binding.btnEditImage
        binding.btnEditImage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Image upload feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeUserData(String uid) {
        userListener = firebaseFirestore.collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("ProfileFragment", "Error fetching user", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        currentUser = snapshot.toObject(User.class);
                        if (currentUser != null) {
                            displayUserData(currentUser);
                        }
                    }
                });
    }

    private void displayUserData(User user) {
        binding.tvProfileName.setText(user.getName());
        binding.tvProfileEmail.setText(user.getEmail());
        binding.etName.setText(user.getName());
        binding.etEmail.setText(user.getEmail());

        if (getActivity() != null && !getActivity().isFinishing()) {
            Glide.with(this)
                    .load(user.getProfilePicUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.profileImage);
        }
    }

    private void updateProfile() {
        String newName = binding.etName.getText().toString().trim();

        if (newName.isEmpty()) {
            binding.etName.setError("Name is required");
            return;
        }

        if (currentUser == null) return;

        binding.btnUpdateProfile.setEnabled(false);
        binding.profileProgressBar.setVisibility(View.VISIBLE);

        firebaseFirestore.collection("users").document(currentUser.getUid())
                .update("name", newName)
                .addOnSuccessListener(aVoid -> {
                    binding.btnUpdateProfile.setEnabled(true);
                    binding.profileProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnUpdateProfile.setEnabled(true);
                    binding.profileProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userListener != null) {
            userListener.remove();
        }
        binding = null;
    }
}
