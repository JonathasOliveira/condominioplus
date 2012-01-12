/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;

/**
 *
 * @author thiagocifani
 */
@Entity
public class Orcamento implements Serializable {
    
    @Id
    @GeneratedValue
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name="data_inicial")
    private Calendar dataInicial;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name="data_final")
    private Calendar dataFinal;
    @Column(name="numero_unidades")
    private int numeroUnidades;
    @Column(name="quantidade_cobrancas_nao_pagas")
    private int quantidadesCobrancasNaoPagas;
    private BigDecimal Diversos;
    private BigDecimal media;
    private BigDecimal media1;
    private BigDecimal media2;
    private BigDecimal media3;
    @Column(name="valor_base")
    private BigDecimal valorBase;
    @Column(name="unidades_descartadas")
    private BigDecimal unidadesDescartadas;
    private int meses;
    @Column(name="sindico_paga")
    private boolean sindicoPaga;
    private List <ContaOrcamentaria> contas = new ArrayList<ContaOrcamentaria>();

    public BigDecimal getDiversos() {
        return Diversos;
    }

    public void setDiversos(BigDecimal Diversos) {
        this.Diversos = Diversos;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public List<ContaOrcamentaria> getContas() {
        return contas;
    }

    public void setContas(List<ContaOrcamentaria> contas) {
        this.contas = contas;
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

    public BigDecimal getMedia() {
        return media;
    }

    public void setMedia(BigDecimal media) {
        this.media = media;
    }

    public BigDecimal getMedia1() {
        return media1;
    }

    public void setMedia1(BigDecimal media1) {
        this.media1 = media1;
    }

    public BigDecimal getMedia2() {
        return media2;
    }

    public void setMedia2(BigDecimal media2) {
        this.media2 = media2;
    }

    public BigDecimal getMedia3() {
        return media3;
    }

    public void setMedia3(BigDecimal media3) {
        this.media3 = media3;
    }

    public int getMeses() {
        return meses;
    }

    public void setMeses(int meses) {
        this.meses = meses;
    }

    public int getNumeroUnidades() {
        return numeroUnidades;
    }

    public void setNumeroUnidades(int numeroUnidades) {
        this.numeroUnidades = numeroUnidades;
    }

    public int getQuantidadesCobrancasNaoPagas() {
        return quantidadesCobrancasNaoPagas;
    }

    public void setQuantidadesCobrancasNaoPagas(int quantidadesCobrancasNaoPagas) {
        this.quantidadesCobrancasNaoPagas = quantidadesCobrancasNaoPagas;
    }

    public boolean isSindicoPaga() {
        return sindicoPaga;
    }

    public void setSindicoPaga(boolean sindicoPaga) {
        this.sindicoPaga = sindicoPaga;
    }

    public BigDecimal getUnidadesDescartadas() {
        return unidadesDescartadas;
    }

    public void setUnidadesDescartadas(BigDecimal unidadesDescartadas) {
        this.unidadesDescartadas = unidadesDescartadas;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }
    
    
    
}
