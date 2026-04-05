package control;

import entity.DbConsts;
import entity.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierControl {
    private final Connection conn;

    public SupplierControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /** Returns all suppliers */
    public List<Supplier> getAllSuppliers() throws SQLException {
        String sql = "SELECT * FROM " + DbConsts.TABLE_SUPPLIERS;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<Supplier> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new Supplier(
                    rs.getInt   (DbConsts.COL_SUPPLIER_ID),
                    rs.getString(DbConsts.COL_SUPPLIER_NAME),
                    rs.getString(DbConsts.COL_SUPPLIER_CONTACT)
                ));
            }
            return list;
        }
    }

    /** Lookup one supplier by its ID (int) */
    public Supplier getById(int id) throws SQLException {
        String sql = "SELECT * FROM " + DbConsts.TABLE_SUPPLIERS
                   + " WHERE " + DbConsts.COL_SUPPLIER_ID + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Supplier(
                    rs.getInt   (DbConsts.COL_SUPPLIER_ID),
                    rs.getString(DbConsts.COL_SUPPLIER_NAME),
                    rs.getString(DbConsts.COL_SUPPLIER_CONTACT)
                );
            }
        }
    }

    /** Lookup one supplier by its ID (String) */
    public Supplier getById(String idStr) throws SQLException {
        try {
            int id = Integer.parseInt(idStr.trim());
            return getById(id);
        } catch (NumberFormatException e) {
            throw new SQLException("Invalid supplier ID format: " + idStr, e);
        }
    }

    /** Insert a new supplier; returns true if inserted */
    public boolean addSupplier(Supplier s) throws SQLException {
        String sql = "{ CALL qryInsSupplier(?,?) }";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, s.getSupplierName());
            cs.setString(2, s.getContactDetails());
            return cs.executeUpdate() == 1;
        }
    }

    /** Update an existing supplier; returns true if updated */
    public boolean updateSupplier(Supplier s) throws SQLException {
        String sql = "{ CALL qryUpdSupplier(?, ?, ?) }";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt   (1, s.getSupplierID());
            cs.setString(2, s.getSupplierName());
            cs.setString(3, s.getContactDetails());
            return cs.executeUpdate() == 1;
        }
    }

    /** Delete a supplier by ID; returns true if deleted */
    public boolean deleteSupplier(int id) throws SQLException {
        String sql = "{ CALL qryDelSupplier(?) }";
        try (CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, id);
            return cs.executeUpdate() == 1;
        }
    }
}
