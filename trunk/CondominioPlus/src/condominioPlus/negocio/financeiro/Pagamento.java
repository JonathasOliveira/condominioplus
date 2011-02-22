/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

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
 * @author Administrador
 */

@Entity
@NamedQueries(value = {
    @NamedQuery(name = "PagamentosContaCorrente", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.pago = true order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosPorData", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.dataPagamento >= ?2 and c.pago =  true order by c.dataPagamento"),
    @NamedQuery(name = "Pagamentos", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 order by c.dataPagamento"),
    @NamedQuery(name = "PagamentosContaPagar", query = "SELECT c FROM Pagamento c WHERE c.contaPagar = ?1 and c.pago = false order by c.dataVencimento"),
    @NamedQuery(name = "PagamentosContaPagarPorPeriodo", query = "SELECT p FROM Pagamento p WHERE p.contaPagar = ?1 and p.pago = false and p.dataVencimento >= ?2 and p.dataVencimento <= ?3 order by p.dataVencimento"),
    @NamedQuery(name = "PagamentosPorFornecedor", query = "SELECT p FROM Pagamento p WHERE p.fornecedor = ?1 and p.dataPagamento >= ?2 and p.dataPagamento <= ?3 order by p.dataPagamento"),
    @NamedQuery(name = "PagamentosPorNumeroDocumento", query = "SELECT c FROM Pagamento c WHERE c.contaPagar = ?1 and c.pago = false and c.dadosPagamento = ?2"),
    @NamedQuery(name = "PagamentosPorForma", query = "SELECT c FROM Pagamento c WHERE c.contaPagar = ?1 and c.pago = false and c.forma = ?2"),
    @NamedQuery(name = "PagamentosAplicacaoFinanceira", query = "SELECT c FROM Pagamento c WHERE c.aplicacao = ?1"),
    @NamedQuery(name = "PagamentosPoupanca", query = "SELECT c FROM Pagamento c WHERE c.poupanca = ?1"),
    @NamedQuery(name = "PagamentosPoupancaOrdenados", query="SELECT p FROM Pagamento p WHERE p.poupanca = ?1 order by p.dataPagamento")
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
    private AplicacaoFinanceira aplicacao;
    @ManyToOne
    private Poupanca poupanca;
    private FormaPagamento forma = FormaPagamento.DINHEIRO;
    @OneToOne(cascade = CascadeType.ALL)
    private DadosPagamento dadosPagamento;
    private boolean pago = false;
    @ManyToOne(cascade=CascadeType.ALL)
    private TransacaoBancaria transacaoBancaria;

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
    
}

