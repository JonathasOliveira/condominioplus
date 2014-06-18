/*
 * Relatorios.java
 * 
 * Created on 16/08/2007, 09:07:11
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.Anotacao;
import condominioPlus.negocio.Banco;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.BoletoBancario;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.DadosCorrespondencia;
import condominioPlus.negocio.cobranca.agua.ContaAgua;
import condominioPlus.negocio.cobranca.agua.Rateio;
import condominioPlus.negocio.financeiro.DadosBoleto;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoAuxiliar;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.relatorios.TipoRelatorio;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.joda.time.DateTime;

/**
 *
 * @author Eugenia
 */
public class Relatorios implements Printable {

    public Class classpath;
    private URL caminhoImagem;

    public Relatorios() {
        classpath = this.getClass();
    }

    public void imprimir(String relatorio, HashMap parametros, List lista, boolean impressaoDireta, boolean mostrarLogo, URL caminho) {
        try {

            if (mostrarLogo) {
                if (caminho == null) {
                    caminhoImagem = getClass().getResource("/condominioPlus/recursos/imagens/logo.jpg");
                    parametros.put("logoEmpresa", caminhoImagem.toString());
                } else {
                    parametros.put("logoEmpresa", caminho.toString());
                }
            }

            String minuto = "";
            if (new DateTime().getMinuteOfHour() <= 9) {
                minuto = "0" + new DateTime().getMinuteOfHour();
            } else {
                minuto = "" + new DateTime().getMinuteOfHour();
            }

            parametros.put("hora", new DateTime().getHourOfDay() + ":" + minuto);

            parametros.put("dataEmissao", DataUtil.toString(new DateTime()));

            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(lista);
            JasperPrint jprint = JasperFillManager.fillReport(
                    obterRelatorio(relatorio), parametros, ds);

            if (impressaoDireta) {
                JasperPrintManager.printReport(jprint, false);
            } else {
                JasperViewer.viewReport(jprint, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Não foi possível executar a operação!",
                    "Falha de Impressão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JasperReport obterRelatorio(String nome) {
        URL url = getClass().getResource("/condominioPlus/relatorios/" + nome + ".jasper");
        JasperReport relatorio = null;
        try {
            relatorio = (JasperReport) JRLoader.loadObject(url);
        } catch (JRException ex) {
            ex.printStackTrace();
        }
        return relatorio;
    }

    /**
     * Os método print será usado para imprimir texto diretamente para a impressora sem usar
     * o JasperReports
     */
    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) { /* We have only one page, and 'page' is zero-based */
            return NO_SUCH_PAGE;
        }

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        /* Now we perform our rendering */
        g.drawString("Hello World", 100, 100);
        g.drawString("Thiago", 100, 110);

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }

    /**
     * O método imprimir que recebe um texto usará o método print e jogará o texto recebido para a
     * impressora do usuário diretamente
     */
    public void imprimir(String texto) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void imprimirCarta(Unidade u, DateTime dataInicial, DateTime dataFinal, TipoRelatorio tipo) {
        List<HashMap<String, Object>> listaCobrancas = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", u.getCondominio().getRazaoSocial());
        parametros.put("nome", u.getCondomino().getNome());
        parametros.put("unidade", u.getUnidade());

        BigDecimal totalOriginal = new BigDecimal(0);
        BigDecimal totalJuros = new BigDecimal(0);
        BigDecimal totalMulta = new BigDecimal(0);
        BigDecimal totalGeral = new BigDecimal(0);

        for (Cobranca co : u.getCobrancas()) {
            if (co.getDataPagamento() == null && DataUtil.compararData(dataInicial, DataUtil.getDateTime(co.getDataVencimento())) == -1 && DataUtil.compararData(dataFinal, DataUtil.getDateTime(co.getDataVencimento())) == 1 && co.isExibir()) {
                HashMap<String, Object> mapa = new HashMap();
                totalOriginal = totalOriginal.add(co.getValorOriginal());
                totalJuros = totalJuros.add(co.getJuros());
                totalMulta = totalMulta.add(co.getMulta());
                totalGeral = totalGeral.add(co.getValorTotal());
                mapa.put("documento", co.getNumeroDocumento());
                mapa.put("vencimento", DataUtil.toString(co.getDataVencimento()));
                mapa.put("valorOriginal", PagamentoUtil.formatarMoeda(co.getValorOriginal().doubleValue()));
                mapa.put("juros", PagamentoUtil.formatarMoeda(co.getJuros().doubleValue()));
                mapa.put("multa", PagamentoUtil.formatarMoeda(co.getMulta().doubleValue()));
                mapa.put("total", PagamentoUtil.formatarMoeda(co.getValorTotal().doubleValue()));

                List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();

                if (tipo == TipoRelatorio.CARTA_ANALITICA) {
                    for (Pagamento pagamento : co.getPagamentos()) {
                        HashMap<String, String> mapa2 = new HashMap();
                        mapa2.put("descricao", pagamento.getDescricao().equals(" ") ? pagamento.getHistorico() : pagamento.getDescricao());
                        mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                        listaPagamentos.add(mapa2);
                    }
                }

                mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));

                listaCobrancas.add(mapa);
            }
        }

