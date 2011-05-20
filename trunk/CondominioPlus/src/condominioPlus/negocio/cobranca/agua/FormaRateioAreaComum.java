/*
 * FormaPagamento.java
 * 
 * Created on 26/09/2007, 10:56:19
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.agua;


/**
 *
 * @author USUARIO
 */
public enum FormaRateioAreaComum {

    NAO_COBRAR("Não Cobrar"),
    PROPORCIONAL_CONSUMO("Proporcional ao Consumo"),
    PROPORCIONAL_FRACAO("Proporcional a Fração Ideal"),
    IGUAL_TODOS("Igual para Todos");
    private String descricao = "";

    FormaRateioAreaComum(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }

}
