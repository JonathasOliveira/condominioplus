/*
 * FormaPagamento.java
 * 
 * Created on 26/09/2007, 10:56:19
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

/**
 *
 * @author USUARIO
 */
public enum FormaPagamento {

    DINHEIRO("Dinheiro", null),
    CHEQUE("Cheque", DadosCheque.class),
    DEPOSITO_EM_CONTA("Depósito em conta", DadosDeposito.class),
    DOC("DOC", DadosDOC.class),
    TED("TED", DadosTED.class);
    private String descricao = "";
    private Class<? extends DadosPagamento> classe;

    FormaPagamento(String descricao, Class<? extends DadosPagamento> classe) {
        this.descricao = descricao;
        this.classe = classe;
    }

    @Override
    public String toString() {
        return descricao;
    }

    public Class<? extends DadosPagamento> getClasse() {
        return classe;
    }
}
