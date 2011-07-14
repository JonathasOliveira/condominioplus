/*
 * Relatorios.java
 * 
 * Created on 16/08/2007, 09:07:11
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import condominioPlus.negocio.NegocioUtil;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import logicpoint.util.DataUtil;
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
        System.out.println("URL: " + url.toString());
        JasperReport relatorio = null;
        try {
            relatorio = (JasperReport) JRLoader.loadObject(url);
        } catch (JRException ex) {
            ex.printStackTrace();
        }
        return relatorio;
    }

    /**
     * Os método print será usado para impri mir texto diretamente para a impressora sem usar
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
}
