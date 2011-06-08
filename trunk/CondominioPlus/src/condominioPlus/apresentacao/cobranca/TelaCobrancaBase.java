/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaCobrancaBase.java
 *
 * Created on 09/05/2011, 13:39:43
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.apresentacao.financeiro.DialogoConta;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Configuracao;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.cobranca.CobrancaBase;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;

/**
 *
 * @author eugenia
 */
public class TelaCobrancaBase extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TabelaModelo_2<CobrancaBase> modeloTabela;
    private CobrancaBase cobranca;
    private List<CobrancaBase> listaCobrancas;
    private Conta conta;
    private Configuracao configuracao = NegocioUtil.getConfiguracao();

    /** Creates new form TelaCobrancaBase */
    public TelaCobrancaBase(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        new ControladorEventos();

        carregarTabela();
        preencherPainelDados(new CobrancaBase());
        preencherPainelJuros();

        if (condominio != null) {
            this.setTitle("Cobrança Base - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<CobrancaBase>(tabelaCobranca, "Conta, Descrição, Valor, Dividir Fração Ideal?".split(",")) {

            @Override
            protected List<CobrancaBase> getCarregarObjetos() {
                return getCobrancas();
            }

            @Override
            public Object getValor(CobrancaBase cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getConta().getCodigo();
                    case 1:
                        return cobranca.getConta().getNome();
                    case 2:
                        return PagamentoUtil.formatarMoeda(cobranca.getValor().doubleValue());
                    case 3:
                        return cobranca.isDividirFracaoIdeal() ? "Sim" : "Não";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaCobranca.getColumn(modeloTabela.getCampo(0)).setMaxWidth(80);
        tabelaCobranca.getColumn(modeloTabela.getCampo(1)).setMinWidth(150);
        tabelaCobranca.getColumn(modeloTabela.getCampo(2)).setCellRenderer(direito);
        tabelaCobranca.getColumn(modeloTabela.getCampo(3)).setCellRenderer(centralizado);
    }

    private List<CobrancaBase> getCobrancas() {
        listaCobrancas = condominio.getCobrancasBase();

        return listaCobrancas;
    }

    private void preencherPainelDados(CobrancaBase c) {
        cobranca = c;
        if (c.getConta() != null) {
            conta = c.getConta();
        }
        if (conta != null && c.getCodigo() != 0) {
            txtConta.setText(String.valueOf(c.getConta().getCodigo()));
            txtHistorico.setText(c.getConta().getNome());
            txtValor.setText(c.getValor().toString().replace(".", ","));
            checkDividirFracaoIdeal.setSelected(c.isDividirFracaoIdeal());
        } else {
            limparCampos();
        }
    }

    private void preencherObjeto() {
        if (cobranca == null) {
            cobranca = new CobrancaBase();
        }

        cobranca.setConta(conta);
        cobranca.setCondominio(condominio);
        cobranca.setDividirFracaoIdeal(checkDividirFracaoIdeal.isSelected());
        cobranca.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));

        if (verificarListaCobranca()) {
            ApresentacaoUtil.exibirAdvertencia("Já existe uma cobrança com essas características.", this);
            limparCampos();
            return;
        } else {
            if (cobranca.getCodigo() == 0) {
                condominio.getCobrancasBase().add(cobranca);
            }
            new DAO().salvar(condominio);
            limparCampos();
        }
    }

    private boolean verificarListaCobranca() {
        System.out.println("lista cobranca " + condominio.getCobrancasBase());
        for (CobrancaBase cobrancaBase : condominio.getCobrancasBase()) {
            System.out.println("cobranca " + cobranca);
            if (cobranca != null && cobranca.getCodigo() != cobrancaBase.getCodigo()) {
                System.out.println("hereeeeeee");
                if (cobrancaBase.getConta().getCodigo() == cobranca.getConta().getCodigo()) {
                    return true;
                }
            }
        }
        return false;
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


            String descricao = "Cobrança base adicionada " + cobranca.getConta().getNome() + " .";
            FuncionarioUtil.registrar(tipo, descricao);
            cobranca = null;

        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void remover() {
        if (!ApresentacaoUtil.perguntar("Desejar remover a(s) cobrança(s)?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<CobrancaBase> itensRemover = modeloTabela.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (CobrancaBase c : itensRemover) {
                    modeloTabela.remover(c);
                    new DAO().remover(c);
                }
            }
            condominio.getCobrancasBase().removeAll(itensRemover);
            new DAO().salvar(condominio);
            limparCampos();
            cobranca = null;

            System.out.println("Cobranças " + condominio.getCobrancasBase().size());

            ApresentacaoUtil.exibirInformacao("Cobrança(s) removida(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void limparCampos() {
        conta = null;
        txtConta.setText("");
        txtHistorico.setText("");
        txtValor.setText("");
        txtValor.grabFocus();
        checkDividirFracaoIdeal.setSelected(false);
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtConta);
        campos.add(txtHistorico);
        campos.add(txtValor);

        return campos;

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false, "");
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

    private void preencherPainelJuros() {
        txtJuros.setText(String.valueOf(configuracao.getPercentualJuros()));
        txtMulta.setText(String.valueOf(configuracao.getPercentualMulta()));
        txtDesconto.setText(PagamentoUtil.formatarMoeda(condominio.getDesconto().doubleValue()));
        chkCalcularMultaProximoMes.setSelected(condominio.isCalcularMultaProximoMes());
    }

    private void salvarPercentualJuros() {
        configuracao.setPercentualJuros(new BigDecimal(txtJuros.getText().replace(",", ".")));
        configuracao.setPercentualMulta(new BigDecimal(txtMulta.getText().replace(",", ".")));
        new DAO().salvar(configuracao);
        condominio.setDesconto(new BigDecimal(txtDesconto.getText().replace(",", ".")));
        condominio.setCalcularMultaProximoMes(chkCalcularMultaProximoMes.isSelected());
        new DAO().salvar(condominio);
        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso", this);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void configurar() {
            tabelaCobranca.addMouseListener(this);
            itemMenuAdicionar.addActionListener(this);
            itemMenuEditar.addActionListener(this);
            itemMenuRemover.addActionListener(this);
            btnConta.addActionListener(this);
            btnCancelar.addActionListener(this);
            txtConta.addFocusListener(this);
            btnSalvar.addActionListener(this);
            btnSalvarJuros.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == itemMenuAdicionar) {
                preencherPainelDados(new CobrancaBase());
            } else if (origem == itemMenuEditar) {
                preencherPainelDados(modeloTabela.getObjetoSelecionado());
            } else if (origem == btnCancelar) {
                limparCampos();
                cobranca = null;
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnSalvar) {
                salvar();
                carregarTabela();
            } else if (origem == itemMenuRemover) {
                remover();
            } else if (origem == btnSalvarJuros) {
                salvarPercentualJuros();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabelaCobranca) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaCobrancaBase.this);
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

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuAdicionar = new javax.swing.JMenuItem();
        itemMenuEditar = new javax.swing.JMenuItem();
        itemMenuRemover = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        painelTabela = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaCobranca = new javax.swing.JTable();
        painelDados = new javax.swing.JPanel();
        txtValor = new javax.swing.JTextField();
        txtConta = new javax.swing.JTextField();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnConta = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnCancelar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        checkDividirFracaoIdeal = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtJuros = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtMulta = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDesconto = new javax.swing.JTextField();
        btnSalvarJuros = new javax.swing.JButton();
        chkCalcularMultaProximoMes = new javax.swing.JCheckBox();

        itemMenuAdicionar.setText("Adicionar Cobrança");
        popupMenu.add(itemMenuAdicionar);

        itemMenuEditar.setText("Editar Cobrança");
        popupMenu.add(itemMenuEditar);

        itemMenuRemover.setText("Remover Selecionados");
        popupMenu.add(itemMenuRemover);

        setClosable(true);
        setTitle("Cobrança Base");
        setToolTipText("");

        painelTabela.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tabelaCobranca.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabelaCobranca);

        javax.swing.GroupLayout painelTabelaLayout = new javax.swing.GroupLayout(painelTabela);
        painelTabela.setLayout(painelTabelaLayout);
        painelTabelaLayout.setHorizontalGroup(
            painelTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTabelaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelTabelaLayout.setVerticalGroup(
            painelTabelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelTabelaLayout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        painelDados.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtValor.setName("Valor"); // NOI18N

        txtConta.setName("Conta"); // NOI18N

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Descrição da Conta:");

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        jLabel3.setText("Valor:");

        btnCancelar.setText("Cancelar");

        btnSalvar.setText("Salvar");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(203, Short.MAX_VALUE)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addGap(175, 175, 175))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnSalvar))
                .addContainerGap())
        );

        checkDividirFracaoIdeal.setText("Calcular Fração Ideal?");

        javax.swing.GroupLayout painelDadosLayout = new javax.swing.GroupLayout(painelDados);
        painelDados.setLayout(painelDadosLayout);
        painelDadosLayout.setHorizontalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(painelDadosLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(55, 55, 55)
                        .addComponent(btnConta)
                        .addGap(32, 32, 32)
                        .addComponent(jLabel2))
                    .addGroup(painelDadosLayout.createSequentialGroup()
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkDividirFracaoIdeal))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelDadosLayout.setVerticalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(btnConta, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkDividirFracaoIdeal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelTabela, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelTabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cobranças Base", jPanel2);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("% Juros");

        txtJuros.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("% Multa");

        txtMulta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Desconto  R$");

        txtDesconto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        btnSalvarJuros.setText("Salvar");

        chkCalcularMultaProximoMes.setText("Calcular Juros/Multa Próximo Mês?");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                            .addComponent(jLabel1))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtMulta, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                            .addComponent(txtDesconto, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                            .addComponent(txtJuros, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                        .addGap(13, 13, 13))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(chkCalcularMultaProximoMes)))
                .addGap(61, 61, 61))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(125, Short.MAX_VALUE)
                .addComponent(btnSalvarJuros)
                .addGap(121, 121, 121))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(chkCalcularMultaProximoMes)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtJuros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMulta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(29, 29, 29)
                .addComponent(btnSalvarJuros)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(133, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(127, 127, 127))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(68, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64))
        );

        jTabbedPane1.addTab("Dados Cobrança", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 574, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnSalvarJuros;
    private javax.swing.JCheckBox checkDividirFracaoIdeal;
    private javax.swing.JCheckBox chkCalcularMultaProximoMes;
    private javax.swing.JMenuItem itemMenuAdicionar;
    private javax.swing.JMenuItem itemMenuEditar;
    private javax.swing.JMenuItem itemMenuRemover;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelDados;
    private javax.swing.JPanel painelTabela;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaCobranca;
    private javax.swing.JTextField txtConta;
    private javax.swing.JTextField txtDesconto;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtJuros;
    private javax.swing.JTextField txtMulta;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
