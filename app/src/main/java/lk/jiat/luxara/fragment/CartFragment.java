package lk.jiat.luxara.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lk.jiat.luxara.R;
import lk.jiat.luxara.adapter.CartAdapter;
import lk.jiat.luxara.databinding.FragmentCartBinding;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.model.Product;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private ListenerRegistration registration;
    private CollectionReference cartRef;
    private CartAdapter adapter;
    private final Map<String, Product> productCache = new HashMap<>();

    public CartFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.cartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        cartRef = db.collection("users").document(uid).collection("cart");

        adapter = new CartAdapter(new CartAdapter.OnCartActionListener() {
            @Override
            public void onRemove(CartAdapter.Row row) {
                cartRef.document(row.item.getProductId()).delete();
            }

            @Override
            public void onQuantityChange(CartAdapter.Row row, int newQuantity) {
                cartRef.document(row.item.getProductId()).update("quantity", newQuantity);
            }
        });
        binding.cartRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.cartRecycler.setAdapter(adapter);

        binding.cartBtnProceed.setOnClickListener(v ->
                Toast.makeText(getContext(), "Checkout coming soon", Toast.LENGTH_SHORT).show());

        registration = cartRef.addSnapshotListener((snap, e) -> {
            if (binding == null || snap == null) return;
            List<CartItem> items = snap.toObjects(CartItem.class);
            loadProductsThenRender(db, items);
        });


        binding.cartBtnProceed.setOnClickListener(v ->{
            CheckoutFragment checkoutFragment = new CheckoutFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, checkoutFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadProductsThenRender(FirebaseFirestore db, List<CartItem> items) {
        Set<String> missing = new HashSet<>();
        for (CartItem i : items) {
            if (i.getProductId() != null && !productCache.containsKey(i.getProductId()))
                missing.add(i.getProductId());
        }

        if (missing.isEmpty()) {
            render(items);
            return;
        }

        int[] remaining = {missing.size()};
        for (String id : missing) {
            db.collection("products").whereEqualTo("productId", id).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            Product p = task.getResult().getDocuments().get(0).toObject(Product.class);
                            if (p != null) productCache.put(id, p);
                        }
                        if (--remaining[0] == 0) render(items);
                    });
        }
    }

    private void render(List<CartItem> items) {
        if (binding == null) return;

        List<CartAdapter.Row> rows = new ArrayList<>();
        double total = 0;
        for (CartItem i : items) {
            Product p = productCache.get(i.getProductId());
            if (p == null) continue;
            rows.add(new CartAdapter.Row(i, p));
            total += p.getPrice() * i.getQuantity();
        }

        adapter.setRows(rows);

        boolean empty = rows.isEmpty();
        binding.cartEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.cartCartItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.cartTextTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null) registration.remove();
        binding = null;
    }
}
