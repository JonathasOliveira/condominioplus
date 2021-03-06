/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoDadosCapa.java
 *
 * Created on 05/04/2013, 15:53:15
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.util.Relatorios;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.util.ComboModelo;
import logicpoint.util.Util;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 *
 * @author eugenia
 */
public class DialogoDadosCapa extends javax.swing.JDialog {

    private List<Condominio> listaCondominios;

    /** Creates new form DialogoDadosCapa */
    public DialogoDadosCapa(java.awt.Frame parent, boolean modal, List<Condominio> listaCondominios) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(null);
        new ControladorEventos();
        carregarComboMes();
        this.listaCondominios = listaCondominios;
    }

    private void sair() {
        dispose();
    }

    private void imprimir(Condominio condominio) {
        List<HashMap<String, Object>> listaCondominos = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();

        List<HashMap<String, String>> listaConselheiros = new ArrayList<HashMap<String, String>>();
        UNIDADES:
        for (Unidade unidade : condominio.getConselheiros()) {
            if (unidade.getCondomino().getTipoConselheiro().equals("Conselheiro de Obras")) {
                continue UNIDADES;
            }
            HashMap<String, String> mapa2 = new HashMap();
            mapa2.put("nome", converterLetraMinuscula(unidade.getCondomino().getNome()));
            mapa2.put("unidade", unidade.getUnidade());
            listaConselheiros.add(mapa2);
        }

        HashMap<String, Object> mapa = new HashMap();
        String mesExtenso = cmbMes.getModel().getSelectedItem().toString();

        mapa.put("condominio", converterLetraMinuscula(condominio.getTituloCapa()));

        mapa.put("periodo", mesExtenso + "/" + txtAno.getText());
        mapa.put("periodoExtenso", retornarMesNumerico(mesExtenso) + "/" + txtAno.getText());
        mapa.put("listaConselheiros", new JRBeanCollectionDataSource(listaConselheiros));
        listaCondominos.add(mapa);

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!listaCondominos.isEmpty()) {
            new Relatorios().imprimir("CapaPrestacaoContas", parametros, listaCondominos, false, false, null);
        }
    }

    private void carregarComboMes() {
        cmbMes.setModel(new ComboModelo<String>(Util.toList(new String[]{"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"}), false));
    }

    private String retornarMesNumerico(String mesExtenso) {
        if (mesExtenso.equals("Janeiro")) {
            return "01";
        } else if (mesExtenso.equals("Fevereiro")) {
            return "02";
        } else if (mesExtenso.equals("Março")) {
            return "03";
        } else if (mesExtenso.equals("Abril")) {
            return "04";
        } else if (mesExtenso.equals("Maio")) {
            return "05";
        } else if (mesExtenso.equals("Junho")) {
            return "06";
        } else if (mesExtenso.equals("Julho")) {
            return "07";
        } else if (mesExtenso.equals("Agosto")) {
            return "08";
        } else if (mesExtenso.equals("Setembro")) {
            return "09";
        } else if (mesExtenso.equals("Outubro")) {
            return "10";
        } else if (mesExtenso.equals("Novembro")) {
            return "11";
        } else if (mesExtenso.equals("Dezembro")) {
            return "12";
        }
        return "";
    }

    public static String converterLetraMinuscula(String frase) {
        String sentence = frase;
        StringBuilder bob = new StringBuilder();

        TESTE:
        for (String string : sentence.split(" ")) {
            if (string.equals("DOS") || string.equals("DA") || string.equals("DAS") || string.equals("E") || string.equals("DU") || string.equals("DE")) {
                bob.append(string.toLowerCase());
                bob.append(" ");
                continue TESTE;
            }
            if (string.equals("AC") || string.equals("AL") || string.equals("AP") || string.equals("AM") || string.equals("BA") || string.equals("CE")
                    || string.equals("DF") || string.equals("ES") || string.equals("GO") || string.equals("MA") || string.equals("MT") || string.equals("MS")
                    || string.equals("MG") || string.equals("PA") || string.equals("PB") || string.equals("PR") || string.equals("PE") || string.equals("PI")
                    || string.equals("RJ") || string.equals("RN") || string.equals("RS") || string.equals("RO") || string.equals("RR") || string.equals("SC")
                    || string.equals("SP") || string.equals("SE") || string.equals("TO")) {
                bob.append(string.toUpperCase());
                bob.append(" ");
            } else {
                if (string.equals("")) {
                    continue TESTE;
                }
                bob.append(string.trim().substring(0, 1).toUpperCase());
                bob.append(string.substring(1).toLowerCase());
                bob.append(" ");
            }

        }

        sentence = bob.substring(0, bob.length() - 1);

        sentence = bob.toString().trim();

        return sentence;
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnOk.addActionListener(this);
            btnCancelar.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnOk) {
                if (listaCondominios.isEmpty()) {
                    imprimir(null);
                } else {
                    for (Condominio condominio : listaCondominios) {
                        imprimir(condominio);
                    }
                }
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
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        txtAno = new javax.swing.JTextField();
        cmbMes = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Capa Prestação de Contas");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnOk.setText("OK");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar)
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnOk))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel23.setText("Mês:");

        jLabel24.setText("Ano:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbMes, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAno, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(cmbMes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cmbMes;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtAno;
    // End of variables declaration//GEN-END:variables
}
