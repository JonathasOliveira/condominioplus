/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrador
 */
public class PagamentoAuxiliar {

    private String formaPagamento;
    private int codigoConta;
    private String nomeConta;
    private List<Pagamento> listaPagamentos = new ArrayList<Pagamento>();

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public int getCodigoConta() {
        return codigoConta;
    }

    public void setCodigoConta(int codigoConta) {
        this.codigoConta = codigoConta;
    }

    public String getNomeConta() {
        return nomeConta;
    }

    public void setNomeConta(String nomeConta) {
        this.nomeConta = nomeConta;
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
