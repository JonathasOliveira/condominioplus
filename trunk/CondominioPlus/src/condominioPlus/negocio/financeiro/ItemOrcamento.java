/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Unidade;
import java.math.BigDecimal;

/**
 *
 * @author eugenia
 */
public class ItemOrcamento {
    
    private Unidade unidade;
    private BigDecimal taxaMedia;
    private BigDecimal taxa1;
    private BigDecimal taxa2;
    private BigDecimal taxa3;

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getTaxaMedia() {
        return taxaMedia;
    }

    public void setTaxaMedia(BigDecimal taxaMedia) {
        this.taxaMedia = taxaMedia;
    }

    public BigDecimal getTaxa1() {
        return taxa1;
    }

    public void setTaxa1(BigDecimal taxa1) {
        this.taxa1 = taxa1;
    }

    public BigDecimal getTaxa2() {
        return taxa2;
    }

    public void setTaxa2(BigDecimal taxa2) {
        this.taxa2 = taxa2;
    }

    public BigDecimal getTaxa3() {
        return taxa3;
    }

    public void setTaxa3(BigDecimal taxa3) {
        this.taxa3 = taxa3;
    }
    
}
