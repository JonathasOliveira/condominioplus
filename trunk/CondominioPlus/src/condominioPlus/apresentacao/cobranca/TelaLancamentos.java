/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaLancamentos.java
 *
 * Created on 11/05/2011, 15:42:54
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.apresentacao.financeiro.DialogoConta;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.AcordoCobranca;
import condominioPlus.negocio.cobranca.BoletoBancario;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.CobrancaBase;
import condominioPlus.negocio.cobranca.DadosCorrespondencia;
import condominioPlus.negocio.cobranca.ItemCobranca;
import condominioPlus.negocio.cobranca.MensagemBoleto;
import condominioPlus.negocio.cobranca.agua.ContaAgua;
import condominioPlus.negocio.cobranca.agua.Rateio;
import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.TaxaExtra;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.DadosBoleto;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.FormaPagamentoEmprestimo;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.financeiro.arquivoRetorno.EntradaArquivoRetorno;
import condominioPlus.negocio.financeiro.arquivoRetorno.RegistroTransacao;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.util.Relatorios;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.CaretEvent;
import javax.swing.event.ChangeEvent;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.RenderizadorCelulaADireita;
import logicpoint.apresentacao.RenderizadorCelulaCentralizada;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.joda.time.DateTime;
import org.jrimum.bopepo.BancoSuportado;
import org.jrimum.bopepo.Boleto;
import org.jrimum.bopepo.view.BoletoViewer;
import org.jrimum.domkee.comum.pessoa.endereco.CEP;
import org.jrimum.domkee.comum.pessoa.endereco.Endereco;
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
public class TelaLancamentos extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TabelaModelo_2<Unidade> modeloTabelaCondominos;
    private List<Unidade> listaUnidades;
    private TabelaModelo_2<ItemCobranca> modeloTabelaItemCobranca;
    private List<ItemCobranca> listaItensCobranca;
    private TabelaModelo_2<ItemCobranca> modeloTabelaDadosAvulsos;
    private List<ItemCobranca> listaItensAvulsos = new ArrayList<ItemCobranca>();
    private TabelaModelo_2<Cobranca> modeloTabelaBoleto;
    private List<Cobranca> listaCobrancas;
    private TabelaModelo_2<Cobranca> modeloTabelaInadimplentes;
    private List<Cobranca> listaInadimplentes;
    private TabelaModelo_2<Cobranca> modeloTabelaPagos;
    private List<Cobranca> listaPagos;
    private TabelaModelo_2<RegistroTransacao> modeloTabelaArquivos;
    List<RegistroTransacao> listaRegistros = new ArrayList<RegistroTransacao>();
    private TabelaModelo_2<AcordoCobranca> modeloTabelaAcordo;
    private List<AcordoCobranca> listaAcordos;
    private TabelaModelo_2<Cobranca> modeloTabelaCobrancasOriginais;
    private TabelaModelo_2<Cobranca> modeloCobrancasGeradas;
    private Conta conta;

    /** Creates new form TelaLancamentos */
    public TelaLancamentos(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        new ControladorEventos();

        carregarTabelaCondominos();
        painelCobrancaBase.setVisible(false);
        carregarTabelaCobranca();
        carregarDateField();
        verificarMensagens();

        painelDetalheAcordo.setVisible(false);

        if (condominio != null) {
            this.setTitle("Cobranças - " + condominio.getRazaoSocial());
        }
    }

    private void carregarDateField() {
        txtDataInicial.setValue(DataUtil.toString(DataUtil.getPrimeiroDiaMes()));
        txtDataFinal.setValue(DataUtil.toString(DataUtil.getUltimoDiaMes()));
    }

    private void carregarTabelaCondominos() {

        modeloTabelaCondominos = new TabelaModelo_2<Unidade>(tabelaCondominos, "Unidade, Nome dos Condôminos".split(",")) {

            @Override
            protected List<Unidade> getCarregarObjetos() {
                return getUnidades();
            }

            @Override
            protected List<Unidade> getFiltrar(List<Unidade> unidades) {
                return filtrarListaCondominoPorUnidade(txtFiltro.getText(), unidades);
            }

            @Override
            public Object getValor(Unidade unidade, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return unidade.getUnidade();
                    case 1:
                        return unidade.getCondomino().getNome();
                    default:
                        return null;
                }
            }
        };

        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setMaxWidth(50);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());

    }

    private List<Unidade> getUnidades() {
        listaUnidades = condominio.getUnidades();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Unidade u1 = (Unidade) o1;
                Unidade u2 = (Unidade) o2;
                return u1.getUnidade().compareTo(u2.getUnidade());
            }
        };

        Collections.sort(listaUnidades, c);

        return listaUnidades;
    }

    private void carregarTabelaCobrancaBase() {
        modeloTabelaItemCobranca = new TabelaModelo_2<ItemCobranca>(tabelaCobrancasBase, "Conta, Descrição, Valor, Dividir?".split(",")) {

            @Override
            protected List<ItemCobranca> getCarregarObjetos() {
                return getItensCobrancas();
            }

            @Override
            public void setValor(ItemCobranca item, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 1:
                        item.setDescricao((String) valor);
                        break;
                }
            }

            @Override
            public Object getValor(ItemCobranca item, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return item.getCodigoConta();
                    case 1:
                        return item.getDescricao();
                    case 2:
                        return PagamentoUtil.formatarMoeda(item.getValor().doubleValue());
                    case 3:
                        return item.isDividirFracaoIdeal() ? "Sim" : "Não";
                    default:
                        return null;
                }
            }
        };

        tabelaCobrancasBase.getColumn(modeloTabelaItemCobranca.getCampo(0)).setMaxWidth(50);
        tabelaCobrancasBase.getColumn(modeloTabelaItemCobranca.getCampo(1)).setMinWidth(180);
        tabelaCobrancasBase.getColumn(modeloTabelaItemCobranca.getCampo(2)).setMaxWidth(70);
        tabelaCobrancasBase.getColumn(modeloTabelaItemCobranca.getCampo(3)).setMaxWidth(50);

        tabelaCobrancasBase.getColumn(modeloTabelaItemCobranca.getCampo(2)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasBase.getColumn(modeloTabelaItemCobranca.getCampo(3)).setCellRenderer(new RenderizadorCelulaCentralizada());

        modeloTabelaItemCobranca.setEditaveis(1);

    }

    private List<ItemCobranca> getItensCobrancas() {
        listaItensCobranca = new ArrayList<ItemCobranca>();

        ItemCobranca ultimoItem = new ItemCobranca();

        COBRANCABASE:
        for (CobrancaBase co : condominio.getCobrancasBase()) {
            // Se a conta for Tarifa Bancária, colocar na ultima posição da lista
            if (co.getConta().getCodigo() == 28103) {
                ultimoItem.setCodigoConta(co.getConta().getCodigo());
                ultimoItem.setCodigoObjeto(co.getCodigo());
                ultimoItem.setDescricao(co.getConta().getNome());
                ultimoItem.setDividirFracaoIdeal(co.isDividirFracaoIdeal());
                ultimoItem.setValor(co.getValor());
                continue COBRANCABASE;

            }
            ItemCobranca item = new ItemCobranca();
            item.setCodigoConta(co.getConta().getCodigo());
            item.setCodigoObjeto(co.getCodigo());
            item.setDescricao(co.getConta().getNome());
            item.setDividirFracaoIdeal(co.isDividirFracaoIdeal());
            item.setValor(co.getValor());
            listaItensCobranca.add(item);

        }

        for (ContaAgua c : condominio.getContasDeAgua()) {
            RateioTaxaExtra rateioAuxiliar = new RateioTaxaExtra();
            Conta co = new DAO().localizar(Conta.class, 11452);
            if (c.getDataVencimentoConta() != null && DataUtil.compararData(DataUtil.getDateTime(DataUtil.getCalendar(txtDataVencimento.getValue())), DataUtil.getDateTime(c.getDataVencimentoConta()).minusDays(5)) == 1 && DataUtil.compararData(DataUtil.getDateTime(DataUtil.getCalendar(txtDataVencimento.getValue())), DataUtil.getDateTime(c.getDataVencimentoConta()).plusDays(5)) == -1) {
                ItemCobranca item = new ItemCobranca();
                int codigoConta = 0;
                String descricao = "";
                int codigoObjeto = 0;
                boolean dividirFracaoIdeal = false;
                rateioAuxiliar.setValorACobrar(new BigDecimal(0));
                for (Rateio r : c.getRateios()) {
                    if (r.getValorTotalCobrar().doubleValue() > rateioAuxiliar.getValorACobrar().doubleValue()) {
                        codigoConta = co.getCodigo();
                        descricao = co.getNome();
                        codigoObjeto = r.getCodigo();
                        dividirFracaoIdeal = false;
                        rateioAuxiliar.setValorACobrar(r.getValorTotalCobrar());
                    }
                }
                if (rateioAuxiliar.getValorACobrar() != null && rateioAuxiliar.getValorACobrar().doubleValue() != 0) {
                    item.setCodigoConta(codigoConta);
                    item.setCodigoObjeto(codigoObjeto);
                    item.setDescricao(descricao);
                    item.setDividirFracaoIdeal(dividirFracaoIdeal);
                    item.setValor(rateioAuxiliar.getValorACobrar());
                    listaItensCobranca.add(item);
                }
            }
        }

        for (TaxaExtra txe : condominio.getTaxas()) {
            RateioTaxaExtra rateioAuxiliar = new RateioTaxaExtra();
            if (!txe.isTotalmentePaga() && txe.isCobrarComCondominio()) {
                ItemCobranca item = new ItemCobranca();
                int codigoConta = 0;
                String descricao = "";
                int codigoObjeto = 0;
                boolean dividirFracaoIdeal = false;
                for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
                    rateioAuxiliar.setValorACobrar(new BigDecimal(0));
                    for (RateioTaxaExtra rateio : parcela.getRateios()) {
                        if (rateio.getDataVencimento() != null && DataUtil.compararData(DataUtil.getDateTime(DataUtil.getCalendar(txtDataVencimento.getValue())), DataUtil.getDateTime(rateio.getDataVencimento()).minusDays(5)) == 1 && DataUtil.compararData(DataUtil.getDateTime(DataUtil.getCalendar(txtDataVencimento.getValue())), DataUtil.getDateTime(rateio.getDataVencimento()).plusDays(5)) == -1) {
                            if (rateio.getValorACobrar().doubleValue() > rateioAuxiliar.getValorACobrar().doubleValue()) {
                                codigoConta = txe.getConta().getCodigo();
                                descricao = "Parcela " + parcela.getNumeroParcela() + "/" + txe.getParcelas().size() + " da Taxa Extra " + txe.getDescricao();
                                codigoObjeto = rateio.getCodigo();
                                dividirFracaoIdeal = txe.isDividirFracaoIdeal();
                                rateioAuxiliar.setValorACobrar(rateio.getValorACobrar());
                            }
                        }
                    }
                }
                if (rateioAuxiliar.getValorACobrar() != null && rateioAuxiliar.getValorACobrar().doubleValue() != 0) {
                    item.setCodigoConta(codigoConta);
                    item.setCodigoObjeto(codigoObjeto);
                    item.setDescricao(descricao);
                    item.setDividirFracaoIdeal(dividirFracaoIdeal);
                    item.setValor(rateioAuxiliar.getValorACobrar());
                    listaItensCobranca.add(item);
                }
            }
        }

        if (ultimoItem.getValor().doubleValue() != 0) {
            listaItensCobranca.add(ultimoItem);
        }

        return listaItensCobranca;
    }

    private void carregarTabelaDadosAvulsos() {
        modeloTabelaDadosAvulsos = new TabelaModelo_2<ItemCobranca>(tabelaDadosAvulsos, "Conta, Descrição, Valor, Dividir por Fração Ideal?".split(",")) {

            @Override
            protected List<ItemCobranca> getCarregarObjetos() {
                return getItensCobrancasAvulsas();
            }

            @Override
            public void setValor(ItemCobranca item, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 1:
                        item.setDescricao((String) valor);
                        break;
                    case 2:
                        item.setValor(((Moeda) valor).bigDecimalValue());
                        break;
                    case 3:
                        item.setDividirFracaoIdeal((Boolean) valor);
                }
            }

            @Override
            public Object getValor(ItemCobranca item, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return item.getCodigoConta();
                    case 1:
                        return item.getDescricao();
                    case 2:
                        return new Moeda(item.getValor());
                    case 3:
                        return item.isDividirFracaoIdeal();
                    default:
                        return null;
                }
            }
        };

        tabelaDadosAvulsos.getColumn(modeloTabelaDadosAvulsos.getCampo(0)).setMaxWidth(50);
        tabelaDadosAvulsos.getColumn(modeloTabelaDadosAvulsos.getCampo(1)).setMinWidth(180);
        tabelaDadosAvulsos.getColumn(modeloTabelaDadosAvulsos.getCampo(2)).setMaxWidth(70);

        tabelaDadosAvulsos.getColumn(modeloTabelaDadosAvulsos.getCampo(2)).setCellRenderer(new RenderizadorCelulaADireita());

        modeloTabelaDadosAvulsos.setEditaveis(1, 2, 3);

    }

    private List<ItemCobranca> getItensCobrancasAvulsas() {
        return listaItensAvulsos;
    }

    private void carregarTabelaCobranca() {
        modeloTabelaBoleto = new TabelaModelo_2<Cobranca>(tabelaCobrancas, "Unidade, Condominio, Vencimento, Documento, Valor Original, Juros, Multa, Total, Linha Digitável, Acordo ".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getCobrancas();
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getRazaoSocial();
                    case 2:
                        return DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return cobranca.getNumeroDocumento();
                    case 4:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 8:
                        return cobranca.getLinhaDigitavel();
                    case 9:
                        return cobranca.getAcordo() != null ? cobranca.getAcordo().getCodigo() : "";
                    default:
                        return null;
                }
            }
        };

        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(3)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(4)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(5)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(6)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(7)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(8)).setCellRenderer(new RenderizadorCelulaCentralizada());
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(9)).setCellRenderer(new RenderizadorCelulaADireita());

        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(0)).setMaxWidth(50);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(1)).setMinWidth(250);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(4)).setMinWidth(80);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(8)).setMinWidth(265);

        tabelaCobrancas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getCobrancas() {
        listaCobrancas = new ArrayList<Cobranca>();
        for (Unidade u : condominio.getUnidades()) {
            for (Cobranca c : u.getCobrancas()) {
                if (modeloTabelaCondominos.getObjetoSelecionado() == null) {
                    if (c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) < 1) {
                        listaCobrancas.add(c);
                    }
                } else {
                    if (c.getUnidade().getCodigo() == modeloTabelaCondominos.getObjetoSelecionado().getCodigo() && c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) < 1) {
                        listaCobrancas.add(c);
                    }
                }
            }
        }

        Comparator c1 = null;

        c1 = new Comparator() {

            public int compare(Object o1, Object o2) {
                Cobranca e1 = (Cobranca) o1;
                Cobranca e2 = (Cobranca) o2;
                return e1.getDataVencimento().compareTo(e2.getDataVencimento());
            }
        };

        Collections.sort(listaCobrancas, c1);

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Cobranca co1 = (Cobranca) o1;
                Cobranca co2 = (Cobranca) o2;
                return co1.getUnidade().getUnidade().compareTo(co2.getUnidade().getUnidade());
            }
        };

        Collections.sort(listaCobrancas, c);

        return listaCobrancas;
    }

    private void carregarTabelaInadimplentes() {
        modeloTabelaInadimplentes = new TabelaModelo_2<Cobranca>(tabelaInadimplentes, "Unidade, Condominio, Vencimento, V. Prorrogado, Documento, Valor Original, Juros, Multa, Total, Linha Digitável, Acordo ".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getInadimplentes();
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getRazaoSocial();
                    case 2:
                        return DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return cobranca.getVencimentoProrrogado() != null ? DataUtil.toString(DataUtil.getDate(cobranca.getVencimentoProrrogado())) : " ";
                    case 4:
                        return cobranca.getNumeroDocumento();
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 8:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 9:
                        return cobranca.getLinhaDigitavel();
                    case 10:
                        return cobranca.getAcordo() != null ? cobranca.getAcordo().getCodigo() : "";
                    default:
                        return null;
                }
            }
        };

        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(3)).setCellRenderer(new RenderizadorCelulaCentralizada());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(4)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(5)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(6)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(7)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(8)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(9)).setCellRenderer(new RenderizadorCelulaCentralizada());
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(10)).setCellRenderer(new RenderizadorCelulaADireita());

        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(0)).setMaxWidth(50);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(1)).setMinWidth(250);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(3)).setMinWidth(85);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(5)).setMinWidth(80);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(9)).setMinWidth(265);

        tabelaInadimplentes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getInadimplentes() {
        listaInadimplentes = new ArrayList<Cobranca>();
        for (Unidade u : condominio.getUnidades()) {
            for (Cobranca c : u.getCobrancas()) {
                if (modeloTabelaCondominos.getObjetoSelecionado() == null) {
                    if (c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) >= 1 && c.isExibir()) {
                        listaInadimplentes.add(c);
                    }
                } else {
                    if (c.getUnidade().getCodigo() == modeloTabelaCondominos.getObjetoSelecionado().getCodigo() && c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) >= 1 && c.isExibir()) {
                        listaInadimplentes.add(c);
                    }
                }
            }
        }
        return listaInadimplentes;
    }

    private void carregarTabelaPagos() {
        modeloTabelaPagos = new TabelaModelo_2<Cobranca>(tabelaPagos, "Unidade, Condominio, Documento, Vencimento, Pagamento, Valor Original, Juros, Multa, Total, Desconto, Valor Pago, Diferença".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getPagos(DataUtil.getDateTime(txtDataInicial.getValue()), DataUtil.getDateTime(txtDataFinal.getValue()));
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getRazaoSocial();
                    case 2:
                        return cobranca.getNumeroDocumento();
                    case 3:
                        return cobranca.getVencimentoProrrogado() != null ? DataUtil.getDateTime(cobranca.getVencimentoProrrogado()) : DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 4:
                        return DataUtil.getDateTime(cobranca.getDataPagamento());
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 8:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 9:
                        return PagamentoUtil.formatarMoeda(cobranca.getDesconto().doubleValue());
                    case 10:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorPago().doubleValue());
                    case 11:
                        return PagamentoUtil.formatarMoeda(cobranca.getDiferencaPagamento().negate().doubleValue());
                    default:
                        return null;
                }
            }
        };

        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(2)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(5)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(6)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(7)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(8)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(9)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(10)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(11)).setCellRenderer(new RenderizadorCelulaADireita());

        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(0)).setMaxWidth(50);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(1)).setMinWidth(250);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(4)).setMinWidth(80);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(5)).setMinWidth(80);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(9)).setMinWidth(80);

        tabelaPagos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getPagos(DateTime dataInicial, DateTime dataFinal) {
        listaPagos = new ArrayList<Cobranca>();

        if (modeloTabelaCondominos.getObjetoSelecionado() == null) {
            listaPagos = new DAO().listar("CobrancasPagasPorPeriodo", condominio, DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
        } else {
            listaPagos = new DAO().listar("CobrancasPagasPorPeriodoUnidade", modeloTabelaCondominos.getObjetoSelecionado(), DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
        }

        return listaPagos;
    }

    private void carregarTabelaArquivos() {
        modeloTabelaArquivos = new TabelaModelo_2<RegistroTransacao>(tabelaArquivoRetorno, "Unidade, Condominio, Nº Documento, Pagamento, Valor Original, Valor Pago".split(",")) {

            @Override
            protected List<RegistroTransacao> getCarregarObjetos() {
                return listaRegistros;
            }

            @Override
            public Object getValor(RegistroTransacao registro, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return registro.getCobranca() != null ? registro.getCobranca().getUnidade().getUnidade() : "";
                    case 1:
                        return registro.getCobranca() != null ? registro.getCobranca().getUnidade().getCondominio().getRazaoSocial() : "";
                    case 2:
                        return registro.getDocumento();
                    case 3:
                        return registro.getData() != null ? DataUtil.getDateTime(registro.getData()) : "";
                    case 4:
                        return PagamentoUtil.formatarMoeda(registro.getValorTitulo().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(registro.getValorPago().doubleValue());
                    default:
                        return null;
                }
            }
        };

        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(2)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(4)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(5)).setCellRenderer(new RenderizadorCelulaADireita());

        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(0)).setMaxWidth(50);
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(1)).setMinWidth(250);
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(2)).setMinWidth(100);
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(3)).setMinWidth(80);
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(4)).setMinWidth(80);
        tabelaArquivoRetorno.getColumn(modeloTabelaArquivos.getCampo(5)).setMinWidth(80);

        tabelaArquivoRetorno.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private void gerarCobrancas(List<Unidade> lista) {
        if (verificarData(DataUtil.getDateTime(txtDataVencimento.getValue()))) {
            if (lista.size() > 1 && !txtNumeroDocumento.getText().equals("")) {
                ApresentacaoUtil.exibirAdvertencia("Selecione apenas um condômino.", this);
                return;
            }
            if (!txtNumeroDocumento.getText().equals("") && txtNumeroDocumento.getText().length() != 12) {
                ApresentacaoUtil.exibirAdvertencia("O número documento informado está incorreto.", this);
                return;
            }

            UNIDADES:
            for (Unidade u : lista) {
                Cobranca cobranca = new Cobranca();
                cobranca.setUnidade(u);
                cobranca.setValorTotal(new BigDecimal(0));
                cobranca.setValorOriginal(new BigDecimal(0));

                if (painelCobrancaBase.getSelectedIndex() == 0) {
                    cobranca.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
                } else if (painelCobrancaBase.getSelectedIndex() == 1) {
                    cobranca.setDataVencimento(DataUtil.getCalendar(txtDataVencimentoAvulso.getValue()));
                }

                if (txtNumeroDocumento.getText().equals("")) {
                    if (painelCobrancaBase.getSelectedIndex() == 0) {
                        cobranca.setNumeroDocumento(BoletoBancario.gerarNumeroDocumento(condominio, DataUtil.getDateTime(txtDataVencimento.getValue())));
                    } else if (painelCobrancaBase.getSelectedIndex() == 1) {
                        cobranca.setNumeroDocumento(BoletoBancario.gerarNumeroDocumento(condominio, DataUtil.getDateTime(txtDataVencimentoAvulso.getValue())));
                    }
                } else {
                    cobranca.setNumeroDocumento(txtNumeroDocumento.getText());
                }

                if (u.isSindico() && !condominio.isSindicoPaga()) {
                    continue UNIDADES;
                }

                if (painelCobrancaBase.getSelectedIndex() == 0) {
                    calcularCobrancas(u, cobranca);
                } else if (painelCobrancaBase.getSelectedIndex() == 1) {
                    if (listaItensAvulsos.isEmpty()) {
                        ApresentacaoUtil.exibirAdvertencia("Antes de gerar a cobrança, preencha a lista.", this);
                        return;
                    }
                    if (!calcularCobrancasAvulsas(u, cobranca)) {
                        return;
                    }
                }

                u.getCobrancas().add(cobranca);
                cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavel(cobranca));
                cobranca.setNumeroDocumento(cobranca.getNumeroDocumento() + BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento()));
                new DAO().salvar(u);
            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma data a partir de hoje.", this);
            return;
        }
    }

    private void calcularCobrancas(Unidade u, Cobranca cobranca) {
        String texto = "";
        for (CobrancaBase co : condominio.getCobrancasBase()) {
            Pagamento pagamento = new Pagamento();
            pagamento.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
            pagamento.setCobranca(cobranca);
            pagamento.setConta(co.getConta());
            for (ItemCobranca item : listaItensCobranca) {
                if (co.getCodigo() == item.getCodigoObjeto()) {
                    texto = item.getDescricao();
                }
            }
            pagamento.setDescricao(texto);
            pagamento.setHistorico(texto + " " + cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());
            if (u.getValorPrincipal().doubleValue() != 0) {
                pagamento.setValor(u.getValorPrincipal());
            } else {
                if (co.isDividirFracaoIdeal()) {
                    pagamento.setValor(new BigDecimal(calcularPorFracaoIdeal(u, co.getValor())).setScale(2, RoundingMode.UP));
                } else {
                    pagamento.setValor(co.getValor());
                }
            }
            cobranca.getPagamentos().add(pagamento);
            cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
            cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
        }
        for (ContaAgua c : condominio.getContasDeAgua()) {
            if (c.getDataVencimentoConta() != null && DataUtil.compararData(DataUtil.getDateTime(cobranca.getDataVencimento()), DataUtil.getDateTime(c.getDataVencimentoConta()).minusDays(5)) == 1 && DataUtil.compararData(DataUtil.getDateTime(cobranca.getDataVencimento()), DataUtil.getDateTime(c.getDataVencimentoConta()).plusDays(5)) == -1) {
                for (Rateio r : c.getRateios()) {
                    if (r.getUnidade().getCodigo() == u.getCodigo()) {
                        Pagamento pagamento = new Pagamento();
                        pagamento.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
                        pagamento.setCobranca(cobranca);
                        pagamento.setConta(new DAO().localizar(Conta.class, 11452));
                        for (ItemCobranca item : listaItensCobranca) {
                            if (item.getCodigoConta() == 11452) {
                                texto = item.getDescricao();
                            }
                        }
                        pagamento.setHistorico(texto + " " + cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());
                        pagamento.setValor(r.getValorTotalCobrar());
                        cobranca.getPagamentos().add(pagamento);
                        cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
                        cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
                    }
                }
            }
        }
        for (TaxaExtra txe : condominio.getTaxas()) {
            if (!txe.isTotalmentePaga() && txe.isCobrarComCondominio()) {
                for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
                    for (RateioTaxaExtra rateio : parcela.getRateios()) {
                        if (rateio.getDataVencimento() != null && DataUtil.compararData(DataUtil.getDateTime(cobranca.getDataVencimento()), DataUtil.getDateTime(rateio.getDataVencimento()).minusDays(5)) == 1 && DataUtil.compararData(DataUtil.getDateTime(cobranca.getDataVencimento()), DataUtil.getDateTime(rateio.getDataVencimento()).plusDays(5)) == -1) {
                            if (rateio.getUnidade().getCodigo() == u.getCodigo()) {
                                rateio.setCobranca(cobranca);
                                Pagamento pagamento = new Pagamento();
                                pagamento.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
                                pagamento.setCobranca(cobranca);
                                pagamento.setConta(txe.getConta());
                                pagamento.setHistorico("Parcela " + parcela.getNumeroParcela() + "/" + txe.getParcelas().size() + " da " + pagamento.getConta().getNome() + " " + txe.getDescricao() + " " + cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());
                                pagamento.setValor(rateio.getValorACobrar());
                                cobranca.getPagamentos().add(pagamento);
                                cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
                                cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
                                new DAO().salvar(rateio);
                            }
                        }
                    }
                }
            }
        }
        verificarResiduoAnterior(cobranca);
    }

    private boolean calcularCobrancasAvulsas(Unidade u, Cobranca cobranca) {
        for (ItemCobranca item : listaItensAvulsos) {
            if (item.getValor().doubleValue() != 0) {
                Pagamento pagamento = new Pagamento();
                pagamento.setDataVencimento(DataUtil.getCalendar(txtDataVencimentoAvulso.getValue()));
                pagamento.setCobranca(cobranca);
                Conta c = new DAO().localizar(Conta.class, item.getCodigoConta());
                pagamento.setConta(c);
                pagamento.setDescricao(item.getDescricao());
                pagamento.setHistorico(item.getDescricao() + " " + cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());
                if (u.getValorPrincipal().doubleValue() != 0) {
                    pagamento.setValor(u.getValorPrincipal());
                } else {
                    if (item.isDividirFracaoIdeal()) {
                        pagamento.setValor(new BigDecimal(calcularPorFracaoIdeal(u, item.getValor())).setScale(2, RoundingMode.UP));
                    } else {
                        pagamento.setValor(item.getValor());
                    }
                }
                cobranca.getPagamentos().add(pagamento);
                cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
                cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
            } else {
                ApresentacaoUtil.exibirAdvertencia("Existe(m) item(s) que o valor é igual a 0. Edite-o (s) ou remova-o(s) da lista.", this);
                return false;
            }
        }
        return true;
    }

    private double calcularPorFracaoIdeal(Unidade u, BigDecimal valor) {
        double resultado = 0;
        resultado = (u.getFracaoIdeal() * valor.doubleValue()) / getMaiorFracaoIdeal().getFracaoIdeal();
        System.out.println("resultado - " + resultado);
        return resultado;
    }

