/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaEmprestimo.java
 *
 * Created on 28/02/2011, 12:07:28
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContratoEmprestimo;
import condominioPlus.negocio.financeiro.Emprestimo;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.FormaPagamentoEmprestimo;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class TelaEmprestimo extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private Emprestimo emprestimo;
    private Pagamento pagamento;
    private ContratoEmprestimo contrato;
    private List<ContratoEmprestimo> contratos;
    private TabelaModelo_2<ContratoEmprestimo> modelo;
    private Conta conta;

    /** Creates new form TelaEmprestimo */
    public TelaEmprestimo(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getEmprestimo() == null) {
            emprestimo = new Emprestimo();
            condominio.setEmprestimo(emprestimo);
            emprestimo.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            emprestimo = condominio.getEmprestimo();
            if (emprestimo.getCondominio() == null) {
                emprestimo.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }
        initComponents();

        new ControladorEventos();

        carregarTabela();
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<ContratoEmprestimo>(tabela, "Data, Descrição, Parcelas, Valor".split(",")) {

            @Override
            protected List<ContratoEmprestimo> getCarregarObjetos() {
                return getContratos();
            }

            @Override
            public Object getValor(ContratoEmprestimo c, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(c.getDataContrato());
                    case 1:
                        return c.getDescricao();
                    case 2:
                        return c.getNumeroParcelas();
                    case 3:
                        return c.getValor();
                    default:
                        return null;

                }
            }
        };

    }

    private List<ContratoEmprestimo> getContratos() {
        contratos = new DAO().listar("ContratosPorData");
        return contratos;

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtConta);
        campos.add(txtHistorico);
        campos.add(txtNumeroParcelas);
        campos.add(txtValor);
        campos.add(txtValorParcelas);

        return campos;

    }

    private void preencherObjeto() {
        if (conta.getNomeVinculo().equals("EM")) {
            contrato = new ContratoEmprestimo();
            contrato.setDataContrato(DataUtil.getCalendar(txtData.getValue()));
            contrato.setDescricao(txtHistorico.getText());
            contrato.setEmprestimo(emprestimo);
            if (radioAVista.isSelected()) {
                contrato.setForma(FormaPagamentoEmprestimo.PAGAMENTO_A_VISTA);
            } else if (radioConformeDisponibilidade.isSelected()) {
                contrato.setForma(FormaPagamentoEmprestimo.CONFORME_DISPONIBILIDADE);
            } else if (radioParcelado.isSelected()) {
                contrato.setForma(FormaPagamentoEmprestimo.PARCELADO);
            }

            contrato.setNumeroParcelas(Integer.valueOf(txtNumeroParcelas.getText()));
            contrato.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));

            if (contrato.getNumeroParcelas() > 0) {
                if (contrato.getForma() == FormaPagamentoEmprestimo.PARCELADO) {
                } else {
                    pagamento = new Pagamento();
                    pagamento.setDataVencimento(DataUtil.getCalendar(txtDataPrimeiroPagamento.getValue()));
                    pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
                    pagamento.setConta(conta);
                    pagamento.setContratoEmprestimo(contrato);
                    pagamento.setHistorico(txtHistorico.getText());
                    pagamento.setForma(FormaPagamento.DINHEIRO);

                    verificarVinculo();

                    new DAO().salvar(condominio);
                    limparCampos();
                }


            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma conta vinculada à Empréstimo!", this);
            return;
        }
    }

    private void verificarVinculo() {
        if (conta.getContaVinculada() != null) {

            TransacaoBancaria transacao = new TransacaoBancaria();
            if (pagamento.getTransacaoBancaria() != null) {
                transacao = pagamento.getTransacaoBancaria();
            }

            Pagamento pagamentoRelacionado = new Pagamento();
            if (transacao.getPagamentos() != null) {
                for (Pagamento p : transacao.getPagamentos()) {
                    if (!p.equals(pagamento)) {
                        pagamentoRelacionado = p;
                    }
                }
            }

            pagamentoRelacionado.setDataVencimento(DataUtil.getCalendar(txtData.getValue()));
            pagamentoRelacionado.setHistorico(conta.getContaVinculada().getNome());
            pagamentoRelacionado.setConta(conta.getContaVinculada());
            if (pagamentoRelacionado.getConta().isCredito()) {
                pagamentoRelacionado.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
            } else {
                pagamentoRelacionado.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
            }
            pagamentoRelacionado.setSaldo(new BigDecimal(0));
            pagamentoRelacionado.setDadosPagamento(pagamento.getDadosPagamento());


            pagamentoRelacionado.setContaPagar(condominio.getContaPagar());
            pagamentoRelacionado.setPago(false);


            transacao.adicionarPagamento(pagamento);
            transacao.adicionarPagamento(pagamentoRelacionado);

            condominio.getContaPagar().adicionarPagamento(pagamentoRelacionado);

            System.out.println("Transacao Bancária: " + transacao);

            pagamento.setTransacaoBancaria(transacao);
            pagamentoRelacionado.setTransacaoBancaria(transacao);
        }

    }

    private void salvar() {
    }

    private void remover() {
    }

    private void limparCampos() {
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public void configurar() {
            btnCalcular.addActionListener(this);
            btnConta.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnIncluir.addActionListener(this);
            tabela.addMouseListener(this);
            txtConta.addFocusListener(this);
        }

        @Override
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
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
        jLabel4 = new javax.swing.JLabel();
        txtNumeroParcelas = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtValorParcelas = new javax.swing.JTextField();
        radioAVista = new javax.swing.JRadioButton();
        radioParcelado = new javax.swing.JRadioButton();
        radioConformeDisponibilidade = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        txtDataPrimeiroPagamento = new net.sf.nachocalendar.components.DateField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();

        setClosable(true);

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

        jLabel4.setText("Nº de Parcelas:");

        jLabel5.setText("Valor Parcelas:");

        txtValorParcelas.setName("Valor"); // NOI18N

        buttonGroup1.add(radioAVista);
        radioAVista.setText("À vista");

        buttonGroup1.add(radioParcelado);
        radioParcelado.setText("Parcelado");

        buttonGroup1.add(radioConformeDisponibilidade);
        radioConformeDisponibilidade.setText("Conforme Disponibilidade");

        jLabel6.setText("Data 1º Pagamento:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtData, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtDataPrimeiroPagamento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                            .addComponent(jLabel4))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtValorParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioAVista)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioParcelado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioConformeDisponibilidade))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnConta)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnImprimir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnIncluir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCalcular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(33, 33, 33)
                            .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtValorParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioAVista)
                            .addComponent(radioParcelado)
                            .addComponent(radioConformeDisponibilidade)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataPrimeiroPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9))
        );

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radioAVista;
    private javax.swing.JRadioButton radioConformeDisponibilidade;
    private javax.swing.JRadioButton radioParcelado;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private net.sf.nachocalendar.components.DateField txtDataPrimeiroPagamento;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroParcelas;
    private javax.swing.JTextField txtValor;
    private javax.swing.JTextField txtValorParcelas;
    // End of variables declaration//GEN-END:variables
}
