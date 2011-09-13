/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.luz;

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
@Table(name = "rateio_luz")
public class RateioLuz implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @ManyToOne
    private Unidade unidade;
    @Column(name="leitura_anterior", precision = 20, scale = 3)
    private BigDecimal leituraAnterior = new BigDecimal(0);
    @Column(name="leitura_atual", precision = 20, scale = 3)
    private BigDecimal leituraAtual = new BigDecimal(0);
    @Column(name="consumo_watts", precision = 20, scale = 3)
    private BigDecimal consumoWatts = new BigDecimal(0);
    @Column(name="consumo_reais_unidades", precision = 20, scale = 2)
    private BigDecimal consumoEmReaisUnidade = new BigDecimal(0);
    @Column(name="consumo_reais_area_comum", precision = 20, scale = 2)
    private BigDecimal consumoReaisAreaComum = new BigDecimal(0);
    @Column(name="consumo_total", precision = 20, scale = 2)
    private BigDecimal consumoTotal = new BigDecimal(0);
    @ManyToOne
    private ContaLuz conta;

    public RateioLuz(Unidade unidade) {
        this.unidade = unidade;
    }

    public RateioLuz() {
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

    public BigDecimal getConsumoWatts() {
        return consumoWatts;
    }

    public void setConsumoWatts(BigDecimal consumoWatts) {
        this.consumoWatts = consumoWatts;
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

    public ContaLuz getConta() {
        return conta;
    }

    public void setConta(ContaLuz conta) {
        this.conta = conta;
    }
}
