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
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.BoletoBancario;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.TaxaExtra;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.util.Relatorios;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

                List<HashMap<String, Object>> lista = new ArrayList<HashMap<String, Object>>();
                HashMap<String, Object> parametros = new HashMap();

                UNIDADES:
                for (Unidade u : condominio.getUnidades()) {

                    List<HashMap<String, String>> listaCobrancas = new ArrayList<HashMap<String, String>>();

                    parametros.put("periodo", DataUtil.toString(dataInicial) + " a " + DataUtil.toString(dataFinal));
                    parametros.put("condominio", condominio.getRazaoSocial());

                    BigDecimal totalOriginal = new BigDecimal(0);
                    BigDecimal totalJuros = new BigDecimal(0);
                    BigDecimal totalMulta = new BigDecimal(0);
                    BigDecimal totalGeral = new BigDecimal(0);
                    for (Cobranca co : u.getCobrancas()) {                        
                        Cobranca cobrancaAux = new Cobranca();
                        cobrancaAux.setValorOriginal(co.getValorOriginal());
                        cobrancaAux.setJuros(co.getJuros());
                        cobrancaAux.setMulta(co.getMulta());
                        cobrancaAux.setValorTotal(co.getValorTotal());
                        cobrancaAux.setDataVencimento(co.getDataVencimento());
                        cobrancaAux.setDataPagamento(co.getDataPagamento());
                        cobrancaAux.setExibir(co.isExibir());
                        cobrancaAux.setNumeroDocumento(co.getNumeroDocumento());                        
                        if (cobrancaAux.getDataPagamento() == null && DataUtil.compararData(dataInicial, DataUtil.getDateTime(cobrancaAux.getDataVencimento())) == -1 && DataUtil.compararData(dataFinal, DataUtil.getDateTime(cobrancaAux.getDataVencimento())) == 1 && cobrancaAux.isExibir()) {
                            calcularJurosMulta(cobrancaAux, dataFinal);
                            HashMap<String, String> mapa = new HashMap();
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

                    URL caminho = getClass().getResource("/condominioPlus/relatorios/");
                    parametros.put("subrelatorio", caminho.toString());

                }

                new Relatorios().imprimir("InadimplenciaSintetica", parametros, lista, false);

            }
        }
    }

    private void calcularJurosMulta(Cobranca cobranca, DateTime dataProrrogada) {
        Moeda juros = new Moeda();
        Moeda multa = new Moeda();
        cobranca.setValorTotal(new BigDecimal(0));
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getValorOriginal()));
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 336, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 141, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnOk;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    // End of variables declaration//GEN-END:variables
}
