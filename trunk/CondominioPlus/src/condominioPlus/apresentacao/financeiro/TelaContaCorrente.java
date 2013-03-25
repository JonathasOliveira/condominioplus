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

import condominioPlus.apresentacao.cobranca.DialogoDadosRelatorioGerencial;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.ContaCorrente;
import condominioPlus.negocio.financeiro.DadosBoleto;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.util.Relatorios;
import condominioPlus.util.RenderizadorCelulaCor;
import condominioPlus.util.RenderizadorCelulaCorData;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import logicpoint.util.Util;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class TelaContaCorrente extends javax.swing.JInternalFrame {

    private ContaCorrente contaCorrente;
    private Condominio condominio;
    private TabelaModelo_2 modeloTabela;
    private List<Pagamento> pagamentos;
    private RenderizadorCelulaCor renderizadorCelulaCor;
    private DateTime dataInicial;
    private DateTime dataFinal;

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

        painelSaldos.setVisible(false);
        calcularSaldo();

        carregarTabela();

        carregarComboFiltro();

        verificarListaVisualizacao();

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
                        return getFormaPagamento(pagamento);
                    case 2:
                        return pagamento.getConta().getCodigo();
                    case 3:
                        return pagamento.getHistorico().toUpperCase();
                    case 4:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(pagamento.getSaldo().doubleValue());
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

        renderizadorCelulaCor = new RenderizadorCelulaCor(modeloTabela);
        RenderizadorCelulaCorData renderizadorCelula = new RenderizadorCelulaCorData(modeloTabela);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(0)).setCellRenderer(renderizadorCelula);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(1)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(2)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setCellRenderer(renderizadorCelulaCor);


        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setMinWidth(300);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);

    }

    private String getFormaPagamento(Pagamento p) {
        if (p.getForma() == FormaPagamento.BOLETO) {
            return ((DadosBoleto) p.getDadosPagamento()).getNumeroBoleto();
        } else if (p.getForma() == FormaPagamento.CHEQUE) {
            return String.valueOf(((DadosCheque) p.getDadosPagamento()).getNumero());
        } else {
            return String.valueOf(((DadosDOC) p.getDadosPagamento()).getNumeroDocumento());
        }
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

    private List<Pagamento> getPagamentos() {
        if (dataInicial != null && dataFinal != null) {
            pagamentos = new DAO().listar("PagamentosPorPeriodoContaCorrente", condominio.getContaCorrente(), DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
        } else if (dataInicial == null && dataFinal == null) {
            pagamentos = new DAO().listar("PagamentosContaCorrente", condominio.getContaCorrente());
        }
        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();
        Collections.sort(pagamentos, comCod);
        ComparatorPagamento comparator = new ComparatorPagamento();
        Collections.sort(pagamentos, comparator);
        return pagamentos;
    }

    private void verificarLista() {
        if (condominio.getContaCorrente().getPagamentos().size() == 1) {
            for (Pagamento p : getPagamentos()) {
                p.setSaldo(p.getValor());
                condominio.getContaCorrente().setSaldo(p.getValor());

            }
        }
    }

    private void calcularSaldo() {
        verificarLista();
        contaCorrente.calculaSaldo(contaCorrente);
        carregarTabela();
    }

    private void preencherTelaComSaldos() {
        if (btnVisualizarSaldos.isSelected()) {
            painelSaldos.setVisible(true);
            txtSaldoAnteriorContaCorrente.setText(String.valueOf(pegarSaldoAnterior().toString()));
            txtCreditosContaCorrente.setText(pegarCreditoDoMes().toString());
            txtDebitosContaCorrente.setText(pegarDebitoDoMes().toString());
            txtSaldoPoupanca.setText(new Moeda(condominio.getPoupanca().getSaldo()).toString());
            txtSaldoEmprestimo.setText(new Moeda(condominio.getEmprestimo().getSaldo()).toString());
            txtSaldoConsignacao.setText(new Moeda(condominio.getConsignacao().getSaldo()).toString());
            txtSaldoAplicacao.setText(new Moeda(condominio.getAplicacao().getSaldo()).toString());
            txtSaldoContaCorrente.setText(new Moeda(condominio.getContaCorrente().getSaldo()).toString());
            txtSaldoExtrato.setText(pegarSaldoFinalExtrato().toString());
            txtSaldoAnteriorExtratoBancario.setText(getExtratoMensal().toString());
            txtCreditosExtratoBancario.setText(pegarCreditoExtratoDoMes().toString());
            txtDebitosExtratoBancario.setText(pegarDebitoExtratoDoMes().toString());
        } else {
            painelSaldos.setVisible(false);
        }

    }

    private Moeda pegarSaldoAnterior() {
        List<Pagamento> pagamentosUltimoDia = new DAO().listar("PagamentosDoDia", condominio.getContaCorrente(), DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes()).minusMonths(1)), DataUtil.getCalendar(new DateTime(DataUtil.getUltimoDiaMes()).minusMonths(1)));
        Moeda saldo = new Moeda();
        if (!pagamentosUltimoDia.isEmpty()) {
            ComparadorPagamentoCodigo c = new ComparadorPagamentoCodigo();

            Collections.sort(pagamentosUltimoDia, c);

            ComparatorPagamento c2 = new ComparatorPagamento();

            Collections.sort(pagamentosUltimoDia, c2);


            saldo = new Moeda(pagamentosUltimoDia.get(pagamentosUltimoDia.size() - 1).getSaldo());
        }


        return saldo;


    }

    private Moeda pegarCreditoDoMes() {
        List<Pagamento> pagamentosUltimoDia = new DAO().listar("PagamentosDoMes", condominio.getContaCorrente(), DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes())), DataUtil.getCalendar(new DateTime(DataUtil.getUltimoDiaMes())));
        Moeda total = new Moeda(BigDecimal.ZERO);

        for (Pagamento p : pagamentosUltimoDia) {
            System.out.println("pagamento sem comparator " + DataUtil.toString(p.getDataPagamento()) + " " + p.getHistorico());
            if (p.getConta().isCredito()) {
                total.soma(p.getValor());
            }
        }
        return new Moeda(total);


    }

    private Moeda pegarDebitoDoMes() {
        List<Pagamento> pagamentosUltimoDia = new DAO().listar("PagamentosDoMes", condominio.getContaCorrente(), DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes())), DataUtil.getCalendar(new DateTime(DataUtil.getUltimoDiaMes())));
        Moeda total = new Moeda(BigDecimal.ZERO);

        for (Pagamento p : pagamentosUltimoDia) {
            System.out.println("pagamento sem comparator " + DataUtil.toString(p.getDataPagamento()) + " " + p.getHistorico());
            if (!p.getConta().isCredito()) {
                total.soma(p.getValor());
            }
        }
        return new Moeda(total);
    }

    private Moeda pegarSaldoFinalExtrato() {
        List<ExtratoBancario> listaExtratos = new DAO().listar("ExtratoSaldoFinal", condominio, 2);
        Moeda saldoFinal = new Moeda(obterSaldoFinal(listaExtratos));
        return saldoFinal;
    }

    private Moeda obterSaldoFinal(List<ExtratoBancario> lista) {
        Moeda valor = new Moeda(BigDecimal.ZERO);

        if (!lista.isEmpty()) {
            valor.soma(lista.get(lista.size() - 1).getValor());
        }

        return valor;
    }

    private Calendar pegarUltimoDiaUtilDoMes(Calendar dia) {

        switch (dia.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return DataUtil.getCalendar(new DateTime(dia).minusDays(2));
            case Calendar.SATURDAY:
                return DataUtil.getCalendar(new DateTime(dia).minusDays(1));
            default:
                return dia;
        }

    }

    private Moeda getExtratoMensal() {
        Calendar dtaInicial = pegarUltimoDiaUtilDoMes(DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes()).minusDays(1)));
        List<ExtratoBancario> listaExtratoMensal = new DAO().listar("ExtratosPorDia", condominio, dtaInicial);

        Moeda saldo = new Moeda(0);
        if (listaExtratoMensal.size() >= 1) {
            saldo = new Moeda(listaExtratoMensal.get(listaExtratoMensal.size() - 1).getValor());
        }
        return saldo;
    }

    private Moeda pegarCreditoExtratoDoMes() {
        List<ExtratoBancario> extratosUltimoDia = new DAO().listar("ExtratosPorMes", condominio, DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes())), DataUtil.getCalendar(new DateTime(DataUtil.getUltimoDiaMes())));
        Moeda total = new Moeda(BigDecimal.ZERO);

        for (ExtratoBancario e : extratosUltimoDia) {
            if (e.getTipo().equalsIgnoreCase("C")) {
                total.soma(e.getValor());
            }
        }
        return new Moeda(total);


    }

    private Moeda pegarDebitoExtratoDoMes() {
        List<ExtratoBancario> extratosUltimoDia = new DAO().listar("ExtratosPorMes", condominio, DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes())), DataUtil.getCalendar(new DateTime(DataUtil.getUltimoDiaMes())));
        Moeda total = new Moeda(BigDecimal.ZERO);

        for (ExtratoBancario e : extratosUltimoDia) {
            if (e.getTipo().equalsIgnoreCase("D")) {
                total.soma(e.getValor());
            }
        }
        return new Moeda(total);


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
            calcularSaldo();
            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void editarPagamento() {
        if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
            DialogoEditarPagamentoContaCorrente tela = new DialogoEditarPagamentoContaCorrente((Pagamento) modeloTabela.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloTabela.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
        }
    }

    public void imprimirExtrato(TipoRelatorio tipo) {
        DialogoDadosRelatorioGerencial dialogo = new DialogoDadosRelatorioGerencial(null, true, dataInicial, dataFinal, tipo);
        dialogo.setVisible(true);

        if (dialogo.getDataInicial() != null && dialogo.getDataFinal() != null) {
            List<Pagamento> listaPagamentos = new DAO().listar("PagamentosPorPeriodoContaCorrente", condominio.getContaCorrente(), DataUtil.getCalendar(dialogo.getDataInicial()), DataUtil.getCalendar(dialogo.getDataFinal()));

            ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();
            Collections.sort(listaPagamentos, comCod);
            ComparatorPagamento comparator = new ComparatorPagamento();
            Collections.sort(listaPagamentos, comparator);

            if (tipo == TipoRelatorio.EXTRATO_CONTA_CORRENTE) {
                new Relatorios().imprimirExtratoContaCorrente(condominio, dialogo.getDataInicial(), dialogo.getDataFinal(), listaPagamentos);
            } else if (tipo == TipoRelatorio.EXTRATO_CONFERENCIA_CONTA_CORRENTE) {
                new Relatorios().imprimirExtratoConferenciaContaCorrente(condominio, dialogo.getDataInicial(), dialogo.getDataFinal(), listaPagamentos);
            } else if (tipo == TipoRelatorio.BALANCETE_SINTETICO || tipo == TipoRelatorio.BALANCETE_ANALITICO){
                new Relatorios().imprimirBalancete(condominio, dialogo.getDataInicial(), dialogo.getDataFinal(), listaPagamentos, tipo);
            }
        }
    }
    
    public void imprimirRecibo(){
        new Relatorios().imprimirRecibo(condominio, (Pagamento)modeloTabela.getObjetoSelecionado());
    }

    public void verificarListaVisualizacao() {
        if (radioTodos.isSelected()) {
            txtDataInicial.setEnabled(false);
            txtDataFinal.setEnabled(false);
            dataInicial = null;
            dataFinal = null;
        } else if (radioPeriodo.isSelected()) {
            txtDataInicial.setEnabled(true);
            txtDataFinal.setEnabled(true);
            dataInicial = DataUtil.getDateTime(txtDataInicial.getValue());
            dataFinal = DataUtil.getDateTime(txtDataFinal.getValue());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private class ControladorEventos extends ControladorEventosGenerico {

        int contador;
        Object origem;

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == itemMenuApagarSelecionados) {
                apagarItensSelecionados();

            } else if (origem == btnVisualizarSaldos) {
                preencherTelaComSaldos();
            } else if (origem == itemMenuEditarPagamento) {
                editarPagamento();
            } else if (origem == radioPeriodo || origem == radioTodos) {
                if (origem == radioPeriodo) {
                    Calendar dat1 = Calendar.getInstance();
                    dat1.setTime(DataUtil.getDate(DataUtil.hoje()));
                    dat1.add(Calendar.MONTH, -1);
                    txtDataInicial.setValue(DataUtil.getDate(DataUtil.getPrimeiroDiaMes(DataUtil.getDateTime(dat1))));
                    txtDataFinal.setValue(DataUtil.getDate(DataUtil.getUltimoDiaMes(DataUtil.getDateTime(dat1))));
                }
                verificarListaVisualizacao();
                carregarTabela();
            } else if (origem == itemMenuImprimirExtrato) {
                imprimirExtrato(TipoRelatorio.EXTRATO_CONTA_CORRENTE);
            } else if (origem == itemMenuImprimirExtratoConferencia) {
                imprimirExtrato(TipoRelatorio.EXTRATO_CONFERENCIA_CONTA_CORRENTE);
            } else if (origem == itemMenuImprimirBalanceteAnalitico){
                imprimirExtrato(TipoRelatorio.BALANCETE_ANALITICO);
            } else if (origem == itemMenuImprimirBalanceteSintetico){
                imprimirExtrato(TipoRelatorio.BALANCETE_SINTETICO);
            } else if (origem == itemMenuImprimirRecibo){
                imprimirRecibo();
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

            cbFiltros.addItemListener(this);
            tabelaContaCorrente.addMouseListener(this);
            itemMenuApagarSelecionados.addActionListener(this);
            btnVisualizarSaldos.addActionListener(this);
            itemMenuEditarPagamento.addActionListener(this);
            radioPeriodo.addActionListener(this);
            radioTodos.addActionListener(this);
            txtDataInicial.addChangeListener(this);
            txtDataFinal.addChangeListener(this);
            itemMenuImprimirExtrato.addActionListener(this);
            itemMenuImprimirExtratoConferencia.addActionListener(this);
            itemMenuImprimirBalanceteSintetico.addActionListener(this);
            itemMenuImprimirBalanceteAnalitico.addActionListener(this);
            itemMenuImprimirRecibo.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            origem = e.getSource();
            if (e.getSource() == txtDataInicial || e.getSource() == txtDataFinal) {
                ApresentacaoUtil.verificarDatas(e.getSource(), txtDataInicial, txtDataFinal, this);
                verificarListaVisualizacao();
                carregarTabela();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuApagarSelecionados = new javax.swing.JMenuItem();
        itemMenuEditarPagamento = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuImprimirExtrato = new javax.swing.JMenuItem();
        itemMenuImprimirExtratoConferencia = new javax.swing.JMenuItem();
        itemMenuImprimirBalanceteSintetico = new javax.swing.JMenuItem();
        itemMenuImprimirBalanceteAnalitico = new javax.swing.JMenuItem();
        itemMenuImprimirRecibo = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContaCorrente = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        cbFiltros = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        btnVisualizarSaldos = new javax.swing.JToggleButton();
        radioTodos = new javax.swing.JRadioButton();
        radioPeriodo = new javax.swing.JRadioButton();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        jLabel18 = new javax.swing.JLabel();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        painelSaldos = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtSaldoAnteriorContaCorrente = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCreditosContaCorrente = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtDebitosContaCorrente = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtSaldoPoupanca = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtSaldoAplicacao = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtSaldoConsignacao = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtSaldoEmprestimo = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtSaldoAnteriorExtratoBancario = new javax.swing.JTextField();
        txtCreditosExtratoBancario = new javax.swing.JTextField();
        txtDebitosExtratoBancario = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtSaldoContaCorrente = new javax.swing.JTextField();
        txtSaldoExtrato = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        itemMenuEditarPagamento.setText("Editar Pagamento Selecionado");
        popupMenu.add(itemMenuEditarPagamento);
        popupMenu.add(jSeparator1);

        itemMenuImprimirExtrato.setText("Imprimir Extrato Conta Corrente");
        popupMenu.add(itemMenuImprimirExtrato);

        itemMenuImprimirExtratoConferencia.setText("Imprimir Extrato Conferência Conta Corrente");
        popupMenu.add(itemMenuImprimirExtratoConferencia);

        itemMenuImprimirBalanceteSintetico.setText("Imprimir Balancete Sintético");
        popupMenu.add(itemMenuImprimirBalanceteSintetico);

        itemMenuImprimirBalanceteAnalitico.setText("Imprimir Balancete Analítico");
        popupMenu.add(itemMenuImprimirBalanceteAnalitico);

        itemMenuImprimirRecibo.setText("Imprimir 2ª Via Recibo");
        popupMenu.add(itemMenuImprimirRecibo);

        setClosable(true);
        setTitle("Conta Corrente");

        tabelaContaCorrente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaCorrente);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Filtrar por:");

        btnVisualizarSaldos.setText("Visualizar Saldos");
        btnVisualizarSaldos.setToolTipText("Visualizar Saldos");

        buttonGroup1.add(radioTodos);
        radioTodos.setSelected(true);
        radioTodos.setText("Mostrar Todos");

        buttonGroup1.add(radioPeriodo);
        radioPeriodo.setText("Período");

        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("a");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnVisualizarSaldos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                .addComponent(radioTodos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(radioPeriodo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(radioTodos)
                        .addComponent(radioPeriodo))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel18)
                        .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(cbFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnVisualizarSaldos)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelSaldos.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setBackground(new java.awt.Color(0, 102, 102));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Resumo da Conta Corrente:");
        jLabel1.setAlignmentX(1.0F);
        jLabel1.setOpaque(true);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel2.setText("Saldo Anterior");

        txtSaldoAnteriorContaCorrente.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoAnteriorContaCorrente.setEditable(false);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel3.setText("Créditos");

        txtCreditosContaCorrente.setBackground(new java.awt.Color(204, 204, 204));
        txtCreditosContaCorrente.setEditable(false);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel4.setText("Débitos");

        txtDebitosContaCorrente.setBackground(new java.awt.Color(204, 204, 204));
        txtDebitosContaCorrente.setEditable(false);

        jLabel5.setBackground(new java.awt.Color(0, 102, 102));
        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Saldos de Aplicações, Empréstimos e Outros:");
        jLabel5.setOpaque(true);

        txtSaldoPoupanca.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoPoupanca.setEditable(false);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel7.setText("Poupança");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel8.setText("Aplicações");

        txtSaldoAplicacao.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoAplicacao.setEditable(false);
        txtSaldoAplicacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSaldoAplicacaoActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel9.setText("Consignações");

        txtSaldoConsignacao.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoConsignacao.setEditable(false);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel10.setText("Empréstimos");

        txtSaldoEmprestimo.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoEmprestimo.setEditable(false);

        jLabel11.setBackground(new java.awt.Color(0, 102, 102));
        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Resumo Extrato Bancário");
        jLabel11.setOpaque(true);

        txtSaldoAnteriorExtratoBancario.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoAnteriorExtratoBancario.setEditable(false);

        txtCreditosExtratoBancario.setBackground(new java.awt.Color(204, 204, 204));
        txtCreditosExtratoBancario.setEditable(false);

        txtDebitosExtratoBancario.setBackground(new java.awt.Color(204, 204, 204));
        txtDebitosExtratoBancario.setEditable(false);

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel12.setText("Saldo Anterior");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel13.setText("Créditos");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel14.setText("Débitos");

        jLabel15.setBackground(new java.awt.Color(0, 102, 102));
        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Saldos Atuais");
        jLabel15.setOpaque(true);

        txtSaldoContaCorrente.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoContaCorrente.setEditable(false);

        txtSaldoExtrato.setBackground(new java.awt.Color(204, 204, 204));
        txtSaldoExtrato.setEditable(false);

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel16.setText("Caixa");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel17.setText("Extrato");

        javax.swing.GroupLayout painelSaldosLayout = new javax.swing.GroupLayout(painelSaldos);
        painelSaldos.setLayout(painelSaldosLayout);
        painelSaldosLayout.setHorizontalGroup(
            painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelSaldosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addGroup(painelSaldosLayout.createSequentialGroup()
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(txtSaldoAnteriorContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCreditosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtDebitosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)))
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelSaldosLayout.createSequentialGroup()
                                        .addComponent(txtSaldoPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtSaldoAplicacao, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(painelSaldosLayout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addGap(51, 51, 51)
                                        .addComponent(jLabel8)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelSaldosLayout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addComponent(txtSaldoConsignacao))))
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel10))
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSaldoEmprestimo, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))))
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(113, 113, 113)
                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(painelSaldosLayout.createSequentialGroup()
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSaldoAnteriorExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(txtCreditosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDebitosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)))
                    .addComponent(jLabel15)
                    .addGroup(painelSaldosLayout.createSequentialGroup()
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(89, 89, 89))
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addComponent(txtSaldoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(txtSaldoExtrato, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(45, 45, 45))
        );
        painelSaldosLayout.setVerticalGroup(
            painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelSaldosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelSaldosLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSaldoAnteriorExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCreditosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDebitosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSaldoExtrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .addGroup(painelSaldosLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSaldoAnteriorContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelSaldosLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtCreditosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDebitosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelSaldosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSaldoPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoAplicacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoConsignacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoEmprestimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelSaldos, javax.swing.GroupLayout.Alignment.LEADING, 0, 843, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 841, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(painelSaldos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSaldoAplicacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSaldoAplicacaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSaldoAplicacaoActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnVisualizarSaldos;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbFiltros;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JMenuItem itemMenuEditarPagamento;
    private javax.swing.JMenuItem itemMenuImprimirBalanceteAnalitico;
    private javax.swing.JMenuItem itemMenuImprimirBalanceteSintetico;
    private javax.swing.JMenuItem itemMenuImprimirExtrato;
    private javax.swing.JMenuItem itemMenuImprimirExtratoConferencia;
    private javax.swing.JMenuItem itemMenuImprimirRecibo;
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPanel painelSaldos;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JRadioButton radioPeriodo;
    private javax.swing.JRadioButton radioTodos;
    private javax.swing.JTable tabelaContaCorrente;
    private javax.swing.JTextField txtCreditosContaCorrente;
    private javax.swing.JTextField txtCreditosExtratoBancario;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtDebitosContaCorrente;
    private javax.swing.JTextField txtDebitosExtratoBancario;
    private javax.swing.JTextField txtSaldoAnteriorContaCorrente;
    private javax.swing.JTextField txtSaldoAnteriorExtratoBancario;
    private javax.swing.JTextField txtSaldoAplicacao;
    private javax.swing.JTextField txtSaldoConsignacao;
    private javax.swing.JTextField txtSaldoContaCorrente;
    private javax.swing.JTextField txtSaldoEmprestimo;
    private javax.swing.JTextField txtSaldoExtrato;
    private javax.swing.JTextField txtSaldoPoupanca;
    // End of variables declaration//GEN-END:variables
}