//    private double getMaiorFracaoIdeal() {
//        double resultado = 0;
//        for (Unidade u : condominio.getUnidades()) {
//            if (u.getFracaoIdeal() > resultado) {
//                resultado = u.getFracaoIdeal();
//            }
//        }
//        return resultado;
//    }
    private Unidade getMaiorFracaoIdeal() {
        double resultado = 0;
        Unidade unidade = new Unidade();
        for (Unidade u : condominio.getUnidades()) {
            if (u.getFracaoIdeal() > resultado) {
                resultado = u.getFracaoIdeal();
                unidade = u;
            }
        }
        return unidade;
    }

    private boolean verificarData(DateTime data) {
        if (DataUtil.compararData(DataUtil.hoje(), data) == 1) {
            return false;
        }
        return true;
    }

    private void verificarResiduoAnterior(Cobranca c) {
        List<Cobranca> lista = getPagosPorUnidade(c.getUnidade());
        if (!lista.isEmpty()) {
            Cobranca ultimaCobrancaPaga = lista.get(lista.size() - 1);
            if (ultimaCobrancaPaga.getDiferencaPagamento().doubleValue() != 0) {
                Pagamento pagamento = new Pagamento();
                pagamento.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
                pagamento.setCobranca(c);
                if (ultimaCobrancaPaga.getDiferencaPagamento().doubleValue() < 0) {
                    pagamento.setConta(new DAO().localizar(Conta.class, 40502));
                } else if (ultimaCobrancaPaga.getDiferencaPagamento().doubleValue() > 0) {
                    pagamento.setConta(new DAO().localizar(Conta.class, 40503));
                }
                pagamento.setHistorico(pagamento.getConta().getNome() + " " + c.getUnidade().getUnidade() + " " + c.getUnidade().getCondomino().getNome());
                pagamento.setValor(ultimaCobrancaPaga.getDiferencaPagamento());
                c.getPagamentos().add(pagamento);
                c.setValorTotal(c.getValorTotal().add(pagamento.getValor()));
                c.setValorOriginal(c.getValorOriginal().add(pagamento.getValor()));
            }
        }
    }

    private List<Cobranca> getPagosPorUnidade(Unidade u) {
        DateTime dataInicial = DataUtil.getPrimeiroDiaMes().minusMonths(4);
        DateTime dataFinal = DataUtil.getUltimoDiaMes();
        System.out.println("Data Inicial: " + DataUtil.toString(dataInicial));
        System.out.println("Data Final: " + DataUtil.toString(dataFinal));
        List<Cobranca> lista = new DAO().listar("CobrancasPagasPorPeriodoUnidade", u, DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
        return lista;
    }

    private void remover() {
        if (!ApresentacaoUtil.perguntar("Desejar remover a(s) cobrança(s)?", this)) {
            return;
        }
        if (modeloTabelaBoleto.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabelaBoleto.getLinhasSelecionadas());
            List<Cobranca> itensRemover = modeloTabelaBoleto.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Cobranca c : itensRemover) {
                    modeloTabelaBoleto.remover(c);
                    for (Unidade u : condominio.getUnidades()) {
                        if (c.getUnidade().getCodigo() == u.getCodigo()) {
                            u.getCobrancas().remove(c);
                        }
                    }
                    new DAO().remover(c);
                }
            }
            ApresentacaoUtil.exibirInformacao("Cobrança(s) removida(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void calcularJurosMulta(Cobranca cobranca, DateTime dataProrrogada) {
        Moeda diferenca = new Moeda();
        Moeda juros = new Moeda();
        Moeda multa = new Moeda();
        cobranca.setValorTotal(new BigDecimal(0));
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getValorOriginal()));
        for (Pagamento pagamento : cobranca.getPagamentos()) {
            //codigo da conta tarifa bancaria
            if (pagamento.getConta().getCodigo() == 28103) {
                diferenca.soma(pagamento.getValor());
                cobranca.setValorTotal(cobranca.getValorTotal().subtract(pagamento.getValor()));
            }
        }
        double diferencaMeses = 0;
        diferencaMeses = DataUtil.getDiferencaEmMeses(dataProrrogada, DataUtil.getDateTime(cobranca.getDataVencimento()));
        if (diferencaMeses > 0) {
//            System.out.println("diferenca meses: " + new Double(diferencaMeses).intValue());
            if (diferencaMeses >= 0 && diferencaMeses <= 1) {
                diferencaMeses = 1;
            }
            juros.soma(new Double(diferencaMeses).intValue()).multiplica(NegocioUtil.getConfiguracao().getPercentualJuros().divide(new BigDecimal(100)));
//            System.out.println("juros: " + juros);
            juros.multiplica(cobranca.getValorTotal());
            multa.soma(NegocioUtil.getConfiguracao().getPercentualMulta().divide(new BigDecimal(100)));
            multa.multiplica(cobranca.getValorTotal());
            cobranca.setVencimentoProrrogado(DataUtil.getCalendar(dataProrrogada));
        }
        cobranca.setJuros(juros.bigDecimalValue().setScale(2, RoundingMode.UP));
        cobranca.setMulta(multa.bigDecimalValue().setScale(2, RoundingMode.UP));
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getJuros().add(cobranca.getMulta().add(diferenca.bigDecimalValue()))).setScale(2, RoundingMode.UP));
        cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavel(cobranca));
        if (jTabbedPane1.getSelectedIndex() == 1) {
            System.out.println("Dentro do if em calcularJurosMulta.");
            new DAO().salvar(cobranca);
            carregarTabelaInadimplentes();
        }
    }

    private void imprimirBoleto(List<Cobranca> listaCobrancas) {
        List<Boleto> boletos = new ArrayList<Boleto>();
        for (Cobranca cobranca : listaCobrancas) {

            if (DataUtil.compararData(DataUtil.getDateTime(cobranca.getDataVencimento()), DataUtil.getDateTime(txtVencimentoProrrogado.getValue())) == -1) {
                calcularJurosMulta(cobranca, DataUtil.getDateTime(DataUtil.getDateTime(txtVencimentoProrrogado.getValue())));
            }

            List<DadosCorrespondencia> listaDados = new ArrayList<DadosCorrespondencia>();

            for (Cobranca co : listaCobrancas) {
                boolean imprimirProprietario = false;
                boolean imprimirInquilino = false;
                if (co.getUnidade().isBoletoProprietario()) {
                    imprimirProprietario = true;
                    imprimirInquilino = false;
                } else if (co.getUnidade().isBoletoInquilino()) {
                    imprimirProprietario = false;
                    imprimirInquilino = true;
                } else if (co.getUnidade().isBoletoProprietarioInquilino()) {
                    imprimirProprietario = true;
                    imprimirInquilino = true;
                }
                listaDados = DadosCorrespondencia.preencherLista(co.getUnidade(), listaDados, imprimirProprietario, imprimirInquilino, co);
            }

            for (DadosCorrespondencia dados : listaDados) {
                Sacado sacado = getSacado(dados);
                gerarBoleto(boletos, dados.getCobranca(), sacado);
            }
        }

        /*
         * GERANDO O(S) BOLETO(S) BANCÁRIO(S).
         */

        File pdf = BoletoViewer.groupInOnePDF("MeuPrimeiroBoleto.pdf", boletos);
        BoletoBancario.mostreBoletoNaTela(pdf);
    }

    private Sacado getSacado(DadosCorrespondencia dados) {
        /*
         * INFORMANDO DADOS SOBRE O SACADO.
         */
        Sacado sacado = new Sacado(dados.getUnidade() + " " + dados.getNome());

        // Informando o endereço do sacado.
        Endereco enderecoSac = new Endereco();

        enderecoSac.setUF(BoletoBancario.getUnidadeFederativa(dados.getEstado()));
        enderecoSac.setLocalidade(dados.getCidade());
        enderecoSac.setCep(new CEP(dados.getCep()));
        enderecoSac.setBairro(dados.getBairro());
        enderecoSac.setLogradouro(dados.getLogradouro());
        enderecoSac.setNumero(dados.getNumero() + " " + dados.getComplemento());

        sacado.addEndereco(enderecoSac);

        return sacado;
    }

    private void gerarBoleto(List<Boleto> boletos, Cobranca cobranca, Sacado sacado) {
        /*
         * INFORMANDO DADOS SOBRE O CEDENTE.
         */
        Cedente cedente = new Cedente(cobranca.getUnidade().getCondominio().getRazaoSocial(), BoletoBancario.retirarCaracteresCnpj(cobranca.getUnidade().getCondominio().getCnpj()));

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
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(cobranca.getUnidade().getCondominio().getContaBancaria().getCodigoCedente())));
        contaBancaria.setCarteira(new Carteira(102));
        contaBancaria.setAgencia(new Agencia(Integer.parseInt(cobranca.getUnidade().getCondominio().getContaBancaria().getBanco().getAgencia()), "0"));

        Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
        titulo.setNumeroDoDocumento(cobranca.getNumeroDocumento().substring(0, 12));
        titulo.setNossoNumero(cobranca.getNumeroDocumento().substring(0, 12));
        titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento().substring(0, 12)));
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

        boleto.setLocalPagamento("Pagável preferencialmente na Rede X ou em "
                + "qualquer Banco até o Vencimento.");
