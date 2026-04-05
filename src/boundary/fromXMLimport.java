package boundary;

import control.inventoryItemControl;
import control.SupplierControl;
import entity.InventoryItem;
import entity.Supplier;

import org.w3c.dom.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.*;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class fromXMLimport extends JFrame {
    private final inventoryItemControl controller;
    private final SupplierControl       supDao;
    private final DefaultTableModel     model;
    private final JTextField            txtXMLPath;

    public fromXMLimport() {
        super("📂 Import Inventory XML");
        try {
            controller = new inventoryItemControl();
            supDao     = new SupplierControl();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        setSize(1000,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        // file chooser + import button...
        JPanel left = new JPanel(new BorderLayout(5,5));
        txtXMLPath = new JTextField(); txtXMLPath.setEditable(false);
        JButton btnBrowse = new JButton("Browse…");
        JButton btnImport = new JButton("Import");
        left.add(txtXMLPath,BorderLayout.NORTH);
        left.add(btnBrowse,BorderLayout.CENTER);
        left.add(btnImport,BorderLayout.SOUTH);
        add(left,BorderLayout.WEST);

        // table of results
        model = new DefaultTableModel(
            new String[]{"ItemID","Name","CategoryID","Qty","SupplierID","Expiry","Threshold"},0
        );
        JTable tbl = new JTable(model);
        add(new JScrollPane(tbl),BorderLayout.CENTER);

        btnBrowse.addActionListener(e->{
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("XML","xml"));
            if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
                txtXMLPath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        btnImport.addActionListener(e->{
            File f = new File(txtXMLPath.getText().trim());
            if(!f.exists()) {
                JOptionPane.showMessageDialog(this,"Select a valid XML!");
                return;
            }
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(f);
                NodeList items = doc.getElementsByTagName("Item");
                for(int i=0;i<items.getLength();i++) {
                    Element el = (Element)items.item(i);
                    String name  = getTag(el,"ItemName");
                    String desc  = getTag(el,"Description");
                    int    cat   = Integer.parseInt(getTag(el,"CategoryID"));
                    int    qty   = Integer.parseInt(getTag(el,"QuantityInStock"));
                    int    sid   = Integer.parseInt(getTag(el,"SupplierID"));
                    Supplier sup  = supDao.getById(sid);
                    if(sup==null) throw new RuntimeException("No supplier #"+sid);
                    LocalDate exp = LocalDate.now().plusYears(1);
                    int thr = 0;
                    InventoryItem itm = new InventoryItem(
                        name, desc, cat, qty, sup, exp, thr
                    );
                    controller.insert(itm);
                }
                reloadTable();
                JOptionPane.showMessageDialog(this,"✅ XML Imported!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });
        reloadTable();
        setVisible(true);
    }

    private String getTag(Element e, String tag) {
        Node n = e.getElementsByTagName(tag).item(0);
        return n==null ? "" : n.getTextContent().trim();
    }

    private void reloadTable() {
        model.setRowCount(0);
        try {
            List<InventoryItem> list = controller.getAll();
            for(InventoryItem it:list){
                model.addRow(new Object[]{
                    it.getItemID(),
                    it.getItemName(),
                    it.getCategoryID(),
                    it.getQuantityInStock(),
                    it.getSupplier().getSupplierID(),
                    it.getExpirationDate(),
                    it.getReorderThreshold()
                });
            }
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(fromXMLimport::new);
    }
}
