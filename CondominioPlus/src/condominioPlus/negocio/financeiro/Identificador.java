/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author eugenia
 */
@Entity
public class Identificador implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @Column(name="codigo_historico")
    private int codigoHistorico;
    @ManyToOne
    private Conta conta;
    @Column(name = "palavra_chave")
    private String palavraChave;

    public Identificador() {
    }
    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public String getPalavraChave() {
        return palavraChave;
    }

    public void setPalavraChave(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    public int getCodigoHistorico() {
        return codigoHistorico;
    }

    public void setCodigoHistorico(int codigoHistorico) {
        this.codigoHistorico = codigoHistorico;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identificador other = (Identificador) obj;
        if (this.codigo != other.codigo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + this.codigo;
        return hash;
    }

    
    
}
