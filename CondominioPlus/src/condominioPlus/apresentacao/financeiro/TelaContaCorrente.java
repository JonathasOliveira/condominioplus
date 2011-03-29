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
import condominioPlus.negocio.financeiro.ContaCorrente;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.fornecedor.Fornecedor;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class TelaContaCorrente extends javax.swing.JInternalFrame {

    private ContaCorrente contaCorrente;
    private Pagamento pagamento;
    private Condominio condominio;
    private Conta conta;
    private TabelaModelo_2 modeloTabela;
    private List<Pagamento> cheques = new ArrayList<Pagamento>();
    private List<Pagamento> pagamentos;

    /** Creates new form TelaContaCorrente */
    public TelaContaCorrente(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getContaCorrente() == null) {
            contaCorrente = new ContaCorrente();
            condominio.setContaCorrente(contaCorrente);
            contaCorrente.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            contaCorrente = condominio.getContaCorrente();
            if (contaCorrente.getCondominio() == null) {
                contaCorrente.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }

        initComponents();
        new ControladorEventos();

        carregarFornecedor();

        carregarTabela();

        carregarComboFiltro();

        if (condominio != null) {
            this.setTitle("Conta Corrente - " + condominio.getRazaoSocial());
        }
    }
    //doida da claudia

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabelaContaCorrente, "Data, Documento, Conta, Descrição, Valor, Saldo ".split(",")) {

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

            @Override
            protected List<Pagamento> getFiltrar(List<Pagamento> pagamentos) {
                return filtrarListaPorCredito(pagamentos);
            }

            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getDataPagamento());
                    case 1:
                        return pagamento.getForma() == FormaPagamento.CHEQUE ? String.valueOf(((DadosCheque) pagamento.getDadosPagamento()).getNumero()) : String.valueOf(((DadosDOC) pagamento.getDadosPagamento()).getNumeroDocumento());
                    case 2:
                        return pagamento.getConta().getCodigo();
                    case 3:
                        return pagamento.getHistorico();
                    case 4:
                        return pagamento.getValor();
                    case 5:
                        return pagamento.getSaldo();
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Pagamento pagamento) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getHistorico() + " ?", TelaContaCorrente.this)) {
                    return false;
                }

                try {
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getHistorico());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaContaCorrente.this);
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

        tabelaContaCorrente.getColumn(modeloTabela.getCampo(1)).setCellRenderer(direita);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(2)).setCellRenderer(direita);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setCellRenderer(direita);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setCellRenderer(direita);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setCellRenderer(direita);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setMinWidth(300);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);

    }

    private void carregarComboFiltro() {
        cbFiltros.setModel(new ComboModelo<String>(Util.toList(new String[]{" ", "Crédito", "Débito"}), false));
    }

    private List<Pagamento> filtrarListaPorCredito(List<Pagamento> listaGeral) {
        List<Pagamento> lista = new ArrayList<Pagamento>();
        if (cbFiltros.getSelectedIndex() != -1) {
            if (cbFiltros.getSelectedItem().toString().equals("Crédito")) {
                for (Pagamento p : listaGeral) {
                    if (p.getConta().isCredito()) {
                        lista.add(p);
                    }
                }
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(0);
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMaxWidth(0);
                return lista;

            } else if (cbFiltros.getSelectedItem().toString().equals("Débito")) {
                for (Pagamento p : getPagamentos()) {
                    if (!p.getConta().isCredito()) {
                        lista.add(p);
                    }
                }
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(0);
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMaxWidth(0);
                return lista;
            } else if (cbFiltros.getSelectedItem().toString().equals(" ")) {
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(100);
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMaxWidth(100);

                return getPagamentos();
            }
        }
        return getPagamentos();

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtNumeroDocumento);
        campos.add(txtConta);
        campos.add(txtValor);
        campos.add(txtHistorico);
        return campos;
    }

    private List<Pagamento> getPagamentos() {
        pagamentos = new DAO().listar("PagamentosContaCorrente", condominio.getContaCorrente());
        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();
        Collections.sort(pagamentos, comCod);
        ComparatorPagamento comparator = new ComparatorPagamento();
        Collections.sort(pagamentos, comparator);
        return pagamentos;
    }

    private void preencherPagamento() {
        pagamento = new Pagamento();
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(this);
            return;
        }
        pagamento.setDataPagamento(DataUtil.getCalendar(txtData.getValue()));
        pagamento.setHistorico(txtHistorico.getText());
        pagamento.setFornecedor((Fornecedor) cbFornecedores.getModel().getSelectedItem());
        pagamento.setConta(conta);
        if (pagamento.getConta().isCredito()) {
            pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
        } else {
            pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
        }
        pagamento.setSaldo(new BigDecimal(0));
        pagamento.setContaCorrente(condominio.getContaCorrente());
        pagamento.setPago(true);
        verificarDataPagamento(pagamento);

        pagamento.setForma(FormaPagamento.DINHEIRO);
        pagamento.setDadosPagamento(new DadosDOC(Long.valueOf(txtNumeroDocumento.getText())));
        condominio.getContaCorrente().adicionarPagamento(pagamento);
        condominio.getContaCorrente().setSaldo(condominio.getContaCorrente().getSaldo().add(pagamento.getValor()));

        verificarVinculo();

        new DAO().salvar(condominio);
        limparCampos();

    }

    private void verificarVinculo() {
        if (conta.getContaVinculada() != null) {
            TransacaoBancaria transacao = new TransacaoBancaria();
            if (pagamento.getTransacaoBancaria() != null) {
                transacao = pagamento.getTransacaoBancaria();
            }

            Pagamento pagamentoRelacionado = new Pagamento();
            if(transacao.getPagamentos() != null){
                for (Pagamento p : transacao.getPagamentos()) {
                    if(!p.equals(pagamento)){
                        pagamentoRelacionado = p;
                    }
                }
            }



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


            transacao.adicionarPagamento(pagamento);
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

    private String fixarHistorico() {
        String texto = "";
        if (btnFixarHistórico.isSelected()) {
            texto = txtHistorico.getText();
            return texto;
        } else {
            return texto;
        }

    }

    private void verificarDataPagamento(Pagamento p2) {
        if (condominio.getContaCorrente().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            condominio.getContaCorrente().setSaldo(p2.getValor());

        }
    }

    private void verificarLista() {
        if (condominio.getContaCorrente().getPagamentos().size() == 1) {
            for (Pagamento p : getPagamentos()) {
                p.setSaldo(p.getValor());
                condominio.getContaCorrente().setSaldo(p.getValor());

            }
        }
    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false);
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
        txtNumeroDocumento.setText("");
        txtValor.setText("");
        cbFornecedores.setSelectedIndex(-1);
    }

    private void carregarFornecedor() {
        cbFornecedores.setModel(new ComboModelo<Fornecedor>(new DAO().listar(Fornecedor.class)));
    }

    private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Desejar remover os pagamentos?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<Pagamento> itensRemoverContaCorrente = modeloTabela.getObjetosSelecionados();
            List<Pagamento> itensRelacionadosRemover = new ArrayList<Pagamento>();

            for (Pagamento p : itensRemoverContaCorrente) {
                if (p.getTransacaoBancaria() != null) {
                    TransacaoBancaria transacao = p.getTransacaoBancaria();
                    Pagamento pagamentoRelacionado = new Pagamento();
                    for (Pagamento p2 : transacao.getPagamentos()) {
                        if (!p.equals(p2)) {
                            pagamentoRelacionado = p2;
                            pagamentoRelacionado.setDadosPagamento(null);

                            String nome = pagamentoRelacionado.getConta().getNomeVinculo();

                            if (nome.equals("AF")) {
                                condominio.getAplicacao().setSaldo(condominio.getAplicacao().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            } else if (nome.equals("PO")) {
                                condominio.getPoupanca().setSaldo(condominio.getPoupanca().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            } else if (nome.equals("CO")) {
                                condominio.getConsignacao().setSaldo(condominio.getConsignacao().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            } else if (nome.equals("EM")) {
                            }
                            //verificar

                            itensRelacionadosRemover.add(pagamentoRelacionado);
                        }
                    }
                    new DAO().remover(transacao);
                }
                modeloTabela.remover(p);
                modeloTabela.notificar();
                contaCorrente.setSaldo(contaCorrente.getSaldo().subtract(p.getValor()));
            }
            if (!itensRelacionadosRemover.isEmpty()) {
                for (Pagamento p : itensRelacionadosRemover) {

                    String nome = p.getConta().getNomeVinculo();

                    if (nome.equals("AF")) {
                        condominio.getAplicacao().getPagamentos().remove(p);
                    } else if (nome.equals("PO")) {
                        condominio.getPoupanca().getPagamentos().remove(p);
                    } else if (nome.equals("CO")) {
                        condominio.getConsignacao().getPagamentos().remove(p);
                    } else if (nome.equals("EM")) {
                    }
                }
                new DAO().remover(itensRelacionadosRemover);
                //verificar
            }
            new DAO().remover(itensRemoverContaCorrente);
            condominio.getContaCorrente().getPagamentos().removeAll(itensRemoverContaCorrente);
            new DAO().salvar(condominio);
            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
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
            if (origem == btnDocumento) {
                contador = 0;
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnIncluir) {
                adicionarPagamento();
                carregarTabela();
            } else if (origem == btnCalcular) {
                verificarLista();
                contaCorrente.calculaSaldo(contaCorrente);
                carregarTabela();
            } else if (origem == btnGravar) {
//                gravarCheques();
            } else if (origem == itemMenuApagarSelecionados) {
                apagarItensSelecionados();
            } else if (origem == btnFixarHistórico) {
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (cbFiltros.getSelectedIndex() != -1) {
                modeloTabela.filtrar();
            }
        }

        @Override
        public void configurar() {

            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaContaCorrente.this, JTextField.class);

            btnConta.addActionListener(this);
            btnDocumento.addActionListener(this);
            cbFiltros.addItemListener(this);
            btnCalcular.addActionListener(this);
            btnFixarHistórico.addActionListener(this);
            btnGravar.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnIncluir.addActionListener(this);
            btnPesquisar.addActionListener(this);
            tabelaContaCorrente.addMouseListener(this);
            itemMenuApagarSelecionados.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuApagarSelecionados = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContaCorrente = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnPesquisar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        btnGravar = new javax.swing.JButton();
        btnIncluir = new javax.swing.JButton();
        btnCalcular = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtNumeroDocumento = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtData = new net.sf.nachocalendar.components.DateField();
        txtValor = new javax.swing.JTextField();
        txtConta = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        btnDocumento = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        txtHistorico = new javax.swing.JTextField();
        cbFornecedores = new javax.swing.JComboBox();
        btnFixarHistórico = new javax.swing.JToggleButton();
        cbFiltros = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        jMenuItem2.setText("jMenuItem2");
        popupMenu.add(jMenuItem2);

        jMenuItem3.setText("jMenuItem3");
        popupMenu.add(jMenuItem3);

        setClosable(true);
        setTitle("Conta Corrente");

        tabelaContaCorrente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaCorrente);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnPesquisar.setText("Pesquisar");

        btnImprimir.setText("Imprimir");

        btnGravar.setText("Gravar");

        btnIncluir.setText("Incluir");

        btnCalcular.setText("Recalc");

        jLabel3.setText("Valor:");

        txtNumeroDocumento.setName("Número Documento"); // NOI18N

        jLabel1.setText("Data Lançamento:");

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

        btnDocumento.setText("Nº Cheque:");
        btnDocumento.setBorder(null);
        btnDocumento.setBorderPainted(false);
        btnDocumento.setContentAreaFilled(false);
        btnDocumento.setFocusable(false);
        btnDocumento.setRequestFocusEnabled(false);
        btnDocumento.setVerifyInputWhenFocusTarget(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtData, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(29, 29, 29)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDocumento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCalcular)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnGravar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPesquisar))
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCalcular, btnGravar, btnImprimir, btnIncluir});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnConta, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(btnDocumento))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnIncluir)
                        .addComponent(btnCalcular)
                        .addComponent(btnImprimir)
                        .addComponent(btnGravar)
                        .addComponent(btnPesquisar))
                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtHistorico.setName("Histórico"); // NOI18N

        btnFixarHistórico.setText("Fixar Histórico");

        jLabel6.setText("Filtrar por:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFixarHistórico)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(cbFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFixarHistórico))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnDocumento;
    private javax.swing.JToggleButton btnFixarHistórico;
    private javax.swing.JButton btnGravar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JComboBox cbFiltros;
    private javax.swing.JComboBox cbFornecedores;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaContaCorrente;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}

