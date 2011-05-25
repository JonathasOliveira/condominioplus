/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.agua;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name="conta_agua")
public class ContaAgua implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @ManyToOne
    private CobrancaAgua cobranca;
    private List<Rateio> rateios;
    private List<Pipa> pipas;
    @Column(name="data_inicial")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataInicial;
    @Column(name="data_final")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataFinal;
    @Column(name="valor_prolagos")
    private BigDecimal valorProlagos;
    @Column(name="consumo_prolagos")
    private BigDecimal consumoProlagos;
    @Column(name="valor_pipa")
    private BigDecimal valorPipa;
    @Column(name="consumo_pipa")
    private BigDecimal consumoPipa;
    @Column(name="preco_metro_cubico")
    private BigDecimal precoMetroCubico;
    @Column(name="data_vencimento_conta")
    @Temporal(TemporalType.DATE)
    private Calendar dataVencimentoConta;
    @Column(name="consumo_unidades_metro_cubico")
    private BigDecimal consumoUnidadesMetroCubico;
    @Column(name="preco_total_unidades")
    private BigDecimal precoTotalUnidades;
    @Column(name="consumo_area_comum")
    private BigDecimal consumoAreaComum;
    @Column(name="preco_area_comum")
    private BigDecimal precoAreaComum;
    @Column(name="total_despesas")
    private BigDecimal totalDespesas;

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

   
    

}
