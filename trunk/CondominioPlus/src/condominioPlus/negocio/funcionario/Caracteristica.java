/*
 * PermissaoAcesso.java
 *
 * Created on 26/07/2007, 18:38:09
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.funcionario;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author USUARIO
 */
@Entity
@Table(name = "caracteristica")
public class Caracteristica implements Serializable {

    @Id
    @GeneratedValue
    private int id;
    @Basic
    private CaracteristicaAcesso caracteristicaAcesso;

    /** Cria uma nova instância de PermissaoAcesso */
    public Caracteristica() {
    }

    public Caracteristica(CaracteristicaAcesso caracteristicaAcesso) {
        this.caracteristicaAcesso = caracteristicaAcesso;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public CaracteristicaAcesso getCaracteristicaAcesso() {
        return caracteristicaAcesso;
    }

    public void setCaracteristicaAcesso(CaracteristicaAcesso caracteristicaAcesso) {
        this.caracteristicaAcesso = caracteristicaAcesso;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if(obj instanceof CaracteristicaAcesso) {
            return this.caracteristicaAcesso == obj;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Caracteristica other = (Caracteristica) obj;
        if (this.caracteristicaAcesso != other.caracteristicaAcesso && (this.caracteristicaAcesso == null || !this.caracteristicaAcesso.equals(other.caracteristicaAcesso))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash += this.caracteristicaAcesso != null ? this.caracteristicaAcesso.hashCode() : 0;
        return hash;
    }

    @Override
    public String toString() {
        return caracteristicaAcesso.toString();
    }
}