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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import logicpoint.persistencia.DAO;
import logicpoint.persistencia.Removivel;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "PagamentosContaCorrente", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.pago = true order by c.data_lancamento"),
    @NamedQuery(name = "PagamentosPorData", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 and c.data_lancamento >= ?2 order by c.data_lancamento"),
    @NamedQuery(name = "Pagamentos", query = "SELECT c FROM Pagamento c WHERE c.contaCorrente = ?1 order by c.data_lancamento")
})
public class Pagamento implements Serializable, Removivel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_lancamento")
    private Calendar data_lancamento;
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
    @Column(name = "numero_documento")
    private String numeroDocumento;
    @Column(precision = 20, scale = 2)
    private BigDecimal saldo = new BigDecimal(0);
    @ManyToOne
    private ContaCorrente contaCorrente;
    @ManyToOne
    private ContaPagar contaPagar;
    private boolean removido;
    private FormaPagamento forma = FormaPagamento.DINHEIRO;
    private boolean pago = false;

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

    public Calendar getData_lancamento() {
        return data_lancamento;
    }

    public void setData_lancamento(Calendar data_lancamento) {
        this.data_lancamento = data_lancamento;
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

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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
        List<Pagamento> lista = new DAO().listar(Pagamento.class, "PagamentosPorData", this.getData_lancamento());

        Pagamento p = lista.get(lista.size() - 1);

        this.setSaldo(this.valor.add(p.getSaldo()));

    }

    public void setRemovido(boolean removido) {
        this.removido = removido;
    }

    public boolean isRemovido() {
        return removido;
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

    
}

