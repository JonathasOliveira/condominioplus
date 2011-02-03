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
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "dados_talao_cheque")
public class DadosTalaoCheque implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Column(name="numero_inicial")
    private String numeroInicial;
    @Column(name="numero_final")
    private String numeroFinal;

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNumeroFinal() {
        return numeroFinal;
    }

    public void setNumeroFinal(String numeroFinal) {
        this.numeroFinal = numeroFinal;
    }

    public String getNumeroInicial() {
        return numeroInicial;
    }

    public void setNumeroInicial(String numeroInicial) {
        this.numeroInicial = numeroInicial;
    }
}