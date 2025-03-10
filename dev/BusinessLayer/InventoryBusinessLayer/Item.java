package BusinessLayer.InventoryBusinessLayer;

import java.time.LocalDate;
public class Item {
    private final int ItemID;
    private int SupplierID;
    private int BranchID;
    private int ProductID;
    private LocalDate ExpiredDate;
    private LocalDate ArrivalDate;
    private String DefectiveDiscription;
    private double PriceFromSupplier;
    private double PriceInBranch;
    private StatusEnum StatusType;
    private double priceAfterDiscount;
    private Product product;

    public Item(int itemID,int branchID, LocalDate expiredDate, LocalDate arrivalDate, double priceFromSupplier , double priceInBranch ,int supplierID, Product product)
    {
        this.ItemID = itemID;
        this.BranchID = branchID;
        this.ProductID = product.getProductID();
        this.PriceFromSupplier=priceFromSupplier;
        this.PriceInBranch = priceInBranch;
        this.SupplierID = supplierID;
        this.ExpiredDate = expiredDate;
        this.ArrivalDate = arrivalDate;
        this.priceAfterDiscount = priceInBranch;
        this.product = product;
    }
    public Product getProduct() {return product;}
    public int getBranchID() {
        return BranchID;
    }
    public int getProductID() {
        return ProductID;
    }
    public LocalDate getExpiredDate() {
        return ExpiredDate;
    }
    public double getPriceFromSupplier() {
        return PriceFromSupplier;
    }
    public double getPriceInBranch() {
        return PriceInBranch;
    }
    public int getItemID() {return this.ItemID;}
    public int getSupplierID() {
        return SupplierID;
    }
    public String getDefectiveDiscription() {
        return DefectiveDiscription;
    }
    public void setDefectiveDiscription(String defectiveDiscription) {
        DefectiveDiscription = defectiveDiscription;
    }
    public void setPriceInBranch(double priceInBranch) {PriceInBranch = priceInBranch;}
    public StatusEnum getStatusType() {
        return StatusType;
    }
    public void setStatusType(StatusEnum statusType) {StatusType = statusType;}
    public double getPriceAfterDiscount() {return priceAfterDiscount;}
    public void setPriceAfterDiscount(double priceAfterDiscount) {this.priceAfterDiscount = priceAfterDiscount;}
    public String getArrivalDate() {return this.ArrivalDate.toString();}
    @Override
    public String toString() {
        String returnString = "";
        returnString += "Name: " + product.getProductName() + " ";
        returnString += "Item ID : " + ItemID + " ";
        returnString += "\n" + "Branch ID : " + BranchID + " ";
        returnString += "\n" + "Product ID : " + ProductID + " ";
        returnString += "\n" + "Supplier ID : " + SupplierID + " ";
        if (ExpiredDate != null) {returnString += "\n" + "Expired Date : " + ExpiredDate + " ";}
        returnString += "\n" + "PriceFromSupplier : " + PriceFromSupplier + " ";
        returnString += "\n" + "PriceInBranch : " + PriceInBranch + " ";
        returnString += "\n" + "PriceAfterDiscount : " + priceAfterDiscount + " ";
        returnString += "\n" + "Status : " + StatusType + " ";
        if (DefectiveDiscription != null) {returnString += "\n" + "Defective Discription : " + DefectiveDiscription + " ";}
        returnString += "\n" + "Arrival Date : " + ArrivalDate + " ";
        return returnString;
    }
}
