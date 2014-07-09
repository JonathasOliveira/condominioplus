/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro.arquivoRetorno;

import condominioPlus.Main;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.Identificador;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import java.util.ArrayList;
import java.util.List;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class ExtratoDiarioUtil {

    private String linha = null;
    private ArrayList<ExtratoBancario> registros = new ArrayList<ExtratoBancario>();
    private List<Condominio> condominios;
    private List<ExtratoBancario> extratosSalvos;
    private List<ArquivoAtualizado> listaArquivos;
    private List<Identificador> identificadores;
    private ArquivoAtualizado arquivo;

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
                    PagamentoUtil.verificarListaVaziaContaCorrente(p);
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
                pagamento.setContaCorrente(ex.getCondominio().getContaCorrente());
                pagamento.setPago(true);
                pagamento.setHistorico(ident.getConta().getNome());
                if (ident.getConta().isCredito()) {
                    pagamento.setValor(ex.getValor());
                } else {
                    pagamento.setValor(ex.getValor().negate());
                }

                pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
                PagamentoUtil.verificarListaVaziaContaCorrente(pagamento);
                new DAO().salvar(pagamento);
            } else {
                for (Identificador identificador : identificadores) {
                    if (identificador.getPalavraChave().equals(ex.getHistorico())) {
                        Pagamento pagamento = new Pagamento();
                        pagamento.setConta(identificador.getConta());
                        pagamento.setDataPagamento(ex.getDataPagamento());
                        pagamento.setContaCorrente(ex.getCondominio().getContaCorrente());
                        pagamento.setPago(true);
                        pagamento.setHistorico(identificador.getConta().getNome());
                        if (ident.getConta().isCredito()) {
                            pagamento.setValor(ex.getValor());
                        } else {
                            pagamento.setValor(ex.getValor().negate());
                        }

                        pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
                        PagamentoUtil.verificarListaVaziaContaCorrente(pagamento);
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
                pagamento.setPoupanca(ex.getCondominio().getPoupanca());
                pagamento.setPago(true);
                pagamento.setHistorico(ident.getConta().getNome());
                if (ident.getConta().isCredito()) {
                    pagamento.setValor(ex.getValor());
                } else {
                    pagamento.setValor(ex.getValor().negate());
                }

                pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
                PagamentoUtil.verificarListaVaziaContaCorrente(pagamento);
                new DAO().salvar(Main.getCondominio());
            }
        } else if (ident != null && ident.getConta().getCodigo() == 13072) {
            Pagamento pagamento = new Pagamento();
            pagamento.setConta(ident.getConta());
            pagamento.setDataPagamento(ex.getDataPagamento());
            pagamento.setContaCorrente(ex.getCondominio().getContaCorrente());
            pagamento.setPago(true);
            pagamento.setHistorico(ident.getConta().getNome());
            if (ident.getConta().isCredito()) {
                pagamento.setValor(ex.getValor());
            } else {
                pagamento.setValor(ex.getValor().negate());
            }

            pagamento.setDadosPagamento(new DadosDOC(Pagamento.gerarNumeroDocumento()));
            PagamentoUtil.verificarListaVaziaContaCorrente(pagamento);
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
}
