/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.agua;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name="conta_agua")
@NamedQueries(value = {
    @NamedQuery(name = "ContasPorCondominio", query = "SELECT c FROM ContaAgua c where c.condominio = ?1 order by c.dataInicial")})
public class ContaAgua implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @OneToMany(mappedBy="conta", cascade = CascadeType.ALL)
    private List<Rateio> rateios = new ArrayList<Rateio>();
    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL)
    private List<Pipa> pipas;
    @Column(name="data_inicial")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataInicial;
    @Column(name="data_final")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataFinal;
    @Column(name="valor_prolagos")
    private BigDecimal valorProlagos = new BigDecimal(0);
    @Column(name="consumo_prolagos")
    private BigDecimal consumoProlagos = new BigDecimal(0);
    @Column(name="valor_pipa")
    private BigDecimal valorPipa = new BigDecimal(0);
    @Column(name="consumo_pipa")
    private BigDecimal consumoPipa = new BigDecimal(0);
    @Column(name="preco_metro_cubico")
    private BigDecimal precoMetroCubico = new BigDecimal(0);
    @Column(name="data_vencimento_conta")
    @Temporal(TemporalType.DATE)
    private Calendar dataVencimentoConta;
    @Column(name="consumo_unidades_metro_cubico")
    private BigDecimal consumoUnidadesMetroCubico = new BigDecimal(0);
    @Column(name="preco_total_unidades")
    private BigDecimal precoTotalUnidades = new BigDecimal(0);
    @Column(name="consumo_area_comum")
    private BigDecimal consumoAreaComum = new BigDecimal(0);
    @Column(name="preco_area_comum")
    private BigDecimal precoAreaComum = new BigDecimal(0);
    @Column(name="total_despesas")
    private BigDecimal totalDespesas = new BigDecimal(0);
    @ManyToOne
    private Condominio condominio;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getConsumoAreaComum() {
        return consumoAreaComum;
    }

    public void setConsumoAreaComum(BigDecimal consumoAreaComum) {
        this.consumoAreaComum = consumoAreaComum;
    }

    public BigDecimal getConsumoPipa() {
        return consumoPipa;
    }

    public void setConsumoPipa(BigDecimal consumoPipa) {
        this.consumoPipa = consumoPipa;
    }

    public BigDecimal getConsumoProlagos() {
        return consumoProlagos;
    }

    public void setConsumoProlagos(BigDecimal consumoProlagos) {
        this.consumoProlagos = consumoProlagos;
    }

    public BigDecimal getConsumoUnidadesMetroCubico() {
        return consumoUnidadesMetroCubico;
    }

    public void setConsumoUnidadesMetroCubico(BigDecimal consumoUnidadesMetroCubico) {
        this.consumoUnidadesMetroCubico = consumoUnidadesMetroCubico;
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

    public Calendar getDataVencimentoConta() {
        return dataVencimentoConta;
    }

    public void setDataVencimentoConta(Calendar dataVencimentoConta) {
        this.dataVencimentoConta = dataVencimentoConta;
    }

    public List<Pipa> getPipas() {
        return pipas;
    }

    public void setPipas(List<Pipa> pipas) {
        this.pipas = pipas;
    }

    public BigDecimal getPrecoAreaComum() {
        return precoAreaComum;
    }

    public void setPrecoAreaComum(BigDecimal precoAreaComum) {
        this.precoAreaComum = precoAreaComum;
    }

    public BigDecimal getPrecoMetroCubico() {
        return precoMetroCubico;
    }

    public void setPrecoMetroCubico(BigDecimal precoMetroCubico) {
        this.precoMetroCubico = precoMetroCubico;
    }

    public BigDecimal getPrecoTotalUnidades() {
        return precoTotalUnidades;
    }

    public void setPrecoTotalUnidades(BigDecimal precoTotalUnidades) {
        this.precoTotalUnidades = precoTotalUnidades;
    }

    public List<Rateio> getRateios() {
        return rateios;
    }

    public void setRateios(List<Rateio> rateios) {
        this.rateios = rateios;
    }

    public BigDecimal getTotalDespesas() {
        return totalDespesas;
    }

    public void setTotalDespesas(BigDecimal totalDespesas) {
        this.totalDespesas = totalDespesas;
    }

    public BigDecimal getValorPipa() {
        return valorPipa;
    }

    public void setValorPipa(BigDecimal valorPipa) {
        this.valorPipa = valorPipa;
    }

    public BigDecimal getValorProlagos() {
        return valorProlagos;
    }

    public void setValorProlagos(BigDecimal valorProlagos) {
        this.valorProlagos = valorProlagos;
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }


}
