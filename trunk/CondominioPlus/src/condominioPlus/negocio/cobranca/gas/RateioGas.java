/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.gas;

import condominioPlus.negocio.Unidade;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "rateio_gas")
public class RateioGas implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @ManyToOne
    private Unidade unidade;
    @Column(name="leitura_anterior")
    private BigDecimal leituraAnterior = new BigDecimal(0);
    @Column(name="leitura_atual")
    private BigDecimal leituraAtual = new BigDecimal(0);
    @Column(name="consumo_metro_cubico")
    private BigDecimal consumoMetroCubico = new BigDecimal(0);
    @Column(name="consumo_reais_unidades")
    private BigDecimal consumoEmReaisUnidade = new BigDecimal(0);
    @Column(name="consumo_reais_area_comum")
    private BigDecimal consumoReaisAreaComum = new BigDecimal(0);
    @Column(name="consumo_total")
    private BigDecimal consumoTotal = new BigDecimal(0);
    @ManyToOne
    private ContaGas conta;

    public RateioGas(Unidade unidade) {
        this.unidade = unidade;
    }

    public RateioGas() {
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getConsumoEmReaisUnidade() {
        return consumoEmReaisUnidade;
    }

    public void setConsumoEmReaisUnidade(BigDecimal consumoEmReaisUnidade) {
        this.consumoEmReaisUnidade = consumoEmReaisUnidade;
    }

    public BigDecimal getConsumoTotal() {
        return consumoTotal;
    }

    public void setConsumoTotal(BigDecimal consumoTotal) {
        this.consumoTotal = consumoTotal;
    }

    public BigDecimal getConsumoMetroCubico() {
        return consumoMetroCubico;
    }

    public void setConsumoMetroCubico(BigDecimal consumoMetroCubico) {
        this.consumoMetroCubico = consumoMetroCubico;
    }

    public BigDecimal getConsumoReaisAreaComum() {
        return consumoReaisAreaComum;
    }

    public void setConsumoReaisAreaComum(BigDecimal consumoReaisAreaComum) {
        this.consumoReaisAreaComum = consumoReaisAreaComum;
    }

    public BigDecimal getLeituraAnterior() {
        return leituraAnterior;
    }

    public void setLeituraAnterior(BigDecimal leituraAnterior) {
        this.leituraAnterior = leituraAnterior;
    }

    public BigDecimal getLeituraAtual() {
        return leituraAtual;
    }

    public void setLeituraAtual(BigDecimal leituraAtual) {
        this.leituraAtual = leituraAtual;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public ContaGas getConta() {
        return conta;
    }

    public void setConta(ContaGas conta) {
        this.conta = conta;
    }
}
