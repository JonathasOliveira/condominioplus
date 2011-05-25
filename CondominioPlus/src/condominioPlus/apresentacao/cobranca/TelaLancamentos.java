/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaLancamentos.java
 *
 * Created on 11/05/2011, 15:42:54
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.BoletoBancario;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.CobrancaBase;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
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
import org.jrimum.bopepo.BancoSuportado;
import org.jrimum.bopepo.Boleto;
import org.jrimum.bopepo.view.BoletoViewer;
import org.jrimum.domkee.comum.pessoa.endereco.CEP;
import org.jrimum.domkee.comum.pessoa.endereco.Endereco;
import org.jrimum.domkee.financeiro.banco.febraban.Agencia;
import org.jrimum.domkee.financeiro.banco.febraban.Carteira;
import org.jrimum.domkee.financeiro.banco.febraban.Cedente;
import org.jrimum.domkee.financeiro.banco.febraban.ContaBancaria;
import org.jrimum.domkee.financeiro.banco.febraban.NumeroDaConta;
import org.jrimum.domkee.financeiro.banco.febraban.Sacado;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeTitulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo.EnumAceite;

/**
 *
 * @author eugenia
 */
public class TelaLancamentos extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TabelaModelo_2<Unidade> modeloTabelaCondominos;
    private List<Unidade> listaUnidades;
    private TabelaModelo_2<CobrancaBase> modeloTabelaCobrancaBase;
    private List<CobrancaBase> listaCobrancasBase;
    private TabelaModelo_2<Cobranca> modeloTabelaBoleto;
    private List<Cobranca> listaCobrancas;
    private TabelaModelo_2<Cobranca> modeloTabelaInadimplentes;
    private List<Cobranca> listaInadimplentes;
    private TabelaModelo_2<Cobranca> modeloTabelaPagos;
    private List<Cobranca> listaPagos;

    /** Creates new form TelaLancamentos */
    public TelaLancamentos(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        new ControladorEventos();

        carregarTabelaCondominos();
        carregarTabelaCobrancaBase();
        carregarTabelaCobranca();

        carregarDateField();

        if (condominio != null) {
            this.setTitle("Cobranças - " + condominio.getRazaoSocial());
        }
    }

    private void carregarDateField() {
        txtDataInicial.setValue(DataUtil.toString(DataUtil.getPrimeiroDiaMes()));
        txtDataFinal.setValue(DataUtil.toString(DataUtil.getUltimoDiaMes()));
    }

    private void carregarTabelaCondominos() {

        modeloTabelaCondominos = new TabelaModelo_2<Unidade>(tabelaCondominos, "Unidade, Nome dos Condôminos".split(",")) {

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
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setMaxWidth(50);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setCellRenderer(direito);

    }

    private List<Unidade> getUnidades() {
        listaUnidades = condominio.getUnidades();

        return listaUnidades;
    }

    private void carregarTabelaCobrancaBase() {
        modeloTabelaCobrancaBase = new TabelaModelo_2<CobrancaBase>(tabelaCobrancasBase, "Conta, Descrição, Valor, Dividir?".split(",")) {

            @Override
            protected List<CobrancaBase> getCarregarObjetos() {
                return getCobrancasBase();
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

        tabelaCobrancasBase.getColumn(modeloTabelaCobrancaBase.getCampo(0)).setMaxWidth(50);
        tabelaCobrancasBase.getColumn(modeloTabelaCobrancaBase.getCampo(1)).setMinWidth(180);
        tabelaCobrancasBase.getColumn(modeloTabelaCobrancaBase.getCampo(2)).setMaxWidth(70);
        tabelaCobrancasBase.getColumn(modeloTabelaCobrancaBase.getCampo(3)).setMaxWidth(50);

        tabelaCobrancasBase.getColumn(modeloTabelaCobrancaBase.getCampo(2)).setCellRenderer(direito);
        tabelaCobrancasBase.getColumn(modeloTabelaCobrancaBase.getCampo(3)).setCellRenderer(centralizado);
    }

    private List<CobrancaBase> getCobrancasBase() {
        listaCobrancasBase = condominio.getCobrancasBase();

        return listaCobrancasBase;
    }

    private void carregarTabelaCobranca() {
        modeloTabelaBoleto = new TabelaModelo_2<Cobranca>(tabelaCobrancas, "Unidade, Condominio, Vencimento, Documento, Valor Original, Juros, Multa, Total, Linha Digitável ".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getCobrancas();
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getCodigo();
                    case 2:
                        return DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return cobranca.getNumeroDocumento();
                    case 4:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 8:
                        return cobranca.getLinhaDigitavel();
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(0)).setCellRenderer(direito);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(3)).setCellRenderer(direito);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(4)).setCellRenderer(direito);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(5)).setCellRenderer(direito);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(6)).setCellRenderer(direito);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(7)).setCellRenderer(direito);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(8)).setCellRenderer(centralizado);

        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(0)).setMaxWidth(50);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(1)).setMaxWidth(80);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(4)).setMinWidth(80);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(8)).setMinWidth(265);

        tabelaCobrancas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getCobrancas() {
        listaCobrancas = new ArrayList<Cobranca>();
        for (Unidade u : condominio.getUnidades()) {
            for (Cobranca c : u.getCobrancas()) {
                if (c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) < 1) {
                    listaCobrancas.add(c);
                }
            }
        }
        return listaCobrancas;
    }

    private void carregarTabelaInadimplentes() {
        modeloTabelaInadimplentes = new TabelaModelo_2<Cobranca>(tabelaInadimplentes, "Unidade, Condominio, Vencimento, V. Prorrogado, Documento, Valor Original, Juros, Multa, Total, Linha Digitável ".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getInadimplentes();
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getCodigo();
                    case 2:
                        return DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return DataUtil.getDateTime(cobranca.getVencimentoProrrogado());
                    case 4:
                        return cobranca.getNumeroDocumento();
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 8:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 9:
                        return cobranca.getLinhaDigitavel();
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(0)).setCellRenderer(direito);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(4)).setCellRenderer(direito);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(5)).setCellRenderer(direito);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(6)).setCellRenderer(direito);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(7)).setCellRenderer(direito);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(8)).setCellRenderer(direito);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(9)).setCellRenderer(centralizado);

        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(0)).setMaxWidth(50);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(3)).setMinWidth(85);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(1)).setMaxWidth(80);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(5)).setMinWidth(80);
        tabelaInadimplentes.getColumn(modeloTabelaInadimplentes.getCampo(9)).setMinWidth(265);

        tabelaInadimplentes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getInadimplentes() {
        listaInadimplentes = new ArrayList<Cobranca>();
        for (Unidade u : condominio.getUnidades()) {
            for (Cobranca c : u.getCobrancas()) {
                if (modeloTabelaCondominos.getObjetoSelecionado() == null) {
                    if (c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) >= 1) {
                        listaInadimplentes.add(c);
                    }
                } else {
                    if (c.getUnidade().getCodigo() == modeloTabelaCondominos.getObjetoSelecionado().getCodigo() && c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) >= 1) {
                        listaInadimplentes.add(c);
                    }
                }
            }
        }
        return listaInadimplentes;
    }

    private void carregarTabelaPagos() {
        modeloTabelaPagos = new TabelaModelo_2<Cobranca>(tabelaPagos, "Unidade, Condominio, Vencimento, Pagamento, Documento, Valor Original, Juros, Multa, Total".split(",")) {

            @Override
            protected List<Cobranca> getCarregarObjetos() {
                return getPagos(DataUtil.getDateTime(txtDataInicial.getValue()), DataUtil.getDateTime(txtDataFinal.getValue()));
            }

            @Override
            public Object getValor(Cobranca cobranca, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return cobranca.getUnidade().getUnidade();
                    case 1:
                        return cobranca.getUnidade().getCondominio().getCodigo();
                    case 2:
                        return cobranca.getVencimentoProrrogado() != null ? DataUtil.getDateTime(cobranca.getVencimentoProrrogado()) : DataUtil.getDateTime(cobranca.getDataVencimento());
                    case 3:
                        return DataUtil.getDateTime(cobranca.getDataPagamento());
                    case 4:
                        return cobranca.getNumeroDocumento();
                    case 5:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorOriginal().doubleValue());
                    case 6:
                        return PagamentoUtil.formatarMoeda(cobranca.getJuros().doubleValue());
                    case 7:
                        return PagamentoUtil.formatarMoeda(cobranca.getMulta().doubleValue());
                    case 8:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(0)).setCellRenderer(direito);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(4)).setCellRenderer(direito);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(5)).setCellRenderer(direito);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(6)).setCellRenderer(direito);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(7)).setCellRenderer(direito);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(8)).setCellRenderer(direito);

        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(0)).setMaxWidth(50);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(3)).setMinWidth(80);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(1)).setMaxWidth(80);
        tabelaPagos.getColumn(modeloTabelaPagos.getCampo(5)).setMinWidth(80);

        tabelaPagos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Cobranca> getPagos(DateTime dataInicial, DateTime dataFinal) {
        listaPagos = new ArrayList<Cobranca>();

        if (modeloTabelaCondominos.getObjetoSelecionado() == null) {
            listaPagos = new DAO().listar("CobrancasPagasPorPeriodo", condominio, DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
        } else {
            listaPagos = new DAO().listar("CobrancasPagasPorPeriodoUnidade", modeloTabelaCondominos.getObjetoSelecionado(), DataUtil.getCalendar(dataInicial), DataUtil.getCalendar(dataFinal));
        }

        return listaPagos;
    }

    private void gerarCobrancas(List<Unidade> lista) {
        if (lista.size() > 1 && !txtNumeroDocumento.getText().equals("")) {
            ApresentacaoUtil.exibirAdvertencia("Selecione apenas um condômino.", this);
            return;
        }
        if (!txtNumeroDocumento.getText().equals("") && txtNumeroDocumento.getText().length() != 12) {
            ApresentacaoUtil.exibirAdvertencia("O número documento informado está incorreto.", this);
            return;
        }

        UNIDADES:
        for (Unidade u : lista) {
            Cobranca cobranca = new Cobranca();
            cobranca.setUnidade(u);
            cobranca.setValorTotal(new BigDecimal(0));
            cobranca.setValorOriginal(new BigDecimal(0));
            cobranca.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
            if (txtNumeroDocumento.getText().equals("")) {
                cobranca.setNumeroDocumento(BoletoBancario.gerarNumeroDocumento(condominio, DataUtil.getDateTime(txtDataVencimento.getValue())));
            } else {
                cobranca.setNumeroDocumento(txtNumeroDocumento.getText());
            }
            if (u.isSindico() && !condominio.isSindicoPaga()) {
                continue UNIDADES;
            }
            calcularCobrancas(u, cobranca);
            u.getCobrancas().add(cobranca);
            cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavel(cobranca));
            new DAO().salvar(u);
        }
    }

    private void calcularCobrancas(Unidade u, Cobranca cobranca) {
        for (CobrancaBase co : condominio.getCobrancasBase()) {
            Pagamento pagamento = new Pagamento();
            pagamento.setDataVencimento(DataUtil.getCalendar(txtDataVencimento.getValue()));
            pagamento.setCobranca(cobranca);
            pagamento.setConta(co.getConta());
            if (co.isDividirFracaoIdeal()) {
                pagamento.setValor(new BigDecimal(calcularPorFracaoIdeal(u, co)));
            } else {
                pagamento.setValor(co.getValor());
            }
            cobranca.getPagamentos().add(pagamento);
            cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
            cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
        }
    }

    private double calcularPorFracaoIdeal(Unidade u, CobrancaBase cobrancaBase) {
        double resultado = 0;
        resultado = (u.getFracaoIdeal() * cobrancaBase.getValor().doubleValue()) / getMaiorFracaoIdeal();
        System.out.println("resultado - " + resultado);
        return resultado;
    }

    private double getMaiorFracaoIdeal() {
        double resultado = 0;
        for (Unidade u : condominio.getUnidades()) {
            if (u.getFracaoIdeal() > resultado) {
                resultado = u.getFracaoIdeal();
            }
        }
        return resultado;
    }

    private void remover() {
        if (!ApresentacaoUtil.perguntar("Desejar remover a(s) cobrança(s)?", this)) {
            return;
        }
        if (modeloTabelaBoleto.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabelaBoleto.getLinhasSelecionadas());
            List<Cobranca> itensRemover = modeloTabelaBoleto.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Cobranca c : itensRemover) {
                    modeloTabelaBoleto.remover(c);
                    for (Unidade u : condominio.getUnidades()) {
                        if (c.getUnidade().getCodigo() == u.getCodigo()) {
                            u.getCobrancas().remove(c);
                        }
                    }
                    new DAO().remover(c);
                }
            }

            ApresentacaoUtil.exibirInformacao("Cobrança(s) removida(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void calcularJurosMulta(Cobranca cobranca, DateTime dataProrrogada) {
        Moeda juros = new Moeda();
        Moeda multa = new Moeda();
        cobranca.setValorTotal(new BigDecimal(0));
        cobranca.setValorTotal(cobranca.getValorOriginal());
        int diferencaDias = 0;
        diferencaDias = DataUtil.getDiferencaEmDias(dataProrrogada, DataUtil.getDateTime(cobranca.getDataVencimento()));
        if (diferencaDias > 0) {
            System.out.println("diferenca dias: " + diferencaDias);
            juros.soma(diferencaDias).multiplica(NegocioUtil.getConfiguracao().getPercentualJuros().divide(new BigDecimal(100)));
            System.out.println("juros: " + juros);
            juros.multiplica(cobranca.getValorTotal());
            multa.soma(NegocioUtil.getConfiguracao().getPercentualMulta().divide(new BigDecimal(100)));
            multa.multiplica(cobranca.getValorTotal());
            cobranca.setVencimentoProrrogado(DataUtil.getCalendar(dataProrrogada));
        }
        cobranca.setJuros(juros.bigDecimalValue());
        cobranca.setMulta(multa.bigDecimalValue());
        cobranca.setValorTotal(cobranca.getValorTotal().add(cobranca.getJuros().add(cobranca.getMulta())));
        cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavel(cobranca));
        new DAO().salvar(cobranca);
        carregarTabelaInadimplentes();
    }

    private void imprimirBoleto(List<Cobranca> listaCobrancas) {
        List<Boleto> boletos = new ArrayList<Boleto>();
        for (Cobranca cobranca : listaCobrancas) {

            /*
             * INFORMANDO DADOS SOBRE O CEDENTE.
             */
            Cedente cedente = new Cedente(cobranca.getUnidade().getCondominio().getRazaoSocial(), BoletoBancario.retirarCaracteresCnpj(cobranca.getUnidade().getCondominio().getCnpj()));

            /*
             * INFORMANDO DADOS SOBRE O SACADO.
             */
            Sacado sacado = new Sacado(cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());

            // Informando o endereço do sacado.
            Endereco enderecoSac = new Endereco();

            for (condominioPlus.negocio.Endereco e : cobranca.getUnidade().getCondomino().getEnderecos()) {
                if (e.isPadrao()) {
                    enderecoSac.setUF(BoletoBancario.getUnidadeFederativa(e.getEstado()));
                    enderecoSac.setLocalidade(e.getCidade());
                    enderecoSac.setCep(new CEP(e.getCep()));
                    enderecoSac.setBairro(e.getBairro());
                    enderecoSac.setLogradouro(e.getLogradouro());
                    enderecoSac.setNumero(e.getNumero() + " " + e.getComplemento());
                }
            }

            sacado.addEndereco(enderecoSac);

            /*
             * INFORMANDO DADOS SOBRE O SACADOR AVALISTA.
             */
//                SacadorAvalista sacadorAvalista = new SacadorAvalista("JRimum Enterprise", "00.000.000/0001-91");
//
//                // Informando o endereço do sacador avalista.
//                Endereco enderecoSacAval = new Endereco();
//                enderecoSacAval.setUF(UnidadeFederativa.DF);
//                enderecoSacAval.setLocalidade("Brasília");
//                enderecoSacAval.setCep(new CEP("59000-000"));
//                enderecoSacAval.setBairro("Grande Centro");
//                enderecoSacAval.setLogradouro("Rua Eternamente Principal");
//                enderecoSacAval.setNumero("001");9
//                sacadorAvalista.addEndereco(enderecoSacAval);

            /*
             * INFORMANDO OS DADOS SOBRE O TÍTULO.
             */

            // Informando dados sobre a conta bancária do título.
            ContaBancaria contaBancaria = new ContaBancaria(BancoSuportado.BANCO_SANTANDER.create());
            contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(cobranca.getUnidade().getCondominio().getContaBancaria().getCodigoCedente())));
            contaBancaria.setCarteira(new Carteira(102));
            contaBancaria.setAgencia(new Agencia(3918, "0"));

            Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
            titulo.setNumeroDoDocumento(cobranca.getNumeroDocumento());
            titulo.setNossoNumero(cobranca.getNumeroDocumento());
            titulo.setDigitoDoNossoNumero(BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento()));
            titulo.setValor(cobranca.getValorTotal());
            titulo.setDataDoDocumento(DataUtil.getDate(DataUtil.hoje()));
            if (cobranca.getVencimentoProrrogado() != null) {
                titulo.setDataDoVencimento(DataUtil.getDate(cobranca.getVencimentoProrrogado()));
            } else {
                titulo.setDataDoVencimento(DataUtil.getDate(cobranca.getDataVencimento()));
            }
            titulo.setTipoDeDocumento(TipoDeTitulo.DM_DUPLICATA_MERCANTIL);
            titulo.setAceite(EnumAceite.N);
            titulo.setDesconto(null);
            titulo.setDeducao(null);
            titulo.setMora(null);
            titulo.setAcrecimo(null);
            titulo.setValorCobrado(null);

            /*
             * INFORMANDO OS DADOS SOBRE O BOLETO.
             */
            Boleto boleto = new Boleto(titulo);

            boleto.setLocalPagamento("Pagável preferencialmente na Rede X ou em "
                    + "qualquer Banco até o Vencimento.");
