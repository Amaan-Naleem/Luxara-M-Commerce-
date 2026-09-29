package lk.jiat.luxara.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.luxara.R;
import lk.jiat.luxara.activity.SignInActivity;
import lk.jiat.luxara.adapter.SectionAdapter;
import lk.jiat.luxara.databinding.FragmentHomeBinding;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.util.CartHelper;
import lk.jiat.luxara.model.Product;


public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

                            binding.homeTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);

                            SectionAdapter adapter = new SectionAdapter(products, new SectionAdapter.OnListingItemClickListener() {
                                @Override
                                public void onListingItemClick(Product product) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("productId", product.getProductId());

                                    ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                    productDetailsFragment.setArguments(bundle);

                                    getParentFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_in_right, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_right)
                                            .replace(R.id.fragment_container, productDetailsFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }

                                @Override
                                public void onAddToCartClick(Product product, List<CartItem.Attribute> attributes) {
                                    CartHelper.addToCart(requireContext(), product, attributes);
                                }
                            });

                            binding.homeTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.homeTopSellSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });
    }

}