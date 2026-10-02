package lk.jiat.luxara.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.luxara.databinding.FragmentCheckoutBinding;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.model.Order;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();

        binding.checkoutBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.shippingDetailsCheckBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.layoutBillingSection.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });

        binding.checkoutBtnProceed.setOnClickListener(v -> {
            db.collection("users").document(uid).collection("cart")
                    .get()
                    .addOnSuccessListener(qds -> {
                        if (binding == null) return;

                        if (qds.isEmpty()) {
                            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<CartItem> cartItems = qds.toObjects(CartItem.class);

                        Order order = new Order();
                        order.setOrderId(String.valueOf(System.currentTimeMillis()));
                        order.setUserId(uid);
                        order.setStatus("PENDING");
                        order.setOrderDate(System.currentTimeMillis());

                        Order.Address shippingAddress = Order.Address.builder()
                                .name(binding.shippingDetailsName.getText().toString())
                                .email(binding.shippingDetailsEmail.getText().toString())
                                .contact(binding.shippingDetailsContact.getText().toString())
                                .address1(binding.shippingDetailsAddress1.getText().toString())
                                .address2(binding.shippingDetailsAddress2.getText().toString())
                                .city(binding.shippingDetailsCity.getText().toString())
                                .postcode(binding.shippingDetailsPostal.getText().toString())
                                .build();

                        order.setShippingAddress(shippingAddress);

                        if (binding.shippingDetailsCheckBilling.isChecked()) {
                            order.setBillingAddress(shippingAddress);
                        } else {
                            Order.Address billingAddress = Order.Address.builder()
                                    .name(binding.billingDetailsName.getText().toString())
                                    .email(binding.billingDetailsEmail.getText().toString())
                                    .contact(binding.billingDetailsContact.getText().toString())
                                    .address1(binding.billingDetailsAddress1.getText().toString())
                                    .address2(binding.billingDetailsAddress2.getText().toString())
                                    .city(binding.billingDetailsCity.getText().toString())
                                    .postcode(binding.billingDetailsPostal.getText().toString())
                                    .build();

                            order.setBillingAddress(billingAddress);
                        }

                        List<Order.OrderItem> orderItems = new ArrayList<>();

                        for (CartItem cartItem : cartItems) {

                            List<Order.OrderItem.Attribute> attributes = new ArrayList<>();

                            if (cartItem.getAttributes() != null) {
                                for (CartItem.Attribute at : cartItem.getAttributes()) {
                                    attributes.add(Order.OrderItem.Attribute.builder()
                                            .name(at.getName())
                                            .value(at.getValue())
                                            .build());
                                }
                            }

                            orderItems.add(Order.OrderItem.builder()
                                    .productId(cartItem.getProductId())
                                    .unitPrice(0)
                                    .quantity(cartItem.getQuantity())
                                    .attributes(attributes)
                                    .build());
                        }

                        order.setOrderItems(orderItems);

                        db.collection("orders").document()
                                .set(order)
                                .addOnSuccessListener(aVoid -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Order Saved !", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}