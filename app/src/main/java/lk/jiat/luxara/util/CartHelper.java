package lk.jiat.luxara.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.luxara.activity.SignInActivity;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.model.Product;

public class CartHelper {

    private static List<Product.Attribute> defaultAttributes() {
        List<Product.Attribute> list = new ArrayList<>();
        list.add(new Product.Attribute("Size", "text", Arrays.asList("Small", "Medium", "Large")));
        list.add(new Product.Attribute("Color", "color", Arrays.asList("#000000", "#FFFFFF", "#C9A24D", "#8B0000")));
        return list;
    }

    private static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density);
    }

    // Call ONCE (e.g. MainActivity.onCreate), then remove the call.
    public static void seedProductAttributes(Runnable onDone) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").get().addOnSuccessListener(qs -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                List<Map<String, Object>> attrs = new ArrayList<>();

                Map<String, Object> size = new HashMap<>();
                size.put("name", "Size");
                size.put("type", "text");
                size.put("values", Arrays.asList("Small", "Medium", "Large"));
                attrs.add(size);

                Map<String, Object> color = new HashMap<>();
                color.put("name", "Color");
                color.put("type", "color");
                color.put("values", Arrays.asList("#000000", "#FFFFFF", "#C9A24D", "#8B0000"));
                attrs.add(color);

                batch.update(doc.getReference(), "attributes", attrs);
            }
            batch.commit().addOnSuccessListener(u -> { if (onDone != null) onDone.run(); });
        });
    }

    // Builds Size / Color chip selectors inside the card container.
    public static Map<String, ChipGroup> bindAttributes(Context context, ViewGroup container, Product product) {
        container.removeAllViews();
        Map<String, ChipGroup> groups = new LinkedHashMap<>();

        List<Product.Attribute> attrs = product.getAttributes();
        if (attrs == null || attrs.isEmpty()) attrs = defaultAttributes();

        for (Product.Attribute attr : attrs) {
            if (attr.getValues() == null || attr.getValues().isEmpty()) continue;

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView label = new TextView(context);
            label.setText(attr.getName());
            label.setTextSize(11f);
            label.setTextColor(Color.parseColor("#9A9A9A"));
            label.setLayoutParams(new LinearLayout.LayoutParams(dp(context, 40),
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            row.addView(label);

            ChipGroup group = new ChipGroup(context);
            group.setSingleSelection(true);
            group.setSelectionRequired(true);
            group.setChipSpacingHorizontal(dp(context, 4));
            group.setChipSpacingVertical(0);

            boolean first = true;
            for (String value : attr.getValues()) {
                Chip chip = new Chip(context);
                chip.setId(View.generateViewId());
                chip.setCheckable(true);
                chip.setTag(value);
                chip.setChipStrokeWidth(2f);
                chip.setEnsureMinTouchTargetSize(false);
                chip.setChipMinHeight(dp(context, 24));
                chip.setTextSize(10f);
                chip.setChipStartPadding(dp(context, 2));
                chip.setChipEndPadding(dp(context, 2));

                if ("color".equalsIgnoreCase(attr.getType())) {
                    try {
                        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
                        chip.setText("  ");
                    } catch (Exception e) {
                        chip.setText(value);
                    }
                } else {
                    chip.setText(value);
                }

                group.addView(chip);
                if (first) {
                    group.check(chip.getId());
                    first = false;
                }
            }
            row.addView(group);
            container.addView(row);
            groups.put(attr.getName(), group);
        }
        return groups;
    }

    public static List<CartItem.Attribute> getSelected(Map<String, ChipGroup> groups) {
        List<CartItem.Attribute> selected = new ArrayList<>();
        if (groups == null) return selected;
        for (Map.Entry<String, ChipGroup> e : groups.entrySet()) {
            int id = e.getValue().getCheckedChipId();
            if (id != View.NO_ID) {
                Chip chip = e.getValue().findViewById(id);
                if (chip != null) {
                    selected.add(new CartItem.Attribute(e.getKey(), String.valueOf(chip.getTag())));
                }
            }
        }
        return selected;
    }

    public static void addToCart(Context context, Product product, List<CartItem.Attribute> attributes) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            context.startActivity(new Intent(context, SignInActivity.class));
            return;
        }

        CartItem cartItem = new CartItem(product.getProductId(), 1, attributes);
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("cart").document(product.getProductId())
                .set(cartItem)
                .addOnSuccessListener(unused ->
                        Toast.makeText(context, "Item added to cart!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(err ->
                        Toast.makeText(context, "Failed: " + err.getMessage(), Toast.LENGTH_LONG).show());
    }
}
