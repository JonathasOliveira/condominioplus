/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.OneToOne;
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
    @OneToOne
    private Condominio condominio;

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

    public void calculaSaldo(ContaCorrente contaCorrente) {

        Calendar novaData = Calendar.getInstance();

        novaData = DataUtil.getCalendar(dataFechamento);


        novaData.add(Calendar.DAY_OF_MONTH, -1);
        System.out.println("data " + DataUtil.toString(novaData));

        List<Pagamento> pagamentos = new DAO().listar(Pagamento.class, "PagamentosPorData", this, novaData);

        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();

        Collections.sort(pagamentos, comCod);

        ComparatorPagamento comparator = new ComparatorPagamento();

        Collections.sort(pagamentos, comparator);
        for (Pagamento pagamento : pagamentos) {
            System.out.println("pagamento " + pagamento.getHistorico() + " " + DataUtil.toString(pagamento.getDataPagamento()));
        }
        if (!pagamentos.isEmpty()){
            if(DataUtil.compararData(DataUtil.getDateTime(pagamentos.get(0).getDataPagamento()), DataUtil.getDateTime(dataFechamento)) == 1 ){
                Pagamento p2 = pagamentos.get(0);
                BigDecimal valor = pagamentos.get(0).getValor();
                 p2.setSaldo(valor);
            }
        }

        for (int i = 0; i < pagamentos.size(); i++) {
            if (i != 0) {
                Pagamento p1 = pagamentos.get(i);
                System.out.println("p1 " + p1.getHistorico() + " " + DataUtil.toString(p1.getDataPagamento()));
                Pagamento pagamentoAnterior = pagamentos.get(i - 1);
                p1.setSaldo(pagamentoAnterior.getSaldo().add(p1.getValor()));

//                if (p1.getConta().isCredito()) {
//                    p1.setSaldo(pagamentoAnterior.getSaldo().add(p1.getValor()));
//                } else {
//                    p1.setSaldo(pagamentoAnterior.getSaldo().subtract(p1.getValor()));
//                }
                condominio.getContaCorrente().setSaldo(p1.getSaldo());
                new DAO().salvar(p1);
            }
        }

        contaCorrente.setPagamentos(pagamentos);
        new DAO().salvar(contaCorrente);
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }
}
    

