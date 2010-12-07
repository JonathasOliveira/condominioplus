/*
 * DadosCheque.java
 *
 * Created on 09/10/2007, 13:52:28
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Banco;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import logicpoint.util.Util;

/**
 *
 * @author Thiago
 */
@Entity
@Table(name = "dados_cheque")
public class DadosCheque extends DadosPagamento {

    private String numero;
    private String conta;
    private String titular;
    private String agencia;
    private Banco banco;
    @Column(name = "codigo_verificacao")
    private String codigoVerificacao;

    public DadosCheque() {
    }

    public DadosCheque(String numero, String conta, String titular, String agencia, Banco banco) {
        this.numero = numero;
        this.conta = conta;
        this.titular = titular;
        this.agencia = agencia;
        this.banco = banco;
    }

    public DadosCheque(String numero, String conta, String titular, String agencia, Banco banco, String codigoVerificacao) {
        this.numero = numero;
        this.conta = conta;
        this.titular = titular;
        this.agencia = agencia;
        this.banco = banco;
        this.codigoVerificacao = codigoVerificacao;
    }

    @Override
    public boolean verificar() {
        return Util.verificar(numero) && Util.verificar(conta) &&
                Util.verificar(titular) && Util.verificar(agencia) &&
                banco != null && Util.verificar(codigoVerificacao);
    }

    @Override
    public DadosCheque clone() {
        return new DadosCheque(numero, conta, titular, agencia, banco, codigoVerificacao);
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public String getConta() {
        return conta;
    }

    public void setConta(String conta) {
        this.conta = conta;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getCodigoVerificacao() {
        return codigoVerificacao;
    }

    public void setCodigoVerificacao(String codigoVerificacao) {
        this.codigoVerificacao = codigoVerificacao;
    }
}
