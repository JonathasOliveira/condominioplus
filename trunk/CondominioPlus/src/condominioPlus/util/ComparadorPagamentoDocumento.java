/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.financeiro.DadosBoleto;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import java.util.Comparator;

/**
 *
 * @author Administrador
 */
public class ComparadorPagamentoDocumento implements Comparator<Pagamento> {

    public int compare(Pagamento p1, Pagamento p2) {
        String documento1 = "";
        if (p1.getForma() == FormaPagamento.DINHEIRO) {
            documento1 = ((DadosDOC) p1.getDadosPagamento()).getNumeroDocumento();
        } else if (p1.getForma() == FormaPagamento.CHEQUE) {
            documento1 = ((DadosCheque) p1.getDadosPagamento()).getNumero();
        } else if (p1.getForma() == FormaPagamento.BOLETO) {
            documento1 = ((DadosBoleto) p1.getDadosPagamento()).getNumeroBoleto();
        }

        String documento2 = "";
        if (p2.getForma() == FormaPagamento.DINHEIRO) {
            documento2 = ((DadosDOC) p2.getDadosPagamento()).getNumeroDocumento();
        } else if (p2.getForma() == FormaPagamento.CHEQUE) {
            documento2 = ((DadosCheque) p2.getDadosPagamento()).getNumero();
        } else if (p2.getForma() == FormaPagamento.BOLETO) {
            documento2 = ((DadosBoleto) p2.getDadosPagamento()).getNumeroBoleto();
        }

        return documento1.compareTo(documento2); // aqui vc implementa o metodo compare(Object o1, Object o2) da interface Comparator
    }
}
