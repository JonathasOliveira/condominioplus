/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoPesquisarConteudoCaixa.java
 *
 * Created on 18/07/2013, 15:24:27
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaCorrente;
import condominioPlus.negocio.financeiro.DadosBoleto;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.util.Relatorios;
import condominioPlus.util.RenderizadorCelulaCor;
import condominioPlus.util.RenderizadorCelulaCorData;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;

/**
 *
 * @author eugenia
 */
public class DialogoPesquisarConteudoCaixa extends javax.swing.JDialog {

    private DateTime dataInicial;
    private DateTime dataFinal;
    private ContaCorrente contaCorrente;
    private Conta conta;
    private List<Pagamento> listaPagamentos = new ArrayList<Pagamento>();
    private TabelaModelo_2 modeloTabela;
    private RenderizadorCelulaCor renderizadorCelulaCor;
    private String texto;

    /** Creates new form DialogoPesquisarConteudoCaixa */
    public DialogoPesquisarConteudoCaixa(java.awt.Frame parent, boolean modal, ContaCorrente contaCorrente) {
        super(parent, modal);
        initComponents();
        new ControladorEventos();
        this.setLocationRelativeTo(null);

        this.contaCorrente = contaCorrente;

        txtDataInicial.setValue(DataUtil.getDate(DataUtil.getPrimeiroDiaMes()));
        verificarSelecaoRadio();
    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false, "T");
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabela, "Data, Documento, Conta, Descrição, Valor".split(",")) {

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
                        return getFormaPagamento(pagamento);
                    case 2:
                        return pagamento.getConta().getCodigo();
                    case 3:
                        return pagamento.getHistorico().toUpperCase();
                    case 4:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
                    default:
                        return null;
                }
            }
        };

        renderizadorCelulaCor = new RenderizadorCelulaCor(modeloTabela);
        RenderizadorCelulaCorData renderizadorCelula = new RenderizadorCelulaCorData(modeloTabela);
        tabela.getColumn(modeloTabela.getCampo(0)).setCellRenderer(renderizadorCelula);
        tabela.getColumn(modeloTabela.getCampo(1)).setCellRenderer(renderizadorCelulaCor);
        tabela.getColumn(modeloTabela.getCampo(2)).setCellRenderer(renderizadorCelulaCor);
        tabela.getColumn(modeloTabela.getCampo(3)).setCellRenderer(renderizadorCelulaCor);
        tabela.getColumn(modeloTabela.getCampo(4)).setCellRenderer(renderizadorCelulaCor);

        tabela.getColumn(modeloTabela.getCampo(3)).setMinWidth(300);
        tabela.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);
    }

    private String getFormaPagamento(Pagamento p) {
        if (p.getForma() == FormaPagamento.BOLETO) {
            return ((DadosBoleto) p.getDadosPagamento()).getNumeroBoleto();
        } else if (p.getForma() == FormaPagamento.CHEQUE) {
            return String.valueOf(((DadosCheque) p.getDadosPagamento()).getNumero());
        } else {
            return String.valueOf(((DadosDOC) p.getDadosPagamento()).getNumeroDocumento());
        }
    }

    private List<Pagamento> getPagamentos() {
        listaPagamentos.clear();
        texto = "Pesquisa por ";
        dataInicial = DataUtil.getDateTime(txtDataInicial.getValue());
        dataFinal = DataUtil.getDateTime(txtDataFinal.getValue());
        if (radioConta.isSelected()) {
            texto = texto + "Conta: " + conta.getCodigo();
            listaPagamentos = new DAO().listar(Pagamento.class, "PagamentosEfetuadosPorConta", contaCorrente, DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal), conta);
        } else if (radioValor.isSelected()) {
            texto = texto + "Valor: " + PagamentoUtil.formatarMoeda(new BigDecimal(txtValor.getText().replace(',', '.')).doubleValue());
            List<Pagamento> listaAuxiliar = new ArrayList<Pagamento>();
            BigDecimal valor = new BigDecimal(txtValor.getText().replace(',', '.'));
            listaAuxiliar = new DAO().listar(Pagamento.class, "PagamentosPorPeriodoContaCorrente", contaCorrente, DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
            for (Pagamento p : listaAuxiliar) {
                if (p.getValor().compareTo(valor) == 0) {
                    listaPagamentos.add(p);
                }
            }
        } else if (radioNumeroDocumento.isSelected()) {
            texto = texto + "Nº Documento: " + txtNumeroDocumento.getText();
            List<Pagamento> listaAuxiliar = new ArrayList<Pagamento>();
            String documento = txtNumeroDocumento.getText();
            listaAuxiliar = new DAO().listar(Pagamento.class, "PagamentosPorPeriodoContaCorrente", contaCorrente, DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
            for (Pagamento p : listaAuxiliar) {
                if (documento.equals(getFormaPagamento(p))) {
                    listaPagamentos.add(p);
                }
            }
        }
        return listaPagamentos;
    }

    private void editarPagamento() {
        if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
            DialogoEditarPagamentoContaCorrente tela = new DialogoEditarPagamentoContaCorrente((Pagamento) modeloTabela.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloTabela.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
        }
    }

    private void imprimir() {
        new Relatorios().imprimirExtratoConferenciaContaCorrente(contaCorrente.getCondominio(), dataInicial, dataFinal, (List<Pagamento>) modeloTabela.getObjetos(), TipoRelatorio.EXTRATO_PESQUISAR_CONTEUDO_CAIXA, texto);
    }

    private void verificarSelecaoRadio() {
        if (radioConta.isSelected()) {
            txtConta.setEnabled(true);
            txtConta.setBackground(Color.WHITE);
            txtConta.grabFocus();
            btnConta.setEnabled(true);
            txtValor.setEnabled(false);
            txtValor.setBackground(Color.LIGHT_GRAY);
            radioValor.setForeground(Color.GRAY);
            txtValor.setText("");
            txtNumeroDocumento.setEnabled(false);
            txtNumeroDocumento.setBackground(Color.LIGHT_GRAY);
            radioNumeroDocumento.setForeground(Color.GRAY);
            txtNumeroDocumento.setText("");
        } else if (radioValor.isSelected()) {
            txtValor.setEnabled(true);
            txtValor.setBackground(Color.WHITE);
            txtValor.grabFocus();
            radioValor.setForeground(Color.BLACK);
            txtConta.setEnabled(false);
            txtConta.setBackground(Color.LIGHT_GRAY);
            btnConta.setEnabled(false);
            txtConta.setText("");
            txtNumeroDocumento.setEnabled(false);
            txtNumeroDocumento.setBackground(Color.LIGHT_GRAY);
            radioNumeroDocumento.setForeground(Color.GRAY);
            txtNumeroDocumento.setText("");
        } else if (radioNumeroDocumento.isSelected()) {
            txtNumeroDocumento.setEnabled(true);
            txtNumeroDocumento.setBackground(Color.WHITE);
            radioNumeroDocumento.setForeground(Color.BLACK);
            txtNumeroDocumento.grabFocus();
            txtValor.setEnabled(false);
            txtValor.setBackground(Color.LIGHT_GRAY);
            radioValor.setForeground(Color.GRAY);
            txtConta.setEnabled(false);
            txtConta.setBackground(Color.LIGHT_GRAY);
            btnConta.setEnabled(false);
            txtConta.setText("");
        }
    }

    private void sair() {
        dispose();
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void configurar() {
            btnPesquisar.addActionListener(this);
            btnCancelar.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnConta.addActionListener(this);
            txtDataInicial.addChangeListener(this);
            txtDataFinal.addChangeListener(this);
            txtConta.addFocusListener(this);
            radioConta.addActionListener(this);
            radioValor.addActionListener(this);
            radioNumeroDocumento.addActionListener(this);
            tabela.addMouseListener(this);
            itemMenuEditarPagamento.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == btnPesquisar) {
                carregarTabela();
            } else if (origem == btnImprimir) {
                imprimir();
            } else if (origem == btnCancelar) {
                sair();
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == radioConta || origem == radioValor || origem == radioNumeroDocumento) {
                verificarSelecaoRadio();
            } else if (origem == itemMenuEditarPagamento) {
                editarPagamento();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            origem = e.getSource();
            if (e.getSource() == txtDataInicial || e.getSource() == txtDataFinal) {
                ApresentacaoUtil.verificarDatas(e.getSource(), txtDataInicial, txtDataFinal, this);
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
                        } else {
                            ApresentacaoUtil.exibirErro("Código Inexistente!", DialogoPesquisarConteudoCaixa.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        popupMenu = new javax.swing.JPopupMenu();
        itemMenuEditarPagamento = new javax.swing.JMenuItem();
        painelDados = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnPesquisar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtConta = new javax.swing.JTextField();
        txtValor = new javax.swing.JTextField();
        radioConta = new javax.swing.JRadioButton();
        radioValor = new javax.swing.JRadioButton();
        radioNumeroDocumento = new javax.swing.JRadioButton();

        itemMenuEditarPagamento.setText("Editar Pagamento Selecionado");
        popupMenu.add(itemMenuEditarPagamento);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Pesquisar Conteúdo do Caixa");

        painelDados.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnPesquisar.setText("Pesquisar");

        btnCancelar.setText("Cancelar");

        btnImprimir.setText("Imprimir");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(btnPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnPesquisar)
                .addComponent(btnImprimir)
                .addComponent(btnCancelar))
        );

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Selecione o período desejado:");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("a");

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabela);

        btnConta.setText("Conta");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtConta.setName("Conta"); // NOI18N

        txtValor.setName("Valor"); // NOI18N

        buttonGroup1.add(radioConta);
        radioConta.setSelected(true);

        buttonGroup1.add(radioValor);
        radioValor.setText("Valor");

        buttonGroup1.add(radioNumeroDocumento);
        radioNumeroDocumento.setText("Nº Documento");

        javax.swing.GroupLayout painelDadosLayout = new javax.swing.GroupLayout(painelDados);
        painelDados.setLayout(painelDadosLayout);
        painelDadosLayout.setHorizontalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDadosLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelDadosLayout.createSequentialGroup()
                        .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelDadosLayout.createSequentialGroup()
                                .addComponent(radioConta)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnConta))
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioValor))
                        .addGap(18, 18, 18)
                        .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(radioNumeroDocumento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 693, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        painelDadosLayout.setVerticalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(radioConta)
                    .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnConta)
                        .addComponent(radioValor)
                        .addComponent(radioNumeroDocumento)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem itemMenuEditarPagamento;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel painelDados;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JRadioButton radioConta;
    private javax.swing.JRadioButton radioNumeroDocumento;
    private javax.swing.JRadioButton radioValor;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
