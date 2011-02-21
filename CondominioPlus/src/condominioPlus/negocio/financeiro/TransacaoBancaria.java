/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Administrador
 */
// Guarda os pagamentos relacionados na transação de transferencias entre contas investimento, emprestimo e poupanca!

@Entity
public class TransacaoBancaria implements Serializable {
    @Id
    private int codigo;
    @OneToMany(cascade=CascadeType.ALL, mappedBy="transacaoBancaria")
    private List<Pagamento> pagamentos;

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

}
