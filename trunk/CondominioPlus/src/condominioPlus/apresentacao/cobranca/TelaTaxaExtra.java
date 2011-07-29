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
import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.TaxaExtra;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContratoEmprestimo;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.FormaPagamentoEmprestimo;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
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
import java.util.List;
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
import logicpoint.util.Moeda;
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
    private TabelaModelo_2<RateioTaxaExtra> modeloRateio;
    private Conta conta;

    /** Creates new form TelaEmprestimo */
    public TelaTaxaExtra(Condominio condominio) {

        this.condominio = condominio;

        initComponents();

        desabilitarBotoesUmaParcela();

        paineTaxaExtra.setVisible(false);

        new ControladorEventos();

        definirMinimoSpinner();

        carregarTabela();
        preencherTela();

        if (condominio != null) {
            this.setTitle("Taxa Extra - " + condominio.getRazaoSocial());
        }
    }

    private void preencherTela() {
        txtDataFinal.setValue(DataUtil.toString(new DateTime(DataUtil.hoje().plusMonths(1))));
    }

    private void desabilitarBotoesUmaParcela() {
//        if (radioSindicoSim.isSelected()) {
//            txtNumeroParcelas.setText("1");
//            txtValorParcelas.setText(txtValor.getText());
//            txtNumeroParcelas.setEnabled(false);
//            txtValorParcelas.setEnabled(false);
//        } else if (RadioSindicoNao.isSelected()) {
//            txtValorParcelas.setText("");
//            txtNumeroParcelas.setText("");
//            txtNumeroParcelas.setEnabled(true);
//            txtValorParcelas.setEnabled(true);
//        } else if (radioConformeDisponibilidade.isSelected()) {
//            txtNumeroParcelas.setText("1");
//            txtValorParcelas.setText(txtValor.getText());
//            txtNumeroParcelas.setEnabled(false);
//            txtValorParcelas.setEnabled(false);
//        }
    }

    private String obterFormaPagamento(ContratoEmprestimo c) {
        if (c.getForma() == FormaPagamentoEmprestimo.CONFORME_DISPONIBILIDADE) {
            return "Conforme Disponibilidade Financeira";
        } else if (c.getForma() == FormaPagamentoEmprestimo.PAGAMENTO_A_VISTA) {
            return "À vista";
        } else if (c.getForma() == FormaPagamentoEmprestimo.PARCELADO) {
            return "Parcelado";
        }
        return "";
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<TaxaExtra>(tabela, "Período, Conta, Descrição, Valor, Nº Cotas, Vencimento, Sindico Paga?, Dividir Fração Ideal?, Cobrar com Condomínio?".split(",")) {

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
                        return t.getDiaVencimento();
                    case 6:
                        return t.isSindicoPaga();
                    case 7:
                        return t.isDividirFracaoIdeal();
                    case 8:
                        return t.isCobrarComCondominio();
                    default:
                        return null;

                }
            }
        };

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabela.getColumn(modelo.getCampo(4)).setCellRenderer(direita);
        tabela.getColumn(modelo.getCampo(5)).setCellRenderer(direita);

        tabela.getColumn(modelo.getCampo(0)).setMinWidth(140);
        tabela.getColumn(modelo.getCampo(1)).setMinWidth(80);
        tabela.getColumn(modelo.getCampo(2)).setMinWidth(150);
        tabela.getColumn(modelo.getCampo(3)).setMaxWidth(60);
        tabela.getColumn(modelo.getCampo(6)).setMinWidth(80);
        tabela.getColumn(modelo.getCampo(7)).setMinWidth(115);
        tabela.getColumn(modelo.getCampo(8)).setMinWidth(135);

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
        taxa.setDiaVencimento((Integer) spnDia.getValue());

        if (conta != null) {
            taxa.setConta(conta);
        }

