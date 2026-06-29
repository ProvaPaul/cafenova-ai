package com.smartcafe.model;

/** Transient model representing one line in the POS cart (never persisted directly). */
public class CartItem {

    private final Product product;
    private int           quantity;
    private String        notes;

    public CartItem(Product product, int quantity) {
        this.product  = product;
        this.quantity = quantity;
    }

    public double getSubtotal() { return product.getPrice() * quantity; }

    public Product getProduct()          { return product; }

    public int     getQuantity()       { return quantity; }
    public void    setQuantity(int v)  { quantity = v; }

    public String  getNotes()          { return notes; }
    public void    setNotes(String v)  { notes = v; }
}
