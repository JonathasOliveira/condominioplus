/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import logicpoint.persistencia.Removivel;

/**
 *
 * @author Administrador
 */
@Entity
public class Banco implements Serializable, Removivel {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int codigo;
    @Column(name="numero_banco")
    private String numeroBanco = "";
    private String agencia = "";
    @Column(name="nome_banco")
    private String nomeBanco = "";
    private boolean removido;

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNomeBanco() {
        return nomeBanco;
    }

    public void setNomeBanco(String nomeBanco) {
        this.nomeBanco = nomeBanco;
    }

    public String getNumeroBanco() {
        return numeroBanco;
    }

    public void setNumeroBanco(String numeroBanco) {
        this.numeroBanco = numeroBanco;
    }

    public void setRemovido(boolean removido) {
        this.removido = removido;
    }

    public boolean isRemovido() {
        return removido;
    }

    @Override
    public String toString() {
        return nomeBanco;
    }


}
