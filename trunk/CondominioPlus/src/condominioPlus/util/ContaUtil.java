/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.financeiro.Conta;
import javax.swing.JTextField;

/**
 *
 * @author Administrador
 */
public class ContaUtil {

    private boolean validarNome(JTextField txt, Conta vinculo) {
        if (vinculo.getNome().equals(txt.getText())) {
            return false;
        }
        return true;
    }

//     public boolean verificarTipo(Conta c) {
//
//        if (isCredito()) {
//            if (c.isCredito()) {
//                return true;
//            }
//        } else if (!isCredito()) {
//            if (!c.isCredito()) {
//                return true;
//            }
//        }
//
//        return false;
//    }

}
