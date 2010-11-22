/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

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
        List<Pagamento> pagamentos = new DAO().listar(Pagamento.class, "PagamentosPorData", this, dataFechamento);
        for (Pagamento pagamento : pagamentos) {
            System.out.println("pagamento " + pagamento.getCodigo() + " " + pagamento.getHistorico() + " " + DataUtil.toString(pagamento.getData_lancamento()));
        }
        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();

        Collections.sort(pagamentos, comCod);
        
        ComparatorPagamento comparator = new ComparatorPagamento();
        
        Collections.sort(pagamentos, comparator);
        
           for (Pagamento pagamento : pagamentos) {
            System.out.println("pagamento com comparatores " + pagamento.getCodigo() + " " + pagamento.getHistorico() + " " + DataUtil.toString(pagamento.getData_lancamento()));
        }
        for (int i = 0; i < pagamentos.size(); i++) {
            if (i != 0) {
                Pagamento p1 = pagamentos.get(i);
                if (p1.getConta().isCredito()) {
                    p1.setSaldo(pagamentos.get(i - 1).getSaldo().add(pagamentos.get(i).getValor()));
                } else {
                    p1.setSaldo(pagamentos.get(i - 1).getSaldo().subtract(pagamentos.get(i).getValor()));
                }
            }

            System.out.println("pagamento comparado por codigo " + pagamentos.get(i).getCodigo() + " " + pagamentos.get(i).getHistorico() + " " + DataUtil.toString(pagamentos.get(i).getData_lancamento()));
        }
        new DAO().salvar(pagamentos);

    }
}
    

