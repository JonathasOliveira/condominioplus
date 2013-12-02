/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.math.BigDecimal;

/**
 *
 * @author Roberto Jana
 */
public class ContaOrcamentaria {

//    private int codigo;
    private Conta Conta;
    private BigDecimal total = new BigDecimal(0);
    private BigDecimal media = new BigDecimal(0);
    private BigDecimal media1 = new BigDecimal(0);
    private BigDecimal media2 = new BigDecimal(0);
    private BigDecimal media3 = new BigDecimal(0);
    private BigDecimal somaJaneiro = new BigDecimal(0);
    private BigDecimal somaFevereiro = new BigDecimal(0);
    private BigDecimal somaMarco = new BigDecimal(0);
    private BigDecimal somaAbril = new BigDecimal(0);
    private BigDecimal somaMaio = new BigDecimal(0);
    private BigDecimal somaJunho = new BigDecimal(0);
    private BigDecimal somaJulho = new BigDecimal(0);
    private BigDecimal somaAgosto = new BigDecimal(0);
    private BigDecimal somaSetembro = new BigDecimal(0);
    private BigDecimal somaOutubro = new BigDecimal(0);
    private BigDecimal somaNovembro = new BigDecimal(0);
    private BigDecimal somaDezembro = new BigDecimal(0);

    public condominioPlus.negocio.financeiro.Conta getConta() {
        return Conta;
    }

    public void setConta(condominioPlus.negocio.financeiro.Conta Conta) {
        this.Conta = Conta;
    }

//    public int getCodigo() {
//        return codigo;
//    }
//
//    public void setCodigo(int codigo) {
//        this.codigo = codigo;
//    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String descricao() {
        return getConta().getNome();
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

    public BigDecimal getSomaAbril() {
        return somaAbril;
    }

    public void setSomaAbril(BigDecimal somaAbril) {
        this.somaAbril = somaAbril;
    }

    public BigDecimal getSomaAgosto() {
        return somaAgosto;
    }

    public void setSomaAgosto(BigDecimal somaAgosto) {
        this.somaAgosto = somaAgosto;
    }

    public BigDecimal getSomaDezembro() {
        return somaDezembro;
    }

    public void setSomaDezembro(BigDecimal somaDezembro) {
        this.somaDezembro = somaDezembro;
    }

    public BigDecimal getSomaFevereiro() {
        return somaFevereiro;
    }

    public void setSomaFevereiro(BigDecimal somaFevereiro) {
        this.somaFevereiro = somaFevereiro;
    }

    public BigDecimal getSomaJaneiro() {
        return somaJaneiro;
    }

    public void setSomaJaneiro(BigDecimal somaJaneiro) {
        this.somaJaneiro = somaJaneiro;
    }

    public BigDecimal getSomaJulho() {
        return somaJulho;
    }

    public void setSomaJulho(BigDecimal somaJulho) {
        this.somaJulho = somaJulho;
    }

    public BigDecimal getSomaJunho() {
        return somaJunho;
    }

    public void setSomaJunho(BigDecimal somaJunho) {
        this.somaJunho = somaJunho;
    }

    public BigDecimal getSomaMaio() {
        return somaMaio;
    }

    public void setSomaMaio(BigDecimal somaMaio) {
        this.somaMaio = somaMaio;
    }

    public BigDecimal getSomaMarco() {
        return somaMarco;
    }

    public void setSomaMarco(BigDecimal somaMarco) {
        this.somaMarco = somaMarco;
    }

    public BigDecimal getSomaNovembro() {
        return somaNovembro;
    }

    public void setSomaNovembro(BigDecimal somaNovembro) {
        this.somaNovembro = somaNovembro;
    }

    public BigDecimal getSomaOutubro() {
        return somaOutubro;
    }

    public void setSomaOutubro(BigDecimal somaOutubro) {
        this.somaOutubro = somaOutubro;
    }

    public BigDecimal getSomaSetembro() {
        return somaSetembro;
    }

    public void setSomaSetembro(BigDecimal somaSetembro) {
        this.somaSetembro = somaSetembro;
    }
}
