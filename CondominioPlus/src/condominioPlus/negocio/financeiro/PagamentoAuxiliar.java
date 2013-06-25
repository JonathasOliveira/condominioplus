/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Condominio;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrador
 */
public class PagamentoAuxiliar {

    private String numeroDocumento;
    private int codigoConta;
    private String nomeConta;
    private Condominio condominio;
    private List<Pagamento> listaPagamentos = new ArrayList<Pagamento>();

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
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
