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

import bemaJava.Bematech;
import com.sun.jna.Native;
import condominioPlus.Main;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.DadosTalaoCheque;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaPagar;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.fornecedor.Fornecedor;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
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
    private Condominio condominio;
    private Conta conta;
    private TabelaModelo_2 modeloTabela;
    private TabelaModelo_2 modeloTabela2;
    private List<Pagamento> pagamentos;
    private List<Pagamento> cheques = new ArrayList<Pagamento>();

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

//        carregarComboFiltro();
//        new LimitarCaracteres(10).ValidaNumero(txtConta);
        txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());
        painelCheques.setVisible(false);
        btnGravar.setEnabled(false);
        btnImprimir.setEnabled(false);
        if (condominio != null) {
            this.setTitle("Contas a Pagar - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabelaContaPagar, "Vencimento, Conta, Documento, Fornecedor, Descrição, Valor".split(",")) {

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
//            protected List<Pagamento> getFiltrar(List<Pagamento> lista) {
//                return filtrarListaPorCredito(lista);
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
                        return pagamento.getFornecedor() != null ? pagamento.getFornecedor().getNome() : "";
                    case 4:
                        return pagamento.getHistorico();
                    case 5:
                        return pagamento.getValor();
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Pagamento pagamento) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getHistorico() + " ?", TelaContaPagar.this)) {
                    return false;
                }

                try {
                    new DAO().remover(modeloTabela.getObjetosSelecionados());
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getHistorico());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaContaPagar.this);
                    return false;
                }
            }
        };

        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        esquerda.setHorizontalAlignment(SwingConstants.LEFT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaContaPagar.getColumn(modeloTabela.getCampo(1)).setCellRenderer(direita);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(2)).setCellRenderer(direita);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(3)).setCellRenderer(centralizado);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(4)).setCellRenderer(esquerda);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(5)).setCellRenderer(direita);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(3)).setMinWidth(180);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(4)).setMinWidth(280);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(5)).setMinWidth(110);
    }

    private void carregarTabelaCheque() {
        modeloTabela2 = new TabelaModelo_2<Pagamento>(tabelaCheque, "Data, Cheque, Descrição, Valor".split(",")) {

            @Override
            protected List<Pagamento> getCarregarObjetos() {
                return cheques;
            }

            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getDataVencimento());
                    case 1:
                        return String.valueOf(((DadosCheque) pagamento.getDadosPagamento()).getNumero());
                    case 2:
                        return pagamento.getHistorico();
                    case 3:
                        return pagamento.getValor();

                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Pagamento pagamento) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getHistorico() + " ?", TelaContaPagar.this)) {
                    return false;
                }
                if (cheques.removeAll(modeloTabela2.getObjetosSelecionados())) {
                    modeloTabela2.carregarObjetos();
                    return true;

                } else {

                    return false;
                }


            }
        };
        tabelaContaPagar.getColumn(modeloTabela.getCampo(1)).setMinWidth(180);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(2)).setMinWidth(280);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(3)).setMinWidth(110);
    }

    private void gravarCheques() {
        if (!cheques.isEmpty()) {
            condominio.getContaCorrente().getPagamentos().addAll(cheques);
            new DAO().salvar(cheques);
            cheques.clear();
            carregarTabelaCheque();
            carregarTabela();
        } else {
            ApresentacaoUtil.exibirInformacao("Não é possivel gravar sem ter inserido cheques!", this);
        }
    }

    private String somarCheque() {
        BigDecimal total = new BigDecimal(0);
        for (Pagamento cheque : cheques) {
            total = total.add(cheque.getValor());
        }
        return String.valueOf(total.negate());
    }

    private void imprimirCheques() {
        int iRetorno;
        Pagamento p = null;
        if (!cheques.isEmpty()) {
            p = cheques.get(0);
        } else {
            ApresentacaoUtil.exibirErro("Deve-se incluir cheques para impressão!", this);
        }

        Bematech lib =
                (Bematech) Native.loadLibrary("BEMADP32", Bematech.class);
        iRetorno = lib.Bematech_DP_IniciaPorta("COM1");
        lib.Bematech_DP_IncluiAlteraBanco("555", "3,7,9,11,13,92,20,8,10,62,23,32,55");
        String valor = somarCheque().replace('.', ',');
        System.out.println("valor " + valor);
        iRetorno = lib.Bematech_DP_ImprimeCheque("555", valor, p.getFornecedor().getNome(), "ARMACAO DOS BUZIOS", DataUtil.getDateTime(p.getDataVencimento()).toString("ddMMyy"), "");
        System.out.println(iRetorno);

    }

    private void limparCampos() {
        txtHistorico.setText(fixarHistorico());
        txtConta.setText("");
        txtNumeroDocumento.setText("");
        txtValor.setText("");
        cbFornecedores.setSelectedIndex(-1);
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

    private List<Pagamento> getPagamentos() {
        Date datInicio = (Date) dataInicio.getValue();
        Date datTermino = (Date) dataTermino.getValue();

        pagamentos = new DAO().listar(Pagamento.class, "PagamentosContaPagarPorPeriodo", contaPagar, datInicio, datTermino);

        return pagamentos;
    }

    private DadosTalaoCheque getDadosTalaoCheque() {
        if (!condominio.getDadosTalaoCheques().isEmpty()) {
            for (DadosTalaoCheque dados : condominio.getDadosTalaoCheques()) {
                if (dados.isEmUso()) {
                    return dados;
                }
            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Não existem cheques Cadastrados", this);
        }
        return null;
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
        if (btnNumeroDocumento.isSelected()) {
            if (!getDadosTalaoCheque().verificarIntervaloCheque(txtNumeroDocumento.getText())) {
                ApresentacaoUtil.exibirAdvertencia("Número do cheque incorreto! Digite um numero entre " + getDadosTalaoCheque().getNumeroInicial() + " - " + getDadosTalaoCheque().getNumeroFinal(), this);
                txtNumeroDocumento.grabFocus();
                txtNumeroDocumento.selectAll();
                return;
            }
        }

        pagamento.setDataVencimento(DataUtil.getCalendar(txtData.getValue()));
        pagamento.setHistorico(txtHistorico.getText().toUpperCase());
        pagamento.setConta(conta);
        if (pagamento.getConta() != null) {
            if (pagamento.getConta().isCredito()) {
                pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
            } else {
                pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
            }
        }
        pagamento.setSaldo(new BigDecimal(0));
        pagamento.setFornecedor((Fornecedor) cbFornecedores.getModel().getSelectedItem());
        pagamento.setContaPagar(condominio.getContaPagar());
        pagamento = selecionaFormaPagamento(pagamento);
        if (pagamento.getForma() == FormaPagamento.CHEQUE) {
            cheques.add(pagamento);
            carregarTabelaCheque();
            limparCampos();
            txtNumeroDocumento.grabFocus();
            txtNumeroDocumento.setText(condominio.getContaBancaria().getContaCorrente());
        } else {
            verificarDataPagamento(pagamento);
            condominio.getContaPagar().adicionarPagamento(pagamento);
            new DAO().salvar(condominio);
            limparCampos();
        }
    }

    private Pagamento selecionaFormaPagamento(Pagamento p) {
        if (btnNumeroDocumento.isSelected()) {
            p.setForma(FormaPagamento.CHEQUE);
            p.setDadosPagamento(new DadosCheque(Long.valueOf(txtNumeroDocumento.getText()), condominio.getContaBancaria().getContaCorrente(), condominio.getRazaoSocial()));
            for (Pagamento cheque : cheques) {
                if (((DadosCheque) cheque.getDadosPagamento()).getNumero() == ((DadosCheque) p.getDadosPagamento()).getNumero()) {
                    p.setDadosPagamento(((DadosCheque) cheque.getDadosPagamento()));
                }
            }
            return p;
        } else {
            p.setForma(FormaPagamento.DINHEIRO);
            p.setDadosPagamento(new DadosDOC(Long.valueOf(txtNumeroDocumento.getText())));
            List<Pagamento> documentos = new DAO().listar("PagamentosPorForma", Main.getCondominio().getContaPagar(), FormaPagamento.DINHEIRO);
            for (Pagamento documento : documentos) {
                if (((DadosDOC) documento.getDadosPagamento()).getNumeroDocumento() == ((DadosDOC) p.getDadosPagamento()).getNumeroDocumento()) {
                    p.setDadosPagamento(((DadosDOC) documento.getDadosPagamento()));
                }
            }
            return p;
        }

    }

    private void verificarDataPagamento(Pagamento p2) {
        if (condominio.getContaCorrente().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getContaCorrente().setSaldo(p2.getValor());

        }
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
        DialogoConta c = new DialogoConta(null, true, false, false);
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

    private void adicionarPagamento() {
        preencherPagamento();
    }

    private void apagarItensSelecionados() {
        if (modeloTabela.getObjetosSelecionados().isEmpty()) {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        } else if (ApresentacaoUtil.perguntar("Deseja remover os pagamentos?", this)) {
            List<Pagamento> itensRemover = modeloTabela.getObjetosSelecionados();

            for (Pagamento p : itensRemover) {
                modeloTabela.remover(p);
                modeloTabela.notificar();

                new DAO().remover(itensRemover);
//                condominio.getContaCorrente().getPagamentos().removeAll(itensRemover);
                new DAO().salvar(contaPagar);
            }
            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
        }
    }

    private void trocarFormaPagamento() {
        if (btnNumeroDocumento.isSelected()) {
            btnNumeroDocumento.setText("Nº Cheque:");
            painelCheques.setVisible(true);
            btnGravar.setEnabled(true);
            btnImprimir.setEnabled(true);
        } else {
            btnNumeroDocumento.setText("Nº Doc:");
            btnGravar.setEnabled(false);
            btnImprimir.setEnabled(false);
            painelCheques.setVisible(false);
            txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());
        }

    }

    private void editarPagamento() {
        if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
            DialogoEditarContaPagar tela = new DialogoEditarContaPagar((Pagamento) modeloTabela.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloTabela.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
        }
    }

    private void PagarPagamento() {
        if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
            DialogoPagarContaPagar tela = new DialogoPagarContaPagar((Pagamento) modeloTabela.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloTabela.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
        }
    }

//    private void carregarComboFiltro() {
//        cbFiltros.setModel(new ComboModelo<String>(Util.toList(new String[]{" ", "Crédito", "Débito"}), false));
//    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private class ControladorEventos extends ControladorEventosGenerico {

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
                PagarPagamento();
            } else if (origem == btnFixarHistórico) {
            } else if (origem == btnNumeroDocumento) {
                trocarFormaPagamento();
            } else if (origem == btnGravar) {
                gravarCheques();
            } else if (origem == btnImprimir) {
                imprimirCheques();
            } else if (origem == itemMenuEditarPagamento) {
                editarPagamento();

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
            itemMenuEditarPagamento.addActionListener(this);
            dataInicio.addChangeListener(this);
            dataTermino.addChangeListener(this);
            itemMenuPagarSelecionados.addActionListener(this);
            txtConta.addFocusListener(this);
            btnNumeroDocumento.addActionListener(this);
            btnGravar.addActionListener(this);
            btnImprimir.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                System.out.println("released");
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                System.out.println("pressed");
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaContaPagar.this);
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

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuApagarSelecionados = new javax.swing.JMenuItem();
        itemMenuPagarSelecionados = new javax.swing.JMenuItem();
        itemMenuEditarPagamento = new javax.swing.JMenuItem();
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
        txtNumeroDocumento = new javax.swing.JTextField();
        btnNumeroDocumento = new javax.swing.JToggleButton();
        btnImprimir = new javax.swing.JButton();
        btnGravar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        dataTermino = new net.sf.nachocalendar.components.DateField();
        jLabel4 = new javax.swing.JLabel();
        dataInicio = new net.sf.nachocalendar.components.DateField();
        painelCheques = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaCheque = new javax.swing.JTable();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        itemMenuPagarSelecionados.setText("Pagar Selecionados");
        itemMenuPagarSelecionados.setToolTipText("Efetua Pagamento dos Itens Selecionados");
        popupMenu.add(itemMenuPagarSelecionados);

        itemMenuEditarPagamento.setText("Editar Pagamento");
        itemMenuEditarPagamento.setToolTipText("Editar um Pagamento Selecionado");
        popupMenu.add(itemMenuEditarPagamento);

        setClosable(true);
        setTitle("Contas a Pagar");
        setPreferredSize(new java.awt.Dimension(843, 626));

        tabelaContaPagar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaPagar);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
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

        btnFixarHistórico.setText("Fixar Histórico");

        jLabel2.setText("Fornecedor:");

        btnNumeroDocumento.setText("Nº Doc");
        btnNumeroDocumento.setToolTipText("Clique para alternar o tipo de Registro!");
        btnNumeroDocumento.setBorderPainted(false);
        btnNumeroDocumento.setContentAreaFilled(false);
        btnNumeroDocumento.setFocusPainted(false);

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Cheque");

        btnGravar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/buscar.gif"))); // NOI18N
        btnGravar.setToolTipText("Gravar Cheques");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(71, 71, 71)
                        .addComponent(btnNumeroDocumento)
                        .addGap(33, 33, 33)
                        .addComponent(btnConta)
                        .addGap(33, 33, 33)
                        .addComponent(jLabel3)
                        .addGap(99, 99, 99)
                        .addComponent(jLabel2))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFixarHistórico)
                        .addGap(37, 37, 37)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGravar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(btnNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnConta)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnFixarHistórico, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnIncluir)
                    .addComponent(btnImprimir)
                    .addComponent(btnGravar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnFixarHistórico, btnGravar, btnImprimir, btnIncluir});

        jLabel5.setText("Data Inicial:");

        jLabel4.setText("Data Final:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(266, 266, 266)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataTermino, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(271, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(dataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(dataTermino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaCheque.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tabelaCheque);

        javax.swing.GroupLayout painelChequesLayout = new javax.swing.GroupLayout(painelCheques);
        painelCheques.setLayout(painelChequesLayout);
        painelChequesLayout.setHorizontalGroup(
            painelChequesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE)
        );
        painelChequesLayout.setVerticalGroup(
            painelChequesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE)
                            .addComponent(painelCheques, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelCheques, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JToggleButton btnFixarHistórico;
    private javax.swing.JButton btnGravar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JToggleButton btnNumeroDocumento;
    private javax.swing.JComboBox cbFornecedores;
    private net.sf.nachocalendar.components.DateField dataInicio;
    private net.sf.nachocalendar.components.DateField dataTermino;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JMenuItem itemMenuEditarPagamento;
    private javax.swing.JMenuItem itemMenuPagarSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel painelCheques;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaCheque;
    private javax.swing.JTable tabelaContaPagar;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
