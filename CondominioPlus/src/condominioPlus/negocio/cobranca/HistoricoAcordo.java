/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author eugenia
 */
@Entity
@Table(name="historico_acordo")
public class HistoricoAcordo implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @OneToMany(mappedBy = "historico")
    private List<Cobranca> cobrancasOriginais = new ArrayList<Cobranca>();

    public List<Cobranca> getCobrancasOriginais() {
        return cobrancasOriginais;
    }

    public void setCobrancasOriginais(List<Cobranca> cobrancasOriginais) {
        this.cobrancasOriginais = cobrancasOriginais;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
    
}
