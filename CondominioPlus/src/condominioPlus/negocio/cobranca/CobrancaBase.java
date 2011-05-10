/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author eugenia
 */
@Entity
@Table(name="cobranca_base")
@NamedQueries ( value= {
    @NamedQuery (name="CobrancaBasePorCondominio", query="SELECT c FROM CobrancaBase c WHERE c.condominio = ?1 ORDER BY c.codigo")
})
public class CobrancaBase implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @ManyToOne
    private Conta conta;
    @Column(precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);
    private boolean dividirFracaoIdeal;
    @ManyToOne
    private Condominio condominio;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
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

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }

}
