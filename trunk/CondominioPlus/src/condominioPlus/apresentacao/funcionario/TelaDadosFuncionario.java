/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaDadosFuncionario.java
 *
 * Created on Aug 5, 2010, 11:49:35 AM
 */
package condominioPlus.apresentacao.funcionario;

import condominioPlus.Main;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.funcionario.CategoriaFuncionario;
import condominioPlus.negocio.funcionario.Funcionario;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.Identificavel;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.usuario.Usuario;
import logicpoint.util.ComboModelo;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class TelaDadosFuncionario extends javax.swing.JInternalFrame implements Identificavel<Funcionario> {

    private ControladorEventos controlador;
    private Funcionario funcionario;

    /** Creates new form TelaDadosFuncionario2 */
    public TelaDadosFuncionario(Funcionario funcionario) {
        controlador = new ControladorEventos();

        this.funcionario = funcionario;

        carregarComboSexo();

        try {
            carregarComboCategoria();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
        preencher();
    }

    public Funcionario getIdentificacao() {
        return funcionario;
    }

    private void carregarComboCategoria() {
        ComboModelo modelo = new ComboModelo(new DAO().listar(CategoriaFuncionario.class));
        cmbCategoria.setModel(modelo);
    }

    private void carregarComboSexo() {
        cmbSexo.setModel(new ComboModelo<String>(Util.toList(new String[]{"Masculino", "Feminino"}), false));
    }

    private void preencher() {
        controlador.preencher(funcionario);
    }

    private boolean validarCampos() {
        boolean valido = true;
        String mensagem = "";

        if (funcionario.getCategoria() == null) {
            mensagem += "\n- Selecione uma Categoria";
            valido = false;
        }

        try {
            Funcionario f = new DAO().localizar(Funcionario.class, "FuncionarioPorLogin", funcionario.getUsuario().getUsuario());
            if (f != null && !f.equals(funcionario)) {
                valido = false;
                mensagem += "\n- Usuário já existe.";
            }
        } catch (Throwable t) {
            valido = false;
            new TratadorExcecao(t, this, true);
        }

        if (!valido) {
            mensagem = "Corrija os campos abaixo:" + mensagem;
            JOptionPane.showMessageDialog(this, mensagem, "Salvar Funcionário", JOptionPane.WARNING_MESSAGE);
        }

        return valido;
    }

    private void salvar() {
        try {
            controlador.capturar(funcionario);

            if (!validarCampos()) {
                return;
            }

            TipoAcesso tipo = null;
            if (funcionario.getId() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }

            DAO dao = new DAO(false);
            dao.salvar(funcionario);
            dao.concluirTransacao();
            TelaPrincipal.getInstancia().notificarClasse(funcionario);
            String descricao = "Cadastro do Funcionário " + funcionario.getNome() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

            if (funcionario.equals(Main.getFuncionario())) {
                TelaPrincipal.getInstancia().recarregarFuncionario();
            }
            fechar();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void fechar() {
        this.doDefaultCloseAction();
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaDadosFuncionario.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, TelaDadosFuncionario.this, JTextField.class);

            put(Funcionario.class, TelaDadosFuncionario.this);
            put(Usuario.class, painelFuncionario);
            put(Endereco.class, painelEndereco);
            btnSalvar.addActionListener(this);
            btnVoltar.addActionListener(this);
            chkLogin.addActionListener(this);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSalvar) {
                salvar();
            }
            if (e.getSource() == btnVoltar) {
                fechar();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        dateField1 = new net.sf.nachocalendar.components.DateField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        cmbSexo = new javax.swing.JComboBox();
        painelFuncionario = new javax.swing.JPanel();
        txtUsuario = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        chkLogin = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        txtSenha = new javax.swing.JPasswordField();
        jPanel4 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        painelEndereco = new condominioPlus.apresentacao.PainelEndereco();

        setPreferredSize(new java.awt.Dimension(540, 370));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Dados Gerais"));

        jLabel1.setText("Nome");

        txtNome.setName("nome"); // NOI18N

        jLabel5.setText("CPF");

        jLabel6.setText("Identidade");

        jLabel7.setText("Data Nascimento");

        dateField1.setFont(new java.awt.Font("Verdana", 0, 11));
        dateField1.setName("dataNascimento"); // NOI18N

        jTextField3.setName("cpf"); // NOI18N

        jTextField4.setName("identidade"); // NOI18N

        jLabel11.setText("Estado Civil");

        jTextField8.setName("estadoCivil"); // NOI18N

        jLabel12.setText("Categoria");

        cmbCategoria.setName("categoria"); // NOI18N

        jLabel14.setText("Sexo");

        cmbSexo.setName("sexo"); // NOI18N

        txtUsuario.setName("usuario"); // NOI18N

        jLabel3.setText("Senha");

        chkLogin.setFont(new java.awt.Font("Arial", 0, 10));
        chkLogin.setText("Permitir Login");
        chkLogin.setName("habilitado"); // NOI18N

        jLabel2.setText("Usuário");

        txtSenha.setName("senha"); // NOI18N

        javax.swing.GroupLayout painelFuncionarioLayout = new javax.swing.GroupLayout(painelFuncionario);
        painelFuncionario.setLayout(painelFuncionarioLayout);
        painelFuncionarioLayout.setHorizontalGroup(
            painelFuncionarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelFuncionarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelFuncionarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelFuncionarioLayout.createSequentialGroup()
                        .addGap(378, 378, 378)
                        .addComponent(chkLogin))
                    .addGroup(painelFuncionarioLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelFuncionarioLayout.setVerticalGroup(
            painelFuncionarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelFuncionarioLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelFuncionarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(txtSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cmbSexo, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtNome))
                .addGap(28, 28, 28)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                            .addComponent(cmbCategoria, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateField1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addComponent(painelFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(cmbSexo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(dateField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setLayout(new java.awt.GridBagLayout());

        btnSalvar.setText("Salvar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel4.add(btnSalvar, gridBagConstraints);

        btnVoltar.setText("Voltar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(btnVoltar, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Endereço"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(painelEndereco, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(painelEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox chkLogin;
    private javax.swing.JComboBox cmbCategoria;
    private javax.swing.JComboBox cmbSexo;
    private net.sf.nachocalendar.components.DateField dateField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField8;
    private condominioPlus.apresentacao.PainelEndereco painelEndereco;
    private javax.swing.JPanel painelFuncionario;
    private javax.swing.JTextField txtNome;
    private javax.swing.JPasswordField txtSenha;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
