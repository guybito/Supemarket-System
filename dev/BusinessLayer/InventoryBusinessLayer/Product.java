package BusinessLayer.InventoryBusinessLayer;

public class Product
{
    private String productName;
    private int productID;
    private String manufacturer;
    private double productWeight;
    private Category parentCategory;
    private Category subCategory;
    private Category subSubCategory;
    public Product(int productID, String name, String manufacturer, double productWeight, Category parentCategory, Category subCategory, Category subSubCategory) {
        this.productID = productID;
        this.productName = name;
        this.manufacturer = manufacturer;
        this.productWeight = productWeight;
        this.parentCategory = parentCategory;
        this.subCategory = subCategory;
        this.subSubCategory = subSubCategory;
    }

    public Category getParentCategory() {return parentCategory;}
    public Category getSubCategory() {return subCategory;}
    public Category getSubSubCategory() {return subSubCategory;}
    public double getProductWeight() {return productWeight;}
    public int getProductID() {return productID;}
    public String getManufacturer() {return manufacturer;}
    public String getProductName() {return productName;}
    public void setProductID(int id) {this.productID = id;}
    @Override
    public String toString() {
        return "Product ID: " + productID + "\n" +
                "Product Name: " + productName + "\n" +
                "Manufacturer: " + manufacturer + "\n" +
                "Product Weight: " + productWeight + "\n" +
                "Product Parent Category: " + parentCategory.getCategoryName() + "\n" +
                "Product Sub Category: " + subCategory.getCategoryName() + "\n" +
                "Product SubSub Category: " + subSubCategory.getCategoryName() + "\n";
    }
}

