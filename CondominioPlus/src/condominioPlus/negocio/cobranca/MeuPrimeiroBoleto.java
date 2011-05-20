package condominioPlus.negocio.cobranca;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.jrimum.bopepo.BancoSuportado;
import org.jrimum.bopepo.Boleto;
import org.jrimum.bopepo.view.BoletoViewer;
import org.jrimum.domkee.comum.pessoa.endereco.CEP;
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
 * <p>
 * Exemplo de código para geração de um boleto simples.
 * </p>
 * <p>
 * Utiliza o Banco Bradesco como exemplo, já que possui um implementação simples.
 * </p>
 *
 * @author <a href="http://gilmatryx.googlepages.com/">Gilmar P.S.L</a>
 * @author <a href="mailto:misaelbarreto@gmail.com">Misael Barreto</a>
 * @author <a href="mailto:romulomail@gmail.com">Rômulo Augusto</a>
 *
 * @since 0.2
 *
 * @version 0.2
 */
public class MeuPrimeiroBoleto {

    public static void main(String[] args) {

        /*
         * INFORMANDO DADOS SOBRE O CEDENTE.
         */
        Cedente cedente = new Cedente("Búzios Green", "00.000.208/0001-00");

        /*
         * INFORMANDO DADOS SOBRE O SACADO.
         */
        Sacado sacado = new Sacado("01101 JOSE WALTER ZAMPIROLI");

        // Informando o endereço do sacado.
        Endereco enderecoSac = new Endereco();
        enderecoSac.setUF(UnidadeFederativa.RJ);
        enderecoSac.setLocalidade("NITEROI");
        enderecoSac.setCep(new CEP("24110-000"));
        enderecoSac.setBairro("BARRETO");
        enderecoSac.setLogradouro("RUA BENJAMIN CONSTANT,");
        enderecoSac.setNumero("313");
        sacado.addEndereco(enderecoSac);

        /*
         * INFORMANDO DADOS SOBRE O SACADOR AVALISTA.
         */
//                SacadorAvalista sacadorAvalista = new SacadorAvalista("JRimum Enterprise", "00.000.000/0001-91");
//
//                // Informando o endereço do sacador avalista.
//                Endereco enderecoSacAval = new Endereco();
//                enderecoSacAval.setUF(UnidadeFederativa.DF);
//                enderecoSacAval.setLocalidade("Brasília");
//                enderecoSacAval.setCep(new CEP("59000-000"));
//                enderecoSacAval.setBairro("Grande Centro");
//                enderecoSacAval.setLogradouro("Rua Eternamente Principal");
//                enderecoSacAval.setNumero("001");9
//                sacadorAvalista.addEndereco(enderecoSacAval);

        /*
         * INFORMANDO OS DADOS SOBRE O TÍTULO.
         */

        // Informando dados sobre a conta bancária do título.
        ContaBancaria contaBancaria = new ContaBancaria(BancoSuportado.BANCO_SANTANDER.create());
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt("3993698")));
        contaBancaria.setCarteira(new Carteira(102));
        contaBancaria.setAgencia(new Agencia(3918, "0"));

        Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
        titulo.setNumeroDoDocumento("243820576591");
        titulo.setNossoNumero("243820576591");
        titulo.setDigitoDoNossoNumero(calculoDvNossoNumeroSantander("243820576591"));
        titulo.setValor(BigDecimal.valueOf(725.70));
        titulo.setDataDoDocumento(new Date());
        titulo.setDataDoVencimento(new Date());
        titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
        titulo.setAceite(EnumAceite.N);
        titulo.setDesconto(BigDecimal.ZERO);
        titulo.setDeducao(BigDecimal.ZERO);
        titulo.setMora(BigDecimal.ZERO);
        titulo.setAcrecimo(BigDecimal.ZERO);
        titulo.setValorCobrado(BigDecimal.ZERO);

        /*
         * INFORMANDO OS DADOS SOBRE O BOLETO.
         */
        Boleto boleto = new Boleto(titulo);

                boleto.setLocalPagamento("Pagável preferencialmente na Rede X ou em " +
                                "qualquer Banco até o Vencimento.");
//                boleto.setInstrucaoAoSacado("Senhor sacado, sabemos sim que o valor " +
//                                "cobrado não é o esperado, aproveite o DESCONTÃO!");
//                boleto.setInstrucao1("PARA PAGAMENTO 1 até Hoje não cobrar nada!");
//                boleto.setInstrucao2("PARA PAGAMENTO 2 até Amanhã Não cobre!");
//                boleto.setInstrucao3("PARA PAGAMENTO 3 até Depois de amanhã, OK, não cobre.");
//                boleto.setInstrucao4("PARA PAGAMENTO 4 até 04/xx/xxxx de 4 dias atrás COBRAR O VALOR DE: R$ 01,00");
//                boleto.setInstrucao5("PARA PAGAMENTO 5 até 05/xx/xxxx COBRAR O VALOR DE: R$ 02,00");
//                boleto.setInstrucao6("PARA PAGAMENTO 6 até 06/xx/xxxx COBRAR O VALOR DE: R$ 03,00");
//                boleto.setInstrucao7("PARA PAGAMENTO 7 até xx/xx/xxxx COBRAR O VALOR QUE VOCÊ QUISER!");
//                boleto.setInstrucao8("APÓS o Vencimento, Pagável Somente na Rede X.");

        /*
         * GERANDO O BOLETO BANCÁRIO.
         */
        // Instanciando um objeto "BoletoViewer", classe responsável pela
        // geração do boleto bancário.
        BoletoViewer boletoViewer = new BoletoViewer(boleto);
//        boletoViewer.setTemplate("BoletoTemplateSemSacadorAvalista.pdf");

        // Gerando o arquivo. No caso o arquivo mencionado será salvo na mesma
        // pasta do projeto. Outros exemplos:
        // WINDOWS: boletoViewer.getAsPDF("C:/Temp/MeuBoleto.pdf");
        // LINUX: boletoViewer.getAsPDF("/home/temp/MeuBoleto.pdf");
        File arquivoPdf = boletoViewer.getPdfAsFile("MeuPrimeiroBoleto.pdf");

        System.out.println("campo livre " + boleto.getCampoLivre().write());
        System.out.println("linha digitavel " + boleto.getLinhaDigitavel().write());
        System.out.println("Codigo Barras " + boleto.getCodigoDeBarras().write());


        // Mostrando o boleto gerado na tela.
        mostreBoletoNaTela(arquivoPdf);
    }

    /**
     * Exibe o arquivo na tela.
     *
     * @param arquivoBoleto
     */
    private static void mostreBoletoNaTela(File arquivoBoleto) {

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
            System.out.println("numero " + Integer.parseInt(campo.substring(i-1, i)));
            System.out.println("multiplicação " + multiplicacao);
            soma_campo = soma_campo + multiplicacao;
            System.out.println("soma campo" + soma_campo);
            multiplicador++;
            if (multiplicador > 9) {
                multiplicador = 2;
                System.out.println("primeiro if");

            }
        }
        int dac = (soma_campo % 11);
        System.out.println("dac " + dac);
        if (dac >= 10) {
            dac = 1;
        } else if ((dac == 0 || dac == 1)) {
            dac = 0;
        } else {
            dac = 11 - dac;
        }
        System.out.println("fasdf" + ((Integer) dac).toString());
        return ((Integer) dac).toString();
    }
}
