/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.taxaExtra;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */

@Entity
public class ParcelaTaxaExtra implements Serializable{

    @Id
    @GeneratedValue
    private int codigo;
    private int numeroParcela;
    @Column(precision=20, scale=2)
    private BigDecimal valor;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataVencimento;
    @ManyToOne
    private TaxaExtra taxa;
    @OneToMany(mappedBy = "parcela", cascade = CascadeType.ALL)
    private List<RateioTaxaExtra> rateios = new ArrayList();

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

    public int getNumeroParcela() {
        return numeroParcela;
    }

    public void setNumeroParcela(int numeroParcela) {
        this.numeroParcela = numeroParcela;
    }

    public TaxaExtra getTaxa() {
        return taxa;
    }

    public void setTaxa(TaxaExtra taxa) {
        this.taxa = taxa;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public List<RateioTaxaExtra> getRateios() {
        return rateios;
    }

    public void setRateios(List<RateioTaxaExtra> rateios) {
        this.rateios = rateios;
    }

}