        parametros.put("totalOriginal", PagamentoUtil.formatarMoeda(totalOriginal.doubleValue()));
        parametros.put("totalJuros", PagamentoUtil.formatarMoeda(totalJuros.doubleValue()));
        parametros.put("totalMulta", PagamentoUtil.formatarMoeda(totalMulta.doubleValue()));
        parametros.put("totalGeral", PagamentoUtil.formatarMoeda(totalGeral.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");

        if (!listaCobrancas.isEmpty()) {
            if (tipo == TipoRelatorio.CARTA_SINTETICA) {
                imprimir("RelatorioCartaSintetica", parametros, listaCobrancas, false, true, null);
            } else if (tipo == TipoRelatorio.CARTA_ANALITICA) {
                parametros.put("subrelatorio", caminho.toString());
                imprimir("RelatorioCartaAnalitica", parametros, listaCobrancas, false, true, null);
            }
        }
    }

    public void imprimirRelatorioInadimplencia(Condominio condominio, List<Unidade> unidades, DateTime dataInicial, DateTime dataFinal, DateTime dataCalculo, TipoRelatorio tipo) {
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> parametros = new HashMap();
        List<Unidade> listaUnidades = new ArrayList<Unidade>();

        BigDecimal somaValorOriginal = new BigDecimal(0);
        BigDecimal somaJuros = new BigDecimal(0);
        BigDecimal somaMulta = new BigDecimal(0);
        BigDecimal somaTotalGeral = new BigDecimal(0);

        if (unidades == null) {
            listaUnidades = ordenarUnidades(condominio.getUnidades());
        } else {
            listaUnidades = ordenarUnidades(unidades);
        }

        UNIDADES:
        for (Unidade u : listaUnidades) {

            List<HashMap<String, Object>> listaCobrancas = new ArrayList<HashMap<String, Object>>();

            BigDecimal totalOriginal = new BigDecimal(0);
            BigDecimal totalJuros = new BigDecimal(0);
            BigDecimal totalMulta = new BigDecimal(0);
            BigDecimal totalGeral = new BigDecimal(0);

            List<Cobranca> cobrancas = new ArrayList<Cobranca>();
            cobrancas = getCobrancas(u);

            for (Cobranca co : cobrancas) {
                Cobranca cobrancaAux = new Cobranca();
                cobrancaAux.setValorOriginal(co.getValorOriginal());
                cobrancaAux.setJuros(co.getJuros());
                cobrancaAux.setMulta(co.getMulta());
                cobrancaAux.setValorTotal(co.getValorTotal());
                cobrancaAux.setDataVencimento(co.getDataVencimento());
                cobrancaAux.setDataPagamento(co.getDataPagamento());
                cobrancaAux.setExibir(co.isExibir());
                cobrancaAux.setNumeroDocumento(co.getNumeroDocumento());
                cobrancaAux.setPagamentos(co.getPagamentos());
                if (cobrancaAux.getDataPagamento() == null && DataUtil.compararData(dataInicial, DataUtil.getDateTime(cobrancaAux.getDataVencimento())) == -1 && DataUtil.compararData(dataFinal, DataUtil.getDateTime(cobrancaAux.getDataVencimento())) == 1 && cobrancaAux.isExibir()) {
                    calcularJurosMulta(cobrancaAux, dataCalculo);
                    HashMap<String, Object> mapa = new HashMap();
                    totalOriginal = totalOriginal.add(cobrancaAux.getValorOriginal());
                    totalJuros = totalJuros.add(cobrancaAux.getJuros());
                    totalMulta = totalMulta.add(cobrancaAux.getMulta());
                    totalGeral = totalGeral.add(cobrancaAux.getValorTotal());

                    somaValorOriginal = somaValorOriginal.add(cobrancaAux.getValorOriginal());
                    somaJuros = somaJuros.add(cobrancaAux.getJuros());
                    somaMulta = somaMulta.add(cobrancaAux.getMulta());
                    somaTotalGeral = somaTotalGeral.add(cobrancaAux.getValorTotal());

                    mapa.put("documento", cobrancaAux.getNumeroDocumento());
                    mapa.put("vencimento", DataUtil.toString(cobrancaAux.getDataVencimento()));
                    mapa.put("valorOriginal", PagamentoUtil.formatarMoeda(cobrancaAux.getValorOriginal().doubleValue()));
                    mapa.put("juros", PagamentoUtil.formatarMoeda(cobrancaAux.getJuros().doubleValue()));
                    mapa.put("multa", PagamentoUtil.formatarMoeda(cobrancaAux.getMulta().doubleValue()));
                    mapa.put("total", PagamentoUtil.formatarMoeda(cobrancaAux.getValorTotal().doubleValue()));

                    List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();

                    if (tipo == TipoRelatorio.INADIMPLENCIA_ANALITICA) {
                        for (Pagamento pagamento : cobrancaAux.getPagamentos()) {
                            HashMap<String, String> mapa2 = new HashMap();
                            mapa2.put("descricao", pagamento.getDescricao().equals(" ") ? pagamento.getHistorico() : pagamento.getDescricao());
                            mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                            listaPagamentos.add(mapa2);
                        }
                    }

                    mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));

                    listaCobrancas.add(mapa);
                }
            }
            if (listaCobrancas.isEmpty()) {
                continue UNIDADES;
            } else {
                HashMap<String, Object> mapa2 = new HashMap();
                mapa2.put("unidade", u.getUnidade());
                mapa2.put("nome", u.getCondomino().getNome());
                mapa2.put("totalOriginal", PagamentoUtil.formatarMoeda(totalOriginal.doubleValue()));
                mapa2.put("totalJuros", PagamentoUtil.formatarMoeda(totalJuros.doubleValue()));
                mapa2.put("totalMulta", PagamentoUtil.formatarMoeda(totalMulta.doubleValue()));
                mapa2.put("totalGeral", PagamentoUtil.formatarMoeda(totalGeral.doubleValue()));
                mapa2.put("lista", new JRBeanCollectionDataSource(listaCobrancas));
                lista.add(mapa2);
            }
        }

        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", condominio.getRazaoSocial());
        parametros.put("dataCalculo", DataUtil.toString(dataCalculo));
        parametros.put("somaValorOriginal", PagamentoUtil.formatarMoeda(somaValorOriginal.doubleValue()));
        parametros.put("somaJuros", PagamentoUtil.formatarMoeda(somaJuros.doubleValue()));
        parametros.put("somaMulta", PagamentoUtil.formatarMoeda(somaMulta.doubleValue()));
        parametros.put("somaTotalGeral", PagamentoUtil.formatarMoeda(somaTotalGeral.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (tipo == TipoRelatorio.INADIMPLENCIA_ANALITICA) {
            imprimir("InadimplenciaAnalitica", parametros, lista, false, true, null);
        } else if (tipo == TipoRelatorio.INADIMPLENCIA_SINTETICA) {
            imprimir("InadimplenciaSintetica", parametros, lista, false, true, null);
        }
    }

    public void imprimirRelatorioPagamentosEfetuados(Condominio condominio, List<Unidade> unidades, DateTime dataInicial, DateTime dataFinal, TipoRelatorio tipo) {
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> parametros = new HashMap();
        List<Unidade> listaUnidades = new ArrayList<Unidade>();

        BigDecimal somaValorOriginal = new BigDecimal(0);
        BigDecimal somaJuros = new BigDecimal(0);
        BigDecimal somaMulta = new BigDecimal(0);
        BigDecimal somaTotalGeral = new BigDecimal(0);

        if (unidades == null) {
            listaUnidades = ordenarUnidades(condominio.getUnidades());
        } else {
            listaUnidades = ordenarUnidades(unidades);
        }

        UNIDADES:
        for (Unidade u : listaUnidades) {
            BigDecimal somaGeral = new BigDecimal(0);
            List<HashMap<String, Object>> listaCobrancas = new ArrayList<HashMap<String, Object>>();

            List<Cobranca> cobrancas = new ArrayList<Cobranca>();
            cobrancas = getCobrancas(u);

            for (Cobranca co : cobrancas) {
                if (co.getDataPagamento() != null && DataUtil.compararData(dataInicial, DataUtil.getDateTime(co.getDataPagamento())) == -1 && DataUtil.compararData(dataFinal, DataUtil.getDateTime(co.getDataPagamento())) == 1 && co.isExibir()) {
                    HashMap<String, Object> mapa = new HashMap();

                    somaGeral = somaGeral.add(co.getValorPago());

                    somaValorOriginal = somaValorOriginal.add(co.getValorTotal());
                    somaJuros = somaJuros.add(co.getJuros());
                    somaMulta = somaMulta.add(co.getMulta());
                    somaTotalGeral = somaTotalGeral.add(co.getValorPago());

                    mapa.put("documento", co.getNumeroDocumento());
                    mapa.put("valor", PagamentoUtil.formatarMoeda(co.getValorTotal().doubleValue()));
                    mapa.put("juros", PagamentoUtil.formatarMoeda(co.getJuros().doubleValue()));
                    mapa.put("multa", PagamentoUtil.formatarMoeda(co.getMulta().doubleValue()));
                    mapa.put("vencimento", DataUtil.toString(co.getDataVencimento()));
                    mapa.put("dataPagamento", DataUtil.toString(co.getDataPagamento()));
                    mapa.put("valorPago", PagamentoUtil.formatarMoeda(co.getValorPago().doubleValue()));

                    List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();

                    if (tipo == TipoRelatorio.PAGAMENTOS_EFETUADOS_ANALITICO) {
                        for (Pagamento pagamento : co.getPagamentos()) {
                            HashMap<String, String> mapa2 = new HashMap();
                            mapa2.put("descricao", pagamento.getDescricao().equals(" ") ? pagamento.getHistorico() : pagamento.getDescricao());
                            mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                            listaPagamentos.add(mapa2);
                        }
                    }

                    mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));

                    listaCobrancas.add(mapa);
                }
            }
            if (listaCobrancas.isEmpty()) {
                continue UNIDADES;
            } else {
                HashMap<String, Object> mapa2 = new HashMap();
                mapa2.put("unidade", u.getUnidade());
                mapa2.put("nome", u.getCondomino().getNome());
                mapa2.put("totalGeral", PagamentoUtil.formatarMoeda(somaGeral.doubleValue()));
                mapa2.put("lista", new JRBeanCollectionDataSource(listaCobrancas));
                lista.add(mapa2);
            }
        }

        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", condominio.getRazaoSocial());
        parametros.put("somaValorOriginal", PagamentoUtil.formatarMoeda(somaValorOriginal.doubleValue()));
        parametros.put("somaJuros", PagamentoUtil.formatarMoeda(somaJuros.doubleValue()));
        parametros.put("somaMulta", PagamentoUtil.formatarMoeda(somaMulta.doubleValue()));
        parametros.put("somaTotalGeral", PagamentoUtil.formatarMoeda(somaTotalGeral.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (tipo == TipoRelatorio.PAGAMENTOS_EFETUADOS_ANALITICO) {
            imprimir("RelatorioPagamentosEfetuadosAnalitico", parametros, lista, false, true, null);
        } else if (tipo == TipoRelatorio.PAGAMENTOS_EFETUADOS_SINTETICO) {
            imprimir("RelatorioPagamentosEfetuadosSintetico", parametros, lista, false, true, null);
        }
    }

    public void imprimirCobrancasExistentesAVencer(Condominio condominio, List<Unidade> unidades, TipoRelatorio tipo) {
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> parametros = new HashMap();
        List<Unidade> listaUnidades = new ArrayList<Unidade>();

        BigDecimal somaValorOriginal = new BigDecimal(0);
        BigDecimal somaJuros = new BigDecimal(0);
        BigDecimal somaMulta = new BigDecimal(0);
        BigDecimal somaTotalGeral = new BigDecimal(0);

        if (unidades == null) {
            listaUnidades = ordenarUnidades(condominio.getUnidades());
        } else {
            listaUnidades = ordenarUnidades(unidades);
        }

        UNIDADES:
        for (Unidade u : listaUnidades) {

            List<HashMap<String, Object>> listaCobrancas = new ArrayList<HashMap<String, Object>>();

            BigDecimal totalOriginal = new BigDecimal(0);
            BigDecimal totalJuros = new BigDecimal(0);
            BigDecimal totalMulta = new BigDecimal(0);
            BigDecimal totalGeral = new BigDecimal(0);

            List<Cobranca> cobrancas = new ArrayList<Cobranca>();
            cobrancas = getCobrancas(u);

            for (Cobranca co : cobrancas) {
                if (co.getDataPagamento() == null && DataUtil.compararData(DataUtil.hoje(), DataUtil.getDateTime(co.getDataVencimento())) == -1 && co.isExibir()) {
                    HashMap<String, Object> mapa = new HashMap();
                    totalOriginal = totalOriginal.add(co.getValorOriginal());
                    totalJuros = totalJuros.add(co.getJuros());
                    totalMulta = totalMulta.add(co.getMulta());
                    totalGeral = totalGeral.add(co.getValorTotal());

                    somaValorOriginal = somaValorOriginal.add(co.getValorOriginal());
                    somaJuros = somaJuros.add(co.getJuros());
                    somaMulta = somaMulta.add(co.getMulta());
                    somaTotalGeral = somaTotalGeral.add(co.getValorTotal());

                    mapa.put("documento", co.getNumeroDocumento());
                    mapa.put("vencimento", DataUtil.toString(co.getDataVencimento()));
                    mapa.put("valorOriginal", PagamentoUtil.formatarMoeda(co.getValorOriginal().doubleValue()));
                    mapa.put("juros", PagamentoUtil.formatarMoeda(co.getJuros().doubleValue()));
                    mapa.put("multa", PagamentoUtil.formatarMoeda(co.getMulta().doubleValue()));
                    mapa.put("total", PagamentoUtil.formatarMoeda(co.getValorTotal().doubleValue()));

                    List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();

                    if (tipo == TipoRelatorio.COBRANCAS_EXISTENTES_A_VENCER_ANALITICO) {
                        for (Pagamento pagamento : co.getPagamentos()) {
                            HashMap<String, String> mapa2 = new HashMap();
                            mapa2.put("descricao", pagamento.getDescricao().equals(" ") ? pagamento.getHistorico() : pagamento.getDescricao());
                            mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                            listaPagamentos.add(mapa2);
                        }
                    }

                    mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));

                    listaCobrancas.add(mapa);
                }
            }
            if (listaCobrancas.isEmpty()) {
                continue UNIDADES;
            } else {
                HashMap<String, Object> mapa2 = new HashMap();
                mapa2.put("unidade", u.getUnidade());
                mapa2.put("nome", u.getCondomino().getNome());
                mapa2.put("totalOriginal", PagamentoUtil.formatarMoeda(totalOriginal.doubleValue()));
                mapa2.put("totalJuros", PagamentoUtil.formatarMoeda(totalJuros.doubleValue()));
                mapa2.put("totalMulta", PagamentoUtil.formatarMoeda(totalMulta.doubleValue()));
                mapa2.put("totalGeral", PagamentoUtil.formatarMoeda(totalGeral.doubleValue()));
                mapa2.put("lista", new JRBeanCollectionDataSource(listaCobrancas));
                lista.add(mapa2);
            }
        }

