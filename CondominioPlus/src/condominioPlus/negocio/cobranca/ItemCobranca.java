/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca;

import java.math.BigDecimal;

/**
 *
 * @author eugenia
 */
public class ItemCobranca {

    private int codigoObjeto;
    private int codigoConta;
    private String descricao;
    private BigDecimal valor;
    private boolean dividirFracaoIdeal;

    public int getCodigoConta() {
        return codigoConta;
    }

    public void setCodigoConta(int codigoConta) {
        this.codigoConta = codigoConta;
    }

    public int getCodigoObjeto() {
        return codigoObjeto;
    }

    public void setCodigoObjeto(int codigoObjeto) {
        this.codigoObjeto = codigoObjeto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isDividirFracaoIdeal() {
        return dividirFracaoIdeal;
    }

    public void setDividirFracaoIdeal(boolean dividirFracaoIdeal) {
        this.dividirFracaoIdeal = dividirFracaoIdeal;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

}
