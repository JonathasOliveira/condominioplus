/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author USUARIO
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "MaxNumeroDocumento", query = "SELECT Max(d.numeroDocumento) FROM DadosDOC d")
})
@Table(name = "dados_doc")
public class DadosDOC extends DadosPagamento {

    @Column(name = "numero_documento")
    private long numeroDocumento;

    public DadosDOC() {
    }

    public DadosDOC(long numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    @Override
    public DadosPagamento clone() {
        return new DadosDOC(numeroDocumento);
    }

    public long getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(long numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
}
