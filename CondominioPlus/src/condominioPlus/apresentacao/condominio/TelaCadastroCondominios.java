/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaCadastroCondominios.java
 *
 * Created on May 27, 2010, 4:19:34 PM
 */
package condominioPlus.apresentacao.condominio;

import avant.view.AvantViewUtil;
import avant.view.model.AvantListModel;
import avant.view.model.AvantTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.Telefone;

import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import logicpoint.persistencia.DAO;

/**
 *
 * @author thiagocifani
 */
public class TelaCadastroCondominios extends JInternalFrame {

    private Condominio condominio;
    private Endereco endereco;
    private AvantTableModel<Condominio> modelo;
    private AvantListModel<Telefone> modeloTelefone;
    private Telefone telefone;

    /** Creates new form TelaCadastroCondominios */
    //Deve receber um condominio e um modelo para atualização
    public TelaCadastroCondominios(Condominio condominio, AvantTableModel<Condominio> modelo) {
        initComponents();
        this.condominio = condominio;
        this.modelo = modelo;
        txtCnpj.requestFocus();
        txtUf.setDocument(new LimitarCaracteres(2));
        txtNumero.setDocument(new LimitarCaracteres(5));
        preencherTela();
        AdicionarOuvintes();
    }

    private void AdicionarOuvintes() {
        ControladorDeEventos controlador = new ControladorDeEventos();
        btnSalvar.addActionListener(controlador);
        btnCancelar.addActionListener(controlador);
        btnRemover.addActionListener(controlador);
//        btnAdicionarTelefone.addActionListener(controlador);
        btnEditarTelefone.addActionListener(controlador);
        btnRemoverTelefone.addActionListener(controlador);
    }

   

    private void preencherTela() {
        if (condominio != null) {
            List<Telefone> telefones = condominio.getTelefones();
//            modeloTelefone = new AvantListModel<Telefone>(telefones, telefones);
            txtCnpj.setText(condominio.getCnpj());
            txtRazaoSocial.setText(condominio.getRazaoSocial());
            txtEmail.setText(condominio.getEmail());
            txtContato.setText(condominio.getContato());
            txtZelador.setText(condominio.getZelador());
            txtSite.setText(condominio.getSite());
            checkBoxAtivo.setSelected(condominio.isAtivo());

            if (condominio.getEndereco() != null) {
                txtRua.setText(condominio.getEndereco().getLogradouro());
                txtNumero.setText(condominio.getEndereco().getNumero());
                txtBairro.setText(condominio.getEndereco().getBairro());
                txtComplemento.setText(condominio.getEndereco().getComplemento());
                txtCidade.setText(condominio.getEndereco().getCidade());
                txtUf.setText(condominio.getEndereco().getEstado());
                txtCep.setText(condominio.getEndereco().getCep());
                textAreaAnotacoes.setText(condominio.getAnotacoes());
            }
        }
    }

    private void preencherObjeto() {

        condominio.setRazaoSocial(txtRazaoSocial.getText().trim().toUpperCase());
        condominio.setCnpj(txtCnpj.getText());
        condominio.setEmail(txtEmail.getText().trim().toLowerCase());
        condominio.setContato(txtContato.getText().trim().toUpperCase());
        condominio.setZelador(txtZelador.getText().trim().toUpperCase());
        condominio.setSite(txtSite.getText().trim().toLowerCase());
        condominio.setAnotacoes(textAreaAnotacoes.getText());
        condominio.setAtivo(checkBoxAtivo.isSelected());
        endereco = new Endereco();
        endereco.setLogradouro(txtRua.getText().trim().toUpperCase());
        endereco.setNumero(txtNumero.getText().trim().toUpperCase());
        endereco.setBairro(txtBairro.getText().trim().toUpperCase());
        endereco.setCidade(txtCidade.getText().trim().toUpperCase());
        endereco.setComplemento(txtComplemento.getText().trim().toUpperCase());
        endereco.setEstado(txtUf.getText().trim().toUpperCase());
        endereco.setCep(txtCep.getText());
        condominio.setEndereco(endereco);

    }

