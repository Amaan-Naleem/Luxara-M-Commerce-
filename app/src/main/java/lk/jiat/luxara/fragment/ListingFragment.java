package lk.jiat.luxara.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.jiat.luxara.R;
import lk.jiat.luxara.adapter.ListingyAdapter;
import lk.jiat.luxara.databinding.FragmentListingBinding;
import lk.jiat.luxara.model.Product;


public class ListingFragment extends Fragment {

private FragmentListingBinding binding;
private ListingyAdapter adapter;
private String categoryId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            categoryId = getArguments().getString("categoryId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      binding = FragmentListingBinding.inflate(inflater,container,false);
      return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewLkistening.setLayoutManager(new GridLayoutManager(getContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Product p1 = new Product("pid1", "Cashmere Blend Oversized Coat",
//                "Premium winter oversized coat made with soft cashmere blend for luxury comfort.",
//                2450.0, "cat1",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                10, true);
//
//        Product p2 = new Product("pid2", "Slim Fit Formal Shirt",
//                "Elegant slim fit shirt perfect for office and formal occasions.",
//                1200.0, "cat1",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                20, true);
//
//        Product p3 = new Product("pid3", "Women Summer Dress",
//                "Lightweight and stylish summer dress for everyday fashion wear.",
//                1800.0, "cat2",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                15, true);
//
//        Product p4 = new Product("pid4", "Kids Cotton T-Shirt",
//                "Soft cotton t-shirt designed for maximum comfort for kids.",
//                800.0, "cat3",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                30, true);
//
//        Product p5 = new Product("pid5", "Running Sneakers",
//                "Comfortable and durable sneakers for daily running and sports use.",
//                2200.0, "cat4",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                25, true);
//
//        Product p6 = new Product("pid6", "Leather Handbag",
//                "Premium leather handbag with modern luxury design.",
//                3000.0, "cat5",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                12, true);
//
//        Product p7 = new Product("pid7", "Trending Hoodie",
//                "Stylish oversized hoodie from latest fashion collection.",
//                1600.0, "cat7",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                18, true);
//
//        Product p8 = new Product("pid8", "Discount Jeans",
//                "Comfortable slim jeans available at special offer price.",
//                1400.0, "cat8",
//                Arrays.asList("imageUrl1", "imageUrl2"),
//                40, true);
//
//
//        List<Product> list = Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8);
//        WriteBatch batch = db.batch();
//
//        for (Product p : list) {
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref, p);
//        }
//
//        batch.commit();

        db.collection("products")
                .whereEqualTo("categoryId",categoryId)
                .orderBy("title" , Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds ->{
                   if (!ds.isEmpty()){
                   List<Product> products = ds.toObjects(Product.class);
                       adapter = new ListingyAdapter(products, product -> {

                           Bundle bundle = new Bundle();
                           bundle.putString("productId", product.getProductId()); // or getPid() based on your model

                           ProductDetailsFragment fragment = new ProductDetailsFragment();
                           fragment.setArguments(bundle);

                           getParentFragmentManager().beginTransaction()
                                   .replace(R.id.fragment_container, fragment)
                                   .addToBackStack(null)
                                   .commit();

                       });
                     binding.recyclerViewLkistening.setAdapter(adapter);
                   }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore","Error:"+e.getMessage());
                    }
                });
        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}