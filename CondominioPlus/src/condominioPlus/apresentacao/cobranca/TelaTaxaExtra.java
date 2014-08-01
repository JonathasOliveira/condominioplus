/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaTaxaExtra.java
 *
 * Created on 28/02/2011, 12:07:28
 */
package condominioPlus.apresentacao.cobranca;

import condominioPlus.apresentacao.financeiro.*;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.BoletoBancario;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.DadosCorrespondencia;
import condominioPlus.negocio.cobranca.taxaExtra.ParcelaTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.RateioTaxaExtra;
import condominioPlus.negocio.cobranca.taxaExtra.TaxaExtra;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.ContaUtil;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.util.Relatorios;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.RenderizadorCelulaADireita;
import logicpoint.apresentacao.RenderizadorCelulaData;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import net.sf.nachocalendar.table.JTableCustomizer;
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
 * @author Administrador
 */
public class TelaTaxaExtra extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TaxaExtra taxa;
    private List<TaxaExtra> listaTaxas;
    private TabelaModelo_2<TaxaExtra> modelo;
    private TabelaModelo_2<Unidade> modeloTabelaCondominos;
    private TabelaModelo_2<ParcelaTaxaExtra> modeloParcela;
    private TabelaModelo_2<RateioTaxaExtra> modeloRateio;
    private Conta conta;

    /** Creates new form TelaTaxaExtra */
    public TelaTaxaExtra(Condominio condominio) {

        this.condominio = condominio;

        initComponents();

        painelTaxaExtra.setVisible(false);
        painelRateio.setVisible(false);

        new ControladorEventos();

        verificarParcelas();
        esconderPainelCondominos();
        carregarTabelaCondominos();
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Taxa Extra - " + condominio.getRazaoSocial());
        }
    }

    private void esconderPainelCondominos() {
        this.setSize(742, 534);
        painelCondominos.setVisible(false);
        tabelaCondominos.clearSelection();
    }

    private void mostrarPainelCondominos() {
        this.setSize(916, 534);
        painelCondominos.setVisible(true);
        if (radioFracaoSim.isSelected()) {
            radioFracaoNao.setSelected(true);
        }
    }

    private void carregarTabelaCondominos() {

        modeloTabelaCondominos = new TabelaModelo_2<Unidade>(tabelaCondominos, "Unidade, Nome".split(",")) {

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

        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setMaxWidth(50);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(1)).setMinWidth(200);
        tabelaCondominos.getColumn(modeloTabelaCondominos.getCampo(0)).setCellRenderer(new RenderizadorCelulaADireita());

        tabelaCondominos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    }

    private List<Unidade> getUnidades() {

        List<Unidade> lista = condominio.getUnidades();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Unidade u1 = (Unidade) o1;
                Unidade u2 = (Unidade) o2;
                return u1.getUnidade().compareTo(u2.getUnidade());
            }
        };

        Collections.sort(lista, c);

        return lista;

    }

    public void verificarParcelas() {
        for (TaxaExtra taxaExtra : condominio.getTaxas()) {
            BigDecimal somaPagamentos = new BigDecimal(0);
            for (ParcelaTaxaExtra parcela : taxaExtra.getParcelas()) {
                for (RateioTaxaExtra rateio : parcela.getRateios()) {
                    if (rateio.getCobranca() != null && rateio.getCobranca().getDataPagamento() != null) {
                        somaPagamentos.add(rateio.getValorACobrar());
                    }
                }
            }
            if (!taxaExtra.isTotalmentePaga() && somaPagamentos.doubleValue() == taxaExtra.getValor().doubleValue()) {
                taxaExtra.setTotalmentePaga(true);
                new DAO().salvar(taxaExtra);
            }
        }
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<TaxaExtra>(tabela, "Conta, Descrição, Valor, Nº Cotas, Cobr. a Descartar, Vencimento, Sindico Paga?, Dividir Fração Ideal?, Cobrar com Condomínio?".split(",")) {

            @Override
            protected List<TaxaExtra> getCarregarObjetos() {
                return getTaxas();
            }

            @Override
            public Object getValor(TaxaExtra t, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return t.getConta().getCodigo();
                    case 1:
                        return t.getDescricao();
                    case 2:
                        return PagamentoUtil.formatarMoeda(t.getValor().doubleValue());
                    case 3:
                        return t.getNumeroCotas();
                    case 4:
                        return t.getCobrancasADescartar();
                    case 5:
                        return DataUtil.getDateTime(t.getPrimeiroVencimento());
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

        tabela.getColumn(modelo.getCampo(2)).setCellRenderer(direita);
        tabela.getColumn(modelo.getCampo(3)).setCellRenderer(direita);

        tabela.getColumn(modelo.getCampo(0)).setMaxWidth(60);
        tabela.getColumn(modelo.getCampo(1)).setMinWidth(150);
        tabela.getColumn(modelo.getCampo(2)).setMaxWidth(60);
        tabela.getColumn(modelo.getCampo(4)).setMinWidth(100);
        tabela.getColumn(modelo.getCampo(5)).setMaxWidth(80);
        tabela.getColumn(modelo.getCampo(6)).setMinWidth(80);
        tabela.getColumn(modelo.getCampo(7)).setMinWidth(110);
        tabela.getColumn(modelo.getCampo(8)).setMinWidth(130);

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

    private boolean preencherObjeto() {

        taxa = new TaxaExtra();
        taxa.setDescricao(txtHistorico.getText());
        taxa.setNumeroCotas(Integer.valueOf(txtNumeroParcelas.getText()));

        if (btnValor.isSelected()) {
            taxa.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).multiply(new BigDecimal(taxa.getNumeroCotas())));
        } else {
            taxa.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
        }

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

        taxa.setCobrancasADescartar((Integer) spnCobrancasADescartar.getValue());
        taxa.setPrimeiroVencimento(DataUtil.getCalendar(txtPrimeiroVencimento.getValue()));

        if (conta != null) {
            taxa.setConta(conta);
        }

        for (int i = 0; i < taxa.getNumeroCotas(); i++) {
            ParcelaTaxaExtra parcela = new ParcelaTaxaExtra();
            parcela.setNumeroParcela(i + 1);
            parcela.setDataVencimento(DataUtil.getCalendar(DataUtil.getDateTime(taxa.getPrimeiroVencimento()).plusMonths(i)));

            if (btnValor.isSelected()) {
                parcela.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
            } else {
                parcela.setValor(taxa.getValor().divide(new BigDecimal(taxa.getNumeroCotas())));
            }

            parcela.setTaxa(taxa);
            taxa.getParcelas().add(parcela);
        }

        taxa.setCondominio(condominio);

        if (!calcularRateio(taxa)) {
            return false;
        }

        condominio.getTaxas().add(taxa);

//        new DAO().salvar(condominio);

        if (!taxa.isCobrarComCondominio()) {
            gerarCobranca(taxa);
            imprimirCobrancas(taxa);
            ApresentacaoUtil.exibirAdvertencia("As cobranças foram geradas e seus respectivos boletos impressos.", this);
        }

        limparCampos();

        ApresentacaoUtil.exibirAdvertencia("Rateio efetuado com sucesso.", this);

        return true;

    }

    private void gerarCobranca(TaxaExtra txe) {
        for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
            for (RateioTaxaExtra rateio : parcela.getRateios()) {
                Cobranca cobranca = new Cobranca();
                cobranca.setUnidade(rateio.getUnidade());
                cobranca.setCodigoBanco(rateio.getUnidade().getCondominio().getContaBancaria().getBanco().getNumeroBanco());
                cobranca.setAgencia(rateio.getUnidade().getCondominio().getContaBancaria().getBanco().getAgencia());
                if (cobranca.getCodigoBanco().equals("033")){
                    cobranca.setNumeroConta(rateio.getUnidade().getCondominio().getContaBancaria().getCodigoCedente() );
                    cobranca.setDigitoConta(rateio.getUnidade().getCondominio().getContaBancaria().getDigitoCedente());
                } else if(cobranca.getCodigoBanco().equals("237")){
                    cobranca.setNumeroConta(rateio.getUnidade().getCondominio().getContaBancaria().getContaCorrente());
                    cobranca.setDigitoConta(rateio.getUnidade().getCondominio().getContaBancaria().getDigitoCorrente());
                }
                cobranca.setValorTotal(new BigDecimal(0));
                cobranca.setValorOriginal(new BigDecimal(0));
                cobranca.setDataVencimento(rateio.getDataVencimento());
                cobranca.setNumeroDocumento(BoletoBancario.gerarNumeroDocumento(cobranca, DataUtil.getDateTime(cobranca.getDataVencimento())));

                rateio.setCobranca(cobranca);
                Pagamento pagamento = new Pagamento();
                pagamento.setFornecedor("");
                pagamento.setDataVencimento(rateio.getDataVencimento());
                pagamento.setCobranca(cobranca);
                pagamento.setConta(txe.getConta());
                pagamento.setHistorico("Parcela " + parcela.getNumeroParcela() + "/" + txe.getParcelas().size() + " da " + " " + txe.getDescricao() + " " + cobranca.getUnidade().getUnidade() + " " + cobranca.getUnidade().getCondomino().getNome());
                pagamento.setValor(rateio.getValorACobrar());
                cobranca.getPagamentos().add(pagamento);
                cobranca.setValorTotal(cobranca.getValorTotal().add(pagamento.getValor()));
                cobranca.setValorOriginal(cobranca.getValorOriginal().add(pagamento.getValor()));
//                new DAO().salvar(rateio);

                if (rateio.getUnidade().getCondominio().getContaBancaria().getBanco().getNumeroBanco().equals("033")) {
                    cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavelSantander(cobranca));
                    cobranca.setNumeroDocumento(cobranca.getNumeroDocumento() + BoletoBancario.calculoDvNossoNumeroSantander(cobranca.getNumeroDocumento()));
                } else if (rateio.getUnidade().getCondominio().getContaBancaria().getBanco().getNumeroBanco().equals("237")) {
                    cobranca.setLinhaDigitavel(BoletoBancario.getLinhaDigitavelBradesco(cobranca));
                    cobranca.setNumeroDocumento(cobranca.getNumeroDocumento());
                }

                rateio.getUnidade().getCobrancas().add(cobranca);
            }
        }
    }

    private void imprimirCobrancas(TaxaExtra txe) {
        List<DadosCorrespondencia> listaDados = new ArrayList<DadosCorrespondencia>();

        for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
            for (RateioTaxaExtra rateio : parcela.getRateios()) {

                listaDados = DadosCorrespondencia.preencherLista(rateio.getCobranca().getUnidade(), listaDados, rateio.getCobranca().getUnidade().isBoletoProprietario(), rateio.getCobranca().getUnidade().isBoletoInquilino(), rateio.getCobranca());

            }
        }

        List<BoletoBancario> boletos = new ArrayList<BoletoBancario>();
        for (DadosCorrespondencia dados : listaDados) {
            boletos.add(BoletoBancario.gerarBoleto(dados));
        }

        /*
         * GERANDO O(S) BOLETO(S) BANCÁRIO(S).
         */

        new Relatorios().imprimirBoleto(boletos, condominio);