//        if (contrato.getNumeroParcelas() > 0) {
//            String texto = "";
//            if (contrato.getForma() == FormaPagamentoEmprestimo.PARCELADO && contrato.getNumeroParcelas() > 1) {
//                for (int i = 0; i < contrato.getNumeroParcelas(); i++) {
//                    texto = "PAGAMENTO PARCELA " + (i + 1);
//                    pagamento = new Pagamento();
//                    if (i == 0) {
//                        pagamento.setDataVencimento(DataUtil.getCalendar(txtDataFinal.getValue()));
//                    } else {
//                        pagamento.setDataVencimento(DataUtil.getCalendar(new DateTime(txtDataFinal.getValue()).plusMonths(i)));
//                    }
//                    pagamento.setConta(conta.getContaVinculada());
//                        if (pagamento.getConta().isCredito()) {
//                            pagamento.setValor(new BigDecimal(txtValorParcelas.getText().replace(",", ".")));
//                        } else {
//                            pagamento.setValor(new BigDecimal(txtValorParcelas.getText().replace(",", ".")).negate());
//                        }

//                    pagamento.setContratoEmprestimo(contrato);
//                    pagamento.setHistorico(texto + " " + contrato.getDescricao());
//                    System.out.println("pagamento historico " + pagamento.getHistorico());
//                    pagamento.setForma(FormaPagamento.DINHEIRO);
//                    pagamento.setDadosPagamento(new DadosDOC(Long.valueOf(Pagamento.gerarNumeroDocumento())));
//
//                    if (conta.isCredito()) {
//                        new DAO().salvar(pagamento);
//                    } else {
//                        if (i == 0) {
//                            verificarVinculo(pagamento, texto);
//                        } else {
//                            verificarVinculo(pagamento, texto);
//                        }
//                    }
//
//                }
//
//            } else {
//                System.out.println("teste");
//                texto = "PAGAMENTO ";
//                pagamento = new Pagamento();
//                pagamento.setDataVencimento(DataUtil.getCalendar(txtDataFinal.getValue()));
//                pagamento.setConta(conta.getContaVinculada());
//                if (pagamento.getConta().isCredito()) {
//                    pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
//                } else {
//                    pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
//                }
//
//                pagamento.setContratoEmprestimo(contrato);
//                pagamento.setHistorico(texto + " " + contrato.getDescricao());
//                pagamento.setDadosPagamento(new DadosDOC(Long.valueOf(Pagamento.gerarNumeroDocumento())));
//                pagamento.setForma(FormaPagamento.DINHEIRO);
//
//                if (conta.isCredito()) {
//                    new DAO().salvar(pagamento);
//                } else {
//                    verificarVinculo(pagamento, texto);
//                }
//            }

//            Pagamento p = new Pagamento();
//            p.setDataPagamento(DataUtil.getCalendar(txtDataInicial.getValue()));
//            p.setHistorico(conta.getContaVinculada().getNome() + " " + contrato.getDescricao());
//            p.setConta(conta.getContaVinculada());
//            p.setContratoEmprestimo(contrato);
//            if (p.getConta().isCredito()) {
//                p.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
//            } else {
//                p.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
//            }
//            p.setSaldo(new BigDecimal(0));
//            p.setDadosPagamento(new DadosDOC(Long.valueOf(Pagamento.gerarNumeroDocumento()) + 1));
//
//            p.setContaCorrente(condominio.getContaCorrente());
//            p.setPago(true);

        taxa.setCondominio(condominio);
        condominio.getTaxas().add(taxa);
        new DAO().salvar(condominio);


        limparCampos();
//                calcularSaldo();
//            return true;

//        }
//        return false;
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

            paineTaxaExtra.setVisible(false);
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
        definirMinimoSpinner();
//        txtValorParcelas.setText("");
    }

    public List<ParcelaTaxaExtra> listarParcelas(TaxaExtra t) {
        List<ParcelaTaxaExtra> lista = t.getParcelas();
        return lista;
    }