//                boleto.setInstrucaoAoSacado("Senhor sacado, sabemos sim que o valor " +
//                                "cobrado não é o esperado, aproveite o DESCONTÃO!");
        boleto.setInstrucao1(condominio.getMensagens().get(0).getMensagem());
        boleto.setInstrucao2(condominio.getMensagens().get(1).getMensagem());
        boleto.setInstrucao3(condominio.getMensagens().get(2).getMensagem());
        boleto.setInstrucao4(condominio.getMensagens().get(3).getMensagem());
        boleto.setInstrucao5(condominio.getMensagens().get(4).getMensagem());
        boleto.setInstrucao6(condominio.getMensagens().get(5).getMensagem());
        boleto.setInstrucao7(condominio.getMensagens().get(6).getMensagem());
        boleto.setInstrucao8(condominio.getMensagens().get(7).getMensagem());

        System.out.println("campo livre " + boleto.getCampoLivre().write());
        System.out.println("linha digitavel " + boleto.getLinhaDigitavel().write());
        System.out.println("Codigo Barras " + boleto.getCodigoDeBarras().write());

        boletos.add(boleto);
    }

    private void limparSelecoesTabelas() {
        tabelaCobrancas.clearSelection();
        tabelaCondominos.clearSelection();
    }

    private void lerArquivoRetorno() {
        EntradaArquivoRetorno entrada = new EntradaArquivoRetorno();
        for (RegistroTransacao r : entrada.getRegistros()) {
            listaRegistros.add(r);
        }
        carregarTabelaArquivos();
    }

    private void baixarVariasCobrancas() {
        for (RegistroTransacao r : listaRegistros) {
            baixarCobranca(r, true);
        }
        listaRegistros.clear();
        carregarTabelaArquivos();
    }

    private void baixarCobranca(RegistroTransacao r, boolean setContaCorrente) {
        Moeda desconto = new Moeda();
        if (r.getCobranca() != null) {

            Cobranca c = r.getCobranca();
            c.setDataPagamento(DataUtil.getCalendar(r.getData()));

            if (r.getValorPago().doubleValue() < r.getValorTitulo().doubleValue()) {
                Moeda calculoDiferencaValorPagoValorTitulo = new Moeda();
                calculoDiferencaValorPagoValorTitulo.soma(r.getValorTitulo()).subtrai(r.getValorPago());
                Pagamento pAuxiliar = getPagamentoMaiorValor(c);
                if (r.getCobranca().getUnidade().getCondominio().getDesconto().doubleValue() > 0 && calculoDiferencaValorPagoValorTitulo.doubleValue() == r.getCobranca().getUnidade().getCondominio().getDesconto().doubleValue()) {
                    desconto = new Moeda(r.getCobranca().getUnidade().getCondominio().getDesconto());
                    c.setDesconto(desconto.bigDecimalValue());
                }
                c.setDiferencaPagamento(c.getDiferencaPagamento().subtract(r.getValorPago().bigDecimalValue()));
                c.setDiferencaPagamento(c.getDiferencaPagamento().add(r.getValorTitulo().bigDecimalValue()));
                c.setDiferencaPagamento(c.getDiferencaPagamento().subtract(desconto.bigDecimalValue()));
                pAuxiliar.setValor(pAuxiliar.getValor().subtract(c.getDesconto()).subtract(c.getDiferencaPagamento()));
            } else {

                Moeda diferencaValorPagoValorTitulo = new Moeda();
                diferencaValorPagoValorTitulo.soma(r.getValorPago()).subtrai(r.getValorTitulo());

                //Verifica se o cálculo de juros e multa realizado pelo banco esta correto
                if (DataUtil.compararData(DataUtil.getDateTime(c.getDataPagamento()), DataUtil.getDateTime(c.getDataVencimento())) == 1) {
                    Cobranca cAuxiliar = new Cobranca();
                    cAuxiliar = new DAO().localizar(Cobranca.class, c.getCodigo());
                    calcularJurosMulta(cAuxiliar, r.getData());
                    Moeda valorSomaCalculoJurosMulta = new Moeda();
                    valorSomaCalculoJurosMulta.soma(cAuxiliar.getJuros()).soma(cAuxiliar.getMulta());
                    Moeda valorSomaJurosMultaInicial = new Moeda();
                    valorSomaJurosMultaInicial.soma(c.getJuros()).soma(c.getMulta());
                    Moeda resultado = new Moeda();
                    resultado.soma(valorSomaCalculoJurosMulta).subtrai(valorSomaJurosMultaInicial);
                    Moeda valorMinimo = new Moeda(diferencaValorPagoValorTitulo);
                    Moeda valorMaximo = new Moeda(diferencaValorPagoValorTitulo);
                    valorMinimo.subtrai(0.05);
                    valorMaximo.soma(0.05);

                    if (resultado.doubleValue() >= valorMinimo.doubleValue() && resultado.doubleValue() <= valorMaximo.doubleValue()) {
                        c.setMulta(c.getMulta().add(diferencaValorPagoValorTitulo.bigDecimalValue()));
                    } else {
                        Moeda diferenca = new Moeda(valorSomaCalculoJurosMulta);
                        diferenca.subtrai(diferencaValorPagoValorTitulo);
                        if (resultado.doubleValue() > valorMinimo.doubleValue()) {
                            c.setMulta(c.getMulta().add(valorSomaCalculoJurosMulta.bigDecimalValue()).subtract(diferenca.bigDecimalValue()));
                        } else {
                            c.setMulta(c.getMulta().add(valorSomaCalculoJurosMulta.bigDecimalValue()));
                        }
                        if (c.getUnidade().getCondominio().isCalcularMultaProximoMes() || diferenca.doubleValue() < 0) {
                            c.setDiferencaPagamento(diferenca.bigDecimalValue());
                        }
                    }

                } else if (diferencaValorPagoValorTitulo.doubleValue() > 0) {
                    //Verifica se houve pagamento superior ao valor da fatura
                    c.setDiferencaPagamento(c.getDiferencaPagamento().subtract(diferencaValorPagoValorTitulo.bigDecimalValue()));
                }
            }

            Moeda valor = new Moeda(c.getJuros());
            valor.soma(c.getMulta());
            if (valor.doubleValue() > 0) {
                Pagamento pagamento = new Pagamento();
                pagamento.setConta(new DAO().localizar(Conta.class, 37226));
                pagamento.setHistorico(pagamento.getConta().getNome() + " " + c.getUnidade().getUnidade() + " " + c.getUnidade().getCondomino().getNome());
                pagamento.setValor(valor.bigDecimalValue());
                pagamento.setCobranca(c);
                c.getPagamentos().add(pagamento);
            }

            if (c.getDiferencaPagamento().doubleValue() < 0) {
                Pagamento pagamento = new Pagamento();
                pagamento.setConta(new DAO().localizar(Conta.class, 39602));
                pagamento.setHistorico(pagamento.getConta().getNome() + " " + c.getUnidade().getUnidade() + " " + c.getUnidade().getCondomino().getNome());
                pagamento.setValor(c.getDiferencaPagamento().negate());
                pagamento.setCobranca(c);
                c.getPagamentos().add(pagamento);
            }

            for (Pagamento p : c.getPagamentos()) {
                Pagamento pagamentoAuxiliar = p;
                pagamentoAuxiliar.setDataPagamento(DataUtil.getCalendar(r.getData()));
                pagamentoAuxiliar.setPago(true);
                pagamentoAuxiliar.setForma(FormaPagamento.BOLETO);
                pagamentoAuxiliar.setDadosPagamento(new DadosBoleto(r.getDocumento()));
                if (setContaCorrente) {
                    pagamentoAuxiliar.setContaCorrente(c.getUnidade().getCondominio().getContaCorrente());
                    c.setLancadoCaixa(true);
                }
            }

            c.setValorPago(r.getValorPago().bigDecimalValue());

            new DAO().salvar(c);
            atualizarCobrancasCondominio(c);
        }
    }

    private Pagamento getPagamentoMaiorValor(Cobranca c) {
        Pagamento pagamento = new Pagamento();
        pagamento.setValor(new BigDecimal(0));
        for (Pagamento p : c.getPagamentos()) {
            if (p.getValor().doubleValue() > pagamento.getValor().doubleValue()) {
                pagamento = p;
            }
        }
        return pagamento;
    }

    private void atualizarCobrancasCondominio(Cobranca c) {
        if (c.getUnidade().getCondominio().getCodigo() == condominio.getCodigo()) {
            for (Unidade u : condominio.getUnidades()) {
                if (c.getUnidade().getCodigo() == u.getCodigo()) {
                    List<Cobranca> cobrancas = new DAO().listar("CobrancasEmAbertoPorUnidade", u);
                    for (Cobranca co : cobrancas) {
                        if (co.getCodigo() == c.getCodigo()) {
                            co = c;
                        }
                    }
                    u.setCobrancas(cobrancas);
                }
            }
        }
    }

    public void efetuarBaixaManual(Cobranca c) {
        DialogoBaixaManual dialogo = new DialogoBaixaManual(null, true);
        dialogo.setVisible(true);

        if (dialogo.isProximaPergunta()) {
            if (!ApresentacaoUtil.perguntar("Confirmar os dados inseridos?", this)) {
                return;
            }
        }

        if (dialogo.getValorPago() != null) {
            System.out.println("data pagamento: " + DataUtil.toString(dialogo.getDataPagamento()));
            System.out.println("valor: " + PagamentoUtil.formatarMoeda(dialogo.getValorPago().doubleValue()));

            RegistroTransacao registro = new RegistroTransacao();
            registro.setCobranca(c);
            registro.setData(dialogo.getDataPagamento());
            registro.setValorPago(new Moeda(dialogo.getValorPago()));
            registro.setValorTitulo(new Moeda(c.getValorTotal()));
            registro.setDocumento(registro.getCobranca().getNumeroDocumento());

            boolean setContaCorrente = true;

            if (!ApresentacaoUtil.perguntar("Lançar pagamento(s) no caixa?", this)) {
                setContaCorrente = false;
            }

            baixarCobranca(registro, setContaCorrente);
            carregarTabelaCobranca();
        }
    }

    private void verificarMensagens() {
        List<MensagemBoleto> mensagens = condominio.getMensagens();
        while (mensagens.size() < 8) {
            MensagemBoleto mensagem = new MensagemBoleto();
            mensagem.setCondominio(condominio);
            mensagens.add(mensagem);
        }
        condominio.setMensagens(mensagens);
        new DAO().salvar(condominio);
    }

    private void ordenarMensagens() {
        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                MensagemBoleto m1 = (MensagemBoleto) o1;
                MensagemBoleto m2 = (MensagemBoleto) o2;
                return Integer.valueOf(m1.getCodigo()).compareTo(Integer.valueOf(m2.getCodigo()));
            }
        };

        Collections.sort(condominio.getMensagens(), c);
    }

    private void preencherPainelMensagens() {
        ordenarMensagens();
        txtMensagem1.setText(condominio.getMensagens().get(0).getMensagem());
        txtMensagem2.setText(condominio.getMensagens().get(1).getMensagem());
        txtMensagem3.setText(condominio.getMensagens().get(2).getMensagem());
        txtMensagem4.setText(condominio.getMensagens().get(3).getMensagem());
        txtMensagem5.setText(condominio.getMensagens().get(4).getMensagem());
        txtMensagem6.setText(condominio.getMensagens().get(5).getMensagem());
        txtMensagem7.setText(condominio.getMensagens().get(6).getMensagem());
        txtMensagem8.setText(condominio.getMensagens().get(7).getMensagem());
    }

    private void salvarMensagens() {
        ordenarMensagens();
        condominio.getMensagens().get(0).setMensagem(txtMensagem1.getText());
        condominio.getMensagens().get(1).setMensagem(txtMensagem2.getText());
        condominio.getMensagens().get(2).setMensagem(txtMensagem3.getText());
        condominio.getMensagens().get(3).setMensagem(txtMensagem4.getText());
        condominio.getMensagens().get(4).setMensagem(txtMensagem5.getText());
        condominio.getMensagens().get(5).setMensagem(txtMensagem6.getText());
        condominio.getMensagens().get(6).setMensagem(txtMensagem7.getText());
        condominio.getMensagens().get(7).setMensagem(txtMensagem8.getText());
        new DAO().salvar(condominio);
        ApresentacaoUtil.exibirInformacao("Mensagens salvas com sucesso!", this);
    }

    private void lancarNoCaixa(List<Cobranca> lista) {
        for (Cobranca c : lista) {
            if (!c.isLancadoCaixa()) {
                for (Pagamento pagamento : c.getPagamentos()) {
                    pagamento.setContaCorrente(c.getUnidade().getCondominio().getContaCorrente());
                    c.setLancadoCaixa(true);
                }
                new DAO().salvar(c);
                atualizarCobrancasCondominio(c);
            }
        }
        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso!", this);
    }

    private void efetuarAcordo(List<Cobranca> lista) {
        Moeda valorTotalCobrancas = new Moeda();
        AcordoCobranca acordo = new AcordoCobranca();

        DialogoDadosAcordo dialogo = new DialogoDadosAcordo(null, true);
        dialogo.setVisible(true);

        if (dialogo.getForma() != null && dialogo.getNumeroParcelas() >= 1) {

            for (Cobranca co : lista) {
                valorTotalCobrancas = valorTotalCobrancas.soma(co.getValorTotal());
                co.setExibir(false);
                co.setHistorico(acordo.getHistorico());
                acordo.getHistorico().getCobrancasOriginais().add(co);
            }

            acordo.setValor(valorTotalCobrancas.bigDecimalValue());

            System.out.println("Total Cobranças: " + PagamentoUtil.formatarMoeda(valorTotalCobrancas.doubleValue()));

            System.out.println("Forma Pagamento: " + dialogo.getForma().name());
            System.out.println("Número Parcelas: " + dialogo.getNumeroParcelas());
            System.out.println("Data primeiro pagamento: " + DataUtil.toString(dialogo.getDataPrimeiroPagamento()));
            acordo.setDataPrimeiroPagamento(DataUtil.getCalendar(dialogo.getDataPrimeiroPagamento()));
            acordo.setForma(dialogo.getForma());
            acordo.setNumeroParcelas(dialogo.getNumeroParcelas());
            acordo.setCobrancasGeradas(new ArrayList<Cobranca>());
            gerarCobrancasAcordo(acordo);
            System.out.println(acordo.getUnidade().getCondominio().getRazaoSocial() + " - Unidade " + acordo.getUnidade().getUnidade());
            new DAO().salvar(acordo);
            carregarTabelaInadimplentes();
            atualizarAcordosCondominio(acordo);
        }

        System.out.println("lista originais: " + acordo.getHistorico().getCobrancasOriginais().size());
        System.out.println("lista gerada: " + acordo.getCobrancasGeradas().size());

        new DAO().salvar(lista);
    }

    private void gerarCobrancasAcordo(AcordoCobranca ac) {
        Unidade u = modeloTabelaCondominos.getObjetoSelecionado();
        for (int i = 0; i < ac.getNumeroParcelas(); i++) {
            Cobranca cobranca = new Cobranca();
            cobranca.setUnidade(u);
            cobranca.setValorTotal(new BigDecimal(0));
            cobranca.setValorOriginal(new BigDecimal(0));
            cobranca.setDataVencimento(DataUtil.getCalendar(DataUtil.getDateTime(ac.getDataPrimeiroPagamento()).plusMonths(i)));
            cobranca.setNumeroDocumento(BoletoBancario.gerarNumeroDocumento(condominio, DataUtil.getDateTime(txtDataVencimento.getValue())));
            gerarPagamentosCobrancasAcordo(ac, cobranca, i);
            u.getCobrancas().add(cobranca);
            cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavel(cobranca));
            cobranca.setNumeroDocumento(cobranca.getNumeroDocumento() + BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento()));
            ac.getCobrancasGeradas().add(cobranca);
            cobranca.setAcordo(ac);
        }
        ac.setUnidade(u);
    }

    private void gerarPagamentosCobrancasAcordo(AcordoCobranca ac, Cobranca cobranca, int i) {
        Pagamento pagamento = new Pagamento();
        pagamento.setDataVencimento(DataUtil.getCalendar(DataUtil.getDateTime(ac.getDataPrimeiroPagamento()).plusMonths(i)));
        pagamento.setCobranca(cobranca);
        pagamento.setConta(new DAO().localizar(Conta.class, 41102));
        pagamento.setHistorico(pagamento.getConta().getNome() + " " + cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());
        pagamento.setValor(ac.getValor());
        pagamento.setValor(new Moeda(pagamento.getValor().doubleValue() / (ac.getNumeroParcelas())).bigDecimalValue());
        cobranca.getPagamentos().add(pagamento);
        cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
        cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
        System.out.println("Pagamento " + (i + 1) + " - valor: " + PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
    }

    private void carregarTabelaAcordo() {
        modeloTabelaAcordo = new TabelaModelo_2<AcordoCobranca>(tabelaAcordo, "Código, Unidade, Forma Pgto., Parcelas, Valor".split(",")) {

            @Override
            protected List<AcordoCobranca> getCarregarObjetos() {
                return getAcordos();
            }

            @Override
            public Object getValor(AcordoCobranca acordo, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return acordo.getCodigo();
                    case 1:
                        return acordo.getUnidade().getUnidade();
                    case 2:
                        return acordo.getForma() == FormaPagamentoEmprestimo.PAGAMENTO_A_VISTA ? "À vista" : "Parcelado";
                    case 3:
                        return acordo.getNumeroParcelas();
                    case 4:
                        return PagamentoUtil.formatarMoeda(acordo.getValor().doubleValue());
                    default:
                        return null;
                }
            }
        };

        tabelaAcordo.getColumn(modeloTabelaAcordo.getCampo(4)).setCellRenderer(new RenderizadorCelulaADireita());
    }

    private void atualizarAcordosCondominio(AcordoCobranca a) {
        if (a.getUnidade().getCondominio().getCodigo() == condominio.getCodigo()) {
            for (Unidade u : condominio.getUnidades()) {
                if (a.getUnidade().getCodigo() == u.getCodigo()) {
                    List<AcordoCobranca> acordos = new DAO().listar("AcordosPorUnidade", u);
                    for (AcordoCobranca ac : acordos) {
                        if (ac.getCodigo() == a.getCodigo()) {
                            ac = a;
                        }
                    }
                    u.setAcordos(acordos);
                }
            }
        }
    }

    private List<AcordoCobranca> getAcordos() {
        listaAcordos = new ArrayList<AcordoCobranca>();
        for (Unidade u : condominio.getUnidades()) {
            for (AcordoCobranca ac : u.getAcordos()) {
                if (modeloTabelaCondominos.getObjetoSelecionado() == null) {
                    listaAcordos.add(ac);
                } else {
                    if (ac.getUnidade().getCodigo() == modeloTabelaCondominos.getObjetoSelecionado().getCodigo()) {
                        listaAcordos.add(ac);
                    }
                }
            }
        }
        return listaAcordos;
    }

    private void exibirPainelDetalheAcordo() {
        painelDetalheAcordo.setVisible(true);
        carregarTabelaCobrancasOriginais();
        carregarTabelaCobrancasGeradas();
    }

    private void esconderPainelDetalheAcordo() {
        painelDetalheAcordo.setVisible(false);
    }

    private void carregarTabelaCobrancasOriginais() {
        modeloTabelaCobrancasOriginais = new TabelaModelo_2<Cobranca>(tabelaCobrancasOriginais, "Unidade, Condominio, Vencimento, Documento, Valor Original, Juros, Multa, Total, Linha Digitável ".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getCobrancasOriginais();
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getRazaoSocial();
                    case 2:
                        return DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return cobranca.getNumeroDocumento();
                    case 4:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 8:
                        return cobranca.getLinhaDigitavel();
                    default:
                        return null;
                }
            }
        };

        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(3)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(4)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(5)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(6)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(7)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(8)).setCellRenderer(new RenderizadorCelulaCentralizada());

        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(0)).setMaxWidth(50);
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(1)).setMinWidth(250);
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(4)).setMinWidth(80);
        tabelaCobrancasOriginais.getColumn(modeloTabelaCobrancasOriginais.getCampo(8)).setMinWidth(265);

        tabelaCobrancasOriginais.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getCobrancasOriginais() {
        List<Cobranca> listaCobrancasOriginais = modeloTabelaAcordo.getObjetoSelecionado().getHistorico().getCobrancasOriginais();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Cobranca e1 = (Cobranca) o1;
                Cobranca e2 = (Cobranca) o2;
                return e1.getDataVencimento().compareTo(e2.getDataVencimento());
            }
        };

        Collections.sort(listaCobrancasOriginais, c);

        return listaCobrancasOriginais;
    }

    private void carregarTabelaCobrancasGeradas() {
        modeloCobrancasGeradas = new TabelaModelo_2<Cobranca>(tabelaCobrancasGeradas, "Unidade, Condominio, Vencimento, Pagamento, Documento, Valor Original, Juros, Multa, Total, Linha Digitável ".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getCobrancasGeradas(modeloTabelaAcordo.getObjetoSelecionado());
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getRazaoSocial();
                    case 2:
                        return DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return DataUtil.getDateTime(cobranca.getDataPagamento());
                    case 4:
                        return cobranca.getNumeroDocumento();
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 8:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 9:
                        return cobranca.getLinhaDigitavel();
                    default:
                        return null;
                }
            }
        };

        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(4)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(5)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(6)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(7)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(8)).setCellRenderer(new RenderizadorCelulaADireita());
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(9)).setCellRenderer(new RenderizadorCelulaCentralizada());

        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(0)).setMaxWidth(50);
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(1)).setMinWidth(250);
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(5)).setMinWidth(80);
        tabelaCobrancasGeradas.getColumn(modeloCobrancasGeradas.getCampo(9)).setMinWidth(265);

        tabelaCobrancasGeradas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getCobrancasGeradas(AcordoCobranca acordo) {
        List<Cobranca> lista = acordo.getCobrancasGeradas();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Cobranca e1 = (Cobranca) o1;
                Cobranca e2 = (Cobranca) o2;
                return e1.getDataVencimento().compareTo(e2.getDataVencimento());
            }
        };

        Collections.sort(lista, c);

        return lista;
    }

    private void removerAcordo() {
        if (!ApresentacaoUtil.perguntar("Desejar remover o(s) acordo(s)?", this)) {
            return;
        }
        if (modeloTabelaAcordo.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabelaAcordo.getLinhasSelecionadas());
            boolean removido = false;
            List<AcordoCobranca> itensRemover = modeloTabelaAcordo.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                ACORDO:
                for (AcordoCobranca ac : itensRemover) {
                    for (Cobranca cg : ac.getCobrancasGeradas()) {
                        if (cg.getDataPagamento() != null) {
                            ApresentacaoUtil.exibirAdvertencia("Não foi possível remover o acordo " + ac.getCodigo() + ", pois já existem pagamentos efetuados para o mesmo.", this);
                            continue ACORDO;
                        }
                    }
                    modeloTabelaAcordo.remover(ac);
                    for (Unidade u : condominio.getUnidades()) {
                        if (ac.getUnidade().getCodigo() == u.getCodigo()) {
                            u.getAcordos().remove(ac);
                        }
                    }
                    for (Cobranca co : ac.getHistorico().getCobrancasOriginais()) {
                        if (co.getHistorico() != null) {
                            co.setHistorico(null);
                        }
                        co.setExibir(true);
                        new DAO().salvar(co);
                        atualizarCobrancasCondominio(co);
                    }
                    new DAO().remover(ac);
                    for (Cobranca c : ac.getCobrancasGeradas()) {
                        atualizarCobrancasCondominio(c);
                    }
                    removido = true;
                }
            }
            if (removido) {
                esconderPainelDetalheAcordo();
                ApresentacaoUtil.exibirInformacao("Acordo(s) removido(s) com sucesso!", this);
            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void imprimirDetalheAcordo(AcordoCobranca ac) {
        Moeda totalOriginal = new Moeda();
        Moeda totalGerado = new Moeda();

        Comparator<Cobranca> comparador = new Comparator<Cobranca>() {

            public int compare(Cobranca o1, Cobranca o2) {
                return o1.getDataVencimento().compareTo(o2.getDataVencimento());
            }
        };

        List<HashMap<String, String>> listaCobrancasOriginais = new ArrayList<HashMap<String, String>>();
        Collections.sort(ac.getHistorico().getCobrancasOriginais(), comparador);
        for (Cobranca co : ac.getHistorico().getCobrancasOriginais()) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("valorPrestacao", PagamentoUtil.formatarMoeda(co.getValorTotal().doubleValue()));
            mapa.put("dataVencimento", DataUtil.toString(co.getDataVencimento()));
            mapa.put("pagamento", "-");
            mapa.put("documento", co.getNumeroDocumento());
            totalOriginal.soma(co.getValorTotal());
            listaCobrancasOriginais.add(mapa);
        }

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("codigo", String.valueOf(ac.getCodigo()));
        parametros.put("valor", PagamentoUtil.formatarMoeda(ac.getValor().doubleValue()));
        parametros.put("numeroParcelas", String.valueOf(ac.getNumeroParcelas()));
        parametros.put("forma", ac.getForma().name());
        parametros.put("condominio", ac.getUnidade().getCondominio().getRazaoSocial());
        parametros.put("unidade", ac.getUnidade().getUnidade());
        parametros.put("condomino", ac.getUnidade().getCondomino().getNome());
        parametros.put("lista", new JRBeanCollectionDataSource(listaCobrancasOriginais));
        parametros.put("totalOriginal", PagamentoUtil.formatarMoeda(totalOriginal.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");

        parametros.put("subrelatorio", caminho.toString());

        List<HashMap<String, String>> listaCobrancasGeradas = new ArrayList<HashMap<String, String>>();
        Collections.sort(ac.getCobrancasGeradas(), comparador);
        int i = 0;
        for (Cobranca co : ac.getCobrancasGeradas()) {
            i += 1;
            HashMap<String, String> mapa = new HashMap();
            mapa.put("numero", i + "/" + ac.getNumeroParcelas());
            mapa.put("valorPrestacao", PagamentoUtil.formatarMoeda(co.getValorTotal().doubleValue()));
            mapa.put("dataVencimento", DataUtil.toString(co.getDataVencimento()));
            mapa.put("pagamento", co.getDataPagamento() != null ? DataUtil.toString(co.getDataPagamento()) : "Em aberto");
            mapa.put("documento", co.getNumeroDocumento());
            totalGerado.soma(co.getValorTotal());
            listaCobrancasGeradas.add(mapa);
        }

        parametros.put("totalGerado", PagamentoUtil.formatarMoeda(totalGerado.doubleValue()));

        new Relatorios().imprimir("RelatorioDetalheAcordo", parametros, listaCobrancasGeradas, false, true, null);
    }

    private void imprimirCarta(TipoRelatorio tipo) {
        DialogoDadosCartaSintetica dialogo = new DialogoDadosCartaSintetica(null, true);
        dialogo.setVisible(true);

        if (dialogo.getDataIncial() != null && dialogo.getDataFinal() != null) {
            if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
                for (Unidade u : condominio.getUnidades()) {
                    new Relatorios().imprimirCarta(u, dialogo.getDataIncial(), dialogo.getDataFinal(), tipo);
                }
            } else {
                for (Unidade u : modeloTabelaCondominos.getObjetosSelecionados()) {
                    new Relatorios().imprimirCarta(u, dialogo.getDataIncial(), dialogo.getDataFinal(), tipo);
                }
            }
        }
    }

    public void imprimirInadimplencia(TipoRelatorio tipo) {
        if (!modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            DialogoDadosInadimplencia dialogo = new DialogoDadosInadimplencia(null, true, condominio, tipo, modeloTabelaCondominos.getObjetosSelecionados());
            dialogo.setVisible(true);
        } else {
            DialogoDadosInadimplencia dialogo = new DialogoDadosInadimplencia(null, true, condominio, tipo, condominio.getUnidades());
            dialogo.setVisible(true);
        }
    }

    public void imprimirEnvelope() {
        if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            ApresentacaoUtil.exibirAdvertencia("Selecione as unidades desejadas.", this);
        } else {
            DialogoDadosEnvelope dialogo = new DialogoDadosEnvelope(null, true);
            dialogo.setVisible(true);
            if (!dialogo.getCancelar()) {
                new Relatorios().imprimirEnvelope(dialogo.getImprimirRemetente(), dialogo.getDataVencimento(), condominio, modeloTabelaCondominos.getObjetosSelecionados(), dialogo.getImprimirParaInquilino(), dialogo.getImprimirParaProprietario());
            }
        }
    }

    public void imprimirRelacaoPostagem() {
        if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            ApresentacaoUtil.exibirAdvertencia("Selecione as unidades desejadas.", this);
        } else {
            new Relatorios().imprimirRelacaoProprietarios(true, null, condominio, modeloTabelaCondominos.getObjetosSelecionados(), TipoRelatorio.RELACAO_POSTAGEM);
        }
    }

    public void imprimirListaPresentesAssembleia(TipoRelatorio tipo) {
        DialogoDadosListaAssembleia dialogo = new DialogoDadosListaAssembleia(null, true, tipo);
        dialogo.setVisible(true);
        if (!dialogo.getCancelar()) {
            new Relatorios().imprimirListaAssembleia(condominio, modeloTabelaCondominos.getObjetos(), dialogo.getData(), tipo);
        }
    }

    public void imprimirListaFracoesIdeais() {
        new Relatorios().imprimirListaFracoesIdeais(condominio, modeloTabelaCondominos.getObjetos());
    }

    public void imprimirRelacaoProprietarios(TipoRelatorio tipo) {
        new Relatorios().imprimirRelacaoProprietarios(true, null, condominio, modeloTabelaCondominos.getObjetos(), tipo);
    }

    public void imprimirCertificadoQuitacao() {
        if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            ApresentacaoUtil.exibirAdvertencia("Selecione a unidade desejada.", this);
        } else {
            DialogoDadosCertificadoQuitacao dialogo = new DialogoDadosCertificadoQuitacao(null, true);
            dialogo.setVisible(true);

            if (dialogo.getDataIncial() != null && dialogo.getDataFinal() != null) {
                boolean imprimir = true;
                boolean imprimirObservacao = false;

                for (Cobranca co : modeloTabelaCondominos.getObjetoSelecionado().getCobrancas()) {
                    if (co.getDataPagamento() == null && DataUtil.compararData(dialogo.getDataIncial(), DataUtil.getDateTime(co.getDataVencimento())) == -1 && DataUtil.compararData(dialogo.getDataFinal(), DataUtil.getDateTime(co.getDataVencimento())) == 1 && co.isExibir()) {
                        imprimir = false;
                    }
                    if (co.getAcordo() != null) {
                        imprimirObservacao = true;
                    }
                }

                if (imprimir) {
                    new Relatorios().imprimirCertificadoQuitacao(modeloTabelaCondominos.getObjetoSelecionado(), dialogo.getDataFinal(), dialogo.getImprimirAssinaturaBreca(), imprimirObservacao);
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Não é possível imprimir, pois a unidade selecionada possui débito.", this);
                }
            }
        }
    }

    private void imprimirPagamentosEfetuados(TipoRelatorio tipo) {
        if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            new Relatorios().imprimirRelatorioPagamentosEfetuados(condominio, condominio.getUnidades(), DataUtil.getDateTime(txtDataInicial.getValue()), DataUtil.getDateTime(txtDataFinal.getValue()), tipo);
        } else {
            new Relatorios().imprimirRelatorioPagamentosEfetuados(condominio, modeloTabelaCondominos.getObjetosSelecionados(), DataUtil.getDateTime(txtDataInicial.getValue()), DataUtil.getDateTime(txtDataFinal.getValue()), tipo);
        }
    }

    private void imprimirCobrancasAVencer(TipoRelatorio tipo) {
        if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            new Relatorios().imprimirCobrancasExistentesAVencer(condominio, condominio.getUnidades(), tipo);
        } else {
            new Relatorios().imprimirCobrancasExistentesAVencer(condominio, modeloTabelaCondominos.getObjetosSelecionados(), tipo);
        }
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtConta);
        campos.add(txtHistorico);

        return campos;
    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false, "");
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            txtHistorico.setText(conta.getNome());
        }
    }

    private void incluirItemAvulso() {
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(this);
            return;
        }
        ItemCobranca item = new ItemCobranca();
        item.setCodigoConta(conta.getCodigo());
        item.setCodigoObjeto(0);
        item.setDescricao(txtHistorico.getText());
        item.setDividirFracaoIdeal(false);
        item.setValor(new BigDecimal(0));
        listaItensAvulsos.add(item);
    }

    private void limparCampos() {
        txtConta.setText("");
        txtHistorico.setText("");
        conta = null;
    }

    private void limparListaDadosAvulsos() {
        modeloTabelaDadosAvulsos.limpar();
        carregarTabelaDadosAvulsos();
    }

    private void removerItensCobranca(List<ItemCobranca> lista) {
        for (ItemCobranca item : lista) {
            modeloTabelaDadosAvulsos.remover(item);
        }
        carregarTabelaDadosAvulsos();
    }

    private List<Unidade> filtrarListaCondominoPorUnidade(String sequencia, List<Unidade> unidades) {
        ArrayList<Unidade> listaFiltrada = new ArrayList<Unidade>();

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        UNIDADES:
        for (Unidade u : unidades) {
            for (String s : sequencias) {
                if (!u.getUnidade().toUpperCase().contains(s)) {
                    continue UNIDADES;
                }
            }

            listaFiltrada.add(u);
        }

        return listaFiltrada;
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void configurar() {
            btnCarregarInformacoesCobranca.addActionListener(this);
            btnConfirmar.addActionListener(this);
            btnConta.addActionListener(this);
            btnEfetuarAcordo.addActionListener(this);
            btnEsconderPainel.addActionListener(this);
            btnGerarCobranca.addActionListener(this);
            btnGerarCobrancaAvulsa.addActionListener(this);
            btnImprimirBoleto.addActionListener(this);
            btnIncluirItemAvulso.addActionListener(this);
            btnLerArquivoRetorno.addActionListener(this);
            btnLimpar.addActionListener(this);
            btnImprimirBoletoInadimplente.addActionListener(this);
            btnSalvarMensagem.addActionListener(this);
            itemMenuBaixaManual.addActionListener(this);
            itemMenuBaixaManualInadimplente.addActionListener(this);
            itemMenuExibirDetalheAcordo.addActionListener(this);
            itemMenuImprimirDetalheAcordo.addActionListener(this);
            itemMenuImprimirCartaSintetica.addActionListener(this);
            itemMenuImprimirCartaAnalitica.addActionListener(this);
            itemMenuImprimirEnvelopeP.addActionListener(this);
            itemMenuPresentesAE.addActionListener(this);
            itemMenuPresentesAO.addActionListener(this);
            itemMenuRelacaoFracoesIdeais.addActionListener(this);
            itemMenuImprimirRelacaoProprietarios.addActionListener(this);
            itemMenuImprimirRelacaoProprietariosEmail.addActionListener(this);
            itemMenuImprimirRelacaoProprietariosUnidade.addActionListener(this);
            itemMenuImprimirRelacaoPostagem.addActionListener(this);
            itemMenuImprimirCertificadoQuitacao.addActionListener(this);
            itemMenuImprimirInadimplenciaSintetica.addActionListener(this);
            itemMenuImprimirInadimplenciaAnalitica.addActionListener(this);
            itemMenuImprimirPagosSintetico.addActionListener(this);
            itemMenuImprimirPagosAnalitico.addActionListener(this);
            itemMenuLancarNoCaixa.addActionListener(this);
            itemMenuLimparDadosAvulsos.addActionListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            itemMenuCobrancasExistentesAVencerAnalitico.addActionListener(this);
            itemMenuCobrancasExistentesAVencerSintetico.addActionListener(this);
            itemMenuCalcularJurosMulta.addActionListener(this);
            itemMenuOcultar.addActionListener(this);
            itemMenuOcultarDados.addActionListener(this);
            itemMenuRemoverAcordo.addActionListener(this);
            itemMenuRemoverItemCobranca.addActionListener(this);
            jTabbedPane1.addChangeListener(this);
            tabelaAcordo.addKeyListener(this);
            tabelaAcordo.addMouseListener(this);
            tabelaCobrancas.addMouseListener(this);
            tabelaCobrancasBase.addMouseListener(this);
            tabelaCondominos.addMouseListener(this);
            tabelaDadosAvulsos.addMouseListener(this);
            tabelaInadimplentes.addMouseListener(this);
            tabelaPagos.addMouseListener(this);
            txtConta.addFocusListener(this);
            txtDataInicial.addChangeListener(this);
            txtDataFinal.addChangeListener(this);
            txtFiltro.addCaretListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == btnGerarCobranca || origem == btnGerarCobrancaAvulsa) {
                if (modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
                    gerarCobrancas(condominio.getUnidades());
                } else {
                    gerarCobrancas(modeloTabelaCondominos.getObjetosSelecionados());
                }
                carregarTabelaCobranca();
            } else if (origem == itemMenuRemoverSelecionados) {
                remover();
            } else if (origem == itemMenuCalcularJurosMulta) {
                if (modeloTabelaInadimplentes.getLinhaSelecionada() > -1) {
                    calcularJurosMulta(modeloTabelaInadimplentes.getObjetoSelecionado(), DataUtil.getDateTime(txtVencimentoProrrogado.getValue()));
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para calcular!", TelaLancamentos.this);
                }
            } else if (origem == btnImprimirBoleto) {
                if (modeloTabelaBoleto.getLinhaSelecionada() > -1) {
                    imprimirBoleto(modeloTabelaBoleto.getObjetosSelecionados());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a(s) cobrança(s) que deseja imprimir.", TelaLancamentos.this);
                }
            } else if (origem == btnImprimirBoletoInadimplente) {
                if (modeloTabelaInadimplentes.getLinhaSelecionada() > -1) {
                    imprimirBoleto(modeloTabelaInadimplentes.getObjetosSelecionados());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a(s) cobrança(s) que deseja imprimir.", TelaLancamentos.this);
                }
            } else if (origem == itemMenuOcultar) {
                painelCobrancaBase.setVisible(false);
            } else if (origem == itemMenuOcultarDados) {
                painelCobrancaBase.setVisible(false);
            } else if (origem == itemMenuLimparDadosAvulsos) {
                limparListaDadosAvulsos();
            } else if (origem == itemMenuRemoverItemCobranca) {
                removerItensCobranca(modeloTabelaDadosAvulsos.getObjetosSelecionados());
            } else if (origem == btnLimpar) {
                limparSelecoesTabelas();
                if (jTabbedPane1.getSelectedIndex() == 0) {
                    carregarTabelaCobranca();
                } else if (jTabbedPane1.getSelectedIndex() == 1) {
                    carregarTabelaInadimplentes();
                } else if (jTabbedPane1.getSelectedIndex() == 2) {
                    carregarTabelaPagos();
                }
            } else if (origem == btnLerArquivoRetorno) {
                lerArquivoRetorno();
            } else if (origem == btnConfirmar) {
                baixarVariasCobrancas();
            } else if (origem == btnSalvarMensagem) {
                salvarMensagens();
            } else if (origem == itemMenuBaixaManual) {
                if (modeloTabelaBoleto.getObjetosSelecionados().size() == 1) {
                    efetuarBaixaManual(modeloTabelaBoleto.getObjetoSelecionado());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a cobrança que deseja baixar.", TelaLancamentos.this);
                }
            } else if (origem == itemMenuBaixaManualInadimplente) {
                if (modeloTabelaInadimplentes.getObjetosSelecionados().size() == 1) {
                    efetuarBaixaManual(modeloTabelaInadimplentes.getObjetoSelecionado());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a cobrança que deseja baixar.", TelaLancamentos.this);
                }
            } else if (origem == itemMenuLancarNoCaixa) {
                if (modeloTabelaPagos.getObjetosSelecionados().size() > 0) {
                    lancarNoCaixa(modeloTabelaPagos.getObjetosFiltrados());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a(s) cobrança(s) que deseja lançar no caixa.", TelaLancamentos.this);
                }
            } else if (origem == btnEfetuarAcordo) {
                if (modeloTabelaCondominos.getObjetosSelecionados().size() == 1) {
                    if (modeloTabelaInadimplentes.getObjetosSelecionados().size() >= condominio.getNumeroMinimoParcelasAcordo()) {
                        efetuarAcordo(modeloTabelaInadimplentes.getObjetosSelecionados());
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Selecione " + condominio.getNumeroMinimoParcelasAcordo() + " cobranças, no mínimo.", TelaLancamentos.this);
                    }
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione apenas um condômino.", TelaLancamentos.this);
                }
            } else if (origem == itemMenuExibirDetalheAcordo) {
                if (modeloTabelaAcordo.getObjetosSelecionados().size() == 1) {
                    exibirPainelDetalheAcordo();
                } else {
                    ApresentacaoUtil.exibirAdvertencia(("Selecione um acordo, para visualizar os detalhes."), TelaLancamentos.this);
                }
            } else if (origem == btnEsconderPainel) {
                esconderPainelDetalheAcordo();
            } else if (origem == itemMenuRemoverAcordo) {
                removerAcordo();
            } else if (origem == itemMenuImprimirDetalheAcordo) {
                if (modeloTabelaAcordo.getObjetosSelecionados().size() == 1) {
                    imprimirDetalheAcordo(modeloTabelaAcordo.getObjetoSelecionado());
                } else {
                    ApresentacaoUtil.exibirAdvertencia(("Selecione um acordo, para visualizar os detalhes."), TelaLancamentos.this);
                }
            } else if (origem == btnCarregarInformacoesCobranca) {
                painelCobrancaBase.setVisible(true);
                carregarTabelaCobrancaBase();
                carregarTabelaDadosAvulsos();
            } else if (origem == btnIncluirItemAvulso) {
                incluirItemAvulso();
                carregarTabelaDadosAvulsos();
                limparCampos();
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == itemMenuImprimirCartaSintetica) {
                imprimirCarta(TipoRelatorio.CARTA_SINTETICA);
            } else if (origem == itemMenuImprimirCartaAnalitica) {
                imprimirCarta(TipoRelatorio.CARTA_ANALITICA);
            } else if (origem == itemMenuImprimirInadimplenciaSintetica) {
                imprimirInadimplencia(TipoRelatorio.INADIMPLENCIA_SINTETICA);
            } else if (origem == itemMenuImprimirInadimplenciaAnalitica) {
                imprimirInadimplencia(TipoRelatorio.INADIMPLENCIA_ANALITICA);
            } else if (origem == itemMenuImprimirEnvelopeP) {
                imprimirEnvelope();
            } else if (origem == itemMenuPresentesAE) {
                imprimirListaPresentesAssembleia(TipoRelatorio.ASSEMBLEIA_EXTRAORDINARIA);
            } else if (origem == itemMenuPresentesAO) {
                imprimirListaPresentesAssembleia(TipoRelatorio.ASSEMBLEIA_ORDINARIA);
            } else if (origem == itemMenuRelacaoFracoesIdeais) {
                imprimirListaFracoesIdeais();
            } else if (origem == itemMenuImprimirRelacaoProprietarios) {
                imprimirRelacaoProprietarios(TipoRelatorio.RELACAO_PROPRIETARIOS);
            } else if (origem == itemMenuImprimirRelacaoProprietariosEmail) {
                imprimirRelacaoProprietarios(TipoRelatorio.RELACAO_PROPRIETARIOS_EMAIL);
            } else if (origem == itemMenuImprimirRelacaoProprietariosUnidade) {
                imprimirRelacaoProprietarios(TipoRelatorio.RELACAO_PROPRIETARIOS_UNIDADE);
            } else if (origem == itemMenuImprimirRelacaoPostagem) {
                imprimirRelacaoPostagem();
            } else if (origem == itemMenuImprimirCertificadoQuitacao) {
                imprimirCertificadoQuitacao();
            } else if (origem == itemMenuImprimirPagosSintetico) {
                imprimirPagamentosEfetuados(TipoRelatorio.PAGAMENTOS_EFETUADOS_SINTETICO);
            } else if (origem == itemMenuImprimirPagosAnalitico) {
                imprimirPagamentosEfetuados(TipoRelatorio.PAGAMENTOS_EFETUADOS_ANALITICO);
            } else if (origem == itemMenuCobrancasExistentesAVencerSintetico) {
                imprimirCobrancasAVencer(TipoRelatorio.COBRANCAS_EXISTENTES_A_VENCER_SINTETICO);
            } else if (origem == itemMenuCobrancasExistentesAVencerAnalitico) {
                imprimirCobrancasAVencer(TipoRelatorio.COBRANCAS_EXISTENTES_A_VENCER_ANALITICO);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getSource() == tabelaCondominos) {
                if (jTabbedPane1.getSelectedIndex() == 0) {
                    carregarTabelaCobranca();
                } else if (jTabbedPane1.getSelectedIndex() == 1) {
                    carregarTabelaInadimplentes();
                } else if (jTabbedPane1.getSelectedIndex() == 2) {
                    carregarTabelaPagos();
                }
                if (e.isPopupTrigger()) {
                    popupMenuCondominos.show(e.getComponent(), e.getX(), e.getY());
                }
            } else if (e.isPopupTrigger()) {
                if (e.getSource() == tabelaCobrancas) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaInadimplentes) {
                    popupMenuInadimplentes.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaPagos) {
                    popupMenuPagos.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaAcordo) {
                    popupMenuAcordo.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaCobrancasBase) {
                    popupMenuCobrancaBase.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaDadosAvulsos) {
                    popupMenuDadosAvulsos.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == txtConta) {
                Conta resultado = null;
                if (new LimitarCaracteres(10).ValidaNumero(txtConta)) {
                    if (!txtConta.getText().equals("") && txtConta.getText() != null) {
                        resultado = ContaUtil.pesquisarContaPorCodigo(Integer.valueOf(txtConta.getText()));
                        if (resultado != null) {
                            conta = resultado;
                            txtConta.setText(String.valueOf(conta.getCodigo()));
                            txtHistorico.setText(conta.getNome());
                        } else {
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaLancamentos.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            origem = e.getSource();
            if (origem == jTabbedPane1) {
                if (jTabbedPane1.getSelectedIndex() == 0) {
                    carregarTabelaCobranca();
                    limparSelecoesTabelas();
                } else if (jTabbedPane1.getSelectedIndex() == 1) {
                    carregarTabelaInadimplentes();
                    limparSelecoesTabelas();
                } else if (jTabbedPane1.getSelectedIndex() == 2) {
                    carregarTabelaPagos();
                    limparSelecoesTabelas();
                } else if (jTabbedPane1.getSelectedIndex() == 4) {
                    preencherPainelMensagens();
                } else if (jTabbedPane1.getSelectedIndex() == 5) {
                    carregarTabelaAcordo();
                    painelDetalheAcordo.setVisible(false);
                }
            } else if (e.getSource() == txtDataInicial || e.getSource() == txtDataFinal) {
                ApresentacaoUtil.verificarDatas(e.getSource(), txtDataInicial, txtDataFinal, this);
                carregarTabelaPagos();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            origem = e.getSource();
            if ((origem == tabelaAcordo && painelDetalheAcordo.isVisible()) && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
                exibirPainelDetalheAcordo();
            }
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            if (e.getSource() == txtFiltro) {
                modeloTabelaCondominos.filtrar();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuBaixaManual = new javax.swing.JMenuItem();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        itemMenuCobrancasExistentesAVencerSintetico = new javax.swing.JMenuItem();
        itemMenuCobrancasExistentesAVencerAnalitico = new javax.swing.JMenuItem();
        popupMenuInadimplentes = new javax.swing.JPopupMenu();
        itemMenuCalcularJurosMulta = new javax.swing.JMenuItem();
        itemMenuBaixaManualInadimplente = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuImprimirCartaSintetica = new javax.swing.JMenuItem();
        itemMenuImprimirCartaAnalitica = new javax.swing.JMenuItem();
        itemMenuImprimirInadimplenciaSintetica = new javax.swing.JMenuItem();
        itemMenuImprimirInadimplenciaAnalitica = new javax.swing.JMenuItem();
        popupMenuPagos = new javax.swing.JPopupMenu();
        itemMenuLancarNoCaixa = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        itemMenuImprimirPagosSintetico = new javax.swing.JMenuItem();
        itemMenuImprimirPagosAnalitico = new javax.swing.JMenuItem();
        popupMenuAcordo = new javax.swing.JPopupMenu();
        itemMenuExibirDetalheAcordo = new javax.swing.JMenuItem();
        itemMenuImprimirDetalheAcordo = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        itemMenuRemoverAcordo = new javax.swing.JMenuItem();
        popupMenuCobrancaBase = new javax.swing.JPopupMenu();
        itemMenuOcultar = new javax.swing.JMenuItem();
        popupMenuCondominos = new javax.swing.JPopupMenu();
        itemMenuImprimirEnvelopeP = new javax.swing.JMenuItem();
        itemMenuPresentesAO = new javax.swing.JMenuItem();
        itemMenuPresentesAE = new javax.swing.JMenuItem();
        itemMenuRelacaoFracoesIdeais = new javax.swing.JMenuItem();
        itemMenuImprimirRelacaoProprietarios = new javax.swing.JMenuItem();
        itemMenuImprimirRelacaoProprietariosEmail = new javax.swing.JMenuItem();
        itemMenuImprimirRelacaoProprietariosUnidade = new javax.swing.JMenuItem();
        itemMenuImprimirRelacaoPostagem = new javax.swing.JMenuItem();
        itemMenuImprimirCertificadoQuitacao = new javax.swing.JMenuItem();
        popupMenuDadosAvulsos = new javax.swing.JPopupMenu();
        itemMenuOcultarDados = new javax.swing.JMenuItem();
        itemMenuLimparDadosAvulsos = new javax.swing.JMenuItem();
        itemMenuRemoverItemCobranca = new javax.swing.JMenuItem();
        painelCondominos = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaCondominos = new javax.swing.JTable();
        jLabel15 = new javax.swing.JLabel();
        txtFiltro = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        painelLancamentos = new javax.swing.JPanel();
        painelBoletos = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaCobrancas = new javax.swing.JTable();
        painelCobrancaBase = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtDataVencimento = new net.sf.nachocalendar.components.DateField();
        btnGerarCobranca = new javax.swing.JButton();
        btnImprimirBoleto = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaCobrancasBase = new javax.swing.JTable();
        painelDadosAvulsos = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        txtDataVencimentoAvulso = new net.sf.nachocalendar.components.DateField();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnGerarCobrancaAvulsa = new javax.swing.JButton();
        btnImprimirBoletoAvulso = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        tabelaDadosAvulsos = new javax.swing.JTable();
        jPanel11 = new javax.swing.JPanel();
        btnConta = new javax.swing.JButton();
        txtConta = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtHistorico = new javax.swing.JTextField();
        btnIncluirItemAvulso = new javax.swing.JButton();
        painelInadimplentes = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaInadimplentes = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtVencimentoProrrogado = new net.sf.nachocalendar.components.DateField();
        btnImprimirBoletoInadimplente = new javax.swing.JButton();
        btnEfetuarAcordo = new javax.swing.JButton();
        painelPagos = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabelaPagos = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tabelaArquivoRetorno = new javax.swing.JTable();
        btnLerArquivoRetorno = new javax.swing.JButton();
        btnConfirmar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        painelMensagem = new javax.swing.JPanel();
        txtMensagem1 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        btnSalvarMensagem = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtMensagem2 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtMensagem3 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtMensagem4 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtMensagem5 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtMensagem6 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtMensagem7 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtMensagem8 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tabelaAcordo = new javax.swing.JTable();
        painelDetalheAcordo = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        tabelaCobrancasOriginais = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        tabelaCobrancasGeradas = new javax.swing.JTable();
        btnEsconderPainel = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        btnCarregarInformacoesCobranca = new javax.swing.JButton();

        itemMenuBaixaManual.setText("Efetuar Baixa Item Selecionado");
        popupMenu.add(itemMenuBaixaManual);

        itemMenuRemoverSelecionados.setText("Remover Selecionado(s)");
        popupMenu.add(itemMenuRemoverSelecionados);
        popupMenu.add(jSeparator4);

        itemMenuCobrancasExistentesAVencerSintetico.setText("Imprimir Cobranças Existentes à Vencer - Sintético");
        popupMenu.add(itemMenuCobrancasExistentesAVencerSintetico);

        itemMenuCobrancasExistentesAVencerAnalitico.setText("Imprimir Cobranças Existentes à Vencer - Analítico");
        popupMenu.add(itemMenuCobrancasExistentesAVencerAnalitico);

        itemMenuCalcularJurosMulta.setText("Calcular Juros/Multa");
        popupMenuInadimplentes.add(itemMenuCalcularJurosMulta);

        itemMenuBaixaManualInadimplente.setText("Efetuar Baixa Item Selecionado");
        popupMenuInadimplentes.add(itemMenuBaixaManualInadimplente);
        popupMenuInadimplentes.add(jSeparator1);

        itemMenuImprimirCartaSintetica.setText("Imprimir Carta Sintética");
        popupMenuInadimplentes.add(itemMenuImprimirCartaSintetica);

        itemMenuImprimirCartaAnalitica.setText("Imprimir Carta Analítica");
        popupMenuInadimplentes.add(itemMenuImprimirCartaAnalitica);

        itemMenuImprimirInadimplenciaSintetica.setText("Inadimplência Sintética");
        popupMenuInadimplentes.add(itemMenuImprimirInadimplenciaSintetica);

        itemMenuImprimirInadimplenciaAnalitica.setText("Inadimplência Analítica");
        popupMenuInadimplentes.add(itemMenuImprimirInadimplenciaAnalitica);

        itemMenuLancarNoCaixa.setText("Lançar Cobrança no Caixa");
        popupMenuPagos.add(itemMenuLancarNoCaixa);
        popupMenuPagos.add(jSeparator3);

        itemMenuImprimirPagosSintetico.setText("Imprimir Pagamentos Efetuados - Sintético");
        popupMenuPagos.add(itemMenuImprimirPagosSintetico);

        itemMenuImprimirPagosAnalitico.setText("Imprimir Pagamentos Efetuados - Analítico");
        popupMenuPagos.add(itemMenuImprimirPagosAnalitico);

        itemMenuExibirDetalheAcordo.setText("Exibir Detalhes");
        popupMenuAcordo.add(itemMenuExibirDetalheAcordo);

        itemMenuImprimirDetalheAcordo.setText("Imprimir Detalhes");
        popupMenuAcordo.add(itemMenuImprimirDetalheAcordo);
        popupMenuAcordo.add(jSeparator2);

        itemMenuRemoverAcordo.setText("Remover Selecionado(s)");
        popupMenuAcordo.add(itemMenuRemoverAcordo);

        itemMenuOcultar.setText("Mudar Altura da Lista");
        popupMenuCobrancaBase.add(itemMenuOcultar);

        itemMenuImprimirEnvelopeP.setText("Imprimir Envelope Pequeno");
        popupMenuCondominos.add(itemMenuImprimirEnvelopeP);

        itemMenuPresentesAO.setText("Imprimir Lista Presentes na Assembléia Ordinária");
        popupMenuCondominos.add(itemMenuPresentesAO);

        itemMenuPresentesAE.setText("Imprimir Lista Presentes na Assembléia Extraordinária");
        popupMenuCondominos.add(itemMenuPresentesAE);

        itemMenuRelacaoFracoesIdeais.setText("Imprimir Relação Frações Ideais");
        popupMenuCondominos.add(itemMenuRelacaoFracoesIdeais);

        itemMenuImprimirRelacaoProprietarios.setText("Imprimir Relação de Proprietários");
        popupMenuCondominos.add(itemMenuImprimirRelacaoProprietarios);

        itemMenuImprimirRelacaoProprietariosEmail.setText("Imprimir Relação de Proprietários - Email");
        popupMenuCondominos.add(itemMenuImprimirRelacaoProprietariosEmail);

        itemMenuImprimirRelacaoProprietariosUnidade.setText("Imprimir Relação de Proprietários - Unidade");
        popupMenuCondominos.add(itemMenuImprimirRelacaoProprietariosUnidade);

        itemMenuImprimirRelacaoPostagem.setText("Imprimir Relação para Postagem");
        popupMenuCondominos.add(itemMenuImprimirRelacaoPostagem);

        itemMenuImprimirCertificadoQuitacao.setText("Imprimir Certificado de Quitação");
        popupMenuCondominos.add(itemMenuImprimirCertificadoQuitacao);

        itemMenuOcultarDados.setText("Mudar Altura da Lista");
        popupMenuDadosAvulsos.add(itemMenuOcultarDados);

        itemMenuLimparDadosAvulsos.setText("Limpar Lista");
        popupMenuDadosAvulsos.add(itemMenuLimparDadosAvulsos);

        itemMenuRemoverItemCobranca.setText("Remover Selecionado(s)");
        popupMenuDadosAvulsos.add(itemMenuRemoverItemCobranca);

        setClosable(true);
        setTitle("Cobranças");
        setPreferredSize(new java.awt.Dimension(874, 549));
        setVisible(true);

        painelCondominos.setBorder(javax.swing.BorderFactory.createTitledBorder("Condôminos"));

        tabelaCondominos.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCondominos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tabelaCondominos);

        jLabel15.setText("Filtrar por Unidade:");

        txtFiltro.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout painelCondominosLayout = new javax.swing.GroupLayout(painelCondominos);
        painelCondominos.setLayout(painelCondominosLayout);
        painelCondominosLayout.setHorizontalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelCondominosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelCondominosLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        painelCondominosLayout.setVerticalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCondominosLayout.createSequentialGroup()
                .addGroup(painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelBoletos.setBorder(javax.swing.BorderFactory.createTitledBorder("Boletos Gerados"));

        tabelaCobrancas.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCobrancas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tabelaCobrancas);

        javax.swing.GroupLayout painelBoletosLayout = new javax.swing.GroupLayout(painelBoletos);
        painelBoletos.setLayout(painelBoletosLayout);
        painelBoletosLayout.setHorizontalGroup(
            painelBoletosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBoletosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelBoletosLayout.setVerticalGroup(
            painelBoletosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBoletosLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Vencimento");

        btnGerarCobranca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnGerarCobranca.setToolTipText("Gerar Cobrança");

        btnImprimirBoleto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimirBoleto.setToolTipText("Imprimir Boleto");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))
                    .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnGerarCobranca, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnImprimirBoleto, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(342, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnGerarCobranca, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnImprimirBoleto))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaCobrancasBase.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCobrancasBase.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tabelaCobrancasBase);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelCobrancaBase.addTab("Cobranças Base", jPanel8);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Etapa 2"));

        jLabel16.setText("Vencimento");

        btnGerarCobrancaAvulsa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnGerarCobrancaAvulsa.setToolTipText("Gerar Cobrança Avulsa");

        btnImprimirBoletoAvulso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimirBoletoAvulso.setToolTipText("Imprimir Boleto Avulso");

        jLabel17.setText("Nº Documento");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))
                    .addComponent(txtDataVencimentoAvulso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGerarCobrancaAvulsa, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnImprimirBoletoAvulso, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataVencimentoAvulso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnGerarCobrancaAvulsa, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnImprimirBoletoAvulso)))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        tabelaDadosAvulsos.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaDadosAvulsos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane10.setViewportView(tabelaDadosAvulsos);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Etapa 1"));

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtConta.setName("Conta"); // NOI18N

        jLabel21.setText("Histórico:");

        txtHistorico.setName("Histórico"); // NOI18N

        btnIncluirItemAvulso.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluirItemAvulso.setToolTipText("Incluir Item");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnIncluirItemAvulso, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnIncluirItemAvulso, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(btnConta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelDadosAvulsosLayout = new javax.swing.GroupLayout(painelDadosAvulsos);
        painelDadosAvulsos.setLayout(painelDadosAvulsosLayout);
        painelDadosAvulsosLayout.setHorizontalGroup(
            painelDadosAvulsosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosAvulsosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosAvulsosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDadosAvulsosLayout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(painelDadosAvulsosLayout.createSequentialGroup()
                        .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7))))
        );
        painelDadosAvulsosLayout.setVerticalGroup(
            painelDadosAvulsosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosAvulsosLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(painelDadosAvulsosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelCobrancaBase.addTab("Gerar Boleto Avulso", painelDadosAvulsos);

        javax.swing.GroupLayout painelLancamentosLayout = new javax.swing.GroupLayout(painelLancamentos);
        painelLancamentos.setLayout(painelLancamentosLayout);
        painelLancamentosLayout.setHorizontalGroup(
            painelLancamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLancamentosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelLancamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelCobrancaBase, javax.swing.GroupLayout.PREFERRED_SIZE, 551, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelBoletos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelLancamentosLayout.setVerticalGroup(
            painelLancamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelLancamentosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelCobrancaBase)
                .addGap(9, 9, 9)
                .addComponent(painelBoletos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Lançamentos", painelLancamentos);

        tabelaInadimplentes.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaInadimplentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabelaInadimplentes);

        jLabel4.setText("Vencimento Prorrogado ");

        btnImprimirBoletoInadimplente.setText("Imprimir Boleto 2ª via");

        btnEfetuarAcordo.setText("Fazer Acordo");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtVencimentoProrrogado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addComponent(btnImprimirBoletoInadimplente)
                .addGap(33, 33, 33)
                .addComponent(btnEfetuarAcordo)
                .addContainerGap(59, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnImprimirBoletoInadimplente)
                        .addComponent(btnEfetuarAcordo))
                    .addComponent(txtVencimentoProrrogado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelInadimplentesLayout = new javax.swing.GroupLayout(painelInadimplentes);
        painelInadimplentes.setLayout(painelInadimplentesLayout);
        painelInadimplentesLayout.setHorizontalGroup(
            painelInadimplentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelInadimplentesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelInadimplentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelInadimplentesLayout.setVerticalGroup(
            painelInadimplentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInadimplentesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Inadimplência", painelInadimplentes);

        tabelaPagos.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaPagos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(tabelaPagos);

        jLabel2.setText("Data Inicial ");

        jLabel3.setText("Data Final ");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(136, 136, 136)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(152, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelPagosLayout = new javax.swing.GroupLayout(painelPagos);
        painelPagos.setLayout(painelPagosLayout);
        painelPagosLayout.setHorizontalGroup(
            painelPagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPagosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelPagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelPagosLayout.setVerticalGroup(
            painelPagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPagosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pagos", painelPagos);

        tabelaArquivoRetorno.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane6.setViewportView(tabelaArquivoRetorno);

        btnLerArquivoRetorno.setText("Ler Arquivo(s)");

        btnConfirmar.setText("Confirmar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnLerArquivoRetorno)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnConfirmar)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLerArquivoRetorno)
                    .addComponent(btnConfirmar))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Arquivo Retorno", jPanel3);

        painelMensagem.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtMensagem1.setName("Valor"); // NOI18N

        jLabel6.setText("Instrução 1");

        btnSalvarMensagem.setText("Salvar");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(239, Short.MAX_VALUE)
                .addComponent(btnSalvarMensagem)
                .addGap(224, 224, 224))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSalvarMensagem)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel7.setText("Instrução 2");

        txtMensagem2.setName("Valor"); // NOI18N

        jLabel8.setText("Instrução 3");

        txtMensagem3.setName("Valor"); // NOI18N

        jLabel9.setText("Instrução 4");

        txtMensagem4.setName("Valor"); // NOI18N

        jLabel10.setText("Instrução 5");

        txtMensagem5.setName("Valor"); // NOI18N

        jLabel11.setText("Instrução 6");

        txtMensagem6.setName("Valor"); // NOI18N

        jLabel12.setText("Instrução 7");

        txtMensagem7.setName("Valor"); // NOI18N

        jLabel13.setText("Instrução 8");

        txtMensagem8.setName("Valor"); // NOI18N

        javax.swing.GroupLayout painelMensagemLayout = new javax.swing.GroupLayout(painelMensagem);
        painelMensagem.setLayout(painelMensagemLayout);
        painelMensagemLayout.setHorizontalGroup(
            painelMensagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelMensagemLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelMensagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtMensagem1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtMensagem8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        painelMensagemLayout.setVerticalGroup(
            painelMensagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMensagemLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMensagem8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelMensagem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelMensagem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Mensagens", jPanel4);

        tabelaAcordo.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaAcordo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane7.setViewportView(tabelaAcordo);

        painelDetalheAcordo.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tabelaCobrancasOriginais.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCobrancasOriginais.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane8.setViewportView(tabelaCobrancasOriginais);

        jLabel5.setText("Cobranças Originais");

        jLabel14.setText("Cobranças Geradas");

        tabelaCobrancasGeradas.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCobrancasGeradas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane9.setViewportView(tabelaCobrancasGeradas);

        btnEsconderPainel.setText("Fechar");

        javax.swing.GroupLayout painelDetalheAcordoLayout = new javax.swing.GroupLayout(painelDetalheAcordo);
        painelDetalheAcordo.setLayout(painelDetalheAcordoLayout);
        painelDetalheAcordoLayout.setHorizontalGroup(
            painelDetalheAcordoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDetalheAcordoLayout.createSequentialGroup()
                .addGroup(painelDetalheAcordoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDetalheAcordoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(painelDetalheAcordoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                            .addComponent(jLabel14)))
                    .addGroup(painelDetalheAcordoLayout.createSequentialGroup()
                        .addGap(242, 242, 242)
                        .addComponent(btnEsconderPainel))
                    .addGroup(painelDetalheAcordoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(painelDetalheAcordoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                            .addComponent(jLabel5))))
                .addContainerGap())
        );
        painelDetalheAcordoLayout.setVerticalGroup(
            painelDetalheAcordoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDetalheAcordoLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEsconderPainel)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addComponent(painelDetalheAcordo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelDetalheAcordo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Acordo", jPanel7);

        btnLimpar.setText("Limpar Seleção");

        btnCarregarInformacoesCobranca.setText("Carregar Básicas");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(btnLimpar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCarregarInformacoesCobranca))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(painelCondominos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 573, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLimpar)
                            .addComponent(btnCarregarInformacoesCobranca))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(painelCondominos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCarregarInformacoesCobranca;
    private javax.swing.JButton btnConfirmar;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnEfetuarAcordo;
    private javax.swing.JButton btnEsconderPainel;
    private javax.swing.JButton btnGerarCobranca;
    private javax.swing.JButton btnGerarCobrancaAvulsa;
    private javax.swing.JButton btnImprimirBoleto;
    private javax.swing.JButton btnImprimirBoletoAvulso;
    private javax.swing.JButton btnImprimirBoletoInadimplente;
    private javax.swing.JButton btnIncluirItemAvulso;
    private javax.swing.JButton btnLerArquivoRetorno;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnSalvarMensagem;
    private javax.swing.JMenuItem itemMenuBaixaManual;
    private javax.swing.JMenuItem itemMenuBaixaManualInadimplente;
    private javax.swing.JMenuItem itemMenuCalcularJurosMulta;
    private javax.swing.JMenuItem itemMenuCobrancasExistentesAVencerAnalitico;
    private javax.swing.JMenuItem itemMenuCobrancasExistentesAVencerSintetico;
    private javax.swing.JMenuItem itemMenuExibirDetalheAcordo;
    private javax.swing.JMenuItem itemMenuImprimirCartaAnalitica;
    private javax.swing.JMenuItem itemMenuImprimirCartaSintetica;
    private javax.swing.JMenuItem itemMenuImprimirCertificadoQuitacao;
    private javax.swing.JMenuItem itemMenuImprimirDetalheAcordo;
    private javax.swing.JMenuItem itemMenuImprimirEnvelopeP;
    private javax.swing.JMenuItem itemMenuImprimirInadimplenciaAnalitica;
    private javax.swing.JMenuItem itemMenuImprimirInadimplenciaSintetica;
    private javax.swing.JMenuItem itemMenuImprimirPagosAnalitico;
    private javax.swing.JMenuItem itemMenuImprimirPagosSintetico;
    private javax.swing.JMenuItem itemMenuImprimirRelacaoPostagem;
    private javax.swing.JMenuItem itemMenuImprimirRelacaoProprietarios;
    private javax.swing.JMenuItem itemMenuImprimirRelacaoProprietariosEmail;
    private javax.swing.JMenuItem itemMenuImprimirRelacaoProprietariosUnidade;
    private javax.swing.JMenuItem itemMenuLancarNoCaixa;
    private javax.swing.JMenuItem itemMenuLimparDadosAvulsos;
    private javax.swing.JMenuItem itemMenuOcultar;
    private javax.swing.JMenuItem itemMenuOcultarDados;
    private javax.swing.JMenuItem itemMenuPresentesAE;
    private javax.swing.JMenuItem itemMenuPresentesAO;
    private javax.swing.JMenuItem itemMenuRelacaoFracoesIdeais;
    private javax.swing.JMenuItem itemMenuRemoverAcordo;
    private javax.swing.JMenuItem itemMenuRemoverItemCobranca;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelBoletos;
    private javax.swing.JTabbedPane painelCobrancaBase;
    private javax.swing.JPanel painelCondominos;
    private javax.swing.JPanel painelDadosAvulsos;
    private javax.swing.JPanel painelDetalheAcordo;
    private javax.swing.JPanel painelInadimplentes;
    private javax.swing.JPanel painelLancamentos;
    private javax.swing.JPanel painelMensagem;
    private javax.swing.JPanel painelPagos;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JPopupMenu popupMenuAcordo;
    private javax.swing.JPopupMenu popupMenuCobrancaBase;
    private javax.swing.JPopupMenu popupMenuCondominos;
    private javax.swing.JPopupMenu popupMenuDadosAvulsos;
    private javax.swing.JPopupMenu popupMenuInadimplentes;
    private javax.swing.JPopupMenu popupMenuPagos;
    private javax.swing.JTable tabelaAcordo;
    private javax.swing.JTable tabelaArquivoRetorno;
    private javax.swing.JTable tabelaCobrancas;
    private javax.swing.JTable tabelaCobrancasBase;
    private javax.swing.JTable tabelaCobrancasGeradas;
    private javax.swing.JTable tabelaCobrancasOriginais;
    private javax.swing.JTable tabelaCondominos;
    private javax.swing.JTable tabelaDadosAvulsos;
    private javax.swing.JTable tabelaInadimplentes;
    private javax.swing.JTable tabelaPagos;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private net.sf.nachocalendar.components.DateField txtDataVencimento;
    private net.sf.nachocalendar.components.DateField txtDataVencimentoAvulso;
    private javax.swing.JTextField txtFiltro;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtMensagem1;
    private javax.swing.JTextField txtMensagem2;
    private javax.swing.JTextField txtMensagem3;
    private javax.swing.JTextField txtMensagem4;
    private javax.swing.JTextField txtMensagem5;
    private javax.swing.JTextField txtMensagem6;
    private javax.swing.JTextField txtMensagem7;
    private javax.swing.JTextField txtMensagem8;
    private javax.swing.JTextField txtNumeroDocumento;
    private net.sf.nachocalendar.components.DateField txtVencimentoProrrogado;
    // End of variables declaration//GEN-END:variables
}
