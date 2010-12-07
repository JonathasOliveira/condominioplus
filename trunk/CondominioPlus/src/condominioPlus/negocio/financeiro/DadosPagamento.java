/*
 * DadosPagamento.java
 *
 * Created on 09/10/2007, 11:42:29
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Thiago
 */
@Entity
@Table(name = "dados_pagamento")
public abstract class DadosPagamento implements Serializable {

    @Id
    @GeneratedValue
    private int id;

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return getClass().getSimpleName();
    }

    public boolean verificar() {
        return true;
    }

    public abstract DadosPagamento clone();
}
