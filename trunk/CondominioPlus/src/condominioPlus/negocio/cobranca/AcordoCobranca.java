/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.financeiro.FormaPagamentoEmprestimo;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */
@Entity
@Table(name = "acordo_cobranca")
@NamedQueries(value = {
    @NamedQuery(name = "AcordosPorUnidade", query = "SELECT ac FROM AcordoCobranca ac WHERE ac.unidade = ?1 ORDER BY ac.dataPrimeiroPagamento")
})
public class AcordoCobranca implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @ManyToOne
    private Unidade unidade;
    @OneToMany(mappedBy = "acordo", cascade = CascadeType.ALL)
    private List<Cobranca> cobrancasGeradas = new ArrayList<Cobranca>();
    @Column(precision = 20, scale = 2)
    private BigDecimal valor;
    private FormaPagamentoEmprestimo forma;
    @Column(name = "numero_parcelas")
    private int numeroParcelas;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_primeiro_pagamento")
    private Calendar dataPrimeiroPagamento;
    @OneToOne(cascade=CascadeType.ALL)
    private HistoricoAcordo historico = new HistoricoAcordo();

    public List<Cobranca> getCobrancasGeradas() {
        return cobrancasGeradas;
    }

    public void setCobrancasGeradas(List<Cobranca> cobrancasGeradas) {
        this.cobrancasGeradas = cobrancasGeradas;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public FormaPagamentoEmprestimo getForma() {
        return forma;
    }

    public void setForma(FormaPagamentoEmprestimo forma) {
        this.forma = forma;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(int numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public Calendar getDataPrimeiroPagamento() {
        return dataPrimeiroPagamento;
    }

    public void setDataPrimeiroPagamento(Calendar dataPrimeiroPagamento) {
        this.dataPrimeiroPagamento = dataPrimeiroPagamento;
    }

    public HistoricoAcordo getHistorico() {
        return historico;
    }

    public void setHistorico(HistoricoAcordo historico) {
        this.historico = historico;
    }

}
