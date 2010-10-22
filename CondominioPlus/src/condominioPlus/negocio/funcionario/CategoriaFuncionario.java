/*
 * CategoriaFuncionario.java
 *
 * Created on 26/07/2007, 15:44:27
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.funcionario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author USUARIO
 */
@Entity
@Table(name = "categoria_funcionario")
public class CategoriaFuncionario implements Serializable {

    @Id
    @GeneratedValue
    private int id;
    private String nome = "";
    @OneToMany(cascade = CascadeType.ALL)
    private List<Caracteristica> caracteristicas = new ArrayList<Caracteristica>();

    /** Cria uma nova instância de CategoriaFuncionario */
    public CategoriaFuncionario() {
        this("", new ArrayList<Caracteristica>());
    }

    public CategoriaFuncionario(String nome) {
        this(nome, new ArrayList<Caracteristica>());
    }

    public CategoriaFuncionario(String nome, List<Caracteristica> caracteristicas) {
        this.nome = nome;
        this.caracteristicas = caracteristicas;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public List<Caracteristica> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<Caracteristica> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CategoriaFuncionario other = (CategoriaFuncionario) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.nome != other.nome && (this.nome == null || !this.nome.equals(other.nome))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.id;
        hash = 89 * hash + this.nome != null ? this.nome.hashCode() : 0;
        return hash;
    }

    @Override
    public String toString() {
        return nome;
    }
}