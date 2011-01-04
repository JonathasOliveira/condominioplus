/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.util;

import condominioPlus.negocio.financeiro.Pagamento;
import java.util.Comparator;

/**
 *
 * @author Administrador
 */
public class ComparatorPagamento implements Comparator<Pagamento> {


    public int compare(Pagamento p1, Pagamento p2)
    {
        return p1.getDataPagamento().compareTo(p2.getDataPagamento()); // aqui vc implementa o metodo compare(Object o1, Object o2) da interface Comparator
    }

}
