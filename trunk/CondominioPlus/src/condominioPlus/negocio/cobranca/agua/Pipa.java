/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.cobranca.agua;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Administrador
 */
@Entity
public class Pipa implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    private String descricao;
    @Column(name="quantidade_litros_por_mil")
    private int quantidadeLitrosPorMil;
    @Column(name="total_pago", precision=20, scale=2)
    private BigDecimal totalPago;
    @ManyToOne
    private ContaAgua conta;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getQuantidadeLitrosPorMil() {
        return quantidadeLitrosPorMil;
    }

    public void setQuantidadeLitrosPorMil(int quantidadeLitrosPorMil) {
        this.quantidadeLitrosPorMil = quantidadeLitrosPorMil;
    }

    public BigDecimal getTotalPago() {
        return totalPago;
    }

    public void setTotalPago(BigDecimal totalPago) {
        this.totalPago = totalPago;
    }

    public ContaAgua getConta() {
        return conta;
    }

    public void setConta(ContaAgua conta) {
        this.conta = conta;
    }

    

    




}
