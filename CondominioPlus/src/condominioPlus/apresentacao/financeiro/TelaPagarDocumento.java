/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaPagarDocumento.java
 *
 * Created on 17/12/2010, 16:08:54
 */
package condominioPlus.apresentacao.financeiro;

import bemaJava.Bematech;
import com.sun.jna.Native;
import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import condominioPlus.Main;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.DadosPagamento;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class TelaPagarDocumento extends javax.swing.JDialog {

    List<Pagamento> pagamentos;
    List<Pagamento> novaLista = new ArrayList<Pagamento>();
    BigDecimal total = new BigDecimal(0);
    DadosPagamento dados;

    /** Creates new form TelaPagarDocumento */
    public TelaPagarDocumento(java.awt.Frame parent, List<Pagamento> pagamentos) {
        super(parent, true);
        this.pagamentos = pagamentos;
        initComponents();
        this.setLocationRelativeTo(null);

        new ControladorEventos();
    }

    private void efetuarPagamento() {
        for (Pagamento pagamento : pagamentos) {
            if (radioCheque.isSelected()) {
                preencherObjetos();
                pagamento.setForma(FormaPagamento.CHEQUE);
                if (dados == null) {
                    pagamento.setDadosPagamento(dados = new DadosCheque(txtNumeroDocumento.getText(), Main.getCondominio().getContaBancaria().getContaCorrente(), Main.getCondominio().getRazaoSocial(),
                            Main.getCondominio().getContaBancaria().getBanco()));
                } else {
                    pagamento.setDadosPagamento(dados);
                }
                pagamento.setPago(true);
                pagamento.setContaCorrente(Main.getCondominio().getContaCorrente());
                new DAO().salvar(pagamento);
            } else {
                preencherObjetos();
                pagamento.setForma(FormaPagamento.DINHEIRO);
                if (dados == null) {
                    pagamento.setDadosPagamento(dados = new DadosDOC(txtNumeroDocumento.getText()));
                }else{
                    pagamento.setDadosPagamento(dados);
                }
                pagamento.setPago(true);
                pagamento.setContaCorrente(Main.getCondominio().getContaCorrente());
                new DAO().salvar(pagamento);
            }
        }
    }

    private String somarCheque() {
        for (Pagamento pagamento : pagamentos) {
            total = total.add(pagamento.getValor());
        }
        return String.valueOf(total);
    }

    private void imprimirCheques() {
        int iRetorno;
        Pagamento p = null;
        if (!pagamentos.isEmpty()) {
            p = pagamentos.get(0);
        }

        Bematech lib =
                (Bematech) Native.loadLibrary("BEMADP32", Bematech.class);
        iRetorno = lib.Bematech_DP_IniciaPorta("COM1");
        lib.Bematech_DP_IncluiAlteraBanco("555", "3,7,9,11,13,92,20,8,10,62,23,32,55");
        String valor = somarCheque();
        iRetorno = lib.Bematech_DP_ImprimeCheque("555", valor, p.getFornecedor().getNome(), "ARMACAO DOS BUZIOS", DataUtil.getDateTime(p.getDataPagamento()).toString("ddMMyy"), "");
        System.out.println(iRetorno);

    }

    private void preencherObjetos() {
        for (Pagamento pagamento : pagamentos) {
//            pagamento.setNumeroDocumento(txtNumeroDocumento.getText());
            pagamento.setDataPagamento(DataUtil.getCalendar(txtDataPagamento.getValue()));

        }
    }

    public boolean atualizar() {
        return true;
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();

            if (origem == radioCheque) {
                txtNumeroDocumento.setText(Main.getCondominio().getContaBancaria().getContaCorrente());

            } else if (origem == radioDocumento) {
                txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());

            } else if (origem == btnEfetivarPagamento) {
                efetuarPagamento();
                if (radioCheque.isSelected()) {
                    imprimirCheques();
                } else {
                    System.out.println("Documento!");
                }
                dispose();
            } else if (origem == btnCancelar) {
                dispose();
            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaPagarDocumento.this, JTextField.class);

            radioCheque.addActionListener(this);
            txtNumeroDocumento.addActionListener(this);
            radioDocumento.addActionListener(this);
            btnEfetivarPagamento.addActionListener(this);
            btnCancelar.addActionListener(this);
            txtNumeroDocumento.addFocusListener(this);
        }

        @Override
        public void focusLost(FocusEvent e) {
//            if (e.getSource() == txtNumeroDocumento && !txtNumeroDocumento.getText().isEmpty() && radioCheque.isSelected()) {
//                Long numero = Long.valueOf(txtNumeroDocumento.getText());
//                String juncaoInicial = Main.getCondominio().getContaBancaria().getContaCorrente() + Main.getCondominio().getDadosTalao().getNumeroInicial();
//                String juncaoFinal = Main.getCondominio().getContaBancaria().getContaCorrente() + Main.getCondominio().getDadosTalao().getNumeroFinal();
//                Long inicial = Long.valueOf(juncaoInicial);
//                Long numeroFinal = Long.valueOf(juncaoFinal);
//                if (numero <= inicial || numero >= numeroFinal) {
//                    ApresentacaoUtil.exibirInformacao("Digite um numero entre " + Main.getCondominio().getDadosTalao().getNumeroInicial() + " e "
//                            + Main.getCondominio().getDadosTalao().getNumeroFinal(), TelaPagarDocumento.this);
//                    txtNumeroDocumento.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 51), 2));
//                }
//
//            }
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
        txtDataPagamento = new net.sf.nachocalendar.components.DateField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnEfetivarPagamento = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        radioDocumento = new javax.swing.JRadioButton();
        radioCheque = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Efetuar Pagamento");

        jLabel1.setText("Data Pagamento:");

        jLabel2.setText("nº documento/cheque:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtDataPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtDataPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnEfetivarPagamento.setText("Efetivar Pagamento");

        btnCancelar.setText("Cancelar");

        buttonGroup1.add(radioDocumento);
        radioDocumento.setSelected(true);
        radioDocumento.setText("Documento");

        buttonGroup1.add(radioCheque);
        radioCheque.setText("Cheque");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEfetivarPagamento)
                            .addComponent(radioDocumento))
                        .addGap(48, 48, 48)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioCheque)
                            .addComponent(btnCancelar))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancelar, btnEfetivarPagamento});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioCheque, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radioDocumento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnEfetivarPagamento))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEfetivarPagamento;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton radioCheque;
    private javax.swing.JRadioButton radioDocumento;
    private net.sf.nachocalendar.components.DateField txtDataPagamento;
    private javax.swing.JTextField txtNumeroDocumento;
    // End of variables declaration//GEN-END:variables
}
