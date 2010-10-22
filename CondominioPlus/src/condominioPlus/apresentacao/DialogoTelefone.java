/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoTelefone.java
 *
 * Created on Jul 29, 2010, 1:14:47 PM
 */
package condominioPlus.apresentacao;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import condominioPlus.negocio.Telefone;
import condominioPlus.validadores.ValidadorGenerico;
import java.util.ArrayList;
import java.util.List;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.util.ComboModelo;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class DialogoTelefone extends javax.swing.JDialog {

    private Telefone telefone;
    private ControladorDeEventos controlador;

    /** Creates new form DialogoTelefone */
    public DialogoTelefone(Telefone telefone, java.awt.Frame pai, boolean modal) {
        super(pai, modal);
        this.telefone = telefone;
        initComponents();
        controlador = new ControladorDeEventos();
        carregarComboTipo();
        controlador.preencher(telefone);
        this.setLocationRelativeTo(null);

    }

    public static Telefone getTelefone(Telefone telefone, Frame pai, boolean modal) {
        TelaPrincipal.getInstancia().criarJanela(new DialogoTelefone(telefone, pai, modal));
        return telefone;
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtTelefone);

        return campos;
    }

    private void carregarComboTipo() {
        cmbTipo.setModel(new ComboModelo<String>(Util.toList(new String[]{"Celular", "Fixo", "Nextel", "Comercial", "Recado"}), false));
    }

    private void salvar() {
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(null);
            return;
        }
        controlador.capturar(telefone);
        fechar();
    }

    private void cancelar() {
        telefone = null;
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
            put(Telefone.class, DialogoTelefone.this);

            btnSalvar.addActionListener(this);
            btnCancelar.addActionListener(this);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        txtTelefone = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbTipo = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro Telefone");

        btnSalvar.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/ok.GIF"))); // NOI18N
        btnSalvar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnSalvar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnSalvar.setPreferredSize(new java.awt.Dimension(32, 32));

        btnCancelar.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnCancelar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnCancelar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnCancelar.setPreferredSize(new java.awt.Dimension(32, 32));

        try {
            txtTelefone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##)####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtTelefone.setToolTipText("Digite o Telefone");
        txtTelefone.setName("numero"); // NOI18N

        jLabel1.setText("Tipo:");

        jLabel2.setText("Telefone:");

        cmbTipo.setName("tipo"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtTelefone, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                    .addComponent(cmbTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox cmbTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JFormattedTextField txtTelefone;
    // End of variables declaration//GEN-END:variables
}
