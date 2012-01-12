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
    
    private int codigo;
    private Conta Conta;
    private BigDecimal total = new BigDecimal(0);
    private BigDecimal media = new BigDecimal(0);
    private BigDecimal media1 = new BigDecimal(0);
    private BigDecimal media2 = new BigDecimal(0);
    private BigDecimal media3 = new BigDecimal(0);

    public condominioPlus.negocio.financeiro.Conta getConta() {
        return Conta;
    }

    public void setConta(condominioPlus.negocio.financeiro.Conta Conta) {
        this.Conta = Conta;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String descricao(){
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
    
    
    
    
    
}
