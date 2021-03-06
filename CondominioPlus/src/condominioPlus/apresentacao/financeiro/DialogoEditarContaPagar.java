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

import bemaJava.Bematech;
import com.sun.jna.Native;
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class DialogoEditarContaPagar extends javax.swing.JDialog {

    private Pagamento pagamento;
//    private ComboModelo<Fornecedor> modelo;
    private TabelaModelo_2 modeloTabela;
    private ControladorEventosGenerico controlador;
    private Conta conta;
    private List<Pagamento> listaPagamentos;
    private BigDecimal total = new BigDecimal(0);

    /** Creates new form TelaBanco */
    public DialogoEditarContaPagar(Pagamento pagamento) {
        initComponents();
        this.pagamento = pagamento;
        listaPagamentos = getPagamentosSemOriginal();
//        carregarFornecedor();
        preencherTela();
        carregarTabela();
        setTotal();
        controlador = new ControladorEventos();
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabela, "Vencimento, Conta, Documento, Valor".split(",")) {

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
                return listaPagamentos;
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
                        return pagamento.getForma() == FormaPagamento.CHEQUE ? String.valueOf(((DadosCheque) pagamento.getDadosPagamento()).getNumero()) : String.valueOf(((DadosDOC) pagamento.getDadosPagamento()).getNumeroDocumento());
                    case 3:
                        return pagamento.getValor().negate();
                    default:
                        return null;
                }
            }
        };


        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        esquerda.setHorizontalAlignment(SwingConstants.LEFT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabela.getColumn(modeloTabela.getCampo(1)).setCellRenderer(direita);
        tabela.getColumn(modeloTabela.getCampo(2)).setCellRenderer(direita);
        tabela.getColumn(modeloTabela.getCampo(3)).setCellRenderer(centralizado);
//        tabela.getColumn(modeloTabela.getCampo(5)).setCellRenderer(direita);
//        tabela.getColumn(modeloTabela.getCampo(3)).setMinWidth(180);
//        tabela.getColumn(modeloTabela.getCampo(4)).setMinWidth(280);
//        tabela.getColumn(modeloTabela.getCampo(5)).setMinWidth(110);
    }

    private List<Pagamento> getPagamentos() {
        List<Pagamento> lista = Main.getCondominio().getContaPagar().getPagamentos();
        List<Pagamento> novaLista = new ArrayList<Pagamento>();
        for (Pagamento p : lista) {
            if (pagamento.getForma() == FormaPagamento.CHEQUE && p.getForma() == FormaPagamento.CHEQUE) {
                if (((DadosCheque) pagamento.getDadosPagamento()).getNumero() == ((DadosCheque) p.getDadosPagamento()).getNumero()) {
                    novaLista.add(p);
                }
            } else if (pagamento.getForma() == FormaPagamento.DINHEIRO && p.getForma() == FormaPagamento.DINHEIRO) {
                if (((DadosDOC) pagamento.getDadosPagamento()).getNumeroDocumento() == ((DadosDOC) p.getDadosPagamento()).getNumeroDocumento()) {
                    novaLista.add(p);
                }
            }
        }

        if (novaLista.isEmpty()) {
            novaLista.add(pagamento);
        }
        return novaLista;
    }

    private List<Pagamento> getPagamentosSemOriginal() {
        List<Pagamento> lista = getPagamentos();
        System.out.println("listaaaa  " + lista);
        List<Pagamento> listaModificada = new ArrayList<Pagamento>();
        for (Pagamento p2 : lista) {
            if (p2.getCodigo() != pagamento.getCodigo()) {
                listaModificada.add(p2);
            }
        }
        System.out.println("lista modificada " + listaModificada);
        return listaModificada;
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

    private void setTotal() {
        lblTotal.setText(somarCheque());
    }

    private String somarCheque() {
        for (Pagamento p : getPagamentos()) {
            total = total.add(p.getValor());
        }
        return String.valueOf(total);
    }

    private void imprimirCheques() {
        int iRetorno;
        Pagamento p = null;

        if (!getPagamentos().isEmpty()) {
            p = getPagamentos().get(0);
        }
        if (pagamento.getFornecedor() == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione um Fornecedor/Favorecido!", this);
            return;
        }

        Bematech lib =
                (Bematech) Native.loadLibrary("BEMADP32", Bematech.class);
        iRetorno = lib.Bematech_DP_IniciaPorta("COM1");

        lib.Bematech_DP_IncluiAlteraBanco("555", "3,7,9,11,13,92,20,8,10,62,23,32,55");
        String valor = somarCheque().replace('.', ',');
        iRetorno = lib.Bematech_DP_ImprimeCheque("555", valor, p.getFornecedor(), "ARMACAO DOS BUZIOS", DataUtil.getDateTime(p.getDataVencimento()).toString("ddMMyy"), "");

        System.out.println(iRetorno);
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



        if (!listaPagamentos.isEmpty()) {
            System.out.println("teste");


            for (Pagamento p : listaPagamentos) {
                if (btnNumeroDocumento.isSelected()) {
                    if (pagamento.getContratoEmprestimo() == null) {
                        p.setForma(FormaPagamento.CHEQUE);
                        if (p.getDadosPagamento() != null) {
                            p.setDadosPagamento(pagamento.getDadosPagamento());
                        }

                        p.setFornecedor(pagamento.getFornecedor());
                    }


                } else {
                    if (pagamento.getContratoEmprestimo() == null) {
                        p.setForma(FormaPagamento.DINHEIRO);
                        p.setDadosPagamento(pagamento.getDadosPagamento());
                        p.setFornecedor(pagamento.getFornecedor());


                    }
                }
            }
            System.out.println("listta" + listaPagamentos);


            new DAO().salvar(listaPagamentos);


        }

        new DAO().salvar(pagamento);
        dispose();


    }

    private void trocarFormaPagamento() {
        if (btnNumeroDocumento.isSelected()) {
            btnNumeroDocumento.setText("Nº Cheque:");
            txtNumeroDocumento.setText("");
            txtNumeroDocumento.grabFocus();
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
                if (getPagamentos().get(0).getForma() == FormaPagamento.CHEQUE) {
                    imprimirCheques();
                } else {
                    ApresentacaoUtil.exibirInformacao("Relatorio vai ser feito depois", DialogoEditarContaPagar.this);
                }
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", DialogoEditarContaPagar.this);
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
                    ApresentacaoUtil.transferidorFocoEnter, DialogoEditarContaPagar.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, DialogoEditarContaPagar.this, JTextField.class);

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

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
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
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Editar Conta a Pagar");
        setModal(true);

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
        tabela.setName("tabela"); // NOI18N
        jScrollPane1.setViewportView(tabela);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 9));
        jLabel5.setText("Atenção: Caso modifique o número do cheque/doc estes pagamentos também serão alterados!");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel6.setText("Total: R$");

        lblTotal.setFont(new java.awt.Font("Tahoma", 0, 12));
        lblTotal.setForeground(new java.awt.Color(255, 0, 0));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addGap(10, 10, 10)
                            .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5))
        );

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
                .addGap(5, 5, 5)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
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

        jLabel7.setText("Pagamentos Relacionados:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, 0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, 0, 422, Short.MAX_VALUE)
                    .addComponent(painelContaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelContaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel painelContaPagar;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtFornecedor;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
