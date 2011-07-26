/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.taxaExtra;

import condominioPlus.negocio.Unidade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */
@Entity
@Table(name="rateio_taxa_extra")
public class RateioTaxaExtra implements Serializable{

    @Id
    @GeneratedValue
    private int codigo;
    @ManyToOne
    private Unidade unidade;
    @ManyToOne
    private TaxaExtra taxa;
    @Column(name="valor", precision=20, scale=2)
    private BigDecimal valosACobrar;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataVencimento;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Calendar getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Calendar dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public TaxaExtra getTaxa() {
        return taxa;
    }

    public void setTaxa(TaxaExtra taxa) {
        this.taxa = taxa;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValosACobrar() {
        return valosACobrar;
    }

    public void setValosACobrar(BigDecimal valosACobrar) {
        this.valosACobrar = valosACobrar;
    }

}
