/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author USUARIO
 */
@Entity
@Table(name = "dados_boleto")
public class DadosBoleto extends DadosPagamento {

    @Column(name = "numero_boleto")
    private String numeroBoleto;

    public DadosBoleto() {
    }

    public DadosBoleto(String numeroBoleto) {
        this.numeroBoleto = numeroBoleto;
    }

    @Override
    public DadosPagamento clone() {
        return new DadosBoleto(numeroBoleto);
    }

    public String getNumeroBoleto() {
        return numeroBoleto;
    }

    public void setNumeroBoleto(String numeroBoleto) {
        this.numeroBoleto = numeroBoleto;
    }
}
