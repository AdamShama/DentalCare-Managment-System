package entity;

import java.time.LocalDate;

public class InventoryItem {
    private final int       itemID;
    private final String    itemName;
    private final String    description;
    private final int       categoryID;
    private final int       quantityInStock;
    private final Supplier  supplier;
    private final LocalDate expirationDate;
    private final int       reorderThreshold;

    /** Full 8‑arg (reading from DB) */
    public InventoryItem(int itemID,
                         String itemName,
                         String description,
                         int categoryID,
                         int quantityInStock,
                         Supplier supplier,
                         LocalDate expirationDate,
                         int reorderThreshold)
    {
        this.itemID           = itemID;
        this.itemName         = itemName;
        this.description      = description;
        this.categoryID       = categoryID;
        this.quantityInStock  = quantityInStock;
        this.supplier         = supplier;
        this.expirationDate   = expirationDate;
        this.reorderThreshold = reorderThreshold;
    }

    /** 7‑arg overload (for inserts; itemID is auto‑generated) */
    public InventoryItem(String itemName,
                         String description,
                         int categoryID,
                         int quantityInStock,
                         Supplier supplier,
                         LocalDate expirationDate,
                         int reorderThreshold)
    {
        this(0, itemName, description, categoryID, quantityInStock, supplier, expirationDate, reorderThreshold);
    }

    // — getters —

    public int       getItemID()          { return itemID; }
    public String    getItemName()        { return itemName; }
    public String    getDescription()     { return description; }
    public int       getCategoryID()      { return categoryID; }
    public int       getQuantityInStock() { return quantityInStock; }
    public Supplier  getSupplier()        { return supplier; }
    public LocalDate getExpirationDate()  { return expirationDate; }
    public int       getReorderThreshold(){ return reorderThreshold; }
}
