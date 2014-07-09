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
    private String numeroDocumento;

    public DadosDOC() {
    }

    public DadosDOC(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    @Override
    public DadosPagamento clone() {
        return new DadosDOC(numeroDocumento);
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
}
