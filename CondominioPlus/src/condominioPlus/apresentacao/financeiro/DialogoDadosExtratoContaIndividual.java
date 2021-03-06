/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoDadosExtratoContaIndividual.java
 *
 * Created on 07/10/2010, 14:33:06
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.Calendar;
import javax.swing.event.ChangeEvent;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class DialogoDadosExtratoContaIndividual extends javax.swing.JDialog {

    private DateTime dataInicial;
    private DateTime dataFinal;
    private TipoRelatorio tipo;
    private Conta conta;
    private Pagamento pagamento;
    private boolean continuar = true;
    private String unidade;

    /** Creates new form DialogoDadosExtratoContaIndividual */
    public DialogoDadosExtratoContaIndividual(java.awt.Frame parent, boolean modal, DateTime dataInicial, DateTime dataFinal, Pagamento pagamento, TipoRelatorio tipo) {
        super(parent, modal);
        initComponents();
        new ControladorEventos();
        this.setLocationRelativeTo(null);
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.pagamento = pagamento;
        this.tipo = tipo;

        preencherTela();
        
        this.setTitle(tipo.toString());
    }

    public DateTime getDataInicial() {
        return dataInicial;
    }

    public DateTime getDataFinal() {
        return dataFinal;
    }

    public Conta getConta() {
        return conta;
    }
    
    public boolean isContinuar(){
        return continuar;
    }
    
    public String getUnidade(){
        return unidade;
    }

    private void preencherTela() {
        if (pagamento != null) {
            txtConta.setText(pagamento.getConta().getCodigo() + "");
        }
        if (DataUtil.compararData(dataInicial, dataFinal) == 0) {
            Calendar dat1 = Calendar.getInstance();
            dat1.setTime(DataUtil.getDate(DataUtil.hoje()));
            dat1.add(Calendar.MONTH, -1);
            txtDataInicial.setValue(DataUtil.getDate(DataUtil.getPrimeiroDiaMes(DataUtil.getDateTime(dat1))));
            txtDataFinal.setValue(DataUtil.getDate(DataUtil.getUltimoDiaMes(DataUtil.getDateTime(dat1))));
        } else {
            txtDataInicial.setValue(DataUtil.getDate(dataInicial));
            txtDataFinal.setValue(DataUtil.getDate(dataFinal));
        }
    }

    private void salvarDados() {
        dataInicial = DataUtil.getDateTime(txtDataInicial.getValue());
        dataFinal = DataUtil.getDateTime(txtDataFinal.getValue());
        unidade = txtUnidade.getText();

        sair();
    }

    private void sair() {
        dispose();
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnOk.addActionListener(this);
            btnCancelar.addActionListener(this);
            btnCancelar.addActionListener(this);
            txtConta.addFocusListener(this);
            chkUnidade.addChangeListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnOk) {
                salvarDados();
            } else if (source == btnCancelar) {
                continuar = false;
                sair();
            }
            source = null;
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == txtConta) {
                Conta resultado = null;
                if (new LimitarCaracteres(10).ValidaNumero(txtConta)) {
                    if (!txtConta.getText().equals("") && txtConta.getText() != null) {
                        resultado = ContaUtil.pesquisarContaPorCodigo(Integer.valueOf(txtConta.getText()));
                        if (resultado != null) {
                            conta = resultado;
                            txtConta.setText(String.valueOf(conta.getCodigo()));
                        } else {
                            ApresentacaoUtil.exibirErro("Código Inexistente!", DialogoDadosExtratoContaIndividual.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                } else {
                    txtConta.setText("");
                    txtConta.grabFocus();
                }
            }
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == chkUnidade) {
                if (chkUnidade.isSelected()) {
                    txtUnidade.setEnabled(true);
                    txtUnidade.setBackground(Color.WHITE);
                } else {
                    txtUnidade.setEnabled(false);
                    txtUnidade.setBackground(Color.LIGHT_GRAY);
                    txtUnidade.setText(null);
                }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtConta = new javax.swing.JTextField();
        chkUnidade = new javax.swing.JCheckBox();
        txtUnidade = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Extrato Conta Individual");
        setAlwaysOnTop(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnOk.setText("OK");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(118, Short.MAX_VALUE)
                .addComponent(btnOk)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar)
                .addGap(88, 88, 88))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancelar, btnOk});

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

        jLabel1.setText("Digite o código da conta:");

        chkUnidade.setText("Selecionar unidade:");

        txtUnidade.setBackground(new java.awt.Color(204, 204, 204));
        txtUnidade.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(chkUnidade)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkUnidade)
                    .addComponent(txtUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chkUnidade;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtUnidade;
    // End of variables declaration//GEN-END:variables
}
