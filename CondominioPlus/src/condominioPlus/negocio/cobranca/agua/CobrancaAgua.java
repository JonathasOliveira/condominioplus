/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.agua;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name= "cobranca_agua")
public class CobrancaAgua implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @OneToOne
    private Condominio condominio;
    @OneToMany(mappedBy="cobrancaAgua")
    private List<ContaAgua> contas;

    public List<ContaAgua> getContas() {
        return contas;
    }

    public void setContas(List<ContaAgua> contas) {
        this.contas = contas;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }



}
