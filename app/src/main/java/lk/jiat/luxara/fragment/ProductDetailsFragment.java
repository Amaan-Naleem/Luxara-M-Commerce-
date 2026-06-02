package lk.jiat.luxara.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.List;

import lk.jiat.luxara.R;
import lk.jiat.luxara.adapter.ProductSliderAdapter;
import lk.jiat.luxara.adapter.SectionAdapter;
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
                            binding.productDetailsRating.setText(String.valueOf(product.getRating()));

                            binding.productDetailsTitle.setText(product.getTitle());
                            binding.productDetailsPrice.setText("LKR" + product.getPrice());

                            if (product.getAttributes() != null) {
                                product.getAttributes().forEach(attribute -> {
                                    Log.d("ATTR_NAME", String.valueOf(attribute.getName()));
                                    Log.d("ATTR_VALUES", String.valueOf(attribute.getValues()));
                                    renderAttribute(attribute, binding.productDetailsAttributeContainer);
                                });
                            }
                        }
                    }
                });
        loadTopSellproduct();
    }

    private void loadTopSellproduct() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Product> products = qds.toObjects(Product.class);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                            binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);

                            SectionAdapter adapter = new SectionAdapter(products, product -> {

                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                productDetailsFragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, productDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.productDetailsTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });
    }


    private void renderAttribute(Product.Attribute attribute, ViewGroup container) {

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);


        //create Label
        TextView label = new TextView(getContext());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                100,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        label.setLayoutParams(layoutParams);
        label.setText(attribute.getName());

        row.addView(label);


        //create Options
        ChipGroup group = new ChipGroup(getContext());
        group.setSelectionRequired(true);
        group.setSingleSelection(true);

        attribute.getValues().forEach(value -> {
            Chip chip = new Chip(getContext());
            chip.setCheckable(true);
            chip.setChipStrokeWidth(3f);
            if ("color".equalsIgnoreCase(attribute.getType())) {
                try {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
                } catch (Exception e) {
                    chip.setText(value);
                }
            } else {
                chip.setText(value);
            }


            group.addView(chip);
        });
        row.addView(group);
        container.addView(row);

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
