/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "contrato_emprestimo")
public class ContratoEmprestimo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(name = "data_contrato")
    private Calendar dataContrato;
    @Column(name = "numero_parcela")
    private int numeroParcelas;
    @OneToMany(mappedBy = "contratoEmprestimo")
    private List<Pagamento> pagamentos = new ArrayList<Pagamento>();
    @ManyToOne(cascade = CascadeType.ALL)
    private Emprestimo emprestimo;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Calendar getDataContrato() {
        return dataContrato;
    }

    public void setDataContrato(Calendar dataContrato) {
        this.dataContrato = dataContrato;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(int numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public void setEmprestimo(Emprestimo emprestimo) {
        this.emprestimo = emprestimo;
    }

    public void adicionarPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamento.setContratoEmprestimo(this);
            pagamentos.add(pagamento);
        }
    }
}
