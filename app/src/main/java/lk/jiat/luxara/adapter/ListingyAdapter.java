package lk.jiat.luxara.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

import com.google.android.material.chip.ChipGroup;
import lk.jiat.luxara.model.CartItem;
import lk.jiat.luxara.util.AnimUtils;
import lk.jiat.luxara.util.CartHelper;
import android.view.ViewGroup;

import lk.jiat.luxara.R;
import lk.jiat.luxara.model.Category;
import lk.jiat.luxara.model.Product;

public class ListingyAdapter extends RecyclerView.Adapter<ListingyAdapter.ViewHolder> {
    private List<Product> products;
    private OnListingItemClickListener listener;
    private int lastAnimated = -1;

    public ListingyAdapter(List<Product> products, OnListingItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListingyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listening, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingyAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);
        boolean animate = position > lastAnimated;
        if (animate) lastAnimated = position;
        AnimUtils.enter(holder.itemView, position, animate);
        holder.productName.setText(product.getTitle());
        holder.productPrice.setText("LKR"+product.getPrice()+"");
        Glide.with(holder.itemView.getContext())
                .load(product.getImages().get(0))
                .centerCrop()
                .into(holder.productImage);


        holder.itemView.setOnClickListener(v->{
         Animation animation = AnimationUtils.loadAnimation(v.getContext(),R.anim.click_animation);
            v.startAnimation(animation);
            if (listener != null){
                listener.onListingItemClick(product);
            }
        });

        holder.groups = CartHelper.bindAttributes(holder.itemView.getContext(), holder.attrContainer, product);

        holder.btnAddToCart.setOnClickListener(v -> {
            AnimUtils.bounce(v);
            if (listener != null) {
                listener.onAddToCartClick(product, CartHelper.getSelected(holder.groups));
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        View btnAddToCart;
        ViewGroup attrContainer;
        Map<String, ChipGroup> groups;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.listning_item_image);
            productName = itemView.findViewById(R.id.list_item_name);
            productPrice = itemView.findViewById(R.id.list_item_price);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            attrContainer = itemView.findViewById(R.id.cardAttributes);
        }
    }

    public interface OnListingItemClickListener {
        void onListingItemClick(Product product);
        void onAddToCartClick(Product product, List<CartItem.Attribute> attributes);
    }
}
