/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

/**
 *
 * @author Administrador
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 *
 * @author thiagocifani
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "CondominoPorCondominio", query = "SELECT c FROM Condomino c"),
    @NamedQuery(name = "CondominoPorConselheiro", query = "SELECT c FROM Condomino c")
})
public class Condomino implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int codigo;
    private String nome;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "condomino")
    private List<Endereco> enderecos = new ArrayList<Endereco>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "condomino")
    private List<Telefone> telefones = new ArrayList<Telefone>();
    private String cpf;
    private String rg;
    private String email;
    private String contato;
    @Column(name = "correspondencia_para")
    private String correspondenciaPara;
    private boolean falecido;
    private boolean conselheiro;
    @Column(name = "tipo_conselheiro")
    private String tipoConselheiro;
    private String anotacoes;
    private boolean cnpj;

    public Condomino() {
    }

    public boolean isCnpj() {
        return cnpj;
    }

    public void setCnpj(boolean cnpj) {
        this.cnpj = cnpj;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public String getTipoConselheiro() {
        return tipoConselheiro;
    }

    public void setTipoConselheiro(String tipoConselheiro) {
        this.tipoConselheiro = tipoConselheiro;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getCorrespondenciaPara() {
        return correspondenciaPara;
    }

    public void setCorrespondenciaPara(String correspondenciaPara) {
        this.correspondenciaPara = correspondenciaPara;
    }

    public boolean isFalecido() {
        return falecido;
    }

    public void setFalecido(boolean falecido) {
        this.falecido = falecido;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public boolean isConselheiro() {
        return conselheiro;
    }

    public void setConselheiro(boolean conselheiro) {
        this.conselheiro = conselheiro;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }

    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public List<Telefone> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }

    public void adicionarTelefone(Telefone telefone) {
        telefone.setCondomino(this);
        telefones.add(telefone);
    }

    public void removerTelefone(Telefone telefone) {
        telefones.remove(telefone);
    }

    public void adicionarEndereco(Endereco endereco) {
        enderecos.add(endereco);
    }

    public void removerEndereco(Endereco endereco) {
        enderecos.remove(endereco);
    }

    @Override
    public String toString() {
        return nome;
    }
}

