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
import condominioPlus.negocio.NegocioUtil;
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

    public void imprimir(String relatorio, HashMap parametros, List lista, boolean impressaoDireta) {
        try {

            caminhoImagem = getClass().getResource("/condominioPlus/recursos/imagens/logo.jpg");
            parametros.put("logoEmpresa", caminhoImagem.toString());

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

    public void imprimirRelatorioInadimplencia(Condominio condominio, List<Unidade> unidades, DateTime dataInicial, DateTime dataFinal, DateTime dataCalculo, TipoRelatorio tipo) {
        List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> parametros = new HashMap();
        List<Unidade> listaUnidades = new ArrayList<Unidade>();

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
                            mapa2.put("descricao", pagamento.getDescricao());
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

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (tipo == TipoRelatorio.INADIMPLENCIA_ANALITICA) {
            parametros.put("subrelatorio2", caminho.toString());
            imprimir("InadimplenciaAnalitica", parametros, lista, false);
        } else if (tipo == TipoRelatorio.INADIMPLENCIA_SINTETICA){
            imprimir("InadimplenciaSintetica", parametros, lista, false);
        }


    }

    private void calcularJurosMulta(Cobranca cobranca, DateTime dataProrrogada) {
        Moeda juros = new Moeda();
        Moeda multa = new Moeda();
        cobranca.setValorTotal(new BigDecimal(0));
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getValorOriginal()));
        for (Pagamento pagamento : cobranca.getPagamentos()) {
            if (pagamento.getConta().getCodigo() == 28103) {
                cobranca.setValorTotal(cobranca.getValorTotal().subtract(pagamento.getValor()));
            }
        }
        double diferencaMeses = 0;
        diferencaMeses = DataUtil.getDiferencaEmMeses(dataProrrogada, DataUtil.getDateTime(cobranca.getDataVencimento()));
        if (diferencaMeses > 0) {
//            System.out.println("diferenca meses: " + new Double(diferencaMeses).intValue());
            juros.soma(new Double(diferencaMeses).intValue()).multiplica(NegocioUtil.getConfiguracao().getPercentualJuros().divide(new BigDecimal(100)));
//            System.out.println("juros: " + juros);
            juros.multiplica(cobranca.getValorTotal());
            multa.soma(NegocioUtil.getConfiguracao().getPercentualMulta().divide(new BigDecimal(100)));
            multa.multiplica(cobranca.getValorTotal());
        }
        cobranca.setJuros(juros.bigDecimalValue().setScale(2, RoundingMode.UP));
        cobranca.setMulta(multa.bigDecimalValue().setScale(2, RoundingMode.UP));
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getJuros().add(cobranca.getMulta())).setScale(2, RoundingMode.UP));
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
}
