/*
 * TipoAcesso.java
 *
 * Created on 03/10/2007, 16:49:02
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.funcionario;

/**
 *
 * @author Thiago
 */
public enum TipoAcesso {

    INSERCAO("Inserção"),
    EDICAO("Edição"),
    REMOCAO("Remoção"),
    VENDA("Venda"),
    ENTRADA("Entrada"),
    DEVOLUCAO("Devolução"),
    ESTORNO("Estorno"),
    BAIXA("Baixa"),
    LOGON("Logon"),
    ENTREGA("Entrega"),
    INTEGRACAO("Integração");

    TipoAcesso(String nome) {
        this.nome = nome;
    }

    public String toString() {
        return nome;
    }
    private String nome;
}