//        File pdf = BoletoViewer.groupInOnePDF("MeuPrimeiroBoleto.pdf", boletos);
//        BoletoBancario.mostreBoletoNaTela(pdf);
    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }

            if (!preencherObjeto()) {
                return;
            }

            System.out.println("passei do if");

            TipoAcesso tipo = null;
            if (condominio.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }

            new DAO().salvar(condominio);
            carregarTabela();

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
                    for (ParcelaTaxaExtra parcela : t.getParcelas()) {
                        for (RateioTaxaExtra rateio : parcela.getRateios()) {
                            for (Unidade u : condominio.getUnidades()) {
                                if (rateio.getCobranca().getUnidade().getCodigo() == u.getCodigo()) {
                                    u.getCobrancas().remove(rateio.getCobranca());
                                }
                            }
                        }
                    }
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
//        definirMinimoSpinner(spnDia);
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

        modeloParcela = new TabelaModelo_2<ParcelaTaxaExtra>(tabelaParcelas, "Número Parcela, Vencimento, Valor".split(",")) {

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
                        return DataUtil.getDateTime(parcela.getDataVencimento());
                    case 2:
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

    private void exibirPainelDetalhes(TaxaExtra t) {
        painelRateio.setVisible(false);
        if (t != null) {
            painelTaxaExtra.setVisible(true);
            taxa = t;
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

    public void preencherPainelDetalhes(TaxaExtra t) {
        txtCodigoConta.setText(String.valueOf(t.getConta().getCodigo()));
        txtDescricao.setText(t.getDescricao());
        txtCotas.setText(String.valueOf(t.getNumeroCotas()));
        definirMinimoSpinner(spnDiaVencimento);
        if (t.getPrimeiroVencimento() != null) {
            spnDiaVencimento.setValue(DataUtil.getDateTime(t.getPrimeiroVencimento()).getDayOfMonth());
        }
        jTextField1.setText(PagamentoUtil.formatarMoeda(t.getValor().doubleValue()));
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

    private void salvarTaxa() {
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

    private boolean calcularRateio(TaxaExtra tx) {
        List<Unidade> lista = new ArrayList<Unidade>();
        if (!modeloTabelaCondominos.getObjetosSelecionados().isEmpty()) {
            lista = modeloTabelaCondominos.getObjetosSelecionados();
        }
        for (ParcelaTaxaExtra parcela : tx.getParcelas()) {
            if (lista.isEmpty() && !chkUnidades.isSelected()) {
                int numero = tx.getCondominio().getUnidades().size();
                if (tx.isDividirFracaoIdeal()) {
                    efetuarCalculo(new BigDecimal(0), parcela);
                } else {
                    for (Unidade u : tx.getCondominio().getUnidades()) {
                        if (u.isSindico() && !tx.isSindicoPaga()) {
                            numero -= 1;
                        }
                        if (verificarInadimplencia(tx.getCobrancasADescartar(), u)) {
                            numero -= 1;
                        }
                    }
                    if (numero == 0) {
                        ApresentacaoUtil.exibirAdvertencia("Não é possível fazer o cálculo do rateio, pois nenhum número é dividido por 0.", this);
                        return false;
                    }
                    double valorRateio = 0;
                    valorRateio = valorRateio + parcela.getValor().doubleValue();
                    valorRateio = valorRateio / numero;
                    System.out.println("valor rateio " + PagamentoUtil.formatarMoeda(valorRateio));
                    efetuarCalculo(new BigDecimal(valorRateio).setScale(2, RoundingMode.UP), parcela);
                }
            } else {
                for (Unidade u : lista) {
                    RateioTaxaExtra rateio = new RateioTaxaExtra();
                    rateio.setUnidade(u);
                    rateio.setParcela(parcela);
                    rateio.setDataVencimento(parcela.getDataVencimento());
                    rateio.setValorACobrar(new BigDecimal(parcela.getValor().doubleValue() / lista.size()).setScale(2, RoundingMode.UP));
                    parcela.getRateios().add(rateio);
                }
                tabelaCondominos.clearSelection();
            }
        }
        return true;
    }

    private void efetuarCalculo(BigDecimal valor, ParcelaTaxaExtra parcela) {
        RATEIO:
        for (Unidade u : parcela.getTaxa().getCondominio().getUnidades()) {
            if (u.isSindico() && !parcela.getTaxa().isSindicoPaga()) {
                continue RATEIO;
            }

            RateioTaxaExtra rateio = new RateioTaxaExtra();
            rateio.setUnidade(u);
            rateio.setParcela(parcela);
            rateio.setDataVencimento(parcela.getDataVencimento());
            if (parcela.getTaxa().isDividirFracaoIdeal()) {
                rateio.setValorACobrar(new BigDecimal(calcularPorFracaoIdeal(u, parcela.getValor(), parcela.getTaxa().isSindicoPaga())));
            } else {
                rateio.setValorACobrar(valor);
            }
            parcela.getRateios().add(rateio);

        }
    }

    private Unidade getSindico() {
        Unidade u = new Unidade();
        for (Unidade unidade : condominio.getUnidades()) {
            if (unidade.isSindico()) {
                u = unidade;
            }
        }
        return u;
    }

    private double calcularPorFracaoIdeal(Unidade u, BigDecimal valor, boolean sindicoPaga) {
        double resultado = 0;
        double soma = 0;
        if (!sindicoPaga) {
            soma = getSindico().getFracaoIdeal().doubleValue() / (u.getCondominio().getUnidades().size() - 1);
        }
        resultado = (u.getFracaoIdeal().doubleValue() + soma) * valor.doubleValue();
//        System.out.println("resultado - " + resultado);
        return resultado;
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

    private void carregarTabelaRateio() {
        modeloRateio = new TabelaModelo_2<RateioTaxaExtra>(tabelaRateio, "Unidade,Vencimento, Valor".split(",")) {

            @Override
            protected List<RateioTaxaExtra> getCarregarObjetos() {
                return getRateios();
            }

            @Override
            public void setValor(RateioTaxaExtra rateio, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 1:
                        rateio.setDataVencimento(DataUtil.getCalendar(valor));
                        break;
                }
            }

            @Override
            public Object getValor(RateioTaxaExtra r, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return r.getUnidade().getUnidade();
                    case 1:
                        return r.getDataVencimento();
                    case 2:
                        return PagamentoUtil.formatarMoeda(r.getValorACobrar().doubleValue());
                    default:
                        return null;

                }
            }
        };

        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaRateio.getColumn(modeloRateio.getCampo(0)).setCellRenderer(direita);
        tabelaRateio.getColumn(modeloRateio.getCampo(2)).setCellRenderer(direita);

        modeloRateio.setEditaveis(1);

        tabelaRateio.getColumn(modeloRateio.getCampo(1)).setCellRenderer(new RenderizadorCelulaData());
        JTableCustomizer.setEditorForRow(tabelaRateio, 1);

    }

    public List<RateioTaxaExtra> getRateios() {
        List<RateioTaxaExtra> lista = modeloParcela.getObjetoSelecionado().getRateios();

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                RateioTaxaExtra r1 = (RateioTaxaExtra) o1;
                RateioTaxaExtra r2 = (RateioTaxaExtra) o2;
                return r1.getUnidade().getUnidade().compareTo(r2.getUnidade().getUnidade());
            }
        };

        Collections.sort(lista, c);

        return lista;
    }

    private void trocarFormaPagamento() {
        if (btnValor.isSelected()) {
            btnValor.setText("Valor Parcela:");
        } else {
            btnValor.setText("Valor Rateio:");
        }

    }

    private void imprimirDetalheTaxaExtra(TaxaExtra txe) {

        Moeda totalOriginal = new Moeda();
        Moeda totalGerado = new Moeda();

        Comparator<ParcelaTaxaExtra> comparador = new Comparator<ParcelaTaxaExtra>() {

            public int compare(ParcelaTaxaExtra o1, ParcelaTaxaExtra o2) {
                return o1.getDataVencimento().compareTo(o2.getDataVencimento());
            }
        };

        List<HashMap<String, String>> listaParcelas = new ArrayList<HashMap<String, String>>();
        Collections.sort(txe.getParcelas(), comparador);

        Moeda totalAArrecadar = new Moeda();
        Moeda totalArrecadado = new Moeda();
        Moeda totalInadimplencia = new Moeda();

        for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
            Moeda valorAArrecadar = new Moeda(parcela.getValor());
            totalAArrecadar.soma(parcela.getValor());
            Moeda valorArrecadado = new Moeda();
            Moeda valorInadimplencia = new Moeda();
            for (RateioTaxaExtra rateio : parcela.getRateios()) {
                if (rateio.getCobranca() != null && rateio.getCobranca().getDataPagamento() != null) {
                    valorArrecadado.soma(rateio.getCobranca().getValorPago());
                    totalArrecadado.soma(rateio.getCobranca().getValorPago());
                } else {
                    valorInadimplencia.soma(rateio.getValorACobrar());
                    totalInadimplencia.soma(rateio.getValorACobrar());
                }
            }
            HashMap<String, String> mapa = new HashMap();
            mapa.put("vencimento", DataUtil.toString(parcela.getDataVencimento()));
            mapa.put("arrecadar", PagamentoUtil.formatarMoeda(valorAArrecadar.doubleValue()));
            mapa.put("arrecadado", PagamentoUtil.formatarMoeda(valorArrecadado.doubleValue()));
            mapa.put("inadimplencia", PagamentoUtil.formatarMoeda(valorInadimplencia.doubleValue()));
            listaParcelas.add(mapa);
        }

        HashMap<String, Object> parametros = new HashMap();
        parametros.put("codigo", String.valueOf(txe.getCodigo()));
        parametros.put("totalAArrecadar", PagamentoUtil.formatarMoeda(totalAArrecadar.doubleValue()));
        parametros.put("totalArrecadado", PagamentoUtil.formatarMoeda(totalArrecadado.doubleValue()));
        parametros.put("totalInadimplencia", PagamentoUtil.formatarMoeda(totalInadimplencia.doubleValue()));
        parametros.put("condominio", txe.getCondominio().getRazaoSocial());
        parametros.put("conta", "" + txe.getConta().getCodigo());
        parametros.put("historico", txe.getDescricao());
        parametros.put("totalOriginal", PagamentoUtil.formatarMoeda(totalOriginal.doubleValue()));
        parametros.put("totalGerado", PagamentoUtil.formatarMoeda(totalGerado.doubleValue()));
        parametros.put("igualPraTodos", txe.isDividirFracaoIdeal() ? "Não" : "Sim");
        parametros.put("fracaoIdeal", txe.isDividirFracaoIdeal() ? "Sim" : "Não");
        parametros.put("numeroCotas", "" + txe.getNumeroCotas());
        parametros.put("sindicoPaga", txe.isSindicoPaga() ? "Sim" : "Não");
        parametros.put("qtdeInadimplentes", "" + txe.getCobrancasADescartar());
        parametros.put("cobrarComCondominio", txe.isCobrarComCondominio() ? "Sim" : "Não");

        new Relatorios().imprimir("RelatorioDetalheTaxaExtra", parametros, listaParcelas, false, true, null);
    }

    private void imprimirRelatorioGlobal() {

        DialogoDadosRelatorioGerencial dialogo = new DialogoDadosRelatorioGerencial(null, true, DataUtil.getPrimeiroDiaMes(), DataUtil.getUltimoDiaMes(), TipoRelatorio.TAXA_EXTRA_GERENCIAL);
        dialogo.setVisible(true);

        if (dialogo.getDataInicial() != null && dialogo.getDataFinal() != null) {

            List<HashMap<String, String>> listaParcelas = new ArrayList<HashMap<String, String>>();

            for (TaxaExtra txe : condominio.getTaxas()) {
                Moeda totalAArrecadar = new Moeda();
                Moeda totalArrecadado = new Moeda();
                Moeda totalInadimplencia = new Moeda();
                for (ParcelaTaxaExtra parcela : txe.getParcelas()) {
                    if (DataUtil.compararData(DataUtil.getDateTime(parcela.getDataVencimento()), dialogo.getDataInicial()) == 1 && DataUtil.compararData(DataUtil.getDateTime(parcela.getDataVencimento()), dialogo.getDataFinal()) == -1) {
                        Moeda valorAArrecadar = new Moeda(parcela.getValor());
                        totalAArrecadar.soma(parcela.getValor());
                        Moeda valorArrecadado = new Moeda();
                        Moeda valorInadimplencia = new Moeda();
                        for (RateioTaxaExtra rateio : parcela.getRateios()) {
                            if (rateio.getCobranca() != null && rateio.getCobranca().getDataPagamento() != null) {
                                valorArrecadado.soma(rateio.getCobranca().getValorPago());
                                totalArrecadado.soma(rateio.getCobranca().getValorPago());
                            } else {
                                valorInadimplencia.soma(rateio.getValorACobrar());
                                totalInadimplencia.soma(rateio.getValorACobrar());
                            }
                        }
                    }
                }
                HashMap<String, String> mapa = new HashMap();
                mapa.put("conta", "" + txe.getConta().getCodigo());
                mapa.put("historico", txe.getDescricao());
                mapa.put("totalAArrecadar", PagamentoUtil.formatarMoeda(totalAArrecadar.doubleValue()));
                mapa.put("totalArrecadado", PagamentoUtil.formatarMoeda(totalArrecadado.doubleValue()));
                mapa.put("totalInadimplencia", PagamentoUtil.formatarMoeda(totalInadimplencia.doubleValue()));
                listaParcelas.add(mapa);
            }

            HashMap<String, Object> parametros = new HashMap();
            parametros.put("condominio", condominio.getRazaoSocial());
            parametros.put("periodo", DataUtil.toString(dialogo.getDataInicial()) + " a " + DataUtil.toString(dialogo.getDataFinal()));

            new Relatorios().imprimir("RelatorioGlobalTaxaExtra", parametros, listaParcelas, false, true, null);
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == btnIncluir) {
                salvar();
            } else if (origem == itemMenuRemoverSelecionados) {
                remover();
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnVoltar) {
                cancelar();
            } else if (origem == btnSalvar) {
                salvarTaxa();
            } else if (origem == chkUnidades) {
                if (chkUnidades.isSelected()) {
                    mostrarPainelCondominos();
                } else {
                    esconderPainelCondominos();
                }
            } else if (origem == btnLimpar) {
                tabelaCondominos.clearSelection();
            } else if (origem == btnValor) {
                trocarFormaPagamento();
            } else if (origem == btnImprimir) {
                imprimirRelatorioGlobal();
            } else if (origem == itemMenuImprimir) {
                imprimirDetalheTaxaExtra(modelo.getObjetoSelecionado());
            }
        }

        @Override
        public void configurar() {
            btnConta.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnIncluir.addActionListener(this);
            btnLimpar.addActionListener(this);
            btnValor.addActionListener(this);
            tabela.addMouseListener(this);
            txtConta.addFocusListener(this);
            itemMenuImprimir.addActionListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            btnVoltar.addActionListener(this);
            radioSindicoSim.addActionListener(this);
            txtValor.addFocusListener(this);
            radioSindicoNao.addActionListener(this);
            btnSalvar.addActionListener(this);
            tabelaParcelas.addMouseListener(this);
            tabela.addKeyListener(this);
            tabelaParcelas.addKeyListener(this);
            chkUnidades.addActionListener(this);
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaTaxaExtra.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
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
            if (origem == tabela && e.getClickCount() == 1) {
                painelParcela.setVisible(true);
                exibirPainelDetalhes(modelo.getObjetoSelecionado());
            } else if (origem == tabelaParcelas && e.getClickCount() == 1) {
                painelRateio.setVisible(true);
                carregarTabelaRateio();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            origem = e.getSource();
            if ((origem == tabela && painelTaxaExtra.isVisible()) && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
                exibirPainelDetalhes(modelo.getObjetoSelecionado());
            } else if (origem == tabelaParcelas && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
                carregarTabelaRateio();
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
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        itemMenuImprimir = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        painelCondominos = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaCondominos = new javax.swing.JTable();
        btnLimpar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        txtConta = new javax.swing.JTextField();
        txtValor = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnIncluir = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtNumeroParcelas = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        spnCobrancasADescartar = new javax.swing.JSpinner();
        txtPrimeiroVencimento = new net.sf.nachocalendar.components.DateField();
        jPanel2 = new javax.swing.JPanel();
        radioFracaoSim = new javax.swing.JRadioButton();
        radioFracaoNao = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        radioCondominioSim = new javax.swing.JRadioButton();
        radioCondominioNao = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        radioSindicoSim = new javax.swing.JRadioButton();
        radioSindicoNao = new javax.swing.JRadioButton();
        jLabel13 = new javax.swing.JLabel();
        chkUnidades = new javax.swing.JCheckBox();
        btnValor = new javax.swing.JToggleButton();
        txtHistorico = new javax.swing.JTextField();
        btnImprimir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        painelTaxaExtra = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtCodigoConta = new javax.swing.JTextField();
        txtDescricao = new javax.swing.JTextField();
        txtCotas = new javax.swing.JTextField();
        btnVoltar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        radioDetalheSindicoSim = new javax.swing.JRadioButton();
        radioDetalheSindicoNao = new javax.swing.JRadioButton();
        radioDetalheFracaoSim = new javax.swing.JRadioButton();
        radioDetalheFracaoNao = new javax.swing.JRadioButton();
        jLabel17 = new javax.swing.JLabel();
        radioDetalheCondominioSim = new javax.swing.JRadioButton();
        radioDetalheCondominioNao = new javax.swing.JRadioButton();
        jLabel18 = new javax.swing.JLabel();
        spnDiaVencimento = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        spnCobrancasADescartarDetalhe = new javax.swing.JSpinner();
        jTextField1 = new javax.swing.JTextField();
        painelRateio = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaRateio = new javax.swing.JTable();
        painelParcela = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaParcelas = new javax.swing.JTable();

        itemMenuRemoverSelecionados.setText("Remover Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        itemMenuImprimir.setText("Imprimir");
        popupMenu.add(itemMenuImprimir);

        setClosable(true);
        setTitle("Taxa Extra");
        setPreferredSize(new java.awt.Dimension(916, 534));
        setRequestFocusEnabled(false);
        setVisible(true);

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
        tabelaCondominos.setToolTipText("");
        jScrollPane4.setViewportView(tabelaCondominos);

        btnLimpar.setText("Limpar Seleção");

        javax.swing.GroupLayout painelCondominosLayout = new javax.swing.GroupLayout(painelCondominos);
        painelCondominos.setLayout(painelCondominosLayout);
        painelCondominosLayout.setHorizontalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCondominosLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(btnLimpar)
                .addContainerGap(34, Short.MAX_VALUE))
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
        );
        painelCondominosLayout.setVerticalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCondominosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLimpar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
        );

        jPanel4.setPreferredSize(new java.awt.Dimension(700, 483));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel1.setPreferredSize(new java.awt.Dimension(714, 118));

        txtConta.setName("Conta"); // NOI18N

        txtValor.setName("Valor"); // NOI18N

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        jLabel2.setText("Histórico:");

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Gerar Taxa Extra");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel4.setText("Nº de Cotas:");

        txtNumeroParcelas.setName("Nº Parcelas"); // NOI18N

        jLabel5.setText("1º Venc.:");

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setPreferredSize(new java.awt.Dimension(238, 37));

        buttonGroup2.add(radioFracaoSim);
        radioFracaoSim.setText("Dividir Fração Ideal");

        buttonGroup2.add(radioFracaoNao);
        radioFracaoNao.setSelected(true);
        radioFracaoNao.setText("Igual para todos");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioFracaoSim)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioFracaoNao)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioFracaoSim)
                    .addComponent(radioFracaoNao))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setPreferredSize(new java.awt.Dimension(311, 37));

        jLabel14.setText("Cobrar com Cota Condomínio?");

        buttonGroup3.add(radioCondominioSim);
        radioCondominioSim.setText("Sim");

        buttonGroup3.add(radioCondominioNao);
        radioCondominioNao.setSelected(true);
        radioCondominioNao.setText("Não");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioCondominioSim)
                .addGap(2, 2, 2)
                .addComponent(radioCondominioNao)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(radioCondominioNao)
                    .addComponent(radioCondominioSim))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setPreferredSize(new java.awt.Dimension(167, 37));

        jLabel12.setText("Síndico Paga?");

        buttonGroup1.add(radioSindicoSim);
        radioSindicoSim.setText("Sim");

        buttonGroup1.add(radioSindicoNao);
        radioSindicoNao.setSelected(true);
        radioSindicoNao.setText("Não");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addGap(2, 2, 2)
                .addComponent(radioSindicoSim)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioSindicoNao)
                .addContainerGap(4, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addContainerGap())
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioSindicoNao)
                    .addComponent(radioSindicoSim))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel13.setText("<html>Qtde. Cobranças ñ<br>Pagas a Descartar:</html>");

        chkUnidades.setText("Unidades");
        chkUnidades.setFocusable(false);

        btnValor.setText("Valor Rateio:");
        btnValor.setBorderPainted(false);
        btnValor.setContentAreaFilled(false);
        btnValor.setFocusPainted(false);
        btnValor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnValor.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Relatório Gerencial");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkUnidades)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPrimeiroVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(btnValor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spnCobrancasADescartar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(btnValor)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(btnConta))
                                .addGap(2, 2, 2)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(chkUnidades)
                                        .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(txtPrimeiroVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnCobrancasADescartar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(9, 9, 9))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnImprimir)
                            .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        painelTaxaExtra.setPreferredSize(new java.awt.Dimension(714, 226));

        jLabel8.setText("Valor:");

        jLabel9.setText("Nº Cotas:");

        jLabel10.setText("Descrição:");

        jLabel11.setText("Conta:");

        txtCodigoConta.setBackground(new java.awt.Color(204, 204, 204));
        txtCodigoConta.setEditable(false);
        txtCodigoConta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtCotas.setBackground(new java.awt.Color(204, 204, 204));
        txtCotas.setEditable(false);
        txtCotas.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        btnVoltar.setText("Voltar");

        btnSalvar.setText("Salvar");

        jLabel15.setText("Síndico Paga?");

        buttonGroup4.add(radioDetalheSindicoSim);
        radioDetalheSindicoSim.setText("Sim");
        radioDetalheSindicoSim.setEnabled(false);

        buttonGroup4.add(radioDetalheSindicoNao);
        radioDetalheSindicoNao.setSelected(true);
        radioDetalheSindicoNao.setText("Não");
        radioDetalheSindicoNao.setEnabled(false);

        buttonGroup5.add(radioDetalheFracaoSim);
        radioDetalheFracaoSim.setText("Dividir Fração Ideal");
        radioDetalheFracaoSim.setEnabled(false);

        buttonGroup5.add(radioDetalheFracaoNao);
        radioDetalheFracaoNao.setSelected(true);
        radioDetalheFracaoNao.setText("Igual para todos");
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

        painelRateio.setPreferredSize(new java.awt.Dimension(326, 89));

        tabelaRateio.setModel(new javax.swing.table.DefaultTableModel(
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
        tabelaRateio.setToolTipText("Clique no registro para visualizar os detalhes.");
        jScrollPane3.setViewportView(tabelaRateio);

        javax.swing.GroupLayout painelRateioLayout = new javax.swing.GroupLayout(painelRateio);
        painelRateio.setLayout(painelRateioLayout);
        painelRateioLayout.setHorizontalGroup(
            painelRateioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
        );
        painelRateioLayout.setVerticalGroup(
            painelRateioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelRateioLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelParcela.setPreferredSize(new java.awt.Dimension(736, 89));

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
        tabelaParcelas.setPreferredSize(new java.awt.Dimension(736, 89));
        jScrollPane2.setViewportView(tabelaParcelas);

        javax.swing.GroupLayout painelParcelaLayout = new javax.swing.GroupLayout(painelParcela);
        painelParcela.setLayout(painelParcelaLayout);
        painelParcelaLayout.setHorizontalGroup(
            painelParcelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        painelParcelaLayout.setVerticalGroup(
            painelParcelaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelParcelaLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelTaxaExtraLayout = new javax.swing.GroupLayout(painelTaxaExtra);
        painelTaxaExtra.setLayout(painelTaxaExtraLayout);
        painelTaxaExtraLayout.setHorizontalGroup(
            painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                .addGap(285, 285, 285)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVoltar))
            .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelTaxaExtraLayout.createSequentialGroup()
                        .addComponent(radioDetalheFracaoSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDetalheFracaoNao)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDetalheSindicoSim)
                        .addGap(2, 2, 2)
                        .addComponent(radioDetalheSindicoNao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDetalheCondominioSim)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioDetalheCondominioNao))
                    .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                        .addComponent(painelParcela, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(painelRateio, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelTaxaExtraLayout.createSequentialGroup()
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(spnDiaVencimento)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(txtCodigoConta, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(spnCobrancasADescartarDetalhe)
                            .addComponent(jLabel20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCotas, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(217, 217, 217))
        );
        painelTaxaExtraLayout.setVerticalGroup(
            painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelTaxaExtraLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                            .addComponent(jLabel18)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(spnDiaVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                            .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel11)
                                .addComponent(jLabel10))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCodigoConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addComponent(spnCobrancasADescartarDetalhe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(painelTaxaExtraLayout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCotas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioDetalheFracaoSim)
                    .addComponent(radioDetalheFracaoNao)
                    .addComponent(radioDetalheCondominioNao)
                    .addComponent(radioDetalheCondominioSim)
                    .addComponent(jLabel17)
                    .addComponent(radioDetalheSindicoSim)
                    .addComponent(jLabel15)
                    .addComponent(radioDetalheSindicoNao))
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelParcela, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelRateio, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelTaxaExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(btnSalvar))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
                    .addComponent(painelTaxaExtra, javax.swing.GroupLayout.Alignment.LEADING, 0, 692, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 692, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelTaxaExtra, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelCondominos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelCondominos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JToggleButton btnValor;
    private javax.swing.JButton btnVoltar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.JCheckBox chkUnidades;
    private javax.swing.JMenuItem itemMenuImprimir;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel painelCondominos;
    private javax.swing.JPanel painelParcela;
    private javax.swing.JPanel painelRateio;
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
    private javax.swing.JSpinner spnDiaVencimento;
    private javax.swing.JTable tabela;
    private javax.swing.JTable tabelaCondominos;
    private javax.swing.JTable tabelaParcelas;
    private javax.swing.JTable tabelaRateio;
    private javax.swing.JTextField txtCodigoConta;
    private javax.swing.JTextField txtConta;
    private javax.swing.JTextField txtCotas;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroParcelas;
    private net.sf.nachocalendar.components.DateField txtPrimeiroVencimento;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
