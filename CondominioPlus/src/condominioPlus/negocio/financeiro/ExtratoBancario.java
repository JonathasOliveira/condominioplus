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

/**
 *
 * @author eugenia
 */
@Entity
@NamedQueries(value={
    @NamedQuery(name="ExtratosPorDia", query="SELECT e FROM ExtratoBancario e WHERE e.condominio = ?1 and e.dataPagamento= ?2 ORDER BY e.codigo"),
    @NamedQuery(name="ExtratosPorMes", query="SELECT e FROM ExtratoBancario e WHERE e.condominio = ?1 and e.dataPagamento >= ?2 and e.dataPagamento <= ?3 ORDER BY e.codigo"),
    @NamedQuery(name="ExtratosPorCondominio", query="SELECT e FROM ExtratoBancario e WHERE e.condominio = ?1 ORDER BY e.codigo")
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
    @Column(precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);
    @ManyToOne
    private Condominio condominio;
    private String tipo;
    private int natureza;
    @Column(name="identificador_historico")
    private int identificadorRegistro;
    private String historico;
    @Column(name="conta_corrente")
    private String contaCorrente;

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

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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

    public int getIdentificadorRegistro() {
        return identificadorRegistro;
    }

    public void setIdentificadorRegistro(int identificadorRegistro) {
        this.identificadorRegistro = identificadorRegistro;
    }

    public String getHistorico() {
        return historico;
    }

    public void setHistorico(String historico) {
        this.historico = historico;
    }

    public String getContaCorrente() {
        return contaCorrente;
    }

    public void setContaCorrente(String contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

   
    @Override
    public String toString() {
        return DataUtil.toString(dataPagamento) + " " + getValor() + " " + tipo + " " + " " + doc + " " + identificadorRegistro + " " ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExtratoBancario other = (ExtratoBancario) obj;
        if (this.dataPagamento != other.dataPagamento && (this.dataPagamento == null || !this.dataPagamento.equals(other.dataPagamento))) {
            return false;
        }
        if ((this.doc == null) ? (other.doc != null) : !this.doc.equals(other.doc)) {
            return false;
        }
        if (this.condominio != other.condominio && (this.condominio == null || !this.condominio.equals(other.condominio))) {
            return false;
        }
        if ((this.tipo == null) ? (other.tipo != null) : !this.tipo.equals(other.tipo)) {
            return false;
        }
        if (this.identificadorRegistro != other.identificadorRegistro) {
            return false;
        }
        if ((this.historico == null) ? (other.historico != null) : !this.historico.equals(other.historico)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    
    
}
