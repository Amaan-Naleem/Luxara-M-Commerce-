
package lk.jiat.luxara.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.jiat.luxara.databinding.FragmentCheckoutBinding;
import lk.jiat.luxara.listener.FirestoreCallBack;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.model.Order;
import lk.jiat.luxara.model.Product;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private double total;
    private boolean paymentActive;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        binding.checkoutBtnProceed.setOnClickListener(v -> {
            InitRequest req = new InitRequest();
        });
        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();


        double shippingCost = 400;
        binding.checkoutShipping.setText(String.format(Locale.US, "LKR %,.2f", shippingCost));

        getCartItems(cartItems -> {
            if (binding == null) return;
            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                if (cartItem.getProductId() != null) {
                    productIds.add(cartItem.getProductId());
                }
            });
            getProductsByIds(productIds, data -> {
                if (binding == null) return;
                double subTotal = 0;

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());
                    if (product != null) {
                        subTotal += product.getPrice() * cartItem.getQuantity();
                    }
                }

                total = subTotal + shippingCost;

                binding.checkoutSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subTotal));
                binding.checkoutTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
                paymentActive = true;
            });
        });


        binding.checkoutBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        binding.shippingDetailsCheckBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.layoutBillingSection.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });


        binding.checkoutBtnProceed.setOnClickListener(v -> {
            if (paymentActive) {
                InitRequest req = new InitRequest();
                req.setSandBox(true);

                req.setMerchantId("1210XXX");
                req.setMerchantSecret("MzUyNzAzODcwMTExODI0OTU5NjQxOTY2NjE0MDgzOTQ1MDg5Mzc5");
                req.setCurrency("LKR");
                req.setAmount(total);
                req.setOrderId("ESOI-001");
                req.getCustomer().setFirstName("Amaan");
                req.getCustomer().setLastName("Naleem");
                req.getCustomer().setEmail("amaannaleem2006@gmail.com");
                req.getCustomer().setPhone("+94743027890");
                req.getCustomer().getAddress().setAddress("No.1, Galle Road");
                req.getCustomer().getAddress().setCity("Galle");
                req.getCustomer().getAddress().setCountry("Sri Lanka");

                req.setNotifyUrl("https://luxara.requestcatcher.com/");

                Intent intent = new Intent(getActivity(), PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            }

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

    private void getCartItems(FirestoreCallBack<List<CartItem>> callBack) {

        String vid = firebaseAuth.getCurrentUser().getUid();

        db.collection("users").document(vid).collection("cart")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<CartItem> cartItems = qds.toObjects(CartItem.class);
                            callBack.onCallback(cartItems);
                        }
                    }

                });

    }

    private void getProductsByIds(List<String> productIds, FirestoreCallBack<Map<String, Product>> callBack) {

        Map<String, Product> products = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            callBack.onCallback(products);
            return;
        }

        db.collection("products").whereIn("productId", productIds).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                qds.getDocuments().forEach(ds -> {
                    Product product = ds.toObject(Product.class);
                    if (product != null) {
                        products.put(product.getProductId(), product);
                    }
                });
                callBack.onCallback(products);
            }
        });

    }

//    private void getSubTotal() {
//        List<CartItem> cartItems = getCartItems();
//
//        List<String> productsIds = new ArrayList<>();
//
//        cartItems.forEach(cartItem -> {
//            productsIds.add(cartItem.getProductId());
//        });
//        Map<String, Product> products = getProductsByIds(productsIds);
//
//        double subTotal = 0;
//        for (CartItem cartItem : cartItems) {
//            Product product = products.get(cartItem.getProductId());
//            if (product != null) {
//                subTotal += product.getPrice() * cartItem.getQuantity();
//            }
//        }
//        return subTotal;
//
//
//    }


    private final ActivityResultLauncher<Intent> payhereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                                PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                                if (response != null && response.isSuccess()) {
                                    StatusResponse statusResponse = response.getData();
                                    Log.i("PAYHERE", "payment Successfull");
                                } else {
                                    Log.e("PAYHERE", response.getData().getMessage());
                                }
                            }
                        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            Log.e("PAYHERE", "Payment Cancelled !");

                        }
                    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}