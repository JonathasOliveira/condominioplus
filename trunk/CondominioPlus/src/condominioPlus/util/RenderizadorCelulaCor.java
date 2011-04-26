/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.financeiro.Conta;
import java.awt.Color;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Administrador
 */
public class RenderizadorCelulaCor extends DefaultTableCellRenderer {

    public RenderizadorCelulaCor() {
        super();
        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
       setBackground(Color.red);
    }

    @Override
    protected void setValue(Object valor) {
        if (valor != null && valor instanceof Conta) {
            if (((Conta) valor).getCodigo() == 12902) {
                setBackground(Color.red);
            }
        }
        super.setValue(valor);
    }
}
