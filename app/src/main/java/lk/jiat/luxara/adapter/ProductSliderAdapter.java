package lk.jiat.luxara.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.jiat.luxara.R;
import lombok.NonNull;

public class ProductSliderAdapter extends RecyclerView.Adapter<ProductSliderAdapter.ProductSliderViewHolder>{
private List<String>images;
    public ProductSliderAdapter(List<String> images){
        this.images = images;
    }

    public ProductSliderAdapter(){

    }
    @androidx.annotation.NonNull
    @Override
    public ProductSliderViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
    View view =    LayoutInflater.from(parent.getContext()).inflate(R.layout.product_slider_item,parent,false);
        return new ProductSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull ProductSliderViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(images.get(position))
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ProductSliderViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ProductSliderViewHolder(@androidx.annotation.NonNull View itemView){
            super(itemView);
            this.imageView = itemView.findViewById(R.id.product_slider_item_image);
        }
    }
}
