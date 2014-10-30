/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.financeiro.arquivoRetorno;

/**
 *
 * @author Administrador
 */

import condominioPlus.negocio.cobranca.Cobranca;
import java.io.Serializable;
import javax.persistence.OneToOne;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author cifani
 */
 public class RegistroTransacao implements Serializable {

  private Moeda valorPago;
  private Moeda valorTitulo;
  private DateTime data;
  private String documento;
  private Moeda juros;
  @OneToOne
  private Cobranca cobranca;

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public DateTime getData() {
        return data;
    }

    public void setData(DateTime data) {
        this.data = data;
    }

    public Moeda getValorPago() {
        return valorPago;
    }

    public void setValorPago(Moeda valorPago) {
        this.valorPago = valorPago;
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

    public Cobranca getCobranca() {
        return cobranca;
    }

    public void setCobranca(Cobranca cobranca) {
        this.cobranca = cobranca;
    }
    
    @Override
    public String toString(){

    return valorTitulo + " - " + valorPago + " - " + data + " - " + juros + " - " + documento + " - Cobrança";

    }
}