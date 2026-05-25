package lk.jiat.luxara.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import lk.jiat.luxara.R;
import lk.jiat.luxara.adapter.ProductSliderAdapter;
import lk.jiat.luxara.databinding.FragmentProductDetailsBinding;
import lk.jiat.luxara.model.Product;

public class ProductDetailsFragment extends Fragment {
    private FragmentProductDetailsBinding binding;
    private String productId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        //load product details
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        Product product = qds.getDocuments().get(0).toObject(Product.class);

                        if (product != null && product.getImages() != null) {
                            ProductSliderAdapter adapter = new ProductSliderAdapter(product.getImages());
                            binding.productImageSlider.setAdapter(adapter);


                            binding.dotsIndicator.attachTo(binding.productImageSlider);
//                          binding.productDetailsRating.setRating(product.getRating());

                            binding.productDetailsTitle.setText(product.getTitle());
                            binding.productDetailsPrice.setText("LKR" + product.getPrice());
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }
    }
}
