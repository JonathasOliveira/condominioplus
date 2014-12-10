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
import condominioPlus.apresentacao.fornecedor.DialogoFornecedor;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.DadosTalaoCheque;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaPagar;
import condominioPlus.negocio.financeiro.ContaReceber;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.fornecedor.Fornecedor;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
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
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class TelaContaPagar extends javax.swing.JInternalFrame {

    private ContaPagar contaPagar;
    private ContaReceber contaReceber;
    private Pagamento pagamento;
    private Condominio condominio;
    private Conta conta;
    private Fornecedor fornecedor;
    private TabelaModelo_2 modeloTabela;
    private TabelaModelo_2 modeloTabela2;
    private TabelaModelo_2 modeloTabelaContaReceber;
    private List<Pagamento> pagamentos;
    private List<Pagamento> cheques = new ArrayList<Pagamento>();
    private List<Pagamento> pagamentosContaReceber;
    private Calendar datInicio = DataUtil.getCalendar(DataUtil.hoje());
    private Calendar datTermino = DataUtil.getCalendar(DataUtil.hoje());

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
        if (condominio.getContaReceber() == null) {
            contaReceber = new ContaReceber();
            condominio.setContaReceber(contaReceber);
            new DAO().salvar(condominio);
        } else {
            contaReceber = condominio.getContaReceber();
        }

        initComponents();
        new ControladorEventos();

//        carregarFornecedor();

        carregarTabela();
        carregarTabelaContaReceber();

//        carregarComboFiltro();
//        new LimitarCaracteres(10).ValidaNumero(txtConta);
        txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());
        painelCheques.setVisible(false);
        painelSaldoContaCorrente.setVisible(false);
        btnGravar.setEnabled(false);
        btnImprimir.setEnabled(false);
        habilitarDateField();
        if (condominio != null) {
            this.setTitle("Contas a Pagar/Receber - " + condominio.getRazaoSocial());
        }
        
        checkBoxMostrarDateField.setSelected(true);
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
                        return pagamento.getFornecedor() != null ? pagamento.getFornecedor() : "";
                    case 4:
                        return pagamento.getHistorico();
                    case 5:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
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
        tabelaContaPagar.getColumn(modeloTabela.getCampo(0)).setMinWidth(80);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(2)).setMinWidth(90);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(3)).setMinWidth(180);
        tabelaContaPagar.getColumn(modeloTabela.getCampo(4)).setMinWidth(260);
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
                        return pagamento.getHistorico() + " (" + pagamento.getFornecedor().toUpperCase() + ")";
                    case 3:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());

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

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaCheque.getColumn(modeloTabela2.getCampo(3)).setCellRenderer(direita);

        tabelaCheque.getColumn(modeloTabela2.getCampo(1)).setMinWidth(180);
        tabelaCheque.getColumn(modeloTabela2.getCampo(2)).setMinWidth(280);
        tabelaCheque.getColumn(modeloTabela2.getCampo(3)).setMinWidth(110);
    }

    private void carregarTabelaContaReceber() {
        modeloTabelaContaReceber = new TabelaModelo_2<Pagamento>(tabelaContaReceber, "Vencimento, Conta, Documento, Fornecedor, Descrição, Valor".split(",")) {

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
                return getPagamentosContaReceber();
            }

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
                        return pagamento.getFornecedor() != null ? pagamento.getFornecedor() : "";
                    case 4:
                        return pagamento.getHistorico();
                    case 5:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
                    default:
                        return null;
                }
            }
//            @Override
//            public boolean getRemover(Pagamento pagamento) {
//                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getHistorico() + " ?", TelaContaPagar.this)) {
//                    return false;
//                }
//
//                try {
//                    new DAO().remover(modeloTabela.getObjetosSelecionados());
//                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getHistorico());
//                    return true;
//                } catch (Throwable t) {
//                    new TratadorExcecao(t, TelaContaPagar.this);
//                    return false;
//                }
//            }
        };

        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        esquerda.setHorizontalAlignment(SwingConstants.LEFT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(1)).setCellRenderer(direita);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(2)).setCellRenderer(direita);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(3)).setCellRenderer(centralizado);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(4)).setCellRenderer(esquerda);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(5)).setCellRenderer(direita);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(0)).setMinWidth(80);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(2)).setMinWidth(90);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(3)).setMinWidth(180);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(4)).setMinWidth(260);
        tabelaContaReceber.getColumn(modeloTabelaContaReceber.getCampo(5)).setMinWidth(110);
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
        iRetorno = lib.Bematech_DP_ImprimeCheque("555", valor, p.getFornecedor(), "ARMACAO DOS BUZIOS", DataUtil.getDateTime(p.getDataVencimento()).toString("ddMMyy"), "");
        System.out.println(iRetorno);

    }

    private void limparCampos() {
        txtHistorico.setText(fixarHistorico());
        txtConta.setText("");
        txtNumeroDocumento.setText("");
        txtValor.setText("");
        txtFornecedor.setText("");
//        cbFornecedores.setSelectedIndex(-1);
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtConta);
        campos.add(txtValor);
        campos.add(txtHistorico);
        return campos;
    }

