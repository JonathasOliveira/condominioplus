/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.Main;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

    private String nomeCedente;
    private String cnpjCedente;
    private String nomeSacado;
    private String logradouroSacado;
    private String numeroSacado;
    private String complementoSacado;
    private String bairroSacado;
    private String cidadeSacado;
    private String ufSacado;
    private String cepSacado;
    private String agencia;
    private String codigoCedente;
    private String numeroDocumento;
    private String dataDocumento;
    private String dataVencimento;
    private String tipoDocumento;
    private String aceite;
    private String carteira;
    private String especie;
    private String localPagamento;
    private String valor;
    private String codigoBanco;
    private String digitoBanco;
    private String linhaDigitavel;
    private String codigoBarras;
    private List<Pagamento> pagamentos;
    private String mensagem1;
    private String mensagem2;
    private String mensagem3;
    private String mensagem4;
    private String mensagem0;

    public String getAceite() {
        return aceite;
    }

    public void setAceite(String aceite) {
        this.aceite = aceite;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getCarteira() {
        return carteira;
    }

    public void setCarteira(String carteira) {
        this.carteira = carteira;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }

    public void setCodigoBanco(String codigoBanco) {
        this.codigoBanco = codigoBanco;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getCodigoCedente() {
        return codigoCedente;
    }

    public void setCodigoCedente(String codigoCedente) {
        this.codigoCedente = codigoCedente;
    }

    public String getDataDocumento() {
        return dataDocumento;
    }

    public void setDataDocumento(String dataDocumento) {
        this.dataDocumento = dataDocumento;
    }

    public String getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(String dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public String getDigitoBanco() {
        return digitoBanco;
    }

    public void setDigitoBanco(String digitoBanco) {
        this.digitoBanco = digitoBanco;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getLinhaDigitavel() {
        return linhaDigitavel;
    }

    public void setLinhaDigitavel(String linhaDigitavel) {
        this.linhaDigitavel = linhaDigitavel;
    }

    public String getLocalPagamento() {
        return localPagamento;
    }

    public void setLocalPagamento(String localPagamento) {
        this.localPagamento = localPagamento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getBairroSacado() {
        return bairroSacado;
    }

    public void setBairroSacado(String bairroSacado) {
        this.bairroSacado = bairroSacado;
    }

    public String getCepSacado() {
        return cepSacado;
    }

    public void setCepSacado(String cepSacado) {
        this.cepSacado = cepSacado;
    }

    public String getCidadeSacado() {
        return cidadeSacado;
    }

    public void setCidadeSacado(String cidadeSacado) {
        this.cidadeSacado = cidadeSacado;
    }

    public String getCnpjCedente() {
        return cnpjCedente;
    }

    public void setCnpjCedente(String cnpjCedente) {
        this.cnpjCedente = cnpjCedente;
    }

    public String getComplementoSacado() {
        return complementoSacado;
    }

    public void setComplementoSacado(String complementoSacado) {
        this.complementoSacado = complementoSacado;
    }

    public String getLogradouroSacado() {
        return logradouroSacado;
    }

    public void setLogradouroSacado(String logradouroSacado) {
        this.logradouroSacado = logradouroSacado;
    }

    public String getNomeCedente() {
        return nomeCedente;
    }

    public void setNomeCedente(String nomeCedente) {
        this.nomeCedente = nomeCedente;
    }

    public String getNomeSacado() {
        return nomeSacado;
    }

    public void setNomeSacado(String nomeSacado) {
        this.nomeSacado = nomeSacado;
    }

    public String getNumeroSacado() {
        return numeroSacado;
    }

    public void setNumeroSacado(String numeroSacado) {
        this.numeroSacado = numeroSacado;
    }

    public String getUfSacado() {
        return ufSacado;
    }

    public void setUfSacado(String ufSacado) {
        this.ufSacado = ufSacado;
    }

    public String getMensagem1() {
        return mensagem1;
    }

    public void setMensagem1(String mensagem1) {
        this.mensagem1 = mensagem1;
    }

    public String getMensagem2() {
        return mensagem2;
    }

    public void setMensagem2(String mensagem2) {
        this.mensagem2 = mensagem2;
    }

    public String getMensagem3() {
        return mensagem3;
    }

    public void setMensagem3(String mensagem3) {
        this.mensagem3 = mensagem3;
    }

    public String getMensagem4() {
        return mensagem4;
    }

    public void setMensagem4(String mensagem4) {
        this.mensagem4 = mensagem4;
    }

    public String getMensagem0() {
        return mensagem0;
    }

    public void setMensagem0(String mensagem0) {
        this.mensagem0 = mensagem0;
    }

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

    public static String calculoDvNossoNumeroBradesco(String campo) {
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
            if (multiplicador > 7) {
                multiplicador = 2;
//                System.out.println("primeiro if");

            }
        }
        int dac = (soma_campo % 11);
        String resultado = "";
//        System.out.println("dac " + dac);
        if (dac == 0) {
            resultado = "0";
        } else if (dac == 1) {
            resultado = "P";
        } else {
            resultado = "" + (11 - dac);
        }

        return resultado;
    }

    public static String gerarNumeroDocumento(Cobranca cobranca, DateTime data) {

        Condominio condominio = Main.getCondominio();

        String resultado;

        int incremento = Integer.parseInt(condominio.getIncrementoNumeroDocumento());
        String incr = "" + incremento;
        if (cobranca.getCodigoBanco().equals("033")) {
            while (incr.length() < 5) {
                incr = "0" + incr;
            }
        } else if (cobranca.getCodigoBanco().equals("237")) {
            while (incr.length() < 4) {
                incr = "0" + incr;
            }
        }

        System.out.println("incremento: " + incr);

        String mes = String.valueOf(data.getMonthOfYear());
        if (mes.length() == 1) {
            mes = "0" + mes;
        }

        resultado = condominio.getCodigo() + mes + incr;

        // verificar o banco antes de gerar o NumeroDocumento
        if (cobranca.getCodigoBanco().equals("033")) {
            while (resultado.length() < 12) {
                resultado = "0" + resultado;
            }
        } else if (cobranca.getCodigoBanco().equals("237")) {
            while (resultado.length() < 11) {
                resultado = "0" + resultado;
            }
        }

        if (incremento == 99999 || incremento == 9999) {
            if (cobranca.getCodigoBanco().equals("033")) {
                condominio.setIncrementoNumeroDocumento("00001");
            } else if (cobranca.getCodigoBanco().equals("237")) {
                condominio.setIncrementoNumeroDocumento("0001");
            }
        } else {
            incremento += 1;
            String incrementoAuxiliar = "" + incremento;
            if (cobranca.getCodigoBanco().equals("033")) {
                while (incrementoAuxiliar.length() < 5) {
                    incrementoAuxiliar = "0" + incrementoAuxiliar;
                }
            } else if (cobranca.getCodigoBanco().equals("237")) {
                while (incrementoAuxiliar.length() < 4) {
                    incrementoAuxiliar = "0" + incrementoAuxiliar;
                }
            }
            condominio.setIncrementoNumeroDocumento(incrementoAuxiliar);
        }
        new DAO().salvar(condominio);

        System.out.println("resultado" + resultado);

        return resultado;

    }

    public static String preencherNumeroConta(String campo) {
        while (campo.length() < 7) {
            campo = "0" + campo;
        }
        return campo;
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

    public static String getLinhaDigitavelSantander(Cobranca cobranca) {
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
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(cobranca.getNumeroConta() + cobranca.getDigitoConta())));
        contaBancaria.setCarteira(new Carteira(102));
        contaBancaria.setAgencia(new Agencia(Integer.parseInt(cobranca.getAgencia()), "0"));

        Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
        if (cobranca.getNumeroDocumento().length() == 13) {
            titulo.setNumeroDoDocumento(cobranca.getNumeroDocumento().substring(0, 13));
            titulo.setNossoNumero(cobranca.getNumeroDocumento().substring(0, 12));
            titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento().substring(0, 12)));
        } else if (cobranca.getNumeroDocumento().length() == 12) {
            titulo.setNumeroDoDocumento(cobranca.getNumeroDocumento());
            titulo.setNossoNumero(cobranca.getNumeroDocumento());
            titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento()));
        }
        titulo.setValor(cobranca.getValorTotal());
        titulo.setDataDoDocumento(DataUtil.getDate(DataUtil.hoje()));
        if (cobranca.getVencimentoProrrogado() != null) {
            titulo.setDataDoVencimento(DataUtil.getDate(cobranca.getVencimentoProrrogado()));
        } else {
            titulo.setDataDoVencimento(DataUtil.getDate(cobranca.getDataVencimento()));
        }
        titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
        titulo.setAceite(EnumAceite.A);
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

    public static String getLinhaDigitavelBradesco(Cobranca cobranca) {
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
        ContaBancaria contaBancaria = new ContaBancaria(BancoSuportado.BANCO_BRADESCO.create());
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(cobranca.getNumeroConta()), cobranca.getDigitoConta()));
        contaBancaria.setCarteira(new Carteira(6));
        contaBancaria.setAgencia(new Agencia(Integer.parseInt(cobranca.getAgencia()), "0"));

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

    public static BoletoBancario gerarBoleto(DadosCorrespondencia dados) {

        /*
         * INFORMANDO DADOS SOBRE O CEDENTE.
         */
        Cedente cedente = new Cedente(dados.getCobranca().getUnidade().getCondominio().getRazaoSocial(), BoletoBancario.retirarCaracteresCnpj(dados.getCobranca().getUnidade().getCondominio().getCnpj()));

        /*
         * INFORMANDO DADOS SOBRE O SACADO.
         */
        Sacado sacado = new Sacado(dados.getUnidade() + " " + dados.getNome());
        // Informando o endereço do sacado.
//        Endereco enderecoSac = new Endereco();
//
//        enderecoSac.setUF(BoletoBancario.getUnidadeFederativa(dados.getEstado()));
//        enderecoSac.setLocalidade(dados.getCidade());
//        enderecoSac.setCep(new CEP(dados.getCep()));
//        enderecoSac.setBairro(dados.getBairro());
//        enderecoSac.setLogradouro(dados.getLogradouro());
//        enderecoSac.setNumero(dados.getNumero() + " " + dados.getComplemento());
//
//        sacado.addEndereco(enderecoSac);


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
//                enderecoSacAval.setNumero("001");
//                sacadorAvalista.addEndereco(enderecoSacAval);


        /*
         * INFORMANDO OS DADOS SOBRE O BOLETO.
         */

        Boleto boletoAuxiliar = BoletoBancario.getBoletoAuxiliar(sacado, cedente, dados);

        System.out.println("campo livre " + boletoAuxiliar.getCampoLivre().write());
        System.out.println("linha digitavel " + boletoAuxiliar.getLinhaDigitavel().write());
        System.out.println("Codigo Barras " + boletoAuxiliar.getCodigoDeBarras().write());

        BoletoBancario boleto = new BoletoBancario();
        boleto.setNomeCedente(boletoAuxiliar.getTitulo().getCedente().getNome());
        boleto.setCnpjCedente(boletoAuxiliar.getTitulo().getCedente().getCPRF().getCodigoFormatado());
        boleto.setNomeSacado(boletoAuxiliar.getTitulo().getSacado().getNome());
        boleto.setLogradouroSacado(dados.getLogradouro());
        boleto.setNumeroSacado(dados.getNumero());
        boleto.setComplementoSacado(dados.getComplemento());
        boleto.setBairroSacado(dados.getBairro());
        boleto.setCidadeSacado(dados.getCidade());
        boleto.setUfSacado(dados.getEstado());
        boleto.setCepSacado(dados.getCep());

        boleto.setCodigoBanco(dados.getCobranca().getCodigoBanco());
        if (boleto.getCodigoBanco().equals("033")) {
            boleto.setAgencia("" + boletoAuxiliar.getTitulo().getContaBancaria().getAgencia().getCodigo());
            boleto.setDigitoBanco("7");
        } else if (boleto.getCodigoBanco().equals("237")) {
            boleto.setAgencia(boletoAuxiliar.getTitulo().getContaBancaria().getAgencia().getCodigo() + "-" + boletoAuxiliar.getTitulo().getContaBancaria().getAgencia().getDigitoVerificador());
            boleto.setDigitoBanco("2");
        }

        if (boletoAuxiliar.getTitulo().getContaBancaria().getNumeroDaConta().getDigitoDaConta() == null || boletoAuxiliar.getTitulo().getContaBancaria().getNumeroDaConta().getDigitoDaConta().equals("")) {
            boleto.setCodigoCedente(boletoAuxiliar.getTitulo().getContaBancaria().getNumeroDaConta().getCodigoDaConta().toString().substring(0, 6) + "-" + boletoAuxiliar.getTitulo().getContaBancaria().getNumeroDaConta().getCodigoDaConta().toString().substring(6, 7));
        } else {
            boleto.setCodigoCedente(BoletoBancario.preencherNumeroConta("" + boletoAuxiliar.getTitulo().getContaBancaria().getNumeroDaConta().getCodigoDaConta()) + "-" + boletoAuxiliar.getTitulo().getContaBancaria().getNumeroDaConta().getDigitoDaConta());
        }

        boleto.setNumeroDocumento(boletoAuxiliar.getTitulo().getNumeroDoDocumento());
        boleto.setDataDocumento(DataUtil.toString(boletoAuxiliar.getTitulo().getDataDoDocumento()));
        boleto.setDataVencimento(DataUtil.toString(boletoAuxiliar.getTitulo().getDataDoVencimento()));
        boleto.setTipoDocumento(boletoAuxiliar.getTitulo().getTipoDeDocumento().getSigla());
        boleto.setAceite(boletoAuxiliar.getTitulo().getAceite().name());
        boleto.setCarteira("" + boletoAuxiliar.getTitulo().getContaBancaria().getCarteira().getCodigo());
        boleto.setEspecie("R$");
        boleto.setLocalPagamento(boletoAuxiliar.getLocalPagamento());
        boleto.setValor(PagamentoUtil.formatarMoeda(boletoAuxiliar.getTitulo().getValor().doubleValue()));

        boleto.setLinhaDigitavel(boletoAuxiliar.getLinhaDigitavel().write());
        boleto.setCodigoBarras(boletoAuxiliar.getCodigoDeBarras().write());

        boleto.setPagamentos(dados.getCobranca().getPagamentos());

        Comparator c = null;
        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                MensagemBoleto p1 = (MensagemBoleto) o1;
                MensagemBoleto p2 = (MensagemBoleto) o2;
                return Integer.valueOf(p1.getCodigo()).compareTo(Integer.valueOf(p2.getCodigo()));
            }
        };
        List<MensagemBoleto> mensagens = dados.getCobranca().getUnidade().getCondominio().getMensagens();
        Collections.sort(mensagens, c);
        boleto.setMensagem1(mensagens.get(0).getMensagem());
        boleto.setMensagem2(mensagens.get(1).getMensagem());
        boleto.setMensagem3(mensagens.get(2).getMensagem());
        boleto.setMensagem4(mensagens.get(3).getMensagem());

        //se tiver desconto no condominio, mostrar essa mensagem
        if (dados.getCobranca().getDescontoAte() != null) {
            boleto.setMensagem0("ATÉ DIA " + DataUtil.toString(dados.getCobranca().getDescontoAte()) + ", CONCEDER 10% DE DESCONTO - " + PagamentoUtil.formatarMoeda(dados.getCobranca().getTotalComDesconto().doubleValue()));
        }

        return boleto;
    }

    public static Boleto getBoletoAuxiliar(Sacado sacado, Cedente cedente, DadosCorrespondencia dados) {
        /*
         * INFORMANDO OS DADOS SOBRE O TÍTULO.
         */
        if (dados.getCobranca().getCodigoBanco().equals("033")) {
            ContaBancaria contaBancaria = new ContaBancaria(BancoSuportado.BANCO_SANTANDER.create());
            contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(dados.getCobranca().getNumeroConta() + dados.getCobranca().getDigitoConta())));
            contaBancaria.setCarteira(new Carteira(102));
            contaBancaria.setAgencia(new Agencia(Integer.parseInt(dados.getCobranca().getAgencia()), "0"));
            Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
            titulo.setNumeroDoDocumento(dados.getCobranca().getNumeroDocumento().substring(0, 13));
            titulo.setNossoNumero(dados.getCobranca().getNumeroDocumento().substring(0, 12));
            titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroSantander(dados.getCobranca().getNumeroDocumento().substring(0, 12)));
            titulo.setValor(dados.getCobranca().getValorTotal());
            titulo.setDataDoDocumento(DataUtil.getDate(DataUtil.hoje()));
            if (dados.getCobranca().getVencimentoProrrogado() != null) {
                titulo.setDataDoVencimento(DataUtil.getDate(dados.getCobranca().getVencimentoProrrogado()));
            } else {
                titulo.setDataDoVencimento(DataUtil.getDate(dados.getCobranca().getDataVencimento()));
            }
            titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
            titulo.setAceite(EnumAceite.A);
            titulo.setDesconto(null);
            titulo.setDeducao(null);
            titulo.setMora(null);
            titulo.setAcrecimo(null);
            titulo.setValorCobrado(null);

            Boleto boleto = new Boleto(titulo);
            boleto.setLocalPagamento("ATÉ O VENCIMENTO PAGAR EM QUALQUER BANCO E/OU AGÊNCIA");

            return boleto;
        } else if (dados.getCobranca().getCodigoBanco().equals("237")) {
            ContaBancaria contaBancaria = new ContaBancaria(BancoSuportado.BANCO_BRADESCO.create());
            contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(dados.getCobranca().getNumeroConta()), dados.getCobranca().getDigitoConta()));
            contaBancaria.setCarteira(new Carteira(6));
            contaBancaria.setAgencia(new Agencia(Integer.parseInt(dados.getCobranca().getAgencia()), "0"));
            Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
            titulo.setNumeroDoDocumento(dados.getCobranca().getNumeroDocumento());
            titulo.setNossoNumero(dados.getCobranca().getNumeroDocumento());
            titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroBradesco(dados.getCobranca().getNumeroDocumento()));
            titulo.setValor(dados.getCobranca().getValorTotal());
            titulo.setDataDoDocumento(DataUtil.getDate(DataUtil.hoje()));
            if (dados.getCobranca().getVencimentoProrrogado() != null) {
                titulo.setDataDoVencimento(DataUtil.getDate(dados.getCobranca().getVencimentoProrrogado()));
            } else {
                titulo.setDataDoVencimento(DataUtil.getDate(dados.getCobranca().getDataVencimento()));
            }
            titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
            titulo.setAceite(EnumAceite.N);
            titulo.setDesconto(null);
            titulo.setDeducao(null);
            titulo.setMora(null);
            titulo.setAcrecimo(null);
            titulo.setValorCobrado(null);

            Boleto boleto = new Boleto(titulo);
            boleto.setLocalPagamento("Pagável Preferencialmente na rede Bradesco ou no Bradesco Expresso");

            return boleto;
        }

        return null;
    }
}
