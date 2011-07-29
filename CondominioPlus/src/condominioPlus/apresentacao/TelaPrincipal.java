/*
 * TelaPrincipal.java
 *
 * Created on 7 de Agosto de 2007, 09:33
 */
/*
 * TelaPrincipal.java
 *
 * Created on 7 de Agosto de 2007, 09:33
 */
package condominioPlus.apresentacao;

import condominioPlus.apresentacao.cobranca.TelaCobrancaBase;
import condominioPlus.Main;
import condominioPlus.apresentacao.advogado.TelaAdvogado;
import condominioPlus.apresentacao.cobranca.TelaLancamentos;
import condominioPlus.apresentacao.cobranca.TelaTaxaExtra;
import condominioPlus.apresentacao.cobranca.agua.TelaAgua;
import condominioPlus.apresentacao.cobranca.gas.TelaGas;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import condominioPlus.apresentacao.funcionario.TelaControleAcesso;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import condominioPlus.apresentacao.condominio.TelaCondominio;
import condominioPlus.apresentacao.condominio.TelaDadosCondominio;
import condominioPlus.apresentacao.condominio.TelaSelecionarCondominio;
import condominioPlus.apresentacao.condomino.TelaCondomino;
import condominioPlus.apresentacao.financeiro.TelaAplicacaoFinanceira;
import condominioPlus.apresentacao.financeiro.TelaConsignacao;
import condominioPlus.apresentacao.financeiro.TelaConta;
import condominioPlus.apresentacao.financeiro.TelaContaCorrente;
import condominioPlus.apresentacao.financeiro.TelaContaPagar;
import condominioPlus.apresentacao.financeiro.TelaContasIndispensaveis;
import condominioPlus.apresentacao.financeiro.TelaEmprestimo;
import condominioPlus.apresentacao.financeiro.TelaExtratoBancario;
import condominioPlus.apresentacao.financeiro.TelaIdentificadores;
import condominioPlus.apresentacao.financeiro.TelaOrcamento;
import condominioPlus.apresentacao.financeiro.TelaPoupanca;
import condominioPlus.apresentacao.fornecedor.TelaFornecedor;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.funcionario.CaracteristicaAcesso;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.Identificavel;
import logicpoint.apresentacao.NotificavelAtalho;
import logicpoint.apresentacao.NotificavelClasse;
import logicpoint.exception.TratadorExcecao;
import logicpoint.util.DataUtil;
import logicpoint.util.Util;
import org.joda.time.DateTime;

/**
 *
 *
 * @author  USUARIO
 */
public class TelaPrincipal extends javax.swing.JFrame implements NotificavelAtalho {

    private static TelaPrincipal telaPrincipalInstancia;
    private PainelTelaPrincipal painel;
    private List<Window> janelas = new ArrayList<Window>();
    private ListenerJanela listenerJanela = new ListenerJanela();

    /** Creates new form TelaPrincipal */
    public TelaPrincipal() {
        telaPrincipalInstancia = this;

        initComponents();

        carregarKeyListenerUniversal();

        new ControladorEventos();

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.setTitle(Main.getFuncionario() + " - " + this.getTitle());

        atualizarAcesso();
        preencherTela();

        // criarFrame(new TelaSelecionarCondominio());
    }

