/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaContaCorrente.java
 *
 * Created on 29/09/2010, 11:39:09
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaPagar;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.fornecedor.Fornecedor;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ComboModelo_2;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class TelaContaPagar extends javax.swing.JInternalFrame {

    private ContaPagar contaPagar;
    private Pagamento pagamento;
    private ComboModelo_2<Fornecedor> modelo;
    private Condominio condominio;
    private Conta conta;
    private TabelaModelo_2 modeloTabela;
    private TabelaModelo_2 modeloTabela2;
    private List<Pagamento> pagamentos;

    /** Creates new form TelaContaCorrente */
    public TelaContaPagar(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getContaPagar() == null) {
            contaPagar = new ContaPagar();
            condominio.setContaPagar(contaPagar);
            new DAO().salvar(condominio);
        } else {
            contaPagar = condominio.getContaPagar();
        }

        initComponents();
        new ControladorEventos();
        carregarFornecedor();
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Contas a Pagar - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabelaContaPagar, "Vencimento, Conta, Fornecedor, Descrição, Valor".split(",")) {

            @Override
            protected Pagamento getAdicionar() {
                editar(new Pagamento());
                return null;
            }

            @Override
            public void editar(Pagamento pagamento) {
//              TelaPrincipal.getInstancia().criarFrame(new TelaDadosCondominio(condominio));
            }

            @Override
            protected List<Pagamento> getCarregarObjetos() {
                return getPagamentos();
            }

//            @Override
//            protected List<Pagamento> getFiltrar(List<Pagamento> pagamentos) {
//                return filtrarListaPorNome(txtNome.getText(), pagamentos);
//            }
            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getDataVencimento());
                    case 1:
                        return pagamento.getConta().getCodigo();
                    case 2:
                        return pagamento.getFornecedor() != null ? pagamento.getFornecedor().getNome() : "";
                    case 3:
                        return pagamento.getHistorico();
                    case 4:
                        return pagamento.getValor();
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Pagamento pagamento) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getNumeroDocumento() + " ?", TelaContaPagar.this)) {
                    return false;
                }

                try {
                    new DAO().remover(condominio);
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getNumeroDocumento());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaContaPagar.this);
                    return false;
                }
            }
        };
        tabelaContaPagar.getColumn(modeloTabela.getCampo(3)).setCellRenderer(new RenderizadorFundo());
        tabelaContaPagar.getColumn(modeloTabela.getCampo(4)).setCellRenderer(new RenderizadorFundo());

        tabelaContaPagar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(1)).setPreferredWidth(70);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(2)).setPreferredWidth(215);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(3)).setPreferredWidth(250);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(4)).setPreferredWidth(120);

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtConta);
        campos.add(txtValor);
        campos.add(txtHistorico);
        return campos;
    }

    private void carregarFornecedor() {
        cbFornecedores.setModel(new ComboModelo<Fornecedor>(new DAO().listar(Fornecedor.class)));
    }

    private List getPagamentos() {
        Date datInicio = (Date) dataInicio.getValue();
        Date datTermino = (Date) dataTermino.getValue();

        pagamentos = new DAO().listar(Pagamento.class, "PagamentosContaPagarPorPeriodo", contaPagar, datInicio, datTermino);

        return pagamentos;
    }

    private void preencherPagamento() {
        pagamento = new Pagamento();
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(this);
            return;
        }
        if (cbFornecedores.getSelectedItem() == null) {
            ApresentacaoUtil.exibirAdvertencia("Escolha um Fornecedor para esta conta a pagar!", this);
            return;
        }
        pagamento.setDataVencimento(DataUtil.getCalendar(txtData.getValue()));
        pagamento.setHistorico(txtHistorico.getText());
        pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
        pagamento.setConta(conta);
        pagamento.setSaldo(new BigDecimal(0));
        pagamento.setFornecedor((Fornecedor) cbFornecedores.getModel().getSelectedItem());
        pagamento.setContaPagar(condominio.getContaPagar());

        pagamento.setForma(FormaPagamento.DINHEIRO);
        condominio.getContaPagar().adicionarPagamento(pagamento);
        new DAO().salvar(condominio);
        limparCampos();
    }

    private String fixarHistorico() {
        String texto = "";
        if (btnFixarHistórico.isSelected()) {
            texto = txtHistorico.getText();
            return texto;
        } else {
            return texto;
        }

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true);
        c.setVisible(true);


        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            if (!btnFixarHistórico.isSelected()) {
                txtHistorico.setText(conta.getNome());
            } else {
                txtHistorico.setText(fixarHistorico());
            }


        }
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    private void adicionarPagamento() {
        preencherPagamento();
    }

    private void limparCampos() {
        txtHistorico.setText(fixarHistorico());
        txtConta.setText("");
        txtValor.setText("");
    }

    private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Deseja remover os pagamentos?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            List<Pagamento> itensRemover = modeloTabela.getObjetosSelecionados();

            for (Pagamento p : itensRemover) {
                modeloTabela.remover(p);
                modeloTabela.notificar();

                new DAO().remover(itensRemover);
//                condominio.getContaCorrente().getPagamentos().removeAll(itensRemover);
                new DAO().salvar(contaPagar);
                ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void pagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Deseja pagar estes selecionados?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            List<Pagamento> itensPagar = modeloTabela.getObjetosSelecionados();
            TelaPagarDocumento tela = new TelaPagarDocumento(null, itensPagar);
            tela.setVisible(true);
            if (tela.atualizar()) {
                modeloTabela.carregarObjetos();
            }

        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private class ControladorEventos extends ControladorEventosGenerico {

        int contador;

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();
            if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnIncluir) {
                adicionarPagamento();
                carregarTabela();
            } else if (origem == itemMenuApagarSelecionados) {
                apagarItensSelecionados();
            } else if (origem == itemMenuPagarSelecionados) {
                pagarItensSelecionados();
            } else if (origem == btnFixarHistórico) {
            }
        }

        @Override
        public void configurar() {

            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaContaPagar.this, JTextField.class);

            btnConta.addActionListener(this);
            btnFixarHistórico.addActionListener(this);
            btnIncluir.addActionListener(this);
            tabelaContaPagar.addMouseListener(this);
            itemMenuApagarSelecionados.addActionListener(this);
            dataInicio.addChangeListener(this);
            dataTermino.addChangeListener(this);
            itemMenuApagarSelecionados.addActionListener(this);
            itemMenuPagarSelecionados.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            source = e.getSource();
            if (source == dataInicio || source == dataTermino) {
                ApresentacaoUtil.verificarDatas(source, dataInicio, dataTermino, this);
                modeloTabela.carregarObjetos();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuApagarSelecionados = new javax.swing.JMenuItem();
        itemMenuPagarSelecionados = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContaPagar = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnIncluir = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtData = new net.sf.nachocalendar.components.DateField();
        txtValor = new javax.swing.JTextField();
        txtConta = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        btnFixarHistórico = new javax.swing.JToggleButton();
        cbFornecedores = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        dataTermino = new net.sf.nachocalendar.components.DateField();
        jLabel4 = new javax.swing.JLabel();
        dataInicio = new net.sf.nachocalendar.components.DateField();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        itemMenuPagarSelecionados.setText("Pagar Selecionados");
        itemMenuPagarSelecionados.setToolTipText("Efetua Pagamento dos Itens Selecionados");
        popupMenu.add(itemMenuPagarSelecionados);

        jMenuItem3.setText("jMenuItem3");
        popupMenu.add(jMenuItem3);

        setClosable(true);
        setTitle("Contas a Pagar");
        setPreferredSize(new java.awt.Dimension(765, 600));

        tabelaContaPagar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaPagar);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setText("Incluir");
        btnIncluir.setToolTipText("Incluir");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel3.setText("Valor:");

        jLabel1.setText("Data Vencimento:");

        txtData.setFocusable(false);
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

        btnFixarHistórico.setText("Fixar Hist.");

        jLabel2.setText("Fornecedor");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnFixarHistórico, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
                        .addComponent(btnIncluir)
                        .addGap(123, 123, 123))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnConta)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnFixarHistórico))
                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIncluir)))
                .addContainerGap())
        );

        jLabel5.setText("Data Inicial:");

        jLabel4.setText("Data Final:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(228, 228, 228)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataTermino, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(231, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(dataTermino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(4, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 737, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JToggleButton btnFixarHistórico;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JComboBox cbFornecedores;
    private net.sf.nachocalendar.components.DateField dataInicio;
    private net.sf.nachocalendar.components.DateField dataTermino;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JMenuItem itemMenuPagarSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaContaPagar;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables

    private class RenderizadorFundo
            extends DefaultTableCellRenderer {

        private Color corNaoPagoAberta = Color.GREEN.darker();
        private Color corNaoPagoEncerrada = Color.RED;

        public RenderizadorFundo() {
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(JLabel.RIGHT);

            Pagamento p = (Pagamento) modeloTabela.getObjeto(row);

            if (!p.getConta().isCredito()) {
                setForeground(corNaoPagoEncerrada);
            } else {
                setForeground(table.getForeground());
                if (isSelected) {
                    setForeground(Color.WHITE);
                }
            }
            return this;
        }

        public void setValue(Object valor) {

            if (valor != null && valor instanceof Number) {
                Number moeda = (Number) valor;
                NumberFormat nf = NumberFormat.getCurrencyInstance();
                valor = nf.format(moeda.doubleValue());
            }
            super.setValue(valor);
        }
    }
}

