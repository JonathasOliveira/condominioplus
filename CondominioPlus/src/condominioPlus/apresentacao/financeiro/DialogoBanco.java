/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoBanco.java
 *
 * Created on 09/08/2010, 15:58:28
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Banco;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.persistencia.DAO;

/**
 *
 * @author Administrador
 */
public class DialogoBanco extends javax.swing.JDialog {

    private Banco banco;
    private ControladorDeEventos controlador;

    /** Creates new form DialogoTelefone */
    public DialogoBanco(Banco banco, java.awt.Frame pai, boolean modal) {
        super(pai, modal);
        this.banco = banco;
        initComponents();
        controlador = new ControladorDeEventos();
        if (banco != null) {
            controlador.preencher(banco);
        }
        this.setLocationRelativeTo(null);

    }

    public static Banco getBanco(Banco banco, Frame pai, boolean modal) {
        TelaPrincipal.getInstancia().criarJanela(new DialogoBanco(banco, pai, modal));
        return banco;
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtNome);
        campos.add(txtAgencia);
        campos.add(txtNumero);
        return campos;
    }

    private void salvar() {
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(null);
            return;
        }
        controlador.capturar(banco);
        new DAO().salvar(banco);
        fechar();
    }

    private void cancelar() {
        banco = null;
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
            put(Banco.class, DialogoBanco.this);

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

        jLabel4 = new javax.swing.JLabel();
        txtNome1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtAgencia = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtContaMaster = new javax.swing.JTextField();
        txtNome = new javax.swing.JTextField();

        jLabel4.setText("Nome:");

        txtNome1.setName("nomeBanco"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Número:");

        txtNumero.setName("numeroBanco"); // NOI18N

        jLabel3.setText("Nome:");

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

        jLabel2.setText("Agência:");

        txtAgencia.setName("agencia"); // NOI18N

        jLabel5.setText("Conta Master:");

        txtContaMaster.setName("contaMaster"); // NOI18N

        txtNome.setName("nomeBanco"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtAgencia, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNumero, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                                .addContainerGap(69, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtContaMaster, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(69, 69, 69))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtContaMaster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * @param args the command line arguments
     **/
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField txtAgencia;
    private javax.swing.JTextField txtContaMaster;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtNome1;
    private javax.swing.JTextField txtNumero;
    // End of variables declaration//GEN-END:variables
}

