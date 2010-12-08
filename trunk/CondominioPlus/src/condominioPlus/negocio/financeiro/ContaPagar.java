/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Administrador
 */
@Entity
public class ContaPagar implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @OneToMany
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

    public void adicionarPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamento.setContaPagar(this);
            pagamentos.add(pagamento);
        }
    }

    public void removerPagamento(Pagamento pagamento) {
        if (pagamento != null) {
            pagamentos.remove(pagamento);
        }
    }
}
