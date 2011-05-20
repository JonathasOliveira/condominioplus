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
public enum FormaCalculoMetroCubico {

    DIVIDIR_METROS_CUBICOS("Dividir por Metros Cúbicos"),
    SINDICO_PRECO("Síndico estabelece Preço"),
    TABELA_PROLAGOS("Tabela da Prolagos");
    private String descricao = "";

    FormaCalculoMetroCubico(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }

}
