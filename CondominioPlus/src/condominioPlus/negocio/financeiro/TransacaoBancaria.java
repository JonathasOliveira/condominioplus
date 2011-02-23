/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
// Guarda os pagamentos relacionados na transação de transferencias entre contas investimento, emprestimo e poupanca!
@Entity
@Table(name="transacao_bancaria")
public class TransacaoBancaria implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @OneToMany(mappedBy = "transacaoBancaria")
    private List<Pagamento> pagamentos = new ArrayList<Pagamento>();

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

    public void adicionarPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamento.setTransacaoBancaria(this);
            pagamentos.add(pagamento);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(codigo);
    }
}
