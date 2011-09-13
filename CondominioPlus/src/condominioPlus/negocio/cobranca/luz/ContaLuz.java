/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.luz;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "conta_luz")
@NamedQueries(value = {
    @NamedQuery(name = "ContasLuzPorCondominio", query = "SELECT c FROM ContaLuz c where c.condominio = ?1 order by c.dataInicial")})
public class ContaLuz implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Column(name="data_inicial")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataInicial;
    @Column(name="data_final")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataFinal;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name="data_vencimento")
    private Calendar dataVencimento;
    @Column(name="valor_unitario_watts", precision = 20, scale = 5)
    private BigDecimal valorUnitarioWatts = new BigDecimal(0);
    @Column(name="quantidade_watts",precision = 20, scale = 3 )
    private BigDecimal quantidadeWatts = new BigDecimal(0);
    @Column(name="valor", precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);
    @Column(name="constante")
    private BigDecimal constante = new BigDecimal(0);
    @Column(name="taxa_publica", precision = 20, scale = 2)
    private BigDecimal taxaPublica = new BigDecimal(0);
    @Column(name="total_unidades_dinheiro", precision = 20, scale = 2)
    private BigDecimal totalUnidadesDinheiro = new BigDecimal(0);
    @Column(name="total_consumo_area_comum", precision = 20, scale = 2)
    private BigDecimal totalConsumoAreaComum = new BigDecimal(0);
    @Column(name="total_fatura", precision = 20, scale = 2)
    private BigDecimal totalFatura = new BigDecimal(0);
    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL)
    private List<RateioLuz> rateios = new ArrayList<RateioLuz>();
    @ManyToOne
    private Condominio condominio;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }

    public BigDecimal getConstante() {
        return constante;
    }

    public void setConstante(BigDecimal constante) {
        this.constante = constante;
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

    public Calendar getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Calendar dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public BigDecimal getQuantidadeWatts() {
        return quantidadeWatts;
    }

    public void setQuantidadeWatts(BigDecimal quantidadeWatts) {
        this.quantidadeWatts = quantidadeWatts;
    }

    public List<RateioLuz> getRateios() {
        return rateios;
    }

    public void setRateios(List<RateioLuz> rateios) {
        this.rateios = rateios;
    }

    public BigDecimal getTaxaPublica() {
        return taxaPublica;
    }

    public void setTaxaPublica(BigDecimal taxaPublica) {
        this.taxaPublica = taxaPublica;
    }

    public BigDecimal getTotalConsumoAreaComum() {
        return totalConsumoAreaComum;
    }

    public void setTotalConsumoAreaComum(BigDecimal totalConsumoAreaComum) {
        this.totalConsumoAreaComum = totalConsumoAreaComum;
    }

    public BigDecimal getTotalFatura() {
        return totalFatura;
    }

    public void setTotalFatura(BigDecimal totalFatura) {
        this.totalFatura = totalFatura;
    }

    public BigDecimal getTotalUnidadesDinheiro() {
        return totalUnidadesDinheiro;
    }

    public void setTotalUnidadesDinheiro(BigDecimal totalUnidadesDinheiro) {
        this.totalUnidadesDinheiro = totalUnidadesDinheiro;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValorUnitarioWatts() {
        return valorUnitarioWatts;
    }

    public void setValorUnitarioWatts(BigDecimal valorUnitarioWatts) {
        this.valorUnitarioWatts = valorUnitarioWatts;
    }

  
}
