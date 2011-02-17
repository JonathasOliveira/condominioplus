/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaDadosCondominio.java
 *
 * Created on Aug 6, 2010, 1:06:37 PM
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ComboModelo_2;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;

/**
 *
 * @author Administrador
 */
public class TelaDadosConta extends javax.swing.JInternalFrame {

    private Conta conta;
    private ControladorEventos controlador;
    private ComboModelo_2<Conta> modelo;

    /** Creates new form TelaDadosCondominio */
    public TelaDadosConta(Conta conta) {
        this.conta = conta;

        initComponents();
        desativarRadios();

        carregarComboConta();

        controlador = new ControladorEventos();

        if (this.conta != null) {
            preencherTela(this.conta);
        }

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtNome);
        return campos;
    }

    private void salvar() {
        try {

            if (estaVinculada()) {
                salvarVinculo();
            }
            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }

            preencherObjeto();

            TipoAcesso tipo = null;
            if (conta.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }

            DAO dao = new DAO(false);
            dao.salvar(conta);
            dao.concluirTransacao();

            TelaPrincipal.getInstancia().notificarClasse(conta);

            String descricao = "Cadastro do Contas " + conta.getNome() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

            sair();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void sair() {
        this.doDefaultCloseAction();
    }

    private void preencherTela(Conta conta) {
        txtCodigo.setText(Integer.toString(conta.getCodigo()));
        txtNome.setText(conta.getNome());
        if (conta.isVinculada()) {
            checkBoxVinculada.setSelected(true);
            ativarRadios();
            if (conta.getNomeVinculo().equals("CO")) {
                radioConsignacoes.setSelected(true);
            } else if (conta.getNomeVinculo().equals("EM")) {
                radioEmprestimos.setSelected(true);
            } else if (conta.getNomeVinculo().equals("PO")) {
                radioPoupanca.setSelected(true);
            } else if (conta.getNomeVinculo().equals("AF")) {
                radioAplicacoesFinancas.setSelected(true);
            }
        }

        if (conta.isCredito()) {
            radioCredito.setSelected(true);
        } else {
            RadioDebito.setSelected(true);
        }
        if (conta.getContaVinculada() != null) {
            modelo.setSelectedItem(conta.getContaVinculada());
        }

        System.out.println("conta: " + conta);
        System.out.println("conta vinculada: " + conta.getContaVinculada());
    }

    private void autoRelacionar() {

        if (modelo.getLinhaSelecionada() != -1) {
            Conta contaVinculo = modelo.getObjetoSelecionado();
            if (contaVinculo != null) {
                if (conta.verificarNome(contaVinculo)) {
                    ApresentacaoUtil.exibirAdvertencia("Uma conta não pode estar vinculada a ela mesma!", this);
                    return;
                }
                if (conta.verificarTipo(contaVinculo)) {
                    ApresentacaoUtil.exibirAdvertencia("Essa conta só pode estar associada a outra com tipo diferente!", this);
                    return;
                }
            }
            if (conta.getCodigo() == 0 && contaVinculo != null) {
                conta.setContaVinculada(contaVinculo);
                new DAO().salvar(conta);
                contaVinculo.setContaVinculada(conta);
                new DAO().salvar(contaVinculo);
                TelaPrincipal.getInstancia().notificarClasse(contaVinculo);

            } else if (conta.getCodigo() != 0){
                Conta contaJaVinculada = conta.getContaVinculada();
                conta.setContaVinculada(contaVinculo);
                if (contaVinculo != null) {
                    contaVinculo.setContaVinculada(conta);

                    if (contaJaVinculada != null) {
                        contaJaVinculada.setContaVinculada(null);
                        new DAO().salvar(contaJaVinculada);
                        TelaPrincipal.getInstancia().notificarClasse(contaJaVinculada);
                    }

                } else {
                    if (contaJaVinculada != null) {
                        contaJaVinculada.setContaVinculada(null);
                        new DAO().salvar(contaJaVinculada);
                        TelaPrincipal.getInstancia().notificarClasse(contaJaVinculada);
                    }
                }
            }

        }

    }

    private void preencherObjeto() {
        conta.setNome(txtNome.getText());

        if (radioCredito.isSelected()) {
            conta.setCredito(true);
        } else {
            conta.setCredito(false);
        }

        autoRelacionar();
    }

    private void desativarRadios() {
        radioAplicacoesFinancas.setEnabled(false);
        radioConsignacoes.setEnabled(false);
        radioEmprestimos.setEnabled(false);
        radioPoupanca.setEnabled(false);
    }

    private void ativarRadios() {
        radioAplicacoesFinancas.setEnabled(true);
        radioConsignacoes.setEnabled(true);
        radioEmprestimos.setEnabled(true);
        radioPoupanca.setEnabled(true);
    }

    private void salvarVinculo() {
        if (radioPoupanca.isSelected()) {
            conta.setNomeVinculo("PO");
        } else if (radioAplicacoesFinancas.isSelected()) {
            conta.setNomeVinculo("AF");
        } else if (radioConsignacoes.isSelected()) {
            conta.setNomeVinculo("CO");
        } else if (radioEmprestimos.isSelected()) {
            conta.setNomeVinculo("EM");
        }
    }

    private boolean estaVinculada() {
        if (checkBoxVinculada.isSelected()) {
            conta.setVinculada(true);
            ativarRadios();
            return true;
        } else {
            desativarRadios();
            return false;
        }
    }

    private void carregarComboConta() {
        modelo = new ComboModelo_2<Conta>(cmbContaVinculada, new DAO().listar(Conta.class));
        modelo.isPermitirSelecaoNula();
        cmbContaVinculada.setModel(modelo);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSalvar) {
                salvar();
                TelaPrincipal.getInstancia().notificarClasse(conta);
            } else if (e.getSource() == checkBoxVinculada) {
                estaVinculada();
            } else if (e.getSource() == btnVoltar) {
                sair();
            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaDadosConta.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, TelaDadosConta.this, JTextField.class);

            btnSalvar.addActionListener(this);
            btnVoltar.addActionListener(this);
            checkBoxVinculada.addActionListener(this);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel11 = new javax.swing.JPanel();
        btnGroupTipo = new javax.swing.ButtonGroup();
        btnGroupContaVinculada = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        radioCredito = new javax.swing.JRadioButton();
        RadioDebito = new javax.swing.JRadioButton();
        checkBoxVinculada = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        radioPoupanca = new javax.swing.JRadioButton();
        radioConsignacoes = new javax.swing.JRadioButton();
        radioAplicacoesFinancas = new javax.swing.JRadioButton();
        radioEmprestimos = new javax.swing.JRadioButton();
        txtNome = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cmbContaVinculada = new javax.swing.JComboBox();
        jPanel12 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setClosable(true);
        setTitle("Cadastro de Contas");
        setPreferredSize(new java.awt.Dimension(580, 258));

        jPanel1.setPreferredSize(new java.awt.Dimension(679, 439));

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("Código:");

        txtCodigo.setEditable(false);

        jLabel2.setText("Descrição:");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipo de Conta"));

        btnGroupTipo.add(radioCredito);
        radioCredito.setSelected(true);
        radioCredito.setText("Crédito");

        btnGroupTipo.add(RadioDebito);
        RadioDebito.setText("Débito");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioCredito)
                    .addComponent(RadioDebito))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioCredito)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RadioDebito)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        checkBoxVinculada.setText("Esta Conta está vinculada a:");

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        btnGroupContaVinculada.add(radioPoupanca);
        radioPoupanca.setText("Poupança");

        btnGroupContaVinculada.add(radioConsignacoes);
        radioConsignacoes.setText("Consignações");

        btnGroupContaVinculada.add(radioAplicacoesFinancas);
        radioAplicacoesFinancas.setText("Aplicações Financeiras");

        btnGroupContaVinculada.add(radioEmprestimos);
        radioEmprestimos.setText("Empréstimos");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioPoupanca)
                    .addComponent(radioAplicacoesFinancas))
                .addGap(44, 44, 44)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioEmprestimos)
                    .addComponent(radioConsignacoes))
                .addContainerGap(64, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioPoupanca)
                    .addComponent(radioConsignacoes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioAplicacoesFinancas)
                    .addComponent(radioEmprestimos))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtNome.setName("Descrição"); // NOI18N

        jLabel3.setText("Conta:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbContaVinculada, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtNome, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxVinculada))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cmbContaVinculada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxVinculada)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setLayout(new java.awt.GridBagLayout());

        btnSalvar.setText("Salvar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 262, 11, 0);
        jPanel12.add(btnSalvar, gridBagConstraints);

        btnVoltar.setText("Voltar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 18, 11, 268);
        jPanel12.add(btnVoltar, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton RadioDebito;
    private javax.swing.ButtonGroup btnGroupContaVinculada;
    private javax.swing.ButtonGroup btnGroupTipo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox checkBoxVinculada;
    private javax.swing.JComboBox cmbContaVinculada;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton radioAplicacoesFinancas;
    private javax.swing.JRadioButton radioConsignacoes;
    private javax.swing.JRadioButton radioCredito;
    private javax.swing.JRadioButton radioEmprestimos;
    private javax.swing.JRadioButton radioPoupanca;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}

