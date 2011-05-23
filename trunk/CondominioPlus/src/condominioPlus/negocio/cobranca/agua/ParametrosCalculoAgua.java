/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.agua;

import condominioPlus.negocio.Condominio;
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
@Table(name="parametros_agua")
public class ParametrosCalculoAgua implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    private FormaCalculoMetroCubico formaMetroCubico;
    private FormaRateioAreaComum formaAreaComum;
    private int quantidadeMetrosCubicosNaCota;
    @Column(precision = 20, scale = 2)
    private BigDecimal valorMetroCubicoSindico = new BigDecimal(0);
    @Column(precision = 20, scale = 2)
    private BigDecimal valorFixoAreaComum = new BigDecimal(0);
    private boolean hidrometroAreaComum;
    private boolean cobrarPipa;

    public boolean isCobrarPipa() {
        return cobrarPipa;
    }

    public void setCobrarPipa(boolean cobrarPipa) {
        this.cobrarPipa = cobrarPipa;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public FormaRateioAreaComum getFormaAreaComum() {
        return formaAreaComum;
    }

    public void setFormaAreaComum(FormaRateioAreaComum formaAreaComum) {
        this.formaAreaComum = formaAreaComum;
    }

    public FormaCalculoMetroCubico getFormaMetroCubico() {
        return formaMetroCubico;
    }

    public void setFormaMetroCubico(FormaCalculoMetroCubico formaMetroCubico) {
        this.formaMetroCubico = formaMetroCubico;
    }

    public boolean isHidrometroAreaComum() {
        return hidrometroAreaComum;
    }

    public void setHidrometroAreaComum(boolean hidrometroAreaComum) {
        this.hidrometroAreaComum = hidrometroAreaComum;
    }

    public int getQuantidadeMetrosCubicosNaCota() {
        return quantidadeMetrosCubicosNaCota;
    }

    public void setQuantidadeMetrosCubicosNaCota(int quantidadeMetrosCubicosNaCota) {
        this.quantidadeMetrosCubicosNaCota = quantidadeMetrosCubicosNaCota;
    }

    public BigDecimal getValorFixoAreaComum() {
        return valorFixoAreaComum;
    }

    public void setValorFixoAreaComum(BigDecimal valorFixoAreaComum) {
        this.valorFixoAreaComum = valorFixoAreaComum;
    }

    public BigDecimal getValorMetroCubicoSindico() {
        return valorMetroCubicoSindico;
    }

    public void setValorMetroCubicoSindico(BigDecimal valorMetroCubicoSindico) {
        this.valorMetroCubicoSindico = valorMetroCubicoSindico;
    }

    

}
