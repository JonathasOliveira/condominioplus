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
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class RenderizadorCelulaCorData extends RenderizadorCelulaCor {

    public RenderizadorCelulaCorData(TabelaModelo_2 modeloTabela) {
        super(modeloTabela);
    }

    @Override
    public void setValue(Object valor) {
        super.setValue(DataUtil.toString(DataUtil.getDate(valor)));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(JLabel.CENTER);

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
