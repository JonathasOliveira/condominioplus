/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.negocio.Banco;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import logicpoint.util.Util;

/**
 *
 * @author USUARIO
 */
@Entity
@Table(name = "dados_doc")
public class DadosDOC extends DadosPagamento {

    @OneToOne
    private Banco banco;
    @Column(name = "nome_depositante")
    private String nomeDepositante;
    private String numero;
    private String agencia;

    public DadosDOC() {
    }

    public DadosDOC(Banco banco, String nomeDepositante, String numero, String agencia) {
        this.banco = banco;
        this.nomeDepositante = nomeDepositante;
        this.numero = numero;
        this.agencia = agencia;
    }

    @Override
    public boolean verificar() {
        return banco != null && Util.verificar(nomeDepositante) && Util.verificar(numero) && Util.verificar(agencia);
    }

    @Override
    public DadosPagamento clone() {
        return new DadosDOC(banco, nomeDepositante, numero, agencia);
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

    public String getNomeDepositante() {
        return nomeDepositante;
    }

    public void setNomeDepositante(String nomeDepositante) {
        this.nomeDepositante = nomeDepositante;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
