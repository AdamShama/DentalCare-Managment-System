package control;

import entity.DbConsts;          // <-- your actual package
import java.sql.*;

public class SecretaryControl {
    private final Connection conn;

    public SecretaryControl() throws SQLException {
        // load the UCanAccess driver (if needed on your JDK):
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            // fine if your JDK auto‑loads it
        }
        // open the connection with your one CONN_STR constant:
        this.conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    public boolean isSecretary(int staffId) throws SQLException {
        String sql =
            "SELECT COUNT(*) AS cnt " +
            "FROM Staff AS s " +
            "INNER JOIN Roles AS r ON s.RoleID = r.RoleID " +
            "WHERE s.StaffID = ? AND r.RoleName = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setString(2, "Secretary");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        }
    }

}