//    public void carregarTabelaPagamentos() {
//
//        modeloTabelaPagamentos = new TabelaModelo_2<Pagamento>(tabelaPagamentos, "Data Vencimento, Histórico, Valor, Conta, Tipo, Pago?".split(",")) {
//
//            @Override
//            protected List<Pagamento> getCarregarObjetos() {
//                return listarPagamentos();
//            }
//
//            @Override
//            public Object getValor(Pagamento pagamento, int indiceColuna) {
//                switch (indiceColuna) {
//                    case 0:
//                        return DataUtil.getDateTime(pagamento.getDataVencimento());
//                    case 1:
//                        return pagamento.getHistorico();
//                    case 2:
//                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
//                    case 3:
//                        return pagamento.getConta().getCodigo();
//                    case 4:
//                        return pagamento.getConta().isCredito() ? "C" : "D";
//                    case 5:
//                        return pagamento.isPago() ? DataUtil.toString(DataUtil.getDateTime(pagamento.getDataPagamento())) : "Não pago";
//                    default:
//                        return null;
//                }
//            }
//        };
//
//        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
//        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
//        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();
//
//        esquerda.setHorizontalAlignment(SwingConstants.LEFT);
//        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
//        direita.setHorizontalAlignment(SwingConstants.RIGHT);
//
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(2)).setCellRenderer(direita);
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(4)).setCellRenderer(centralizado);
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(5)).setCellRenderer(centralizado);
//
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(1)).setMinWidth(300);
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(2)).setMaxWidth(140);
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(3)).setMaxWidth(140);
//        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(4)).setMaxWidth(50);
//
//    }
//    private void desabilitarCamposContrato() {
//        txtDataContrato.setEnabled(false);
//        txtCodigoContrato.setEnabled(false);
//        txtParcelasContrato.setEnabled(false);
//        txtValorContrato.setEnabled(false);
//    }

    private void exibirPainelContrato(TaxaExtra t) {
        if (t != null) {
            paineTaxaExtra.setVisible(true);
            taxa = t;
//            desabilitarCamposContrato();
            preencherPainelContrato(t);
//            carregarTabelaPagamentos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma taxa!", this);
        }

    }

    private void cancelar() {
        paineTaxaExtra.setVisible(false);
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
    public void preencherPainelContrato(TaxaExtra t) {
        txtPeriodo.setText(DataUtil.toString(t.getDataInicial()) + " a " + DataUtil.toString(t.getDataFinal()));
//        txtCodigoContrato.setText(String.valueOf(c.getCodigo()));
        txtDescricao.setText(t.getDescricao());
//        txtParcelasContrato.setText(String.valueOf(c.getNumeroParcelas()));
//        txtValorContrato.setText(new Moeda(c.getValor()).toString());
    }

//    private void editarPagamentoContrato() {
//        if (!modeloTabelaPagamentos.getObjetosSelecionados().isEmpty()) {
//            DialogoEditarPagamentoContrato tela = new DialogoEditarPagamentoContrato(modeloTabelaPagamentos.getObjetoSelecionado());
//            tela.setLocationRelativeTo(this);
//            tela.setVisible(true);
//            modeloTabelaPagamentos.carregarObjetos();
//        } else {
//            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
//        }
//    }
    private void salvarTaxa() {
        taxa.setDescricao(txtDescricao.getText());
        new DAO().salvar(taxa);
        carregarTabela();

        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso!", this);
    }

    private void definirMinimoSpinner() {
        SpinnerNumberModel nm = new SpinnerNumberModel();
        nm.setValue(1);
        nm.setMinimum(1);
        nm.setMaximum(31);
        spnDia.setModel(nm);
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
//            } else if (origem == btnCalcular) {
//                calcularSaldo();
            } else if (origem == itemMenuRemoverSelecionados) {
                remover();
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == itemMenuVisualizarTaxa) {
                exibirPainelContrato(modelo.getObjetoSelecionado());
            } else if (origem == btnVoltar) {
                cancelar();
            } else if (origem == radioSindicoSim) {
                desabilitarBotoesUmaParcela();
//            } else if (origem == radioConformeDisponibilidade) {
//                desabilitarBotoesUmaParcela();
            } else if (origem == radioSindicoNao) {
                desabilitarBotoesUmaParcela();
            } else if (origem == btnSalvar) {
                salvarTaxa();
            }
        }

        @Override
        public void configurar() {
//            btnCalcular.addActionListener(this);
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
            tabelaPagamentos.addMouseListener(this);
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
            if (origem == tabela && paineTaxaExtra.isVisible()) {
                exibirPainelContrato(modelo.getObjetoSelecionado());
            } else if (origem == tabelaPagamentos && e.getClickCount() == 2) {
//                editarPagamentoContrato();
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
            if ((origem == tabela && paineTaxaExtra.isVisible()) && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
                exibirPainelContrato(modelo.getObjetoSelecionado());
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        paineTaxaExtra = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaPagamentos = new javax.swing.JTable();
        txtCodigoContrato = new javax.swing.JTextField();
        txtDescricao = new javax.swing.JTextField();
        txtParcelasContrato = new javax.swing.JTextField();
        txtValorContrato = new javax.swing.JTextField();
        btnVoltar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        txtPeriodo = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        radioSindicoSim1 = new javax.swing.JRadioButton();
        radioSindicoNao1 = new javax.swing.JRadioButton();
        jLabel16 = new javax.swing.JLabel();
        radioFracaoSim1 = new javax.swing.JRadioButton();
        radioFracaoNao1 = new javax.swing.JRadioButton();
        jLabel17 = new javax.swing.JLabel();
        radioCondominioSim1 = new javax.swing.JRadioButton();
        radioCondominioNao1 = new javax.swing.JRadioButton();

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
        radioSindicoNao.setText("Não");

        jLabel6.setText("Data Final:");

        txtDataFinal.setName("Data Primeiro Pgto"); // NOI18N

        jLabel12.setText("Síndico Paga?");

        jLabel13.setText("Dividir Fração Ideal?");

        buttonGroup2.add(radioFracaoSim);
        radioFracaoSim.setText("Sim");

        buttonGroup2.add(radioFracaoNao);
        radioFracaoNao.setText("Não");

        jLabel14.setText("Cobrar com Cota Condomínio?");

        buttonGroup3.add(radioCondominioNao);
        radioCondominioNao.setText("Não");

        buttonGroup3.add(radioCondominioSim);
        radioCondominioSim.setText("Sim");

        jLabel5.setText("Dia Venc.:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConta))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(txtHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                        .addGap(10, 10, 10)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioSindicoSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioSindicoNao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                        .addComponent(radioCondominioNao)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDataFinal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtDataInicial, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)))
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
                            .addComponent(jLabel2))
                        .addGap(26, 26, 26))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioSindicoNao)
                    .addComponent(radioSindicoSim)
                    .addComponent(jLabel13)
                    .addComponent(radioFracaoSim)
                    .addComponent(radioFracaoNao)
                    .addComponent(jLabel14)
                    .addComponent(radioCondominioNao)
                    .addComponent(radioCondominioSim)
                    .addComponent(jLabel12)))
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

        paineTaxaExtra.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Período:");

        jLabel8.setText("Valor:");

        jLabel9.setText("Nº Cotas:");

        jLabel10.setText("Descrição:");

        jLabel11.setText("Código:");

        tabelaPagamentos.setModel(new javax.swing.table.DefaultTableModel(
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
        tabelaPagamentos.setToolTipText("Clique no pagamento para editá-lo.");
        jScrollPane2.setViewportView(tabelaPagamentos);

        btnVoltar.setText("Voltar");

        btnSalvar.setText("Salvar");

        txtPeriodo.setEnabled(false);

        jLabel15.setText("Síndico Paga?");

        buttonGroup4.add(radioSindicoSim1);
        radioSindicoSim1.setText("Sim");

        buttonGroup4.add(radioSindicoNao1);
        radioSindicoNao1.setSelected(true);
        radioSindicoNao1.setText("Não");

        jLabel16.setText("Dividir Fração Ideal?");

        buttonGroup5.add(radioFracaoSim1);
        radioFracaoSim1.setText("Sim");

        buttonGroup5.add(radioFracaoNao1);
        radioFracaoNao1.setSelected(true);
        radioFracaoNao1.setText("Não");

        jLabel17.setText("Cobrar com Cota Condomínio?");

        buttonGroup6.add(radioCondominioSim1);
        radioCondominioSim1.setSelected(true);
        radioCondominioSim1.setText("Sim");

        buttonGroup6.add(radioCondominioNao1);
        radioCondominioNao1.setText("Não");

        javax.swing.GroupLayout paineTaxaExtraLayout = new javax.swing.GroupLayout(paineTaxaExtra);
        paineTaxaExtra.setLayout(paineTaxaExtraLayout);
        paineTaxaExtraLayout.setHorizontalGroup(
            paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paineTaxaExtraLayout.createSequentialGroup()
                .addGap(273, 273, 273)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVoltar)
                .addGap(12, 12, 12))
            .addGroup(paineTaxaExtraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paineTaxaExtraLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioSindicoSim1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioSindicoNao1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioFracaoSim1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioFracaoNao1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)
                        .addGap(2, 2, 2)
                        .addComponent(radioCondominioSim1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioCondominioNao1))
                    .addGroup(paineTaxaExtraLayout.createSequentialGroup()
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(txtPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCodigoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(txtParcelasContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(txtValorContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(paineTaxaExtraLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
                .addContainerGap())
        );
        paineTaxaExtraLayout.setVerticalGroup(
            paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paineTaxaExtraLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addGroup(paineTaxaExtraLayout.createSequentialGroup()
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCodigoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtValorContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtParcelasContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioSindicoNao1)
                    .addComponent(radioSindicoSim1)
                    .addComponent(jLabel16)
                    .addComponent(radioFracaoSim1)
                    .addComponent(radioFracaoNao1)
                    .addComponent(jLabel17)
                    .addComponent(radioCondominioNao1)
                    .addComponent(radioCondominioSim1)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(paineTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(btnSalvar))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
                    .addComponent(paineTaxaExtra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addGap(13, 13, 13)
                .addComponent(paineTaxaExtra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JPanel paineTaxaExtra;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JRadioButton radioCondominioNao;
    private javax.swing.JRadioButton radioCondominioNao1;
    private javax.swing.JRadioButton radioCondominioSim;
    private javax.swing.JRadioButton radioCondominioSim1;
    private javax.swing.JRadioButton radioFracaoNao;
    private javax.swing.JRadioButton radioFracaoNao1;
    private javax.swing.JRadioButton radioFracaoSim;
    private javax.swing.JRadioButton radioFracaoSim1;
    private javax.swing.JRadioButton radioSindicoNao;
    private javax.swing.JRadioButton radioSindicoNao1;
    private javax.swing.JRadioButton radioSindicoSim;
    private javax.swing.JRadioButton radioSindicoSim1;
    private javax.swing.JSpinner spnDia;
    private javax.swing.JTable tabela;
    private javax.swing.JTable tabelaPagamentos;
    private javax.swing.JTextField txtCodigoContrato;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroParcelas;
    private javax.swing.JTextField txtParcelasContrato;
    private javax.swing.JTextField txtPeriodo;
    private javax.swing.JTextField txtValor;
    private javax.swing.JTextField txtValorContrato;
    // End of variables declaration//GEN-END:variables
}
