/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaAplicacaoFinanceira.java
 *
 * Created on 21/02/2011, 15:46:47
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.AplicacaoFinanceira;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class TelaAplicacaoFinanceira extends javax.swing.JInternalFrame {

   private AplicacaoFinanceira aplicacao;
    private Condominio condominio;
    private Conta conta;
    private Pagamento pagamento;
    private TabelaModelo_2 modeloTabela;
    private List<Pagamento> pagamentos;

    /** Creates new form TelaPoupanca */
    public TelaAplicacaoFinanceira(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getAplicacao() == null) {
            aplicacao = new AplicacaoFinanceira();
            condominio.setAplicacao(aplicacao);
            aplicacao.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            aplicacao = condominio.getAplicacao();
            if (aplicacao.getCondominio() == null) {
                aplicacao.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }

        initComponents();
        new ControladorEventos();

        aplicacao.calculaSaldo();
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Aplicação Financeira - " + condominio.getRazaoSocial());
        }
    }

    public void carregarTabela() {

        modeloTabela = new TabelaModelo_2<Pagamento>(tabela, "Data, Histórico, Valor, Saldo, Conta, Tipo".split(",")) {

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
                        return pagamento.getValor();
                    case 3:
                        return pagamento.getSaldo();
                    case 4:
                        return pagamento.getConta().getCodigo();
                    case 5:
                        return pagamento.getConta().isCredito() ? "C" : "D";
                    default:
                        return null;
                }
            }
        };

    }

    private List<Pagamento> getPagamentos() {
        pagamentos = new DAO().listar("PagamentosAplicacaoFinanceira", condominio.getAplicacao());
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

    private void verificarDataPagamento(Pagamento p2) {
        if (condominio.getAplicacao().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getAplicacao().setSaldo(p2.getValor());

        }
    }

    private void verificarDataPagamentoContaCorrente(Pagamento p2) {
        if (condominio.getContaCorrente().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getContaCorrente().setSaldo(p2.getValor());
        }
    }

    private void verificarLista() {
        if (condominio.getAplicacao().getPagamentos().size() == 1) {
            for (Pagamento p : getPagamentos()) {
                p.setSaldo(p.getValor());
                condominio.getAplicacao().setSaldo(p.getValor());

            }
        }
    }

    private void preencherObjeto() {
        if (conta.getNomeVinculo().equals("AF")) {
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


            pagamento.setAplicacao(condominio.getAplicacao());
            pagamento.setPago(true);
            verificarDataPagamento(pagamento);

            condominio.getAplicacao().adicionarPagamento(pagamento);

            if (conta.getContaVinculada() != null) {

                TransacaoBancaria transacao = new TransacaoBancaria();
                Pagamento pagamentoRelacionado = new Pagamento();


//                new DAO().salvar(transacao);

                pagamentoRelacionado.setDataPagamento(DataUtil.getCalendar(txtData.getValue()));
                pagamentoRelacionado.setHistorico(conta.getContaVinculada().getNome());
                pagamentoRelacionado.setConta(conta.getContaVinculada());
                if (pagamentoRelacionado.getConta().isCredito()) {
                    pagamentoRelacionado.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
                } else {
                    pagamentoRelacionado.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
                }
                pagamentoRelacionado.setSaldo(new BigDecimal(0));
                pagamentoRelacionado.setDadosPagamento(pagamento.getDadosPagamento());


                pagamentoRelacionado.setContaCorrente(condominio.getContaCorrente());
                pagamentoRelacionado.setPago(true);


                transacao.adicionarPagamento(pagamento);
                transacao.adicionarPagamento(pagamentoRelacionado);

                verificarDataPagamentoContaCorrente(pagamentoRelacionado);
                condominio.getContaCorrente().adicionarPagamento(pagamentoRelacionado);

                System.out.println("Transacao Bancária: " + transacao);

                pagamento.setTransacaoBancaria(transacao);
                pagamentoRelacionado.setTransacaoBancaria(transacao);
            }

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


            String descricao = "Pagamento em Aplicação Financeira adicionado! " + pagamento.getHistorico() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

//            sair();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void sair() {
        this.doDefaultCloseAction();
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

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnConta.addActionListener(this);
            btnIncluir.addActionListener(this);
            btnCalcular.addActionListener(this);
            txtConta.addFocusListener(this);
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
                aplicacao.calculaSaldo();
                carregarTabela();
                new DAO().salvar(aplicacao);
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaAplicacaoFinanceira.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtData = new net.sf.nachocalendar.components.DateField();
        jLabel1 = new javax.swing.JLabel();
        txtConta = new javax.swing.JTextField();
        txtValor = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnCalcular = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        btnIncluir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setClosable(true);
        setIconifiable(true);
        setTitle("Aplicação Financeira");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        txtData.setFocusable(false);
        txtData.setRequestFocusEnabled(false);

        jLabel1.setText("Data Lançamento:");

        txtConta.setName("Conta"); // NOI18N

        txtValor.setName("Valor"); // NOI18N

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        jLabel3.setText("Valor:");

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Histórico:");

        btnCalcular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/calculadora.png"))); // NOI18N
        btnCalcular.setToolTipText("Recalcular");

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Cheque");

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(10, 10, 10)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnImprimir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCalcular, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnIncluir, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(btnConta, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir});

        tabela.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabela);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}