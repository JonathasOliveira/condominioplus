/*
 * Funcionario.java
 *
 * Created on 26/07/2007, 15:49:32
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.funcionario;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import condominioPlus.negocio.Endereco;
import logicpoint.persistencia.Removivel;
import logicpoint.util.DataUtil;
import logicpoint.usuario.Usuario;
import org.joda.time.DateTime;

/**
 *
 * @author USUARIO
 */
@Entity
@NamedQueries(value = {
@NamedQuery(name = "FuncionarioPorLogin", query = "SELECT f FROM Funcionario f WHERE f.usuario.usuario like ?1 AND f.removido = false"),
@NamedQuery(name = "FuncionarioPorUsuario", query = "SELECT f FROM Funcionario f WHERE f.usuario = ?1 AND f.removido = false")
})
public class Funcionario implements Removivel, Comparable<Funcionario>, Serializable {

    @Id
    @GeneratedValue
    private int id;
    private String nome = "";
    @OneToOne(cascade = CascadeType.ALL)
    private Usuario usuario = new Usuario();
    private String sexo;
    private String cpf = "";
    private String identidade = "";
    @Temporal(value = TemporalType.DATE)
    @Column(name = "data_nascimento")
    private Calendar dataNascimento = Calendar.getInstance();
    @Column(name = "estado_civil")
    private String estadoCivil = "";
    @OneToOne(cascade = CascadeType.ALL)
    private Endereco endereco = new Endereco();
    @ManyToOne(fetch = FetchType.LAZY)
    private CategoriaFuncionario categoria;
    private boolean removido;

    /** Cria uma nova instância de Funcionario */
    public Funcionario() {
    }

    public Funcionario(String nome) {
        this.nome = nome;
    }

    public int compareTo(Funcionario funcionario) {
        return getNome().compareToIgnoreCase(funcionario.getNome());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public CategoriaFuncionario getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaFuncionario categoria) {
        this.categoria = categoria;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public DateTime getDataNascimento() {
        return DataUtil.getDateTime(dataNascimento);
    }

    public void setDataNascimento(DateTime dataNascimento) {
        this.dataNascimento = DataUtil.getCalendar(dataNascimento);
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getIdentidade() {
        return identidade;
    }

    public void setIdentidade(String identidade) {
        this.identidade = identidade;
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
        final Funcionario other = (Funcionario) obj;
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
        int hash = 5;
        hash = 71 * hash + this.id;
        hash = 71 * hash + this.nome != null ? this.nome.hashCode() : 0;
        return hash;
    }

    @Override
    public String toString() {
        return nome;
    }

    public void setRemovido(boolean removido) {
        this.removido = removido;
    }

    public boolean isRemovido() {
        return removido;
    }
}
