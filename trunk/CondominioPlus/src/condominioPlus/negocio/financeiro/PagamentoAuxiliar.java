/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrado
 */
public class PagamentoAuxiliar {

    private String formaPagamento;
    private List<Pagamento> listaPagamentos = new ArrayList<Pagamento>();

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<Pagamento> getListaPagamentos() {
        return listaPagamentos;
    }

    public void setListaPagamentos(List<Pagamento> listaPagamentos) {
        this.listaPagamentos = listaPagamentos;
    }

    public void adicionarPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            listaPagamentos.add(pagamento);
        }
    }
}
