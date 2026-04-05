package boundary;

import entity.Appointment;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        // 1) set the label
        String text = value == null ? "" : value.toString();
        setText(text);

        // 2) default enable state
        boolean enabled = true;

        // 3) special-case PAY button
        if ("Pay".equals(text)) {
            // column 5 holds the Paid boolean
            Boolean paid = (Boolean) table.getValueAt(row, 5);
            enabled = (paid != null && !paid);
        }
        // 4) special-case SUSPEND button
        else if ("Suspend".equals(text)) {
            // column 4 holds the Status string
            String status = (String) table.getValueAt(row, 4);
            enabled = Appointment.STATUS_SCHEDULED.equals(status);
        }

        setEnabled(enabled);
        return this;
    }
}
