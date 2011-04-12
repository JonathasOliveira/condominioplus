/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Condominio;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;

/**
 *
 * @author eugenia
 */
@Entity
@NamedQueries(value={
    @NamedQuery(name="ExtratosPorDia", query="SELECT e FROM ExtratoBancario e WHERE e.condominio = ?1 and e.dataPagamento= ?2"),
    @NamedQuery(name="ExtratosPorMês", query="SELECT e FROM ExtratoBancario e WHERE e.condominio = ?1 and e.dataPagamento >= ?2 and e.dataPagamento <= ?3 ORDER BY e.dataPagamento"),
    @NamedQuery(name="ExtratosPorIdentificador", query="SELECT e FROM ExtratoBancario e WHERE e.identificador = ?1")
})
@Table(name = "extrato_bancario")
public class ExtratoBancario implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @Column(name="data_pagamento")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataPagamento;
    private String doc;
    @ManyToOne
    private Identificador identificador;
    @Column(precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);
    @ManyToOne
    private Condominio condominio;
    private String tipo;
    private int natureza;

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

    public Identificador getIdentificador() {
        return identificador;
    }

    public void setIdentificador(Identificador identificador) {
        this.identificador = identificador;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public Moeda getValor() {
        return new Moeda(valor);
    }

    public void setValor(Moeda valor) {
        this.valor = valor.bigDecimalValue();
    }

    public Calendar getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Calendar dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getNatureza() {
        return natureza;
    }

    public void setNatureza(int natureza) {
        this.natureza = natureza;
    }

    @Override
    public String toString() {
        return DataUtil.toString(dataPagamento) + " " + getValor() + " " + tipo + " " + identificador.getCodigoHistorico() + " " + identificador.getPalavraChave() + " " + doc;
    }

    
}