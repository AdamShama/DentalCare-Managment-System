// src/control/inventoryItemControl.java
package control;

import entity.DbConsts;
import entity.InventoryItem;
import entity.Supplier;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class inventoryItemControl {
    private final Connection      conn;
    private final SupplierControl supCtrl;   // ← new field

    public inventoryItemControl() throws SQLException {
        conn    = DriverManager.getConnection(DbConsts.CONN_STR);
        supCtrl = new SupplierControl();      // ← initialize it here
    }

    private InventoryItem mapRow(ResultSet rs) throws SQLException {
        int     supId = rs.getInt   (DbConsts.COL_INV_SUPPLIER);
        Supplier sup   = supCtrl.getById(supId);

        return new InventoryItem(
            rs.getInt   (DbConsts.COL_INV_ITEM_ID),
            rs.getString(DbConsts.COL_INV_NAME),
            rs.getString(DbConsts.COL_INV_DESC),
            rs.getInt   (DbConsts.COL_INV_CATEGORY),
            rs.getInt   (DbConsts.COL_INV_QTY),
            sup,
            rs.getDate  (DbConsts.COL_INV_EXPIRY).toLocalDate(),
            rs.getInt   (DbConsts.COL_INV_THRESHOLD)
        );
    }

    public List<InventoryItem> getAll() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(DbConsts.SQL_SEL_INVENTORY))
        {
            List<InventoryItem> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }


    public boolean insert(InventoryItem it) throws SQLException {
        String sql =
            "INSERT INTO " + DbConsts.TABLE_INVENTORY + " (" +
              DbConsts.COL_INV_NAME     + "," +
              DbConsts.COL_INV_DESC     + "," +
              DbConsts.COL_INV_CATEGORY + "," +
              DbConsts.COL_INV_QTY      + "," +
              DbConsts.COL_INV_SUPPLIER + "," +
              DbConsts.COL_INV_EXPIRY   + "," +
              DbConsts.COL_INV_THRESHOLD+
            ") VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, it.getItemName());
            ps.setString(2, it.getDescription());
            ps.setInt   (3, it.getCategoryID());
            ps.setInt   (4, it.getQuantityInStock());
            ps.setInt   (5, it.getSupplier().getSupplierID());   // ← use getSupplier().getSupplierID()
            ps.setDate  (6, Date.valueOf(it.getExpirationDate()));
            ps.setInt   (7, it.getReorderThreshold());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean update(InventoryItem it) throws SQLException {
        String sql =
            "UPDATE " + DbConsts.TABLE_INVENTORY + " SET "
          +   DbConsts.COL_INV_NAME     + " = ?, "
          +   DbConsts.COL_INV_DESC     + " = ?, "
          +   DbConsts.COL_INV_CATEGORY + " = ?, "
          +   DbConsts.COL_INV_QTY      + " = ?, "
          +   DbConsts.COL_INV_SUPPLIER + " = ?, "
          +   DbConsts.COL_INV_EXPIRY   + " = ?, "
          +   DbConsts.COL_INV_THRESHOLD+ " = ? "
          + "WHERE " + DbConsts.COL_INV_ITEM_ID + " = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, it.getItemName());
            ps.setString(2, it.getDescription());
            ps.setInt   (3, it.getCategoryID());
            ps.setInt   (4, it.getQuantityInStock());
            ps.setInt   (5, it.getSupplier().getSupplierID());   // ← fixed
            ps.setDate  (6, Date.valueOf(it.getExpirationDate()));
            ps.setInt   (7, it.getReorderThreshold());
            ps.setInt   (8, it.getItemID());
            return ps.executeUpdate() == 1;
        }
    }
}