//                boleto.setInstrucaoAoSacado("Senhor sacado, sabemos sim que o valor " +
//                                "cobrado não é o esperado, aproveite o DESCONTÃO!");
            boleto.setInstrucao1("PARA PAGAMENTO 1 até 23/05/2011 não cobrar nada!");
//                boleto.setInstrucao2("PARA PAGAMENTO 2 até Amanhã Não cobre!");
//                boleto.setInstrucao3("PARA PAGAMENTO 3 até Depois de amanhã, OK, não cobre.");
//                boleto.setInstrucao4("PARA PAGAMENTO 4 até 04/xx/xxxx de 4 dias atrás COBRAR O VALOR DE: R$ 01,00");
//                boleto.setInstrucao5("PARA PAGAMENTO 5 até 05/xx/xxxx COBRAR O VALOR DE: R$ 02,00");
//                boleto.setInstrucao6("PARA PAGAMENTO 6 até 06/xx/xxxx COBRAR O VALOR DE: R$ 03,00");
//                boleto.setInstrucao7("PARA PAGAMENTO 7 até xx/xx/xxxx COBRAR O VALOR QUE VOCÊ QUISER!");
//                boleto.setInstrucao8("APÓS o Vencimento, Pagável Somente na Rede X.");

            System.out.println("campo livre " + boleto.getCampoLivre().write());
            System.out.println("linha digitavel " + boleto.getLinhaDigitavel().write());
            System.out.println("Codigo Barras " + boleto.getCodigoDeBarras().write());

            boletos.add(boleto);
        }

        /*
         * GERANDO O(S) BOLETO(S) BANCÁRIO(S).
         */

        File pdf = BoletoViewer.groupInOnePDF("MeuPrimeiroBoleto.pdf", boletos);
        BoletoBancario.mostreBoletoNaTela(pdf);
    }

    private void limparSelecoesTabelas() {
        tabelaCobrancas.clearSelection();
        tabelaCondominos.clearSelection();
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void configurar() {
            btnGerarCobranca.addActionListener(this);
            btnImprimirBoleto.addActionListener(this);
            btnLimpar.addActionListener(this);
            btnImprimirBoletoInadimplente.addActionListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            itemMenuCalcularJurosMulta.addActionListener(this);
            itemMenuMudarAltura.addActionListener(this);
            jTabbedPane1.addChangeListener(this);
            tabelaCondominos.addMouseListener(this);
            tabelaInadimplentes.addMouseListener(this);
            txtDataInicial.addChangeListener(this);
            txtDataFinal.addChangeListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == btnGerarCobranca) {
                if (modeloTabelaCondominos.getObjetosSelecionados().size() == 0) {
                    gerarCobrancas(condominio.getUnidades());
                } else {
                    gerarCobrancas(modeloTabelaCondominos.getObjetosSelecionados());
                }
                carregarTabelaCobranca();
            } else if (origem == itemMenuRemoverSelecionados) {
                remover();
            } else if (origem == itemMenuCalcularJurosMulta) {
                if (modeloTabelaInadimplentes.getLinhaSelecionada() > -1) {
                    calcularJurosMulta(modeloTabelaInadimplentes.getObjetoSelecionado(), DataUtil.getDateTime(txtVencimentoProrrogado.getValue()));
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para calcular!", TelaLancamentos.this);
                }
            } else if (origem == btnImprimirBoleto) {
                if (modeloTabelaBoleto.getLinhaSelecionada() > -1) {
                    imprimirBoleto(modeloTabelaBoleto.getObjetosSelecionados());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a(s) cobrança(s) que deseja imprimir.", TelaLancamentos.this);
                }
            } else if (origem == btnImprimirBoletoInadimplente) {
                if (modeloTabelaInadimplentes.getLinhaSelecionada() > -1) {
                    imprimirBoleto(modeloTabelaInadimplentes.getObjetosSelecionados());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Selecione a(s) cobrança(s) que deseja imprimir.", TelaLancamentos.this);
                }
            } else if (origem == itemMenuMudarAltura) {
                if (painelCobrancaBase.isVisible()) {
                    painelCobrancaBase.setVisible(false);
                } else {
                    painelCobrancaBase.setVisible(true);
                }
            } else if (origem == btnLimpar) {
                limparSelecoesTabelas();
                if (jTabbedPane1.getSelectedIndex() == 1) {
                    carregarTabelaInadimplentes();
                } else if (jTabbedPane1.getSelectedIndex() == 2) {
                    carregarTabelaPagos();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabelaCobrancas) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.isPopupTrigger() && e.getSource() == tabelaInadimplentes) {
                popupMenuInadimplentes.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.getSource() == tabelaCondominos) {
                if (jTabbedPane1.getSelectedIndex() == 1) {
                    carregarTabelaInadimplentes();
                } else if (jTabbedPane1.getSelectedIndex() == 2) {
                    carregarTabelaPagos();
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            origem = e.getSource();
            if (origem == jTabbedPane1) {
                if (jTabbedPane1.getSelectedIndex() == 0) {
                    carregarTabelaCobrancaBase();
                    carregarTabelaCobranca();
                    limparSelecoesTabelas();
                } else if (jTabbedPane1.getSelectedIndex() == 1) {
                    carregarTabelaInadimplentes();
                    limparSelecoesTabelas();
                } else if (jTabbedPane1.getSelectedIndex() == 2) {
                    carregarTabelaPagos();
                    limparSelecoesTabelas();
                }
            } else if (e.getSource() == txtDataInicial || e.getSource() == txtDataFinal) {
                ApresentacaoUtil.verificarDatas(e.getSource(), txtDataInicial, txtDataFinal, this);
                carregarTabelaPagos();
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
        itemMenuMudarAltura = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        popupMenuInadimplentes = new javax.swing.JPopupMenu();
        itemMenuCalcularJurosMulta = new javax.swing.JMenuItem();
        painelCondominos = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaCondominos = new javax.swing.JTable();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        painelLancamentos = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtDataVencimento = new net.sf.nachocalendar.components.DateField();
        txtNumeroDocumento = new javax.swing.JTextField();
        btnGerarCobranca = new javax.swing.JButton();
        btnImprimirBoleto = new javax.swing.JButton();
        painelCobrancaBase = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaCobrancasBase = new javax.swing.JTable();
        painelBoletos = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaCobrancas = new javax.swing.JTable();
        painelInadimplentes = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaInadimplentes = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtVencimentoProrrogado = new net.sf.nachocalendar.components.DateField();
        btnImprimirBoletoInadimplente = new javax.swing.JButton();
        painelPagos = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabelaPagos = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        txtDataInicial = new net.sf.nachocalendar.components.DateField();
        txtDataFinal = new net.sf.nachocalendar.components.DateField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnLimpar = new javax.swing.JButton();

        itemMenuMudarAltura.setText("Mudar altura");
        popupMenu.add(itemMenuMudarAltura);
        popupMenu.add(jSeparator1);

        itemMenuRemoverSelecionados.setText("Remover Selecionado(s)");
        popupMenu.add(itemMenuRemoverSelecionados);

        itemMenuCalcularJurosMulta.setText("Calcular Juros/Multa");
        popupMenuInadimplentes.add(itemMenuCalcularJurosMulta);

        setClosable(true);
        setTitle("Cobranças");
        setVisible(true);

        painelCondominos.setBorder(javax.swing.BorderFactory.createTitledBorder("Condôminos"));

        tabelaCondominos.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCondominos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(tabelaCondominos);

        javax.swing.GroupLayout painelCondominosLayout = new javax.swing.GroupLayout(painelCondominos);
        painelCondominos.setLayout(painelCondominosLayout);
        painelCondominosLayout.setHorizontalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelCondominosLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelCondominosLayout.setVerticalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCondominosLayout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.setText("Vencimento");

        btnGerarCobranca.setText("Gerar Cobrança");

        btnImprimirBoleto.setText("Imprimir Boleto");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnGerarCobranca)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnImprimirBoleto)
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtNumeroDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnGerarCobranca)
                        .addComponent(btnImprimirBoleto)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelCobrancaBase.setBorder(javax.swing.BorderFactory.createTitledBorder("Cobranças Base"));

        tabelaCobrancasBase.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCobrancasBase.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tabelaCobrancasBase);

        javax.swing.GroupLayout painelCobrancaBaseLayout = new javax.swing.GroupLayout(painelCobrancaBase);
        painelCobrancaBase.setLayout(painelCobrancaBaseLayout);
        painelCobrancaBaseLayout.setHorizontalGroup(
            painelCobrancaBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCobrancaBaseLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelCobrancaBaseLayout.setVerticalGroup(
            painelCobrancaBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCobrancaBaseLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelBoletos.setBorder(javax.swing.BorderFactory.createTitledBorder("Boletos Gerados"));

        tabelaCobrancas.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaCobrancas.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tabelaCobrancas);

        javax.swing.GroupLayout painelBoletosLayout = new javax.swing.GroupLayout(painelBoletos);
        painelBoletos.setLayout(painelBoletosLayout);
        painelBoletosLayout.setHorizontalGroup(
            painelBoletosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBoletosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelBoletosLayout.setVerticalGroup(
            painelBoletosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBoletosLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout painelLancamentosLayout = new javax.swing.GroupLayout(painelLancamentos);
        painelLancamentos.setLayout(painelLancamentosLayout);
        painelLancamentosLayout.setHorizontalGroup(
            painelLancamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLancamentosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelLancamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelCobrancaBase, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelBoletos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        painelLancamentosLayout.setVerticalGroup(
            painelLancamentosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelLancamentosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelCobrancaBase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelBoletos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Lançamentos", painelLancamentos);

        tabelaInadimplentes.setFont(new java.awt.Font("Tahoma", 0, 10));
        tabelaInadimplentes.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabelaInadimplentes);

        jLabel4.setText("Vencimento Prorrogado ");

        btnImprimirBoletoInadimplente.setText("Imprimir Boleto");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtVencimentoProrrogado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(356, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(222, 222, 222)
                    .addComponent(btnImprimirBoletoInadimplente)
                    .addContainerGap(222, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtVencimentoProrrogado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(13, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(btnImprimirBoletoInadimplente)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout painelInadimplentesLayout = new javax.swing.GroupLayout(painelInadimplentes);
        painelInadimplentes.setLayout(painelInadimplentesLayout);
        painelInadimplentesLayout.setHorizontalGroup(
            painelInadimplentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelInadimplentesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelInadimplentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelInadimplentesLayout.setVerticalGroup(
            painelInadimplentesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInadimplentesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Inadimplência", painelInadimplentes);

        tabelaPagos.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        tabelaPagos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(tabelaPagos);

        jLabel2.setText("Data Inicial ");

        jLabel3.setText("Data Final ");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(136, 136, 136)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(151, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(txtDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelPagosLayout = new javax.swing.GroupLayout(painelPagos);
        painelPagos.setLayout(painelPagosLayout);
        painelPagosLayout.setHorizontalGroup(
            painelPagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelPagosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelPagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        painelPagosLayout.setVerticalGroup(
            painelPagosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPagosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 381, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Pagos", painelPagos);

        btnLimpar.setText("Limpar Seleção");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(painelCondominos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpar)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnLimpar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(painelCondominos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGerarCobranca;
    private javax.swing.JButton btnImprimirBoleto;
    private javax.swing.JButton btnImprimirBoletoInadimplente;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JMenuItem itemMenuCalcularJurosMulta;
    private javax.swing.JMenuItem itemMenuMudarAltura;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelBoletos;
    private javax.swing.JPanel painelCobrancaBase;
    private javax.swing.JPanel painelCondominos;
    private javax.swing.JPanel painelInadimplentes;
    private javax.swing.JPanel painelLancamentos;
    private javax.swing.JPanel painelPagos;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JPopupMenu popupMenuInadimplentes;
    private javax.swing.JTable tabelaCobrancas;
    private javax.swing.JTable tabelaCobrancasBase;
    private javax.swing.JTable tabelaCondominos;
    private javax.swing.JTable tabelaInadimplentes;
    private javax.swing.JTable tabelaPagos;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private net.sf.nachocalendar.components.DateField txtDataVencimento;
    private javax.swing.JTextField txtNumeroDocumento;
    private net.sf.nachocalendar.components.DateField txtVencimentoProrrogado;
    // End of variables declaration//GEN-END:variables
}
