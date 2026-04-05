package control;

import control.XMLimportControl.InventoryXmlException;
import entity.DbConsts;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.*;
import java.time.format.DateTimeFormatter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles importing/exporting inventory as XML, without relying on
 * any external stored procedures.
 */
public class XMLimportControl {
    private final Connection conn;

    public XMLimportControl() throws SQLException {
        conn = DriverManager.getConnection(DbConsts.CONN_STR);
    }

    /**
     * Exports the entire InventoryItems table to the given XML file.
     */
    public void exportInventoryXml(File xmlOut) throws InventoryXmlException {
        String sql = "SELECT ItemID, ItemName, Description, CategoryID,"
                   + " QuantityInStock, SupplierID, ExpirationDate, ReorderThreshold"
                   + " FROM InventoryItems";

        try (
          PreparedStatement ps = conn.prepareStatement(sql);
          ResultSet rs          = ps.executeQuery()
        ) {
            // build XML DOM
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element root = doc.createElement("InventoryItems");
            doc.appendChild(root);

            DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;

            while (rs.next()) {
                Element item = doc.createElement("Item");
                root.appendChild(item);

                // helper to append child text element
                appendTextElement(doc, item, "ItemID",           rs.getInt   ("ItemID"));
                appendTextElement(doc, item, "ItemName",         rs.getString("ItemName"));
                appendTextElement(doc, item, "Description",      rs.getString("Description"));
                appendTextElement(doc, item, "CategoryID",       rs.getInt   ("CategoryID"));
                appendTextElement(doc, item, "QuantityInStock",  rs.getInt   ("QuantityInStock"));
                appendTextElement(doc, item, "SupplierID",       rs.getInt   ("SupplierID"));

                Date exp = rs.getDate("ExpirationDate");
                if (exp != null) {
                    appendTextElement(doc, item, "ExpirationDate", exp.toLocalDate().format(dtf));
                }

                appendTextElement(doc, item, "ReorderThreshold", rs.getInt("ReorderThreshold"));
            }

            // write XML to file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(doc);
            StreamResult dst = new StreamResult(xmlOut);
            t.transform(src, dst);

        } catch (SQLException | ParserConfigurationException | TransformerException ex) {
            throw new InventoryXmlException("Export failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Helper to append a simple text element to a parent.
     */
    private void appendTextElement(Document doc, Element parent, String tag, Object value) {
        Element e = doc.createElement(tag);
        e.setTextContent(value == null ? "" : value.toString());
        parent.appendChild(e);
    }

    /**
     * Placeholder import—your existing logic can stay here.
     */
    public void importInventoryXml(File xmlFile) throws InventoryXmlException {
        // ... your import code ...
    }

    public static class InventoryXmlException extends Exception {
        public InventoryXmlException(String msg)       { super(msg); }
        public InventoryXmlException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
