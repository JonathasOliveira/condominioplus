/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.fornecedor.Fornecedor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import logicpoint.persistencia.DAO;

/**
 *
 * @author Administrado
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "PagamentosContaCorrente", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.pago = true order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosConciliacao", query = "SELECT c FROM Pagamento c WHERE c.conciliacao = ?1 and c.pago = false order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosPorData", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.dataPagamento >= ?2 and c.pago =  true order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosDoDia", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.dataPagamento >= ?2 and c.dataPagamento <= ?3 and c.pago =  true order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosDoMes", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.dataPagamento >= ?2 and c.dataPagamento <= ?3 and c.pago =  true order by c.dataPagamento"),
    @NamedQuery(name = "Pagamentos", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosContaPagar", query = "SELECT c FROM Pagamento c WHERE c.contaPagar = ?1 and c.pago = false order by c.dataVencimento"),
    @NamedQuery(name = "PagamentosContaPagarPorPeriodo", query = "SELECT p FROM Pagamento p WHERE p.contaPagar = ?1 and p.pago = false and p.dataVencimento >= ?2 and p.dataVencimento <= ?3 order by p.dataVencimento"),
    @NamedQuery(name = "PagamentosContaReceber", query = "SELECT c FROM Pagamento c WHERE c.contaReceber = ?1 and c.pago = false order by c.dataVencimento"),
    @NamedQuery(name = "PagamentosContaReceberPorPeriodo", query = "SELECT p FROM Pagamento p WHERE p.contaReceber = ?1 and p.pago = false and p.dataVencimento >= ?2 and p.dataVencimento <= ?3 order by p.dataVencimento"),
    @NamedQuery(name = "PagamentosPorFornecedor", query = "SELECT p FROM Pagamento p WHERE p.fornecedor = ?1 and p.dataPagamento >= ?2 and p.dataPagamento <= ?3 order by p.dataPagamento"),
    @NamedQuery(name = "PagamentosPorNumeroDocumento", query = "SELECT c FROM Pagamento c WHERE c.contaPagar = ?1 and c.pago = false and c.dadosPagamento = ?2"),
    @NamedQuery(name = "PagamentosPorNumeroDocumentoContaReceber", query = "SELECT c FROM Pagamento c WHERE c.contaReceber = ?1 and c.pago = false and c.dadosPagamento = ?2"),
    @NamedQuery(name = "PagamentosPorForma", query = "SELECT c FROM Pagamento c WHERE c.contaPagar = ?1 and c.pago = false and c.forma = ?2"),
    @NamedQuery(name = "PagamentosAplicacaoFinanceira", query = "SELECT c FROM Pagamento c WHERE c.aplicacao = ?1"),
    @NamedQuery(name = "PagamentosAplicacaoOrdenados", query = "SELECT p FROM Pagamento p WHERE p.aplicacao = ?1 order by p.dataPagamento"),
    @NamedQuery(name = "PagamentosPoupanca", query = "SELECT c FROM Pagamento c WHERE c.poupanca = ?1"),
    @NamedQuery(name = "PagamentosPoupancaOrdenados", query = "SELECT p FROM Pagamento p WHERE p.poupanca = ?1 order by p.dataPagamento"),
    @NamedQuery(name = "PagamentosConsignacao", query = "SELECT c FROM Pagamento c WHERE c.consignacao = ?1"),
    @NamedQuery(name = "PagamentosConsignacaoOrdenados", query = "SELECT p FROM Pagamento p WHERE p.consignacao = ?1 order by p.dataPagamento"),
    @NamedQuery(name = "PagamentosPorContratoEmprestimo", query = "SELECT p FROM Pagamento p WHERE p.contratoEmprestimo = ?1 order by p.dataVencimento"),
    @NamedQuery(name = "PagamentosPorContratoEmprestimoCodigo", query = "SELECT p FROM Pagamento p WHERE p.contratoEmprestimo = ?1 and p.contaCorrente is null and p.contaPagar is null order by p.codigo"),
    @NamedQuery(name = "PagamentosPorPeriodoContaCorrente", query = "SELECT p FROM Pagamento p WHERE p.contaCorrente = ?1 and p.pago = true and p.dataPagamento >= ?2 and p.dataPagamento <= ?3 order by p.dataPagamento")
})
public class Pagamento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_pagamento")
    private Calendar dataPagamento;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_vencimento")
    private Calendar dataVencimento;
    @Column(precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);
    private String historico;
    private String descricao = " ";
    @ManyToOne
    private Fornecedor fornecedor;
    @ManyToOne
    private Conta conta;
    @Column(precision = 20, scale = 2)
    private BigDecimal saldo = new BigDecimal(0);
    @ManyToOne
    private ContaCorrente contaCorrente;
    @ManyToOne
    private ContaPagar contaPagar;
    @ManyToOne
    private ContaReceber contaReceber;
    @ManyToOne
    private AplicacaoFinanceira aplicacao;
    @ManyToOne
    private Poupanca poupanca;
    @ManyToOne
    private Conciliacao conciliacao;
    @ManyToOne
    private Consignacao consignacao;
    private FormaPagamento forma = FormaPagamento.DINHEIRO;
    @OneToOne(cascade = CascadeType.ALL)
    private DadosPagamento dadosPagamento;
    private boolean pago = false;
    @ManyToOne(cascade = CascadeType.ALL)
    private TransacaoBancaria transacaoBancaria;
    @ManyToOne(cascade = CascadeType.ALL)
    private ContratoEmprestimo contratoEmprestimo;
    @ManyToOne
    private Cobranca cobranca;

    public Calendar getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Calendar dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public ContaCorrente getContaCorrente() {
        return contaCorrente;
    }

    public void setContaCorrente(ContaCorrente contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public Calendar getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Calendar dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public String getHistorico() {
        return historico;
    }

    public void setHistorico(String historico) {
        this.historico = historico;
    }
    
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public void setForma(FormaPagamento forma) {
        this.forma = forma;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pagamento other = (Pagamento) obj;
        if (this.codigo != other.codigo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.codigo;
        return hash;
    }

    public void calcularSaldo() {
        List<Pagamento> lista = new DAO().listar(Pagamento.class, "PagamentosPorData", this.getDataPagamento());

        Pagamento p = lista.get(lista.size() - 1);

        this.setSaldo(this.valor.add(p.getSaldo()));

    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public ContaPagar getContaPagar() {
        return contaPagar;
    }

    public void setContaPagar(ContaPagar contaPagar) {
        this.contaPagar = contaPagar;
    }

    public ContaReceber getContaReceber() {
        return contaReceber;
    }

    public void setContaReceber(ContaReceber contaReceber) {
        this.contaReceber = contaReceber;
    }

    public DadosPagamento getDadosPagamento() {
        return dadosPagamento;
    }

    public void setDadosPagamento(DadosPagamento dadosPagamento) {
        this.dadosPagamento = dadosPagamento;
    }

    public static String gerarNumeroDocumento() {
        Long resultado = (Long) new DAO().localizar("MaxNumeroDocumento");

        if (resultado == null) {
            resultado = (long) 0;
        }

        String valor = String.valueOf(resultado);

        if (valor == null) {
            return "1";
        }
        int novoValor = Integer.parseInt(valor) + 1;

        return String.valueOf(novoValor);
    }

    public AplicacaoFinanceira getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(AplicacaoFinanceira aplicacao) {
        this.aplicacao = aplicacao;
    }

    public TransacaoBancaria getTransacaoBancaria() {
        return transacaoBancaria;
    }

    public void setTransacaoBancaria(TransacaoBancaria transacaoBancaria) {
        this.transacaoBancaria = transacaoBancaria;
    }

    public Poupanca getPoupanca() {
        return poupanca;
    }

    public void setPoupanca(Poupanca poupanca) {
        this.poupanca = poupanca;
    }

    public Consignacao getConsignacao() {
        return consignacao;
    }

    public void setConsignacao(Consignacao consignacao) {
        this.consignacao = consignacao;
    }

    public ContratoEmprestimo getContratoEmprestimo() {
        return contratoEmprestimo;
    }

    public void setContratoEmprestimo(ContratoEmprestimo contratoEmprestimo) {
        this.contratoEmprestimo = contratoEmprestimo;
    }

    public Conciliacao getConciliacao() {
        return conciliacao;
    }

    public void setConciliacao(Conciliacao conciliacao) {
        this.conciliacao = conciliacao;
    }

    public Cobranca getCobranca() {
        return cobranca;
    }

    public void setCobranca(Cobranca cobranca) {
        this.cobranca = cobranca;
    }

}

