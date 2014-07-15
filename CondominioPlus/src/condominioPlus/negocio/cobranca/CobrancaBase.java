/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */
@Entity
@Table(name = "cobranca_base")
@NamedQueries(value = {
    @NamedQuery(name = "CobrancaBasePorCondominio", query = "SELECT c FROM CobrancaBase c WHERE c.condominio = ?1 ORDER BY c.codigo")
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
    @Column(name = "desconto")
    private boolean concederDesconto;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "desconto_ate")
    private Calendar descontoAte;
    @Column(name = "valor_com_desconto", precision = 20, scale = 2)
    private BigDecimal valorComDesconto = new BigDecimal(0);
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