    public void notificarClasse(final Object... objetos) {
        new Thread(new Runnable() {

            public void run() {
                for (Object o : objetos) {
                    for (int i = 0; i < janelas.size(); i++) {
                        Window janela = janelas.get(i);
                        if (janela instanceof NotificavelClasse) {
                            try {
                                ((NotificavelClasse) janela).notificarClasse(o);
                            } catch (ClassCastException ex) {
                            }
                        }
                    }

                    JInternalFrame[] frames;
                    try {
                        frames = desktop.getAllFrames();
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        frames = desktop.getAllFrames();
                    }
                    for (int i = 0; i < frames.length; i++) {
                        if (frames[i] instanceof NotificavelClasse) {
                            try {
                                ((NotificavelClasse) frames[i]).notificarClasse(o);
                            } catch (ClassCastException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void notificarTecla(int tecla) {
        for (int i = 0; i < janelas.size(); i++) {
            Window janela = janelas.get(i);
            if (janela.isActive() && janela instanceof NotificavelAtalho) {
                ((NotificavelAtalho) janela).notificarTecla(tecla);
                return;
            }
        }
        if (desktop.getSelectedFrame() != null && desktop.getSelectedFrame() instanceof NotificavelAtalho) {
            ((NotificavelAtalho) desktop.getSelectedFrame()).notificarTecla(tecla);
        }
    }

    private void carregarKeyListenerUniversal() {
        //ApresentacaoUtil.telasSemFechamentoPorEsc.add(TelaVenda.class);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(ApresentacaoUtil.fechadorEsc);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(ApresentacaoUtil.notificadorAtalho.setNotificavel(this));
    }

    private void preencherTela() {
        //this.setIconImage(Recursos.getImagem("icone_commerce").getImage());
        preencherPainel();
        preencherUsuario();
        preencherData();
    }

    public void preencherPainel() {
        if (painel == null) {
            painel = new PainelTelaPrincipal();
            desktop.add(painel);
        }
        painel.preencher();
    }

    public void recarregarFuncionario() {
        Main.recarregarFuncionario();
        atualizarAcesso();
    }

    private void atualizarAcesso() {
        btnCondominio.setEnabled(Main.permite(CaracteristicaAcesso.VENDA));
        btnCondominos.setEnabled(Main.permite(CaracteristicaAcesso.PRODUTO_VISUALIZAR));
        btnCliente.setEnabled(Main.permite(CaracteristicaAcesso.CLIENTE_CADASTRO));

        menuItemCondominios.setEnabled(Main.permite(CaracteristicaAcesso.VENDA));
        menuItemCondominos.setEnabled(Main.permite(CaracteristicaAcesso.VENDA_HISTORICO));
        menuItemLancamentos.setEnabled(Main.permite(CaracteristicaAcesso.ENTREGA_PREVISTA));
        menuItemFuncionario.setEnabled(Main.permite(CaracteristicaAcesso.FUNCIONARIO_CADASTRO));
        menuItemCategoriaFuncionario.setEnabled(Main.permite(CaracteristicaAcesso.FUNCIONARIO_CATEGORIA_CADASTRO));
        menuItemControleAcesso.setEnabled(Main.permite(CaracteristicaAcesso.CONTROLE_DE_ACESSO));
        menuRelatorio.setEnabled(Main.permite(CaracteristicaAcesso.RELATORIO));
        menuItemContaCorrente.setEnabled(Main.permite(CaracteristicaAcesso.PLANO_DE_CONTAS));
        menuItemExtratoBancario.setEnabled(Main.permite(CaracteristicaAcesso.PAGAMENTO));
        menuItemComissao.setEnabled(Main.permite(CaracteristicaAcesso.COMISSAO));
        menuItemBanco.setEnabled(Main.permite(CaracteristicaAcesso.BANCO));
        menuItemConfiguracao.setEnabled(Main.permite(CaracteristicaAcesso.CONFIGURACAO));
        menuItemIntegracao.setEnabled(Main.permite(CaracteristicaAcesso.INTEGRACAO));
    }

    @Override
    public void dispose() {
        int resposta = ApresentacaoUtil.perguntar("Qual operação deseja efetuar?", new String[]{"Fechar", "Logoff", "Cancelar"}, this);

        switch (resposta) {
            case ApresentacaoUtil.RESPOSTA1:
                fechar();
                Main.fechar();
                break;
            case ApresentacaoUtil.RESPOSTA2:
                logoff();
                break;
            case ApresentacaoUtil.RESPOSTA3:
                return;
        }
    }

    public void fechar() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(ApresentacaoUtil.fechadorEsc);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(ApresentacaoUtil.notificadorAtalho);
        super.dispose();
    }

    private void preencherUsuario() {
        lblUsuario.setText("Usuário: " + Main.getFuncionario().getNome().split(" ")[0]);
    }

    public static void preencherCondominio(Condominio c) {
        lblCondominio.setText(c.getRazaoSocial());
        lblCondominio.setVisible(true);
    }

    public static TelaPrincipal getInstancia() {
        return telaPrincipalInstancia;
    }

    public void criarJanela(Window janela) {
        janela.addWindowListener(listenerJanela);
        janelas.add(janela);
        janela.setVisible(true);
    }

    public void criarFrame(JInternalFrame frame) {
        for (JInternalFrame f : desktop.getAllFrames()) {
            if (f.getClass() == frame.getClass()) {
                if (!(frame instanceof Identificavel) || ((Identificavel) frame).getIdentificacao().equals(((Identificavel) f).getIdentificacao())) {
                    abrirFrame(f);
                    return;
                }
            }
        }

        if (frame instanceof Identificavel) {
            frame.setTitle(frame.getTitle() + " (" + ((Identificavel) frame).getIdentificacao() + ")");
        }
        desktop.add(frame);
        Point posicao = new Point(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - frame.getWidth() / 2, 20);
        frame.setLocation(posicao);
//        frame.setFrameIcon(Recursos.getImagem("icone_commerce"));
        abrirFrame(frame);
    }

    private boolean validarContaCorrente() {
        if (Main.getCondominio().getContaBancaria().getContaCorrente().equals("")) {
            ApresentacaoUtil.exibirAdvertencia("Preencha o condomínio com uma conta corrente!", this);
            return false;
        } else {
            return true;
        }
    }

    private static void abrirFrame(JInternalFrame frame) {
        frame.moveToFront();
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

    private void logoff() {
        fechar();
        Main.setCondominio(null);
        Main.logoff();
    }

    private void preencherData() {
        DateTime hoje = DataUtil.hoje();
        System.out.println("hoje - " + DataUtil.escreverDiaDaSemana(hoje));
        String texto = DataUtil.escreverDiaDaSemana(hoje);
        texto += ", ";
        texto += DataUtil.toString(hoje);
        lblData.setText(texto);
    }

    private class ListenerJanela implements WindowListener {

        public void windowOpened(WindowEvent evento) {
        }

        public void windowClosing(WindowEvent evento) {
        }

        public void windowClosed(WindowEvent evento) {
            janelas.remove(evento.getWindow());
        }

        public void windowIconified(WindowEvent evento) {
        }

        public void windowDeiconified(WindowEvent evento) {
        }

        public void windowActivated(WindowEvent evento) {
        }

        public void windowDeactivated(WindowEvent evento) {
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();

            try {
                if (source == btnCalculadora) {
                    Util.abrirCalculadora();
                } else if (source == btnCondominio) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaDadosCondominio(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Selecione um Condomínio!", null);
                    }
                } else if (source == menuItemCondominios) {
                    criarFrame(new TelaCondominio());
                } else if (source == menuItemCondominos || source == btnCondominos) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaCondomino(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemAdvogados) {
                    criarFrame(new TelaAdvogado());
                } else if (source == menuItemFornecedores) {
                    criarFrame(new TelaFornecedor());
                } else if (source == menuItemLancamentos) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaLancamentos(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemCobrancaBase) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaCobrancaBase(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemTaxaExtra) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaTaxaExtra(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemAgua) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaAgua(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemGas) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaGas(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemControleAcesso) {
                    criarFrame(new TelaControleAcesso());
                } else if (source == menuItemRelatorioCliente) {
//                    Relatorio.imprimirRelatorioCliente();
                } else if (source == menuItemRelatorioFornecedor) {
//                    Relatorio.imprimirRelatorioFornecedor();
                } else if (source == menuItemRelatorioFuncionarios) {
//                    Relatorio.imprimirRelatorioFuncionario();
                } else if (source == menuItemRelatorioProduto) {
//                    TelaPrincipal.getInstancia().criarJanela(new DialogoRelatorioProduto());
                } else if (source == menuItemRelatorioProdutoNegativo) {
//                    TelaPrincipal.getInstancia().criarJanela(new DialogoRelatorioProdutoNegativo());
                } else if (source == menuItemRelatorioEstorno) {
//                    criarFrame(new TelaRelatorioEstorno());
                } else if (source == menuItemContaCorrente) {
                    if (Main.getCondominio() != null) {
                        if (validarContaCorrente()) {
                            criarFrame(new TelaContaCorrente(Main.getCondominio()));
                        }
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemContaPagar) {
                    if (Main.getCondominio() != null) {
                        if (validarContaCorrente()) {
                            criarFrame(new TelaContaPagar(Main.getCondominio()));
                        }
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemExtratoBancario) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaExtratoBancario(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemPoupanca) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaPoupanca(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemAplicacao) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaAplicacaoFinanceira(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemConsignacao) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaConsignacao(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemEmprestimo) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaEmprestimo(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemOrcamento) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaOrcamento(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemContasIndispensaveis) {
                    if (Main.getCondominio() != null) {
                        criarFrame(new TelaContasIndispensaveis(Main.getCondominio()));
                    } else {
                        ApresentacaoUtil.exibirAdvertencia("Você deve selecionar um condomínio!", null);
                    }
                } else if (source == menuItemComissao) {
//                    criarFrame(new TelaComissao());
                } else if (source == menuItemBanco) {
                    criarFrame(new TelaBanco());
                } else if (source == menuItemConfiguracao) {
                    criarFrame(new TelaConfiguracao());
                } else if (source == menuItemIntegracao) {
//                    criarFrame(new TelaIntegracao());
                } else if (source == menuItemSobre) {
                    criarFrame(new TelaSobre());
                } else if (source == btnSelecionarCondominio) {
                    criarFrame(new TelaSelecionarCondominio(desktop));
                } else if (source == menuItemContas) {
                    criarFrame(new TelaConta());
                } else if (source == menuItemIdentificadores) {
                    criarFrame(new TelaIdentificadores());
                }
            } catch (Throwable t) {
                new TratadorExcecao(t, TelaPrincipal.this);
            }

            source = null;
        }

        @Override
        public void configurar() {
            btnCondominio.addActionListener(this);
            btnCondominos.addActionListener(this);
            btnCliente.addActionListener(this);
            btnCalculadora.addActionListener(this);
            btnSelecionarCondominio.addActionListener(this);
            menuItemCondominios.addActionListener(this);
            menuItemAdvogados.addActionListener(this);
            menuItemCondominos.addActionListener(this);
            menuItemLancamentos.addActionListener(this);
            menuItemControleAcesso.addActionListener(this);
            menuItemRelatorioCliente.addActionListener(this);
            menuItemRelatorioFornecedor.addActionListener(this);
            menuItemRelatorioFuncionarios.addActionListener(this);
            menuItemRelatorioProduto.addActionListener(this);
            menuItemRelatorioProdutoNegativo.addActionListener(this);
            menuItemRelatorioEstorno.addActionListener(this);
            menuItemContaCorrente.addActionListener(this);
            menuItemExtratoBancario.addActionListener(this);
            menuItemComissao.addActionListener(this);
            menuItemBanco.addActionListener(this);
            menuItemConfiguracao.addActionListener(this);
            menuItemIntegracao.addActionListener(this);
            menuItemAgua.addActionListener(this);
            menuItemSobre.addActionListener(this);
            menuItemContas.addActionListener(this);
            menuItemIdentificadores.addActionListener(this);
            menuItemFornecedores.addActionListener(this);
            menuItemCobrancaBase.addActionListener(this);
            menuItemTaxaExtra.addActionListener(this);
            menuItemContaPagar.addActionListener(this);
            menuItemPoupanca.addActionListener(this);
            menuItemAplicacao.addActionListener(this);
            menuItemConsignacao.addActionListener(this);
            menuItemEmprestimo.addActionListener(this);
            menuItemOrcamento.addActionListener(this);
            menuItemGas.addActionListener(this);
            menuItemContasIndispensaveis.addActionListener(this);
            menuLogoff.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    logoff();
                }
            });
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        desktop = new javax.swing.JDesktopPane();
        barraFerramentas = new javax.swing.JToolBar();
        painelBarraFerramentas = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnCondominio = new javax.swing.JButton();
        btnCalculadora = new javax.swing.JButton();
        btnCliente = new javax.swing.JButton();
        btnCondominos = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        lblData = new javax.swing.JLabel();
        lblCondominio = new javax.swing.JLabel();
        btnSelecionarCondominio = new javax.swing.JButton();
        menu = new javax.swing.JMenuBar();
        menuCadastro = new javax.swing.JMenu();
        menuItemCondominios = new javax.swing.JMenuItem();
        menuItemCondominos = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        menuItemAdvogados = new javax.swing.JMenuItem();
        menuItemContas = new javax.swing.JMenuItem();
        menuItemIdentificadores = new javax.swing.JMenuItem();
        menuItemFornecedores = new javax.swing.JMenuItem();
        menuCobrancas = new javax.swing.JMenu();
        menuItemLancamentos = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        menuItemAgua = new javax.swing.JMenuItem();
        menuItemCobrancaBase = new javax.swing.JMenuItem();
        menuItemTaxaExtra = new javax.swing.JMenuItem();
        menuItemCalculoMaiorCota = new javax.swing.JMenuItem();
        menuItemGas = new javax.swing.JMenuItem();
        menuFinanceiro = new javax.swing.JMenu();
        menuItemContaCorrente = new javax.swing.JMenuItem();
        menuItemExtratoBancario = new javax.swing.JMenuItem();
        menuItemComissao = new javax.swing.JMenuItem();
        menuItemBanco = new javax.swing.JMenuItem();
        menuItemRemessaSalario = new javax.swing.JMenuItem();
        menuItemPoupanca = new javax.swing.JMenuItem();
        menuItemAplicacao = new javax.swing.JMenuItem();
        menuItemConsignacao = new javax.swing.JMenuItem();
        menuItemEmprestimo = new javax.swing.JMenuItem();
        menuItemFluxoCaixa = new javax.swing.JMenuItem();
        menuItemContaPagar = new javax.swing.JMenuItem();
        menuItemOrcamento = new javax.swing.JMenuItem();
        menuContas = new javax.swing.JMenu();
        menuItemContasDispensaveis = new javax.swing.JMenuItem();
        menuItemContasIndispensaveis = new javax.swing.JMenuItem();
        menuRelatorio = new javax.swing.JMenu();
        menuItemRelatorioCliente = new javax.swing.JMenuItem();
        menuItemRelatorioFornecedor = new javax.swing.JMenuItem();
        menuItemRelatorioFuncionarios = new javax.swing.JMenuItem();
        menuItemRelatorioProduto = new javax.swing.JMenuItem();
        menuItemRelatorioProdutoNegativo = new javax.swing.JMenuItem();
        menuItemRelatorioEstorno = new javax.swing.JMenuItem();
        menuFuncionario = new javax.swing.JMenu();
        menuItemFuncionario = new javax.swing.JMenuItem();
        menuItemCategoriaFuncionario = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        menuItemControleAcesso = new javax.swing.JMenuItem();
        MenuHistorico = new javax.swing.JMenu();
        menuItemCobrancas = new javax.swing.JMenuItem();
        menuConfiguracao = new javax.swing.JMenu();
        menuItemConfiguracao = new javax.swing.JMenuItem();
        menuItemIntegracao = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        menuItemSobre = new javax.swing.JMenuItem();
        menuLogoff = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CondominioPlus");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(600, 400));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        desktop.setBackground(new java.awt.Color(4, 79, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 1);
        getContentPane().add(desktop, gridBagConstraints);

        barraFerramentas.setFloatable(false);
        barraFerramentas.setRollover(true);
        barraFerramentas.setMaximumSize(new java.awt.Dimension(100, 25));
        barraFerramentas.setMinimumSize(new java.awt.Dimension(100, 25));

        painelBarraFerramentas.setMinimumSize(new java.awt.Dimension(800, 40));
        painelBarraFerramentas.setPreferredSize(new java.awt.Dimension(800, 40));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        btnCondominio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/condominiosBarra.png"))); // NOI18N
        btnCondominio.setToolTipText("Dados Condomínio");
        btnCondominio.setFocusPainted(false);
        btnCondominio.setMaximumSize(new java.awt.Dimension(30, 30));
        btnCondominio.setMinimumSize(new java.awt.Dimension(30, 30));
        btnCondominio.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 11, 0);
        jPanel1.add(btnCondominio, gridBagConstraints);

        btnCalculadora.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/calculadora.gif"))); // NOI18N
        btnCalculadora.setToolTipText("Calculadora");
        btnCalculadora.setFocusPainted(false);
        btnCalculadora.setMaximumSize(new java.awt.Dimension(30, 30));
        btnCalculadora.setMinimumSize(new java.awt.Dimension(30, 30));
        btnCalculadora.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 11, 0);
        jPanel1.add(btnCalculadora, gridBagConstraints);

        btnCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/procurar.png"))); // NOI18N
        btnCliente.setToolTipText("Nothing yet");
        btnCliente.setFocusPainted(false);
        btnCliente.setMaximumSize(new java.awt.Dimension(30, 30));
        btnCliente.setMinimumSize(new java.awt.Dimension(30, 30));
        btnCliente.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 11, 168);
        jPanel1.add(btnCliente, gridBagConstraints);

        btnCondominos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/pacientes.gif"))); // NOI18N
        btnCondominos.setToolTipText("Condôminos");
        btnCondominos.setFocusPainted(false);
        btnCondominos.setMaximumSize(new java.awt.Dimension(30, 30));
        btnCondominos.setMinimumSize(new java.awt.Dimension(30, 30));
        btnCondominos.setPreferredSize(new java.awt.Dimension(30, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 11, 0);
        jPanel1.add(btnCondominos, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        lblUsuario.setFont(new java.awt.Font("Comic Sans MS", 0, 12));
        lblUsuario.setText("Usuário: Breca");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 19, 0, 0);
        jPanel2.add(lblUsuario, gridBagConstraints);

        lblData.setFont(new java.awt.Font("Comic Sans MS", 0, 12));
        lblData.setText("segunda feira, 01/01/01");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel2.add(lblData, gridBagConstraints);

        lblCondominio.setFont(new java.awt.Font("Times New Roman", 1, 12));
        lblCondominio.setForeground(new java.awt.Color(255, 51, 51));
        lblCondominio.setText("NENHUM CONDOMÍNIO SELECIONADO!");

        btnSelecionarCondominio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/procurar.png"))); // NOI18N
        btnSelecionarCondominio.setToolTipText("Selecione um Condomínio...");
        btnSelecionarCondominio.setFocusPainted(false);
        btnSelecionarCondominio.setMaximumSize(new java.awt.Dimension(30, 30));
        btnSelecionarCondominio.setMinimumSize(new java.awt.Dimension(30, 30));
        btnSelecionarCondominio.setPreferredSize(new java.awt.Dimension(30, 30));

        javax.swing.GroupLayout painelBarraFerramentasLayout = new javax.swing.GroupLayout(painelBarraFerramentas);
        painelBarraFerramentas.setLayout(painelBarraFerramentasLayout);
        painelBarraFerramentasLayout.setHorizontalGroup(
            painelBarraFerramentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBarraFerramentasLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(159, 159, 159)
                .addComponent(lblCondominio)
                .addGap(4, 4, 4)
                .addComponent(btnSelecionarCondominio, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        painelBarraFerramentasLayout.setVerticalGroup(
            painelBarraFerramentasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(painelBarraFerramentasLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(lblCondominio))
            .addComponent(btnSelecionarCondominio, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        barraFerramentas.add(painelBarraFerramentas);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(barraFerramentas, gridBagConstraints);

        menuCadastro.setText("Cadastro");

        menuItemCondominios.setText("Condomínio");
        menuCadastro.add(menuItemCondominios);

        menuItemCondominos.setText("Condômino");
        menuCadastro.add(menuItemCondominos);
        menuCadastro.add(jSeparator3);

        menuItemAdvogados.setText("Advogados");
        menuCadastro.add(menuItemAdvogados);

        menuItemContas.setText("Contas");
        menuCadastro.add(menuItemContas);

        menuItemIdentificadores.setText("Identificadores");
        menuCadastro.add(menuItemIdentificadores);

        menuItemFornecedores.setText("Fornecedores");
        menuCadastro.add(menuItemFornecedores);

        menu.add(menuCadastro);

        menuCobrancas.setText("Cobranças");

        menuItemLancamentos.setText("Lançamentos/Inadimplência/Pagos");
        menuCobrancas.add(menuItemLancamentos);
        menuCobrancas.add(jSeparator6);

        menuItemAgua.setText("Água");
        menuCobrancas.add(menuItemAgua);

        menuItemCobrancaBase.setText("Cobrança Base");
        menuCobrancas.add(menuItemCobrancaBase);

        menuItemTaxaExtra.setText("Taxa Extra");
        menuCobrancas.add(menuItemTaxaExtra);

        menuItemCalculoMaiorCota.setText("Cálculo maior Cota");
        menuCobrancas.add(menuItemCalculoMaiorCota);

        menuItemGas.setText("Gás");
        menuCobrancas.add(menuItemGas);

        menu.add(menuCobrancas);

        menuFinanceiro.setText("Financeiro");

        menuItemContaCorrente.setText("Conta Corrente");
        menuFinanceiro.add(menuItemContaCorrente);

        menuItemExtratoBancario.setText("Extrato Bancário");
        menuFinanceiro.add(menuItemExtratoBancario);

        menuItemComissao.setText("Arquivo Retorno");
        menuFinanceiro.add(menuItemComissao);

        menuItemBanco.setText("Bancos");
        menuFinanceiro.add(menuItemBanco);

        menuItemRemessaSalario.setText("Remessa Salário");
        menuFinanceiro.add(menuItemRemessaSalario);

        menuItemPoupanca.setText("Poupança");
        menuFinanceiro.add(menuItemPoupanca);

        menuItemAplicacao.setText("Aplicação");
        menuFinanceiro.add(menuItemAplicacao);

        menuItemConsignacao.setText("Consignações");
        menuFinanceiro.add(menuItemConsignacao);

        menuItemEmprestimo.setText("Empréstimo");
        menuFinanceiro.add(menuItemEmprestimo);

        menuItemFluxoCaixa.setText("Fluxo de Caixa");
        menuFinanceiro.add(menuItemFluxoCaixa);

        menuItemContaPagar.setText("Contas a Pagar/Receber");
        menuFinanceiro.add(menuItemContaPagar);

        menuItemOrcamento.setText("Orçamento");
        menuFinanceiro.add(menuItemOrcamento);

        menu.add(menuFinanceiro);

        menuContas.setText("Contas");

        menuItemContasDispensaveis.setText("Contas Dispensáveis");
        menuContas.add(menuItemContasDispensaveis);

        menuItemContasIndispensaveis.setText("Contas Indispensáveis");
        menuContas.add(menuItemContasIndispensaveis);

        menu.add(menuContas);

        menuRelatorio.setText("Relatórios");

        menuItemRelatorioCliente.setText("Clientes");
        menuRelatorio.add(menuItemRelatorioCliente);

        menuItemRelatorioFornecedor.setText("Fornecedores");
        menuRelatorio.add(menuItemRelatorioFornecedor);

        menuItemRelatorioFuncionarios.setText("Funcionários");
        menuRelatorio.add(menuItemRelatorioFuncionarios);

        menuItemRelatorioProduto.setText("Produtos");
        menuRelatorio.add(menuItemRelatorioProduto);

        menuItemRelatorioProdutoNegativo.setText("Produtos em Falta");
        menuRelatorio.add(menuItemRelatorioProdutoNegativo);

        menuItemRelatorioEstorno.setText("Estornos");
        menuRelatorio.add(menuItemRelatorioEstorno);

        menu.add(menuRelatorio);

        menuFuncionario.setText("Funcionário");

        menuItemFuncionario.setText("Funcionários");
        menuFuncionario.add(menuItemFuncionario);

        menuItemCategoriaFuncionario.setText("Categoria Funcionário");
        menuFuncionario.add(menuItemCategoriaFuncionario);
        menuFuncionario.add(jSeparator2);

        menuItemControleAcesso.setText("Controle de Acesso");
        menuFuncionario.add(menuItemControleAcesso);

        menu.add(menuFuncionario);

        MenuHistorico.setText("Histórico");

        menuItemCobrancas.setText("Cobranças");
        MenuHistorico.add(menuItemCobrancas);

        menu.add(MenuHistorico);

        menuConfiguracao.setText("Configurações");

        menuItemConfiguracao.setText("Configurações");
        menuConfiguracao.add(menuItemConfiguracao);

        menuItemIntegracao.setText("Integração");
        menuConfiguracao.add(menuItemIntegracao);
        menuConfiguracao.add(jSeparator7);

        menuItemSobre.setText("Sobre");
        menuConfiguracao.add(menuItemSobre);

        menu.add(menuConfiguracao);

        menuLogoff.setText("Logoff");
        menu.add(menuLogoff);

        setJMenuBar(menu);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu MenuHistorico;
    private javax.swing.JToolBar barraFerramentas;
    private javax.swing.JButton btnCalculadora;
    private javax.swing.JButton btnCliente;
    private javax.swing.JButton btnCondominio;
    private javax.swing.JButton btnCondominos;
    private javax.swing.JButton btnSelecionarCondominio;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private static javax.swing.JLabel lblCondominio;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JMenuBar menu;
    private javax.swing.JMenu menuCadastro;
    private javax.swing.JMenu menuCobrancas;
    private javax.swing.JMenu menuConfiguracao;
    private javax.swing.JMenu menuContas;
    private javax.swing.JMenu menuFinanceiro;
    private javax.swing.JMenu menuFuncionario;
    private javax.swing.JMenuItem menuItemAdvogados;
    private javax.swing.JMenuItem menuItemAgua;
    private javax.swing.JMenuItem menuItemAplicacao;
    private javax.swing.JMenuItem menuItemBanco;
    private javax.swing.JMenuItem menuItemCalculoMaiorCota;
    private javax.swing.JMenuItem menuItemCategoriaFuncionario;
    private javax.swing.JMenuItem menuItemCobrancaBase;
    private javax.swing.JMenuItem menuItemCobrancas;
    private javax.swing.JMenuItem menuItemComissao;
    private javax.swing.JMenuItem menuItemCondominios;
    private javax.swing.JMenuItem menuItemCondominos;
    private javax.swing.JMenuItem menuItemConfiguracao;
    private javax.swing.JMenuItem menuItemConsignacao;
    private javax.swing.JMenuItem menuItemContaCorrente;
    private javax.swing.JMenuItem menuItemContaPagar;
    private javax.swing.JMenuItem menuItemContas;
    private javax.swing.JMenuItem menuItemContasDispensaveis;
    private javax.swing.JMenuItem menuItemContasIndispensaveis;
    private javax.swing.JMenuItem menuItemControleAcesso;
    private javax.swing.JMenuItem menuItemEmprestimo;
    private javax.swing.JMenuItem menuItemExtratoBancario;
    private javax.swing.JMenuItem menuItemFluxoCaixa;
    private javax.swing.JMenuItem menuItemFornecedores;
    private javax.swing.JMenuItem menuItemFuncionario;
    private javax.swing.JMenuItem menuItemGas;
    private javax.swing.JMenuItem menuItemIdentificadores;
    private javax.swing.JMenuItem menuItemIntegracao;
    private javax.swing.JMenuItem menuItemLancamentos;
    private javax.swing.JMenuItem menuItemOrcamento;
    private javax.swing.JMenuItem menuItemPoupanca;
    private javax.swing.JMenuItem menuItemRelatorioCliente;
    private javax.swing.JMenuItem menuItemRelatorioEstorno;
    private javax.swing.JMenuItem menuItemRelatorioFornecedor;
    private javax.swing.JMenuItem menuItemRelatorioFuncionarios;
    private javax.swing.JMenuItem menuItemRelatorioProduto;
    private javax.swing.JMenuItem menuItemRelatorioProdutoNegativo;
    private javax.swing.JMenuItem menuItemRemessaSalario;
    private javax.swing.JMenuItem menuItemSobre;
    private javax.swing.JMenuItem menuItemTaxaExtra;
    private javax.swing.JMenu menuLogoff;
    private javax.swing.JMenu menuRelatorio;
    private javax.swing.JPanel painelBarraFerramentas;
    // End of variables declaration//GEN-END:variables
}
