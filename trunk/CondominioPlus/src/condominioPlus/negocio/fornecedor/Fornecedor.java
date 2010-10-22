/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.fornecedor;

import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.financeiro.Pagamento;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;

/**
 *
 * @author Administrador
 */
@Entity
public class Fornecedor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    private String nome;
    @OneToMany(mappedBy = "fornecedor")
    private List<Pagamento> pagamentos = new ArrayList<Pagamento>();
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_cadastro")
    private Calendar data_cadastro = Calendar.getInstance();
    @OneToMany(mappedBy = "fornecedor")
    private List<Telefone> telefones = new ArrayList<Telefone>();
    @OneToOne
    private Endereco endereco = new Endereco();

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<Telefone> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Calendar getData_cadastro() {
        return data_cadastro;
    }

    public void setData_cadastro(Calendar data_cadastro) {
        this.data_cadastro = data_cadastro;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }
    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }
}