        parametros.put("periodo", "");
        parametros.put("condominio", condominio.getRazaoSocial());
        parametros.put("somaValorOriginal", PagamentoUtil.formatarMoeda(somaValorOriginal.doubleValue()));
        parametros.put("somaJuros", PagamentoUtil.formatarMoeda(somaJuros.doubleValue()));
        parametros.put("somaMulta", PagamentoUtil.formatarMoeda(somaMulta.doubleValue()));
        parametros.put("somaTotalGeral", PagamentoUtil.formatarMoeda(somaTotalGeral.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (tipo == TipoRelatorio.COBRANCAS_EXISTENTES_A_VENCER_ANALITICO) {
            imprimir("CobrancasAVencerAnalitico", parametros, lista, false, true, null);
        } else if (tipo == TipoRelatorio.COBRANCAS_EXISTENTES_A_VENCER_SINTETICO) {
            imprimir("CobrancasAVencerSintetico", parametros, lista, false, true, null);
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
        }
        cobranca.setJuros(juros.bigDecimalValue().setScale(2, RoundingMode.UP));
        cobranca.setMulta(multa.bigDecimalValue().setScale(2, RoundingMode.UP));
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getJuros().add(cobranca.getMulta().add(diferenca.bigDecimalValue()))).setScale(2, RoundingMode.UP));
    }

    private List<Unidade> ordenarUnidades(List<Unidade> lista) {
        List<Unidade> listaUnidades = lista;

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

    private List<Cobranca> getCobrancas(Unidade unidade) {
        List<Cobranca> listaCobrancas = unidade.getCobrancas();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Cobranca e1 = (Cobranca) o1;
                Cobranca e2 = (Cobranca) o2;
                return e1.getDataVencimento().compareTo(e2.getDataVencimento());
            }
        };

        Collections.sort(listaCobrancas, c);

        return listaCobrancas;
    }

    public void imprimirEnvelope(boolean imprimirRemetente, DateTime dataVencimento, Condominio condominio, List<Unidade> unidades, boolean imprimirInquilino, boolean imprimirProprietario) {
        List<HashMap<String, String>> listaCondominos = new ArrayList<HashMap<String, String>>();

        HashMap<String, Object> parametros = new HashMap();

        List<DadosCorrespondencia> listaDados = new ArrayList<DadosCorrespondencia>();
        for (Unidade unidade : unidades) {
            listaDados = DadosCorrespondencia.preencherLista(unidade, listaDados, imprimirProprietario, imprimirInquilino, null);
        }

        for (DadosCorrespondencia dados : listaDados) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("nome", dados.getNome().toUpperCase());

            mapa.put("endereco", dados.getLogradouro() + ", " + dados.getNumero() + " - " + dados.getComplemento());
            mapa.put("bairro", dados.getBairro());
            mapa.put("cidade", dados.getCidade() + " - " + dados.getEstado());
            mapa.put("cep", dados.getCep());

            mapa.put("inquilino", dados.isInquilino() ? "(Inquilino)" : "");
            mapa.put("condominio", dados.getCondominio() + " " + dados.getUnidade());
            mapa.put("dataVencimento", dataVencimento == null ? " " : "VENCIMENTO: " + DataUtil.toString(dataVencimento));
            listaCondominos.add(mapa);
        }

        if (!listaCondominos.isEmpty()) {
            imprimir("EnvelopePequeno", parametros, listaCondominos, false, imprimirRemetente, null);
        }
    }

    public void imprimirRelacaoProprietarios(boolean imprimirRemetente, DateTime dataVencimento, Condominio condominio, List<Unidade> unidades, TipoRelatorio tipo) {
        List<HashMap<String, String>> listaCondominos = new ArrayList<HashMap<String, String>>();

        HashMap<String, Object> parametros = new HashMap();

        // parametro para o relatório Relacao Proprietarios
        parametros.put("condominio", condominio.getRazaoSocial());

        for (Unidade unidade : unidades) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("nome", unidade.getCondomino().getNome());

            for (Endereco e : unidade.getCondomino().getEnderecos()) {
                if (e.isPadrao()) {
                    mapa.put("endereco", e.getLogradouro() + ", " + e.getNumero() + " - " + e.getComplemento());
                    mapa.put("bairro", e.getBairro());
                    mapa.put("cidade", e.getCidade() + " - " + e.getEstado());
                    mapa.put("cep", e.getCep());
                }
            }

            // campos para o relatório Relacao Proprietarios
            String telefone = "";
            for (Telefone t : unidade.getCondomino().getTelefones()) {
                if (t.getTipo().equals("Fixo")) {
                    telefone = t.getNumero();
                } else if (t.getTipo().equals("Celular")) {
                    telefone = telefone + " / " + t.getNumero();
                    mapa.put("telefone", telefone);
                } else if (t.getTipo().equals("Comercial")) {
                    mapa.put("telcomercial", t.getNumero());
                } else if (t.getTipo().equals("Fax")) {
                    mapa.put("fax", t.getNumero());
                }
            }
            mapa.put("unidade", unidade.getUnidade());
            mapa.put("email", unidade.getCondomino().getEmail());
            // campos para o relatório Relacao Proprietarios       

            mapa.put("condominio", unidade.getCondominio().getRazaoSocial() + " " + unidade.getUnidade());
            mapa.put("dataVencimento", dataVencimento == null ? " " : "VENCIMENTO: " + DataUtil.toString(dataVencimento));
            listaCondominos.add(mapa);
        }

        if (!listaCondominos.isEmpty()) {
            if (tipo == TipoRelatorio.RELACAO_PROPRIETARIOS) {
                imprimir("RelatorioRelacaoProprietarios", parametros, listaCondominos, false, imprimirRemetente, null);
            } else if (tipo == TipoRelatorio.RELACAO_PROPRIETARIOS_EMAIL) {
                imprimir("RelatorioRelacaoProprietariosEmail", parametros, listaCondominos, false, imprimirRemetente, null);
            } else if (tipo == TipoRelatorio.RELACAO_PROPRIETARIOS_UNIDADE) {
                imprimir("RelatorioRelacaoProprietariosUnidade", parametros, listaCondominos, false, imprimirRemetente, null);
            } else if (tipo == TipoRelatorio.RELACAO_POSTAGEM) {
                imprimir("RelatorioRelacaoPostagem", parametros, listaCondominos, false, imprimirRemetente, null);
            }
        }
    }

    public void imprimirListaAssembleia(Condominio condominio, List<Unidade> unidades, DateTime data, TipoRelatorio tipo) {
        List<Unidade> listaUnidades = new ArrayList<Unidade>();
        listaUnidades = ordenarUnidades(unidades);

        List<HashMap<String, String>> lista = new ArrayList<HashMap<String, String>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("condominio", condominio.getRazaoSocial());

        for (Unidade unidade : listaUnidades) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("nome", unidade.getCondomino().getNome());
            mapa.put("unidade", unidade.getUnidade());
            lista.add(mapa);
        }

        parametros.put("nomeRelatorio", tipo.toString());
        parametros.put("data", DataUtil.toString(data));

        if (!lista.isEmpty()) {
            imprimir("RelatorioPresentesAO", parametros, lista, false, true, null);
        }
    }

    public void imprimirListaFracoesIdeais(Condominio condominio, List<Unidade> unidades) {

        BigDecimal somaFracao = new BigDecimal(0);

        List<Unidade> listaUnidades = new ArrayList<Unidade>();
        listaUnidades = ordenarUnidades(unidades);

        List<HashMap<String, String>> lista = new ArrayList<HashMap<String, String>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("condominio", condominio.getRazaoSocial());

        for (Unidade unidade : listaUnidades) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("nome", unidade.getCondomino().getNome());
            mapa.put("unidade", unidade.getUnidade());
            mapa.put("coeficiente", unidade.getCoeficiente());
            mapa.put("fracaoIdeal", String.valueOf(unidade.getFracaoIdeal()));
            somaFracao = somaFracao.add(new BigDecimal(unidade.getFracaoIdeal()));
            lista.add(mapa);
        }

        parametros.put("somaFracao", somaFracao.setScale(2).toString());

        if (!lista.isEmpty()) {
            imprimir("RelatorioRelacaoFracaoIdeal", parametros, lista, false, true, null);
        }
    }

    public void imprimirCertificadoQuitacao(Unidade u, DateTime dataFinal, boolean imprimirAssinaturaBreca, boolean imprimirObservacao) {
        URL caminhoMoldura = getClass().getResource("/condominioPlus/recursos/imagens/moldura.jpg");

        HashMap<String, Object> parametros = new HashMap();

        parametros.put("caminhoMoldura", caminhoMoldura.toString());

        if (imprimirAssinaturaBreca) {
            parametros.put("nomeAssinatura", "Carlos Alberto Costa de Araujo Goes");
            parametros.put("crcAssinatura", "CRC/RJ 428385/0-0");
            parametros.put("craAssinatura", "CPF 260.412.867-53");
            parametros.put("cpfAssinatura", "");
        } else {
            parametros.put("nomeAssinatura", "Artur Mureb de Araujo Goes");
            parametros.put("crcAssinatura", "CRC/RJ 088600/0-0");
            parametros.put("craAssinatura", "CRA/RJ 20-59303-1");
            parametros.put("cpfAssinatura", "CPF 086.799.077-58");
        }

        if (imprimirObservacao) {
            parametros.put("observacao", "Observação: Certificação negativa com efeito positivo.");
        }

        List<HashMap<String, String>> lista = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> mapa = new HashMap();
        mapa.put("unidade", u.getUnidade());
        mapa.put("nome", u.getCondomino().getNome());
        mapa.put("condominio", u.getCondominio().getRazaoSocial());
        mapa.put("data", DataUtil.toString(dataFinal));
        lista.add(mapa);

        imprimir("CertificadoQuitacao", parametros, lista, false, true, null);
    }

    public void imprimirBoleto(List<BoletoBancario> boletos, Condominio condominio) {
        List<HashMap<String, String>> lista = new ArrayList<HashMap<String, String>>();

        HashMap<String, Object> parametros = new HashMap();

        if (condominio.getContaBancaria().getBanco().getNumeroBanco().equals("033")) {
            URL logoSantander = getClass().getResource("/condominioPlus/recursos/imagens/santander_logo.jpg");
            parametros.put("logoSantander", logoSantander.toString());
        } else if (condominio.getContaBancaria().getBanco().getNumeroBanco().equals("237")) {
            URL logoBradesco = getClass().getResource("/condominioPlus/recursos/imagens/bradesco_logo.jpg");
            parametros.put("logoBradesco", logoBradesco.toString());
            URL simboloBradesco = getClass().getResource("/condominioPlus/recursos/imagens/simbolo_bradesco.jpg");
            parametros.put("simboloBradesco", simboloBradesco.toString());
        }

        for (BoletoBancario boleto : boletos) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("nomeCedente", boleto.getNomeCedente());
            mapa.put("cnpjCedente", boleto.getCnpjCedente());
            mapa.put("nomeSacado", boleto.getNomeSacado());

            //endereço sacado
            mapa.put("dadoscorrespondencia", boleto.getLogradouroSacado() + ", " + boleto.getNumeroSacado() + " / " + boleto.getComplementoSacado() + " - " + boleto.getBairroSacado());
            mapa.put("dadoscorrespondencia2", boleto.getCepSacado() + "   " + boleto.getCidadeSacado() + " - " + boleto.getUfSacado());

            mapa.put("agencia", boleto.getAgencia());
            mapa.put("codigoCedente", boleto.getCodigoCedente());
            
            if (condominio.getContaBancaria().getBanco().getNumeroBanco().equals("033")) {
                mapa.put("numeroDocumento", boleto.getNumeroDocumento());
            } else if (condominio.getContaBancaria().getBanco().getNumeroBanco().equals("237")) {
                String carteira = (boleto.getCarteira().length() == 1) ? "0" + boleto.getCarteira() : boleto.getCarteira();
                mapa.put("nossoNumero", boleto.getNumeroDocumento() + "-" + BoletoBancario.calculoDvNossoNumeroBradesco(carteira + boleto.getNumeroDocumento()));
                mapa.put("numeroDocumento", boleto.getNumeroDocumento());
            }
            
            mapa.put("dataDocumento", boleto.getDataDocumento());
            mapa.put("dataVencimento", boleto.getDataVencimento());
            mapa.put("tipoDocumento", boleto.getTipoDocumento());
            mapa.put("aceite", boleto.getAceite());
            mapa.put("carteira", (boleto.getCarteira().length() == 1) ? "0" + boleto.getCarteira() : boleto.getCarteira());
            mapa.put("especie", boleto.getEspecie());
            mapa.put("localPagamento", boleto.getLocalPagamento());
            mapa.put("valor", boleto.getValor());
            mapa.put("codigoBanco", boleto.getCodigoBanco());
            mapa.put("digitoBanco", boleto.getDigitoBanco());
            mapa.put("linhaDigitavel", boleto.getLinhaDigitavel());
            mapa.put("codigoDeBarras", boleto.getCodigoBarras());

            //preenchendo a lista de pagamentos
            int i = 0;
            for (Pagamento p : boleto.getPagamentos()) {
                i += 1;
                mapa.put("detalhe" + i, "   " + p.getDescricao());
                mapa.put("valordetalhe" + i, PagamentoUtil.formatarMoeda(p.getValor().doubleValue()) + "   ");
            }

            mapa.put("mensagem1", boleto.getMensagem1());
            mapa.put("mensagem2", boleto.getMensagem2());
            mapa.put("mensagem3", boleto.getMensagem3());
            mapa.put("mensagem4", boleto.getMensagem4());

            lista.add(mapa);
        }

        if (!lista.isEmpty()) {
            if (condominio.getContaBancaria().getBanco().getNumeroBanco().equals("033")) {
                imprimir("BoletoSantander", parametros, lista, false, true, null);
            } else if (condominio.getContaBancaria().getBanco().getNumeroBanco().equals("237")) {
                imprimir("BoletoBradesco", parametros, lista, false, true, null);
            }
        }
    }

    public void imprimirAnotacoes(Condominio condominio, Unidade unidade, List<Anotacao> anotacoes, TipoRelatorio tipo) {
//        List<Unidade> listaUnidades = new ArrayList<Unidade>();
//        listaUnidades = ordenarUnidades(unidades);

        List<HashMap<String, String>> lista = new ArrayList<HashMap<String, String>>();

        HashMap<String, Object> parametros = new HashMap();

        if (unidade != null) {
            parametros.put("condominio", condominio.getRazaoSocial() + " - " + unidade.getUnidade() + " " + unidade.getCondomino().getNome());
        } else {
            parametros.put("condominio", condominio.getRazaoSocial());
        }

        for (Anotacao anotacao : anotacoes) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("data", DataUtil.toString(anotacao.getData()));
            mapa.put("usuario", anotacao.getUsuario().getUsuario());
            mapa.put("assunto", anotacao.getAssunto());
            mapa.put("texto", anotacao.getTexto());
            lista.add(mapa);
        }

        parametros.put("nomeRelatorio", tipo.toString());
