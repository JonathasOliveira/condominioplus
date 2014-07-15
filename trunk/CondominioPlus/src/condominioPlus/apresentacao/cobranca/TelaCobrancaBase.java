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

import condominioPlus.Main;
import condominioPlus.apresentacao.DialogoAnotacao;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.apresentacao.financeiro.DialogoConta;
import condominioPlus.negocio.Anotacao;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Configuracao;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.cobranca.CobrancaBase;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.util.Relatorios;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.usuario.Usuario;
import logicpoint.util.DataUtil;

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
    private TabelaModelo_2<Anotacao> modeloTabelaAnotacoes;
    private List<Anotacao> listaAnotacoes = new ArrayList<Anotacao>();

    /** Creates new form TelaCobrancaBase */
    public TelaCobrancaBase(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        new ControladorEventos();

        definirMinimoSpinner();

        carregarTabela();
        preencherPainelDados(new CobrancaBase());
        preencherPainelConfiguracoes();
        carregarTabelaAnotacoes();
        verificarDesconto();

        if (condominio != null) {
            this.setTitle("Cobrança Base - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<CobrancaBase>(tabelaCobranca, "Conta, Descrição, Valor, Fração Ideal?, Desconto?, Até dia, Valor c/ Desc.".split(",")) {

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
                    case 4:
                        return cobranca.isConcederDesconto() ? "Sim" : "Não";
                    case 5:
                        return cobranca.getDescontoAte() != null ? DataUtil.toString(cobranca.getDescontoAte()) : "";
                    case 6:
                        return cobranca.getValorComDesconto() != null ? PagamentoUtil.formatarMoeda(cobranca.getValorComDesconto().doubleValue()) : "";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaCobranca.getColumn(modeloTabela.getCampo(0)).setMaxWidth(45);
        tabelaCobranca.getColumn(modeloTabela.getCampo(1)).setMinWidth(170);
        tabelaCobranca.getColumn(modeloTabela.getCampo(2)).setMaxWidth(50);
        tabelaCobranca.getColumn(modeloTabela.getCampo(3)).setMinWidth(65);
        tabelaCobranca.getColumn(modeloTabela.getCampo(4)).setMaxWidth(70);
        tabelaCobranca.getColumn(modeloTabela.getCampo(5)).setMaxWidth(55);
        tabelaCobranca.getColumn(modeloTabela.getCampo(6)).setMinWidth(65);
        tabelaCobranca.getColumn(modeloTabela.getCampo(2)).setCellRenderer(direito);
        tabelaCobranca.getColumn(modeloTabela.getCampo(3)).setCellRenderer(centralizado);
        tabelaCobranca.getColumn(modeloTabela.getCampo(4)).setCellRenderer(centralizado);
        tabelaCobranca.getColumn(modeloTabela.getCampo(5)).setCellRenderer(centralizado);
        tabelaCobranca.getColumn(modeloTabela.getCampo(6)).setCellRenderer(direito);
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
            checkDesconto.setSelected(c.isConcederDesconto());
            verificarDesconto();
            if (c.getDescontoAte() != null) {
                txtDataDesconto.setValue(DataUtil.getDate(c.getDescontoAte()));
            }
            if (c.getValorComDesconto() != null) {
                txtValorDesconto.setText(c.getValorComDesconto().toString().replace(".", ","));
            }
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
        
        if (checkDesconto.isSelected()){
            cobranca.setConcederDesconto(true);
            cobranca.setDescontoAte(DataUtil.getCalendar(txtDataDesconto.getValue()));
            cobranca.setValorComDesconto(new BigDecimal(txtValorDesconto.getText().replace(",", ".")));
        } else {
            cobranca.setConcederDesconto(false);
            cobranca.setDescontoAte(null);
            cobranca.setValorComDesconto(null);
        }

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
        checkDesconto.setSelected(false);
        verificarDesconto();
        txtValorDesconto.setText("");
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtConta);
        campos.add(txtHistorico);
        campos.add(txtValor);
        
        if (checkDesconto.isSelected()){
            campos.add(txtValorDesconto);
        }

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

    private void preencherPainelConfiguracoes() {
//        txtJuros.setText(String.valueOf(configuracao.getPercentualJuros()));
//        txtMulta.setText(String.valueOf(configuracao.getPercentualMulta()));
        txtDesconto.setText(PagamentoUtil.formatarMoeda(condominio.getDesconto().doubleValue()));
        chkCalcularMultaProximoMes.setSelected(condominio.isCalcularMultaProximoMes());
        if (condominio.getNumeroMinimoParcelasAcordo() < 1) {
            spnNumeroMinimoInamplencia.setValue(1);
        } else {
            spnNumeroMinimoInamplencia.setValue(condominio.getNumeroMinimoParcelasAcordo());
        }

    }

    private void salvarPercentualJuros() {
//        configuracao.setPercentualJuros(new BigDecimal(txtJuros.getText().replace(",", ".")));
//        configuracao.setPercentualMulta(new BigDecimal(txtMulta.getText().replace(",", ".")));
//        new DAO().salvar(configuracao);
        condominio.setDesconto(new BigDecimal(txtDesconto.getText().replace(",", ".")));
        condominio.setCalcularMultaProximoMes(chkCalcularMultaProximoMes.isSelected());
        condominio.setNumeroMinimoParcelasAcordo((Integer) spnNumeroMinimoInamplencia.getValue());
        new DAO().salvar(condominio);
        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso", this);
    }

    private void definirMinimoSpinner() {
        SpinnerNumberModel nm = new SpinnerNumberModel();
        nm.setMinimum(1);
        spnNumeroMinimoInamplencia.setModel(nm);
    }

    private void carregarTabelaAnotacoes() {
        modeloTabelaAnotacoes = new TabelaModelo_2<Anotacao>(tabelaAnotacoes, "Assunto, Data, Texto, Usuario".split(",")) {

            @Override
            protected List<Anotacao> getCarregarObjetos() {
                return getAnotacoes();
            }

            @Override
            public Object getValor(Anotacao anotacao, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return anotacao.getAssunto();
                    case 1:
                        return DataUtil.getDateTime(anotacao.getData());
                    case 2:
                        return anotacao.getTexto();
                    case 3:
                        return anotacao.getUsuario().getUsuario();
                    default:
                        return null;
                }
            }
        };
    }

    private List<Anotacao> getAnotacoes() {
        listaAnotacoes.clear();
        for (Anotacao a : condominio.getAnotacoes()) {
            if (a.isCobranca()) {
                listaAnotacoes.add(a);
            }
        }
//        listaAnotacoes = new DAO().listar(Anotacao.class, "AnotacoesCondominio", condominio, true);

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Anotacao a1 = (Anotacao) o1;
                Anotacao a2 = (Anotacao) o2;
                return a1.getData().compareTo(a2.getData());
            }
        };

        Collections.sort(listaAnotacoes, c);

        return listaAnotacoes;
    }

    private void adicionarAnotacao() {
        Anotacao anotacao = DialogoAnotacao.getAnotacao(new Anotacao(condominio), TelaPrincipal.getInstancia(), true);
        if (anotacao.getTexto().equals("")) {
            return;
        }

        if (Main.getFuncionario().getUsuario().getUsuario().equals("")) {
            Usuario usuario = new DAO().localizar(Usuario.class, 50452);
            anotacao.setUsuario(usuario);
        } else {
            anotacao.setUsuario(Main.getFuncionario().getUsuario());
        }

        condominio.adicionarAnotacao(anotacao, true);
        carregarTabelaAnotacoes();
    }

    private void editarAnotacao() {
        Anotacao anotacao = modeloTabelaAnotacoes.getObjetoSelecionado();
        if (anotacao == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione a anotação a ser editada!", this);
            return;
        }
        DialogoAnotacao.getAnotacao(anotacao, TelaPrincipal.getInstancia(), true);
        carregarTabelaAnotacoes();
    }

    private void removerAnotacao() {
        if (modeloTabelaAnotacoes.getLinhaSelecionada() > -1) {
            if (!ApresentacaoUtil.perguntar("Desejar remover o(s) registro(s)?", this)) {
                return;
            }
            System.out.println("removendo... " + modeloTabelaAnotacoes.getLinhasSelecionadas());
            List<Anotacao> itensRemover = modeloTabelaAnotacoes.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Anotacao a : itensRemover) {
                    modeloTabelaAnotacoes.remover(a);
                    //lista auxiliar para não dar erro ao remover o registro da lista de anotações do condominio
                    List<Anotacao> listaAuxiliar = new ArrayList<Anotacao>();
                    for (Anotacao anotacao : condominio.getAnotacoes()) {
                        listaAuxiliar.add(anotacao);
                    }
                    //fim lista auxiliar para não dar erro ao remover o registro da lista de anotações do condominio
                    for (Anotacao o : listaAuxiliar) {
                        if (a.getCodigo() == o.getCodigo()) {
                            condominio.getAnotacoes().remove(a);
                        }
                    }
                    new DAO().remover(a);
                }
            }
            ApresentacaoUtil.exibirInformacao("Anotação(ões) removida(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void imprimirAnotacoes() {
        if (modeloTabelaAnotacoes.getObjetosSelecionados().isEmpty()) {
            List<Anotacao> lista = new DAO().listar(Anotacao.class, "AnotacoesCondominio", condominio, true);
            if (lista.isEmpty()) {
                ApresentacaoUtil.exibirAdvertencia("Não há registros a serem impressos.", this);
            } else {
                new Relatorios().imprimirAnotacoes(condominio, null, lista, TipoRelatorio.ANOTACOES_COBRANCA_BASE);
            }
        } else {
            new Relatorios().imprimirAnotacoes(condominio, null, modeloTabelaAnotacoes.getObjetosSelecionados(), TipoRelatorio.ANOTACOES_COBRANCA_BASE);
        }
    }

    private void salvarAnotacoes() {
        new DAO().salvar(condominio);
    }

    private void verificarDesconto() {
        if (checkDesconto.isSelected()) {
            txtDataDesconto.setEnabled(true);
            txtValorDesconto.setEnabled(true);
            txtValorDesconto.setBackground(Color.WHITE);
        } else {
            txtDataDesconto.setEnabled(false);
            txtValorDesconto.setEnabled(false);
            txtValorDesconto.setBackground(Color.LIGHT_GRAY);
        }
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
            btnAdicionarAnotacao.addActionListener(this);
            btnEditarAnotacao.addActionListener(this);
            btnRemoverAnotacao.addActionListener(this);
            btnImprimirAnotacoes.addActionListener(this);
            btnSalvarAnotacoes.addActionListener(this);
            checkDesconto.addActionListener(this);
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
            } else if (e.getSource() == btnAdicionarAnotacao) {
                adicionarAnotacao();
            } else if (e.getSource() == btnEditarAnotacao) {
                editarAnotacao();
            } else if (e.getSource() == btnRemoverAnotacao) {
                removerAnotacao();
            } else if (e.getSource() == btnImprimirAnotacoes) {
                imprimirAnotacoes();
            } else if (e.getSource() == btnSalvarAnotacoes) {
                salvarAnotacoes();
            } else if (e.getSource() == checkDesconto) {
                verificarDesconto();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabelaCobranca) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == tabelaCobranca && e.getClickCount() == 2) {
                preencherPainelDados(modeloTabela.getObjetoSelecionado());
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
        painelDesconto = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtDataDesconto = new net.sf.nachocalendar.components.DateField();
        checkDesconto = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        txtValorDesconto = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtDesconto = new javax.swing.JTextField();
        chkCalcularMultaProximoMes = new javax.swing.JCheckBox();
        spnNumeroMinimoInamplencia = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        btnSalvarJuros = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabelaAnotacoes = new javax.swing.JTable();
        btnAdicionarAnotacao = new javax.swing.JButton();
        btnEditarAnotacao = new javax.swing.JButton();
        btnRemoverAnotacao = new javax.swing.JButton();
        btnImprimirAnotacoes = new javax.swing.JButton();
        btnSalvarAnotacoes = new javax.swing.JButton();

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

        tabelaCobranca.setFont(new java.awt.Font("Tahoma", 0, 9));
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        jLabel7.setText("Até:");

        checkDesconto.setText("Conceder desconto?");

        jLabel4.setText("Valor:");

        txtValorDesconto.setName("Valor Desconto"); // NOI18N

        javax.swing.GroupLayout painelDescontoLayout = new javax.swing.GroupLayout(painelDesconto);
        painelDesconto.setLayout(painelDescontoLayout);
        painelDescontoLayout.setHorizontalGroup(
            painelDescontoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDescontoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkDesconto)
                .addGap(7, 7, 7)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValorDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(44, Short.MAX_VALUE))
        );
        painelDescontoLayout.setVerticalGroup(
            painelDescontoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDescontoLayout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .addGroup(painelDescontoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDescontoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(txtValorDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtDataDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelDescontoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(checkDesconto)
                        .addComponent(jLabel7)))
                .addContainerGap())
        );

        javax.swing.GroupLayout painelDadosLayout = new javax.swing.GroupLayout(painelDados);
        painelDados.setLayout(painelDadosLayout);
        painelDadosLayout.setHorizontalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(painelDadosLayout.createSequentialGroup()
                        .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkDividirFracaoIdeal)))
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDadosLayout.createSequentialGroup()
                        .addComponent(painelDesconto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))))
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
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(painelTabela, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelDados, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelTabela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cobranças Base", jPanel2);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Desconto  R$");

        txtDesconto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        chkCalcularMultaProximoMes.setText("Calcular Juros/Multa Próximo Mês?");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Mínimo Parcelas Inadimplentes para um Acordo");

        btnSalvarJuros.setText("Salvar");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chkCalcularMultaProximoMes)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)))
                        .addGap(28, 28, 28)
                        .addComponent(txtDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGap(252, 252, 252)
                        .addComponent(spnNumeroMinimoInamplencia, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                .addGap(201, 201, 201))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(264, Short.MAX_VALUE)
                .addComponent(btnSalvarJuros)
                .addGap(266, 266, 266))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(chkCalcularMultaProximoMes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDesconto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnNumeroMinimoInamplencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                .addGap(48, 48, 48)
                .addComponent(btnSalvarJuros)
                .addGap(103, 103, 103))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Dados Cobrança", jPanel3);

        tabelaAnotacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(tabelaAnotacoes);

        btnAdicionarAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarAnotacao.setToolTipText("Adicionar Anotação");
        btnAdicionarAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarAnotacao.setToolTipText("Editar Anotação");
        btnEditarAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverAnotacao.setToolTipText("Remover Anotação");
        btnRemoverAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnImprimirAnotacoes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimirAnotacoes.setToolTipText("Imprimir Anotação(ões)");

        btnSalvarAnotacoes.setText("Salvar Alterações");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEditarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnImprimirAnotacoes, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 290, Short.MAX_VALUE)
                        .addComponent(btnSalvarAnotacoes)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSalvarAnotacoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnImprimirAnotacoes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Anotações", jPanel5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionarAnotacao;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnEditarAnotacao;
    private javax.swing.JButton btnImprimirAnotacoes;
    private javax.swing.JButton btnRemoverAnotacao;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnSalvarAnotacoes;
    private javax.swing.JButton btnSalvarJuros;
    private javax.swing.JCheckBox checkDesconto;
    private javax.swing.JCheckBox checkDividirFracaoIdeal;
    private javax.swing.JCheckBox chkCalcularMultaProximoMes;
    private javax.swing.JMenuItem itemMenuAdicionar;
    private javax.swing.JMenuItem itemMenuEditar;
    private javax.swing.JMenuItem itemMenuRemover;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelDados;
    private javax.swing.JPanel painelDesconto;
    private javax.swing.JPanel painelTabela;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JSpinner spnNumeroMinimoInamplencia;
    private javax.swing.JTable tabelaAnotacoes;
    private javax.swing.JTable tabelaCobranca;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataDesconto;
    private javax.swing.JTextField txtDesconto;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtValor;
    private javax.swing.JTextField txtValorDesconto;
    // End of variables declaration//GEN-END:variables
}
