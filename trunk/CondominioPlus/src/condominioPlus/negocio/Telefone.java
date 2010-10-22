/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

import condominioPlus.negocio.fornecedor.Fornecedor;
import java.io.Serializable;
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
public class Telefone implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int codigo;
    private String numero = "";
    private String tipo = "";
    @ManyToOne
    private Condominio condominio;
    @ManyToOne
    private Condomino condomino;
    @ManyToOne
    private Inquilino inquilino;
    @ManyToOne
    private Advogado advogado;
    @ManyToOne
    private Fornecedor fornecedor;

    public Telefone() {
    }


    public Telefone(Condominio condominio) {
        this.condominio = condominio;
    }

    public Telefone(Condomino condomino) {
        this.condomino = condomino;
    }

    public Telefone(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Telefone(Inquilino inquilino) {
        this.inquilino = inquilino;
    }

    public Telefone(Advogado advogado) {
        this.advogado = advogado;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Advogado getAdvogado() {
        return advogado;
    }

    public void setAdvogado(Advogado advogado) {
        this.advogado = advogado;
    }

    public Inquilino getInquilino() {
        return inquilino;
    }

    public void setInquilino(Inquilino inquilino) {
        this.inquilino = inquilino;
    }

    public Condomino getCondomino() {
        return condomino;
    }

    public void setCondomino(Condomino condomino) {
        this.condomino = condomino;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setId(int codigo) {
        this.codigo = codigo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }

    @Override
    public String toString() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
