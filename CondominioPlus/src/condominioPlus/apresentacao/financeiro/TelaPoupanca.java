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
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.financeiro.Poupanca;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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
public class TelaPoupanca extends javax.swing.JInternalFrame {

    private Poupanca poupanca;
    private Condominio condominio;
    private Conta conta;
    private Pagamento pagamento;
    private TabelaModelo_2 modeloTabela;
    private List<Pagamento> pagamentos;

    /** Creates new form TelaPoupanca */
    public TelaPoupanca(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getPoupanca() == null) {
            poupanca = new Poupanca();
            condominio.setPoupanca(poupanca);
            poupanca.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            poupanca = condominio.getPoupanca();
            if (poupanca.getCondominio() == null) {
                poupanca.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }

        initComponents();
        new ControladorEventos();

        poupanca.calculaSaldo(poupanca);
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Poupança - " + condominio.getRazaoSocial());
        }
    }

    public void carregarTabela() {

        modeloTabela = new TabelaModelo_2<Pagamento>(tabelaPoupanca, "Data, Histórico, Valor, Saldo, Conta, Tipo".split(",")) {

            @Override
            protected List<Pagamento> getCarregarObjetos() {
                return getPagamentos();
            }

            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getDataPagamento());
                    case 1:
                        return pagamento.getHistorico();
                    case 2:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
                    case 3:
                        return PagamentoUtil.formatarMoeda(pagamento.getSaldo().doubleValue());
                    case 4:
                        return pagamento.getConta().getCodigo();
                    case 5:
                        return pagamento.getConta().isCredito() ? "C" : "D";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaPoupanca.getColumn(modeloTabela.getCampo(2)).setCellRenderer(direita);
        tabelaPoupanca.getColumn(modeloTabela.getCampo(3)).setCellRenderer(direita);

    }

    private List<Pagamento> getPagamentos() {
        pagamentos = new DAO().listar("PagamentosPoupanca", condominio.getPoupanca());
        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();
        Collections.sort(pagamentos, comCod);
        ComparatorPagamento comparator = new ComparatorPagamento();
        Collections.sort(pagamentos, comparator);
        return pagamentos;
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtValor);
        campos.add(txtConta);
        campos.add(txtHistorico);
        return campos;
    }

    private void verificarListaPoupancaVazia(Pagamento p2) {
        if (condominio.getPoupanca().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getPoupanca().setSaldo(p2.getValor());

        }
    }

    private void verificarLista() {
        if (condominio.getPoupanca().getPagamentos().size() == 1) {
            for (Pagamento p : getPagamentos()) {
                p.setSaldo(p.getValor());
                condominio.getPoupanca().setSaldo(p.getValor());
            }
        } else if (condominio.getAplicacao().getPagamentos().isEmpty()) {
            condominio.getAplicacao().setSaldo(new BigDecimal(0));
        }
    }

    private void preencherObjeto() {
        if (conta.getNomeVinculo().equals("PO")) {
            pagamento = new Pagamento();

            pagamento.setDataPagamento(DataUtil.getCalendar(txtData.getValue()));
            pagamento.setHistorico(txtHistorico.getText());
            pagamento.setConta(conta);
            if (pagamento.getConta().isCredito()) {
                pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
            } else {
                pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
            }
            pagamento.setSaldo(new BigDecimal(0));
            pagamento.setDadosPagamento(new DadosDOC(Long.valueOf(Pagamento.gerarNumeroDocumento())));


            pagamento.setPoupanca(condominio.getPoupanca());
            pagamento.setPago(true);
            verificarListaPoupancaVazia(pagamento);

            condominio.getPoupanca().adicionarPagamento(pagamento);
            condominio.getPoupanca().setSaldo(condominio.getPoupanca().getSaldo().add(pagamento.getValor()));

            PagamentoUtil.pagamentoVinculado(pagamento);

            new DAO().salvar(condominio);
            limparCampos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma conta vinculada à Poupança!", this);
            return;
        }

    }

    private void limparCampos() {
        txtHistorico.setText("");
        txtConta.setText("");
        txtValor.setText("");
    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
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
        if (!ApresentacaoUtil.perguntar("Desejar remover os pagamentos?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<Pagamento> itensRemoverPoupanca = modeloTabela.getObjetosSelecionados();
            List<Pagamento> itensRelacionadosRemover = new ArrayList<Pagamento>();

            for (Pagamento p : itensRemoverPoupanca) {
                if (p.getTransacaoBancaria() != null) {
                    TransacaoBancaria transacao = p.getTransacaoBancaria();
                    Pagamento pagamentoRelacionado = new Pagamento();
                    for (Pagamento p2 : transacao.getPagamentos()) {
                        if (!p.equals(p2)) {
                            pagamentoRelacionado = p2;
                            pagamentoRelacionado.setDadosPagamento(null);
                            condominio.getContaCorrente().setSaldo(condominio.getContaCorrente().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            itensRelacionadosRemover.add(pagamentoRelacionado);
                        }
                    }
                    new DAO().remover(transacao);
                }
                modeloTabela.remover(p);
                modeloTabela.notificar();
                poupanca.setSaldo(poupanca.getSaldo().subtract(p.getValor()));
            }
            if (!itensRelacionadosRemover.isEmpty()) {
                new DAO().remover(itensRelacionadosRemover);
                condominio.getContaCorrente().getPagamentos().removeAll(itensRelacionadosRemover);
            }
            new DAO().remover(itensRemoverPoupanca);
            condominio.getPoupanca().getPagamentos().removeAll(itensRemoverPoupanca);
            new DAO().salvar(condominio);
            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false, "PO");
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            txtHistorico.setText(conta.getNome());
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnConta.addActionListener(this);
            btnIncluir.addActionListener(this);
            btnCalcular.addActionListener(this);
            txtConta.addFocusListener(this);
            tabelaPoupanca.addMouseListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();
            if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnIncluir) {
                salvar();
                carregarTabela();
            } else if (origem == btnCalcular) {
                verificarLista();
                poupanca.calculaSaldo(poupanca);
                carregarTabela();
                new DAO().salvar(poupanca);
            } else if (origem == itemMenuRemoverSelecionados) {
                apagarItensSelecionados();
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

                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaPoupanca.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
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
        itemMenuImprimirPoupanca = new javax.swing.JMenuItem();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtData = new net.sf.nachocalendar.components.DateField();
        txtValor = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnCalcular = new javax.swing.JButton();
        txtConta = new javax.swing.JTextField();
        btnImprimir = new javax.swing.JButton();
        btnIncluir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaPoupanca = new javax.swing.JTable();

        itemMenuImprimirPoupanca.setText("Imprimir Conta Poupança");
        popupMenu.add(itemMenuImprimirPoupanca);

        itemMenuRemoverSelecionados.setText("Deletar Itens Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        setClosable(true);
        setTitle("Poupança");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText("Valor:");

        jLabel1.setText("Data Lançamento:");

        txtData.setFocusable(false);
        txtData.setRequestFocusEnabled(false);

        txtValor.setName("Valor"); // NOI18N

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Histórico:");

        btnCalcular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/calculadora.gif"))); // NOI18N
        btnCalcular.setToolTipText("Recalcular Saldo");

        txtConta.setName("Conta"); // NOI18N

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Relatório");

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(117, 117, 117))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnImprimir))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(btnConta)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCalcular, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnIncluir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaPoupanca.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaPoupanca);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 893, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 893, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JMenuItem itemMenuImprimirPoupanca;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaPoupanca;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
