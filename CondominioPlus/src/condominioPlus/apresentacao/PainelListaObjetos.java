/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PainelListaObjetos2.java
 *
 * Created on Aug 6, 2010, 1:35:31 PM
 */

package condominioPlus.apresentacao;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.util.ListaModelo;

/**
 *
 * @author Administrador
 */
public class PainelListaObjetos extends javax.swing.JPanel {

    ControladorEvento controlador = new ControladorEvento();

    /** Creates new form painelTelefone */
    public PainelListaObjetos() {
        initComponents();

        lista.addListSelectionListener(controlador);
        btnAdicionar.addActionListener(controlador);
        btnRemover.addActionListener(controlador);

        lista.setModel(new ListaModelo());
    }

    public void setNome(String nome) {
        lista.setName(nome);
    }

    public JScrollPane getBarraRolagem() {
        return rolagem;
    }

    @SuppressWarnings(value = "unchecked")
    public void adicionar() {
        String objeto = JOptionPane.showInputDialog(this, "", "Adicionar", JOptionPane.QUESTION_MESSAGE);
        if (objeto != null) {
            ((ListaModelo) lista.getModel()).adicionar(objeto);
        }
    }

    public void remover() {
        int[] index = lista.getSelectedIndices();

        if (!(index.length > 0)) {
            return;
        }
        for (int i : index) {
            ((ListaModelo) lista.getModel()).remover(i);
        }
    }

    private class ControladorEvento extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnAdicionar) {
                adicionar();
            }
            if (e.getSource() == btnRemover) {
                remover();
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getSource() == lista) {
                boolean remover = false;
                if (lista.getSelectedIndices().length > 0) {
                    remover = true;
                }
                btnRemover.setEnabled(remover);
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rolagem = new javax.swing.JScrollPane();
        lista = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        btnRemover = new javax.swing.JButton();
        btnAdicionar = new javax.swing.JButton();

        rolagem.setViewportView(lista);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        btnRemover.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnRemover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemover.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemover.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemover.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel6.add(btnRemover, gridBagConstraints);

        btnAdicionar.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionar.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel6.add(btnAdicionar, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
            .addComponent(rolagem, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(rolagem, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnRemover;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JList lista;
    private javax.swing.JScrollPane rolagem;
    // End of variables declaration//GEN-END:variables

}
