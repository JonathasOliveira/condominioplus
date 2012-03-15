/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaDadosRelatorioGerencial.java
 *
 * Created on 14/02/2012, 11:48:39
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.TaxaExtra;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.util.Relatorios;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.joda.time.DateTime;

/**
 *
 * @author eugenia
 */
public class TelaDadosRelatorioGerencial extends javax.swing.JInternalFrame {

    private DateTime dataInicial;
    private DateTime dataFinal;
    private DateTime dataCalculoJurosMulta;
    private Condominio condominio;

    /** Creates new form TelaDadosRelatorioGerencial */
    public TelaDadosRelatorioGerencial(Condominio condominio) {
        initComponents();
        new ControladorEventos();
        this.condominio = condominio;
        if (condominio != null) {
            txtDataInicial.setValue(DataUtil.getDate(new DateTime(1970, 1, 1, 0, 0, 0, 0)));
            txtDataInicial.setEnabled(false);
        }
    }

    public DateTime getDataIncial() {
        return dataInicial;
    }

    public DateTime getDataFinal() {
        return dataFinal;
    }

    private void salvarDados() {
        dataInicial = DataUtil.getDateTime(txtDataInicial.getValue());
        dataFinal = DataUtil.getDateTime(txtDataFinal.getValue());

        imprimir();

    }

    private void sair() {
        dispose();
    }

    private void imprimir() {
        if (this.getDataIncial() != null && this.getDataFinal() != null) {

            if (condominio == null) {
                List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();

                List<Condominio> listaCondominios = new DAO().listar(Condominio.class);

                CONDOMINIOS:
                for (Condominio condominio : listaCondominios) {

                    List<HashMap<String, String>> listaTaxas = new ArrayList<HashMap<String, String>>();

                    if (condominio.getTaxas().isEmpty()) {
                        continue CONDOMINIOS;
                    } else {
                        for (TaxaExtra txe : condominio.getTaxas()) {
                            Moeda totalAArrecadar = new Moeda();
                            Moeda totalArrecadado = new Moeda();
                            Moeda totalInadimplencia = new Moeda();
                            for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
                                if (DataUtil.compararData(DataUtil.getDateTime(parcela.getDataVencimento()), this.getDataIncial()) == 1 && DataUtil.compararData(DataUtil.getDateTime(parcela.getDataVencimento()), this.getDataFinal()) == -1) {
                                    totalAArrecadar.soma(parcela.getValor());
                                    Moeda valorArrecadado = new Moeda();
                                    Moeda valorInadimplencia = new Moeda();
                                    for (RateioTaxaExtra rateio : parcela.getRateios()) {
                                        if (rateio.getCobranca() != null && rateio.getCobranca().getDataPagamento() != null) {
                                            valorArrecadado.soma(rateio.getCobranca().getValorPago());
                                            totalArrecadado.soma(rateio.getCobranca().getValorPago());
                                        } else {
                                            valorInadimplencia.soma(rateio.getValorACobrar());
                                            totalInadimplencia.soma(rateio.getValorACobrar());
                                        }
                                    }
                                }
                            }
                            if (totalAArrecadar.doubleValue() != 0 || totalArrecadado.doubleValue() != 0 || totalInadimplencia.doubleValue() != 0) {
                                HashMap<String, String> mapa = new HashMap();
                                mapa.put("conta", "" + txe.getConta().getCodigo());
                                mapa.put("historico", txe.getDescricao());
                                mapa.put("totalAArrecadar", PagamentoUtil.formatarMoeda(totalAArrecadar.doubleValue()));
                                mapa.put("totalArrecadado", PagamentoUtil.formatarMoeda(totalArrecadado.doubleValue()));
                                mapa.put("totalInadimplencia", PagamentoUtil.formatarMoeda(totalInadimplencia.doubleValue()));
                                listaTaxas.add(mapa);
                            }
                        }

                        if (listaTaxas.isEmpty()) {
                            continue CONDOMINIOS;
                        } else {
                            HashMap<String, Object> mapa2 = new HashMap();
                            mapa2.put("condominio", condominio.getRazaoSocial());
                            mapa2.put("lista", new JRBeanCollectionDataSource(listaTaxas));
                            lista.add(mapa2);
                        }

                    }
                }

                HashMap<String, Object> parametros = new HashMap();
                parametros.put("periodo", DataUtil.toString(this.getDataIncial()) + " a " + DataUtil.toString(this.getDataFinal()));

                URL caminho = getClass().getResource("/condominioPlus/relatorios/");

                parametros.put("subrelatorio", caminho.toString());

                new Relatorios().imprimir("RelatorioGerencialTaxaExtra", parametros, lista, false);

            } else {

                new Relatorios().imprimirRelatorioInadimplencia(condominio, dataInicial, dataFinal);

            }
        }
    }    

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnOk.addActionListener(this);
            btnCancelar.addActionListener(this);
            btnCancelar.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnOk) {
                salvarDados();
            } else if (source == btnCancelar) {
                sair();
            }
            source = null;
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtDataCaluloJurosMulta = new net.sf.nachocalendar.components.DateField();

        setClosable(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnOk.setText("OK");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(67, Short.MAX_VALUE)
                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar)
                .addGap(55, 55, 55))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancelar))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jLabel2.setText("Selecione o período desejado:");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("a");

        jLabel1.setText("Calcular juros/multa até:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtDataCaluloJurosMulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(txtDataCaluloJurosMulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private net.sf.nachocalendar.components.DateField txtDataCaluloJurosMulta;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    // End of variables declaration//GEN-END:variables
}
