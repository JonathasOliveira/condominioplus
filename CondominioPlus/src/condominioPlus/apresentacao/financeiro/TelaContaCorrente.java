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
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
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
public class TelaContaCorrente extends javax.swing.JInternalFrame {

    private ContaCorrente contaCorrente;
    private Pagamento pagamento;
    private ComboModelo_2<Fornecedor> modelo;
    private Condominio condominio;
    private Conta conta;
    private TabelaModelo_2 modeloTabela;
    private TabelaModelo_2 modeloTabela2;
    private List<Pagamento> cheques = new ArrayList<Pagamento>();

    /** Creates new form TelaContaCorrente */
    public TelaContaCorrente(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getContaCorrente() == null) {
            contaCorrente = new ContaCorrente();
            condominio.setContaCorrente(contaCorrente);
            new DAO().salvar(condominio);
        } else {
            contaCorrente = condominio.getContaCorrente();
        }

        initComponents();
        new ControladorEventos();
        carregarFornecedor();
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Conta Corrente - " + condominio.getRazaoSocial());
        }
    }

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
                return new DAO().listar("PagamentosPorOrdem", condominio.getContaCorrente());
            }

//            @Override
//            protected List<Pagamento> getFiltrar(List<Pagamento> pagamentos) {
//                return filtrarListaPorNome(txtNome.getText(), pagamentos);
//            }
            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getData_lancamento());
                    case 1:
                        return pagamento.getNumeroDocumento();
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
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getNumeroDocumento() + " ?", TelaContaCorrente.this)) {
                    return false;
                }

                try {
                    new DAO().remover(condominio);
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getNumeroDocumento());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaContaCorrente.this);
                    return false;
                }
            }
        };
        modeloTabela.setLargura(1, 200, 200, -1);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(1)).setCellRenderer(new RenderizadorFundo());
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setCellRenderer(new RenderizadorFundo());
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setCellRenderer(new RenderizadorFundo());
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setCellRenderer(new RenderizadorFundo());

        tabelaContaCorrente.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(1)).setMaxWidth(80);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(2)).setMinWidth(30);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setMinWidth(325);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(100);

    }

    private void carregarTabelaCheque() {
        modeloTabela2 = new TabelaModelo_2<Pagamento>(tabelaCheque, "Data, Cheque, Descricão, Valor".split(",")) {

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
                return cheques;
            }

//            @Override
//            protected List<Pagamento> getFiltrar(List<Pagamento> pagamentos) {
//                return filtrarListaPorNome(txtNome.getText(), pagamentos);
//            }
            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getData_lancamento());
                    case 1:
                        return pagamento.getNumeroDocumento();
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
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getNumeroDocumento() + " ?", TelaContaCorrente.this)) {
                    return false;
                }

                try {
                    new DAO().remover(condominio);
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getNumeroDocumento());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaContaCorrente.this);
                    return false;
                }
            }
        };

        tabelaContaCorrente.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(1)).setMaxWidth(80);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(2)).setMinWidth(30);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setMinWidth(325);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(100);
    }

