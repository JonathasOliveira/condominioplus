/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaEmprestimo.java
 *
 * Created on 28/02/2011, 12:07:28
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.apresentacao.financeiro.*;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.TaxaExtra;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class TelaTaxaExtra extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TaxaExtra taxa;
    private List<TaxaExtra> listaTaxas;
    private TabelaModelo_2<TaxaExtra> modelo;
    private TabelaModelo_2<ParcelaTaxaExtra> modeloParcela;
    private Conta conta;

    /** Creates new form TelaEmprestimo */
    public TelaTaxaExtra(Condominio condominio) {

        this.condominio = condominio;

        initComponents();

        painelTaxaExtra.setVisible(false);

        new ControladorEventos();

        definirMinimoSpinner(spnDia);

        carregarTabela();
        preencherTela();

        if (condominio != null) {
            this.setTitle("Taxa Extra - " + condominio.getRazaoSocial());
        }
    }

    private void preencherTela() {
        txtDataFinal.setValue(DataUtil.toString(new DateTime(DataUtil.hoje().plusMonths(1))));
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<TaxaExtra>(tabela, "Período, Conta, Descrição, Valor, Nº Cotas, Cobr. a Descartar, Vencimento, Sindico Paga?, Dividir Fração Ideal?, Cobrar com Condomínio?".split(",")) {

            @Override
            protected List<TaxaExtra> getCarregarObjetos() {
                return getTaxas();
            }

            @Override
            public Object getValor(TaxaExtra t, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.toString(t.getDataInicial()) + " a " + DataUtil.toString(t.getDataFinal());
                    case 1:
                        return t.getConta().getCodigo();
                    case 2:
                        return t.getDescricao();
                    case 3:
                        return PagamentoUtil.formatarMoeda(t.getValor().doubleValue());
                    case 4:
                        return t.getNumeroCotas();
                    case 5:
                        return t.getCobrancasADescartar();
                    case 6:
                        return t.getDiaVencimento();
                    case 7:
                        return t.isSindicoPaga();
                    case 8:
                        return t.isDividirFracaoIdeal();
                    case 9:
                        return t.isCobrarComCondominio();
                    default:
                        return null;

                }
            }
        };

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabela.getColumn(modelo.getCampo(3)).setCellRenderer(direita);
        tabela.getColumn(modelo.getCampo(4)).setCellRenderer(direita);
        tabela.getColumn(modelo.getCampo(6)).setCellRenderer(direita);

        tabela.getColumn(modelo.getCampo(0)).setMinWidth(140);
        tabela.getColumn(modelo.getCampo(1)).setMinWidth(80);
        tabela.getColumn(modelo.getCampo(2)).setMinWidth(150);
        tabela.getColumn(modelo.getCampo(3)).setMaxWidth(60);
        tabela.getColumn(modelo.getCampo(7)).setMinWidth(80);
        tabela.getColumn(modelo.getCampo(8)).setMinWidth(115);
        tabela.getColumn(modelo.getCampo(9)).setMinWidth(135);

        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private List<TaxaExtra> getTaxas() {
        return listaTaxas = condominio.getTaxas();
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtConta);
        campos.add(txtHistorico);
        campos.add(txtNumeroParcelas);
        campos.add(txtValor);
