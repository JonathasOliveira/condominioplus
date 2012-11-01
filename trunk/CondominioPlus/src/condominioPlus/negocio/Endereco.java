/*
 * Endereco.java
 *
 * Created on 26/07/2007, 09:39:01
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 *
 * @author USUARIO
 */
@Entity
public class Endereco implements Serializable {

    @Id
    @GeneratedValue
    private int id;
    private String logradouro = "";
    private String numero = "";
    private String complemento = "";
    private String bairro = "";
    private String cidade = "";
    private String cep = "";
    private String estado = "";
    private String referencia = "";
    private boolean padrao;
    @OneToOne
    private Condomino condomino;
    @ManyToOne
    private Inquilino inquilino;

    @Override
    public String toString() {
        return logradouro + " - " + numero;
    }
    public Endereco(Condomino condomino){
        this.condomino = condomino;
    }
    
    public Endereco(Inquilino inquilino){
        this.inquilino = inquilino;
    }

    public Endereco(){
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Endereco other = (Endereco) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.id;
        return hash;
    }

    @Override
    public Endereco clone() {
        Endereco endereco = new Endereco();
        endereco.setLogradouro(new String(logradouro));
        endereco.setNumero(new String(numero));
        endereco.setComplemento(new String(complemento));
        endereco.setBairro(new String(bairro));
        endereco.setCidade(new String(cidade));
        endereco.setCep(new String(cep));
        endereco.setEstado(new String(estado));
        endereco.setReferencia(new String(referencia));
        return endereco;
    }

    public int getId() {
        return id;
    }

    public Condomino getCondomino() {
        return condomino;
    }

    public void setCondomino(Condomino condomino) {
        this.condomino = condomino;
    }
    
    public Inquilino getInquilino() {
        return inquilino;
    }

    public void setInquilino(Inquilino inquilino) {
        this.inquilino = inquilino;
    }

    public boolean isPadrao() {
        return padrao;
    }

    public void setPadrao(boolean padrao) {
        this.padrao = padrao;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}