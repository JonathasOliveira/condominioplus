/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro.arquivoRetorno;

import condominioPlus.Main;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Identificador;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class EntradaExtratoDiario {

    private String linha = null;
    private ArrayList<ExtratoBancario> registros = new ArrayList<ExtratoBancario>();
    private List<Condominio> condominios;
    private List<ExtratoBancario> extratosSalvos;
    private List<ArquivoAtualizado> listaArquivos;
    private List<Identificador> identificadores;
    private ArquivoAtualizado arquivo;

    public EntradaExtratoDiario() {
        carregarConexao();
        FileDialog fileDialog = new FileDialog(TelaPrincipal.getInstancia());
        fileDialog.setVisible(true);
        if (fileDialog.getDirectory() != null) {
            File diretorio = new File(fileDialog.getDirectory());
            if (diretorio != null) {
                lerArquivo(diretorio.listFiles());
                setCondominio(registros);
            }
        } else {
            System.out.println("selecione um arquivo");
        }
    }

    public void lerArquivo(File arquivos[]) {
        ARQUIVOS:
        for (int i = 0; i < arquivos.length; i++) {
            BufferedReader leitor = null;
            if (arquivos[i].getName().charAt(0) == 'E') {
                arquivo = new ArquivoAtualizado();
                arquivo.setNome(arquivos[i].getName());
                if (verificarArquivosAtualizados(arquivo)) {
                    continue ARQUIVOS;
                }
                new DAO().salvar(arquivo);

            }

            try {
                leitor = pegarReader(arquivos[i]);
                while ((linha = leitor.readLine()) != null) {

                    if (linha.equalsIgnoreCase("")) {
                        break;
                    }
                    if (isTransacao(linha) && arquivos[i].getName().charAt(0) == 'E') {
//                    editarValor(linha);
                        if (obterRegistro(linha) != null) {
                            System.out.println("arquivo " + arquivos[i].getName());
                            registros.add(obterRegistro(linha));
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean verificarArquivosAtualizados(ArquivoAtualizado arquivo) {
        for (ArquivoAtualizado a : listaArquivos) {
            if (listaArquivos.isEmpty()) {
                return false;
            }
            if (a.getNome().equals(arquivo.getNome())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTransacao(String linha) {

        if (linha.charAt(0) == '1') {
            return true;
        } else {
            return false;
        }
    }

    private void setCondominio(List<ExtratoBancario> extratos) {
        for (ExtratoBancario ex : extratos) {
            Condominio c = localizarCondominio(ex.getContaCorrente());
            if (c != null) {
                ex.setCondominio(c);
                if (!extratosSalvos.isEmpty()) {
                    if (!extratosSalvos.contains(ex)) {
                        extratosSalvos.add(ex);
                        criarNovoPagamento(ex);
                        verificarPagamentoNoContasAPagar(ex);
                        verificarCheques(ex);
                        verificarPoupanca(ex);

                    }
                } else {
                    extratosSalvos.add(ex);
                    criarNovoPagamento(ex);
                    verificarPagamentoNoContasAPagar(ex);
                    verificarCheques(ex);
                    verificarPoupanca(ex);

                }
                //                verificarPoupanca(ex);
            }
        }

        new DAO().salvar(extratosSalvos);
    }

    private void verificarCheques(ExtratoBancario ex) {
        List<Pagamento> pagamentosEmAberto = new DAO().listar("PagamentosContaPagarPorPeriodo", ex.getCondominio().getContaPagar(), DataUtil.getCalendar(DataUtil.getPrimeiroDiaMes()), DataUtil.getCalendar(DataUtil.getUltimoDiaMes()));
        if (pagamentosEmAberto != null) {
            if (ex.getIdentificadorRegistro() == 1) {
                for (Pagamento p : pagamentosEmAberto) {
                    if (p.getForma() == FormaPagamento.CHEQUE) {
                        ex.setValor(ex.getValor().negate());
                        if (((DadosCheque) p.getDadosPagamento()).getNumero().equals(ex.getCondominio().getContaBancaria().getContaCorrente() + ex.getDoc())) {
                            p.setContaCorrente(ex.getCondominio().getContaCorrente());
                            p.setPago(true);
                            p.setDataPagamento(ex.getDataPagamento());
                            new DAO().salvar(p);
                        }

                    }
                }

            }
        }
    }

    private void verificarPagamentoNoContasAPagar(ExtratoBancario ex) {
        Identificador ident = localizaIdentificador(ex.getHistorico());
        if (ident != null && ex.getIdentificadorRegistro() == 1) {
            List<Pagamento> pagamentosEmAberto = new DAO().listar("PagamentosContaPagarPorPeriodo", ex.getCondominio().getContaPagar(), DataUtil.getCalendar(DataUtil.getPrimeiroDiaMes()), DataUtil.getCalendar(DataUtil.getUltimoDiaMes()));
            for (Pagamento p : pagamentosEmAberto) {
                ex.setValor(ex.getValor().negate());
                if (p.getConta().getCodigo() == ident.getConta().getCodigo() && p.getValor().doubleValue() == ex.getValor().doubleValue()) {
                    p.setContaCorrente(ex.getCondominio().getContaCorrente());
                    p.setPago(true);
                    p.setDataPagamento(ex.getDataPagamento());
                    new DAO().salvar(p);
                }

            }
        }
    }

    private void criarNovoPagamento(ExtratoBancario ex) {
        Identificador ident = localizaIdentificador(ex.getHistorico());
        if (ident != null && ex.getIdentificadorRegistro() == 1) {
            if (ident.getConta().getCodigo() == 12902) {
                Pagamento pagamento = new Pagamento();
                pagamento.setConta(ident.getConta());
                pagamento.setDataPagamento(ex.getDataPagamento());
                pagamento.setConciliacao(ex.getCondominio().getConciliacao());
                pagamento.setHistorico(ident.getPalavraChave());
                if (ident.getConta().isCredito()) {
                    pagamento.setValor(ex.getValor());
                } else {
                    pagamento.setValor(ex.getValor().negate());
                }

                pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
                new DAO().salvar(pagamento);
            } else {
                for (Identificador identificador : identificadores) {
                    if (identificador.getPalavraChave().equals(ex.getHistorico())) {
                        Pagamento pagamento = new Pagamento();
                        pagamento.setConta(identificador.getConta());
                        pagamento.setDataPagamento(ex.getDataPagamento());
                        pagamento.setConciliacao(ex.getCondominio().getConciliacao());
                        pagamento.setHistorico(identificador.getPalavraChave());
                        if (ident.getConta().isCredito()) {
                            pagamento.setValor(ex.getValor());
                        } else {
                            pagamento.setValor(ex.getValor().negate());
                        }

                        pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
                        new DAO().salvar(pagamento);

                    }
                }

            }
        }
    }

    private void verificarPoupanca(ExtratoBancario ex) {
        Identificador ident = localizaIdentificador(ex.getHistorico());
        if (ident != null && ex.getIdentificadorRegistro() == 1) {
            if (ident.getConta().getCodigo() == 6408) {
                Pagamento pagamento = new Pagamento();
                pagamento.setConta(ident.getConta());
                pagamento.setDataPagamento(ex.getDataPagamento());
                pagamento.setConciliacao(ex.getCondominio().getConciliacao());
                pagamento.setHistorico(ident.getPalavraChave());
                if (ident.getConta().isCredito()) {
                    pagamento.setValor(ex.getValor());
                } else {
                    pagamento.setValor(ex.getValor().negate());
                }

                pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
                new DAO().salvar(Main.getCondominio());
            }
        } else if (ident != null && ident.getConta().getCodigo() == 13072) {
            Pagamento pagamento = new Pagamento();
            pagamento.setConta(ident.getConta());
            pagamento.setDataPagamento(ex.getDataPagamento());
            pagamento.setConciliacao(ex.getCondominio().getConciliacao());
            pagamento.setHistorico(ident.getPalavraChave());
            if (ident.getConta().isCredito()) {
                pagamento.setValor(ex.getValor());
            } else {
                pagamento.setValor(ex.getValor().negate());
            }

            pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
            new DAO().salvar(Main.getCondominio());

        }
    }

    private Condominio localizarCondominio(String condominio) {

        for (Condominio c : condominios) {
            if (c.getContaBancaria().getContaCorrente().equals(condominio)) {
                return c;
            }
        }

        return null;

    }

    private Identificador localizaIdentificador(String ident) {

        for (Identificador i : identificadores) {
            if (i.getPalavraChave().equalsIgnoreCase(ident)) {
                return i;
            }
        }

        return null;

    }

    private void carregarConexao() {
        extratosSalvos = new DAO().listar(ExtratoBancario.class);
        condominios = new DAO().listar(Condominio.class);
        listaArquivos = new DAO().listar(ArquivoAtualizado.class);
        identificadores = new DAO().listar(Identificador.class);

    }

    private ExtratoBancario obterRegistro(String linha) {
        ExtratoBancario registro = new ExtratoBancario();

        DateTime data = new DateTime(2000 + Integer.parseInt(linha.substring(84, 86)), Integer.parseInt(linha.substring(82, 84)), Integer.parseInt(linha.substring(80, 82)), 0, 0, 0, 0);
        registro.setDataPagamento(DataUtil.getCalendar(data));
        registro.setTipo(linha.substring(104, 105));
        registro.setIdentificadorRegistro(Integer.valueOf(linha.substring(41, 42)));
        registro.setContaCorrente(linha.substring(21, 28));
        registro.setDoc(linha.substring(74, 80));
        if (registro.getIdentificadorRegistro() == 1) {
            registro.setHistorico(linha.substring(49, 74).trim());
        } else if (registro.getIdentificadorRegistro() == 0) {
            registro.setHistorico("SALDO INICIAL");
            registro.setDoc("000000");
        } else if (registro.getIdentificadorRegistro() == 2) {
            registro.setHistorico("SALDO FINAL");

        }

        if (registro.getIdentificadorRegistro() == 1 && !linha.substring(182, 183).equals(" ")) {
            registro.setNatureza(Integer.valueOf(linha.substring(182, 183)));
        }
        registro.setValor(new BigDecimal(editarValor(linha, 86, 104).doubleValue()));

        System.out.println(registro);
        return registro;
    }

    private Moeda editarValor(String linha, int indiceInicio, int indiceTermino) {
        String valor = linha.substring(indiceInicio, indiceTermino);

        double numero = (Double.parseDouble(valor)) / 100;

        return new Moeda(numero);

    }

    private BufferedReader pegarReader(File arquivo) throws FileNotFoundException {
        File file = arquivo;
        FileReader reader = new FileReader(file);
        return new BufferedReader(reader);
    }
}
