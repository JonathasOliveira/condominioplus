/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "conta_corrente")
public class ContaCorrente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @OneToMany(mappedBy = "contaCorrente", cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos = new ArrayList<Pagamento>();
    @Column(precision = 20, scale = 2)
    private BigDecimal saldo = new BigDecimal(0);
    @Column(name = "data_fechamento")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataFechamento;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public void adicionarPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamento.setContaCorrente(this);
            pagamentos.add(pagamento);
            calculaSaldo();
        }
    }

    public void removerPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamentos.remove(pagamento);
        }
    }

    public Calendar getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(Calendar dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public void calculaSaldo() {
        saldo = new BigDecimal(0);
        if(!pagamentos.isEmpty()){
        for (Pagamento pagamento : pagamentos) {
            if (pagamento.getConta().isCredito()) {
                if (dataFechamento.before(pagamento.getData_lancamento())) {
                    if (pagamento.getSaldo().intValue() == 0) {
                        saldo = saldo.add(pagamento.getValor());
                        pagamento.setSaldo(saldo);
                    }
                }
            } else if (!pagamento.getConta().isCredito()) {
                if (dataFechamento.before(pagamento.getData_lancamento())) {
                    if (pagamento.getSaldo().intValue() == 0) {
                        saldo = saldo.subtract(pagamento.getValor());
                        pagamento.setSaldo(saldo);
                    }
                }
            }
        }
        }
        this.setSaldo(saldo);
    }
}
