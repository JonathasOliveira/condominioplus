/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoConselheiro.java
 *
 * Created on 30/08/2010, 15:34:26
 */
package condominioPlus.apresentacao;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Condomino;
import condominioPlus.negocio.Unidade;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import logicpoint.apresentacao.ComboModelo_2;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class DialogoConselheiro extends javax.swing.JDialog {

    private Condominio condominio;
    private ControladorDeEventos controlador;
    private ComboModelo_2<Unidade> modelo;
    private Condomino condomino;

    public DialogoConselheiro(Condominio condominio, java.awt.Frame pai, boolean modal) {
        super(pai, modal);
        this.condominio = condominio;
        initComponents();
        controlador = new ControladorDeEventos();
        carregarComboTipo();

        carregarComboConselheiros();

        this.setLocationRelativeTo(null);

    }

    public static boolean getConselheiro(Condominio condominio, Frame pai, boolean modal) {
        TelaPrincipal.getInstancia().criarJanela(new DialogoConselheiro(condominio, pai, modal));
        return true;
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();


        return campos;
    }

    private Condomino getCondomino() {
        return condomino = modelo.getObjetoSelecionado().getCondomino();
    }

    private void preencherObjeto() {
        getCondomino().setTipoConselheiro(cmbTipo.getModel().getSelectedItem().toString());
        getCondomino().setConselheiro(true);

    }

    private void carregarComboConselheiros() {
        List<Unidade> unidades = new DAO().listar("CondominosPorUnidadeSemSindico", condominio.getCodigo());
        List<Unidade> novas = new ArrayList<Unidade>();
        UNIDADES:
        for (Unidade unidade : unidades) {
            if(unidade.isSindico()){
                continue UNIDADES;
            }
            novas.add(unidade);
        }
        modelo = new ComboModelo_2(cmbCondomino, novas, true);
    }

    private void carregarComboTipo() {
        cmbTipo.setModel(new ComboModelo<String>(Util.toList(new String[]{"Conselheiro de Obras", "Conselheiro de Fiscal", "Conselheiro de Fiscal e Obras"}), false));
    }

    private void salvar() {
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(null);
            return;
        }
        preencherObjeto();
        DAO dao = new DAO();
        dao.salvar(getCondomino());
        fechar();
    }

    private void cancelar() {
        fechar();
    }

    private void fechar() {
        super.dispose();
    }

    @Override
    public void dispose() {
        cancelar();
    }

    private class ControladorDeEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {

            btnSalvar.addActionListener(this);
            btnCancelar.addActionListener(this);
            cmbCondomino.addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (cmbCondomino.getSelectedIndex() != -1) {
                txtUnidade.setText(modelo.getSelectedItem().getUnidade());
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnSalvar) {
                salvar();
            } else if (source == btnCancelar) {
                cancelar();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        cmbCondomino = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtUnidade = new javax.swing.JTextField();
        cmbTipo = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro de Conselheiros");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Condômino:");

        jLabel2.setText("Tipo:");

        jLabel4.setText("Unidade:");

        txtUnidade.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbCondomino, 0, 289, Short.MAX_VALUE)
                            .addComponent(cmbTipo, 0, 289, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbCondomino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        btnSalvar.setText("Salvar");

        btnCancelar.setText("Voltar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(96, 96, 96)
                .addComponent(btnSalvar)
                .addGap(36, 36, 36)
                .addComponent(btnCancelar)
                .addContainerGap(131, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalvar)
                    .addComponent(btnCancelar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox cmbCondomino;
    private javax.swing.JComboBox cmbTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtUnidade;
    // End of variables declaration//GEN-END:variables
}
