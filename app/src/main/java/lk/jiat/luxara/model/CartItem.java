package lk.jiat.luxara.model;

import java.util.List;

import lombok.Builder;

@Builder
public class CartItem {
    private String productId;
    private int quantity;
    private List<Attribute> attributes;

    public CartItem() {
    }

    public CartItem(String productId, int quantity, List<Attribute> attributes) {
        this.productId = productId;
        this.quantity = quantity;
        this.attributes = attributes;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        private String value;

        public Attribute() {
        }

        public Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
