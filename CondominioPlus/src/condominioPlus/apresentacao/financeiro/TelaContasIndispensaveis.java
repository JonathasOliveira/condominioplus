/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaPoupanca.java
 *
 * Created on 21/02/2011, 15:51:51
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaIndispensavel;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author eugenia
 */
public class TelaContasIndispensaveis extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private Conta conta;
    private ContaIndispensavel contaIndispensavel;
    private TabelaModelo_2<ContaIndispensavel> modeloTabela;
    private List<ContaIndispensavel> listaContasIndispensaveis;

    /** Creates new form TelaContasIndispensaveis */
    public TelaContasIndispensaveis(Condominio condominio) {

        this.condominio = condominio;

        initComponents();
        new ControladorEventos();
        painelDadosContaIndispensavel.setVisible(false);
        desabilitarCampos();
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Contas Indispensáveis - " + condominio.getRazaoSocial());
        }
    }

    public void carregarTabela() {

        modeloTabela = new TabelaModelo_2<ContaIndispensavel>(tabelaContasIndispensaveis, "Conta, Histórico, Data Vencimento".split(",")) {

            @Override
            protected List<ContaIndispensavel> getCarregarObjetos() {
                return getContasIndispensaveis();
            }

            @Override
            public Object getValor(ContaIndispensavel contaIndispensavel, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return contaIndispensavel.getConta().getCodigo();
                    case 1:
                        return contaIndispensavel.getConta().getNome();
                    case 2:
                        return DataUtil.getDateTime(contaIndispensavel.getDataVencimento());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        esquerda.setHorizontalAlignment(SwingConstants.LEFT);

        tabelaContasIndispensaveis.getColumn(modeloTabela.getCampo(0)).setCellRenderer(esquerda);
        tabelaContasIndispensaveis.getColumn(modeloTabela.getCampo(1)).setMinWidth(300);

    }

    private List<ContaIndispensavel> getContasIndispensaveis() {
        listaContasIndispensaveis = new DAO().listar("ContasDispensaveisPorCondominio", condominio);
        return listaContasIndispensaveis;
    }

    private void desabilitarCampos() {
        txtConta.setEnabled(false);
        txtHistorico.setEnabled(false);
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtConta);
        campos.add(txtHistorico);
        return campos;
    }

    private boolean verificarContasIndispensaveis() {
        for (ContaIndispensavel c : condominio.getContasIndispensaveis()) {
            if (c.getConta().getCodigo() == conta.getCodigo()) {
                return true;
            }
        }
        return false;
    }

    private void preencherObjeto() {
        contaIndispensavel = new ContaIndispensavel();
        contaIndispensavel.setCondominio(condominio);
        contaIndispensavel.setConta(conta);
        contaIndispensavel.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
        condominio.getContasIndispensaveis().add(contaIndispensavel);
        new DAO().salvar(condominio);
        limparCampos();
    }

    private void limparCampos() {
        txtHistorico.setText("");
        txtConta.setText("");
    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }

            if (verificarContasIndispensaveis()) {
                ApresentacaoUtil.exibirAdvertencia("Já existe uma conta indispensável com as mesmas características.", this);
                return;
            }

            preencherObjeto();

            TipoAcesso tipo = null;
            if (condominio.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }


            String descricao = "Cadastro do Condominio " + condominio.getRazaoSocial() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Desejar remover os registros selecionados?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<ContaIndispensavel> itensRemover = modeloTabela.getObjetosSelecionados();

            for (ContaIndispensavel c : itensRemover) {
                modeloTabela.remover(c);
                modeloTabela.notificar();
                new DAO().remover(c);
            }

            condominio.setContasIndispensaveis(modeloTabela.getObjetos());

            painelDadosContaIndispensavel.setVisible(false);
            ApresentacaoUtil.exibirInformacao("Registros removidos com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false);
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            txtHistorico.setText(conta.getNome());
        }
    }

    private Conta pesquisarContaPorCodigo(int codigo) {
        Conta c = null;
        try {
            c = (Conta) new DAO().localizar(Conta.class, codigo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void exibirPainelContaIndispensavel(ContaIndispensavel c) {
        if (c != null) {
            painelDadosContaIndispensavel.setVisible(true);
            contaIndispensavel = c;
            desabilitarCamposContrato();
            preencherPainelContaIndispensavel(c);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione um contrato!", this);
        }

    }

    private void desabilitarCamposContrato() {
        txtCodigoContra.setEnabled(false);
        txtHistoricoConta.setEnabled(false);
    }

    public void preencherPainelContaIndispensavel(ContaIndispensavel c) {
        txtDataVencimentoIndispensavel.setValue(DataUtil.toString(c.getDataVencimento()));
        txtCodigoContra.setText(String.valueOf(c.getConta().getCodigo()));
        txtHistoricoConta.setText(c.getConta().getNome());
    }

    private void cancelar() {
        painelDadosContaIndispensavel.setVisible(false);
        carregarTabela();
    }

    private void salvarContaIndispensavel() {
        contaIndispensavel.setDataVencimento(DataUtil.getCalendar(txtDataVencimentoIndispensavel.getValue()));
        new DAO().salvar(contaIndispensavel);
        carregarTabela();

        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso!", this);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnConta.addActionListener(this);
            btnIncluir.addActionListener(this);
            txtConta.addFocusListener(this);
            tabelaContasIndispensaveis.addMouseListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            itemMenuEditarContaIndispensavel.addActionListener(this);
            btnVoltar.addActionListener(this);
            btnSalvar.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();
            if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnIncluir) {
                salvar();
                carregarTabela();
            } else if (origem == itemMenuRemoverSelecionados) {
                apagarItensSelecionados();
            } else if (origem == itemMenuEditarContaIndispensavel) {
                exibirPainelContaIndispensavel(modeloTabela.getObjetoSelecionado());
            } else if (origem == btnVoltar) {
                cancelar();
            } else if (origem == btnSalvar) {
                salvarContaIndispensavel();
            }

        }

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == txtConta) {
                Conta resultado = null;
                if (new LimitarCaracteres(10).ValidaNumero(txtConta)) {
                    if (!txtConta.getText().equals("") && txtConta.getText() != null) {
                        resultado = pesquisarContaPorCodigo(Integer.valueOf(txtConta.getText()));
                        if (resultado != null) {
                            conta = resultado;
                            txtConta.setText(String.valueOf(conta.getCodigo()));
                            txtHistorico.setText(conta.getNome());
                        } else {

                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaContasIndispensaveis.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Object origem = e.getSource();
            if (origem == tabelaContasIndispensaveis && painelDadosContaIndispensavel.isVisible()) {
                exibirPainelContaIndispensavel(modeloTabela.getObjetoSelecionado());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
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

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuEditarContaIndispensavel = new javax.swing.JMenuItem();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtDataVencimento = new net.sf.nachocalendar.components.DateField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtConta = new javax.swing.JTextField();
        btnIncluir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContasIndispensaveis = new javax.swing.JTable();
        painelDadosContaIndispensavel = new javax.swing.JPanel();
        txtDataVencimentoIndispensavel = new net.sf.nachocalendar.components.DateField();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtCodigoContra = new javax.swing.JTextField();
        txtHistoricoConta = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();

        itemMenuEditarContaIndispensavel.setText("Editar Conta Indispensável");
        itemMenuEditarContaIndispensavel.setActionCommand("");
        popupMenu.add(itemMenuEditarContaIndispensavel);

        itemMenuRemoverSelecionados.setText("Deletar Itens Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        setClosable(true);
        setResizable(true);
        setTitle("Contas Indispensáveis");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Data Vencimento:");

        txtDataVencimento.setFocusable(false);
        txtDataVencimento.setRequestFocusEnabled(false);

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Histórico:");

        txtConta.setName("Conta"); // NOI18N

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDataVencimento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnConta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaContasIndispensaveis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContasIndispensaveis);

        painelDadosContaIndispensavel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Data Vencimento:");

        jLabel10.setText("Descrição:");

        jLabel11.setText("Conta:");

        btnSalvar.setText("Salvar");

        btnVoltar.setText("Voltar");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(173, 173, 173)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVoltar)
                .addContainerGap(182, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(btnSalvar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelDadosContaIndispensavelLayout = new javax.swing.GroupLayout(painelDadosContaIndispensavel);
        painelDadosContaIndispensavel.setLayout(painelDadosContaIndispensavelLayout);
        painelDadosContaIndispensavelLayout.setHorizontalGroup(
            painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosContaIndispensavelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(painelDadosContaIndispensavelLayout.createSequentialGroup()
                        .addGroup(painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDataVencimentoIndispensavel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCodigoContra, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtHistoricoConta, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                            .addComponent(jLabel10))))
                .addContainerGap())
        );
        painelDadosContaIndispensavelLayout.setVerticalGroup(
            painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDadosContaIndispensavelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelDadosContaIndispensavelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelDadosContaIndispensavelLayout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addGap(8, 8, 8)
                            .addComponent(txtDataVencimentoIndispensavel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(painelDadosContaIndispensavelLayout.createSequentialGroup()
                            .addComponent(jLabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtCodigoContra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(painelDadosContaIndispensavelLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHistoricoConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(118, 118, 118))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(painelDadosContaIndispensavel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addGap(13, 13, 13)
                .addComponent(painelDadosContaIndispensavel, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JMenuItem itemMenuEditarContaIndispensavel;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel painelDadosContaIndispensavel;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaContasIndispensaveis;
    private javax.swing.JTextField txtCodigoContra;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataVencimento;
    private net.sf.nachocalendar.components.DateField txtDataVencimentoIndispensavel;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtHistoricoConta;
    // End of variables declaration//GEN-END:variables
}
