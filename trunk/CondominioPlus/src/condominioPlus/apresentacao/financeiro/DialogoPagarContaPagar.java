/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoEditarContaPagar2.java
 *
 * Created on 02/02/2011, 17:04:42
 */
package condominioPlus.apresentacao.financeiro;

import bemaJava.Bematech;
import com.sun.jna.Native;
import condominioPlus.Main;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.FormaPagamentoEmprestimo;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.fornecedor.Fornecedor;
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
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class DialogoPagarContaPagar extends javax.swing.JDialog {

    private Pagamento pagamento;
    private ComboModelo<Fornecedor> modelo;
    private TabelaModelo_2 modeloTabela;
    private ControladorEventosGenerico controlador;
    private Conta conta;
    private List<Pagamento> listaPagamentos;
    private BigDecimal total = new BigDecimal(0);
    private Condominio condominio = Main.getCondominio();

    /** Creates new form TelaBanco */
    public DialogoPagarContaPagar(Pagamento pagamento) {
        initComponents();
        this.pagamento = pagamento;
        listaPagamentos = getPagamentosSemOriginal();
        carregarFornecedor();
        preencherTela();
        carregarTabela();
        bloquearCampos();
        setTotal();
        verificarConformeDisponibilidade();
        controlador = new ControladorEventos();
    }

    private void verificarConformeDisponibilidade(){
        if(pagamento.getContratoEmprestimo().getForma() == FormaPagamentoEmprestimo.CONFORME_DISPONIBILIDADE){
            if (ApresentacaoUtil.perguntar("Deseja pagar o total desse Empréstimo?", this)){
                painelNovoPagamento.setEnabled(false);

            }else{
                jTabbedPane1.setSelectedIndex(1);

            }
        }
    }

    private void novoPagamento(){
        Pagamento p = new Pagamento();

        p.setDataVencimento(pagamento.getDataVencimento());
        p.setDataPagamento(DataUtil.getCalendar(DataUtil.hoje()));
        p.setContaCorrente(condominio.getContaCorrente());
        p.setContratoEmprestimo(pagamento.getContratoEmprestimo());
        p.setConta(pagamento.getConta());
        p.setHistorico("PAGAMENTO PARCELO " + (pagamento.getContratoEmprestimo().getPagamentos().size()+1) + pagamento.getConta().getNome());
        

    }

    private void bloquearCampos() {
        txtConta.setEnabled(false);
        txtHistorico.setEnabled(false);
        txtData.setEnabled(false);
        txtNumeroDocumento.setEnabled(false);
        txtValor.setEnabled(false);
        cbFornecedores.setEnabled(false);

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
                return getPagamentosSemOriginal();
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
        return new DAO().listar("PagamentosPorNumeroDocumento", Main.getCondominio().getContaPagar(), pagamento.getDadosPagamento());
    }

    private List<Pagamento> getPagamentosSemOriginal() {
        List<Pagamento> lista = new DAO().listar("PagamentosPorNumeroDocumento", Main.getCondominio().getContaPagar(), pagamento.getDadosPagamento());
        System.out.println("listaaaa  " + lista);
        List<Pagamento> listaModificada = new ArrayList<Pagamento>();
        for (Pagamento p2 : lista) {
            if (p2.getCodigo() != pagamento.getCodigo()) {
                listaModificada.add(p2);
            }
        }
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
        txtHistorico.setText(pagamento.getHistorico());
        txtNumeroDocumento.setText(compararForma());
        txtValor.setText(String.valueOf(pagamento.getValor().negate()));
        modelo.setSelectedItem(pagamento.getFornecedor());

    }

//    private void preencherObjeto() {
//        pagamento.setDataVencimento(DataUtil.getCalendar(txtData.getValue()));
//        pagamento.setHistorico(txtHistorico.getText());
//        pagamento.setValor(new BigDecimal(txtValor.getText().replace(',', '.')).negate());
//        pagamento.setFornecedor(modelo.getSelectedItem());
//        pagamento.setConta(conta);
//        selecionaFormaPagamento(pagamento);
//
//    }
    private Pagamento selecionaFormaPagamento(Pagamento p) {
        if (btnNumeroDocumento.isSelected()) {
            p.setForma(FormaPagamento.CHEQUE);
            p.setDadosPagamento(new DadosCheque(Long.valueOf(txtNumeroDocumento.getText()), Main.getCondominio().getContaBancaria().getContaCorrente(), Main.getCondominio().getRazaoSocial()));
            return p;
        } else {
            p.setForma(FormaPagamento.DINHEIRO);
            p.setDadosPagamento(new DadosDOC(Long.valueOf(txtNumeroDocumento.getText())));
            return p;
        }

    }

    private void setTotal() {
        lblTotal.setText(somarCheque());
    }

    private String somarCheque() {
        for (Pagamento p : getPagamentos()) {
            total = total.add(p.getValor());
        }
        return String.valueOf(total.negate());
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
        iRetorno = lib.Bematech_DP_ImprimeCheque("555", valor, p.getFornecedor().getNome(), "ARMACAO DOS BUZIOS", DataUtil.getDateTime(p.getDataVencimento()).toString("ddMMyy"), "");
        System.out.println(iRetorno);

    }

    private void trocarFormaPagamento() {
        if (btnNumeroDocumento.isSelected()) {
            btnNumeroDocumento.setText("Nº Cheque:");
            txtNumeroDocumento.setText(Main.getCondominio().getContaBancaria().getContaCorrente());
        } else {
            btnNumeroDocumento.setText("Nº Doc:");
            txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());
        }

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, false, false);
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
        }
    }

    private Conta pesquisarContaPorCodigo(int codigo) {
        Conta c = null;
        try {
            c = (Conta) new DAO().localizar("LocalizarContas", codigo, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    private void carregarFornecedor() {
        modelo = new ComboModelo<Fornecedor>(new DAO().listar(Fornecedor.class), cbFornecedores);
        cbFornecedores.setModel(modelo);
    }

    private void efetuarPagamento() {
        if (pagamento.getContratoEmprestimo() != null) {
            if(pagamento.getContratoEmprestimo().getForma() == FormaPagamentoEmprestimo.CONFORME_DISPONIBILIDADE){
                
            }
            pagamento.setPago(true);
            pagamento.setContaCorrente(Main.getCondominio().getContaCorrente());
            pagamento.setDataPagamento(DataUtil.getCalendar(DataUtil.hoje()));
            pagamento.getContaCorrente().setSaldo(pagamento.getContaCorrente().getSaldo().add(pagamento.getValor()));
            pagarPagamentoRelacionado(pagamento);
            new DAO().salvar(pagamento);
        } else {
            List<Pagamento> novaLista = getPagamentos();
            for (Pagamento p : novaLista) {
                p.setPago(true);
                p.setContaCorrente(Main.getCondominio().getContaCorrente());
                p.setDataPagamento(DataUtil.getCalendar(DataUtil.hoje()));
                p.getContaCorrente().setSaldo(p.getContaCorrente().getSaldo().add(p.getValor()));
                verificarVinculo(p);
            }

            new DAO().salvar(novaLista);
        }
        new DAO().salvar(condominio);
        ApresentacaoUtil.exibirInformacao("Pagamentos efetuados com sucesso!", this);
        dispose();
    }

    private void pagarPagamentoRelacionado(Pagamento p) {
        for (Pagamento p2 : p.getTransacaoBancaria().getPagamentos()) {
            if (!p2.equals(p)) {
                p2.setPago(true);
                p2.setDataPagamento(DataUtil.getCalendar(DataUtil.hoje()));
            }
            new DAO().salvar(p2);

        }
    }

    private void verificarVinculo(Pagamento p1) {
        if (p1.getConta().getContaVinculada() != null) {
            TransacaoBancaria transacao = new TransacaoBancaria();
            if (p1.getTransacaoBancaria() != null) {
                transacao = p1.getTransacaoBancaria();
            }

            Pagamento pagamentoRelacionado = new Pagamento();
            if (transacao.getPagamentos() != null) {
                for (Pagamento p : transacao.getPagamentos()) {
                    if (!p.equals(p1)) {
                        pagamentoRelacionado = p;
                    }
                }
            }



//                new DAO().salvar(transacao);

            pagamentoRelacionado.setDataPagamento(DataUtil.getCalendar(txtData.getValue()));
            pagamentoRelacionado.setHistorico(p1.getConta().getContaVinculada().getNome());
            pagamentoRelacionado.setConta(p1.getConta().getContaVinculada());
            if (pagamentoRelacionado.getConta().isCredito()) {
                pagamentoRelacionado.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
            } else {
                pagamentoRelacionado.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
            }
            pagamentoRelacionado.setSaldo(new BigDecimal(0));
            pagamentoRelacionado.setDadosPagamento(p1.getDadosPagamento());

            String nome = pagamentoRelacionado.getConta().getNomeVinculo();

            if (nome.equals("AF")) {
                pagamentoRelacionado.setAplicacao(condominio.getAplicacao());
            } else if (nome.equals("PO")) {
                pagamentoRelacionado.setPoupanca(condominio.getPoupanca());
            } else if (nome.equals("CO")) {
                pagamentoRelacionado.setConsignacao(condominio.getConsignacao());
            } else if (nome.equals("EM")) {
            }

            pagamentoRelacionado.setPago(true);


            transacao.adicionarPagamento(p1);
            transacao.adicionarPagamento(pagamentoRelacionado);

            if (nome.equals("AF")) {

                verificarDataPagamentoAplicacao(pagamentoRelacionado);
                condominio.getAplicacao().adicionarPagamento(pagamentoRelacionado);
                condominio.getAplicacao().setSaldo(condominio.getAplicacao().getSaldo().add(pagamentoRelacionado.getValor()));

            } else if (nome.equals("PO")) {

                verificarDataPagamentoPoupanca(pagamentoRelacionado);
                condominio.getPoupanca().adicionarPagamento(pagamentoRelacionado);
                condominio.getPoupanca().setSaldo(condominio.getPoupanca().getSaldo().add(pagamentoRelacionado.getValor()));

            } else if (nome.equals("CO")) {

                verificarDataPagamentoConsignacao(pagamentoRelacionado);
                condominio.getConsignacao().adicionarPagamento(pagamentoRelacionado);
                condominio.getConsignacao().setSaldo(condominio.getConsignacao().getSaldo().add(pagamentoRelacionado.getValor()));

            } else if (nome.equals("EM")) {
            }

            System.out.println("Transacao Bancária: " + transacao);

            pagamento.setTransacaoBancaria(transacao);
            pagamentoRelacionado.setTransacaoBancaria(transacao);
        }
    }

    private void verificarDataPagamentoAplicacao(Pagamento p2) {
        if (condominio.getAplicacao().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getAplicacao().setSaldo(p2.getValor());
        }
    }

    private void verificarDataPagamentoPoupanca(Pagamento p2) {
        if (condominio.getPoupanca().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getPoupanca().setSaldo(p2.getValor());
        }
    }

    private void verificarDataPagamentoConsignacao(Pagamento p2) {
        if (condominio.getConsignacao().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getConsignacao().setSaldo(p2.getValor());
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnPagar) {
                efetuarPagamento();
            } else if (e.getSource() == btnCancelar) {
                dispose();
            } else if (e.getSource() == btnImprimir) {
                if (getPagamentos().get(0).getForma() == FormaPagamento.CHEQUE) {
                    imprimirCheques();
                } else {
                    ApresentacaoUtil.exibirInformacao("Relatorio vai ser feito depois", DialogoPagarContaPagar.this);
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
                        resultado = pesquisarContaPorCodigo(Integer.valueOf(txtConta.getText()));
                        if (resultado != null) {
                            conta = resultado;
                            txtConta.setText(String.valueOf(conta.getCodigo()));
                            txtHistorico.setText(conta.getNome());
                        } else {
                            ApresentacaoUtil.exibirErro("Código Inexistente!", DialogoPagarContaPagar.this);
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
                    ApresentacaoUtil.transferidorFocoEnter, DialogoPagarContaPagar.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, DialogoPagarContaPagar.this, JTextField.class);

            btnPagar.addActionListener(this);
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

        painelContaPagar = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtData = new net.sf.nachocalendar.components.DateField();
        txtValor = new javax.swing.JTextField();
        txtConta = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        cbFornecedores = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnNumeroDocumento = new javax.swing.JToggleButton();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnPagar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        painelNovoPagamento = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        txtHistoricoNovoPagamento = new javax.swing.JTextField();
        btnConta1 = new javax.swing.JButton();
        txtContaNovoPagamento = new javax.swing.JTextField();
        txtValorNovoPagamento = new javax.swing.JTextField();
        txtDataNovoPagamento = new net.sf.nachocalendar.components.DateField();
        jLabel7 = new javax.swing.JLabel();
        txtNumeroDocumentoNovoPagamento = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        btnNumeroDocumentoNovoPagamento = new javax.swing.JToggleButton();
        jLabel9 = new javax.swing.JLabel();
        cbFornecedoresNovoPagamento = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        btnCancelarNovoPagamento = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Editar Conta a Pagar");
        setModal(true);

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

        cbFornecedores.setName("fornecedor"); // NOI18N

        jLabel2.setText("Fornecedor:");

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

        javax.swing.GroupLayout painelContaPagarLayout = new javax.swing.GroupLayout(painelContaPagar);
        painelContaPagar.setLayout(painelContaPagarLayout);
        painelContaPagarLayout.setHorizontalGroup(
            painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContaPagarLayout.createSequentialGroup()
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
                        .addComponent(jLabel2))
                    .addGroup(painelContaPagarLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cbFornecedores, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(11, Short.MAX_VALUE))
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
                .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnPagar.setText("Pagar");
        btnPagar.setToolTipText("Salvar");
        btnPagar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 72, 11, 0);
        jPanel1.add(btnPagar, gridBagConstraints);

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

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12));
        jLabel6.setText("Total: R$");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 9));
        jLabel5.setText("Atenção: Todos os pagamentos com mesmo número serão pagos automaticamente!");

        lblTotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(255, 0, 0));
        lblTotal.setText("10.000,00");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotal)
                    .addComponent(jLabel6))
                .addGap(11, 11, 11)
                .addComponent(jLabel5)
                .addGap(22, 22, 22))
        );

        jTabbedPane1.addTab("Pagamentos Relacionados", jPanel3);

        txtHistoricoNovoPagamento.setName("Histórico"); // NOI18N

        btnConta1.setText("Conta:");
        btnConta1.setBorder(null);
        btnConta1.setBorderPainted(false);
        btnConta1.setContentAreaFilled(false);
        btnConta1.setFocusable(false);
        btnConta1.setRequestFocusEnabled(false);
        btnConta1.setVerifyInputWhenFocusTarget(false);

        txtContaNovoPagamento.setName("Conta"); // NOI18N

        txtValorNovoPagamento.setName("Valor"); // NOI18N

        txtDataNovoPagamento.setFocusable(false);
        txtDataNovoPagamento.setName("data"); // NOI18N
        txtDataNovoPagamento.setRequestFocusEnabled(false);

        jLabel7.setText("Histórico:");

        txtNumeroDocumentoNovoPagamento.setName("documento"); // NOI18N

        jLabel8.setText("Valor:");

        btnNumeroDocumentoNovoPagamento.setText("Nº Doc");
        btnNumeroDocumentoNovoPagamento.setToolTipText("Clique para alternar o tipo de Registro!");
        btnNumeroDocumentoNovoPagamento.setBorderPainted(false);
        btnNumeroDocumentoNovoPagamento.setContentAreaFilled(false);
        btnNumeroDocumentoNovoPagamento.setFocusPainted(false);
        btnNumeroDocumentoNovoPagamento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNumeroDocumentoNovoPagamentoActionPerformed(evt);
            }
        });

        jLabel9.setText("Data Pagamento:");

        cbFornecedoresNovoPagamento.setName("fornecedor"); // NOI18N

        jLabel10.setText("Fornecedor:");

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        jButton1.setToolTipText("Adicionar");

        btnCancelarNovoPagamento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnCancelarNovoPagamento.setToolTipText("Cancelar");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(130, 130, 130)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67)
                .addComponent(btnCancelarNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(cbFornecedoresNovoPagamento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(txtHistoricoNovoPagamento, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel7)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(txtDataNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(btnNumeroDocumentoNovoPagamento)
                                        .addGap(33, 33, 33)
                                        .addComponent(btnConta1)
                                        .addGap(33, 33, 33)
                                        .addComponent(jLabel8))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(txtNumeroDocumentoNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(6, 6, 6)
                                        .addComponent(txtContaNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtValorNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap())))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(9, 9, 9)
                        .addComponent(txtDataNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(btnNumeroDocumentoNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnConta1)
                            .addComponent(jLabel8))
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(txtNumeroDocumentoNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(txtContaNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(txtValorNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtHistoricoNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFornecedoresNovoPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnCancelarNovoPagamento)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnCancelarNovoPagamento, jButton1});

        javax.swing.GroupLayout painelNovoPagamentoLayout = new javax.swing.GroupLayout(painelNovoPagamento);
        painelNovoPagamento.setLayout(painelNovoPagamentoLayout);
        painelNovoPagamentoLayout.setHorizontalGroup(
            painelNovoPagamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelNovoPagamentoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelNovoPagamentoLayout.setVerticalGroup(
            painelNovoPagamentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelNovoPagamentoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Novo Pagamento Conforme Disponibilidade", painelNovoPagamento);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(painelContaPagar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(painelContaPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNumeroDocumentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNumeroDocumentoActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_btnNumeroDocumentoActionPerformed

    private void btnNumeroDocumentoNovoPagamentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNumeroDocumentoNovoPagamentoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNumeroDocumentoNovoPagamentoActionPerformed
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCancelarNovoPagamento;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnConta1;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JToggleButton btnNumeroDocumento;
    private javax.swing.JToggleButton btnNumeroDocumentoNovoPagamento;
    private javax.swing.JButton btnPagar;
    private javax.swing.JComboBox cbFornecedores;
    private javax.swing.JComboBox cbFornecedoresNovoPagamento;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel painelContaPagar;
    private javax.swing.JPanel painelNovoPagamento;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtConta;
    private javax.swing.JTextField txtContaNovoPagamento;
    private net.sf.nachocalendar.components.DateField txtData;
    private net.sf.nachocalendar.components.DateField txtDataNovoPagamento;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtHistoricoNovoPagamento;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtNumeroDocumentoNovoPagamento;
    private javax.swing.JTextField txtValor;
    private javax.swing.JTextField txtValorNovoPagamento;
    // End of variables declaration//GEN-END:variables
}
