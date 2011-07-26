/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.gas;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "conta_gas")
public class ContaGas implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataInicial;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataFinal;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataVencimento;
    private BigDecimal valorUnitarioKg = new BigDecimal(0);
    private BigDecimal quantidadeKg = new BigDecimal(0);
    private BigDecimal valorTotal = new BigDecimal(0);
    private BigDecimal valorUnitarioMetroCubico = new BigDecimal(0);
    private BigDecimal quantidadeMetroCubico = new BigDecimal(0);
    private BigDecimal densidadeMedia = new BigDecimal(0);
    private BigDecimal totalCosumoUnidades = new BigDecimal(0);
    private BigDecimal totalUnidadesDinheiro = new BigDecimal(0);
    private BigDecimal totalCosumoAreaComum = new BigDecimal(0);
    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL)
    private List<RateioGas> rateios = new ArrayList<RateioGas>();
    @ManyToOne
    private Condominio condominio;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getDensidadeMedia() {
        return densidadeMedia;
    }

    public void setDensidadeMedia(BigDecimal densidadeMedia) {
        this.densidadeMedia = densidadeMedia;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public void setQuantidadeKg(BigDecimal quantidadeKg) {
        this.quantidadeKg = quantidadeKg;
    }

    public BigDecimal getQuantidadeMetroCubico() {
        return quantidadeMetroCubico;
    }

    public void setQuantidadeMetroCubico(BigDecimal quantidadeMetroCubico) {
        this.quantidadeMetroCubico = quantidadeMetroCubico;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorUnitarioKg() {
        return valorUnitarioKg;
    }

    public void setValorUnitarioKg(BigDecimal valorUnitarioKg) {
        this.valorUnitarioKg = valorUnitarioKg;
    }

    public BigDecimal getValorUnitarioMetroCubico() {
        return valorUnitarioMetroCubico;
    }

    public void setValorUnitarioMetroCubico(BigDecimal valorUnitarioMetroCubico) {
        this.valorUnitarioMetroCubico = valorUnitarioMetroCubico;
    }

    public Calendar getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(Calendar dataFinal) {
        this.dataFinal = dataFinal;
    }

    public Calendar getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(Calendar dataInicial) {
        this.dataInicial = dataInicial;
    }

    public List<RateioGas> getRateios() {
        return rateios;
    }

    public void setRateios(List<RateioGas> rateios) {
        this.rateios = rateios;
    }

    public BigDecimal getTotalCosumoUnidades() {
        return totalCosumoUnidades;
    }

    public void setTotalCosumoUnidades(BigDecimal totalCosumoUnidades) {
        this.totalCosumoUnidades = totalCosumoUnidades;
    }

    public BigDecimal getTotalUnidadesDinheiro() {
        return totalUnidadesDinheiro;
    }

    public void setTotalUnidadesDinheiro(BigDecimal totalUnidadesDinheiro) {
        this.totalUnidadesDinheiro = totalUnidadesDinheiro;
    }

    public BigDecimal getTotalCosumoAreaComum() {
        return totalCosumoAreaComum;
    }

    public void setTotalCosumoAreaComum(BigDecimal totalCosumoAreaComum) {
        this.totalCosumoAreaComum = totalCosumoAreaComum;
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }

    public Calendar getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Calendar dataVencimento) {
        this.dataVencimento = dataVencimento;
    }
    
}
