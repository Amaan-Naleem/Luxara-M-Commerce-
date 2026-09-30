package lk.jiat.luxara.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.luxara.R;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.model.Product;
import lk.jiat.luxara.util.AnimUtils;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public static class Row {
        public final CartItem item;
        public final Product product;

        public Row(CartItem item, Product product) {
            this.item = item;
            this.product = product;
        }
    }

    public interface OnCartActionListener {
        void onRemove(Row row);

        void onQuantityChange(Row row, int newQuantity);
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnCartActionListener listener;
    private int lastAnimated = -1;

    public CartAdapter(OnCartActionListener listener) {
        this.listener = listener;
    }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Row row = rows.get(position);
        Product p = row.product;
        CartItem c = row.item;

        boolean animate = position > lastAnimated;
        if (animate) lastAnimated = position;
        AnimUtils.enter(h.itemView, position, animate);

        h.title.setText(p.getTitle());
        h.price.setText(h.itemView.getContext().getString(R.string.currency_lkr, String.valueOf(p.getPrice())));
        h.qty.setText(String.valueOf(c.getQuantity()));

        bindAttributes(h.itemView.getContext(), h.attrs, c.getAttributes());

        if (p.getImages() != null && !p.getImages().isEmpty()) {
            Glide.with(h.itemView.getContext()).load(p.getImages().get(0)).centerCrop().into(h.image);
        }

        h.plus.setOnClickListener(v -> {
            AnimUtils.bounce(h.qty);
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Row currentRow = rows.get(pos);
            if (listener != null && currentRow.item.getQuantity() < 10)
                listener.onQuantityChange(currentRow, currentRow.item.getQuantity() + 1);
        });
        h.minus.setOnClickListener(v -> {
            AnimUtils.bounce(h.qty);
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Row currentRow = rows.get(pos);
            if (listener != null && currentRow.item.getQuantity() > 1)
                listener.onQuantityChange(currentRow, currentRow.item.getQuantity() - 1);
        });
        h.remove.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Row currentRow = rows.get(pos);
            h.itemView.animate().alpha(0f).translationX(200f).setDuration(220)
                    .withEndAction(() -> {
                        h.itemView.setAlpha(1f);
                        h.itemView.setTranslationX(0f);
                        if (listener != null) listener.onRemove(currentRow);
                    }).start();
        });
    }

    private void bindAttributes(Context ctx, LinearLayout container, List<CartItem.Attribute> attrs) {
        container.removeAllViews();
        if (attrs == null) return;
        float d = ctx.getResources().getDisplayMetrics().density;

        for (CartItem.Attribute a : attrs) {
            if (a.getValue() == null) continue;
            boolean isColor = "color".equalsIgnoreCase(a.getName()) || a.getValue().startsWith("#");

            if (isColor) {
                View dot = new View(ctx);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (18 * d), (int) (18 * d));
                lp.setMarginEnd((int) (8 * d));
                dot.setLayoutParams(lp);
                try {
                    GradientDrawable g = new GradientDrawable();
                    g.setShape(GradientDrawable.OVAL);
                    g.setColor(Color.parseColor(a.getValue()));
                    g.setStroke((int) (1 * d), Color.parseColor("#D9D2C7"));
                    dot.setBackground(g);
                    container.addView(dot);
                } catch (Exception ignored) {
                }
            } else {
                TextView t = new TextView(ctx);
                t.setText(String.format("%s: %s", a.getName(), a.getValue()));
                t.setTextSize(11f);
                t.setTextColor(Color.parseColor("#6B625A"));
                t.setPadding((int) (8 * d), (int) (3 * d), (int) (8 * d), (int) (3 * d));
                t.setBackgroundResource(R.drawable.bg_savings_chip);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMarginEnd((int) (8 * d));
                t.setLayoutParams(lp);
                container.addView(t);
            }
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, remove;
        TextView title, price, qty, plus, minus;
        LinearLayout attrs;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_cart_remove);
            remove = itemView.findViewById(R.id.cartRemove);
            title = itemView.findViewById(R.id.cart_title);
            attrs = itemView.findViewById(R.id.cartAttrs);
            price = itemView.findViewById(R.id.item_cart_price);
            qty = itemView.findViewById(R.id.cartQty);
            plus = itemView.findViewById(R.id.cartPlus);
            minus = itemView.findViewById(R.id.cartMinus);
        }
    }
}
