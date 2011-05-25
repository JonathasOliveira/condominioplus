/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.NegocioUtil;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;
import org.jrimum.bopepo.BancoSuportado;
import org.jrimum.bopepo.Boleto;
import org.jrimum.domkee.comum.pessoa.endereco.Endereco;
import org.jrimum.domkee.comum.pessoa.endereco.UnidadeFederativa;
import org.jrimum.domkee.financeiro.banco.febraban.Agencia;
import org.jrimum.domkee.financeiro.banco.febraban.Carteira;
import org.jrimum.domkee.financeiro.banco.febraban.Cedente;
import org.jrimum.domkee.financeiro.banco.febraban.ContaBancaria;
import org.jrimum.domkee.financeiro.banco.febraban.NumeroDaConta;
import org.jrimum.domkee.financeiro.banco.febraban.Sacado;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeTitulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo.EnumAceite;

/**
 *
 * @author eugenia
 */
public class BoletoBancario {

    public static void mostreBoletoNaTela(File arquivoBoleto) {

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        try {
            desktop.open(arquivoBoleto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String calculoDvNossoNumeroSantander(String campo) {
        int multiplicador = 2;
        int multiplicacao = 0;
        int soma_campo = 0;
        for (int i = campo.length(); i > 0; i--) {
            multiplicacao = Integer.parseInt(campo.substring(i - 1, i)) * multiplicador;
//            System.out.println("numero " + Integer.parseInt(campo.substring(i - 1, i)));
//            System.out.println("multiplicação " + multiplicacao);
            soma_campo = soma_campo + multiplicacao;
//            System.out.println("soma campo" + soma_campo);
            multiplicador++;
            if (multiplicador > 9) {
                multiplicador = 2;
//                System.out.println("primeiro if");

            }
        }
        int dac = (soma_campo % 11);
//        System.out.println("dac " + dac);
        if (dac >= 10) {
            dac = 1;
        } else if ((dac == 0 || dac == 1)) {
            dac = 0;
        } else {
            dac = 11 - dac;
        }
//        System.out.println("fasdf" + ((Integer) dac).toString());
        return ((Integer) dac).toString();
    }

    public static String gerarNumeroDocumento(Condominio condominio, DateTime data) {

        String resultado;

        int incremento = NegocioUtil.getConfiguracao().getIncrementoNumeroDocumento();

        System.out.println("incremento: " + incremento);

        String mes = String.valueOf(data.getMonthOfYear());
        if (mes.length() == 1) {
            mes = "0" + mes;
        }

        resultado = condominio.getCodigo() + mes + incremento;

        while (resultado.length() < 12) {
            resultado = "0" + resultado;
        }

        if (incremento == 99999) {
            NegocioUtil.getConfiguracao().setIncrementoNumeroDocumento(10000);
        } else {
            incremento += 1;
            NegocioUtil.getConfiguracao().setIncrementoNumeroDocumento(incremento);
        }
        new DAO().salvar(NegocioUtil.getConfiguracao());

        System.out.println("resultado" + resultado);

        return resultado;

    }

    public static UnidadeFederativa getUnidadeFederativa(String uf) {
        if (uf.equals("AC")) {
            return UnidadeFederativa.AC;
        } else if (uf.equals("AL")) {
            return UnidadeFederativa.AL;
        } else if (uf.equals("AM")) {
            return UnidadeFederativa.AM;
        } else if (uf.equals("AP")) {
            return UnidadeFederativa.AP;
        } else if (uf.equals("BA")) {
            return UnidadeFederativa.BA;
        } else if (uf.equals("CE")) {
            return UnidadeFederativa.CE;
        } else if (uf.equals("DF")) {
            return UnidadeFederativa.DF;
        } else if (uf.equals("ES")) {
            return UnidadeFederativa.ES;
        } else if (uf.equals("GO")) {
            return UnidadeFederativa.GO;
        } else if (uf.equals("MA")) {
            return UnidadeFederativa.MA;
        } else if (uf.equals("MG")) {
            return UnidadeFederativa.MG;
        } else if (uf.equals("MS")) {
            return UnidadeFederativa.MS;
        } else if (uf.equals("MT")) {
            return UnidadeFederativa.MT;
        } else if (uf.equals("PA")) {
            return UnidadeFederativa.PA;
        } else if (uf.equals("PB")) {
            return UnidadeFederativa.PB;
        } else if (uf.equals("PE")) {
            return UnidadeFederativa.PE;
        } else if (uf.equals("PI")) {
            return UnidadeFederativa.PI;
        } else if (uf.equals("PR")) {
            return UnidadeFederativa.PR;
        } else if (uf.equals("RJ")) {
            return UnidadeFederativa.RJ;
        } else if (uf.equals("RN")) {
            return UnidadeFederativa.RN;
        } else if (uf.equals("RO")) {
            return UnidadeFederativa.RO;
        } else if (uf.equals("RR")) {
            return UnidadeFederativa.RR;
        } else if (uf.equals("RS")) {
            return UnidadeFederativa.RS;
        } else if (uf.equals("SC")) {
            return UnidadeFederativa.SC;
        } else if (uf.equals("SE")) {
            return UnidadeFederativa.SE;
        } else if (uf.equals("SP")) {
            return UnidadeFederativa.SP;
        } else if (uf.equals("TO")) {
            return UnidadeFederativa.TO;
        } else {
            return UnidadeFederativa.DESCONHECIDO;
        }
    }

    public static String getLinhaDigitavel(Cobranca cobranca) {
        Cedente cedente = new Cedente("");

        /*
         * INFORMANDO DADOS SOBRE O SACADO.
         */
        Sacado sacado = new Sacado("");

        // Informando o endereço do sacado.
        Endereco enderecoSac = new Endereco();

        sacado.addEndereco(enderecoSac);

        /*
         * INFORMANDO OS DADOS SOBRE O TÍTULO.
         */

        // Informando dados sobre a conta bancária do título.
        ContaBancaria contaBancaria = new ContaBancaria(BancoSuportado.BANCO_SANTANDER.create());
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(cobranca.getUnidade().getCondominio().getContaBancaria().getCodigoCedente())));
        contaBancaria.setCarteira(new Carteira(102));
        contaBancaria.setAgencia(new Agencia(3918, "0"));

        Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
        titulo.setNumeroDoDocumento(cobranca.getNumeroDocumento());
        titulo.setNossoNumero(cobranca.getNumeroDocumento());
        titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento()));
        titulo.setValor(cobranca.getValorTotal());
        titulo.setDataDoDocumento(DataUtil.getDate(DataUtil.hoje()));
        if (cobranca.getVencimentoProrrogado() != null) {
            titulo.setDataDoVencimento(DataUtil.getDate(cobranca.getVencimentoProrrogado()));
        } else {
             titulo.setDataDoVencimento(DataUtil.getDate(cobranca.getDataVencimento()));
        }
        titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
        titulo.setAceite(EnumAceite.N);
        titulo.setDesconto(null);
        titulo.setDeducao(null);
        titulo.setMora(null);
        titulo.setAcrecimo(null);
        titulo.setValorCobrado(null);

        /*
         * INFORMANDO OS DADOS SOBRE O BOLETO.
         */
        Boleto boleto = new Boleto(titulo);

        return boleto.getLinhaDigitavel().write();
    }

    public static String retirarCaracteresCnpj(String cnpj) {
        StringBuilder bob = new StringBuilder();

        TESTE:
        for (String string : cnpj.split("")) {
            if (string.equals(".") || string.equals("/") || string.equals("-")) {
                continue TESTE;
            } else {
                if (string.equals("")) {
                    continue TESTE;
                }
                bob.append(string);
                bob.append("");
            }
        }

        cnpj = bob.substring(0, bob.length() - 1);
// or
        cnpj = bob.toString().trim();

        return cnpj;
    }
}
