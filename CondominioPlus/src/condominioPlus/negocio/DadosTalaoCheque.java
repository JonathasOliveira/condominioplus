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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries(
@NamedQuery(name = "TaloesPorCondominio", query = "SELECT d FROM DadosTalaoCheque d WHERE d.condominio.codigo= ?1"))
@Table(name = "dados_talao_cheque")
public class DadosTalaoCheque implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int codigo;
    @Column(name = "numero_inicial")
    private String numeroInicial;
    @Column(name = "numero_final")
    private String numeroFinal;
    @Column(name = "em_uso")
    private boolean emUso;
    private boolean novo;
    private boolean usado;
    @ManyToOne
    private Condominio condominio;

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

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }

    public boolean isEmUso() {
        return emUso;
    }

    public void setEmUso(boolean emUso) {
        this.emUso = emUso;
    }

    public boolean isNovo() {
        return novo;
    }

    public void setNovo(boolean novo) {
        this.novo = novo;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }

    public String getStatus() {
        if (isEmUso()) {
            return "Em Uso";
        } else if (isNovo()) {
            return "Novo";
        } else if (isUsado()) {
            return "Usado";
        }
        return "";
    }

    public boolean verificarIntervaloCheque(String valor) {
        long novoValor = 0;
        long novoNumeroInicial = 0;
        long novoNumeroFinal = 0;
        if (valor != null) {
            novoValor = Long.parseLong(valor);
        }
        if (numeroInicial != null) {
            novoNumeroInicial = Long.parseLong(condominio.getContaBancaria().getContaCorrente() + numeroInicial);
        }
        if (numeroFinal != null) {
            novoNumeroFinal = Long.parseLong(condominio.getContaBancaria().getContaCorrente() + numeroFinal);
        }
        System.out.println(novoValor);
        System.out.println(novoNumeroInicial);
        System.out.println(novoNumeroFinal);
        if (novoValor >= novoNumeroInicial && novoValor <= novoNumeroFinal) {
            return true;
        }
        return false;


    }

    public boolean verificarIntervaloChequeSemContaCorrente(String valor) {
        long novoValor = 0;
        long novoNumeroInicial = 0;
        long novoNumeroFinal = 0;
        if (valor != null) {
            novoValor = Long.parseLong(valor);
        }
        if (numeroInicial != null) {
            novoNumeroInicial = Long.parseLong(numeroInicial);
        }
        if (numeroFinal != null) {
            novoNumeroFinal = Long.parseLong(numeroFinal);
        }
        if (novoValor >= novoNumeroInicial && novoValor <= novoNumeroFinal) {
            return true;
        }
        return false;


    }
}

