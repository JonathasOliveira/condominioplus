/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.gas;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name="conta_gas")
public class ContaGas implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataInicial;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataFinal;
    private BigDecimal valorUnitarioKg;
    private BigDecimal quantidadeKg;
    private BigDecimal valorTotal;
    private BigDecimal valorUnitarioMetroCubico;
    private BigDecimal quantidadeMetroCubico;
    private double densidadeMedia;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public double getDensidadeMedia() {
        return densidadeMedia;
    }

    public void setDensidadeMedia(double densidadeMedia) {
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

    

    

}
