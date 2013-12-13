/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaOrcamento.java
 *
 * Created on 04/05/2011, 13:52:07
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaOrcamentaria;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.util.Relatorios;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author eugenia
 */
public class TelaOrcamento extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private Calendar datInicio = DataUtil.getCalendar(DataUtil.hoje());
    private Calendar datTermino = DataUtil.getCalendar(DataUtil.hoje());
    private TabelaModelo_2<ContaOrcamentaria> modeloTabelaContaOrcamentaria;
    private List<ContaOrcamentaria> contasOrcamentarias = new ArrayList<ContaOrcamentaria>();
    private TabelaModelo_2<ContaOrcamentaria> modeloTabelaContasExtraordinarias;
    private List<ContaOrcamentaria> contasExtraordinarias = new ArrayList<ContaOrcamentaria>();
    private TabelaModelo_2<ContaOrcamentaria> modeloTabelaContasExcluidas;
    private List<ContaOrcamentaria> contasExcluidas = new ArrayList<ContaOrcamentaria>();
    private TabelaModelo_2<Unidade> modeloTabelaCondominos;
    private List<Unidade> listaUnidades = new ArrayList<Unidade>();
    private TabelaModelo_2<Unidade> modeloTabelaCondominosADescartar;
    private List<Unidade> listaUnidadesADescartar = new ArrayList<Unidade>();
    private boolean calcular;
    BigDecimal quantidadeMes = new BigDecimal(0);

    /** Creates new form TelaOrcamento */
    public TelaOrcamento(Condominio condominio) {
        this.condominio = condominio;

        initComponents();
        new ControladorEventos();
        preencherTela();
        configurarSpinners();

        if (condominio != null) {
            this.setTitle("Orçamento - " + condominio.getRazaoSocial());
        }

        carregarTabelas();
    }

    private void carregarTabelas() {
        carregarTabelaCondominos();
        carregarTabelaContasOrcamentarias();
        carregarTabelaContasExcluidas();
        carregarTabelaContasExtraordinarias();
    }

    private void preencherTela() {
        txtNomeCondominio.setText(condominio.getRazaoSocial());
        txtDataInicial.setValue(DataUtil.toString(DataUtil.getPrimeiroDiaMes()));
        txtDataFinal.setValue(DataUtil.toString(DataUtil.getUltimoDiaMes()));
        txtNumeroUnidades.setText(Integer.toString(condominio.getUnidades().size()));
        calcularQuantidadeMeses();
    }

    private List<Unidade> getUnidades() {
        listaUnidades = condominio.getUnidades();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Unidade u1 = (Unidade) o1;
                Unidade u2 = (Unidade) o2;
                return u1.getUnidade().compareTo(u2.getUnidade());
            }
        };

        Collections.sort(listaUnidades, c);

        return listaUnidades;
    }

    private List<ContaOrcamentaria> getContasOrcamentarias() {
        BigDecimal somaMedia = new BigDecimal(0);
        BigDecimal somaMedia1 = new BigDecimal(0);
        BigDecimal somaMedia2 = new BigDecimal(0);
        BigDecimal somaMedia3 = new BigDecimal(0);
        for (ContaOrcamentaria co : contasOrcamentarias) {
            somaMedia = somaMedia.add(co.getMedia());
            somaMedia1 = somaMedia1.add(co.getMedia1());
            somaMedia2 = somaMedia2.add(co.getMedia2());
            somaMedia3 = somaMedia3.add(co.getMedia3());
        }
        txtSomaMedia.setText(PagamentoUtil.formatarMoeda(somaMedia.doubleValue()));
        txtSomaMedia1.setText(PagamentoUtil.formatarMoeda(somaMedia1.doubleValue()));
        txtSomaMedia2.setText(PagamentoUtil.formatarMoeda(somaMedia2.doubleValue()));
        txtSomaMedia3.setText(PagamentoUtil.formatarMoeda(somaMedia3.doubleValue()));
        return contasOrcamentarias;
    }

    private List<ContaOrcamentaria> getContasExtraordinarias() {
        BigDecimal somaMediaExtraordinaria = new BigDecimal(0);
        BigDecimal somaMediaExtraordinaria1 = new BigDecimal(0);
        BigDecimal somaMediaExtraordinaria2 = new BigDecimal(0);
        BigDecimal somaMediaExtraordinaria3 = new BigDecimal(0);
        for (ContaOrcamentaria co : contasExtraordinarias) {
            somaMediaExtraordinaria = somaMediaExtraordinaria.add(co.getMedia());
            somaMediaExtraordinaria1 = somaMediaExtraordinaria1.add(co.getMedia1());
            somaMediaExtraordinaria2 = somaMediaExtraordinaria2.add(co.getMedia2());
            somaMediaExtraordinaria3 = somaMediaExtraordinaria3.add(co.getMedia3());
        }
        txtSomaMediaExtraordinaria.setText(PagamentoUtil.formatarMoeda(somaMediaExtraordinaria.doubleValue()));
        txtSomaMediaExtraordinaria1.setText(PagamentoUtil.formatarMoeda(somaMediaExtraordinaria1.doubleValue()));
        txtSomaMediaExtraordinaria2.setText(PagamentoUtil.formatarMoeda(somaMediaExtraordinaria2.doubleValue()));
        txtSomaMediaExtraordinaria3.setText(PagamentoUtil.formatarMoeda(somaMediaExtraordinaria3.doubleValue()));
        return contasExtraordinarias;
    }

    private List<ContaOrcamentaria> getContasExcluidas() {
        return contasExcluidas;
    }

    private void carregarTabelaCondominos() {

        modeloTabelaCondominos = new TabelaModelo_2<Unidade>(tabelaCondominos, "Unidade, Nome dos Condôminos, É Sindico?".split(",")) {

            @Override
            protected List<Unidade> getCarregarObjetos() {
                return getUnidades();
            }

            @Override
            public Object getValor(Unidade unidade, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return unidade.getUnidade();
                    case 1:
                        return unidade.getCondomino().getNome();
                    case 2:
                        return unidade.isSindico() ? "Sim" : "Não";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setMaxWidth(50);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setCellRenderer(direito);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(1)).setMinWidth(200);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(2)).setMaxWidth(60);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(2)).setCellRenderer(centralizado);
    }

    private void carregarTabelaCondominosADescartar() {

        modeloTabelaCondominosADescartar = new TabelaModelo_2<Unidade>(tabelaCondominosADescartar, "Unidade, Nome dos Condôminos, É Sindico?".split(",")) {

            @Override
            protected List<Unidade> getCarregarObjetos() {
                return getUnidadesDescartadas();
            }

            @Override
            public Object getValor(Unidade unidade, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return unidade.getUnidade();
                    case 1:
                        return unidade.getCondomino().getNome();
                    case 2:
                        return unidade.isSindico() ? "Sim" : "Não";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaCondominosADescartar.getColumn(modeloTabelaCondominosADescartar.getCampo(0)).setMaxWidth(50);
        tabelaCondominosADescartar.getColumn(modeloTabelaCondominosADescartar.getCampo(0)).setCellRenderer(direito);
        tabelaCondominosADescartar.getColumn(modeloTabelaCondominosADescartar.getCampo(1)).setMinWidth(200);
        tabelaCondominosADescartar.getColumn(modeloTabelaCondominosADescartar.getCampo(2)).setMaxWidth(60);
        tabelaCondominosADescartar.getColumn(modeloTabelaCondominosADescartar.getCampo(2)).setCellRenderer(centralizado);
    }

    private void carregarTabelaContasOrcamentarias() {
        String valores = "Conta, Descrição das Despesas, Média, Média + " + spnIncremento1.getValue() + "%, Média + " + spnIncremento2.getValue() + " %, Média + " + spnIncremento3.getValue() + " %";
        modeloTabelaContaOrcamentaria = new TabelaModelo_2<ContaOrcamentaria>(tabelaContaOrcamentaria, valores.split(",")) {

            @Override
            protected List<ContaOrcamentaria> getCarregarObjetos() {
                return getContasOrcamentarias();
            }

            @Override
            public Object getValor(ContaOrcamentaria contaOrcamentaria, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return contaOrcamentaria.getConta().getCodigo();
                    case 1:
                        return contaOrcamentaria.getConta().getNome();
                    case 2:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia().doubleValue());
                    case 3:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia1().doubleValue());
                    case 4:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia2().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia3().doubleValue());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(0)).setMaxWidth(50);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(1)).setMinWidth(200);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(2)).setMaxWidth(70);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(2)).setCellRenderer(direito);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(3)).setMaxWidth(80);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(3)).setCellRenderer(direito);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(4)).setMinWidth(70);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(4)).setCellRenderer(direito);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(5)).setMinWidth(70);
        tabelaContaOrcamentaria.getColumn(modeloTabelaContaOrcamentaria.getCampo(5)).setCellRenderer(direito);
    }

    private void carregarTabelaContasExtraordinarias() {
        String valores = "Conta, Descrição das Despesas, Média, Média + " + spnIncremento1.getValue() + "%, Média + " + spnIncremento2.getValue() + " %, Média + " + spnIncremento3.getValue() + " %";
        modeloTabelaContasExtraordinarias = new TabelaModelo_2<ContaOrcamentaria>(tabelaContasExtraordinarias, valores.split(",")) {

            @Override
            protected List<ContaOrcamentaria> getCarregarObjetos() {
                return getContasExtraordinarias();
            }

            @Override
            public Object getValor(ContaOrcamentaria contaOrcamentaria, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return contaOrcamentaria.getConta().getCodigo();
                    case 1:
                        return contaOrcamentaria.getConta().getNome();
                    case 2:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia().doubleValue());
                    case 3:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia1().doubleValue());
                    case 4:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia2().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia3().doubleValue());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(0)).setMaxWidth(50);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(1)).setMinWidth(200);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(2)).setMaxWidth(70);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(2)).setCellRenderer(direito);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(3)).setMaxWidth(80);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(3)).setCellRenderer(direito);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(4)).setMinWidth(70);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(4)).setCellRenderer(direito);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(5)).setMinWidth(70);
        tabelaContasExtraordinarias.getColumn(modeloTabelaContasExtraordinarias.getCampo(5)).setCellRenderer(direito);
    }

    private void carregarTabelaContasExcluidas() {
        String valores = "Conta, Descrição das Despesas, Média, Média + " + spnIncremento1.getValue() + "%, Média + " + spnIncremento2.getValue() + " %, Média + " + spnIncremento3.getValue() + " %";
        modeloTabelaContasExcluidas = new TabelaModelo_2<ContaOrcamentaria>(tabelaContasExcluidas, valores.split(",")) {

            @Override
            protected List<ContaOrcamentaria> getCarregarObjetos() {
                return getContasExcluidas();
            }

            @Override
            public Object getValor(ContaOrcamentaria contaOrcamentaria, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return contaOrcamentaria.getConta().getCodigo();
                    case 1:
                        return contaOrcamentaria.getConta().getNome();
                    case 2:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia().doubleValue());
                    case 3:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia1().doubleValue());
                    case 4:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia2().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(contaOrcamentaria.getMedia3().doubleValue());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(0)).setMaxWidth(50);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(1)).setMinWidth(200);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(2)).setMaxWidth(70);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(2)).setCellRenderer(direito);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(3)).setMaxWidth(80);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(3)).setCellRenderer(direito);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(4)).setMinWidth(70);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(4)).setCellRenderer(direito);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(5)).setMinWidth(70);
        tabelaContasExcluidas.getColumn(modeloTabelaContasExcluidas.getCampo(5)).setCellRenderer(direito);
    }

    private List<Pagamento> getContasPorPeriodo() {
        List<Pagamento> pagamentos = new DAO().listar(Pagamento.class, "PagamentosPorPeriodoContaCorrente", condominio.getContaCorrente(), datInicio, datTermino);
        return pagamentos;
    }

    private void getApenasDespesas(List<Pagamento> getContasPorPeriodo, List<ContaOrcamentaria> listaContas) {
        listaContas.clear();
        List<Pagamento> pagamentos = new ArrayList<Pagamento>();
        for (Pagamento pagamento : getContasPorPeriodo) {
            if (!pagamento.getConta().isCredito()) {
                pagamentos.add(pagamento);
            }
        }

        List<Conta> contas = new DAO().listar("ListarContasDebito");

        for (Conta conta : contas) {
            ContaOrcamentaria c1 = new ContaOrcamentaria();

            Moeda valor = new Moeda();
            Moeda valorJaneiro = new Moeda(0);
            Moeda valorFevereiro = new Moeda(0);
            Moeda valorMarco = new Moeda(0);
            Moeda valorAbril = new Moeda(0);
            Moeda valorMaio = new Moeda(0);
            Moeda valorJunho = new Moeda(0);
            Moeda valorJulho = new Moeda(0);
            Moeda valorAgosto = new Moeda(0);
            Moeda valorSetembro = new Moeda(0);
            Moeda valorOutubro = new Moeda(0);
            Moeda valorNovembro = new Moeda(0);
            Moeda valorDezembro = new Moeda(0);

            for (Pagamento pagamento : pagamentos) {
                if (pagamento.getConta().getCodigo() == conta.getCodigo()) {
                    System.out.println("Pagamento e conta " + pagamento.getHistorico() + "   " + conta.getNome() + " " + conta.getCodigo());
                    c1.setConta(conta);
                    valor.soma(pagamento.getValor());

                    if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 1) {
                        valorJaneiro.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 2) {
                        valorFevereiro.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 3) {
                        valorMarco.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 4) {
                        valorAbril.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 5) {
                        valorMaio.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 6) {
                        valorJunho.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 7) {
                        valorJulho.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 8) {
                        valorAgosto.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 9) {
                        valorSetembro.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 10) {
                        valorOutubro.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 11) {
                        valorNovembro.soma(pagamento.getValor());
                    } else if (DataUtil.getDateTime(pagamento.getDataPagamento()).getMonthOfYear() == 12) {
                        valorDezembro.soma(pagamento.getValor());
                    }
                }
            }
            if (c1.getConta() != null) {
                c1.setTotal(valor.bigDecimalValue());
                c1.setSomaJaneiro(valorJaneiro.bigDecimalValue());
                c1.setSomaFevereiro(valorFevereiro.bigDecimalValue());
                c1.setSomaMarco(valorMarco.bigDecimalValue());
                c1.setSomaAbril(valorAbril.bigDecimalValue());
                c1.setSomaMaio(valorMaio.bigDecimalValue());
                c1.setSomaJunho(valorJunho.bigDecimalValue());
                c1.setSomaJulho(valorJulho.bigDecimalValue());
                c1.setSomaAgosto(valorAgosto.bigDecimalValue());
                c1.setSomaSetembro(valorSetembro.bigDecimalValue());
                c1.setSomaOutubro(valorOutubro.bigDecimalValue());
                c1.setSomaNovembro(valorNovembro.bigDecimalValue());
                c1.setSomaDezembro(valorDezembro.bigDecimalValue());
                System.out.println("c1 " + c1.getConta().getNome());
                System.out.println("c1 valores " + c1.getTotal());
                System.out.println("c1 valores janeiro = " + c1.getSomaJaneiro());
                System.out.println("c1 valores fevereiro = " + c1.getSomaFevereiro());
                System.out.println("c1 valores março = " + c1.getSomaMarco());
                System.out.println("c1 valores abril = " + c1.getSomaAbril());
                System.out.println("c1 valores maio = " + c1.getSomaMaio());
                System.out.println("c1 valores junho = " + c1.getSomaJunho());
                System.out.println("c1 valores julho = " + c1.getSomaJulho());
                System.out.println("c1 valores agosto = " + c1.getSomaAgosto());
                System.out.println("c1 valores setembro = " + c1.getSomaSetembro());
                System.out.println("c1 valores outubro = " + c1.getSomaOutubro());
                System.out.println("c1 valores novembro = " + c1.getSomaNovembro());
                System.out.println("c1 valores dezembro = " + c1.getSomaDezembro());
                listaContas.add(c1);
            }
        }

        System.out.println("PEGUEI OS TOTAIS!!!");

        if (listaContas.isEmpty()) {
            carregarTabelaContasOrcamentarias();
            ApresentacaoUtil.exibirAdvertencia("Não houve custos no período selecionado.", this);
        } else {
            carregarTabelaCondominosADescartar();
            calcularMedias(listaContas);
            carregarTabelaContasOrcamentarias();
        }
    }

    private void calcularMedias(List<ContaOrcamentaria> listaContas) {
        for (ContaOrcamentaria co : listaContas) {
            double total = 0;
            total += co.getTotal().doubleValue();
            int numeroMeses = 0;
            numeroMeses = verificarValorMes(co);
            co.setMedia(new Moeda(total / numeroMeses).bigDecimalValue().setScale(2, RoundingMode.HALF_UP));
            co.setMedia1(co.getMedia());
            co.setMedia1(co.getMedia1().add(co.getMedia1().multiply(new Moeda((Integer) spnIncremento1.getValue()).divide(100).bigDecimalValue())).setScale(2, RoundingMode.HALF_UP));
            co.setMedia2(co.getMedia());
            co.setMedia2(co.getMedia2().add(co.getMedia2().multiply(new Moeda((Integer) spnIncremento2.getValue()).divide(100).bigDecimalValue())).setScale(2, RoundingMode.HALF_UP));
            co.setMedia3(co.getMedia());
            co.setMedia3(co.getMedia3().add(co.getMedia3().multiply(new Moeda((Integer) spnIncremento3.getValue()).divide(100).bigDecimalValue())).setScale(2, RoundingMode.HALF_UP));
        }
    }

    private int verificarValorMes(ContaOrcamentaria co) {
        int numeroMeses = 0;
        numeroMeses = quantidadeMes.intValue();

        if (co.getSomaJaneiro().doubleValue() == 0 && verificarMes(1)) {
            numeroMeses -= 1;
        }
        if (co.getSomaFevereiro().doubleValue() == 0 && verificarMes(2)) {
            numeroMeses -= 1;
        }
        if (co.getSomaMarco().doubleValue() == 0 && verificarMes(3)) {
            numeroMeses -= 1;
        }
        if (co.getSomaAbril().doubleValue() == 0 && verificarMes(4)) {
            numeroMeses -= 1;
        }
        if (co.getSomaMaio().doubleValue() == 0 && verificarMes(5)) {
            numeroMeses -= 1;
        }
        if (co.getSomaJunho().doubleValue() == 0 && verificarMes(6)) {
            numeroMeses -= 1;
        }
        if (co.getSomaJulho().doubleValue() == 0 && verificarMes(7)) {
            numeroMeses -= 1;
        }
        if (co.getSomaAgosto().doubleValue() == 0 && verificarMes(8)) {
            numeroMeses -= 1;
        }
        if (co.getSomaSetembro().doubleValue() == 0 && verificarMes(9)) {
            numeroMeses -= 1;
        }
        if (co.getSomaOutubro().doubleValue() == 0 && verificarMes(10)) {
            numeroMeses -= 1;
        }
        if (co.getSomaNovembro().doubleValue() == 0 && verificarMes(11)) {
            numeroMeses -= 1;
        }
        if (co.getSomaDezembro().doubleValue() == 0 && verificarMes(12)) {
            numeroMeses -= 1;
        }

        return numeroMeses;
    }

    private boolean verificarMes(int mes) {
        boolean estaNoPeriodo = false;

        for (int i = 0; i < quantidadeMes.intValue(); i++) {
            DateTime dataInicioAuxiliar = new DateTime(DataUtil.getPrimeiroDiaMes(DataUtil.getDateTime(datInicio).plusMonths(i)));
            DateTime dataTerminoAuxiliar = new DateTime(DataUtil.getUltimoDiaMes(dataInicioAuxiliar));

            if (mes == DataUtil.getDateTime(dataInicioAuxiliar).getMonthOfYear() && DataUtil.compararData(dataInicioAuxiliar, DataUtil.getDateTime(datInicio)) <= 1 && DataUtil.compararData(dataTerminoAuxiliar, DataUtil.getDateTime(datTermino)) >= -1) {
                estaNoPeriodo = true;
            }
        }
        return estaNoPeriodo;
    }

    private List<Unidade> getUnidadesDescartadas() {
        listaUnidadesADescartar.clear();
        for (Unidade u : getUnidades()) {
            if (u.isSindico() && !condominio.isSindicoPaga()) {
                listaUnidadesADescartar.add(u);
            } else {
                int quantidadeCobrancasInadimplentes = 0;
                for (Cobranca c : u.getCobrancas()) {
                    if (c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) >= 1 && c.isExibir()) {
                        quantidadeCobrancasInadimplentes += 1;
                    }
                }
                System.out.println("Unidade " + u.getUnidade() + " - número cobranças inadimplentes: " + quantidadeCobrancasInadimplentes);
                if (quantidadeCobrancasInadimplentes >= (Integer) spnQtdeDescarte.getValue() && (Integer) spnQtdeDescarte.getValue() != 0) {
                    listaUnidadesADescartar.add(u);
                }
            }
        }

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Unidade u1 = (Unidade) o1;
                Unidade u2 = (Unidade) o2;
                return u1.getUnidade().compareTo(u2.getUnidade());
            }
        };

        Collections.sort(listaUnidadesADescartar, c);

        return listaUnidadesADescartar;
    }

    private void calcularQuantidadeMeses() {
        if (DataUtil.getDiferencaEmDias(DataUtil.getDateTime(datTermino), DataUtil.getDateTime(datInicio)) > 365) {
            ApresentacaoUtil.exibirAdvertencia("Selecione um intervalo de tempo de, no máximo, 1 ano.", this);
            txtQtdeMeses.setText("");
            calcular = false;
            return;
        }
        quantidadeMes = new BigDecimal(DataUtil.getDiferencaEmMeses(DataUtil.getDateTime(datTermino), DataUtil.getDateTime(datInicio))).setScale(0, RoundingMode.HALF_DOWN);
        txtQtdeMeses.setText("" + quantidadeMes);
        calcular = true;
    }

    private void configurarSpinners() {
        SpinnerNumberModel nm = new SpinnerNumberModel();
        nm.setMinimum(0);
        spnQtdeDescarte.setModel(nm);

        SpinnerNumberModel nm1 = new SpinnerNumberModel();
        nm1.setMinimum(1);
        spnIncremento1.setModel(nm1);
        spnIncremento1.setValue(5);

        SpinnerNumberModel nm2 = new SpinnerNumberModel();
        nm2.setMinimum(1);
        spnIncremento2.setModel(nm2);
        spnIncremento2.setValue(10);

        SpinnerNumberModel nm3 = new SpinnerNumberModel();
        nm3.setMinimum(1);
        spnIncremento3.setModel(nm3);
        spnIncremento3.setValue(15);
    }

    private void incluirAdicional() {
        ContaOrcamentaria contaOrcamentariaAdicional = new ContaOrcamentaria();
        Conta conta = new Conta();
        conta.setCodigo(0);
        conta.setNome(txtDescricaoDiversos.getText());
        contaOrcamentariaAdicional.setConta(conta);
        contaOrcamentariaAdicional.setMedia(new BigDecimal(txtValorDiversos.getText().replace(",", ".")));
        contaOrcamentariaAdicional.setMedia1(contaOrcamentariaAdicional.getMedia());
        contaOrcamentariaAdicional.setMedia1(contaOrcamentariaAdicional.getMedia1().add(contaOrcamentariaAdicional.getMedia1().multiply(new Moeda((Integer) spnIncremento1.getValue()).divide(100).bigDecimalValue())).setScale(2, RoundingMode.HALF_UP));
        contaOrcamentariaAdicional.setMedia2(contaOrcamentariaAdicional.getMedia());
        contaOrcamentariaAdicional.setMedia2(contaOrcamentariaAdicional.getMedia2().add(contaOrcamentariaAdicional.getMedia2().multiply(new Moeda((Integer) spnIncremento2.getValue()).divide(100).bigDecimalValue())).setScale(2, RoundingMode.HALF_UP));
        contaOrcamentariaAdicional.setMedia3(contaOrcamentariaAdicional.getMedia());
        contaOrcamentariaAdicional.setMedia3(contaOrcamentariaAdicional.getMedia3().add(contaOrcamentariaAdicional.getMedia3().multiply(new Moeda((Integer) spnIncremento3.getValue()).divide(100).bigDecimalValue())).setScale(2, RoundingMode.HALF_UP));
        contasOrcamentarias.add(contaOrcamentariaAdicional);
        carregarTabelaContasOrcamentarias();
        limparCamposDiversos();
    }

    private void moverParaContasExcluidasExtraordinarias(List<ContaOrcamentaria> lista) {
        if (modeloTabelaContaOrcamentaria.getObjetoSelecionado() != null) {
            ContaOrcamentaria co = modeloTabelaContaOrcamentaria.getObjetoSelecionado();
            contasOrcamentarias.remove(co);
            lista.add(co);
            carregarTabelas();
        }
    }

    private void moverParaContasOrcamentarias() {
        List<ContaOrcamentaria> lista = new ArrayList<ContaOrcamentaria>();
        ContaOrcamentaria itemASerMovido = null;
        if (painelTabelas.getSelectedIndex() == 1) {
            lista = contasExtraordinarias;
            itemASerMovido = modeloTabelaContasExtraordinarias.getObjetoSelecionado();
        } else if (painelTabelas.getSelectedIndex() == 2) {
            lista = contasExcluidas;
            itemASerMovido = modeloTabelaContasExcluidas.getObjetoSelecionado();
        }
        if (itemASerMovido != null) {
            lista.remove(itemASerMovido);
            contasOrcamentarias.add(itemASerMovido);
            carregarTabelas();
        }
    }

    private void limparCamposDiversos() {
        txtDescricaoDiversos.setText("");
        txtValorDiversos.setText("");
    }

    private void limparTabelas() {
        contasOrcamentarias.clear();
        contasExcluidas.clear();
        contasExtraordinarias.clear();
        carregarTabelas();
    }

    private void imprimir() {
        List<HashMap<String, Object>> listaContasOrcamentarias = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> parametros = new HashMap();

//        DADOS SUBRELATORIO
//        List<HashMap<String, String>> listaConselheiros = new ArrayList<HashMap<String, String>>();
//        for (Unidade unidade : condominio.getConselheiros()) {
//            HashMap<String, String> mapa2 = new HashMap();
//            mapa2.put("nome", converterLetraMinuscula(unidade.getCondomino().getNome()));
//            mapa2.put("unidade", unidade.getUnidade());
//            listaConselheiros.add(mapa2);
//        }

        parametros.put("condominio", condominio.getRazaoSocial());
        parametros.put("periodo", DataUtil.toString(datInicio) + " a " + DataUtil.toString(datTermino));
        parametros.put("media1", "Média + " + spnIncremento1.getValue() + " %");
        parametros.put("media2", "Média + " + spnIncremento2.getValue() + " %");
        parametros.put("media3", "Média + " + spnIncremento3.getValue() + " %");
        parametros.put("somaMedia", txtSomaMedia.getText());
        parametros.put("somaMedia1", txtSomaMedia1.getText());
        parametros.put("somaMedia2", txtSomaMedia2.getText());
        parametros.put("somaMedia3", txtSomaMedia3.getText());
        parametros.put("numeroUnidades", "" + condominio.getUnidades().size());
        parametros.put("sindicoPaga", condominio.isSindicoPaga() ? "Sim" : "Não");
        parametros.put("cobrancasDesprezadas", "" + spnQtdeDescarte.getValue());

        for (ContaOrcamentaria co : contasOrcamentarias) {
            HashMap<String, Object> mapa = new HashMap();
            mapa.put("codigoConta", co.getConta().getCodigo() + "");
            mapa.put("historico", co.getConta().getNome());
            mapa.put("media", PagamentoUtil.formatarMoeda(co.getMedia().doubleValue()));
            mapa.put("media1", PagamentoUtil.formatarMoeda(co.getMedia1().doubleValue()));
            mapa.put("media2", PagamentoUtil.formatarMoeda(co.getMedia2().doubleValue()));
            mapa.put("media3", PagamentoUtil.formatarMoeda(co.getMedia3().doubleValue()));
            listaContasOrcamentarias.add(mapa);
        }

        URL caminho = getClass().getResource("/condominioPlus/relatorios/");
        parametros.put("subrelatorio", caminho.toString());

        if (!listaContasOrcamentarias.isEmpty()) {
            new Relatorios().imprimir("Orcamento", parametros, listaContasOrcamentarias, false, true, null);
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            txtDataInicial.addChangeListener(this);
            txtDataFinal.addChangeListener(this);
            btnCalcular.addActionListener(this);
            btnIncluir.addActionListener(this);
            btnLimpar.addActionListener(this);
            btnImprimir.addActionListener(this);
            tabelaContaOrcamentaria.addMouseListener(this);
            tabelaContasExtraordinarias.addMouseListener(this);
            tabelaContasExcluidas.addMouseListener(this);
            itemMenuContasExcluidas.addActionListener(this);
            itemMenuContasExtraordinarias.addActionListener(this);
            itemMenuContasOrcamentarias.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnCalcular) {
                if (calcular) {
                    limparTabelas();
                    getApenasDespesas(getContasPorPeriodo(), getContasOrcamentarias());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Não é possível efetuar o cálculo para um período maior que 1 ano!", TelaOrcamento.this);
                }
            } else if (source == btnIncluir) {
                incluirAdicional();
            } else if (source == btnLimpar) {
                limparTabelas();
            } else if (source == btnImprimir) {
                imprimir();
            } else if (source == itemMenuContasExcluidas) {
                moverParaContasExcluidasExtraordinarias(contasExcluidas);
            } else if (source == itemMenuContasExtraordinarias) {
                moverParaContasExcluidasExtraordinarias(contasExtraordinarias);
            } else if (source == itemMenuContasOrcamentarias) {
                moverParaContasOrcamentarias();
            }
            source = null;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            source = e.getSource();
            if (source == txtDataInicial || source == txtDataFinal) {
                ApresentacaoUtil.verificarDatas(source, txtDataInicial, txtDataFinal, this);
                datInicio = DataUtil.getCalendar(txtDataInicial.getValue());
                datTermino = DataUtil.getCalendar(txtDataFinal.getValue());

                calcularQuantidadeMeses();
                System.out.println(" thiago");
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == tabelaContaOrcamentaria) {
                    popupMenuContasOrcamentarias.show(e.getComponent(), e.getX(), e.getY());
                } else if (e.getSource() == tabelaContasExtraordinarias || e.getSource() == tabelaContasExcluidas) {
                    popupMenuContasExtraordinarias.show(e.getComponent(), e.getX(), e.getY());
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

        popupMenuContasOrcamentarias = new javax.swing.JPopupMenu();
        itemMenuContasExtraordinarias = new javax.swing.JMenuItem();
        itemMenuContasExcluidas = new javax.swing.JMenuItem();
        popupMenuContasExtraordinarias = new javax.swing.JPopupMenu();
        itemMenuContasOrcamentarias = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        jLabel3 = new javax.swing.JLabel();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        txtNomeCondominio = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        spnIncremento1 = new javax.swing.JSpinner();
        spnIncremento2 = new javax.swing.JSpinner();
        spnIncremento3 = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        painelTabelas = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContaOrcamentaria = new javax.swing.JTable();
        txtSomaMedia = new javax.swing.JTextField();
        txtSomaMedia2 = new javax.swing.JTextField();
        txtSomaMedia3 = new javax.swing.JTextField();
        txtSomaMedia1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaContasExtraordinarias = new javax.swing.JTable();
        txtSomaMediaExtraordinaria = new javax.swing.JTextField();
        txtSomaMediaExtraordinaria1 = new javax.swing.JTextField();
        txtSomaMediaExtraordinaria2 = new javax.swing.JTextField();
        txtSomaMediaExtraordinaria3 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaContasExcluidas = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabelaCondominosADescartar = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaCondominos = new javax.swing.JTable();
        btnLimpar = new javax.swing.JButton();
        btnCalcular = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        spnQtdeDescarte = new javax.swing.JSpinner();
        txtNumeroUnidades = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        btnIncluir = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        txtDescricaoDiversos = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtValorDiversos = new javax.swing.JTextField();
        txtTaxaBase = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtQtdeMeses = new javax.swing.JTextField();

        itemMenuContasExtraordinarias.setText("Mover para Contas Extraordinárias");
        popupMenuContasOrcamentarias.add(itemMenuContasExtraordinarias);

        itemMenuContasExcluidas.setText("Mover para Contas Excluídas");
        popupMenuContasOrcamentarias.add(itemMenuContasExcluidas);

        itemMenuContasOrcamentarias.setText("Mover para Contas Orçamentárias");
        popupMenuContasExtraordinarias.add(itemMenuContasOrcamentarias);

        setClosable(true);
        setTitle("Orçamento");
        setVisible(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Condomínio");

        jLabel2.setText("Período Cáluculo das Médias");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("a");

        txtNomeCondominio.setBackground(new java.awt.Color(204, 204, 204));
        txtNomeCondominio.setEditable(false);

        jLabel4.setText("Nº de Unidades");

        jLabel5.setText("% Incremento da Média");

        jLabel6.setText("Taxa Base R$");

        tabelaContaOrcamentaria.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaOrcamentaria);

        txtSomaMedia.setEditable(false);
        txtSomaMedia.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMedia.setOpaque(false);

        txtSomaMedia2.setEditable(false);
        txtSomaMedia2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMedia2.setOpaque(false);

        txtSomaMedia3.setEditable(false);
        txtSomaMedia3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMedia3.setOpaque(false);

        txtSomaMedia1.setEditable(false);
        txtSomaMedia1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMedia1.setOpaque(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(txtSomaMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSomaMedia1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSomaMedia2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSomaMedia3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSomaMedia3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSomaMedia2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSomaMedia1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSomaMedia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        painelTabelas.addTab("Contas Orçamentárias", jPanel2);

        tabelaContasExtraordinarias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tabelaContasExtraordinarias);

        txtSomaMediaExtraordinaria.setEditable(false);
        txtSomaMediaExtraordinaria.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMediaExtraordinaria.setOpaque(false);

        txtSomaMediaExtraordinaria1.setEditable(false);
        txtSomaMediaExtraordinaria1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMediaExtraordinaria1.setOpaque(false);

        txtSomaMediaExtraordinaria2.setEditable(false);
        txtSomaMediaExtraordinaria2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMediaExtraordinaria2.setOpaque(false);

        txtSomaMediaExtraordinaria3.setEditable(false);
        txtSomaMediaExtraordinaria3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSomaMediaExtraordinaria3.setOpaque(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtSomaMediaExtraordinaria, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSomaMediaExtraordinaria1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSomaMediaExtraordinaria2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSomaMediaExtraordinaria3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSomaMediaExtraordinaria3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSomaMediaExtraordinaria2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSomaMediaExtraordinaria1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSomaMediaExtraordinaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        painelTabelas.addTab("Contas Extraordinárias", jPanel3);

        tabelaContasExcluidas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tabelaContasExcluidas);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelTabelas.addTab("Contas Excluídas", jPanel4);

        tabelaCondominosADescartar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(tabelaCondominosADescartar);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelTabelas.addTab("Unidades a Descartar", jPanel6);

        tabelaCondominos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(tabelaCondominos);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelTabelas.addTab("Unidades", jPanel7);

        btnLimpar.setText("Limpar");

        btnCalcular.setText("Calcular");

        btnImprimir.setText("Imprimir");

        jLabel9.setText("Qtde de Cobranças não Pagas a Descartar");

        txtNumeroUnidades.setBackground(new java.awt.Color(204, 204, 204));
        txtNumeroUnidades.setEditable(false);
        txtNumeroUnidades.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Diversos - Adicional"));

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta Adicional");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel10.setText("Descrição");

        jLabel11.setText("Valor");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDescricaoDiversos, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                .addGap(16, 16, 16)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValorDiversos, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtValorDiversos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(txtDescricaoDiversos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnIncluir, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE))
                .addGap(11, 11, 11))
        );

        txtTaxaBase.setBackground(new java.awt.Color(204, 204, 204));
        txtTaxaBase.setEditable(false);

        jLabel8.setText("Qtde. de Meses");

        txtQtdeMeses.setBackground(new java.awt.Color(204, 204, 204));
        txtQtdeMeses.setEditable(false);
        txtQtdeMeses.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnIncremento1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(spnIncremento2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(spnIncremento3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(painelTabelas, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtNumeroUnidades)
                                            .addComponent(txtDataInicial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                                .addGap(4, 4, 4)
                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(40, 40, 40)
                                                .addComponent(jLabel9)
                                                .addGap(18, 18, 18)
                                                .addComponent(spnQtdeDescarte, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(50, 50, 50)
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtTaxaBase, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                                                .addComponent(jLabel8)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtQtdeMeses, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(txtNomeCondominio, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))))
                        .addGap(41, 41, 41)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(372, Short.MAX_VALUE)
                .addComponent(btnLimpar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCalcular)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnImprimir)
                .addGap(48, 48, 48))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(41, 41, 41))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnLimpar});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNomeCondominio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(spnQtdeDescarte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNumeroUnidades, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8)
                    .addComponent(txtQtdeMeses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtTaxaBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnIncremento1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnIncremento2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(spnIncremento3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelTabelas, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpar)
                    .addComponent(btnCalcular)
                    .addComponent(btnImprimir))
                .addContainerGap())
        );

        painelTabelas.getAccessibleContext().setAccessibleName("Contas Orcamentarias");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 619, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JMenuItem itemMenuContasExcluidas;
    private javax.swing.JMenuItem itemMenuContasExtraordinarias;
    private javax.swing.JMenuItem itemMenuContasOrcamentarias;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane painelTabelas;
    private javax.swing.JPopupMenu popupMenuContasExtraordinarias;
    private javax.swing.JPopupMenu popupMenuContasOrcamentarias;
    private javax.swing.JSpinner spnIncremento1;
    private javax.swing.JSpinner spnIncremento2;
    private javax.swing.JSpinner spnIncremento3;
    private javax.swing.JSpinner spnQtdeDescarte;
    private javax.swing.JTable tabelaCondominos;
    private javax.swing.JTable tabelaCondominosADescartar;
    private javax.swing.JTable tabelaContaOrcamentaria;
    private javax.swing.JTable tabelaContasExcluidas;
    private javax.swing.JTable tabelaContasExtraordinarias;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtDescricaoDiversos;
    private javax.swing.JTextField txtNomeCondominio;
    private javax.swing.JTextField txtNumeroUnidades;
    private javax.swing.JTextField txtQtdeMeses;
    private javax.swing.JTextField txtSomaMedia;
    private javax.swing.JTextField txtSomaMedia1;
    private javax.swing.JTextField txtSomaMedia2;
    private javax.swing.JTextField txtSomaMedia3;
    private javax.swing.JTextField txtSomaMediaExtraordinaria;
    private javax.swing.JTextField txtSomaMediaExtraordinaria1;
    private javax.swing.JTextField txtSomaMediaExtraordinaria2;
    private javax.swing.JTextField txtSomaMediaExtraordinaria3;
    private javax.swing.JTextField txtTaxaBase;
    private javax.swing.JTextField txtValorDiversos;
    // End of variables declaration//GEN-END:variables
}
