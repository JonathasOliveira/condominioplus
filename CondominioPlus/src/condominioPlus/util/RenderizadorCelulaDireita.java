/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.TabelaModelo_2;

/**
 *
 * @author Administrador
 */
public class RenderizadorCelulaDireita extends DefaultTableCellRenderer {

    protected TabelaModelo_2 modeloTabela;

    public RenderizadorCelulaDireita(TabelaModelo_2 modeloTabela) {
        this.modeloTabela = modeloTabela;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(JLabel.RIGHT);

      return this;
    }
}
