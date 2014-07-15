/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.financeiro.Pagamento;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import logicpoint.apresentacao.TabelaModelo_2;

/**
 *
 * @author Administrador
 */
public class RenderizadorCelulaCorDescricao extends RenderizadorCelulaCor {

    public RenderizadorCelulaCorDescricao(TabelaModelo_2 modeloTabela) {
        super(modeloTabela);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(JLabel.LEFT);

        Pagamento p = (Pagamento) modeloTabela.getObjeto(row);

        if (p.getConta().getCodigo() == 12902) {
            setForeground(Color.RED.darker());
        } else {
            setForeground(table.getForeground());
            if (isSelected) {
                setForeground(Color.WHITE);
            }
        }
        return this;
    }
}
