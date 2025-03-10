package DataAccessLayer.InventoryDataAccessLayer;
import BusinessLayer.InventoryBusinessLayer.*;
import BusinessLayer.SupplierBusinessLayer.Order;
import BusinessLayer.SupplierBusinessLayer.SupplierProduct;
import DataAccessLayer.DBConnector;
import ServiceLayer.SupplierServiceLayer.OrderService;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ItemsDaoImpl implements ItemsDao {
    private Connection connection;
    private Map<Integer, Item> itemsMapFromDB;
    private ProductsDao productsDao;
    private ProductMinAmountDao productMinAmountDao;
    public ItemsDaoImpl() throws SQLException {
        connection = DBConnector.connect();
        itemsMapFromDB = new HashMap<>();
        productsDao = new ProductsDaoImpl();
        productMinAmountDao = new ProductMinAmountDaoImpl();
    }
    @Override
    public List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items ");
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all items: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public Item getItemByID(int itemID) throws SQLException {
        if (itemsMapFromDB.containsKey(itemID)) {
            return itemsMapFromDB.get(itemID);
        }
        PreparedStatement statement = null;
        ResultSet rs = null;
        Item item = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE ItemID = ?");
            statement.setInt(1, itemID);
            rs = statement.executeQuery();
            if (rs.next()) {
                LocalDate expiredDate;
                Product product;
                int branchID = rs.getInt("BranchID");
                int productID = rs.getInt("ProductID");
                if (productsDao.getProductsMapFromDB().containsKey(productID)) {
                    product = productsDao.getProductsMapFromDB().get(productID);
                } else {
                    product = productsDao.getProductByID(productID);
                }
                int supplierID = rs.getInt("SupplierID");
                double priceFromSupplier = rs.getDouble("PriceFromSupplier");
                double priceInBranch = rs.getDouble("PriceInBranch");
                String status = rs.getString("Status");
                String defectiveDiscription = rs.getString("DefectiveDiscription");
                LocalDate arrivalDate = LocalDate.parse(rs.getString("ArrivalDate"));
                if (rs.getString("ExpiredDate") != null) {
                    expiredDate = LocalDate.parse(rs.getString("ExpiredDate"));
                    item = new Item(itemID, branchID, expiredDate, arrivalDate, priceFromSupplier, priceInBranch, supplierID, product);
                } else {
                    item = new Item(itemID, branchID, null, arrivalDate, priceFromSupplier, priceInBranch, supplierID, product);
                }
                item.setDefectiveDiscription(defectiveDiscription);
                switch (status) {
                    case "Damaged" -> item.setStatusType(StatusEnum.Damaged);
                    case "Expired" -> item.setStatusType(StatusEnum.Expired);
                    case "Store" -> item.setStatusType(StatusEnum.Store);
                    case "Storage" -> item.setStatusType(StatusEnum.Storage);
                    case "Sold" -> item.setStatusType(StatusEnum.Sold);
                }
                itemsMapFromDB.put(itemID, item);
            }
            return item;
        } catch (Exception e) {
            System.out.println("Error while getting item: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public Item addItem(int branchID, LocalDate expiredDate, LocalDate arrivalDate, double priceFromSupplier, double priceInBranch, int supplierID, Product product) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Item item;
        try {
            statement = connection.prepareStatement("INSERT INTO Items (BranchID, ProductID, SupplierID, ExpiredDate, PriceFromSupplier, PriceInBranch,PriceAfterDiscount,Status,ArrivalDate) VALUES(?,?,?,?,?,?,?,?,?)");
            statement.setInt(1, branchID);
            statement.setInt(2, product.getProductID());
            statement.setInt(3, supplierID);
            if (expiredDate != null) {
                statement.setString(4, expiredDate.toString());
            } else {
                statement.setString(4, null);
            }
            statement.setDouble(5, priceFromSupplier);
            statement.setDouble(6, priceInBranch);
            statement.setDouble(7, priceInBranch);
            statement.setString(8, "Storage");
            statement.setString(9, arrivalDate.toString());
            statement.executeUpdate();
            rs = connection.createStatement().executeQuery("SELECT MAX(ItemID) FROM Items");
            int last_ID = rs.getInt(1);
            item = new Item(last_ID, branchID, expiredDate, arrivalDate, priceFromSupplier, priceInBranch, supplierID, product);
            itemsMapFromDB.put(item.getItemID(), item);
            return item;
        } catch (Exception e) {
            System.out.println("Error while trying to add new item: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }
    @Override
    public Item updateItemStatus(int itemID, String status) throws SQLException {
        Item item;
        PreparedStatement statement = null;
        try {
            if (itemsMapFromDB.containsKey(itemID)) {
                item = itemsMapFromDB.get(itemID);
            } else {
                item = getItemByID(itemID);
            }
            statement = connection.prepareStatement("UPDATE Items SET Status = ? WHERE ItemID = ?");
            statement.setString(1, status);
            statement.setInt(2, itemID);
            statement.executeUpdate();
            switch (status) {
                case "Damaged" -> item.setStatusType(StatusEnum.Damaged);
                case "Expired" -> item.setStatusType(StatusEnum.Expired);
                case "Store" -> item.setStatusType(StatusEnum.Store);
                case "Storage" -> item.setStatusType(StatusEnum.Storage);
                case "Sold" -> item.setStatusType(StatusEnum.Sold);
                default -> System.out.println("The selected status is incorrect, you can update the item's status when the status is one of the following statuses: Store, Storage, Expired, Damaged, Sold .");
            }
            item = this.getItemByID(itemID);
            itemsMapFromDB.put(itemID, item);
            return item;
        } catch (Exception e) {
            System.out.println("Error while trying to update item status: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public Item updateItemPriceAfterDiscount(int itemID, double priceAfterDiscount) throws SQLException {
        Item item;
        PreparedStatement statement = null;
        try {
            if (itemsMapFromDB.containsKey(itemID)) {
                item = itemsMapFromDB.get(itemID);
            } else {
                item = getItemByID(itemID);
            }
            statement = connection.prepareStatement("UPDATE Items SET PriceAfterDiscount = ? WHERE ItemID = ?");
            statement.setDouble(1, priceAfterDiscount);
            statement.setInt(2, itemID);
            statement.executeUpdate();
            item.setPriceAfterDiscount(priceAfterDiscount);
            itemsMapFromDB.put(itemID, item);
            return item;
        } catch (Exception e) {
            System.out.println("Error while trying to update item price after discount : " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public Item updateItemDefectiveDescription(int itemID, String description) throws SQLException {
        Item item;
        PreparedStatement statement = null;
        try {
            if (itemsMapFromDB.containsKey(itemID)) {
                item = itemsMapFromDB.get(itemID);
            } else {
                item = getItemByID(itemID);
            }
            statement = connection.prepareStatement("UPDATE Items SET DefectiveDiscription = ? WHERE ItemID = ?");
            statement.setString(1, description);
            statement.setInt(2, itemID);
            statement.executeUpdate();
            item.setDefectiveDiscription(description);
            itemsMapFromDB.put(itemID, item);
            return item;
        } catch (Exception e) {
            System.out.println("Error while trying to update item defective discription : " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public List<Item> getAllExpiredItemsByBranchID(int branchID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ? ");
            statement.setString(1, "Expired");
            statement.setInt(2, branchID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    item = itemsMapFromDB.get(currItemID);
                    item.setDefectiveDiscription(rs.getString("DefectiveDiscription"));
                    item.setStatusType(StatusEnum.Expired);
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all expired items: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public List<Item> getAllDamagedItemsByBranchID(int branchID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ? ");
            statement.setString(1, "Damaged");
            statement.setInt(2, branchID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    item = itemsMapFromDB.get(currItemID);
                    item.setDefectiveDiscription(rs.getString("DefectiveDiscription"));
                    item.setStatusType(StatusEnum.Damaged);
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all damaged items: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public List<Item> getAllSoldItemByBranchID(int branchID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ? ");
            statement.setString(1, "Sold");
            statement.setInt(2, branchID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all sold items: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public List<Item> getAllStoreItemsByBranchID(int branchID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ?");
            statement.setString(1, "Store");
            statement.setInt(2, branchID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all store items: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public List<Item> getAllStorageItemsByBranchID(int branchID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ? ");
            statement.setString(1, "Storage");
            statement.setInt(2, branchID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all storage items: " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public Item getItemForSale(int productID, int branchID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Item itemToSell = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT * FROM Items WHERE ProductID = ? AND BranchID = ? " +
                            "AND ItemID = (SELECT MIN(ItemID) FROM Items WHERE ProductID = ? AND BranchID = ? AND Status =?)");
            statement.setInt(1, productID);
            statement.setInt(2, branchID);
            statement.setInt(3, productID);
            statement.setInt(4, branchID);
            statement.setString(5, "Store");
            rs = statement.executeQuery();
            if (rs.next()) {
                int itemToSellID = rs.getInt("ItemID");
                if (itemsMapFromDB.containsKey(itemToSellID)) {
                    itemToSell = itemsMapFromDB.get(itemToSellID);
                } else {
                    itemToSell = getItemByID(itemToSellID);
                    itemsMapFromDB.put(itemToSellID, itemToSell);
                }
            }
            return itemToSell;
        } catch (Exception e) {
            System.out.println("Error while getting item for sale: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }
    @Override
    public Map<Integer, Item> getItemsMapFromDB() {
        return itemsMapFromDB;
    }

    @Override
    public HashMap<Integer,Integer> fromStorageToStore(Branch branch) throws SQLException {
        try {
            int minItemsStoreForAllProducts = branch.getMinItemsInShelf();
            int maxItemsStoreForAllProducts = branch.getMaxItemsInShelf();
            HashMap<Integer, Integer> shortageOrder = new HashMap<>();
            List<Product> missingProducts = new ArrayList<>();
            List<Product> products = productsDao.getAllProducts();
            List<Product> allProducts = productsDao.getAllProducts();
            if (allProducts.size() >= 0) {
                for (Product product : allProducts) {
                    List<Item> storeItems = getAllStoreItemsByBranchIDAndProductID(branch.getBranchID(), product.getProductID());// 6
                    List<Item> storageItems = getAllStorageItemsByBranchIDAndProductID(branch.getBranchID(), product.getProductID()); // 50
                    if (storeItems.size() < minItemsStoreForAllProducts) {
                        int currNumItemsInStore = storeItems.size();
                        int currNumItemsInStorage = storageItems.size();

                        while (currNumItemsInStorage > 0 && currNumItemsInStore < maxItemsStoreForAllProducts) {
                            Item item = getMinIDItemInStorage(product.getProductID(), branch.getBranchID());
                            item = updateItemStatus(item.getItemID(), "Store");
                            currNumItemsInStore++;
                            currNumItemsInStorage--;
                        }
                    }
                    storeItems = getAllStoreItemsByBranchIDAndProductID(branch.getBranchID(), product.getProductID());
                    storageItems = getAllStorageItemsByBranchIDAndProductID(branch.getBranchID(), product.getProductID());
                    int totalSumItems = storeItems.size() + storageItems.size();
                    int minCurrProduct = productMinAmountDao.getMinAmountOfProductByBranch(product.getProductID(), branch.getBranchID());
                    if (totalSumItems < minCurrProduct) {
                        String status = productMinAmountDao.getOrderStatusByProductInBranch(product.getProductID(), branch.getBranchID());
                        if (status.equals("Not Invited")) {
                            missingProducts.add(product);
                        }
                    }
                }
                if (missingProducts.size() > 0) {
                    PopUpAlert alert = new PopUpAlert();
                    alert.showPopupWindow(missingProducts); // pop up alert
                }
                if (missingProducts.size() > 0) {
                    for (Product missingProduct : missingProducts) {
                        int minAmount = productMinAmountDao.getMinAmountOfProductByBranch(missingProduct.getProductID(), branch.getBranchID());
                        shortageOrder.put(missingProduct.getProductID(), minAmount + 50);
                    }
                }
            }
            return shortageOrder;
        }
        catch (Exception e)
        {
            System.out.println("Error while trying to do from storage to store : " + e.getMessage());
            return null;
        }
    }
    @Override
    public List<Item> getAllStoreItemsByBranchIDAndProductID(int branchID, int productID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ? AND ProductID = ?");
            statement.setString(1, "Store");
            statement.setInt(2, branchID);
            statement.setInt(3, productID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all store items by product : " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public List<Item> getAllStorageItemsByBranchIDAndProductID(int branchID, int productID) throws SQLException {
        List<Item> items = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM Items WHERE Status = ? AND BranchID = ? AND ProductID = ?");
            statement.setString(1, "Storage");
            statement.setInt(2, branchID);
            statement.setInt(3, productID);
            rs = statement.executeQuery();
            while (rs.next()) {
                Item item;
                int currItemID = rs.getInt("ItemID");
                if (!itemsMapFromDB.containsKey(currItemID)) {
                    item = this.getItemByID(rs.getInt("ItemID"));
                    items.add(item);
                    itemsMapFromDB.put(currItemID, item);
                } else {
                    items.add(itemsMapFromDB.get(currItemID));
                }
            }
            return items;
        } catch (Exception e) {
            System.out.println("Error while getting all storage items by product : " + e.getMessage());
            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
    @Override
    public Item getMinIDItemInStorage(int productID, int branchID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Item itemToSell = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT * FROM Items WHERE ProductID = ? AND BranchID = ? " +
                            "AND ItemID = (SELECT MIN(ItemID) FROM Items WHERE ProductID = ? AND BranchID = ? AND Status =?)");
            statement.setInt(1, productID);
            statement.setInt(2, branchID);
            statement.setInt(3, productID);
            statement.setInt(4, branchID);
            statement.setString(5, "Storage");
            rs = statement.executeQuery();
            if (rs.next()) {
                int itemToSellID = rs.getInt("ItemID");
                if (itemsMapFromDB.containsKey(itemToSellID)) {
                    itemToSell = itemsMapFromDB.get(itemToSellID);
                } else {
                    itemToSell = getItemByID(itemToSellID);
                    itemsMapFromDB.put(itemToSellID, itemToSell);
                }
            }
            return itemToSell;
        } catch (Exception e) {
            System.out.println("Error while getting item with min id in storage: " + e.getMessage());
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }
    @Override
    public void checkExpiredItemsInAllBranches() throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement2 = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM Items WHERE (Status = 'Store' OR Status = 'Storage')");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int itemId = resultSet.getInt("ItemID");
                LocalDate expDate ;
                if (resultSet.getString("ExpiredDate") != null) {
                    expDate = LocalDate.parse(resultSet.getString("ExpiredDate"));
                    if (expDate.isBefore(LocalDate.now())) {
                        preparedStatement2 = connection.prepareStatement("UPDATE Items SET Status = 'Expired',DefectiveDiscription = 'The item has expired' WHERE ItemID = ?");
                        preparedStatement2.setInt(1, itemId);
                        preparedStatement2.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while trying check expired items : " + e.getMessage());
        } finally {
            if (preparedStatement !=null){preparedStatement.close();}
            if (resultSet!=null) {resultSet.close();}
            if (preparedStatement2 !=null){preparedStatement2.close();}
        }
    }
    @Override
    public void EnteringNewOrder(OrderService orderService,Branch branch,Order order) throws SQLException
    {
        try {
            ArrayList<SupplierProduct> itemsInOrder = order.getItemsInOrder();
            int branchID = order.getBranchID();
            if (branch.getBranchID() != branchID)
            {
                throw new SQLException();
            }
            if (itemsInOrder.size() >0) {
                for (SupplierProduct supplierProduct : itemsInOrder) {
                    Product product = productsDao.getProductByID(supplierProduct.getProductID());
                    if (product == null) {
                        throw new SQLException();
                    }
                    int amountInOrder = supplierProduct.getAmount();
                    int supplierID = supplierProduct.getSupplierId();
                    LocalDate arrivalDate = LocalDate.now();
                    double priceFromSupplier = supplierProduct.getPrice();
                    double priceInBranch = priceFromSupplier * 2.5;
                    LocalDate expiredDate = null;
                    if (supplierProduct.getExpirationDays() != -1) {
                        expiredDate = LocalDate.now().plusDays(supplierProduct.getExpirationDays());
                    }
                    for (int i = 0; i < amountInOrder; i++) {
                        Item item = addItem(branchID, expiredDate, arrivalDate, priceFromSupplier, priceInBranch, supplierID, product);
                    }
                }
            }
            fromStorageToStore(branch);
            orderService.markOrderAsCollected(order.getOrderID());
        }
        catch (Exception e)
        {
            System.out.println("Error while trying enter new order to the branch : " + e.getMessage());
        }
    }
    @Override
    public void checkAllOrdersForToday(OrderService orderService, List<Branch> allBranches) throws SQLException {
        try {
            if (allBranches.size() > 0) {
                for (Branch branch : allBranches)
                {
                    HashMap<Integer, Order> allOrdersForToday;
                    allOrdersForToday = orderService.getNoneCollectedOrdersForToday(branch.getBranchID());
                    if (allOrdersForToday.size() > 0)
                    {
                        for (Order order : allOrdersForToday.values())
                        {
                            if (order != null)
                            {
                                EnteringNewOrder(orderService,branch,order);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {System.out.println("Error while trying to check all orders for today : " + e.getMessage());}
    }
}

