/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaAgua.java
 *
 * Created on 18/05/2011, 16:04:14
 */
package condominioPlus.apresentacao.cobranca.agua;

import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.agua.ContaAgua;
import condominioPlus.negocio.cobranca.agua.FormaCalculoMetroCubico;
import condominioPlus.negocio.cobranca.agua.FormaRateioAreaComum;
import condominioPlus.negocio.cobranca.agua.HidrometroAreaComum;
import condominioPlus.negocio.cobranca.agua.ParametrosCalculoAgua;
import condominioPlus.negocio.cobranca.agua.Pipa;
import condominioPlus.negocio.cobranca.agua.Rateio;
import condominioPlus.negocio.cobranca.agua.TarifaProlagos;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.util.FormatadorNumeros;
import condominioPlus.util.Relatorios;
import condominioPlus.util.RenderizadorCelulaCorGenerico;
import condominioPlus.util.RenderizadorCelulaDireita;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.RenderizadorCelulaADireita;
import logicpoint.apresentacao.RenderizadorCelulaCentralizada;
import logicpoint.apresentacao.RenderizadorCelulaData;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import logicpoint.util.Util;
import net.sf.nachocalendar.table.JTableCustomizer;

/**
 *
 * @author Administrador
 */
public class TelaAgua extends javax.swing.JInternalFrame {

    private TabelaModelo_2<TarifaProlagos> modelo;
    private TabelaModelo_2<ContaAgua> modeloContaAgua;
    private TabelaModelo_2<Rateio> modeloRateio;
    private TabelaModelo_2<Pipa> modeloPipa;
    private ControladorEventos controlador;
    private ParametrosCalculoAgua parametros;
    private Condominio condominio;
    private ContaAgua conta;

    /** Creates new form TelaAgua */
    public TelaAgua(Condominio condominio) {
        initComponents();
        carregarComboFormaPrecoMetroCubico();
        carregarComboFormaRateioAreaComum();

        this.condominio = condominio;
        if (condominio.getParametros() != null) {
            this.parametros = condominio.getParametros();
        } else {
            parametros = new ParametrosCalculoAgua();
            condominio.setParametros(parametros);
            new DAO().salvar(condominio);
        }

        carregarTabela();
        carregarTabelaContaAgua();
        tabelaContaAgua.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        controlador = new ControladorEventos();



        preencherTela();

        if (modeloContaAgua.size() > 0) {
            conta = modeloContaAgua.getObjeto(0);
            carregarTabelaPipa();
            carregarTabelaRateio();
            modeloContaAgua.selecionar(conta, 0);
            preencherTelaHidrometro();
        }
    }

    private void carregarComboFormaPrecoMetroCubico() {
        cbFormaCalculoMetroCubico.setModel(new ComboModelo<String>(Util.toList(new String[]{FormaCalculoMetroCubico.SEM_VALOR.toString(), FormaCalculoMetroCubico.DIVIDIR_METROS_CUBICOS.toString(), FormaCalculoMetroCubico.SINDICO_PRECO.toString(), FormaCalculoMetroCubico.TABELA_PROLAGOS.toString()})));
    }

    private void carregarComboFormaRateioAreaComum() {
        cbFormaRateioAreaComum.setModel(new ComboModelo<String>(Util.toList(new String[]{FormaRateioAreaComum.SEM_VALOR.toString(), FormaRateioAreaComum.IGUAL_TODOS.toString(), FormaRateioAreaComum.NAO_COBRAR.toString(), FormaRateioAreaComum.PROPORCIONAL_CONSUMO.toString(), FormaRateioAreaComum.PROPORCIONAL_FRACAO.toString(), FormaRateioAreaComum.VALOR_FIXO.toString()})));
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<TarifaProlagos>(tabela, "Consumo Inicial, Consumo Final, Valor".split(",")) {

            @Override
            protected List<TarifaProlagos> getCarregarObjetos() {
                return getTarifaProlagos();
            }

            @Override
            public Object getValor(TarifaProlagos tarifa, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return tarifa.getConsumoInicial();
                    case 1:
                        return tarifa.getConsumoFinal();
                    case 2:
                        return PagamentoUtil.formatarMoeda(tarifa.getValor().doubleValue());
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaDireita renderizadorCelulaDireita = new RenderizadorCelulaDireita(modelo);
        tabela.getColumn(modelo.getCampo(2)).setCellRenderer(renderizadorCelulaDireita);

    }

    private void carregarTabelaContaAgua() {
        modeloContaAgua = new TabelaModelo_2<ContaAgua>(tabelaContaAgua, "Data Inicial, Data Final, Valor Prolagos(R$), Consumo Prolagos(M3), Valor Pipa (R$), Consumo Pipa(M3), Preço(M3), Data Vencimento da Conta, Consumo Unidades(M3), Consumo Total Unidades(R$), Consumo Area Comum(M3), Consumo Area Comum(R$), Total Despesas".split(",")) {

            @Override
            protected List<ContaAgua> getCarregarObjetos() {
                return getContasAgua();
            }

            @Override
            public void setValor(ContaAgua conta, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        conta.setDataInicial(DataUtil.getCalendar(valor));
                        break;
                    case 1:
                        conta.setDataFinal(DataUtil.getCalendar(valor));
                        break;
                    case 2:
                        conta.setValorProlagos(((Moeda) valor).bigDecimalValue());
                        break;
                    case 3:
                        conta.setConsumoProlagos((BigDecimal) valor);
                        break;
                    case 6:
                        conta.setPrecoMetroCubico(((Moeda) valor).bigDecimalValue());
                        break;
                    case 7:
                        conta.setDataVencimentoConta(DataUtil.getCalendar(valor));
                        System.out.println("conta vencimento " + DataUtil.getDateTime(conta.getDataVencimentoConta()));
                        break;



                }
            }

            @Override
            public Object getValor(ContaAgua conta, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(conta.getDataInicial());
                    case 1:
                        return DataUtil.getDateTime(conta.getDataFinal());
                    case 2:
                        return new Moeda(conta.getValorProlagos());
                    case 3:
                        return conta.getConsumoProlagos();
                    case 4:
                        return new Moeda(conta.getValorPipa());
                    case 5:
                        return conta.getConsumoPipa();
                    case 6:
                        return new Moeda(conta.getPrecoMetroCubico());
                    case 7:
                        return DataUtil.getDateTime(conta.getDataVencimentoConta());
                    case 8:
                        return conta.getConsumoUnidadesMetroCubico();
                    case 9:
                        return new Moeda(conta.getPrecoTotalUnidades());
                    case 10:
                        return conta.getConsumoAreaComum();
                    case 11:
                        return new Moeda(conta.getPrecoAreaComum());

                    case 12:
                        return new Moeda(conta.getTotalDespesasPipa());
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaADireita direito = new RenderizadorCelulaADireita();
        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        tabelaContaAgua.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        modeloContaAgua.setEditaveis(0, 1, 2, 3, 7);

        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(0)).setCellRenderer(new RenderizadorCelulaData());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(1)).setCellRenderer(new RenderizadorCelulaData());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(7)).setCellRenderer(new RenderizadorCelulaData());
        JTableCustomizer.setEditorForRow(tabelaContaAgua, 0);
        JTableCustomizer.setEditorForRow(tabelaContaAgua, 1);
        JTableCustomizer.setEditorForRow(tabelaContaAgua, 7);



        for (int i = 2; i < 7; i++) {
            tabelaContaAgua.getColumn(modeloContaAgua.getCampo(i)).setCellRenderer(direito);

        }
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(8)).setCellRenderer(centralizado);

