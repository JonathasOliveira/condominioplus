/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

import condominioPlus.negocio.financeiro.ContaCorrente;
import condominioPlus.negocio.financeiro.ContaPagar;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import logicpoint.persistencia.Removivel;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "CondominioPorRazaoSocial", query = "SELECT c FROM Condominio c WHERE c.razaoSocial like ?1 AND c.removido = false AND c.ativo = true")
})
public class Condominio implements Removivel, Comparable<Condominio>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int codigo;
    @Column(name = "razao_social")
    private String razaoSocial = "";
    private String email = "";
    private String contato = "";
    private String cnpj = "";
    private String zelador = "";
    private String site = "";
    @Temporal(value = TemporalType.DATE)
    @Column(name = "data_cadastro")
    private Calendar dataCadastro = Calendar.getInstance();
    private boolean ativo;
    private boolean removido;
    private String anotacoes = "";
    @Column(name = "responsavel_cheque")
    private String responsavelCheque = "";
    @Column(name = "responsave_cnpj")
    private String responsavelCnpj = "";
    @Column(name = "responsavel_cpf")
    private String responsavelCpf = "";
    private String instrumento = "";
    @OneToOne
    private Endereco endereco = new Endereco();
    @OneToMany(mappedBy = "condominio", fetch = FetchType.LAZY)
    private List<Telefone> telefones = new LinkedList<Telefone>();
    @OneToMany(mappedBy = "condominio", cascade = CascadeType.ALL)
    private List<Unidade> unidades = new LinkedList<Unidade>();
    @OneToOne(cascade = CascadeType.ALL)
    private ContaBancaria contaBancaria = new ContaBancaria();
    @OneToOne(cascade = CascadeType.ALL)
    private ContaCorrente contaCorrente = new ContaCorrente();
    @OneToOne(cascade = CascadeType.ALL)
    private ContaPagar contaPagar = new ContaPagar();
    @OneToMany(mappedBy = "condominio", cascade=CascadeType.ALL)
    private List<DadosTalaoCheque> dadosTalaoCheques = new LinkedList<DadosTalaoCheque>();
    @Column(name="numero_minimo_taloes")
    private int numeroMinimoTaloes = 0;

    public Condominio() {
    }

    public Condominio(String nome) {
        this.razaoSocial = nome;
    }

    public ContaBancaria getContaBancaria() {
        return contaBancaria;
    }

    public void setContaBancaria(ContaBancaria contaBancaria) {
        this.contaBancaria = contaBancaria;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public String getInstrumento() {
        return instrumento;
    }

    public void setInstrumento(String instrumento) {
        this.instrumento = instrumento;
    }

    public String getResponsavelCheque() {
        return responsavelCheque;
    }

    public void setResponsavelCheque(String responsavelCheque) {
        this.responsavelCheque = responsavelCheque;
    }

    public String getResponsavelCnpj() {
        return responsavelCnpj;
    }

    public void setResponsavelCnpj(String responsavelCnpj) {
        this.responsavelCnpj = responsavelCnpj;
    }

    public String getResponsavelCpf() {
        return responsavelCpf;
    }

    public void setResponsavelCpf(String responsavelCpf) {
        this.responsavelCpf = responsavelCpf;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public Calendar getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Calendar dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public List<Telefone> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }

    public List<Unidade> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<Unidade> unidades) {
        this.unidades = unidades;
    }

    public String getZelador() {
        return zelador;
    }

    public void setZelador(String zelador) {
        this.zelador = zelador;
    }

    public void setRemovido(boolean removido) {
        this.removido = removido;
    }

    public boolean isRemovido() {
        return removido;
    }

    public String getSindico() {
        for (Unidade unidade : unidades) {
            if (unidade.isSindico()) {
                return unidade.getCondomino().getNome();
            }
        }
        return "";
    }

    public int compareTo(Condominio condominio) {
        return getRazaoSocial().compareToIgnoreCase(condominio.getRazaoSocial());
    }

    @Override
    public String toString() {
        return razaoSocial;
    }

    public List<Unidade> getConselheiros() {
        List<Unidade> conselheiros = new ArrayList<Unidade>();

        for (Unidade u : getUnidades()) {
            if (u.getCondomino().isConselheiro()) {
                conselheiros.add(u);
            }
        }
        return conselheiros;
    }

    public ContaCorrente getContaCorrente() {
        return contaCorrente;
    }

    public void setContaCorrente(ContaCorrente contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    public ContaPagar getContaPagar() {
        return contaPagar;
    }

    public void setContaPagar(ContaPagar contaPagar) {
        this.contaPagar = contaPagar;
    }

    public List<DadosTalaoCheque> getDadosTalaoCheques() {
        return dadosTalaoCheques;
    }

    public void setDadosTalaoCheques(List<DadosTalaoCheque> dadosTalaoCheques) {
        this.dadosTalaoCheques = dadosTalaoCheques;
    }

    public int getNumeroMinimoTaloes() {
        return numeroMinimoTaloes;
    }

    public void setNumeroMinimoTaloes(int numeroMinimoTaloes) {
        this.numeroMinimoTaloes = numeroMinimoTaloes;
    }
}
