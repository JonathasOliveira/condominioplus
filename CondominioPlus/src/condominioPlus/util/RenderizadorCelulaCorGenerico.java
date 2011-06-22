/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Administrador
 */
public class RenderizadorCelulaCorGenerico extends DefaultTableCellRenderer {

    public RenderizadorCelulaCorGenerico() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(JLabel.RIGHT);

        setBackground(new Color(255, 253, 208));
        setForeground(Color.BLACK);

        if (isSelected) {
            setForeground(Color.BLACK);
        }
        return this;
    }
}

