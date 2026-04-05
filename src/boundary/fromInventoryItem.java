package boundary;

import control.inventoryItemControl;
import control.SupplierControl;
import entity.InventoryItem;
import entity.Supplier;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class fromInventoryItem extends JFrame {
    private final JTextField txtName, txtDesc, txtCategory,
                              txtQuantity, txtSupplierId,
                              txtExpiry, txtThreshold;
    private final JButton    btnAdd;
    private final inventoryItemControl controller;
    private final SupplierControl       supCtrl;

    public fromInventoryItem() {
        super("🦷 Manage Inventory Items");
        try {
            controller = new inventoryItemControl();
            supCtrl    = new SupplierControl();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        setLayout(new BorderLayout(10,10));
        setSize(800,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(8,2,5,5));
        form.setBorder(BorderFactory.createTitledBorder("New Inventory Item"));
        form.add(new JLabel("Name:"));          txtName      = new JTextField(); form.add(txtName);
        form.add(new JLabel("Description:"));   txtDesc      = new JTextField(); form.add(txtDesc);
        form.add(new JLabel("CategoryID:"));    txtCategory  = new JTextField(); form.add(txtCategory);
        form.add(new JLabel("QuantityInStock:"));//
        txtQuantity  = new JTextField();        form.add(txtQuantity);
        form.add(new JLabel("SupplierID:"));    txtSupplierId= new JTextField(); form.add(txtSupplierId);
        form.add(new JLabel("ExpirationDate (YYYY-MM-DD):"));
                                               txtExpiry     = new JTextField(); form.add(txtExpiry);
        form.add(new JLabel("ReorderThreshold:"));
                                               txtThreshold = new JTextField(); form.add(txtThreshold);
        add(form, BorderLayout.CENTER);

        btnAdd = new JButton("+ Add Item");
        JPanel south = new JPanel();
        south.add(btnAdd);
        add(south, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String desc = txtDesc.getText().trim();
                int    cat  = Integer.parseInt(txtCategory.getText().trim());
                int    qty  = Integer.parseInt(txtQuantity.getText().trim());
                int    sid  = Integer.parseInt(txtSupplierId.getText().trim());
                LocalDate exp = LocalDate.parse(txtExpiry.getText().trim());
                int    thr  = Integer.parseInt(txtThreshold.getText().trim());

                Supplier sup = supCtrl.getById(sid);
                if (sup==null) {
                    JOptionPane.showMessageDialog(this,
                        "No supplier #"+sid,"Input Error",JOptionPane.ERROR_MESSAGE);
                    return;
                }

             // …
                InventoryItem itm = new InventoryItem(
                    name, desc,cat,qty,sup,exp,thr
                );

                if (controller.insert(itm)) {
                    JOptionPane.showMessageDialog(this,"✅ Item added.");
                    txtName.setText("");
                    txtDesc.setText("");
                    txtCategory.setText("");
                    txtQuantity.setText("");
                    txtSupplierId.setText("");
                    txtExpiry.setText("");
                    txtThreshold.setText("");
                } else {
                    JOptionPane.showMessageDialog(this,"Insert failed.","Error",JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex){
                JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(fromInventoryItem::new);
    }
}
