/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 *
 * @author Administrador
 */
public class FormatadorNumeros {

    public static String formatarDoubleToString(Double valor, String formatadorPadrao) {
        DecimalFormat decimal = new DecimalFormat(formatadorPadrao);
        String valorFormatado = decimal.format(valor);

        return valorFormatado;
    }

    public static BigDecimal casasDecimais(int casas, BigDecimal valor) {
        String quantCasas = "%." + casas + "f", textoValor = "0";
        try {
            textoValor = String.format(Locale.getDefault(), quantCasas, valor);
        } catch (java.lang.IllegalArgumentException e) {
            // Quando os digitos com 2 casas decimais forem Zeros, exemplo: 0.000001233888.
            // Nao existe valor 0,00 , logo entra na java.lang.IllegalArgumentException.
            if (e.getMessage().equals("Digits < 0")) {
                textoValor = "0";
            }
            System.out.println(e.getMessage());
        }
        return new BigDecimal(textoValor.replace(",", "."));
    }
}
