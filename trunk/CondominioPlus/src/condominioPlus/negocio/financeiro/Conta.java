/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "ListarContas", query = "SELECT c FROM Conta c WHERE c.credito = ?1 order by c.codigo"),
    @NamedQuery(name = "LocalizarContas", query = "SELECT c FROM Conta c WHERE c.codigo = ?1 and c.credito = ?2 order by c.codigo")
})

public class Conta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    private String nome;
    private boolean credito;
    private boolean vinculada = false;
    private String nomeVinculo = "";

    public Conta() {
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public boolean isCredito() {
        return credito;
    }

    public void setCredito(boolean credito) {
        this.credito = credito;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeVinculo() {
        return nomeVinculo;
    }

    public void setNomeVinculo(String nomeVinculo) {
        this.nomeVinculo = nomeVinculo;
    }

    public boolean isVinculada() {
        return vinculada;
    }

    public void setVinculada(boolean vinculada) {
        this.vinculada = vinculada;
    }

    @Override
    public String toString() {
        return nome;
    }
}