package boundary;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.function.IntConsumer;

public class ButtonEditor extends AbstractCellEditor
    implements TableCellEditor, ActionListener {
    private final JButton button = new JButton();
    private int value;
    private final IntConsumer callback;

    public ButtonEditor(String label, IntConsumer callback) {
        button.setText(label);
        this.callback = callback;
        button.addActionListener(this);
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object val,
            boolean isSelected, int row, int col) {
        // assume col 0 holds the ID
        this.value = Integer.parseInt(table.getValueAt(row, 0).toString());
        button.setText(val == null ? "" : val.toString());
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        callback.accept(value);
        fireEditingStopped();
    }
}
