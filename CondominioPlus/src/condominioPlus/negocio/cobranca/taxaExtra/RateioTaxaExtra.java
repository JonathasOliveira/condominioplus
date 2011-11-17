/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.taxaExtra;

import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.Cobranca;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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
    private ParcelaTaxaExtra parcela;
    @Column(name="valor", precision=20, scale=2)
    private BigDecimal valorACobrar;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataVencimento;
    @OneToOne(cascade= CascadeType.ALL)
    private Cobranca cobranca;

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

    public ParcelaTaxaExtra getParcela() {
        return parcela;
    }

    public void setParcela(ParcelaTaxaExtra parcela) {
        this.parcela = parcela;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValorACobrar() {
        return valorACobrar;
    }

    public void setValorACobrar(BigDecimal valorACobrar) {
        this.valorACobrar = valorACobrar;
    }

    public Cobranca getCobranca() {
        return cobranca;
    }

    public void setCobranca(Cobranca cobranca) {
        this.cobranca = cobranca;
    }

}
