/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @OneToOne
    private Condominio condominio;
    @OneToMany(mappedBy = "emprestimo", cascade = CascadeType.ALL)
    private List<ContratoEmprestimo> contratos;
    @Column(precision = 20, scale = 2)
    private BigDecimal saldo = new BigDecimal(0);

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
}
