/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.financeiro.Pagamento;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */
@Entity
public class Cobranca implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @ManyToOne
    private Unidade unidade;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_vencimento")
    private Calendar dataVencimento;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_pagamento")
    private Calendar dataPagamento;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "vencimento_prorrogado")
    private Calendar vencimentoProrrogado;
    private String numeroDocumento;
    private BigDecimal valorTotal;
    @OneToMany(mappedBy = "cobranca", cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Calendar getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Calendar dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Calendar getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Calendar dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public Unidade getUnidade() {
        return unidade;
    }

    public void setUnidade(Unidade unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Calendar getVencimentoProrrogado() {
        return vencimentoProrrogado;
    }

    public void setVencimentoProrrogado(Calendar vencimentoProrrogado) {
        this.vencimentoProrrogado = vencimentoProrrogado;
    }
}