        for (int i = 0; i <= 12; i++) {
            tabelaContaAgua.getColumn(modeloContaAgua.getCampo(i)).setMinWidth(150);

        }

        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(4)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(5)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(6)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(8)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(9)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(10)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(11)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(12)).setCellRenderer(new RenderizadorCelulaCorGenerico());

        tabelaContaAgua.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "selectNextColumnCell");


    }

    private BigDecimal verificarValor(Object valor) {

        if (valor instanceof String) {
            return FormatadorNumeros.casasDecimais(3, new BigDecimal(((String) valor).replaceAll(",", ".")));
        }

        return FormatadorNumeros.casasDecimais(3, new BigDecimal(((BigDecimal) valor).toString().replaceAll(",", ".")));

    }

    private void carregarTabelaRateio() {
        modeloRateio = new TabelaModelo_2<Rateio>(tabelaRateio, "Unidade, Fração Ideal, Leitura Anterior, Leitura Atual, Consumo(M3), Consumo a Cobrar(M3), Valor(M3), Percentual, Valor PIPA(R$), Total Unidades(R$), Percentual Area Comum, Consumo Área Comum(M3), Valor Area Comum(R$), Valor Total a Cobrar ".split(",")) {

            @Override
            protected List<Rateio> getCarregarObjetos() {
                return getUnidadesRateio();
            }

            @Override
            public void setValor(Rateio rateio, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 3:
                        rateio.setLeituraAtual(verificarValor(valor));
                        break;
                }
            }

            @Override
            public Object getValor(Rateio rateio, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return rateio.getUnidade().getUnidade();
                    case 1:
                        return rateio.getUnidade().getFracaoIdeal();
                    case 2:
                        return rateio.getLeituraAnterior();
                    case 3:
                        return rateio.getLeituraAtual();
                    case 4:
                        return rateio.getConsumoMetroCubico();
                    case 5:
                        return rateio.getConsumoMetroCubicoACobrar();
                    case 6:
                        return new Moeda(rateio.getValorDoMetroCubico());
                    case 7:
                        return FormatadorNumeros.casasDecimais(2, rateio.getPercentualGasto());
                    case 8:
                        return rateio.getValorRateioPipa();
                    case 9:
                        return new Moeda(rateio.getValorTotalConsumido());
                    case 10:
                        return FormatadorNumeros.casasDecimais(2, rateio.getPercentualRateioAreaComum());
                    case 11:
                        return FormatadorNumeros.casasDecimais(3, rateio.getConsumoMetroCubicoAreaComum());
                    case 12:
                        return new Moeda(rateio.getConsumoEmDinheiroAreaComum());
                    case 13:
                        return new Moeda(rateio.getValorTotalCobrar());
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaADireita direito = new RenderizadorCelulaADireita();
        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        tabelaRateio.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        modeloRateio.setEditaveis(3);



        for (int i = 0; i < 13; i++) {
            tabelaRateio.getColumn(modeloRateio.getCampo(i)).setCellRenderer(direito);

        }
        tabelaRateio.getColumn(modeloRateio.getCampo(8)).setCellRenderer(centralizado);
        tabelaRateio.getColumn(modeloRateio.getCampo(3)).setCellRenderer(new RenderizadorCelulaCorGenerico());

        for (int i = 2; i <= 13; i++) {
            tabelaRateio.getColumn(modeloRateio.getCampo(i)).setMinWidth(150);

        }

        tabelaRateio.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "selectNextCell");


    }

    private void carregarTabelaPipa() {
        modeloPipa = new TabelaModelo_2<Pipa>(tabelaPipa, "Data, Descrição, Quantidade x 1000 , Total Pago ".split(",")) {

            @Override
            protected List<Pipa> getCarregarObjetos() {
                return getPipas();
            }

            @Override
            public void setValor(Pipa pipa, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        pipa.setData(DataUtil.getCalendar(valor));
                        break;
                    case 1:
                        pipa.setDescricao((String) valor);
                        break;
                    case 2:
                        pipa.setQuantidadeLitrosPorMil((Integer) valor);
                        break;
                    case 3:
                        pipa.setTotalPago(((Moeda) valor).bigDecimalValue());
                        break;
                }
            }

            @Override
            public Object getValor(Pipa pipa, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pipa.getDataCadastro());
                    case 1:
                        return pipa.getDescricao();
                    case 2:
                        return pipa.getQuantidadeLitrosPorMil();
                    case 3:
                        return new Moeda(pipa.getTotalPago());
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        modeloPipa.setEditaveis(0, 1, 2, 3);


        for (int i = 0; i <= 3; i++) {
            tabelaPipa.getColumn(modeloPipa.getCampo(0)).setCellRenderer(centralizado);
        }

        tabelaPipa.getColumn(modeloPipa.getCampo(0)).setCellRenderer(new RenderizadorCelulaData());
        JTableCustomizer.setEditorForRow(tabelaPipa, 0);

        tabelaPipa.getColumn(modeloPipa.getCampo(0)).setMinWidth(100);
        tabelaPipa.getColumn(modeloPipa.getCampo(1)).setMinWidth(200);
        tabelaPipa.getColumn(modeloPipa.getCampo(2)).setMinWidth(100);
        tabelaPipa.getColumn(modeloPipa.getCampo(3)).setMinWidth(100);

        tabelaPipa.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "selectNextColumnCell");


    }

    private List<Pipa> getPipas() {
        return conta.getPipas();
    }

    private List<Rateio> getUnidadesRateio() {
        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Rateio r1 = (Rateio) o1;
                Rateio r2 = (Rateio) o2;
                return (r1.getUnidade().getUnidade()).compareTo(r2.getUnidade().getUnidade());
            }
        };

        Collections.sort(conta.getRateios(), c);



        return conta.getRateios();
    }

    private void verificarParametrosMetroCubico(Rateio rateio) {
        FormaCalculoMetroCubico parametro = condominio.getParametros().getFormaMetroCubico();
        if (parametro == FormaCalculoMetroCubico.DIVIDIR_METROS_CUBICOS) {
            if (conta.getValorProlagos() != null && conta.getConsumoProlagos() != null) {
                if (checkNaoCobrarPipa.isSelected()) {
                    double valor = (conta.getValorProlagos().doubleValue() + conta.getValorPipa().doubleValue()) / conta.getConsumoProlagos().doubleValue();
                    conta.setPrecoMetroCubico(new BigDecimal(valor));
                    rateio.setValorDoMetroCubico(new BigDecimal(0).add(new BigDecimal(valor)));
                } else {
                    double valor = (conta.getValorProlagos().doubleValue() + conta.getValorPipa().doubleValue()) / (conta.getConsumoProlagos().doubleValue() + conta.getConsumoPipa().doubleValue());
                    conta.setPrecoMetroCubico(new BigDecimal(valor));
                    rateio.setValorDoMetroCubico(new BigDecimal(0).add(new BigDecimal(valor)));
                }
            } else {
                conta.setPrecoMetroCubico(BigDecimal.ZERO);
                rateio.setValorDoMetroCubico(BigDecimal.ZERO);
            }

        } else if (parametro == FormaCalculoMetroCubico.SINDICO_PRECO) {
            if (condominio.getParametros().getValorMetroCubicoSindico() != null && condominio.getParametros().getValorMetroCubicoSindico().intValue() > 0) {
                conta.setPrecoMetroCubico(condominio.getParametros().getValorMetroCubicoSindico());
                rateio.setValorDoMetroCubico(condominio.getParametros().getValorMetroCubicoSindico());
            } else {
                ApresentacaoUtil.exibirInformacao("Digite na aba de parametros um valor de metro cúbico especificado pelo síndico!", this);
            }
        } else if (parametro == FormaCalculoMetroCubico.TABELA_PROLAGOS) {
            for (TarifaProlagos t : getTarifaProlagos()) {
                if (rateio.getConsumoMetroCubicoACobrar().doubleValue() >= t.getConsumoInicial().doubleValue() && rateio.getConsumoMetroCubicoACobrar().doubleValue() <= t.getConsumoFinal().doubleValue()) {
                    conta.setPrecoMetroCubico(new BigDecimal(0));
                    rateio.setValorDoMetroCubico(t.getValor());
                } else {
                    //System.out.println("ferrou tudo meu irmao!");
                }
            }
        }

    }

    private void verificarParametrosAreaComum(Rateio rateio) {
        FormaRateioAreaComum parametro = condominio.getParametros().getFormaAreaComum();

        if (parametro == FormaRateioAreaComum.IGUAL_TODOS) {
            if (conta.getConsumoAreaComum().intValue() > 0) {
                BigDecimal valorTotal = conta.getValorProlagos().add(conta.getValorPipa());
                BigDecimal valorResiduo = valorTotal.subtract(conta.getPrecoTotalUnidades());
                Moeda valorMetroCubico = new Moeda(valorResiduo.divide(conta.getConsumoAreaComum(), RoundingMode.UP));
                double total = conta.getConsumoAreaComum().doubleValue() / condominio.getUnidades().size();
                if (total < 0) {
                    rateio.setConsumoMetroCubicoAreaComum(BigDecimal.ZERO);
                } else {
                    rateio.setConsumoMetroCubicoAreaComum(new BigDecimal(total));
                }

                Moeda consumoEmDinheiro = new Moeda(total).multiplica(valorMetroCubico);
                if (consumoEmDinheiro.doubleValue() < 0) {
                    rateio.setConsumoEmDinheiroAreaComum(new BigDecimal(0));
                } else {
                    rateio.setConsumoEmDinheiroAreaComum(consumoEmDinheiro.bigDecimalValue());
                }

                BigDecimal consumoAreaComum = conta.getConsumoAreaComum().multiply(valorMetroCubico.bigDecimalValue());
                if (consumoAreaComum.doubleValue() < 0) {
                    conta.setPrecoAreaComum(new BigDecimal(BigInteger.ZERO));

                } else {
                    conta.setPrecoAreaComum(consumoAreaComum);
                }

                BigDecimal valor = new BigDecimal(((rateio.getConsumoMetroCubicoAreaComum().doubleValue() * 100) / conta.getConsumoAreaComum().doubleValue()));

                rateio.setPercentualRateioAreaComum(valor);
            }
        } else if (parametro == FormaRateioAreaComum.PROPORCIONAL_CONSUMO) {

            if (conta.getConsumoAreaComum().intValue() > 0) {
                BigDecimal valorResiduo = conta.getValorProlagos().subtract(conta.getPrecoTotalUnidades());
                Moeda valorMetroCubico = new Moeda(valorResiduo.divide(conta.getConsumoAreaComum(), RoundingMode.UP));

                double calculo = (rateio.getConsumoMetroCubico().doubleValue() * 100) / conta.getConsumoUnidadesMetroCubico().doubleValue();

                double total = (conta.getConsumoAreaComum().doubleValue() * calculo) / 100;

                if (total < 0) {
                    rateio.setConsumoMetroCubicoAreaComum(BigDecimal.ZERO);
                } else {
                    rateio.setConsumoMetroCubicoAreaComum(new BigDecimal(total));
                }

                Moeda consumoEmDinheiro = new Moeda(total).multiplica(valorMetroCubico);

                if (consumoEmDinheiro.doubleValue() < 0) {
                    rateio.setConsumoEmDinheiroAreaComum(new BigDecimal(0));
                } else {
                    rateio.setConsumoEmDinheiroAreaComum(consumoEmDinheiro.bigDecimalValue());
                }

                BigDecimal consumoAreaComum = conta.getConsumoAreaComum().multiply(valorMetroCubico.bigDecimalValue());
                if (consumoAreaComum.doubleValue() < 0) {
                    conta.setPrecoAreaComum(new BigDecimal(BigInteger.ZERO));

                } else {
                    conta.setPrecoAreaComum(consumoAreaComum);
                }

                BigDecimal valor = new BigDecimal(((rateio.getConsumoMetroCubicoAreaComum().doubleValue() * 100) / conta.getConsumoAreaComum().doubleValue()));

                rateio.setPercentualRateioAreaComum(valor);
            }


        } else if (parametro == FormaRateioAreaComum.PROPORCIONAL_FRACAO) {

            if (conta.getConsumoAreaComum().intValue() > 0) {
                BigDecimal valorResiduo = conta.getValorProlagos().subtract(conta.getPrecoTotalUnidades());
                Moeda valorMetroCubico = new Moeda(valorResiduo.divide(conta.getConsumoAreaComum(), RoundingMode.UP));

                double resultado = (conta.getConsumoAreaComum().doubleValue() * getMaiorFracaoIdeal()) / 100;

                double total = (rateio.getUnidade().getFracaoIdeal().doubleValue() * resultado) / getMaiorFracaoIdeal();

                if (total < 0) {
                    rateio.setConsumoMetroCubicoAreaComum(BigDecimal.ZERO);
                } else {
                    rateio.setConsumoMetroCubicoAreaComum(new BigDecimal(total));
                }

                Moeda consumoEmDinheiro = new Moeda(total).multiplica(valorMetroCubico);

                if (consumoEmDinheiro.doubleValue() < 0) {
                    rateio.setConsumoEmDinheiroAreaComum(new BigDecimal(0));
                } else {
                    rateio.setConsumoEmDinheiroAreaComum(consumoEmDinheiro.bigDecimalValue());
                }

                BigDecimal consumoAreaComum = conta.getConsumoAreaComum().multiply(valorMetroCubico.bigDecimalValue());
                if (consumoAreaComum.doubleValue() < 0) {
                    conta.setPrecoAreaComum(new BigDecimal(BigInteger.ZERO));

                } else {
                    conta.setPrecoAreaComum(consumoAreaComum);
                }

                BigDecimal valor = new BigDecimal(((rateio.getConsumoMetroCubicoAreaComum().doubleValue() * 100) / conta.getConsumoAreaComum().doubleValue()));

                rateio.setPercentualRateioAreaComum(valor);
            }


        } else if (parametro == FormaRateioAreaComum.VALOR_FIXO) {
            if (condominio.getParametros().getValorFixoAreaComum().intValue() > 0) {


                if (conta.getConsumoAreaComum().intValue() > 0) {
                    BigDecimal valorResiduo = conta.getValorProlagos().subtract(conta.getPrecoTotalUnidades());
                    Moeda valorMetroCubico = new Moeda(valorResiduo.divide(conta.getConsumoAreaComum(), RoundingMode.UP));
                    double total = conta.getConsumoAreaComum().doubleValue() / condominio.getUnidades().size();
                    if (total < 0) {
                        rateio.setConsumoMetroCubicoAreaComum(BigDecimal.ZERO);
                    } else {
                        rateio.setConsumoMetroCubicoAreaComum(new BigDecimal(total));
                    }

                    Moeda consumoEmDinheiro = new Moeda(condominio.getParametros().getValorFixoAreaComum());
                    if (consumoEmDinheiro.doubleValue() < 0) {
                        rateio.setConsumoEmDinheiroAreaComum(new BigDecimal(0));
                    } else {
                        rateio.setConsumoEmDinheiroAreaComum(consumoEmDinheiro.bigDecimalValue());
                    }

                    BigDecimal consumoAreaComum = conta.getConsumoAreaComum().multiply(valorMetroCubico.bigDecimalValue());
                    if (consumoAreaComum.doubleValue() < 0) {
                        conta.setPrecoAreaComum(new BigDecimal(BigInteger.ZERO));

                    } else {
                        conta.setPrecoAreaComum(consumoAreaComum);
                    }

                    BigDecimal valor = new BigDecimal(((rateio.getConsumoMetroCubicoAreaComum().doubleValue() * 100) / conta.getConsumoAreaComum().doubleValue()));

                    rateio.setPercentualRateioAreaComum(valor);
                }

            } else {
                ApresentacaoUtil.exibirAdvertencia("O valor fixo na aba de Parâmetros deve ser maior que 0 (Zero)!", this);
            }
        } else if (parametro == FormaRateioAreaComum.NAO_COBRAR) {
            rateio.setConsumoMetroCubicoAreaComum(new BigDecimal(0));
            rateio.setConsumoEmDinheiroAreaComum(new BigDecimal(0));
            conta.setPrecoAreaComum(new BigDecimal(0));
            rateio.setPercentualRateioAreaComum(new BigDecimal(0));
        }
    }

    private void verificarHidrometroAreaComum() {
        if (checkHidrometroAreaComum.isSelected()) {
            painelHidrometro.setVisible(true);
            System.out.println("here");
        } else {
            painelHidrometro.setVisible(false);
        }

    }

    private double getMaiorFracaoIdeal() {
        double resultado = 0;
        for (Unidade u : condominio.getUnidades()) {
            if (u.getFracaoIdeal().doubleValue() > resultado) {
                resultado = u.getFracaoIdeal().doubleValue();
            }
        }
        return resultado;
    }

    private void calcularPercentual(Rateio rateio) {
        if (modeloContaAgua.getObjetoSelecionado().getConsumoProlagos().intValue() > 0) {
            BigDecimal valor = new BigDecimal(((rateio.getConsumoMetroCubico().doubleValue() * 100) / conta.getConsumoProlagos().doubleValue()));
            rateio.setPercentualGasto(valor);
        }
    }

    private void calcularTotalConsumoUnidades() {
        List<Rateio> rateios = modeloContaAgua.getObjetoSelecionado().getRateios();
        Moeda total = new Moeda(BigDecimal.ZERO);
        Moeda totalDinheiro = new Moeda(0);
        for (Rateio rateio : rateios) {
            total.soma(rateio.getConsumoMetroCubicoACobrar());
            totalDinheiro.soma(rateio.getValorDoMetroCubico().multiply(rateio.getConsumoMetroCubicoACobrar()));
        }

        conta.setConsumoUnidadesMetroCubico(total.bigDecimalValue());
        conta.setPrecoTotalUnidades(totalDinheiro.bigDecimalValue());
    }

    private void calcularTotalAreaComum() {
        BigDecimal soma = BigDecimal.ZERO;
        if (checkNaoCobrarPipa.isSelected()) {
            soma = conta.getConsumoProlagos();
        } else {
            soma = conta.getConsumoProlagos().add(conta.getConsumoPipa());
        }
        BigDecimal total = BigDecimal.ZERO;
        if (checkHidrometroAreaComum.isSelected()) {
            if (txtTotalAreaComum.getText().length() > 0) {
                Moeda valorSomado = new Moeda(txtTotalAreaComum.getText().replace(",", "."));
                System.out.println("soma dentro do check hidrometro " + total.toString());
                System.out.println("conta.getCosumoPipa" + conta.getConsumoPipa());
                valorSomado.soma(conta.getConsumoPipa());
                total = valorSomado.bigDecimalValue();
                System.out.println("soma dentro do check hidrometro depois d add pipa " + total.toString());
            } else {
                ApresentacaoUtil.exibirInformacao("Digite um valor pra Area Comum e salve!", this);
            }

        } else {
            total = soma.subtract(conta.getConsumoUnidadesMetroCubico());
        }
        if (total.intValue() > 0) {
            conta.setConsumoAreaComum(FormatadorNumeros.casasDecimais(3, total));
        } else {
            conta.setConsumoAreaComum(new BigDecimal(BigInteger.ZERO));
        }
    }

    private void calcularTotalConsumoUnidade(Rateio rateio) {
        Moeda total = new Moeda(rateio.getConsumoMetroCubicoACobrar());
        total.multiplica(rateio.getValorDoMetroCubico());
        rateio.setValorTotalConsumido(total.bigDecimalValue());
    }

    private void calcular() {
        List<Rateio> rateios = modeloContaAgua.getObjetoSelecionado().getRateios();
        if (!rateios.isEmpty()) {

            for (Rateio rateio : rateios) {
                if (rateio.getLeituraAtual() != null && rateio.getLeituraAnterior() != null) {
                    if (rateio.getLeituraAnterior().doubleValue() > rateio.getLeituraAtual().doubleValue()) {
                        ApresentacaoUtil.exibirAdvertencia("O valor da leitura atual não pode ser Menor que a anterior!", this);
                        return;
                    } else {
                        BigDecimal valorConsumo = rateio.getLeituraAtual().subtract(rateio.getLeituraAnterior());
                        if (valorConsumo.intValue() < 0) {
                            rateio.setConsumoMetroCubico(BigDecimal.ZERO);
                        } else {
                            rateio.setConsumoMetroCubico(valorConsumo);
                        }
                    }
                } else {
                    System.out.println("valor nulo");
                }


                if (condominio.getParametros().getQuantidadeMetrosCubicosNaCota() != null) {
                    BigDecimal valorAtualizado = rateio.getConsumoMetroCubico().subtract(condominio.getParametros().getQuantidadeMetrosCubicosNaCota());

                    if (valorAtualizado.intValue() < 0) {
                        rateio.setConsumoMetroCubicoACobrar(new BigDecimal(BigInteger.ZERO));
                    } else {
                        rateio.setConsumoMetroCubicoACobrar(valorAtualizado);
                    }
                }

                totalPipa();

                verificarParametrosMetroCubico(rateio);
                calcularPercentual(rateio);
                calcularTotalConsumoUnidade(rateio);
            }

            calcularTotalConsumoUnidades();
            calcularTotalAreaComum();

            for (Rateio rateio : rateios) {
                verificarParametrosAreaComum(rateio);
                rateio.setValorTotalCobrar(new BigDecimal(totalValorRateio(rateio)).setScale(2, RoundingMode.UP));
            }


//            modeloContaAgua.carregarObjetos();
            modeloRateio.carregarObjetos();
            totalValorConta();

        }
    }

    private double totalValorRateio(Rateio rateio) {
        double total = 0;

        total = rateio.getValorRateioPipa().doubleValue() + rateio.getValorTotalConsumido().doubleValue() + rateio.getConsumoEmDinheiroAreaComum().doubleValue();

        return total;
    }

    private void totalValorConta() {
        Moeda somatorio = new Moeda();
        ContaAgua c = modeloContaAgua.getObjetoSelecionado();
        c.setTotalDespesas(BigDecimal.ZERO);

        somatorio.soma(c.getPrecoAreaComum());
        somatorio.soma(c.getTotalDespesasPipa());
        somatorio.soma(c.getPrecoTotalUnidades());

        c.setTotalDespesas(somatorio.bigDecimalValue());


    }

    private void totalPipa() {
        Moeda totalDinheiro = new Moeda();
        int totalMetroCubico = 0;

        for (Pipa pipa : conta.getPipas()) {
            totalDinheiro.soma(pipa.getTotalPago());
            totalMetroCubico = totalMetroCubico + pipa.getQuantidadeLitrosPorMil();
        }

        conta.setConsumoPipa(new BigDecimal(totalMetroCubico));
        if (checkNaoCobrarPipa.isSelected()) {
            conta.setValorPipa(BigDecimal.ZERO);
        } else {
            conta.setValorPipa(totalDinheiro.bigDecimalValue());
        }
    }

    private void incluirContaAgua() {

        limparCampos();

        System.out.println("tamanho " + modeloContaAgua.size());

        conta = new ContaAgua();

        List<Unidade> unidades = new DAO().listar("UnidadePorCondominio", condominio.getCodigo());
        List<Rateio> rateios = new ArrayList<Rateio>();

        for (Unidade unidade : unidades) {

            Rateio rateio = new Rateio(unidade);
            if (modeloContaAgua.size() == 0) {
                rateio.setLeituraAnterior(BigDecimal.ZERO);
            } else {
                ContaAgua c = modeloContaAgua.getObjeto(modeloContaAgua.size() - 1);
                for (Rateio r : c.getRateios()) {
                    if (r.getUnidade().equals(rateio.getUnidade())) {
                        rateio.setLeituraAnterior(r.getLeituraAtual());
                    }

                }
            }
            rateio.setConta(conta);
            rateios.add(rateio);

        }
        conta.setRateios(rateios);
        conta.setCondominio(condominio);
        modeloContaAgua.adicionar(conta);
        modeloContaAgua.selecionar(conta, 0);
        carregarTabelaRateio();
        carregarTabelaPipa();
//        modeloContaAgua.setSelecaoMultipla(false);



    }

    private void incluirPipa() {

        if (modeloContaAgua.size() == 0 || modeloContaAgua.getObjetoSelecionado() == null) {
            ApresentacaoUtil.exibirAdvertencia("Você deve incluir uma conta de água antes ou selecionar uma já existente!", this);
        } else {

            abaPipa.setSelectedIndex(1);

            Pipa pipa = new Pipa();

            pipa.setConta(modeloContaAgua.getObjetoSelecionado());
            modeloPipa.adicionar(pipa);
            modeloPipa.selecionar(pipa, 0);

        }


    }

    private void removerContaAgua() {
        if (modeloContaAgua.getObjetoSelecionado() != null) {
            if (ApresentacaoUtil.perguntar("Tem certeza que deseja excluir? ", this) == true) {


                modeloContaAgua.remover(modeloContaAgua.getObjetoSelecionado());

                new DAO().remover(conta);

                conta = null;

                modeloRateio.setObjetos(null);

                carregarTabelaContaAgua();

                ApresentacaoUtil.exibirInformacao("Removido com Sucesso!", this);
            }

        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void removerPipa() {
        if (modeloContaAgua.getObjetoSelecionado() != null) {
            if (modeloPipa.getObjetoSelecionado() != null) {

                Pipa pipa = modeloPipa.getObjetoSelecionado();

                modeloContaAgua.getObjetoSelecionado().getPipas().remove(pipa);
                modeloPipa.setObjetos(modeloContaAgua.getObjetoSelecionado().getPipas());

                new DAO().remover(pipa);
                new DAO().salvar(modeloContaAgua.getObjetoSelecionado());


                ApresentacaoUtil.exibirInformacao("Pipa removida com sucesso!", this);

            }

        }

    }

    private List<TarifaProlagos> getTarifaProlagos() {
        List<TarifaProlagos> tarifas = new DAO().listar("TarifaPorId");
        return tarifas;
    }

    private List<ContaAgua> getContasAgua() {
        List<ContaAgua> contas = new DAO().listar("ContasPorCondominio", condominio);
        return contas;
    }

    private void adicionar() {
        DialogoTarifaProlagos.getTarifa(new TarifaProlagos(), TelaPrincipal.getInstancia(), true);
        carregarTabela();
    }

    private void editar() {
        if (modelo.getLinhaSelecionada() != -1) {
            DialogoTarifaProlagos.getTarifa(modelo.getObjetoSelecionado(), TelaPrincipal.getInstancia(), true);
            carregarTabela();
        } else {
            ApresentacaoUtil.exibirInformacao("Você deve selecionar uma tarifa para editar!", this);

        }
    }

    private void remover() {
        if (modelo.getLinhaSelecionada() > -1) {
            List<TarifaProlagos> tarifas = modelo.getObjetosSelecionados();

            new DAO().remover(tarifas);
            ApresentacaoUtil.exibirInformacao("Tarifa(s) removidas com sucesso!", this);
            carregarTabela();

        } else {
            ApresentacaoUtil.exibirInformacao("Você deve selecionar uma tarifa para remover!", this);
        }

    }

    private FormaRateioAreaComum selecionarRateioAreaComum() {
        if (cbFormaRateioAreaComum.getSelectedItem() == null) {
            return FormaRateioAreaComum.SEM_VALOR;
        }
        if (cbFormaRateioAreaComum.getSelectedItem().equals(FormaRateioAreaComum.SEM_VALOR.toString())) {
            return FormaRateioAreaComum.SEM_VALOR;
        } else if (cbFormaRateioAreaComum.getSelectedItem().equals(FormaRateioAreaComum.IGUAL_TODOS.toString())) {
            return FormaRateioAreaComum.IGUAL_TODOS;
        } else if (cbFormaRateioAreaComum.getSelectedItem().equals(FormaRateioAreaComum.NAO_COBRAR.toString())) {
            return FormaRateioAreaComum.NAO_COBRAR;
        } else if (cbFormaRateioAreaComum.getSelectedItem().equals(FormaRateioAreaComum.PROPORCIONAL_CONSUMO.toString())) {
            return FormaRateioAreaComum.PROPORCIONAL_CONSUMO;
        } else if (cbFormaRateioAreaComum.getSelectedItem().equals(FormaRateioAreaComum.PROPORCIONAL_FRACAO.toString())) {
            return FormaRateioAreaComum.PROPORCIONAL_FRACAO;
        } else if (cbFormaRateioAreaComum.getSelectedItem().equals(FormaRateioAreaComum.VALOR_FIXO.toString())) {
            return FormaRateioAreaComum.VALOR_FIXO;
        }

        return FormaRateioAreaComum.SEM_VALOR;
    }

    private FormaCalculoMetroCubico selecionarCalculoMetroCubico() {
        if (cbFormaCalculoMetroCubico.getSelectedItem() == null) {
            return FormaCalculoMetroCubico.SEM_VALOR;
        }
        if (cbFormaCalculoMetroCubico.getSelectedItem().equals(FormaCalculoMetroCubico.SEM_VALOR.toString())) {
            return FormaCalculoMetroCubico.SEM_VALOR;
        } else if (cbFormaCalculoMetroCubico.getSelectedItem().equals(FormaCalculoMetroCubico.DIVIDIR_METROS_CUBICOS.toString())) {
            return FormaCalculoMetroCubico.DIVIDIR_METROS_CUBICOS;
        } else if (cbFormaCalculoMetroCubico.getSelectedItem().equals(FormaCalculoMetroCubico.SINDICO_PRECO.toString())) {
            return FormaCalculoMetroCubico.SINDICO_PRECO;
        } else if (cbFormaCalculoMetroCubico.getSelectedItem().equals(FormaCalculoMetroCubico.TABELA_PROLAGOS.toString())) {
            return FormaCalculoMetroCubico.TABELA_PROLAGOS;
        }

        return FormaCalculoMetroCubico.SEM_VALOR;
    }

    private void preencherObjeto() {

        parametros.setFormaAreaComum(selecionarRateioAreaComum());
        parametros.setFormaMetroCubico(selecionarCalculoMetroCubico());
        parametros.setQuantidadeMetrosCubicosNaCota(new BigDecimal((Integer) spinnerQuantidadeIncluirCota.getValue()));
        parametros.setCobrarPipa(checkNaoCobrarPipa.isSelected());
        parametros.setHidrometroAreaComum(checkHidrometroAreaComum.isSelected());
        parametros.setValorFixoAreaComum(new Moeda(txtValorFixoAreaComum.getText()).bigDecimalValue());
        parametros.setValorMetroCubicoSindico(new Moeda(txtValorSindico.getText()).bigDecimalValue());
    }

    private void preencherTela() {

        if (parametros.getFormaAreaComum() != null) {
            cbFormaRateioAreaComum.setSelectedItem(parametros.getFormaAreaComum().toString());
        }
        if (parametros.getFormaMetroCubico() != null) {
            cbFormaCalculoMetroCubico.setSelectedItem(parametros.getFormaMetroCubico().toString());
        }
        if (parametros.getQuantidadeMetrosCubicosNaCota() != null) {
            spinnerQuantidadeIncluirCota.setValue(parametros.getQuantidadeMetrosCubicosNaCota().intValue());
        }
        checkHidrometroAreaComum.setSelected(parametros.isHidrometroAreaComum());
        checkNaoCobrarPipa.setSelected(parametros.isCobrarPipa());
        txtValorFixoAreaComum.setText(new Moeda(parametros.getValorFixoAreaComum()).toString());
        txtValorSindico.setText(new Moeda(parametros.getValorMetroCubicoSindico()).toString());

        verificarHidrometroAreaComum();

    }

    private void fechar() {
        doDefaultCloseAction();
    }

    private void preencherTelaHidrometro() {
        if (conta.getHidrometro() != null) {
            txtLeituraAnteriorAreaComum.setText(conta.getHidrometro().getLeituraAtual().toString());
            txtLeituraAtualAreaComum.setText(conta.getHidrometro().getLeituraFinal().toString());

            BigDecimal total = conta.getHidrometro().getLeituraFinal().subtract(conta.getHidrometro().getLeituraAtual());
            txtTotalAreaComum.setText(total.toString());
        }

    }

    private void preencherHidrometro() {

        if (checkHidrometroAreaComum.isSelected()) {
            HidrometroAreaComum h = new HidrometroAreaComum();

            h.setLeituraAtual(new BigDecimal(txtLeituraAnteriorAreaComum.getText().replace(",", ".")));
            if (txtLeituraAtualAreaComum.getText().length() > 0) {
                h.setLeituraFinal(new BigDecimal(txtLeituraAtualAreaComum.getText().replace(",", ".")));
            }

            conta.setHidrometro(h);

            new DAO().salvar(h);

            BigDecimal total = h.getLeituraFinal().subtract(h.getLeituraAtual());
            txtLeituraAnteriorAreaComum.setText(h.getLeituraAtual().toString());

            txtTotalAreaComum.setText(FormatadorNumeros.casasDecimais(3, total).toString());



        }

    }

    private void salvar() {

        preencherObjeto();
        try {
            new DAO().salvar(condominio.getParametros());

            ApresentacaoUtil.exibirInformacao("Parâmetros salvos com Sucesso!", this);
            preencherTela();

        } catch (Exception e) {
            e.printStackTrace();
            ApresentacaoUtil.exibirInformacao("Ocorreu um erro ao tentar salvar os parâmetros", this);
        }

    }

    private void limparCampos() {
        txtLeituraAnteriorAreaComum.setText("");
        txtLeituraAtualAreaComum.setText("");
        txtTotalAreaComum.setText("");

        System.out.println("condominio size " + condominio.getContasDeAgua().size());
        if (condominio.getContasDeAgua().size() >= 1) {
            txtLeituraAnteriorAreaComum.setText(condominio.getContasDeAgua().get((condominio.getContasDeAgua().size() - 1)).getHidrometro().getLeituraFinal().toString());

        } else {

            txtLeituraAnteriorAreaComum.setText("0");
        }

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        if (checkHidrometroAreaComum.isSelected()) {
            campos.add(txtLeituraAtualAreaComum);
        }

        return campos;
    }

    private boolean validarCampos() {
        if (conta.getDataInicial() != null && conta.getDataFinal() != null) {
            if (DataUtil.compararData(DataUtil.getDateTime(conta.getDataInicial()), DataUtil.getDateTime(conta.getDataFinal())) == 1) {
                ApresentacaoUtil.exibirErro("Data Inicial nâo pode ser maior que a data Final", this);
                return false;
            }
        } else {
            ApresentacaoUtil.exibirErro("Por favor insira uma data Inicial e Final!", this);
            return false;
        }

        if (conta.getValorProlagos().doubleValue() > 0 && conta.getConsumoProlagos().doubleValue() > 0) {
            if (conta.getDataVencimentoConta() == null) {
                ApresentacaoUtil.exibirAdvertencia("Por favor entre com o vencimento da conta!", this);
                return false;
            } else {
                return true;
            }


        }

        return true;
    }

    private void salvarAgua() {
        preencherObjeto();
        preencherHidrometro();
        try {
            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }

            if (!validarCampos()) {
                return;
            }
            new DAO().salvar(conta.getRateios());
            new DAO().salvar(conta);

            ApresentacaoUtil.exibirInformacao("Conta Salva com Sucesso!", this);
            preencherTela();

        } catch (Exception e) {
            e.printStackTrace();
            ApresentacaoUtil.exibirInformacao("Ocorreu um erro ao tentar salvar a conta", this);
        }

    }
    
    private void imprimir(){
        if (modeloContaAgua.getObjetoSelecionado() == null){
            ApresentacaoUtil.exibirAdvertencia("Selecione um registro na tabela das contas.", this);
        } else {
            boolean possuiHidrometro = false;
            if (checkHidrometroAreaComum.isSelected()){
                possuiHidrometro = true;
            }
            boolean consideraPipa = true;
            if (checkNaoCobrarPipa.isSelected()){
                possuiHidrometro = false;
            }
            String formaRateio = cbFormaCalculoMetroCubico.getSelectedItem().toString();
            String formaRateioAreaComum = cbFormaRateioAreaComum.getSelectedItem().toString();
            new Relatorios().imprimirRelatorioConsumoAgua(modeloContaAgua.getObjetoSelecionado(), formaRateio, (Integer)spinnerQuantidadeIncluirCota.getValue(), possuiHidrometro, formaRateioAreaComum, consideraPipa);
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();

            if (origem == itemMenuAdicionar || origem == btnAdicionar) {
                adicionar();
            } else if (origem == itemMenuEditar || origem == btnEditar) {
                editar();
            } else if (origem == itemMenuRemover || origem == btnRemover) {
                remover();
            } else if (origem == btnVoltar) {
                fechar();
            } else if (origem == btnSalvar) {
                salvar();
            } else if (origem == btnIncluir || origem == itemMenuIncluirRegistroContaAgua) {
                incluirContaAgua();
            } else if (origem == itemMenuDeletarRegistroContaAgua) {
                removerContaAgua();
            } else if (origem == itemMenuDeletarPipa) {
                removerPipa();
            } else if (origem == btnCalcular) {
                calcular();
            } else if (origem == btnIncluirPipa || origem == itemMenuIncluirPipa) {
                incluirPipa();
            } else if (origem == btnSalvarAgua || origem == itemMenuGravarAlteracoesContaAgua || origem == itemMenuGravarAlteracoesPipa) {
                salvarAgua();
            } else if (origem == btnImprimir){
                imprimir();
            }
        }

        @Override
        public void configurar() {


            itemMenuAdicionar.addActionListener(this);
            itemMenuEditar.addActionListener(this);
            itemMenuRemover.addActionListener(this);
            itemMenuIncluirRegistroContaAgua.addActionListener(this);
            itemMenuDeletarRegistroContaAgua.addActionListener(this);
            itemMenuGravarAlteracoesContaAgua.addActionListener(this);
            itemMenuGravarAlteracoesPipa.addActionListener(this);
            itemMenuIncluirPipa.addActionListener(this);
            itemMenuDeletarPipa.addActionListener(this);
            tabela.addMouseListener(this);
            btnVoltar.addActionListener(this);
            btnSalvar.addActionListener(this);
            btnAdicionar.addActionListener(this);
            btnEditar.addActionListener(this);
            btnRemover.addActionListener(this);
            btnIncluir.addActionListener(this);
            tabelaPipa.addMouseListener(this);
            tabelaContaAgua.addMouseListener(this);
            tabelaContaAgua.addKeyListener(this);
            tabelaRateio.addMouseListener(this);
            btnCalcular.addActionListener(this);
            btnIncluirPipa.addActionListener(this);
            btnSalvarAgua.addActionListener(this);
            btnImprimir.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabela) {
                popupTarifaProlagos.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.isPopupTrigger() && e.getSource() == tabelaContaAgua) {
                popupContaAgua.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.isPopupTrigger() && e.getSource() == tabelaPipa) {
                popupPipa.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.getSource() == tabelaContaAgua) {
                conta = modeloContaAgua.getObjetoSelecionado();
                carregarTabelaPipa();
                carregarTabelaRateio();
                preencherTelaHidrometro();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Object origem = e.getSource();
            if (origem == tabelaContaAgua && (e.getKeyCode() == KeyEvent.VK_UP) || origem == tabelaContaAgua && (e.getKeyCode() == KeyEvent.VK_DOWN)) {
                modeloRateio.setObjetos(modeloContaAgua.getObjetoSelecionado().getRateios());
                preencherTelaHidrometro();

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

        popupTarifaProlagos = new javax.swing.JPopupMenu();
        itemMenuAdicionar = new javax.swing.JMenuItem();
        itemMenuEditar = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        itemMenuRemover = new javax.swing.JMenuItem();
        popupContaAgua = new javax.swing.JPopupMenu();
        itemMenuIncluirRegistroContaAgua = new javax.swing.JMenuItem();
        itemMenuDeletarRegistroContaAgua = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itemMenuGravarAlteracoesContaAgua = new javax.swing.JMenuItem();
        popupPipa = new javax.swing.JPopupMenu();
        itemMenuIncluirPipa = new javax.swing.JMenuItem();
        itemMenuDeletarPipa = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        itemMenuGravarAlteracoesPipa = new javax.swing.JMenuItem();
        abaCalculoMensal = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        btnIncluir = new javax.swing.JButton();
        btnIncluirPipa = new javax.swing.JButton();
        btnSalvarAgua = new javax.swing.JButton();
        btnCalcular = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaContaAgua = new javax.swing.JTable();
        abaPipa = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaRateio = new javax.swing.JTable();
        painelHidrometro = new javax.swing.JPanel();
        txtLeituraAnteriorAreaComum = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtLeituraAtualAreaComum = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtTotalAreaComum = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaPipa = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnAdicionar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnRemover = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cbFormaCalculoMetroCubico = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        spinnerQuantidadeIncluirCota = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        txtValorSindico = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtValorFixoAreaComum = new javax.swing.JTextField();
        checkHidrometroAreaComum = new javax.swing.JCheckBox();
        checkNaoCobrarPipa = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        cbFormaRateioAreaComum = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();

        itemMenuAdicionar.setText("Adicionar Tarifa");
        popupTarifaProlagos.add(itemMenuAdicionar);

        itemMenuEditar.setText("Editar Tarifa");
        popupTarifaProlagos.add(itemMenuEditar);
        popupTarifaProlagos.add(jSeparator4);

        itemMenuRemover.setText("Remover Tarifa");
        popupTarifaProlagos.add(itemMenuRemover);

        itemMenuIncluirRegistroContaAgua.setText("Incluir Registro");
        popupContaAgua.add(itemMenuIncluirRegistroContaAgua);

        itemMenuDeletarRegistroContaAgua.setText("Deletar Registro");
        popupContaAgua.add(itemMenuDeletarRegistroContaAgua);
        popupContaAgua.add(jSeparator1);

        itemMenuGravarAlteracoesContaAgua.setText("Gravar Alterações");
        popupContaAgua.add(itemMenuGravarAlteracoesContaAgua);

        itemMenuIncluirPipa.setText("Incluir Registro");
        popupPipa.add(itemMenuIncluirPipa);

        itemMenuDeletarPipa.setText("Deletar Registro");
        popupPipa.add(itemMenuDeletarPipa);
        popupPipa.add(jSeparator3);

        itemMenuGravarAlteracoesPipa.setText("Gravar Alterações");
        popupPipa.add(itemMenuGravarAlteracoesPipa);

        setClosable(true);
        setTitle("Cálculo de Água");

        btnIncluir.setText("Incluir");

        btnIncluirPipa.setText("Incluir PIPA");

        btnSalvarAgua.setText("Salvar");

        btnCalcular.setText("Calcular");

        btnImprimir.setText("Imprimir");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(86, 86, 86)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnIncluirPipa)
                .addGap(18, 18, 18)
                .addComponent(btnSalvarAgua)
                .addGap(18, 18, 18)
                .addComponent(btnCalcular)
                .addGap(27, 27, 27)
                .addComponent(btnImprimir)
                .addContainerGap(86, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir, btnIncluirPipa, btnSalvarAgua});

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIncluirPipa)
                    .addComponent(btnSalvarAgua)
                    .addComponent(btnCalcular)
                    .addComponent(btnImprimir))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir, btnIncluirPipa, btnSalvarAgua});

        tabelaContaAgua.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tabelaContaAgua);

        tabelaRateio.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tabelaRateio);

        painelHidrometro.setBorder(javax.swing.BorderFactory.createTitledBorder("Hidrômetro Área Comum"));

        txtLeituraAnteriorAreaComum.setBackground(new java.awt.Color(204, 204, 204));
        txtLeituraAnteriorAreaComum.setEditable(false);

        jLabel7.setText("Leitura Anterior");

        jLabel8.setText("Leitura Final:");

        jLabel9.setText("Total:");

        txtTotalAreaComum.setBackground(new java.awt.Color(204, 204, 204));
        txtTotalAreaComum.setEditable(false);

        jLabel10.setText("Os valores calculados aqui automaticamente serão adicionados na área comum da conta ao clicar em calcular!");

        javax.swing.GroupLayout painelHidrometroLayout = new javax.swing.GroupLayout(painelHidrometro);
        painelHidrometro.setLayout(painelHidrometroLayout);
        painelHidrometroLayout.setHorizontalGroup(
            painelHidrometroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelHidrometroLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLeituraAnteriorAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLeituraAtualAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(82, 82, 82)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTotalAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelHidrometroLayout.createSequentialGroup()
                .addContainerGap(162, Short.MAX_VALUE)
                .addComponent(jLabel10))
        );

        painelHidrometroLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtLeituraAnteriorAreaComum, txtLeituraAtualAreaComum, txtTotalAreaComum});

        painelHidrometroLayout.setVerticalGroup(
            painelHidrometroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelHidrometroLayout.createSequentialGroup()
                .addGroup(painelHidrometroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtLeituraAnteriorAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtLeituraAtualAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtTotalAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jLabel10))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 698, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelHidrometro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelHidrometro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        abaPipa.addTab("Rateio", jPanel8);

        tabelaPipa.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(tabelaPipa);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addContainerGap())
        );

        abaPipa.addTab("Fornecimento de PIPA", jPanel9);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(abaPipa, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(abaPipa, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        abaCalculoMensal.addTab("Cálculos Mensais de Àgua", jPanel1);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        jLabel1.setText("Tabela Tarifário Prolagos");

        btnAdicionar.setText("Adicionar");

        btnEditar.setText("Editar");

        btnRemover.setText("Remover");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(btnAdicionar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addComponent(btnEditar)
                .addGap(66, 66, 66)
                .addComponent(btnRemover)
                .addGap(36, 36, 36))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAdicionar, btnEditar, btnRemover});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRemover)
                    .addComponent(btnEditar)
                    .addComponent(btnAdicionar))
                .addGap(22, 22, 22))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Metro Cúbico"));

        jLabel2.setText("Forma de Cálculo do Preço:");

        jLabel3.setText("Qtd para Incluir na Taxa Condominial:");

        jLabel4.setText("Valor Informado Pelo Síndico:");

        jLabel5.setText("Valor Fixo Cobrado Área Comum:");

        checkHidrometroAreaComum.setText("Condomínio com Hidrômetro na Área Comum");

        checkNaoCobrarPipa.setText("Não Cobrar Despesas com Pipa");

        jLabel6.setText("Forma de Rateio da Área Comum:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkHidrometroAreaComum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(checkNaoCobrarPipa)
                    .addComponent(jLabel2)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerQuantidadeIncluirCota, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorSindico, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
                    .addComponent(cbFormaCalculoMetroCubico, 0, 237, Short.MAX_VALUE)
                    .addComponent(jLabel6)
                    .addComponent(cbFormaRateioAreaComum, 0, 237, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorFixoAreaComum, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFormaCalculoMetroCubico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinnerQuantidadeIncluirCota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtValorSindico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(txtValorFixoAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(checkHidrometroAreaComum)
                .addGap(29, 29, 29)
                .addComponent(checkNaoCobrarPipa)
                .addGap(34, 34, 34)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFormaRateioAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(21, 21, 21))
        );

        btnSalvar.setText("Salvar");

        btnVoltar.setText("Fechar");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(223, 223, 223)
                .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(132, 132, 132)
                .addComponent(btnVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(214, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnSalvar, btnVoltar});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnVoltar, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSalvar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnSalvar, btnVoltar});

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        abaCalculoMensal.addTab("Parâmetros para Cálculo de Consumo de Água", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(abaCalculoMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 748, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(abaCalculoMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane abaCalculoMensal;
    private javax.swing.JTabbedPane abaPipa;
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnIncluirPipa;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnSalvarAgua;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JComboBox cbFormaCalculoMetroCubico;
    private javax.swing.JComboBox cbFormaRateioAreaComum;
    private javax.swing.JCheckBox checkHidrometroAreaComum;
    private javax.swing.JCheckBox checkNaoCobrarPipa;
    private javax.swing.JMenuItem itemMenuAdicionar;
    private javax.swing.JMenuItem itemMenuDeletarPipa;
    private javax.swing.JMenuItem itemMenuDeletarRegistroContaAgua;
    private javax.swing.JMenuItem itemMenuEditar;
    private javax.swing.JMenuItem itemMenuGravarAlteracoesContaAgua;
    private javax.swing.JMenuItem itemMenuGravarAlteracoesPipa;
    private javax.swing.JMenuItem itemMenuIncluirPipa;
    private javax.swing.JMenuItem itemMenuIncluirRegistroContaAgua;
    private javax.swing.JMenuItem itemMenuRemover;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPanel painelHidrometro;
    private javax.swing.JPopupMenu popupContaAgua;
    private javax.swing.JPopupMenu popupPipa;
    private javax.swing.JPopupMenu popupTarifaProlagos;
    private javax.swing.JSpinner spinnerQuantidadeIncluirCota;
    private javax.swing.JTable tabela;
    private javax.swing.JTable tabelaContaAgua;
    private javax.swing.JTable tabelaPipa;
    private javax.swing.JTable tabelaRateio;
    private javax.swing.JTextField txtLeituraAnteriorAreaComum;
    private javax.swing.JTextField txtLeituraAtualAreaComum;
    private javax.swing.JTextField txtTotalAreaComum;
    private javax.swing.JTextField txtValorFixoAreaComum;
    private javax.swing.JTextField txtValorSindico;
    // End of variables declaration//GEN-END:variables
}
