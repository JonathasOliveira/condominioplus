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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 *
 * @author Administrador
 */

@Entity
public class Emprestimo implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @OneToOne
    private Condominio condominio;
    @OneToMany(mappedBy = "emprestimo", cascade = CascadeType.ALL)
    private List<ContratoEmprestimo> contratos;
    private BigDecimal saldo;

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

    public List<ContratoEmprestimo> getContratos() {
        return contratos;
    }

    public void setContratos(List<ContratoEmprestimo> contratos) {
        this.contratos = contratos;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public void calculaSaldo(Emprestimo emprestimo) {

        Calendar novaData = Calendar.getInstance();


        novaData.add(Calendar.DAY_OF_MONTH, -1);

        List<Pagamento> pagamentos = new ArrayList<Pagamento>();

        for (ContratoEmprestimo contrato : emprestimo.getContratos()){
            for (Pagamento p: contrato.getPagamentos()){
                pagamentos.add(p);
            }
        }

        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();

        Collections.sort(pagamentos, comCod);

        ComparatorPagamento comparator = new ComparatorPagamento();

        Collections.sort(pagamentos, comparator);

        for (int i = 0; i < pagamentos.size(); i++) {
            Pagamento p1 = pagamentos.get(i);
            if (i == 0) {
                p1.setSaldo(p1.getValor());
            } else if (i != 0) {
                Pagamento pagamentoAnterior = pagamentos.get(i - 1);
                p1.setSaldo(pagamentoAnterior.getSaldo().add(p1.getValor()));

                condominio.getEmprestimo().setSaldo(p1.getSaldo());
            }

        }

        // falta implementar código para salvar os pagamentos

    }

}
