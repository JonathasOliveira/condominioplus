/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoRateioTaxaExtra.java
 *
 * Created on 10/08/2011, 14:11:26
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.RenderizadorCelulaData;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.util.DataUtil;
import net.sf.nachocalendar.table.JTableCustomizer;

/**
 *
 * @author eugenia
 */
public class DialogoRateioTaxaExtra extends javax.swing.JDialog {

    private ParcelaTaxaExtra parcela;
    private TabelaModelo_2<RateioTaxaExtra> modelo;

    /** Creates new form DialogoRateioTaxaExtra */
    public DialogoRateioTaxaExtra(ParcelaTaxaExtra parcela) {
        initComponents();
        this.parcela = parcela;
        carregarTabela();
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<RateioTaxaExtra>(tabela, "Unidade,Vencimento, Valor".split(",")) {

            @Override
            protected List<RateioTaxaExtra> getCarregarObjetos() {
                return getRateios();
            }

            @Override
            public void setValor(RateioTaxaExtra rateio, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 1:
                        rateio.setDataVencimento(DataUtil.getCalendar(valor));
                        break;
                }
            }

            @Override
            public Object getValor(RateioTaxaExtra r, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return r.getUnidade().getUnidade();
                    case 1:
                        return r.getDataVencimento();
                    case 2:
                        return PagamentoUtil.formatarMoeda(r.getValorACobrar().doubleValue());
                    default:
                        return null;

                }
            }
        };

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabela.getColumn(modelo.getCampo(0)).setCellRenderer(direita);
        tabela.getColumn(modelo.getCampo(2)).setCellRenderer(direita);

        modelo.setEditaveis(1);

        tabela.getColumn(modelo.getCampo(1)).setCellRenderer(new RenderizadorCelulaData());
        JTableCustomizer.setEditorForRow(tabela, 1);

    }

    public List<RateioTaxaExtra> getRateios() {
        List<RateioTaxaExtra> lista = parcela.getRateios();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                RateioTaxaExtra r1 = (RateioTaxaExtra) o1;
                RateioTaxaExtra r2 = (RateioTaxaExtra) o2;
                return r1.getUnidade().getUnidade().compareTo(r2.getUnidade().getUnidade());
            }
        };

        Collections.sort(lista, c);

        return lista;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Rateio Taxa Extra");

        tabela.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabela);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    // End of variables declaration//GEN-END:variables
}
