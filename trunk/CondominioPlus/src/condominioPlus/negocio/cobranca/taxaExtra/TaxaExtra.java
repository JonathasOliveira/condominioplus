/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca.taxaExtra;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */
@Entity
@Table(name = "taxa_extra")
public class TaxaExtra implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar primeiroVencimento;
    private int numeroCotas;
    private int cobrancasADescartar;
    private BigDecimal valor;
    @ManyToOne
    private Condominio condominio;
    @ManyToOne
    private Conta conta;
    private String descricao;
    private boolean dividirFracaoIdeal;
    private boolean cobrarComCondominio;
    private boolean sindicoPaga;
    @OneToMany(mappedBy = "taxa", cascade = CascadeType.ALL)
    private List<ParcelaTaxaExtra> parcelas = new ArrayList<ParcelaTaxaExtra>();

    public boolean isCobrarComCondominio() {
        return cobrarComCondominio;
    }

    public void setCobrarComCondominio(boolean cobrarComCondominio) {
        this.cobrarComCondominio = cobrarComCondominio;
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

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Calendar getPrimeiroVencimento() {
        return primeiroVencimento;
    }

    public void setPrimeiroVencimento(Calendar primeiroVencimento) {
        this.primeiroVencimento = primeiroVencimento;
    }

    public boolean isDividirFracaoIdeal() {
        return dividirFracaoIdeal;
    }

    public void setDividirFracaoIdeal(boolean dividirFracaoIdeal) {
        this.dividirFracaoIdeal = dividirFracaoIdeal;
    }

    public int getNumeroCotas() {
        return numeroCotas;
    }

    public void setNumeroCotas(int numeroCotas) {
        this.numeroCotas = numeroCotas;
    }

    public int getCobrancasADescartar() {
        return cobrancasADescartar;
    }

    public void setCobrancasADescartar(int cobrancasADescartar) {
        this.cobrancasADescartar = cobrancasADescartar;
    }

    public boolean isSindicoPaga() {
        return sindicoPaga;
    }

    public void setSindicoPaga(boolean sindicoPaga) {
        this.sindicoPaga = sindicoPaga;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public List<ParcelaTaxaExtra> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ParcelaTaxaExtra> parcelas) {
        this.parcelas = parcelas;
    }
}