//        parametros.put("data", DataUtil.toString(data));

        if (!lista.isEmpty()) {
            imprimir("RelatorioAnotacoes", parametros, lista, false, true, null);
        }
    }

    public void imprimirExtratoContaCorrente(Condominio condominio, DateTime dataInicial, DateTime dataFinal, List<Pagamento> pagamentos) {
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", condominio.getRazaoSocial());

        BigDecimal saldoAnterior = new BigDecimal(0);
        BigDecimal creditos = new BigDecimal(0);
        BigDecimal debitos = new BigDecimal(0);
        BigDecimal saldoAtual = new BigDecimal(0);

        for (Pagamento p : pagamentos) {
            HashMap<String, Object> mapa = new HashMap();

            mapa.put("data", DataUtil.toString(p.getDataPagamento()));
            mapa.put("documento", getNumeroDocumento(p));
            mapa.put("codigoConta", p.getConta().getCodigo() + "");
            mapa.put("historico", p.getHistorico());
            mapa.put("valor", PagamentoUtil.formatarMoeda(p.getValor().doubleValue()));
            mapa.put("saldo", PagamentoUtil.formatarMoeda(p.getSaldo().doubleValue()));

            if (p.getValor().compareTo(new BigDecimal(0)) == -1) {
                debitos = debitos.add(p.getValor());
            } else {
                creditos = creditos.add(p.getValor());
            }

            saldoAtual = p.getSaldo();

            lista.add(mapa);
        }

        saldoAnterior = saldoAnterior.add(saldoAtual).subtract(creditos).subtract(debitos);

        parametros.put("saldoAnterior", PagamentoUtil.formatarMoeda(saldoAnterior.doubleValue()));
        parametros.put("creditos", PagamentoUtil.formatarMoeda(creditos.doubleValue()));
        parametros.put("debitos", PagamentoUtil.formatarMoeda(debitos.doubleValue()));
        parametros.put("saldoAtual", PagamentoUtil.formatarMoeda(saldoAtual.doubleValue()));

        if (!lista.isEmpty()) {
            imprimir("RelatorioExtratoContaCorrente", parametros, lista, false, true, null);
        }
    }

    public void imprimirExtratoConferenciaContaCorrente(Condominio condominio, DateTime dataInicial, DateTime dataFinal, List<Pagamento> pagamentos, TipoRelatorio tipo, String texto) {
        List<PagamentoAuxiliar> pagamentosAuxiliares = new ArrayList<PagamentoAuxiliar>();
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", condominio.getRazaoSocial());

        BigDecimal saldoAnterior = new BigDecimal(0);
        BigDecimal creditos = new BigDecimal(0);
        BigDecimal debitos = new BigDecimal(0);
        BigDecimal saldoAtual = new BigDecimal(0);

        //campo para calcular o saldo de cada pagamento e mostrar no relatorio
        BigDecimal saldoAuxiliar = new BigDecimal(0);

        for (Pagamento p : pagamentos) {
            boolean continuar = true;
            String numeroDocumento = getNumeroDocumento(p);
            PagamentoAuxiliar pa = null;

            if (pagamentosAuxiliares.isEmpty()) {
                pa = new PagamentoAuxiliar();
                pa.setNumeroDocumento(numeroDocumento);
                pa.adicionarPagamento(p);
            } else {
                for (PagamentoAuxiliar p1 : pagamentosAuxiliares) {
                    if (p1.getNumeroDocumento().equalsIgnoreCase(numeroDocumento)) {
                        p1.adicionarPagamento(p);
                        continuar = false;
                    } else {
                        pa = new PagamentoAuxiliar();
                        pa.setNumeroDocumento(numeroDocumento);
                        pa.adicionarPagamento(p);
                    }
                }
            }

            if (pa != null && continuar == true) {
                pagamentosAuxiliares.add(pa);
            }

            if (p.getValor().compareTo(new BigDecimal(0)) == -1) {
                debitos = debitos.add(p.getValor());
            } else {
                creditos = creditos.add(p.getValor());
            }

            saldoAtual = p.getSaldo();
        }

        saldoAnterior = saldoAnterior.add(saldoAtual).subtract(creditos).subtract(debitos);
        saldoAuxiliar = saldoAuxiliar.add(saldoAnterior);

        BigDecimal totalGeral = new BigDecimal(0);

        for (PagamentoAuxiliar p : pagamentosAuxiliares) {
            HashMap<String, Object> mapa = new HashMap();
            List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();
            BigDecimal soma = new BigDecimal(0);

            for (Pagamento pagamento : p.getListaPagamentos()) {
                saldoAuxiliar = saldoAuxiliar.add(pagamento.getValor());
                HashMap<String, String> mapa2 = new HashMap();
                mapa2.put("data", DataUtil.toString(pagamento.getDataPagamento()));
                mapa2.put("codigoConta", pagamento.getConta().getCodigo() + "");
                mapa2.put("historico", pagamento.getHistorico());
                mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                if (tipo == TipoRelatorio.EXTRATO_CONFERENCIA_CONTA_CORRENTE) {
                    mapa2.put("saldo", PagamentoUtil.formatarMoeda(saldoAuxiliar.doubleValue()));
                }
                listaPagamentos.add(mapa2);
                soma = soma.add(pagamento.getValor());
                totalGeral = totalGeral.add(pagamento.getValor());
            }

            mapa.put("documento", p.getNumeroDocumento());
            mapa.put("soma", PagamentoUtil.formatarMoeda(soma.doubleValue()));
            mapa.put("lista", new JRBeanCollectionDataSource(listaPagamentos));

            lista.add(mapa);
        }

        parametros.put("saldoAnterior", PagamentoUtil.formatarMoeda(saldoAnterior.doubleValue()));
        parametros.put("creditos", PagamentoUtil.formatarMoeda(creditos.doubleValue()));
        parametros.put("debitos", PagamentoUtil.formatarMoeda(debitos.doubleValue()));
        parametros.put("saldoAtual", PagamentoUtil.formatarMoeda(saldoAtual.doubleValue()));
        parametros.put("totalGeral", PagamentoUtil.formatarMoeda(totalGeral.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!lista.isEmpty()) {
            parametros.put("titulo", tipo.toString());
            if (tipo == TipoRelatorio.EXTRATO_CONFERENCIA_CONTA_CORRENTE) {
                imprimir("RelatorioExtratoConferenciaContaCorrente", parametros, lista, false, true, null);
            } else if (tipo == TipoRelatorio.EXTRATO_CUSTOMIZADO) {
                imprimir("RelatorioExtratoCustomizado", parametros, lista, false, true, null);
            } else if (tipo == TipoRelatorio.EXTRATO_PESQUISAR_CONTEUDO_CAIXA) {
                parametros.put("texto", texto);
                imprimir("RelatorioPesquisaConteudoCaixa", parametros, lista, false, true, null);
            }
        }
    }

    private String getNumeroDocumento(Pagamento p) {
        if (p.getForma() == FormaPagamento.BOLETO) {
            return ((DadosBoleto) p.getDadosPagamento()).getNumeroBoleto();
        } else if (p.getForma() == FormaPagamento.CHEQUE) {
            return String.valueOf(((DadosCheque) p.getDadosPagamento()).getNumero());
        } else {
            return String.valueOf(((DadosDOC) p.getDadosPagamento()).getNumeroDocumento());
        }
    }

    public void imprimirBalancete(Condominio condominio, DateTime dataInicial, DateTime dataFinal, List<Pagamento> pagamentos, TipoRelatorio tipo) {
        List<PagamentoAuxiliar> pagamentosAuxiliaresDebito = new ArrayList<PagamentoAuxiliar>();
        List<PagamentoAuxiliar> pagamentosAuxiliaresCredito = new ArrayList<PagamentoAuxiliar>();
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", condominio.getRazaoSocial());

        BigDecimal saldoAnterior = new BigDecimal(0);
        BigDecimal creditos = new BigDecimal(0);
        BigDecimal debitos = new BigDecimal(0);
        BigDecimal saldoAtual = new BigDecimal(0);

        //campo para calcular o saldo de cada pagamento e mostrar no relatorio
//        BigDecimal saldoAuxiliar = new BigDecimal(0);

        for (Pagamento p : pagamentos) {
            boolean continuar = true;
            int codigoConta = p.getConta().getCodigo();
            PagamentoAuxiliar pa = null;

            if (p.getConta().isCredito()) {
                if (pagamentosAuxiliaresCredito.isEmpty()) {
                    pa = new PagamentoAuxiliar();
                    pa.setCodigoConta(codigoConta);
                    pa.setNomeConta(p.getConta().getNome());
                    pa.adicionarPagamento(p);
                } else {
                    for (PagamentoAuxiliar p1 : pagamentosAuxiliaresCredito) {
                        if (p1.getCodigoConta() == codigoConta) {
                            p1.adicionarPagamento(p);
                            continuar = false;
                        } else {
                            pa = new PagamentoAuxiliar();
                            pa.setCodigoConta(codigoConta);
                            pa.setNomeConta(p.getConta().getNome());
                            pa.adicionarPagamento(p);
                        }
                    }
                }

                if (pa != null && continuar == true) {
                    pagamentosAuxiliaresCredito.add(pa);
                }
            } else {
                if (pagamentosAuxiliaresDebito.isEmpty()) {
                    pa = new PagamentoAuxiliar();
                    pa.setCodigoConta(codigoConta);
                    pa.setNomeConta(p.getConta().getNome());
                    pa.adicionarPagamento(p);
                } else {
                    for (PagamentoAuxiliar p1 : pagamentosAuxiliaresDebito) {
                        if (p1.getCodigoConta() == codigoConta) {
                            p1.adicionarPagamento(p);
                            continuar = false;
                        } else {
                            pa = new PagamentoAuxiliar();
                            pa.setCodigoConta(codigoConta);
                            pa.setNomeConta(p.getConta().getNome());
                            pa.adicionarPagamento(p);
                        }
                    }
                }

                if (pa != null && continuar == true) {
                    pagamentosAuxiliaresDebito.add(pa);
                }
            }

            if (p.getConta().isCredito()) {
                creditos = creditos.add(p.getValor());
            } else {
                debitos = debitos.add(p.getValor());
            }

            saldoAtual = p.getSaldo();
        }

        saldoAnterior = saldoAnterior.add(saldoAtual).subtract(creditos).subtract(debitos);
//        saldoAuxiliar = saldoAuxiliar.add(saldoAnterior);

        HashMap<String, Object> mapa = new HashMap();
        List<HashMap<String, Object>> listaCredito = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> listaDebito = new ArrayList<HashMap<String, Object>>();

        //preenchendo as listas para visualização do relatório//
        for (PagamentoAuxiliar p : ordenarPagamentosPorConta(pagamentosAuxiliaresCredito)) {
            listaCredito.add(preencherListaBalancete(p, tipo));
        }
        for (PagamentoAuxiliar p : ordenarPagamentosPorConta(pagamentosAuxiliaresDebito)) {
            listaDebito.add(preencherListaBalancete(p, tipo));
        }

        mapa.put("listaCredito", new JRBeanCollectionDataSource(listaCredito));
        mapa.put("listaDebito", new JRBeanCollectionDataSource(listaDebito));
        mapa.put("somaCredito", PagamentoUtil.formatarMoeda(creditos.doubleValue()));
        mapa.put("somaDebito", PagamentoUtil.formatarMoeda(debitos.doubleValue()));
        lista.add(mapa);

        BigDecimal totalSubRecursos = new BigDecimal(0);
        totalSubRecursos = totalSubRecursos.add(saldoAtual).add(condominio.getPoupanca().getSaldo()).add(condominio.getAplicacao().getSaldo()).add(condominio.getEmprestimo().getSaldo()).add(condominio.getConsignacao().getSaldo());

        List<Pagamento> contasAPagar = new DAO().listar("PagamentosContaPagar", condominio.getContaPagar());
        BigDecimal somaValorContasAPagar = new BigDecimal(0);
        PAGAMENTOS:
        for (Pagamento p : contasAPagar) {
            if (p.getConta().getNomeVinculo().equals("EM")) {
                continue PAGAMENTOS;
            }
            somaValorContasAPagar = somaValorContasAPagar.add(p.getValor());
        }

        BigDecimal deficitSuperavit = new BigDecimal(0);
        deficitSuperavit = deficitSuperavit.add(totalSubRecursos).add(somaValorContasAPagar);

        parametros.put("saldoAnterior", PagamentoUtil.formatarMoeda(saldoAnterior.doubleValue()));
        parametros.put("creditos", PagamentoUtil.formatarMoeda(creditos.doubleValue()));
        parametros.put("debitos", PagamentoUtil.formatarMoeda(debitos.doubleValue()));
        parametros.put("saldoAtual", PagamentoUtil.formatarMoeda(saldoAtual.doubleValue()));
        parametros.put("poupanca", PagamentoUtil.formatarMoeda(condominio.getPoupanca().getSaldo().doubleValue()));
        parametros.put("aplicacoes", PagamentoUtil.formatarMoeda(condominio.getAplicacao().getSaldo().doubleValue()));
        parametros.put("emprestimos", PagamentoUtil.formatarMoeda(condominio.getEmprestimo().getSaldo().doubleValue()));
        parametros.put("consignacoes", PagamentoUtil.formatarMoeda(condominio.getConsignacao().getSaldo().doubleValue()));
        parametros.put("pagamentosNaoEfetuados", PagamentoUtil.formatarMoeda(somaValorContasAPagar.doubleValue()));
        parametros.put("deficitSuperavit", PagamentoUtil.formatarMoeda(deficitSuperavit.doubleValue()));

        parametros.put("totalSubRecursos", PagamentoUtil.formatarMoeda(totalSubRecursos.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!lista.isEmpty()) {
            if (tipo == TipoRelatorio.BALANCETE_SINTETICO) {
                imprimir("BalanceteSintetico", parametros, lista, false, true, null);
            } else if (tipo == TipoRelatorio.BALANCETE_ANALITICO) {
                imprimir("BalanceteAnalitico", parametros, lista, false, true, null);
            }
        }
    }

    private List<PagamentoAuxiliar> ordenarPagamentosPorConta(List<PagamentoAuxiliar> lista) {
        List<PagamentoAuxiliar> listaPagamentos = lista;

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                PagamentoAuxiliar p1 = (PagamentoAuxiliar) o1;
                PagamentoAuxiliar p2 = (PagamentoAuxiliar) o2;
                return Integer.valueOf(p1.getCodigoConta()).compareTo(Integer.valueOf(p2.getCodigoConta()));
            }
        };

        Collections.sort(listaPagamentos, c);

        return listaPagamentos;
    }

    private HashMap<String, Object> preencherListaBalancete(PagamentoAuxiliar p, TipoRelatorio tipo) {
        HashMap<String, Object> mapa = new HashMap();
        List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();

        BigDecimal soma = new BigDecimal(0);

        for (Pagamento pagamento : p.getListaPagamentos()) {

            if (tipo == TipoRelatorio.BALANCETE_ANALITICO) {
                HashMap<String, String> mapa2 = new HashMap();
                mapa2.put("data", DataUtil.toString(pagamento.getDataPagamento()));
                mapa2.put("documento", getNumeroDocumento(pagamento));
                mapa2.put("historico", pagamento.getHistorico());
                mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                listaPagamentos.add(mapa2);
            }

            soma = soma.add(pagamento.getValor());
        }

        mapa.put("codigoConta", p.getCodigoConta() + "");
        mapa.put("nomeConta", p.getNomeConta().toUpperCase());
        mapa.put("somaConta", PagamentoUtil.formatarMoeda(soma.doubleValue()));
        mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));

        return mapa;
    }

    public void imprimirRecibo(Condominio condominio, Pagamento p) {
        HashMap<String, Object> parametros = new HashMap();
        List<Pagamento> pagamentos = new ArrayList<Pagamento>();
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

        PagamentoAuxiliar pa = new PagamentoAuxiliar();
        pa.setNumeroDocumento(getNumeroDocumento(p));

        pagamentos = new DAO().listar(Pagamento.class, "PagamentosContaCorrentePorNumeroDocumento", condominio.getContaCorrente(), p.getDataPagamento());

        List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();
        BigDecimal soma = new BigDecimal(0);

        for (Pagamento pagamento : pagamentos) {
            if (pa.getNumeroDocumento().equalsIgnoreCase(getNumeroDocumento(pagamento))) {
                HashMap<String, String> mapa2 = new HashMap();
                mapa2.put("descricao", pagamento.getHistorico());
                mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
                listaPagamentos.add(mapa2);

                soma = soma.add(pagamento.getValor());
            }
        }

        parametros.put("soma", PagamentoUtil.formatarMoeda(soma.doubleValue()));

        HashMap<String, Object> mapa = new HashMap();
        mapa.put("data", DataUtil.toString(p.getDataPagamento()));
        mapa.put("numeroDocumento", pa.getNumeroDocumento());
        mapa.put("condominio", condominio.getRazaoSocial());
        mapa.put("endereco", condominio.getEndereco().getLogradouro());
        mapa.put("cnpj", condominio.getCnpj());
        mapa.put("inscricaoEstadual", "");
        mapa.put("emissor", p.getFornecedor() == null ? "" : p.getFornecedor().getNome());
        mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));
        lista.add(mapa);

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!lista.isEmpty()) {
            imprimir("Recibo", parametros, lista, false, true, null);
        }
    }

    public void imprimirExtratoContaIndividual(Condominio condominio, DateTime dataInicial, DateTime dataFinal, List<Pagamento> pagamentos) {
        HashMap<String, Object> parametros = new HashMap();
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

        Pagamento p = new Pagamento();
        p = pagamentos.get(0);
        PagamentoAuxiliar pa = new PagamentoAuxiliar();
        pa.setNumeroDocumento(getNumeroDocumento(p));
        pa.setNomeConta(p.getConta().getNome());
        pa.setCodigoConta(p.getConta().getCodigo());

        List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();
        BigDecimal soma = new BigDecimal(0);

        for (Pagamento pagamento : pagamentos) {
            HashMap<String, String> mapa2 = new HashMap();
            mapa2.put("data", DataUtil.toString(pagamento.getDataPagamento()));
            mapa2.put("documento", getNumeroDocumento(pagamento));
            mapa2.put("historico", pagamento.getHistorico());
            mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));
            listaPagamentos.add(mapa2);

            soma = soma.add(pagamento.getValor());
        }

        HashMap<String, Object> mapa = new HashMap();
        mapa.put("codigoConta", pa.getCodigoConta() + "");
        mapa.put("nomeConta", pa.getNomeConta());
        mapa.put("somaConta", PagamentoUtil.formatarMoeda(soma.doubleValue()));
        mapa.put("listaPagamentos", new JRBeanCollectionDataSource(listaPagamentos));
        lista.add(mapa);

        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
        parametros.put("condominio", condominio.getRazaoSocial());

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!lista.isEmpty()) {
            imprimir("RelatorioExtratoContaIndividual", parametros, lista, false, true, null);
        }
    }

    public void imprimirExtratoChequesEmitidos(DateTime dataInicial, DateTime dataFinal, List<Pagamento> pagamentos, TipoRelatorio tipo, Banco banco) {
        List<PagamentoAuxiliar> pagamentosAuxiliares = new ArrayList<PagamentoAuxiliar>();
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));

        //campo para calcular o saldo de cada pagamento e mostrar no relatorio
        BigDecimal saldoAuxiliar = new BigDecimal(0);

        for (Pagamento p : pagamentos) {
            boolean continuar = true;
            Condominio condominio = p.getContaCorrente().getCondominio();
            PagamentoAuxiliar pa = null;

            if (pagamentosAuxiliares.isEmpty()) {
                pa = new PagamentoAuxiliar();
                pa.setNumeroDocumento(getNumeroDocumento(p));
                pa.setCondominio(condominio);
                pa.adicionarPagamento(p);
            } else {
                for (PagamentoAuxiliar p1 : pagamentosAuxiliares) {
                    if (p1.getCondominio() == condominio) {
                        p1.adicionarPagamento(p);
                        continuar = false;
                    } else {
                        pa = new PagamentoAuxiliar();
                        pa.setNumeroDocumento(getNumeroDocumento(p));
                        pa.setCondominio(condominio);
                        pa.adicionarPagamento(p);
                    }
                }
            }

            if (pa != null && continuar == true) {
                pagamentosAuxiliares.add(pa);
            }

        }

        BigDecimal totalGeral = new BigDecimal(0);

        for (PagamentoAuxiliar p : pagamentosAuxiliares) {
            HashMap<String, Object> mapa = new HashMap();
            List<HashMap<String, String>> listaPagamentos = new ArrayList<HashMap<String, String>>();
            BigDecimal soma = new BigDecimal(0);

            for (Pagamento pagamento : p.getListaPagamentos()) {
                saldoAuxiliar = saldoAuxiliar.add(pagamento.getValor());
                HashMap<String, String> mapa2 = new HashMap();
                mapa2.put("data", DataUtil.toString(pagamento.getDataPagamento()));
                mapa2.put("contaCorrente", p.getCondominio().getContaBancaria().getContaCorrente() + " " + p.getCondominio().getContaBancaria().getDigitoCorrente());
                mapa2.put("historico", pagamento.getHistorico());
                mapa2.put("numeroCheque", getNumeroDocumento(pagamento));
                mapa2.put("valor", PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue()));

                listaPagamentos.add(mapa2);
                soma = soma.add(pagamento.getValor());
                totalGeral = totalGeral.add(pagamento.getValor());
            }

            mapa.put("condominio", p.getCondominio().getRazaoSocial());
            mapa.put("soma", PagamentoUtil.formatarMoeda(soma.doubleValue()));
            mapa.put("lista", new JRBeanCollectionDataSource(listaPagamentos));

            lista.add(mapa);
        }

        parametros.put("numeroBanco", banco.getNumeroBanco());
        parametros.put("agencia", banco.getAgencia());
        parametros.put("contaMaster", banco.getContaMaster());
        parametros.put("totalGeral", PagamentoUtil.formatarMoeda(totalGeral.doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!lista.isEmpty()) {
            parametros.put("titulo", tipo.toString());
            imprimir("RelatorioExtratoChequesEmitidos", parametros, lista, false, true, null);
        }
    }

    public void imprimirRelatorioConsumoAgua(ContaAgua conta, String formaRateio, int qtdeM3InclusosTaxaCondominial, boolean possuiHidrometro, String formaRateioAreaComum, boolean consideraPipa) {
        HashMap<String, Object> parametros = new HashMap();

//      DADOS DO RATEIO
        BigDecimal somaConsumoM3 = new BigDecimal(0);
        BigDecimal somaConsumoACobrarM3 = new BigDecimal(0);
        BigDecimal somaPercentualAreaComum = new BigDecimal(0);
        BigDecimal somaConsumoM3AreaComum = new BigDecimal(0);
        BigDecimal somaConsumoUnidade = new BigDecimal(0);
        BigDecimal somaConsumoAreaComum = new BigDecimal(0);
        BigDecimal somaTotalACobrar = new BigDecimal(0);
        List<HashMap<String, String>> listaRateio = new ArrayList<HashMap<String, String>>();
        for (Rateio rateio : conta.getRateios()) {
            HashMap<String, String> mapa = new HashMap();
            mapa.put("unidade", rateio.getUnidade().getUnidade());
            mapa.put("fracaoIdeal", FormatadorNumeros.formatarDoubleToString(rateio.getUnidade().getFracaoIdeal(), "0.###"));
            mapa.put("leituraAnterior", "" + rateio.getLeituraAnterior());
            mapa.put("leituraAtual", "" + rateio.getLeituraAtual());
            somaConsumoM3 = somaConsumoM3.add(rateio.getConsumoMetroCubico());
            mapa.put("consumoM3", "" + rateio.getConsumoMetroCubico());
            somaConsumoACobrarM3 = somaConsumoACobrarM3.add(rateio.getConsumoMetroCubicoACobrar());
            mapa.put("consumoACobrarM3", "" + rateio.getConsumoMetroCubicoACobrar());
            somaPercentualAreaComum = somaPercentualAreaComum.add(rateio.getPercentualRateioAreaComum());
            mapa.put("percentualAreaComum", "" + FormatadorNumeros.casasDecimais(2, rateio.getPercentualRateioAreaComum()));
            somaConsumoM3AreaComum = somaConsumoM3AreaComum.add(rateio.getConsumoMetroCubicoAreaComum());
            mapa.put("consumoM3AreaComum", "" + FormatadorNumeros.casasDecimais(3, rateio.getConsumoMetroCubicoAreaComum()));
            somaConsumoUnidade = somaConsumoUnidade.add(rateio.getValorTotalConsumido());
            mapa.put("consumoUnidade", PagamentoUtil.formatarMoeda(rateio.getValorTotalConsumido().setScale(2, RoundingMode.UP).doubleValue()));
            somaConsumoAreaComum = somaConsumoAreaComum.add(rateio.getConsumoEmDinheiroAreaComum());
            mapa.put("consumoAreaComum", PagamentoUtil.formatarMoeda(rateio.getConsumoEmDinheiroAreaComum().doubleValue()));
            somaTotalACobrar = somaTotalACobrar.add(rateio.getValorTotalCobrar());
            mapa.put("totalACobrar", PagamentoUtil.formatarMoeda(rateio.getValorTotalCobrar().doubleValue()));
            listaRateio.add(mapa);
        }

        parametros.put("condominio", conta.getCondominio().getRazaoSocial());

        parametros.put("somaConsumoM3", "" + somaConsumoM3);
        parametros.put("somaConsumoACobrarM3", "" + somaConsumoACobrarM3);
        parametros.put("somaPercentualAreaComum", "" + FormatadorNumeros.casasDecimais(2, somaPercentualAreaComum));
        parametros.put("somaConsumoM3AreaComum", "" + FormatadorNumeros.casasDecimais(3, somaConsumoM3AreaComum));
        parametros.put("somaConsumoUnidade", PagamentoUtil.formatarMoeda(somaConsumoUnidade.setScale(2, RoundingMode.UP).doubleValue()));
        parametros.put("somaConsumoAreaComum", PagamentoUtil.formatarMoeda(somaConsumoAreaComum.doubleValue()));
        parametros.put("somaTotalACobrar", PagamentoUtil.formatarMoeda(somaTotalACobrar.doubleValue()));

//      DADOS DA CONTA DE AGUA
        parametros.put("formaRateioAreaComum", formaRateio);
        parametros.put("qtdeM3TaxaCondominial", "" + qtdeM3InclusosTaxaCondominial);
        parametros.put("possuiHidrometroAreaComum", possuiHidrometro ? "Sim" : "Não");
        parametros.put("formaCalculoValor", formaRateioAreaComum);
        parametros.put("consideraPipa", consideraPipa ? "Condomínio Considera Água Fornecida por Pipa" : "Condomínio Não Considera Água Fornecida por Pipa");
        parametros.put("periodoConsumo", DataUtil.toString(conta.getDataInicial()) + " a " + DataUtil.toString(conta.getDataFinal()));
        parametros.put("vencimentoCobranca", DataUtil.toString(conta.getDataVencimentoConta()));
        parametros.put("precoM3", PagamentoUtil.formatarMoeda(conta.getPrecoMetroCubico().doubleValue()));
        parametros.put("despesaPipa", PagamentoUtil.formatarMoeda(conta.getValorPipa().doubleValue()));
        parametros.put("consumoUnidades", PagamentoUtil.formatarMoeda(conta.getPrecoTotalUnidades().doubleValue()));
        parametros.put("consumoAreaComum", PagamentoUtil.formatarMoeda(conta.getPrecoAreaComum().doubleValue()));

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!listaRateio.isEmpty()) {
            new Relatorios().imprimir("RelatorioConsumoAgua", parametros, listaRateio, false, true, null);
        }
    }
}
