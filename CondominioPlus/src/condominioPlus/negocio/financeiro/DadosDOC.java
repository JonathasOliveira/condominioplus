/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import logicpoint.util.Util;

/**
 *
 * @author USUARIO
 */
@Entity
@Table(name = "dados_doc")
public class DadosDOC extends DadosPagamento {

    @Column(name = "numero_documento")
    private String numeroDocumento = "";

    public DadosDOC() {
    }

    public DadosDOC(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    @Override
    public boolean verificar() {
        return Util.verificar(numeroDocumento);
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
