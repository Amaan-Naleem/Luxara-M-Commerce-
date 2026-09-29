package lk.jiat.luxara.model;

import java.util.List;

import lombok.Builder;

@Builder
public class Product {
    private String productId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private List<String> images;
    private int stockCount;
    private boolean status;
    private double rating;
    private List<Attribute> attributes;

    public Product() {
    }

    public Product(String productId, String title, String description, double price, String categoryId, List<String> images, int stockCount, boolean status, double rating, List<Attribute> attributes) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.images = images;
        this.stockCount = stockCount;
        this.status = status;
        this.rating = rating;
        this.attributes = attributes;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getStockCount() {
        return stockCount;
    }

    public void setStockCount(int stockCount) {
        this.stockCount = stockCount;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Builder
    public static class Attribute {
        private String name;
        private String type;
        private List<String> values;

        public Attribute() {
        }

        public Attribute(String name, String type, List<String> values) {
            this.name = name;
            this.type = type;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }
    }
}
