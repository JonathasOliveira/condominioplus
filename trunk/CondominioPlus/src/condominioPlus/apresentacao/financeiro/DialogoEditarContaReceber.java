/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoEditarContaPagar.java
 *
 * Created on 02/02/2011, 17:04:42
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.Main;
import condominioPlus.negocio.DadosTalaoCheque;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.fornecedor.Fornecedor;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class DialogoEditarContaReceber extends javax.swing.JDialog {

    private Pagamento pagamento;
//    private ComboModelo<Fornecedor> modelo;
    private ControladorEventosGenerico controlador;
    private Conta conta;

    /** Creates new form TelaBanco */
    public DialogoEditarContaReceber(Pagamento pagamento) {
        initComponents();
        this.pagamento = pagamento;
//        carregarFornecedor();
        preencherTela();
        controlador = new ControladorEventos();
    }

    private String compararForma() {
        return pagamento.getForma() == FormaPagamento.CHEQUE ? String.valueOf(((DadosCheque) pagamento.getDadosPagamento()).getNumero()) : String.valueOf(((DadosDOC) pagamento.getDadosPagamento()).getNumeroDocumento());
    }

    private void preencherTela() {
        txtData.setValue(DataUtil.getDate(pagamento.getDataVencimento()));
        if (pagamento.getConta() != null) {
            conta = pagamento.getConta();
        }
        txtConta.setText(Util.IntegerToString(pagamento.getConta().getCodigo()));
        if (pagamento.getForma() == FormaPagamento.CHEQUE) {
            btnNumeroDocumento.setSelected(true);
            trocarFormaPagamento();
        }
        txtHistorico.setText(pagamento.getHistorico());
        txtNumeroDocumento.setText(compararForma());
        txtValor.setText(String.valueOf(pagamento.getValor()));
        txtFornecedor.setText(pagamento.getFornecedor());

    }

    private void preencherObjeto() {
        BigDecimal valor = new BigDecimal(txtValor.getText().replace(',', '.'));
        pagamento.setDataVencimento(DataUtil.getCalendar(txtData.getValue()));
        pagamento.setHistorico(txtHistorico.getText());
        pagamento.setFornecedor(txtFornecedor.getText());
        pagamento.setConta(conta);
        if (pagamento.getConta().isCredito() && valor.compareTo(new BigDecimal(0)) == -1) {
            pagamento.setValor(valor.negate());
        } else if (!pagamento.getConta().isCredito() && valor.compareTo(new BigDecimal(0)) == 1) {
            pagamento.setValor(valor.negate());
        } else {
            pagamento.setValor(valor);
        }
        selecionaFormaPagamento(pagamento);

    }

    private DadosTalaoCheque getDadosTalaoCheque() {
        if (!Main.getCondominio().getDadosTalaoCheques().isEmpty()) {
            for (DadosTalaoCheque dados : Main.getCondominio().getDadosTalaoCheques()) {
                if (dados.isEmUso()) {
                    return dados;
                }
            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Não existem cheques Cadastrados", this);
        }
        return null;
    }

    private void selecionaFormaPagamento(Pagamento p) {
        if (btnNumeroDocumento.isSelected()) {
            p.setForma(FormaPagamento.CHEQUE);
            if (p.getCodigo() == 0) {
                p.setDadosPagamento(new DadosCheque(txtNumeroDocumento.getText(), Main.getCondominio().getContaBancaria().getContaCorrente(), Main.getCondominio().getRazaoSocial()));
            } else {
                ((DadosCheque) p.getDadosPagamento()).setNumero(txtNumeroDocumento.getText());
            }
        } else {
            p.setForma(FormaPagamento.DINHEIRO);
            if (p.getCodigo() == 0) {
                p.setDadosPagamento(new DadosDOC(txtNumeroDocumento.getText()));
            } else {
                ((DadosDOC) p.getDadosPagamento()).setNumeroDocumento(txtNumeroDocumento.getText());
            }
        }
    }

    private void salvar() {

        if (btnNumeroDocumento.isSelected()) {
            if (!getDadosTalaoCheque().verificarIntervaloCheque(txtNumeroDocumento.getText())) {
                ApresentacaoUtil.exibirAdvertencia("Número do cheque incorreto! Digite um numero entre " + getDadosTalaoCheque().getNumeroInicial() + " - " + getDadosTalaoCheque().getNumeroFinal(), this);
                txtNumeroDocumento.grabFocus();
                txtNumeroDocumento.selectAll();
                return;
            }
        }
        preencherObjeto();



        new DAO().salvar(pagamento);
        dispose();


    }

    private void trocarFormaPagamento() {
        if (btnNumeroDocumento.isSelected()) {
            btnNumeroDocumento.setText("Nº Cheque:");
            txtNumeroDocumento.setText(Main.getCondominio().getContaBancaria().getContaCorrente());
        } else {
            btnNumeroDocumento.setText("Nº Doc:");
            txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());
        }

    }

    private void pegarConta() {
        boolean exibirCredito = false;
        if (pagamento.getConta().isCredito()) {
            exibirCredito = true;
        }
        DialogoConta c = new DialogoConta(null, true, exibirCredito, false, "");
        c.setVisible(true);



        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));


        }
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