//    private List<Condominio> filtrarListaPorNome(String sequencia, List<Condominio> condominios) {
//        ArrayList<Condominio> listaFiltrada = new ArrayList<Condominio>();
//
//        String[] sequencias = sequencia.toUpperCase().split(" ", 0);
//
//        CONDOMINIOS:
//        for (Condominio c : condominios) {
//            for (String s : sequencias) {
//                if (!c.getRazaoSocial().toUpperCase().contains(s)) {
//                    continue CONDOMINIOS;
//                }
//            }
//
//            listaFiltrada.add(c);
//        }
//
//        return listaFiltrada;
//    }
    private void gravarCheques() {
        condominio.getContaCorrente().getPagamentos().addAll(cheques);
        cheques.clear();
        carregarTabelaCheque();
        contaCorrente.calculaSaldo();
        carregarTabela();
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtNumeroDocumento);
        campos.add(txtConta);
        campos.add(txtValor);
        campos.add(txtHistorico);
        return campos;
    }

    private void preencherPagamento() {
        pagamento = new Pagamento();
        ValidadorGenerico validador = new ValidadorGenerico();
        if (!validador.validar(listaCampos())) {
            validador.exibirErros(this);
        }
        pagamento.setData_lancamento(DataUtil.getCalendar(txtData.getValue()));
        pagamento.setHistorico(txtHistorico.getText());
        pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
        pagamento.setNumeroDocumento(txtNumeroDocumento.getText());
        pagamento.setFornecedor((Fornecedor) cbFornecedores.getModel().getSelectedItem());
        pagamento.setConta(conta);
        pagamento.setSaldo(new BigDecimal(0));
        pagamento.setContaCorrente(condominio.getContaCorrente());

        if (btnDocumento.getText().equalsIgnoreCase("Nº Cheque:")) {
            pagamento.setFormaPagamento("cheque");
            cheques.add(pagamento);
            carregarTabelaCheque();
        } else {
            pagamento.setFormaPagamento("documento");
            condominio.getContaCorrente().adicionarPagamento(pagamento);
            new DAO().salvar(condominio);
        }
    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true);
        c.setVisible(true);


        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            txtHistorico.setText(conta.getNome());


        }
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    private void adicionarPagamento() {
        preencherPagamento();
        limparCampos();
    }

    private void limparCampos() {
        txtHistorico.setText("");
        txtConta.setText("");
        txtNumeroDocumento.setText("");
        txtValor.setText("");
        cbFornecedores.setSelectedIndex(-1);
    }

    private void carregarFornecedor() {
        cbFornecedores.setModel(new ComboModelo<Fornecedor>(new DAO().listar(Fornecedor.class)));
    }

    private int verificarTipoDocumento(int contador) {
        if (contador == 1) {
            painelCheque.setVisible(true);
            btnDocumento.setText("Nº Cheque:");
            btnGravar.setEnabled(true);
            btnImprimir.setEnabled(true);

            return 0;

        } else {
            painelCheque.setVisible(false);
            btnDocumento.setText("Nº Doc:");
            btnGravar.setEnabled(false);
            btnImprimir.setEnabled(false);

            return 1;
        }
    }

    private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Desejar remover os pagamentos?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<Pagamento> itensRemover = modeloTabela.getObjetosSelecionados();

            for (Pagamento p : itensRemover) {
                modeloTabela.remover(p);
                modeloTabela.notificar();

                if (!p.getConta().isCredito()) {
                    contaCorrente.setSaldo(contaCorrente.getSaldo().add(p.getValor()));
                } else {
                    contaCorrente.setSaldo(contaCorrente.getSaldo().subtract(p.getValor()));
                }
            }
            new DAO().remover(itensRemover);
            new DAO().salvar(contaCorrente);
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
                contador = verificarTipoDocumento(contador);
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnIncluir) {
                adicionarPagamento();
                carregarTabela();
            } else if (origem == btnCalcular) {
                contaCorrente.calculaSaldo();
                carregarTabela();
                new DAO().salvar(contaCorrente);
            } else if (origem == btnGravar) {
                gravarCheques();
            } else if (origem == itemMenuApagarSelecionados) {
                apagarItensSelecionados();
            }
        }

        @Override
        public void configurar() {
            btnConta.addActionListener(this);
            btnDocumento.addActionListener(this);
            btnABN.addActionListener(this);
            btnBK.addActionListener(this);
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
        btnBK = new javax.swing.JButton();
        btnABN = new javax.swing.JButton();
        btnFixarHistórico = new javax.swing.JToggleButton();
        painelCheque = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaCheque = new javax.swing.JTable();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        jMenuItem2.setText("jMenuItem2");
        popupMenu.add(jMenuItem2);

        jMenuItem3.setText("jMenuItem3");
        popupMenu.add(jMenuItem3);

        setClosable(true);
        setMaximizable(true);
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

        jLabel1.setText("Data do Lançamento:");

        txtData.setFocusable(false);

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
                        .addComponent(txtData, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
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

        btnBK.setText("BK");

        btnABN.setText("ABN");

        btnFixarHistórico.setText("Fixar Histórico");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFixarHistórico)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBK)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnABN)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnABN)
                    .addComponent(btnBK)
                    .addComponent(cbFornecedores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFixarHistórico))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaCheque.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Data", "Cheque", "Descrição", "Valor"
            }
        ));
        jScrollPane2.setViewportView(tabelaCheque);

        javax.swing.GroupLayout painelChequeLayout = new javax.swing.GroupLayout(painelCheque);
        painelCheque.setLayout(painelChequeLayout);
        painelChequeLayout.setHorizontalGroup(
            painelChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 883, Short.MAX_VALUE)
        );
        painelChequeLayout.setVerticalGroup(
            painelChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(painelCheque, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 883, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnABN;
    private javax.swing.JButton btnBK;
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnDocumento;
    private javax.swing.JToggleButton btnFixarHistórico;
    private javax.swing.JButton btnGravar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnPesquisar;
    private javax.swing.JComboBox cbFornecedores;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel painelCheque;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaCheque;
    private javax.swing.JTable tabelaContaCorrente;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroDocumento;
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

