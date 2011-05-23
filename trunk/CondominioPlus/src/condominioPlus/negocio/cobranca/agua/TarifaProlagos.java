/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.agua;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name="tarifa_prolagos")
@NamedQueries(value={@NamedQuery(name="TarifaPorId", query="SELECT t FROM TarifaProlagos t order by t.codigo")})
public class TarifaProlagos implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @Column(precision = 20, scale = 2)
    private double consumoInicial;
    @Column(precision = 20, scale = 2)
    private double consumoFinal;
    @Column(precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);

    public double getConsumoFinal() {
        return consumoFinal;
    }

    public void setConsumoFinal(double consumoFinal) {
        this.consumoFinal = consumoFinal;
    }

    public double getConsumoInicial() {
        return consumoInicial;
    }

    public void setConsumoInicial(double consumoInicial) {
        this.consumoInicial = consumoInicial;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

}