//    private void carregarFornecedor() {
//        modelo = new ComboModelo<Fornecedor>(new DAO().listar(Fornecedor.class), cbFornecedores);
//        cbFornecedores.setModel(modelo);
//    }
    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSalvar) {
                salvar();
            } else if (e.getSource() == btnCancelar) {
                dispose();
            } else if (e.getSource() == btnImprimir) {
                ApresentacaoUtil.exibirInformacao("Relatorio vai ser feito depois", DialogoEditarContaReceber.this);
            } else if (e.getSource() == btnConta) {
                pegarConta();
            } else if (e.getSource() == btnNumeroDocumento) {
                trocarFormaPagamento();
            }
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
                            txtHistorico.setText(conta.getNome());
                        } else {
                            ApresentacaoUtil.exibirErro("Código Inexistente!", DialogoEditarContaReceber.this);
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
        public void configurar() {
            ApresentacaoUtil.adicionarListener(
                    ApresentacaoUtil.transferidorFocoEnter, DialogoEditarContaReceber.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, DialogoEditarContaReceber.this, JTextField.class);

            btnSalvar.addActionListener(this);
            btnCancelar.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnConta.addActionListener(this);
            txtConta.addFocusListener(this);
            btnNumeroDocumento.addActionListener(this);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        painelContaPagar = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtData = new net.sf.nachocalendar.components.DateField();
        txtValor = new javax.swing.JTextField();
        txtConta = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnNumeroDocumento = new javax.swing.JToggleButton();
        jLabel4 = new javax.swing.JLabel();
        txtFornecedor = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Editar Conta a Receber");
        setModal(true);

        painelContaPagar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Valor:");

        jLabel1.setText("Data Vencimento:");

        txtData.setFocusable(false);
        txtData.setName("data"); // NOI18N
        txtData.setRequestFocusEnabled(false);

        txtValor.setName("Valor"); // NOI18N

        txtConta.setName("Conta"); // NOI18N

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Fornecedor/Beneficiário:");

        txtNumeroDocumento.setName("documento"); // NOI18N

        btnNumeroDocumento.setText("Nº Doc");
        btnNumeroDocumento.setToolTipText("Clique para alternar o tipo de Registro!");
        btnNumeroDocumento.setBorderPainted(false);
        btnNumeroDocumento.setContentAreaFilled(false);
        btnNumeroDocumento.setFocusPainted(false);
        btnNumeroDocumento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNumeroDocumentoActionPerformed(evt);
            }
        });

        jLabel4.setText("Histórico:");

        txtFornecedor.setName("Histórico"); // NOI18N

        javax.swing.GroupLayout painelContaPagarLayout = new javax.swing.GroupLayout(painelContaPagar);
        painelContaPagar.setLayout(painelContaPagarLayout);
        painelContaPagarLayout.setHorizontalGroup(
            painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContaPagarLayout.createSequentialGroup()
                .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(painelContaPagarLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(txtHistorico))
                        .addGroup(painelContaPagarLayout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(painelContaPagarLayout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addGap(71, 71, 71)
                                    .addComponent(btnNumeroDocumento)
                                    .addGap(33, 33, 33)
                                    .addComponent(btnConta)
                                    .addGap(33, 33, 33)
                                    .addComponent(jLabel3))
                                .addGroup(painelContaPagarLayout.createSequentialGroup()
                                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(6, 6, 6)
                                    .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(6, 6, 6)
                                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(painelContaPagarLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel4))
                        .addGroup(painelContaPagarLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel2)))
                    .addGroup(painelContaPagarLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtFornecedor, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)))
                .addContainerGap())
        );
        painelContaPagarLayout.setVerticalGroup(
            painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContaPagarLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(painelContaPagarLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(btnNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnConta)
                    .addComponent(jLabel3))
                .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelContaPagarLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(painelContaPagarLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelContaPagarLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(painelContaPagarLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnSalvar.setText("Salvar");
        btnSalvar.setToolTipText("Salvar");
        btnSalvar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 72, 11, 0);
        jPanel1.add(btnSalvar, gridBagConstraints);

        btnCancelar.setText("Cancelar");
        btnCancelar.setToolTipText("Gravar Cheques");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 44, 11, 0);
        jPanel1.add(btnCancelar, gridBagConstraints);

        btnImprimir.setText("Imprimir");
        btnImprimir.setToolTipText("Imprimir Cheque");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 45, 11, 54);
        jPanel1.add(btnImprimir, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, 0, 0, Short.MAX_VALUE)
                    .addComponent(painelContaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelContaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNumeroDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNumeroDocumentoActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_btnNumeroDocumentoActionPerformed
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JToggleButton btnNumeroDocumento;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel painelContaPagar;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtFornecedor;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