//    private void carregarFornecedor() {
//        cbFornecedores.setModel(new ComboModelo<Fornecedor>(new DAO().listar(Fornecedor.class)));
//    }
    private List<Pagamento> getPagamentos() {
        pagamentos = new DAO().listar(Pagamento.class, "PagamentosContaPagarPorPeriodo", contaPagar, datInicio, datTermino);
        return pagamentos;
    }

    private List<Pagamento> getPagamentosContaReceber() {
        pagamentosContaReceber = new DAO().listar(Pagamento.class, "PagamentosContaReceberPorPeriodo", contaReceber, datInicio, datTermino);
        return pagamentosContaReceber;
    }

    private DadosTalaoCheque getDadosTalaoCheque() {
        List<DadosTalaoCheque> taloes = new DAO().listar("TaloesPorCondominio", condominio.getCodigo());
        if (!taloes.isEmpty()) {
            for (DadosTalaoCheque dados : taloes) {
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
//        if (cbFornecedores.getSelectedItem() == null) {
//            ApresentacaoUtil.exibirAdvertencia("Escolha um Fornecedor para esta conta a pagar!", this);
//            return;
//        }
        if (txtFornecedor.getText().isEmpty()) {
            ApresentacaoUtil.exibirAdvertencia("Informe um Fornecedor para esse pagamento!", this);
            return;
        }
        if (btnNumeroDocumento.isSelected() && painelContasPagarReceber.getSelectedIndex() == 0) {
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
        pagamento.setFornecedor(txtFornecedor.getText());
        if (!pagamento.getConta().isCredito()) {
            pagamento.setContaPagar(condominio.getContaPagar());
            pagamento = selecionaFormaPagamento(pagamento);
        } else if (pagamento.getConta().isCredito()) {
            pagamento.setContaReceber(condominio.getContaReceber());
            pagamento = selecionaFormaPagamentoContaReceber(pagamento);
        }
        if (pagamento.getForma() == FormaPagamento.CHEQUE && painelContasPagarReceber.getSelectedIndex() == 0) {
            cheques.add(pagamento);
            carregarTabelaCheque();
            limparCampos();
            txtNumeroDocumento.setText("");
            txtNumeroDocumento.grabFocus();
        } else {
            verificarDataPagamento(pagamento);
            if (!pagamento.getConta().isCredito()) {
                condominio.getContaPagar().adicionarPagamento(pagamento);
            } else if (pagamento.getConta().isCredito()) {
                condominio.getContaReceber().adicionarPagamento(pagamento);
            }
            new DAO().salvar(condominio);
            limparCampos();
        }
    }

    private Pagamento selecionaFormaPagamento(Pagamento p) {
        if (btnNumeroDocumento.isSelected()) {
            p.setForma(FormaPagamento.CHEQUE);
            if (p.getCodigo() == 0) {
                p.setDadosPagamento(new DadosCheque(txtNumeroDocumento.getText(), condominio.getContaBancaria().getContaCorrente(), condominio.getRazaoSocial()));
            } else {
                ((DadosCheque) p.getDadosPagamento()).setNumero(txtNumeroDocumento.getText());
            }
            for (Pagamento cheque : cheques) {
                if (((DadosCheque) cheque.getDadosPagamento()).getNumero().equals(((DadosCheque) p.getDadosPagamento()).getNumero())) {
                    p.setDadosPagamento(((DadosCheque) cheque.getDadosPagamento()));
                }
            }
            return p;
        } else {
            p.setForma(FormaPagamento.DINHEIRO);
            if (p.getCodigo() == 0) {
                p.setDadosPagamento(new DadosDOC(txtNumeroDocumento.getText()));
            } else {
                ((DadosDOC) p.getDadosPagamento()).setNumeroDocumento(txtNumeroDocumento.getText());
            }
            List<Pagamento> documentos = new DAO().listar("PagamentosPorForma", Main.getCondominio().getContaPagar(), FormaPagamento.DINHEIRO);
            for (Pagamento documento : documentos) {
                if (((DadosDOC) documento.getDadosPagamento()).getNumeroDocumento().equals(((DadosDOC) p.getDadosPagamento()).getNumeroDocumento())) {
                    p.setDadosPagamento(((DadosDOC) documento.getDadosPagamento()));
                }
            }
            return p;
        }

    }

    private Pagamento selecionaFormaPagamentoContaReceber(Pagamento p) {
        if (btnNumeroDocumento.isSelected()) {
            p.setForma(FormaPagamento.CHEQUE);
            p.setDadosPagamento(new DadosCheque(txtNumeroDocumento.getText(), " ", condominio.getRazaoSocial()));
            return p;
        } else {
            p.setForma(FormaPagamento.DINHEIRO);
            p.setDadosPagamento(new DadosDOC(txtNumeroDocumento.getText()));
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
        if (btnFixarHistorico.isSelected()) {
            texto = txtHistorico.getText();
            return texto;
        } else {
            return texto;
        }

    }

    private void pegarConta() {
        boolean exibirCredito = false;
        if (painelContasPagarReceber.getSelectedIndex() == 1) {
            exibirCredito = true;
        }
        DialogoConta c = new DialogoConta(null, true, exibirCredito, false, "");
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            if (!btnFixarHistorico.isSelected()) {
                txtHistorico.setText(conta.getNome());
            } else {
                txtHistorico.setText(fixarHistorico());
            }
        }
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    private void pegarFornecedor() {
        DialogoFornecedor f = new DialogoFornecedor(null, true, true);
        f.setVisible(true);

        if (f.getFornecedor() != null) {
            fornecedor = f.getFornecedor();
            txtFornecedor.setText(fornecedor.getNome());
        }
    }

    private void adicionarPagamento() {
        preencherPagamento();
    }

    private void apagarItensSelecionados() {
        if (painelContasPagarReceber.getSelectedIndex() == 0) {
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
        } else if (painelContasPagarReceber.getSelectedIndex() == 1) {
            if (modeloTabelaContaReceber.getObjetosSelecionados().isEmpty()) {
                ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
            } else if (ApresentacaoUtil.perguntar("Deseja remover os pagamentos?", this)) {
                List<Pagamento> itensRemover = modeloTabelaContaReceber.getObjetosSelecionados();

                for (Pagamento p : itensRemover) {
                    modeloTabelaContaReceber.remover(p);
                    modeloTabelaContaReceber.notificar();
                    new DAO().remover(itensRemover);
                    new DAO().salvar(contaReceber);
                }
                ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
            }
        }
    }

    private void trocarFormaPagamento() {
        if (btnNumeroDocumento.isSelected()) {
            btnNumeroDocumento.setText("Nº Cheque:");
            if (painelContasPagarReceber.getSelectedIndex() == 0) {
                txtNumeroDocumento.setText("");
                txtNumeroDocumento.grabFocus();
                painelCheques.setVisible(true);
                btnGravar.setEnabled(true);
                btnImprimir.setEnabled(true);
            } else if (painelContasPagarReceber.getSelectedIndex() == 1) {
                txtNumeroDocumento.setText("");
            }
        } else {
            btnNumeroDocumento.setText("Nº Doc:");
            btnGravar.setEnabled(false);
            btnImprimir.setEnabled(false);
            painelCheques.setVisible(false);
            txtNumeroDocumento.setText(Pagamento.gerarNumeroDocumento());
        }

    }

    private void editarPagamento() {
        if (painelContasPagarReceber.getSelectedIndex() == 0) {
            if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
                DialogoEditarContaPagar tela = new DialogoEditarContaPagar((Pagamento) modeloTabela.getObjetoSelecionado());
                tela.setLocationRelativeTo(this);
                tela.setVisible(true);
                modeloTabela.carregarObjetos();
            } else {
                ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
            }
        } else if (painelContasPagarReceber.getSelectedIndex() == 1) {
            if (!modeloTabelaContaReceber.getObjetosSelecionados().isEmpty()) {
                DialogoEditarContaReceber tela = new DialogoEditarContaReceber((Pagamento) modeloTabelaContaReceber.getObjetoSelecionado());
                tela.setLocationRelativeTo(this);
                tela.setVisible(true);
                modeloTabelaContaReceber.carregarObjetos();
            } else {
                ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
            }
        }
    }

    private void efetuarPagamento() {
        if (painelContasPagarReceber.getSelectedIndex() == 0) {
            if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
                DialogoPagarContaPagar tela = new DialogoPagarContaPagar((Pagamento) modeloTabela.getObjetoSelecionado());
                tela.setLocationRelativeTo(this);
                tela.setVisible(true);
                modeloTabela.carregarObjetos();
            } else {
                ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
            }
        } else if (painelContasPagarReceber.getSelectedIndex() == 1) {
            if (!modeloTabelaContaReceber.getObjetosSelecionados().isEmpty()) {
                DialogoReceberContaReceber tela = new DialogoReceberContaReceber((Pagamento) modeloTabelaContaReceber.getObjetoSelecionado());
                tela.setLocationRelativeTo(this);
                tela.setVisible(true);
                modeloTabelaContaReceber.carregarObjetos();
            } else {
                ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
            }
        }
    }

    private void verificarSaldoContaCorrente() {
        if (painelSaldoContaCorrente.isVisible()) {
            btnExibirSaldoCC.setToolTipText("Exibir Saldo Conta Corrente");
            painelSaldoContaCorrente.setVisible(false);
        } else {
            btnExibirSaldoCC.setToolTipText("Ocultar Saldo Conta Corrente");
            painelSaldoContaCorrente.setVisible(true);
            lblSaldoContaCorrente.setText(condominio.getContaCorrente().getSaldo().toString().replace(".", ","));
        }
    }

    private void desabilitarDateField() {
        dataInicio.setEnabled(false);
        dataTermino.setEnabled(false);
        btnHoje.setEnabled(true);
        btnSemana.setEnabled(true);
        btnMes.setEnabled(true);
    }

    private void habilitarDateField() {
        dataInicio.setEnabled(true);
        dataTermino.setEnabled(true);
        btnHoje.setEnabled(false);
        btnSemana.setEnabled(false);
        btnMes.setEnabled(false);
        dataInicio.setValue(DataUtil.getDate(DataUtil.getDateTime(DataUtil.getPrimeiroDiaMes())));
        dataTermino.setValue(DataUtil.getDate(DataUtil.getDateTime(DataUtil.getUltimoDiaMes())));
        preencherTabelaDateField();
    }

    private void getPeriodoSemanal() {
        Calendar dia = DataUtil.getCalendar(DataUtil.hoje());
        datInicio = DataUtil.getCalendar(DataUtil.hoje());
        datTermino = DataUtil.getCalendar(DataUtil.hoje());
        if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            datTermino = DataUtil.getCalendar(new DateTime(datTermino).plusDays(6));
        } else if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            datInicio = DataUtil.getCalendar(new DateTime(datInicio).minusDays(1));
            datTermino = DataUtil.getCalendar(new DateTime(datTermino).plusDays(5));
        } else if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            datInicio = DataUtil.getCalendar(new DateTime(datInicio).minusDays(2));
            datTermino = DataUtil.getCalendar(new DateTime(datTermino).plusDays(4));
        } else if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            datInicio = DataUtil.getCalendar(new DateTime(datInicio).minusDays(3));
            datTermino = DataUtil.getCalendar(new DateTime(datTermino).plusDays(3));
        } else if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            datInicio = DataUtil.getCalendar(new DateTime(datInicio).minusDays(4));
            datTermino = DataUtil.getCalendar(new DateTime(datTermino).plusDays(2));
        } else if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            datInicio = DataUtil.getCalendar(new DateTime(datInicio).minusDays(5));
            datTermino = DataUtil.getCalendar(new DateTime(datTermino).plusDays(1));
        } else if (dia.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            datInicio = DataUtil.getCalendar(new DateTime(datInicio).minusDays(6));
        }

    }

    private void preencherTabelaDateField() {
        datInicio = DataUtil.getCalendar(dataInicio.getValue());
        datTermino = DataUtil.getCalendar(dataTermino.getValue());
        modeloTabela.carregarObjetos();
        modeloTabelaContaReceber.carregarObjetos();
    }

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
            } else if (origem == btnFornecedor) {
                pegarFornecedor();
            } else if (origem == btnIncluir) {
                adicionarPagamento();
                if (painelContasPagarReceber.getSelectedIndex() == 0) {
                    carregarTabela();
                } else if (painelContasPagarReceber.getSelectedIndex() == 1) {
                    carregarTabelaContaReceber();
                }
            } else if (origem == itemMenuApagarSelecionados || origem == itemMenuApagarSelecionadosReceber) {
                apagarItensSelecionados();
            } else if (origem == itemMenuPagarSelecionados || origem == itemMenuPagarSelecionadosReceber) {
                efetuarPagamento();
            } else if (origem == btnFixarHistorico) {
                fixarHistorico();
            } else if (origem == btnNumeroDocumento) {
                trocarFormaPagamento();
            } else if (origem == btnGravar) {
                gravarCheques();
            } else if (origem == btnImprimir) {
                imprimirCheques();
            } else if (origem == itemMenuEditarPagamento || origem == itemMenuEditarPagamentoReceber) {
                editarPagamento();
            } else if (origem == btnExibirSaldoCC) {
                verificarSaldoContaCorrente();
            } else if (origem == checkBoxMostrarDateField) {
                if (checkBoxMostrarDateField.isSelected()) {
                    habilitarDateField();
                } else {
                    desabilitarDateField();
                }
            } else if (origem == btnHoje) {
                datInicio = DataUtil.getCalendar(DataUtil.hoje());
                datTermino = DataUtil.getCalendar(DataUtil.hoje());
                modeloTabela.carregarObjetos();
                modeloTabelaContaReceber.carregarObjetos();
            } else if (origem == btnSemana) {
                getPeriodoSemanal();
                modeloTabela.carregarObjetos();
                modeloTabelaContaReceber.carregarObjetos();
            } else if (origem == btnMes) {
                datInicio = DataUtil.getCalendar(DataUtil.getPrimeiroDiaMes());
                datTermino = DataUtil.getCalendar(DataUtil.getUltimoDiaMes());
                modeloTabela.carregarObjetos();
                modeloTabelaContaReceber.carregarObjetos();
            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaContaPagar.this, JTextField.class);

            btnConta.addActionListener(this);
            btnFornecedor.addActionListener(this);
            btnFixarHistorico.addActionListener(this);
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
            btnExibirSaldoCC.addActionListener(this);
            painelContasPagarReceber.addChangeListener(this);
            tabelaContaReceber.addMouseListener(this);
            itemMenuApagarSelecionadosReceber.addActionListener(this);
            itemMenuPagarSelecionadosReceber.addActionListener(this);
            itemMenuEditarPagamentoReceber.addActionListener(this);
            checkBoxMostrarDateField.addActionListener(this);
            btnHoje.addActionListener(this);
            btnSemana.addActionListener(this);
            btnMes.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                System.out.println("released");
                if (e.getSource() == tabelaContaPagar) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaContaReceber) {
                    popupMenuReceber.show(e.getComponent(), e.getX(), e.getY());
                }
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
                preencherTabelaDateField();
            } else if (source == painelContasPagarReceber && painelContasPagarReceber.getSelectedIndex() == 0 && btnNumeroDocumento.isSelected()) {
                btnGravar.setEnabled(true);
                btnImprimir.setEnabled(true);
            } else if (source == painelContasPagarReceber && painelContasPagarReceber.getSelectedIndex() == 1) {
                btnGravar.setEnabled(false);
                btnImprimir.setEnabled(false);

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
        popupMenuReceber = new javax.swing.JPopupMenu();
        itemMenuApagarSelecionadosReceber = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuPagarSelecionadosReceber = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        itemMenuEditarPagamentoReceber = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        btnIncluir = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtData = new net.sf.nachocalendar.components.DateField();
        txtValor = new javax.swing.JTextField();
        txtConta = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        btnFixarHistorico = new javax.swing.JToggleButton();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnNumeroDocumento = new javax.swing.JToggleButton();
        btnImprimir = new javax.swing.JButton();
        btnGravar = new javax.swing.JButton();
        btnExibirSaldoCC = new javax.swing.JButton();
        btnFornecedor = new javax.swing.JButton();
        txtFornecedor = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        dataTermino = new net.sf.nachocalendar.components.DateField();
        jLabel4 = new javax.swing.JLabel();
        dataInicio = new net.sf.nachocalendar.components.DateField();
        checkBoxMostrarDateField = new javax.swing.JCheckBox();
        btnHoje = new javax.swing.JButton();
        btnMes = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        btnSemana = new javax.swing.JButton();
        painelSaldoContaCorrente = new javax.swing.JPanel();
        lblTextoSaldo = new javax.swing.JLabel();
        lblSaldoContaCorrente = new javax.swing.JLabel();
        painelContasPagarReceber = new javax.swing.JTabbedPane();
        painelContaPagar = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContaPagar = new javax.swing.JTable();
        painelCheques = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaCheque = new javax.swing.JTable();
        painelContaReceber = new javax.swing.JPanel();
        painel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaContaReceber = new javax.swing.JTable();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        itemMenuPagarSelecionados.setText("Pagar Selecionado");
        itemMenuPagarSelecionados.setToolTipText("Efetua Pagamento dos Itens Selecionados");
        popupMenu.add(itemMenuPagarSelecionados);

        itemMenuEditarPagamento.setText("Editar Pagamento");
        itemMenuEditarPagamento.setToolTipText("Editar um Pagamento Selecionado");
        popupMenu.add(itemMenuEditarPagamento);

        itemMenuApagarSelecionadosReceber.setText("Apagar Selecionados");
        popupMenuReceber.add(itemMenuApagarSelecionadosReceber);
        popupMenuReceber.add(jSeparator1);

        itemMenuPagarSelecionadosReceber.setText("Receber Selecionado");
        popupMenuReceber.add(itemMenuPagarSelecionadosReceber);
        popupMenuReceber.add(jSeparator2);

        itemMenuEditarPagamentoReceber.setText("Editar Selecionados");
        popupMenuReceber.add(itemMenuEditarPagamentoReceber);

        setClosable(true);
        setTitle("Contas a Pagar/Receber");
        setPreferredSize(new java.awt.Dimension(878, 626));

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

        btnFixarHistorico.setText("Fixar Histórico");

        btnNumeroDocumento.setText("Nº Doc");
        btnNumeroDocumento.setToolTipText("Clique para alternar o tipo de Registro!");
        btnNumeroDocumento.setBorderPainted(false);
        btnNumeroDocumento.setContentAreaFilled(false);
        btnNumeroDocumento.setFocusPainted(false);

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Cheque");

        btnGravar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/buscar.gif"))); // NOI18N
        btnGravar.setToolTipText("Gravar Cheques");

        btnExibirSaldoCC.setText("Saldo C/C");

        btnFornecedor.setText("Selecionar Fornecedor/Beneficiário:");
        btnFornecedor.setBorder(null);
        btnFornecedor.setBorderPainted(false);
        btnFornecedor.setContentAreaFilled(false);
        btnFornecedor.setFocusable(false);
        btnFornecedor.setRequestFocusEnabled(false);
        btnFornecedor.setVerifyInputWhenFocusTarget(false);

        txtFornecedor.setName("Histórico"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnFixarHistorico)
                        .addGap(37, 37, 37)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnGravar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(btnExibirSaldoCC))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(71, 71, 71)
                                .addComponent(btnNumeroDocumento)))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnConta)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnFornecedor))))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConta)))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(btnFornecedor)))
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
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnFixarHistorico, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addComponent(btnIncluir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnImprimir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnGravar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnExibirSaldoCC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnFixarHistorico, btnGravar, btnImprimir, btnIncluir});

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Data Inicial:");

        dataTermino.setEnabled(false);

        jLabel4.setText("Data Final:");

        dataInicio.setEnabled(false);

        btnHoje.setText("Hoje");

        btnMes.setText("Mês Atual");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Filtrar por:");

        btnSemana.setText("Semana");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnHoje)
                .addGap(18, 18, 18)
                .addComponent(btnSemana)
                .addGap(18, 18, 18)
                .addComponent(btnMes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                .addComponent(checkBoxMostrarDateField)
                .addGap(12, 12, 12)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataTermino, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(4, 4, 4))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dataTermino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(4, 4, 4))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(btnHoje)
                            .addComponent(btnSemana)
                            .addComponent(btnMes))
                        .addComponent(checkBoxMostrarDateField)))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        lblTextoSaldo.setFont(new java.awt.Font("Tahoma", 0, 14));
        lblTextoSaldo.setText("Saldo Cta. Corrente: R$");

        lblSaldoContaCorrente.setFont(new java.awt.Font("Tahoma", 1, 14));
        lblSaldoContaCorrente.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSaldoContaCorrente.setText("0,00");

        javax.swing.GroupLayout painelSaldoContaCorrenteLayout = new javax.swing.GroupLayout(painelSaldoContaCorrente);
        painelSaldoContaCorrente.setLayout(painelSaldoContaCorrenteLayout);
        painelSaldoContaCorrenteLayout.setHorizontalGroup(
            painelSaldoContaCorrenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelSaldoContaCorrenteLayout.createSequentialGroup()
                .addContainerGap(562, Short.MAX_VALUE)
                .addComponent(lblTextoSaldo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSaldoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelSaldoContaCorrenteLayout.setVerticalGroup(
            painelSaldoContaCorrenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelSaldoContaCorrenteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelSaldoContaCorrenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSaldoContaCorrente)
                    .addComponent(lblTextoSaldo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaContaPagar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaPagar);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addContainerGap())
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelChequesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelChequesLayout.setVerticalGroup(
            painelChequesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelChequesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout painelContaPagarLayout = new javax.swing.GroupLayout(painelContaPagar);
        painelContaPagar.setLayout(painelContaPagarLayout);
        painelContaPagarLayout.setHorizontalGroup(
            painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelContaPagarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(painelCheques, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelContaPagarLayout.setVerticalGroup(
            painelContaPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContaPagarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelCheques, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelContasPagarReceber.addTab("Contas a Pagar", painelContaPagar);

        tabelaContaReceber.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tabelaContaReceber);

        javax.swing.GroupLayout painel3Layout = new javax.swing.GroupLayout(painel3);
        painel3.setLayout(painel3Layout);
        painel3Layout.setHorizontalGroup(
            painel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                .addContainerGap())
        );
        painel3Layout.setVerticalGroup(
            painel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout painelContaReceberLayout = new javax.swing.GroupLayout(painelContaReceber);
        painelContaReceber.setLayout(painelContaReceberLayout);
        painelContaReceberLayout.setHorizontalGroup(
            painelContaReceberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 836, Short.MAX_VALUE)
            .addGroup(painelContaReceberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelContaReceberLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(painel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        painelContaReceberLayout.setVerticalGroup(
            painelContaReceberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 317, Short.MAX_VALUE)
            .addGroup(painelContaReceberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelContaReceberLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(painel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        painelContasPagarReceber.addTab("Contas a Receber", painelContaReceber);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelSaldoContaCorrente, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelContasPagarReceber, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelContasPagarReceber, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelSaldoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnExibirSaldoCC;
    private javax.swing.JToggleButton btnFixarHistorico;
    private javax.swing.JButton btnFornecedor;
    private javax.swing.JButton btnGravar;
    private javax.swing.JButton btnHoje;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnMes;
    private javax.swing.JToggleButton btnNumeroDocumento;
    private javax.swing.JButton btnSemana;
    private javax.swing.JCheckBox checkBoxMostrarDateField;
    private net.sf.nachocalendar.components.DateField dataInicio;
    private net.sf.nachocalendar.components.DateField dataTermino;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JMenuItem itemMenuApagarSelecionadosReceber;
    private javax.swing.JMenuItem itemMenuEditarPagamento;
    private javax.swing.JMenuItem itemMenuEditarPagamentoReceber;
    private javax.swing.JMenuItem itemMenuPagarSelecionados;
    private javax.swing.JMenuItem itemMenuPagarSelecionadosReceber;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel lblSaldoContaCorrente;
    private javax.swing.JLabel lblTextoSaldo;
    private javax.swing.JPanel painel3;
    private javax.swing.JPanel painelCheques;
    private javax.swing.JPanel painelContaPagar;
    private javax.swing.JPanel painelContaReceber;
    private javax.swing.JTabbedPane painelContasPagarReceber;
    private javax.swing.JPanel painelSaldoContaCorrente;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JPopupMenu popupMenuReceber;
    private javax.swing.JTable tabelaCheque;
    private javax.swing.JTable tabelaContaPagar;
    private javax.swing.JTable tabelaContaReceber;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private javax.swing.JTextField txtFornecedor;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroDocumento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
