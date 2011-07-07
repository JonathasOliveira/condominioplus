/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.agua;

import condominioPlus.negocio.Unidade;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Administrador
 */
@Entity
public class Rateio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @ManyToOne(cascade = CascadeType.ALL)
    private Unidade unidade;
    @Column(name = "leitura_anterior", precision = 20, scale = 3)
    private BigDecimal leituraAnterior = new BigDecimal(0);
    @Column(name = "leitura_atual", precision = 20, scale = 3)
    private BigDecimal leituraAtual = new BigDecimal(0);
    @Column(name = "consumo_metro_cubico", precision = 20, scale = 3)
    private BigDecimal consumoMetroCubico = new BigDecimal(0);
    @Column(name = "consumo_metro_cubico_cobrar", precision = 20, scale = 3)
    private BigDecimal consumoMetroCubicoACobrar = new BigDecimal(0);
    @Column(name = "valor_metro_cubico", precision = 20, scale = 2)
    private BigDecimal valorDoMetroCubico = new BigDecimal(0);
    @Column(name = "percentual_gasto", precision = 20, scale = 2)
    private BigDecimal percentualGasto = new BigDecimal(0);
    @Column(name = "valor_rateio_pipa", precision = 20, scale = 2)
    private BigDecimal valorRateioPipa = new BigDecimal(0);
    @Column(name="preco_metro_cubico_area_comum" , precision = 20, scale = 2)
    private BigDecimal precoMetroCubicoAreaComum = new BigDecimal(0);
    @Column(name = "valor_total_consumido", precision = 20, scale = 3)
    private BigDecimal valorTotalConsumido = new BigDecimal(0);
    @Column(name = "percentual_rateio_area_comum", precision = 20, scale = 2)
    private BigDecimal percentualRateioAreaComum = new BigDecimal(0);
    @Column(name = "consumo_metro_cubico_area_comum", precision = 20, scale = 3)
    private BigDecimal consumoMetroCubicoAreaComum = new BigDecimal(0);
    @Column(name = "consumo_dinheiro_area_comum", precision = 20, scale = 2)
    private BigDecimal consumoEmDinheiroAreaComum = new BigDecimal(0);
    @Column(name = "valor_total_cobrar", precision = 20, scale = 2)
    private BigDecimal valorTotalCobrar = new BigDecimal(0);
    @ManyToOne
    private ContaAgua conta;

    public Rateio() {
    }

    public Rateio(Unidade unidade) {
        this.unidade = unidade;
    }

    public ContaAgua getConta() {
        return conta;
    }

    public void setConta(ContaAgua conta) {
        this.conta = conta;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getConsumoMetroCubicoACobrar() {
        return consumoMetroCubicoACobrar;
    }

    public void setConsumoMetroCubicoACobrar(BigDecimal consumoMetroCubicoACobrar) {
        this.consumoMetroCubicoACobrar = consumoMetroCubicoACobrar;
    }

    public BigDecimal getConsumoEmDinheiroAreaComum() {
        return consumoEmDinheiroAreaComum;
    }

    public void setConsumoEmDinheiroAreaComum(BigDecimal consumoEmDinheiroAreaComum) {
        this.consumoEmDinheiroAreaComum = consumoEmDinheiroAreaComum;
    }

    public BigDecimal getConsumoMetroCubico() {
        return consumoMetroCubico;
    }

    public void setConsumoMetroCubico(BigDecimal consumoMetroCubico) {
        this.consumoMetroCubico = consumoMetroCubico;
    }

    public BigDecimal getConsumoMetroCubicoAreaComum() {
        return consumoMetroCubicoAreaComum;
    }

    public void setConsumoMetroCubicoAreaComum(BigDecimal consumoMetroCubicoAreaComum) {
        this.consumoMetroCubicoAreaComum = consumoMetroCubicoAreaComum;
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

    public BigDecimal getPercentualGasto() {
        return percentualGasto;
    }

    public void setPercentualGasto(BigDecimal percentualGasto) {
        this.percentualGasto = percentualGasto;
    }

    public BigDecimal getPercentualRateioAreaComum() {
        return percentualRateioAreaComum;
    }

    public void setPercentualRateioAreaComum(BigDecimal percentualRateioAreaComum) {
        this.percentualRateioAreaComum = percentualRateioAreaComum;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValorDoMetroCubico() {
        return valorDoMetroCubico;
    }

    public void setValorDoMetroCubico(BigDecimal valorDoMetroCubico) {
        this.valorDoMetroCubico = valorDoMetroCubico;
    }

    public BigDecimal getValorRateioPipa() {
        return valorRateioPipa;
    }

    public void setValorRateioPipa(BigDecimal valorRateioPipa) {
        this.valorRateioPipa = valorRateioPipa;
    }

    public BigDecimal getValorTotalCobrar() {
        return valorTotalCobrar;
    }

    public void setValorTotalCobrar(BigDecimal valorTotalCobrar) {
        this.valorTotalCobrar = valorTotalCobrar;
    }

    public BigDecimal getValorTotalConsumido() {
        return valorTotalConsumido;
    }

    public void setValorTotalConsumido(BigDecimal valorTotalConsumido) {
        this.valorTotalConsumido = valorTotalConsumido;
    }

    public BigDecimal getPrecoMetroCubicoAreaComum() {
        return precoMetroCubicoAreaComum;
    }

    public void setPrecoMetroCubicoAreaComum(BigDecimal precoMetroCubicoAreaComum) {
        this.precoMetroCubicoAreaComum = precoMetroCubicoAreaComum;
    }

    
}
