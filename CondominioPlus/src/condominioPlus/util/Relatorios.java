/*
 * Relatorios.java
 * 
 * Created on 16/08/2007, 09:07:11
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.financeiro.Pagamento;
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

    public void imprimirRelatorioEnvelope(boolean imprimirRemetente, DateTime dataVencimento, Condominio condominio, List<Unidade> unidades, TipoRelatorio tipo) {
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
            if (tipo == TipoRelatorio.ENVELOPE_PEQUENO) {
                imprimir("EnvelopePequeno", parametros, listaCondominos, false, imprimirRemetente, null);
            } else if (tipo == TipoRelatorio.RELACAO_PROPRIETARIOS) {
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
        
        if(imprimirObservacao){
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
}
