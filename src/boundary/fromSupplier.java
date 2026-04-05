package boundary;

import control.SupplierControl;
import entity.Supplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * fromSupplier.java
 *
 * A Swing JFrame that shows:
 *   – “Search by ID” + “Show All”
 *   – A JTable listing all Suppliers (ID, Name, Contact).
 *   – Form fields (Name, Contact) at the bottom.
 *   – Buttons: Add / Update / Delete / Back to Menu.
 */
public class fromSupplier extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    private JTextField txtName, txtContact;
    private JTextField txtSearchId;
    private JButton    btnAdd, btnUpdate, btnDelete, btnSearch, btnShowAll, btnBack;

    // don't initialize here (can't throw), do it in the ctor
    private SupplierControl controller;

    public fromSupplier() {
        super("🦷 Manage Suppliers");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 250, 255));

        // ─── create the controller, catching SQLException ─────────────────────────
        try {
            controller = new SupplierControl();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Unable to connect to database:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(ex);
        }

        // ─────────────────────── Title ───────────────────────
        JLabel title = new JLabel("Dental Inventory Manager", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ─────────────────────── Search Panel ───────────────────────
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(245, 250, 255));
        searchPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        txtSearchId = new JTextField(5);
        btnSearch   = new JButton("Search by ID");
        btnShowAll  = new JButton("Show All");
        btnBack     = new JButton("Back to Menu");

        styleButton(btnSearch);
        styleButton(btnShowAll);
        styleButton(btnBack);

        searchPanel.add(new JLabel("Supplier ID:"));
        searchPanel.add(txtSearchId);
        searchPanel.add(btnSearch);
        searchPanel.add(btnShowAll);
        searchPanel.add(btnBack);

        // ─────────────────────── Center: JTable ───────────────────────
        model = new DefaultTableModel(
            new String[]{ "Supplier ID", "Name", "Contact Details" },
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Suppliers"));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 250, 255));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ─────────────────────── Bottom: Form + Buttons ───────────────────────
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        formPanel.setBackground(new Color(245, 250, 255));

        txtName    = new JTextField();
        txtContact = new JTextField();

        formPanel.add(new JLabel("Supplier Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Contact Details:"));
        formPanel.add(txtContact);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(new Color(245, 250, 255));

        btnAdd    = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");

        styleButton(btnAdd);
        styleButton(btnUpdate);
        styleButton(btnDelete);

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        southContainer.setBackground(new Color(245, 250, 255));
        southContainer.add(formPanel, BorderLayout.CENTER);
        southContainer.add(btnPanel, BorderLayout.SOUTH);

        add(southContainer, BorderLayout.SOUTH);

        // ─────────────────────── JTable Selection Listener ───────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                txtName .setText(model.getValueAt(row, 1).toString());
                txtContact.setText(model.getValueAt(row, 2).toString());
            }
        });

        // ─────────────────────── Button Actions ───────────────────────

        // (1) Add
        btnAdd.addActionListener(e -> {
            String name    = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            if (name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter both Name and Contact Details.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            Supplier newSup = new Supplier(name, contact);
            try {
                if (controller.addSupplier(newSup)) {
                    JOptionPane.showMessageDialog(this, "Supplier added successfully.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "Error adding supplier:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            txtName.setText("");
            txtContact.setText("");
            loadSuppliers();
        });

        // (2) Update
        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please click on a row first to update.",
                    "No Supplier Selected",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            int    id      = (int) model.getValueAt(row, 0);
            String name    = txtName.getText().trim();
            String contact = txtContact.getText().trim();
            if (name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Name and Contact cannot be empty.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            Supplier updSup = new Supplier(id, name, contact);
            try {
                if (controller.updateSupplier(updSup)) {
                    JOptionPane.showMessageDialog(this, "Supplier updated successfully.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "Error updating supplier:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            loadSuppliers();
        });

        // (3) Delete
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please select a row to delete.",
                    "No Supplier Selected",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete Supplier ID " + id + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
                ) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                if (controller.deleteSupplier(id)) {
                    JOptionPane.showMessageDialog(this, "Supplier deleted successfully.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "Error deleting supplier:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
            txtName.setText("");
            txtContact.setText("");
            loadSuppliers();
        });

        // (4) Search by ID
        btnSearch.addActionListener(e -> {
            String idStr = txtSearchId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Enter a Supplier ID to search.",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            int searchID;
            try {
                searchID = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Supplier ID must be a valid integer.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            model.setRowCount(0);
            List<Supplier> all;
            try {
                all = controller.getAllSuppliers();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    "Error loading suppliers:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            boolean found = false;
            for (Supplier s : all) {
                if (s.getSupplierID() == searchID) {
                    model.addRow(new Object[]{
                        s.getSupplierID(),
                        s.getSupplierName(),
                        s.getContactDetails()
                    });
                    found = true;
                    break;
                }
            }
            if (!found) {
                JOptionPane.showMessageDialog(
                    this,
                    "No supplier found with ID " + searchID,
                    "Not Found",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        // (5) Show All
        btnShowAll.addActionListener(e -> {
            txtSearchId.setText("");
            loadSuppliers();
        });

        // (6) Back to Main Menu
        btnBack.addActionListener(e -> {
            new Main();
            dispose();
        });

        // Initial load
        loadSuppliers();
        setVisible(true);
    }

    /** reloads the table from the DB, swallowing SQLException */
    private void loadSuppliers() {
        model.setRowCount(0);
        List<Supplier> list;
        try {
            list = controller.getAllSuppliers();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error loading suppliers:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        for (Supplier s : list) {
            model.addRow(new Object[]{
                s.getSupplierID(),
                s.getSupplierName(),
                s.getContactDetails()
            });
        }
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(173, 216, 230));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(fromSupplier::new);
    }
}