//        campos.add(txtValorParcelas);

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

    private void preencherObjeto() {

        taxa = new TaxaExtra();
        taxa.setDataInicial(DataUtil.getCalendar(txtDataInicial.getValue()));
        taxa.setDataFinal(DataUtil.getCalendar(txtDataFinal.getValue()));
        taxa.setDescricao(txtHistorico.getText());
        taxa.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));

        if (radioSindicoSim.isSelected()) {
            taxa.setSindicoPaga(true);
        } else if (radioSindicoNao.isSelected()) {
            taxa.setSindicoPaga(false);
        }

        if (radioFracaoSim.isSelected()) {
            taxa.setDividirFracaoIdeal(true);
        } else if (radioFracaoNao.isSelected()) {
            taxa.setDividirFracaoIdeal(false);
        }

        if (radioCondominioSim.isSelected()) {
            taxa.setCobrarComCondominio(true);
        } else if (radioCondominioNao.isSelected()) {
            taxa.setCobrarComCondominio(false);
        }

        taxa.setNumeroCotas(Integer.valueOf(txtNumeroParcelas.getText()));
        taxa.setCobrancasADescartar((Integer) spnCobrancasADescartar.getValue());
        taxa.setDiaVencimento((Integer) spnDia.getValue());

        if (conta != null) {
            taxa.setConta(conta);
        }

        for (int i = 0; i < taxa.getNumeroCotas(); i++) {
            ParcelaTaxaExtra parcela = new ParcelaTaxaExtra();
            parcela.setNumeroParcela(i + 1);
            parcela.setValor(taxa.getValor().divide(new BigDecimal(taxa.getNumeroCotas())));

            parcela.setTaxa(taxa);
            taxa.getParcelas().add(parcela);
        }

        taxa.setCondominio(condominio);
        condominio.getTaxas().add(taxa);
        new DAO().salvar(condominio);

        limparCampos();

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

            new DAO().salvar(condominio);

            String descricao = "Taxa Extra adicionada! " + taxa.getDescricao() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void remover() {
        if (!ApresentacaoUtil.perguntar("Desejar remover a(s) taxa(s)?", this)) {
            return;
        }
        if (modelo.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modelo.getObjetosSelecionados());
            List<TaxaExtra> itensRemover = modelo.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (TaxaExtra t : itensRemover) {
                    modelo.remover(t);
                    new DAO().remover(t);
                }
            }
            new DAO().remover(itensRemover);
            new DAO().salvar(condominio);

            painelTaxaExtra.setVisible(false);
            ApresentacaoUtil.exibirInformacao("Taxa(s) removida(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void limparCampos() {
        txtValor.setText("");
        txtConta.setText("");
        txtHistorico.setText("");
        txtNumeroParcelas.setText("");
        definirMinimoSpinner(spnDia);
        radioSindicoNao.setSelected(true);
        radioFracaoNao.setSelected(true);
        radioCondominioNao.setSelected(true);
    }

    public List<ParcelaTaxaExtra> listarParcelas(TaxaExtra t) {
        List<ParcelaTaxaExtra> lista = t.getParcelas();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                ParcelaTaxaExtra p1 = (ParcelaTaxaExtra) o1;
                ParcelaTaxaExtra p2 = (ParcelaTaxaExtra) o2;
                return new Integer(p1.getNumeroParcela()).compareTo(new Integer(p2.getNumeroParcela()));
            }
        };

        Collections.sort(lista, c);

        return lista;
    }

    public void carregarTabelaParcelas() {

        modeloParcela = new TabelaModelo_2<ParcelaTaxaExtra>(tabelaParcelas, "Número Parcela, Valor".split(",")) {

            @Override
            protected List<ParcelaTaxaExtra> getCarregarObjetos() {
                return listarParcelas(modelo.getObjetoSelecionado());
            }

            @Override
            public Object getValor(ParcelaTaxaExtra parcela, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return parcela.getNumeroParcela();
                    case 1:
                        return PagamentoUtil.formatarMoeda(parcela.getValor().doubleValue());
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

//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(2)).setCellRenderer(direita);
//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(4)).setCellRenderer(centralizado);
//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(5)).setCellRenderer(centralizado);
//
//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(1)).setMinWidth(300);
//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(2)).setMaxWidth(140);
//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(3)).setMaxWidth(140);
//        tabelaParcelas.getColumn(modeloTabelaPagamentos.getCampo(4)).setMaxWidth(50);

    }

//    private void desabilitarCamposContrato() {
//        txtDataContrato.setEnabled(false);
//        txtCodigoContrato.setEnabled(false);
//        txtParcelasContrato.setEnabled(false);
//        txtValorContrato.setEnabled(false);
//    }
    private void exibirPainelDetalhes(TaxaExtra t) {
        if (t != null) {
            painelTaxaExtra.setVisible(true);
            taxa = t;
//            desabilitarCamposContrato();
            preencherPainelDetalhes(t);
            carregarTabelaParcelas();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma taxa!", this);
        }

    }

    private void cancelar() {
        painelTaxaExtra.setVisible(false);
        carregarTabela();
    }

//    public BigDecimal calculaSaldo() {
//        BigDecimal valor = new BigDecimal(0);
//        List<ContratoEmprestimo> lista = new DAO().listar("ContratosPorData", emprestimo);
//        for (ContratoEmprestimo c : lista) {
//            valor = valor.add(c.getSaldo());
//
//
//
////            valor = valor.soma(c.getSaldo());
//        }
//        emprestimo.setSaldo(valor);
//        new DAO().salvar(emprestimo);
//        return valor;
//    }
    public void preencherPainelDetalhes(TaxaExtra t) {
        txtPeriodo.setText(DataUtil.toString(t.getDataInicial()) + " a " + DataUtil.toString(t.getDataFinal()));
        txtCodigoConta.setText(String.valueOf(t.getConta().getCodigo()));
        txtDescricao.setText(t.getDescricao());
        txtCotas.setText(String.valueOf(t.getNumeroCotas()));
        definirMinimoSpinner(spnDiaVencimento);
        spnDiaVencimento.setValue(t.getDiaVencimento());
        txtValorTaxa.setText(PagamentoUtil.formatarMoeda(t.getValor().doubleValue()));
        if (taxa.isSindicoPaga()) {
            radioDetalheSindicoSim.setSelected(true);
        } else {
            radioDetalheSindicoNao.setSelected(true);
        }

        if (taxa.isDividirFracaoIdeal()) {
            radioDetalheFracaoSim.setSelected(true);
        } else {
            radioDetalheFracaoNao.setSelected(true);
        }

        if (taxa.isCobrarComCondominio()) {
            radioDetalheCondominioSim.setSelected(true);
        } else {
            radioDetalheCondominioNao.setSelected(true);
        }
    }

    private void exibirDetalheParcela() {
        if (!modeloParcela.getObjetosSelecionados().isEmpty()) {
            DialogoRateioTaxaExtra tela = new DialogoRateioTaxaExtra(modeloParcela.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloParcela.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma parcela!", this);
        }
    }

    private void salvarTaxa() {
        taxa.setDiaVencimento((Integer) spnDiaVencimento.getValue());
        taxa.setDescricao(txtDescricao.getText());
        new DAO().salvar(taxa);
        carregarTabela();

        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso!", this);
    }

    private void definirMinimoSpinner(JSpinner spinner) {
        SpinnerNumberModel nm = new SpinnerNumberModel();
        nm.setValue(1);
        nm.setMinimum(1);
        nm.setMaximum(31);
        spinner.setModel(nm);
    }

    private void calcularRateio() {
        if (modeloParcela.getObjetosSelecionados().size() == 1) {
            ParcelaTaxaExtra parcela = modeloParcela.getObjetoSelecionado();
            TaxaExtra tx = modeloParcela.getObjetoSelecionado().getTaxa();
            int numero = tx.getCondominio().getUnidades().size();
            if (!parcela.getRateios().isEmpty()) {
                ApresentacaoUtil.exibirAdvertencia("O rateio para essa parcela já foi calculado.", this);
                return;
            }
            if (tx.isDividirFracaoIdeal()) {
            } else {
                for (Unidade u : tx.getCondominio().getUnidades()) {
                    if (u.isSindico() || verificarInadimplencia(tx.getCobrancasADescartar(), u)) {
                        numero -= 1;
                    }
                }
                BigDecimal valorRateio = new BigDecimal(0);
                valorRateio = valorRateio.add(modeloParcela.getObjetoSelecionado().getValor());
                valorRateio = valorRateio.divide(new BigDecimal(numero));
                System.out.println("valor rateio " + PagamentoUtil.formatarMoeda(valorRateio.doubleValue()));
                RATEIO:
                for (Unidade u : tx.getCondominio().getUnidades()) {
                    if (u.isSindico() && !tx.isSindicoPaga()) {
                        continue RATEIO;
                    }
                    RateioTaxaExtra rateio = new RateioTaxaExtra();
                    rateio.setUnidade(u);
                    rateio.setParcela(modeloParcela.getObjetoSelecionado());
                    rateio.setValosACobrar(valorRateio);
                    parcela.getRateios().add(rateio);
                    new DAO().salvar(parcela);
                    ApresentacaoUtil.exibirAdvertencia("Cálculo efetuado com sucesso.", this);
                }
            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma parcela.", this);
            return;
        }
    }

    private boolean verificarInadimplencia(int cobrancasADescartar, Unidade u) {
        int inadimplencia = 0;
        List<Cobranca> lista = new DAO().listar("CobrancasEmAbertoPorUnidade", u);
        for (Cobranca co : lista) {
            if (DataUtil.compararData(DataUtil.hoje(), DataUtil.getDateTime(co.getDataVencimento())) == 1) {
                inadimplencia += 1;
            }
        }
        if (cobrancasADescartar != 0 && inadimplencia >= cobrancasADescartar) {
            return true;
        }
        return false;
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == btnIncluir) {
                salvar();
                carregarTabela();
//                calcularSaldo();
            } else if (origem == btnCalcular) {
                calcularRateio();
            } else if (origem == itemMenuRemoverSelecionados) {
                remover();
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == itemMenuVisualizarTaxa) {
                exibirPainelDetalhes(modelo.getObjetoSelecionado());
            } else if (origem == btnVoltar) {
                cancelar();
            } else if (origem == btnSalvar) {
                salvarTaxa();
            }
        }

        @Override
        public void configurar() {
            btnCalcular.addActionListener(this);
            btnConta.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnIncluir.addActionListener(this);
            tabela.addMouseListener(this);
            txtConta.addFocusListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            itemMenuVisualizarTaxa.addActionListener(this);
            btnVoltar.addActionListener(this);
            radioSindicoSim.addActionListener(this);
            txtValor.addFocusListener(this);
            radioSindicoNao.addActionListener(this);
            btnSalvar.addActionListener(this);
            tabelaParcelas.addMouseListener(this);
            txtDataInicial.addChangeListener(this);
            tabela.addKeyListener(this);
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaTaxaExtra.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
            }
            if (e.getSource() == txtValor) {
//                if (radioSindicoSim.isSelected() || radioConformeDisponibilidade.isSelected()) {
//                    String resultado = null;
//                    if (!txtValor.getText().equals("") && txtValor.getText() != null) {
//                        resultado = txtValor.getText();
//                        if (resultado != null) {
//                            txtValorParcelas.setText(resultado);
//                        } else {
//                            ApresentacaoUtil.exibirErro("Digíte um valor para o empréstimo!", TelaTaxaExtra.this);
//                            return;
//                        }
//
//                    }
//                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabela) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            origem = e.getSource();
            if (origem == tabela && painelTaxaExtra.isVisible()) {
                exibirPainelDetalhes(modelo.getObjetoSelecionado());
            } else if (origem == tabelaParcelas && e.getClickCount() == 2) {
                exibirDetalheParcela();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            origem = e.getSource();
            if (origem == txtDataInicial) {
                txtDataFinal.setValue(DataUtil.toString(DataUtil.getDateTime(txtDataInicial.getValue()).plusMonths(1)));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            origem = e.getSource();
            if ((origem == tabela && painelTaxaExtra.isVisible()) && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
                exibirPainelDetalhes(modelo.getObjetoSelecionado());
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
        itemMenuVisualizarTaxa = new javax.swing.JMenuItem();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        itemMenuImprimir = new javax.swing.JMenuItem();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        jLabel1 = new javax.swing.JLabel();
        txtConta = new javax.swing.JTextField();
        txtValor = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnImprimir = new javax.swing.JButton();
        btnIncluir = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtNumeroParcelas = new javax.swing.JTextField();
        radioSindicoSim = new javax.swing.JRadioButton();
        radioSindicoNao = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        radioFracaoSim = new javax.swing.JRadioButton();
        radioFracaoNao = new javax.swing.JRadioButton();
        jLabel14 = new javax.swing.JLabel();
        radioCondominioNao = new javax.swing.JRadioButton();
        radioCondominioSim = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        spnDia = new javax.swing.JSpinner();
        spnCobrancasADescartar = new javax.swing.JSpinner();
        jLabel19 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        painelTaxaExtra = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaParcelas = new javax.swing.JTable();
        txtCodigoConta = new javax.swing.JTextField();
        txtDescricao = new javax.swing.JTextField();
        txtCotas = new javax.swing.JTextField();
        txtValorTaxa = new javax.swing.JTextField();
        btnVoltar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        txtPeriodo = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        radioDetalheSindicoSim = new javax.swing.JRadioButton();
        radioDetalheSindicoNao = new javax.swing.JRadioButton();
        jLabel16 = new javax.swing.JLabel();
        radioDetalheFracaoSim = new javax.swing.JRadioButton();
        radioDetalheFracaoNao = new javax.swing.JRadioButton();
        jLabel17 = new javax.swing.JLabel();
        radioDetalheCondominioSim = new javax.swing.JRadioButton();
        radioDetalheCondominioNao = new javax.swing.JRadioButton();
        jLabel18 = new javax.swing.JLabel();
        spnDiaVencimento = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        spnCobrancasADescartarDetalhe = new javax.swing.JSpinner();
        btnCalcular = new javax.swing.JButton();

        itemMenuVisualizarTaxa.setText("Visualizar Detalhes");
        popupMenu.add(itemMenuVisualizarTaxa);

        itemMenuRemoverSelecionados.setText("Remover Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        itemMenuImprimir.setText("Imprimir");
        popupMenu.add(itemMenuImprimir);

        setClosable(true);
        setTitle("Taxa Extra");
        setPreferredSize(new java.awt.Dimension(750, 534));
        setVisible(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(714, 118));

        txtDataInicial.setFocusable(false);
        txtDataInicial.setRequestFocusEnabled(false);

        jLabel1.setText("Data Inicial:");

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

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Cheque");

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel4.setText("Nº de Cotas:");

        txtNumeroParcelas.setName("Nº Parcelas"); // NOI18N

        buttonGroup1.add(radioSindicoSim);
        radioSindicoSim.setText("Sim");

        buttonGroup1.add(radioSindicoNao);
        radioSindicoNao.setSelected(true);
        radioSindicoNao.setText("Não");

        jLabel6.setText("Data Final:");

        txtDataFinal.setName("Data Primeiro Pgto"); // NOI18N

        jLabel12.setText("Síndico Paga?");

        jLabel13.setText("Dividir Fração Ideal?");

        buttonGroup2.add(radioFracaoSim);
        radioFracaoSim.setText("Sim");

        buttonGroup2.add(radioFracaoNao);
        radioFracaoNao.setSelected(true);
        radioFracaoNao.setText("Não");

        jLabel14.setText("Cobrar com Cota Condomínio?");

        buttonGroup3.add(radioCondominioNao);
        radioCondominioNao.setSelected(true);
        radioCondominioNao.setText("Não");

        buttonGroup3.add(radioCondominioSim);
        radioCondominioSim.setText("Sim");

        jLabel5.setText("Dia Venc.:");

        jLabel19.setText("Cobr. a Descartar");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNumeroParcelas)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spnDia)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioSindicoSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioSindicoNao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioFracaoSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioFracaoNao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel14)
                        .addGap(2, 2, 2)
                        .addComponent(radioCondominioSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioCondominioNao)
                        .addGap(18, 18, 18)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(spnCobrancasADescartar)
                        .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDataFinal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtDataInicial, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spnDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(26, 26, 26))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnConta, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel19))
                        .addGap(26, 26, 26))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(spnCobrancasADescartar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnImprimir)
                    .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(radioSindicoNao)
                        .addComponent(radioSindicoSim)
                        .addComponent(jLabel13)
                        .addComponent(radioFracaoSim)
                        .addComponent(radioFracaoNao)
                        .addComponent(jLabel14)
                        .addComponent(radioCondominioNao)
                        .addComponent(radioCondominioSim)
                        .addComponent(jLabel12))))
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

        painelTaxaExtra.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Período:");

        jLabel8.setText("Valor:");

        jLabel9.setText("Nº Cotas:");

        jLabel10.setText("Descrição:");

        jLabel11.setText("Conta:");

        tabelaParcelas.setModel(new javax.swing.table.DefaultTableModel(
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
        tabelaParcelas.setToolTipText("Clique no registro para visualizar os detalhes.");
        jScrollPane2.setViewportView(tabelaParcelas);

        txtCodigoConta.setEditable(false);

        txtCotas.setEditable(false);

        txtValorTaxa.setEditable(false);

        btnVoltar.setText("Voltar");

        btnSalvar.setText("Salvar");

        txtPeriodo.setEditable(false);
        txtPeriodo.setEnabled(false);

        jLabel15.setText("Síndico Paga?");

        buttonGroup4.add(radioDetalheSindicoSim);
        radioDetalheSindicoSim.setText("Sim");
        radioDetalheSindicoSim.setEnabled(false);

        buttonGroup4.add(radioDetalheSindicoNao);
        radioDetalheSindicoNao.setSelected(true);
        radioDetalheSindicoNao.setText("Não");
        radioDetalheSindicoNao.setEnabled(false);

        jLabel16.setText("Dividir Fração Ideal?");

        buttonGroup5.add(radioDetalheFracaoSim);
        radioDetalheFracaoSim.setText("Sim");
        radioDetalheFracaoSim.setEnabled(false);

        buttonGroup5.add(radioDetalheFracaoNao);
        radioDetalheFracaoNao.setSelected(true);
        radioDetalheFracaoNao.setText("Não");
        radioDetalheFracaoNao.setEnabled(false);

        jLabel17.setText("Cobrar com Cota Condomínio?");

        buttonGroup6.add(radioDetalheCondominioSim);
        radioDetalheCondominioSim.setSelected(true);
        radioDetalheCondominioSim.setText("Sim");
        radioDetalheCondominioSim.setEnabled(false);

        buttonGroup6.add(radioDetalheCondominioNao);
        radioDetalheCondominioNao.setText("Não");
        radioDetalheCondominioNao.setEnabled(false);

        jLabel18.setText("Dia Venc.:");

        spnDiaVencimento.setEnabled(false);

        jLabel20.setText("Cobr. a Descartar");

        spnCobrancasADescartarDetalhe.setEnabled(false);

        btnCalcular.setText("Calcular");

        javax.swing.GroupLayout painelTaxaExtraLayout = new javax.swing.GroupLayout(painelTaxaExtra);
        painelTaxaExtra.setLayout(painelTaxaExtraLayout);
        painelTaxaExtraLayout.setHorizontalGroup(
            painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(txtPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(spnDiaVencimento)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(txtCodigoConta, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelTaxaExtraLayout.createSequentialGroup()
                                .addComponent(spnCobrancasADescartarDetalhe, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCotas, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioDetalheSindicoSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioDetalheSindicoNao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDetalheFracaoSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioDetalheFracaoNao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)
                        .addGap(2, 2, 2)
                        .addComponent(radioDetalheCondominioSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDetalheCondominioNao)))
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(txtValorTaxa, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(btnCalcular)))
                .addGap(8, 8, 8))
            .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                .addGap(274, 274, 274)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVoltar)
                .addContainerGap(306, Short.MAX_VALUE))
        );
        painelTaxaExtraLayout.setVerticalGroup(
            painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelTaxaExtraLayout.createSequentialGroup()
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7)
                        .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                            .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel9)
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCotas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(spnCobrancasADescartarDetalhe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                                    .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel11)
                                        .addComponent(jLabel10))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtCodigoConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                                    .addComponent(jLabel18)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(spnDiaVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(txtPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorTaxa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioDetalheSindicoNao)
                    .addComponent(radioDetalheSindicoSim)
                    .addComponent(jLabel16)
                    .addComponent(radioDetalheFracaoSim)
                    .addComponent(radioDetalheFracaoNao)
                    .addComponent(jLabel17)
                    .addComponent(radioDetalheCondominioNao)
                    .addComponent(radioDetalheCondominioSim)
                    .addComponent(jLabel15)
                    .addComponent(btnCalcular))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(btnSalvar))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(painelTaxaExtra, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelTaxaExtra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.JMenuItem itemMenuImprimir;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JMenuItem itemMenuVisualizarTaxa;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel painelTaxaExtra;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JRadioButton radioCondominioNao;
    private javax.swing.JRadioButton radioCondominioSim;
    private javax.swing.JRadioButton radioDetalheCondominioNao;
    private javax.swing.JRadioButton radioDetalheCondominioSim;
    private javax.swing.JRadioButton radioDetalheFracaoNao;
    private javax.swing.JRadioButton radioDetalheFracaoSim;
    private javax.swing.JRadioButton radioDetalheSindicoNao;
    private javax.swing.JRadioButton radioDetalheSindicoSim;
    private javax.swing.JRadioButton radioFracaoNao;
    private javax.swing.JRadioButton radioFracaoSim;
    private javax.swing.JRadioButton radioSindicoNao;
    private javax.swing.JRadioButton radioSindicoSim;
    private javax.swing.JSpinner spnCobrancasADescartar;
    private javax.swing.JSpinner spnCobrancasADescartarDetalhe;
    private javax.swing.JSpinner spnDia;
    private javax.swing.JSpinner spnDiaVencimento;
    private javax.swing.JTable tabela;
    private javax.swing.JTable tabelaParcelas;
    private javax.swing.JTextField txtCodigoConta;
    private javax.swing.JTextField txtConta;
    private javax.swing.JTextField txtCotas;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroParcelas;
    private javax.swing.JTextField txtPeriodo;
    private javax.swing.JTextField txtValor;
    private javax.swing.JTextField txtValorTaxa;
    // End of variables declaration//GEN-END:variables
}
