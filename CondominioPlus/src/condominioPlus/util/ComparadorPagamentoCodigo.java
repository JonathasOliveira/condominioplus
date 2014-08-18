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
public class ComparadorPagamentoCodigo implements Comparator<Pagamento> {

    public int compare(Pagamento p1, Pagamento p2) {
        return Integer.valueOf(p1.getCodigo()).compareTo(Integer.valueOf(p2.getCodigo()));
//        return p1.getCodigo().compareTo(p2.getCodigo()); // aqui vc implementa o metodo compare(Object o1, Object o2) da interface Comparator
    }
}