    private List pegarListaDeCampos() {
        List campos = new ArrayList<Object>();
        campos.add(txtRazaoSocial);
        campos.add(txtEmail);
        campos.add(txtCnpj);
        campos.add(checkBoxAtivo);

        return campos;
    }

    private void fechar() {
        this.doDefaultCloseAction();
    }

    private void excluir() {
        try {
            if (condominio != null) {
                new DAO().remover(condominio);
//                TelaPrincipal2.reloadComboCondominios();
                AvantViewUtil.perguntar("Tem certeza que deseja remover o condomínio?", this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AvantViewUtil.exibirInformacao("Problema ao remover Condominio!", this);
        }
    }

    private void salvar() {
        ValidadorGenerico validador = new ValidadorGenerico();
        try {
            if (validador.validar(pegarListaDeCampos())) {
                preencherObjeto();
                if (condominio.getCodigo() == 0) {
                    new DAO().salvar(condominio);
                    modelo.adicionar(condominio);
//                    TelaPrincipal2.reloadComboCondominios();
                    AvantViewUtil.exibirInformacao("Dados Salvos com Sucesso!", this);
                    this.doDefaultCloseAction();
                } else {
                    //editar
                    new DAO().salvar(condominio);
                    modelo.notificar(condominio);
//                    TelaPrincipal2.reloadComboCondominios();
                    AvantViewUtil.exibirInformacao("Dados atualizados com Sucesso!", this);
                    this.doDefaultCloseAction();
                }
            } else {
                validador.exibirErros(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AvantViewUtil.exibirErro("Não foi possível conectar a base de dados!", this);
        }
    }

    private class ControladorDeEventos implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();

            if (origem == btnSalvar) {
                salvar();
            } else if (origem == btnCancelar) {
                fechar();
            } else if (origem == btnRemover) {
                excluir();
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
        java.awt.GridBagConstraints gridBagConstraints;

        tabCondominios = new javax.swing.JTabbedPane();
        jPanel10 = new javax.swing.JPanel();
        panelContatos = new javax.swing.JPanel();
        btnEditarTelefone = new javax.swing.JButton();
        btnRemoverTelefone = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtContato = new javax.swing.JTextField();
        txtZelador = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtSite = new javax.swing.JTextField();
        panelDadosBancários = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtNumeroBanco = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtAgencia = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtContaCorrente = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtContaPoupanca = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cbNomeDoBanco = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtUsuarioBanking = new javax.swing.JTextField();
        txtSenha = new javax.swing.JTextField();
        txtCpfBanking = new javax.swing.JTextField();
        txtLimiteChequeEspecial = new javax.swing.JTextField();
        panelCoselheiros = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbConselheiros = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btnAdicionarConselheiro = new javax.swing.JButton();
        btnRemoverConselheiro = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        txtResponsaveisCheques = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        cbInstrumento = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtResponsavelCNPJCondominio = new javax.swing.JTextField();
        txtCpfResponsavelCondominio = new javax.swing.JTextField();
        panelAnotacoes = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textAreaAnotacoes = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        txtRazaoSocial = new javax.swing.JTextField();
        txtSindico = new javax.swing.JTextField();
        txtRua = new javax.swing.JTextField();
        txtNumero = new javax.swing.JTextField();
        txtComplemento = new javax.swing.JTextField();
        txtBairro = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtCidade = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtUf = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        checkBoxAtivo = new javax.swing.JCheckBox();
        txtCnpj = new javax.swing.JFormattedTextField();
        txtCep = new javax.swing.JFormattedTextField();
        jPanel9 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnRemover = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Cadastro de Condomínio");

        panelContatos.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), "Telefones"));

        btnEditarTelefone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/editar.png"))); // NOI18N
        btnEditarTelefone.setToolTipText("Editar Telefone");

        btnRemoverTelefone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/remover.png"))); // NOI18N
        btnRemoverTelefone.setToolTipText("Remover Telefone");

        org.jdesktop.layout.GroupLayout panelContatosLayout = new org.jdesktop.layout.GroupLayout(panelContatos);
        panelContatos.setLayout(panelContatosLayout);
        panelContatosLayout.setHorizontalGroup(
            panelContatosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelContatosLayout.createSequentialGroup()
                .addContainerGap(294, Short.MAX_VALUE)
                .add(panelContatosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(btnRemoverTelefone, 0, 0, Short.MAX_VALUE)
                    .add(btnEditarTelefone, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelContatosLayout.setVerticalGroup(
            panelContatosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelContatosLayout.createSequentialGroup()
                .add(77, 77, 77)
                .add(btnEditarTelefone)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnRemoverTelefone)
                .addContainerGap())
        );

        panelContatosLayout.linkSize(new java.awt.Component[] {btnEditarTelefone, btnRemoverTelefone}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), "Contatos"));

        jLabel10.setText("Contato:");

        jLabel11.setText("Zelador:");

        txtContato.setName("Contato"); // NOI18N
        txtContato.setNextFocusableComponent(txtZelador);

        txtZelador.setNextFocusableComponent(txtEmail);

        jLabel12.setText("Email:");

        txtEmail.setName("Email"); // NOI18N
        txtEmail.setNextFocusableComponent(txtSite);

        jLabel13.setText("Site:");

        txtSite.setNextFocusableComponent(txtAgencia);

        org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .add(18, 18, 18)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel12)
                    .add(jLabel10)
                    .add(jLabel11)
                    .add(jLabel13))
                .add(18, 18, 18)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(txtEmail)
                        .add(txtZelador)
                        .add(txtContato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 319, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(txtSite, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                .add(22, 22, 22))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel12Layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel10)
                    .add(txtContato, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(txtZelador, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel12)
                    .add(txtEmail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel13)
                    .add(txtSite, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(panelContatos, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, panelContatos, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tabCondominios.addTab("Contatos", jPanel10);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)), "Conta Bancária"));

        jLabel1.setText("Numero do Banco:");

        txtNumeroBanco.setEditable(false);
        txtNumeroBanco.setNextFocusableComponent(txtAgencia);

        jLabel2.setText("Agência:");

        txtAgencia.setEditable(false);
        txtAgencia.setNextFocusableComponent(cbNomeDoBanco);

        jLabel3.setText("Conta Corrente:");

        txtContaCorrente.setNextFocusableComponent(txtUsuarioBanking);

        jLabel4.setText("Conta Poupança:");

        txtContaPoupanca.setNextFocusableComponent(txtContaCorrente);

        jLabel6.setText("Nome do Banco:");

        cbNomeDoBanco.setNextFocusableComponent(txtContaPoupanca);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel6)
                    .add(jLabel4)
                    .add(jLabel3)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbNomeDoBanco, 0, 261, Short.MAX_VALUE)
                    .add(txtContaPoupanca, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .add(txtContaCorrente, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtAgencia, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtNumeroBanco, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
                        .add(107, 107, 107)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(txtNumeroBanco, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtAgencia, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbNomeDoBanco, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(txtContaPoupanca, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(txtContaCorrente, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)), "Internet Banking"));

        jLabel5.setText("Usuário:");

        jLabel7.setText("Senha:");

        jLabel8.setText("CPF:");

        jLabel9.setText("Limite Cheque Especial:");

        txtUsuarioBanking.setNextFocusableComponent(txtSenha);

        txtSenha.setNextFocusableComponent(txtCpfBanking);

        txtCpfBanking.setNextFocusableComponent(txtLimiteChequeEspecial);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel5Layout.createSequentialGroup()
                .add(34, 34, 34)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel7)
                            .add(jLabel8)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txtCpfBanking, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .add(txtSenha, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .add(txtUsuarioBanking, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                        .add(27, 27, 27))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel9)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtLimiteChequeEspecial, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)))
                .add(23, 23, 23))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(txtUsuarioBanking, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(17, 17, 17)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(txtSenha, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(txtCpfBanking, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(txtLimiteChequeEspecial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout panelDadosBancáriosLayout = new org.jdesktop.layout.GroupLayout(panelDadosBancários);
        panelDadosBancários.setLayout(panelDadosBancáriosLayout);
        panelDadosBancáriosLayout.setHorizontalGroup(
            panelDadosBancáriosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelDadosBancáriosLayout.createSequentialGroup()
                .add(20, 20, 20)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelDadosBancáriosLayout.setVerticalGroup(
            panelDadosBancáriosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, panelDadosBancáriosLayout.createSequentialGroup()
                .add(25, 25, 25)
                .add(panelDadosBancáriosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(21, 21, 21))
        );

        tabCondominios.addTab("Dados Bancários", panelDadosBancários);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), "Conselheiros"));

        tbConselheiros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tbConselheiros);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnAdicionarConselheiro.setText("adicionar");
        jPanel1.add(btnAdicionarConselheiro, new java.awt.GridBagConstraints());

        btnRemoverConselheiro.setText("Remover");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 6, 11, 0);
        jPanel1.add(btnRemoverConselheiro, gridBagConstraints);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 425, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), "Responsáveis"));

        jLabel15.setText("Responsáveis assinar cheques:");

        txtResponsaveisCheques.setNextFocusableComponent(cbInstrumento);

        jLabel16.setText("Instrumento:");

        cbInstrumento.setNextFocusableComponent(txtResponsavelCNPJCondominio);

        jLabel17.setText("Responsável pelo CNPJ Condomínio:");

        jLabel18.setText("CPF:");

        txtResponsavelCNPJCondominio.setNextFocusableComponent(txtCpfResponsavelCondominio);

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel15)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtResponsaveisCheques, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .add(jLabel16)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cbInstrumento, 0, 259, Short.MAX_VALUE)
                    .add(jLabel17)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtResponsavelCNPJCondominio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .add(jLabel18)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, txtCpfResponsavelCondominio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(jLabel15)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtResponsaveisCheques, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel16)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbInstrumento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel17)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtResponsavelCNPJCondominio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel18)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtCpfResponsavelCondominio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout panelCoselheirosLayout = new org.jdesktop.layout.GroupLayout(panelCoselheiros);
        panelCoselheiros.setLayout(panelCoselheirosLayout);
        panelCoselheirosLayout.setHorizontalGroup(
            panelCoselheirosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelCoselheirosLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelCoselheirosLayout.setVerticalGroup(
            panelCoselheirosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelCoselheirosLayout.createSequentialGroup()
                .addContainerGap()
                .add(panelCoselheirosLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabCondominios.addTab("Conselheiros/Responsáveis", panelCoselheiros);

        jPanel14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 51, 51), 1, true));

        textAreaAnotacoes.setColumns(20);
        textAreaAnotacoes.setRows(5);
        jScrollPane3.setViewportView(textAreaAnotacoes);

        org.jdesktop.layout.GroupLayout jPanel14Layout = new org.jdesktop.layout.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout panelAnotacoesLayout = new org.jdesktop.layout.GroupLayout(panelAnotacoes);
        panelAnotacoes.setLayout(panelAnotacoesLayout);
        panelAnotacoesLayout.setHorizontalGroup(
            panelAnotacoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelAnotacoesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelAnotacoesLayout.setVerticalGroup(
            panelAnotacoesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panelAnotacoesLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabCondominios.addTab("Anotações", panelAnotacoes);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), "Dados Gerais"));

        jLabel19.setText("Código:");

        jLabel20.setText("Razão Social*");
        jLabel20.setToolTipText("Campo Obrigatório");

        jLabel21.setText("CNPJ:*");
        jLabel21.setToolTipText("Campo Obrigatório");

        jLabel22.setForeground(new java.awt.Color(255, 51, 51));
        jLabel22.setText("Síndico:");

        jLabel23.setText("Endereço:");

        jLabel24.setText("Número:");

        jLabel25.setText("Compl.:");

        jLabel26.setText("Bairro:");

        txtCodigo.setEditable(false);

        txtRazaoSocial.setToolTipText("Digite a Razão Social");
        txtRazaoSocial.setName("Razão Social"); // NOI18N
        txtRazaoSocial.setNextFocusableComponent(txtRua);

        txtSindico.setEditable(false);
        txtSindico.setCaretColor(new java.awt.Color(255, 51, 51));

        txtRua.setToolTipText("Digite o Endereço");
        txtRua.setNextFocusableComponent(txtNumero);

        txtComplemento.setToolTipText("");
        txtComplemento.setNextFocusableComponent(txtBairro);

        txtBairro.setNextFocusableComponent(txtCidade);

        jLabel27.setText("Cidade:");

        txtCidade.setNextFocusableComponent(txtUf);

        jLabel28.setText("UF:");

        jLabel29.setText("CEP:");

        checkBoxAtivo.setText("Condomínio está ativo?");
        checkBoxAtivo.setName("Condomínio Ativo?"); // NOI18N

        try {
            txtCnpj.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtCnpj.setToolTipText("Digite o CNPJ");
        txtCnpj.setName("CNPJ"); // NOI18N
        txtCnpj.setNextFocusableComponent(txtRazaoSocial);

        try {
            txtCep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###-###")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel19)
                    .add(jLabel20)
                    .add(jLabel23)
                    .add(jLabel25)
                    .add(jLabel27))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel8Layout.createSequentialGroup()
                                .add(txtCidade, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(34, 34, 34)
                                .add(jLabel28)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txtUf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(txtComplemento, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .add(txtRua, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .add(txtRazaoSocial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 420, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 27, 27)
                        .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel22)
                            .add(jLabel24)
                            .add(jLabel26)
                            .add(jLabel29))
                        .add(10, 10, 10))
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(txtCodigo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel21)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtCnpj, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(228, 228, 228)))
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(checkBoxAtivo)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(txtBairro, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                        .add(txtSindico, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                    .add(txtNumero, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(txtCep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel19)
                    .add(txtCodigo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel21)
                    .add(txtCnpj, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(checkBoxAtivo))
                .add(8, 8, 8)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel20)
                    .add(txtRazaoSocial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(txtSindico, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel22))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel23)
                    .add(jLabel24)
                    .add(txtRua, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(txtNumero, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel8Layout.createSequentialGroup()
                        .add(7, 7, 7)
                        .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel26)
                            .add(txtBairro, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel25)
                            .add(txtComplemento, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel27)
                        .add(txtCidade, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel28)
                        .add(txtUf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel29)
                        .add(txtCep, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel9.setLayout(new java.awt.GridBagLayout());

        btnSalvar.setText("Salvar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 251, 23, 0);
        jPanel9.add(btnSalvar, gridBagConstraints);

        btnCancelar.setText("Fechar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 18, 23, 0);
        jPanel9.add(btnCancelar, gridBagConstraints);

        btnRemover.setText("Remover");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 18, 23, 256);
        jPanel9.add(btnRemover, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(19, 19, 19)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, tabCondominios, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 775, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(32, 32, 32)
                        .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 762, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(tabCondominios, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 257, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionarConselheiro;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEditarTelefone;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnRemoverConselheiro;
    private javax.swing.JButton btnRemoverTelefone;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox cbInstrumento;
    private javax.swing.JComboBox cbNomeDoBanco;
    private javax.swing.JCheckBox checkBoxAtivo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelAnotacoes;
    private javax.swing.JPanel panelContatos;
    private javax.swing.JPanel panelCoselheiros;
    private javax.swing.JPanel panelDadosBancários;
    private javax.swing.JTabbedPane tabCondominios;
    private javax.swing.JTable tbConselheiros;
    private javax.swing.JTextArea textAreaAnotacoes;
    private javax.swing.JTextField txtAgencia;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JFormattedTextField txtCnpj;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtContaCorrente;
    private javax.swing.JTextField txtContaPoupanca;
    private javax.swing.JTextField txtContato;
    private javax.swing.JTextField txtCpfBanking;
    private javax.swing.JTextField txtCpfResponsavelCondominio;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtLimiteChequeEspecial;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtNumeroBanco;
    private javax.swing.JTextField txtRazaoSocial;
    private javax.swing.JTextField txtResponsaveisCheques;
    private javax.swing.JTextField txtResponsavelCNPJCondominio;
    private javax.swing.JTextField txtRua;
    private javax.swing.JTextField txtSenha;
    private javax.swing.JTextField txtSindico;
    private javax.swing.JTextField txtSite;
    private javax.swing.JTextField txtUf;
    private javax.swing.JTextField txtUsuarioBanking;
    private javax.swing.JTextField txtZelador;
    // End of variables declaration//GEN-END:variables
}
