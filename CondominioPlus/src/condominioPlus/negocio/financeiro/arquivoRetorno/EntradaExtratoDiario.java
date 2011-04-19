/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro.arquivoRetorno;

import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.Identificador;
import condominioPlus.negocio.financeiro.Pagamento;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    private List<Pagamento> pagamentosEmAberto;
    private ArquivoAtualizado arquivo;

    public EntradaExtratoDiario() {
        carregarConexao();
        FileDialog fileDialog = new FileDialog(TelaPrincipal.getInstancia());
        fileDialog.setVisible(true);
        File diretorio = new File(fileDialog.getDirectory());
        lerArquivo(diretorio.listFiles());
        setCondominio(registros);
        lancarNoContaCorrente();
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
                verificarPagamento(ex);
            }
        }
    }

    private void verificarPagamento(ExtratoBancario e) {
        if (!extratosSalvos.isEmpty()) {
            for (ExtratoBancario ex : extratosSalvos) {
                if (!ex.equals(e)) {
                    new DAO().salvar(e);
                }
            }
        } else {
            new DAO().salvar(e);

        }
    }

    private void lancarNoContaCorrente() {
        Identificador identificador = null;
        DAO dao = new DAO();
        for (ExtratoBancario e : registros) {
            identificador = localizaIdentificador(e.getHistorico());
            if (identificador != null && e.getIdentificadorRegistro() == 1) {
                pagamentosEmAberto = dao.listar("PagamentosContaPagarPorPeriodo", localizarCondominio(e.getContaCorrente()).getContaPagar(), e.getDataPagamento(), e.getDataPagamento());
                System.out.println("identificador - " + identificador.getPalavraChave());
                Pagamento p = new Pagamento();
                p.setConta(identificador.getConta());
                p.setContaCorrente(localizarCondominio(e.getContaCorrente()).getContaCorrente());
                p.setDataPagamento(e.getDataPagamento());
                p.setPago(true);
                p.setHistorico(p.getConta().getNome());
                p.setValor(e.getValor().bigDecimalValue());
                if (!pagamentosEmAberto.isEmpty()) {
                    for (Pagamento pagamentoEmAberto : pagamentosEmAberto) {
                        if (pagamentoEmAberto.getValor() == p.getValor() && DataUtil.compararData(DataUtil.getDateTime(pagamentoEmAberto.getDataVencimento()), DataUtil.getDateTime(p.getDataPagamento())) == 0) {
                            pagamentoEmAberto.setPago(true);
                            pagamentoEmAberto.setDataPagamento(p.getDataPagamento());
                            pagamentoEmAberto.setContaCorrente(p.getContaCorrente());
                            System.out.println("Pagamento em aberto: " + DataUtil.toString(pagamentoEmAberto.getDataPagamento()) + " " + pagamentoEmAberto.getValor().toString() + " " + pagamentoEmAberto.getConta().getNome());
//                            dao.salvar(pagamentoEmAberto);
                        } else {
                            System.out.println("Novo Pagamento : " + DataUtil.toString(p.getDataPagamento()) + " " + p.getValor().toString() + " " + p.getConta().getNome());
//                            dao.salvar(p);
                        }
                    }
                } else {
                    System.out.println("Novo pagamento: " + DataUtil.toString(p.getDataPagamento()) + " " + p.getValor().toString() + " " + p.getConta().getNome());
//                    dao.salvar(p);
                }
            }
        }
    }

    private void verificarCheques() {
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
        registro.setValor(new Moeda(editarValor(linha, 86, 104)));

        System.out.println(registro);
        return registro;
    }

    private double editarValor(String linha, int indiceInicio, int indiceTermino) {
        String valor = linha.substring(indiceInicio, indiceTermino);
        double numero = (Double.parseDouble(valor)) / 100;

        return numero;
    }

    private BufferedReader pegarReader(File arquivo) throws FileNotFoundException {
        File file = arquivo;
        FileReader reader = new FileReader(file);
        return new BufferedReader(reader);
    }
}
