/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 *
 * @author eugenia
 */
public class ItemCobranca {

    private int codigoObjeto = 0;;
    private int codigoConta = 0;
    private String descricao = "";
    private BigDecimal valor = new BigDecimal(0);
    private boolean dividirFracaoIdeal;
    private boolean concederDesconto;
    private Calendar descontoAte;
    private BigDecimal valorComDesconto = new BigDecimal(0);

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

    public boolean isConcederDesconto() {
        return concederDesconto;
    }

    public void setConcederDesconto(boolean concederDesconto) {
        this.concederDesconto = concederDesconto;
    }

    public Calendar getDescontoAte() {
        return descontoAte;
    }

    public void setDescontoAte(Calendar descontoAte) {
        this.descontoAte = descontoAte;
    }

    public BigDecimal getValorComDesconto() {
        return valorComDesconto;
    }

    public void setValorComDesconto(BigDecimal valorComDesconto) {
        this.valorComDesconto = valorComDesconto;
    }

}
