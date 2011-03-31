/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.financeiro.arquivoRetorno;

/**
 *
 * @author Administrador
 */

import java.io.Serializable;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author cifani
 */
public class RegistroTransacao implements Serializable {

  private Moeda valor;
  private Moeda valorTitulo;
  private DateTime data;
  private int numeroPrestacao;
  private String codigo;
  private Moeda juros;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public DateTime getData() {
        return data;
    }

    public void setData(DateTime data) {
        this.data = data;
    }

    public int getNumeroPrestacao() {
        return numeroPrestacao;
    }

    public void setNumeroPrestacao(int numeroPrestacao) {
        this.numeroPrestacao = numeroPrestacao;
    }

    public Moeda getValor() {
        return valor;
    }

    public void setValor(Moeda valor) {
        this.valor = valor;
    }

    public Moeda getValorTitulo() {
        return valorTitulo;
    }

    public void setValorTitulo(Moeda valorTitulo) {
        this.valorTitulo = valorTitulo;
    }

    public Moeda getJuros() {
        return juros;
    }

    public void setJuros(Moeda juros) {
        this.juros = juros;
    }

    @Override
    public String toString(){

    return valor +" - " + valorTitulo +" - " + data +" - " + juros +" - " + codigo;

    }
}