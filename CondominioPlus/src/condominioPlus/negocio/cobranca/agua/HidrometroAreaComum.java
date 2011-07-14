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
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name="hidrometro_area_comum")
public class HidrometroAreaComum implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Column(name="leitura_atual")
    private BigDecimal leituraAtual;
    @Column(name="leitura_final")
    private BigDecimal leituraFinal;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getLeituraAtual() {
        return leituraAtual;
    }

    public void setLeituraAtual(BigDecimal leituraAtual) {
        this.leituraAtual = leituraAtual;
    }

    public BigDecimal getLeituraFinal() {
        return leituraFinal;
    }

    public void setLeituraFinal(BigDecimal leituraFinal) {
        this.leituraFinal = leituraFinal;
    }

    

}
