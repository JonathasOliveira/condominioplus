/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria implements Serializable {

    @Id
    @GeneratedValue
    private int codigo;
    @Column(name = "conta_corrente")
    private String contaCorrente = "";
    @Column(name = "digito_conta_corrente")
    private String digitoCorrente = "";
    @Column(name = "conta_poupanca")
    private String contaPoupanca = "";
    @Column(name = "digito_poupanca")
    private String digitoPoupanca = "";
    @Column(name="codigo_cedente")
    private String codigoCedente;
    @OneToOne(cascade = CascadeType.ALL)
    private Banco banco;
    @Column(name = "usuario_banking")
    private String usuarioBanking = "";
    @Column(name = "senha_banking")
    private String senhaBanking = "";
    @Column(name = "cpf_banking")
    private String cpfBanking = "";
    @Column(precision = 20, scale = 2)
    private BigDecimal valor = new BigDecimal(0);

    public ContaBancaria(Banco banco) {
        this.banco = banco;
    }

    public ContaBancaria() {
    }

    public String getContaCorrente() {
        return contaCorrente;
    }

    public void setContaCorrente(String contaCorrente) {
        this.contaCorrente = contaCorrente;
    }

    public String getContaPoupanca() {
        return contaPoupanca;
    }

    public void setContaPoupanca(String contaPoupanca) {
        this.contaPoupanca = contaPoupanca;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        if (banco == null) {
            return;
        } else {
            this.banco = banco;
        }
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getCpfBanking() {
        return cpfBanking;
    }

    public void setCpfBanking(String cpfBanking) {
        this.cpfBanking = cpfBanking;
    }

    public String getSenhaBanking() {
        return senhaBanking;
    }

    public void setSenhaBanking(String senhaBanking) {
        this.senhaBanking = senhaBanking;
    }

    public String getUsuarioBanking() {
        return usuarioBanking;
    }

    public void setUsuarioBanking(String usuarioBanking) {
        this.usuarioBanking = usuarioBanking;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDigitoCorrente() {
        return digitoCorrente;
    }

    public void setDigitoCorrente(String digitoCorrente) {
        this.digitoCorrente = digitoCorrente;
    }

    public String getDigitoPoupanca() {
        return digitoPoupanca;
    }

    public void setDigitoPoupanca(String digitoPoupanca) {
        this.digitoPoupanca = digitoPoupanca;
    }

    public String getCodigoCedente() {
        return codigoCedente;
    }

    public void setCodigoCedente(String codigoCedente) {
        this.codigoCedente = codigoCedente;
    }
    
}
