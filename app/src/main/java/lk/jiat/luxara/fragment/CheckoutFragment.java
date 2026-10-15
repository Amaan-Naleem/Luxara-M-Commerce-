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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.jiat.luxara.R;
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

                req.setMerchantId("1231416"); // real numeric ID from PayHere sandbox
                req.setMerchantSecret("MzUyNzAzODcwMTExODI0OTU5NjQxOTY2NjE0MDgzOTQ1MDg5Mzc5"); // secret of the app registered with package lk.jiat.luxara
                req.setCurrency("LKR");
                req.setAmount(Math.round(total * 100.0) / 100.0);
                req.setOrderId("ORD-" + System.currentTimeMillis());
                req.setItemsDescription("");
                req.getCustomer().setFirstName(binding.shippingDetailsName.getText().toString());
                req.getCustomer().setLastName("Naleem");
                req.getCustomer().setEmail("amaannaleem2006@gmail.com");
                req.getCustomer().setPhone("0743027890");
                req.getCustomer().getAddress().setAddress("No.1, Galle Road");
                req.getCustomer().getAddress().setCity("Galle");
                req.getCustomer().getAddress().setCountry("Sri Lanka");

//                req.setNotifyUrl("https://luxara.requestcatcher.com/");


                Intent intent = new Intent(getActivity(), PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

                payhereLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "Loading totals, try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrder(String uid) {
        {
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
                        order.setOrderDate(Timestamp.now());

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
        }
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
                        Intent data = result.getData();
                        Log.e("PAYHERE", "resultCode=" + result.getResultCode() + ", data=" + data);

                        if (data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                            Object extra = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                            Log.e("PAYHERE", "result=" + extra);

                            if (extra instanceof PHResponse) {
                                PHResponse<?> response = (PHResponse<?>) extra;
                                String msg = response.toString();
                                if (response.getData() instanceof StatusResponse) {
                                    msg = ((StatusResponse) response.getData()).getMessage();
                                }
                                if (response.isSuccess()) {
                                        StatusResponse statusResponse = (StatusResponse) response.getData();
                                        saveOrder(statusResponse  );

                                    Log.i("PAYHERE", "Payment successful: " + msg);
                                } else {
                                    Log.e("PAYHERE", "Payment failed: " + msg);
                                }
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "PayHere: " + msg, Toast.LENGTH_LONG).show();
                                }
                            }
                        } else if (getContext() != null) {
                            Toast.makeText(getContext(), "PayHere closed, no result (code " + result.getResultCode() + ")", Toast.LENGTH_LONG).show();
                        }
                    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void saveOrder(StatusResponse  statusResponse){
        getCartItems(cartItems -> {

            String uid = firebaseAuth.getCurrentUser().getUid();

            Order order = new Order();
            order.setOrderId(String.valueOf(System.currentTimeMillis()));
            order.setUserId(uid);
            order.setTotalAmount(total);
            order.setStatus("PAID");
            order.setOrderDate(Timestamp.now());


            String shipping_name = binding.shippingDetailsName.getText().toString();
            String shipping_email = binding.shippingDetailsEmail.getText().toString();
            String shipping_contact = binding.shippingDetailsContact.getText().toString();
            String shipping_address1 = binding.shippingDetailsAddress1.getText().toString();
            String shipping_address2 = binding.shippingDetailsAddress2.getText().toString();
            String shipping_city = binding.shippingDetailsCity.getText().toString();
            String shipping_postCode = binding.shippingDetailsPostal.getText().toString();

            Order.Address shippingAddress = Order.Address.builder().name(shipping_name).email(shipping_email).contact(shipping_contact).address1(shipping_address1).address2(shipping_address2).city(shipping_city).postcode(shipping_postCode).build();

            order.setShippingAddress(shippingAddress);

            if (!binding.shippingDetailsCheckBilling.isChecked()) {
                String billing_name = binding.billingDetailsName.getText().toString();
                String billing_email = binding.billingDetailsEmail.getText().toString();
                String billing_contact = binding.billingDetailsContact.getText().toString();
                String billing_address1 = binding.billingDetailsAddress1.getText().toString();
                String billing_address2 = binding.billingDetailsAddress2.getText().toString();
                String billing_city = binding.billingDetailsCity.getText().toString();
                String billing_postCode = binding.billingDetailsPostal.getText().toString();

                Order.Address billingAddress = Order.Address.builder().name(billing_name).email(billing_email).contact(billing_contact).address1(billing_address1).address2(billing_address2).city(billing_city).postcode(billing_postCode).build();

                order.setBillingAddress(billingAddress);
            }


            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });

            List<Order.OrderItem> orderItems = new ArrayList<>();

            getProductsByIds(productIds, data -> {

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());

                    if (product != null) {

                        List<Order.OrderItem.Attribute> attributes = new ArrayList<>();

                        for (CartItem.Attribute at : cartItem.getAttributes()) {
                            Order.OrderItem.Attribute attribute = Order.OrderItem.Attribute.builder().name(at.getName()).value(at.getValue()).build();

                            attributes.add(attribute);
                        }

                        Order.OrderItem orderItem = Order.OrderItem.builder().productId(cartItem.getProductId()).unitPrice(product.getPrice()).quantity(cartItem.getQuantity()).attributes(attributes).build();
                        orderItems.add(orderItem);



                        order.setOrderItems(orderItems);

                    }
                }
                db.collection("orders").document().set(order).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Order Saved!", Toast.LENGTH_SHORT).show();

                    // Clear cart
                    db.collection("users").document(uid).collection("cart")
                            .get()
                            .addOnSuccessListener(qds -> {
                                qds.getDocuments().forEach(ds -> {
                                    ds.getReference().delete();
                                });
                            });


                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();

                });

            });


        });

    }
}