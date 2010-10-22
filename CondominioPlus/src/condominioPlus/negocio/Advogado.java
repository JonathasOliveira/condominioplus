/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 *
 * @author Administrador
 */
@Entity
public class Advogado implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int codigo;
    private String nome;
    @Column(name="numero_ordem")
    private String numero_ordem;
    @OneToMany(mappedBy = "advogado",cascade=CascadeType.ALL)
    private List<Telefone> telefones = new ArrayList<Telefone>();
    @OneToOne(cascade=CascadeType.ALL)
    private Endereco endereco = new Endereco();
    @OneToMany(mappedBy = "advogado",cascade=CascadeType.ALL)
    private List<ProcessoJudicial> processoJudiciais = new ArrayList<ProcessoJudicial>();
    @OneToMany(mappedBy = "advogado",cascade=CascadeType.ALL)
    private List<NotificacaoJudicial> notificacaoJudiciais =  new ArrayList<NotificacaoJudicial>();

    public Advogado() {
    }

    public Advogado(String nome) {
        this.nome = nome;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNumero_ordem() {
        return numero_ordem;
    }

    public void setNumero_ordem(String numero_ordem) {
        this.numero_ordem = numero_ordem;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Telefone> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }

    public List<ProcessoJudicial> getProcessoJudiciais() {
        return processoJudiciais;
    }

    public void setProcessoJudiciais(List<ProcessoJudicial> processoJudiciais) {
        this.processoJudiciais = processoJudiciais;
    }

    public List<NotificacaoJudicial> getNotificacaoJudiciais() {
        return notificacaoJudiciais;
    }

    public void setNotificacaoJudiciais(List<NotificacaoJudicial> notificacaoJudiciais) {
        this.notificacaoJudiciais = notificacaoJudiciais;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Advogado other = (Advogado) obj;
        if (this.codigo != other.codigo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.codigo;
        return hash;
    }

    @Override
    public String toString() {
        return nome == null ? "" : nome;
    }


}
