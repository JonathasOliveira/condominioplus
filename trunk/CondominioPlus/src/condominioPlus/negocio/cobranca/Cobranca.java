/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.financeiro.Pagamento;
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
import javax.persistence.Temporal;

/**
 *
 * @author eugenia
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "CobrancasPagasPorPeriodo", query = "SELECT c FROM Cobranca c WHERE c.unidade.condominio = ?1 AND c.dataPagamento >= ?2 AND c.dataPagamento <= ?3 ORDER BY c.dataPagamento, c.unidade.unidade"),
    @NamedQuery(name = "CobrancasPagasPorPeriodoUnidade", query = "SELECT c FROM Cobranca c WHERE c.unidade = ?1 AND c.dataPagamento >= ?2 AND c.dataPagamento <=?3 ORDER BY c.dataPagamento, c.unidade.unidade"),
    @NamedQuery(name = "CobrancasEmAberto", query = "SELECT c FROM Cobranca c WHERE c.dataPagamento is null ORDER BY c.dataVencimento"),
    @NamedQuery(name = "CobrancasEmAbertoPorUnidade", query = "SELECT c FROM Cobranca c WHERE c.dataPagamento is null AND c.unidade = ?1 ORDER BY c.dataVencimento"),
    @NamedQuery(name = "CobrancaPorNumeroDocumento", query = "SELECT c FROM Cobranca c WHERE c.numeroDocumento LIKE ?1")
})
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
    @Column(name = "numero_documento")
    private String numeroDocumento;
    @Column(name = "valor_original", precision = 20, scale = 2)
    private BigDecimal valorOriginal;
    @Column(name = "valor_total", precision = 20, scale = 2)
    private BigDecimal valorTotal;
    @Column(name = "valor_pago", precision = 20, scale = 2)
    private BigDecimal valorPago;
    @Column(precision = 20, scale = 2)
    private BigDecimal juros = new BigDecimal(0);
    @Column(name = "diferenca_pagamento", precision = 20, scale = 2)
    private BigDecimal diferencaPagamento = new BigDecimal(0);
    @Column(name = "desconto", precision = 20, scale = 2)
    private BigDecimal desconto = new BigDecimal(0);
    @Column(precision = 20, scale = 2)
    private BigDecimal multa = new BigDecimal(0);
    @OneToMany(mappedBy = "cobranca", cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos = new ArrayList<Pagamento>();
    @Column(name = "linha_digitavel")
    private String linhaDigitavel;
    @Column(name="lancado_caixa")
    private boolean lancadoCaixa;
    @ManyToOne
    private AcordoCobranca acordo;
    @ManyToOne
    private HistoricoAcordo historico;
    private boolean exibir = true;

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

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public BigDecimal getJuros() {
        return juros;
    }

    public void setJuros(BigDecimal juros) {
        this.juros = juros;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public void setMulta(BigDecimal multa) {
        this.multa = multa;
    }

    public BigDecimal getDiferencaPagamento() {
        return diferencaPagamento;
    }

    public void setDiferencaPagamento(BigDecimal diferencaPagamento) {
        this.diferencaPagamento = diferencaPagamento;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }
   
    public Calendar getVencimentoProrrogado() {
        return vencimentoProrrogado;
    }

    public void setVencimentoProrrogado(Calendar vencimentoProrrogado) {
        this.vencimentoProrrogado = vencimentoProrrogado;
    }

    public String getLinhaDigitavel() {
        return linhaDigitavel;
    }

    public void setLinhaDigitavel(String linhaDigitavel) {
        this.linhaDigitavel = linhaDigitavel;
    }

    public boolean isLancadoCaixa() {
        return lancadoCaixa;
    }

    public void setLancadoCaixa(boolean lancadoCaixa) {
        this.lancadoCaixa = lancadoCaixa;
    }

    public AcordoCobranca getAcordo() {
        return acordo;
    }

    public void setAcordo(AcordoCobranca acordo) {
        this.acordo = acordo;
    }

    public HistoricoAcordo getHistorico() {
        return historico;
    }

    public void setHistorico(HistoricoAcordo historico) {
        this.historico = historico;
    }

    public boolean isExibir() {
        return exibir;
    }

    public void setExibir(boolean exibir) {
        this.exibir = exibir;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cobranca other = (Cobranca) obj;
        if (this.codigo != other.codigo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.codigo;
        return hash;
    }
    
}
