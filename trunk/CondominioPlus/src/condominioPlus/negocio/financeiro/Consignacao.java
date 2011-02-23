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
import logicpoint.persistencia.DAO;

/**
 *
 * @author Administrador
 */

@Entity
public class Consignacao implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @OneToMany(mappedBy = "consignacao", cascade = CascadeType.ALL)
    private List<Pagamento> pagamentos = new ArrayList<Pagamento>();
    @Column(precision = 20, scale = 2)
    private BigDecimal saldo = new BigDecimal(0);
    @OneToOne
    private Condominio condominio;

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
            pagamento.setConsignacao(this);
            pagamentos.add(pagamento);
        }
    }

    public void calculaSaldo() {

        Calendar novaData = Calendar.getInstance();


        novaData.add(Calendar.DAY_OF_MONTH, -1);

        List<Pagamento> pagamentos = new DAO().listar(Pagamento.class, "PagamentosConsignacaoOrdenados", this);

        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();

        Collections.sort(pagamentos, comCod);

        ComparatorPagamento comparator = new ComparatorPagamento();

        Collections.sort(pagamentos, comparator);

        for (int i = 0; i < pagamentos.size(); i++) {
            if (i != 0) {
                Pagamento p1 = pagamentos.get(i);
                Pagamento pagamentoAnterior = pagamentos.get(i - 1);
                p1.setSaldo(pagamentoAnterior.getSaldo().add(p1.getValor()));

                condominio.getConsignacao().setSaldo(p1.getSaldo());
            }

        }

        new DAO().salvar(pagamentos);

    }

}
