/*
 * PainelTelaPrincipal.java
 *
 * Created on 18 de Outubro de 2007, 13:13
 */
package condominioPlus.apresentacao;

import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import javax.swing.ImageIcon;
import condominioPlus.negocio.NegocioUtil;
import logicpoint.recursos.Recursos;

/**
 *
 * @author Thiago
 */
public class PainelTelaPrincipal extends javax.swing.JPanel {

    /** Creates new form PainelTelaPrincipal */
    public PainelTelaPrincipal() {
        initComponents();

        this.addHierarchyBoundsListener(new HierarchyBoundsListener() {

            public void ancestorMoved(HierarchyEvent e) {
            }

            public void ancestorResized(HierarchyEvent e) {
                alocar();
            }
        });

        preencher();
    }

    public void preencher() {
        lblTitulo.setText(NegocioUtil.getConfiguracao().getNomeEmpresa());
        ImageIcon logo = NegocioUtil.getConfiguracao().getLogoEmpresa();
        if (logo == null) {
            logo = Recursos.getImagem("logo");
        }
        lblLogo.setIcon(logo);
    }

    private void alocar() {
        this.setBounds(0, 0, TelaPrincipal.getInstancia().getWidth() - 0, TelaPrincipal.getInstancia().getHeight() - 0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblLogo = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(250, 250));

        painelTitulo.setBackground(new java.awt.Color(4, 79, 0));
        painelTitulo.setMinimumSize(new java.awt.Dimension(250, 250));

        lblTitulo.setFont(new java.awt.Font("Bookman Old Style", 0, 70));
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("CondomínioPlus");

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout painelTituloLayout = new javax.swing.GroupLayout(painelTitulo);
        painelTitulo.setLayout(painelTituloLayout);
        painelTituloLayout.setHorizontalGroup(
            painelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
            .addComponent(lblLogo, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );
        painelTituloLayout.setVerticalGroup(
            painelTituloLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTituloLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLogo, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(painelTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelTitulo;
    // End of variables declaration//GEN-END:variables
}