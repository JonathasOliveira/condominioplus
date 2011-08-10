/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaAgua.java
 *
 * Created on 18/05/2011, 16:04:14
 */
package condominioPlus.apresentacao.cobranca.gas;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.agua.Rateio;
import condominioPlus.negocio.cobranca.gas.ContaGas;
import condominioPlus.negocio.cobranca.gas.RateioGas;
import condominioPlus.util.FormatadorNumeros;
import condominioPlus.util.RenderizadorCelulaCorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
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
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import net.sf.nachocalendar.table.JTableCustomizer;

/**
 *
 * @author Administrador
 */
public class TelaGas extends javax.swing.JInternalFrame {

    private TabelaModelo_2<ContaGas> modeloContaGas;
    private TabelaModelo_2<RateioGas> modeloRateio;
    private ControladorEventos controlador;
    private Condominio condominio;
    private ContaGas conta;

    /** Creates new form TelaAgua */
    public TelaGas(Condominio condominio) {
        initComponents();

        this.condominio = condominio;

        carregarTabelaContaGas();
        tabelaContaGas.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        controlador = new ControladorEventos();




        if (modeloContaGas.size() > 0) {
            conta = modeloContaGas.getObjeto(0);
            carregarTabelaRateio();
            modeloContaGas.selecionar(conta, 0);
        }
    }

    private void carregarTabelaContaGas() {
        modeloContaGas = new TabelaModelo_2<ContaGas>(tabelaContaGas, "Data Inicial, Data Final, densidade, Quantidade KG, Valor Unitário KG, Quantidade M3, Valor Unitário M3, Total Despesas".split(",")) {

            @Override
            protected List<ContaGas> getCarregarObjetos() {
                return getContasGas();
            }

            @Override
            public void setValor(ContaGas conta, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        conta.setDataInicial(DataUtil.getCalendar(valor));
                        break;
                    case 1:
                        conta.setDataFinal(DataUtil.getCalendar(valor));
                        break;
                    case 2:
                        conta.setDensidadeMedia(((BigDecimal) valor));
                        break;
                    case 3:
                        conta.setQuantidadeKg((BigDecimal) valor);
                        break;
                    case 4:
                        conta.setValorUnitarioKg(((BigDecimal) valor));
                        break;
                    case 5:
                        conta.setQuantidadeMetroCubico(((BigDecimal) valor));
                        break;
                    case 6:
                        conta.setValorUnitarioMetroCubico(((BigDecimal) valor));
                        break;
                    case 7:
                        conta.setValorTotal(((BigDecimal) valor));
                        break;
                }
            }

            @Override
            public Object getValor(ContaGas conta, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return conta.getDataInicial();
                    case 1:
                        return conta.getDataFinal();
                    case 2:
                        return conta.getDensidadeMedia();
                    case 3:
                        return conta.getQuantidadeKg();
                    case 4:
                        return conta.getValorUnitarioKg();
                    case 5:
                        return conta.getQuantidadeMetroCubico();
                    case 6:
                        return conta.getValorUnitarioMetroCubico();
                    case 7:
                        return conta.getValorTotal();
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaADireita direito = new RenderizadorCelulaADireita();
//        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        tabelaContaGas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//
//
        modeloContaGas.setEditaveis(0, 1, 2, 3, 4);
//
        tabelaContaGas.getColumn(modeloContaGas.getCampo(0)).setCellRenderer(new RenderizadorCelulaData());
        tabelaContaGas.getColumn(modeloContaGas.getCampo(1)).setCellRenderer(new RenderizadorCelulaData());
//        tabelaContaGas.getColumn(modeloContaAgua.getCampo(7)).setCellRenderer(new RenderizadorCelulaData());
        JTableCustomizer.setEditorForRow(tabelaContaGas, 0);
        JTableCustomizer.setEditorForRow(tabelaContaGas, 1);
//        JTableCustomizer.setEditorForRow(tabelaContaGas, 7);



        for (int i = 2; i <= 7; i++) {
            tabelaContaGas.getColumn(modeloContaGas.getCampo(i)).setCellRenderer(direito);

        }
//        tabelaContaGas.getColumn(modeloContaAgua.getCampo(8)).setCellRenderer(centralizado);
//
        for (int i = 0; i <= 7; i++) {
            tabelaContaGas.getColumn(modeloContaGas.getCampo(i)).setMinWidth(100);

        }
//
        tabelaContaGas.getColumn(modeloContaGas.getCampo(5)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaGas.getColumn(modeloContaGas.getCampo(6)).setCellRenderer(new RenderizadorCelulaCorGenerico());
        tabelaContaGas.getColumn(modeloContaGas.getCampo(7)).setCellRenderer(new RenderizadorCelulaCorGenerico());

        tabelaContaGas.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "selectNextColumnCell");
    }

    private List<ContaGas> getContasGas() {
        return condominio.getContasDeGas();
    }

    private void calcularMetroCubico() {
        if (conta != null) {
            conta.setValorUnitarioMetroCubico(conta.getValorUnitarioKg().multiply(conta.getDensidadeMedia()));
            conta.setQuantidadeMetroCubico(new Moeda(conta.getQuantidadeKg().doubleValue() / (conta.getDensidadeMedia().doubleValue())).bigDecimalValue().setScale(2, RoundingMode.UP));
            conta.setValorTotal(new Moeda(conta.getValorUnitarioMetroCubico()).multiplica(conta.getQuantidadeMetroCubico()).bigDecimalValue().setScale(2, RoundingMode.UP));

        }
    }

    private BigDecimal verificarValor(Object valor) {

        if (valor instanceof String) {
            return FormatadorNumeros.casasDecimais(3, new BigDecimal(((String) valor).replaceAll(",", ".")));
        }

        return FormatadorNumeros.casasDecimais(3, new BigDecimal(((BigDecimal) valor).toString().replaceAll(",", ".")));

    }

    private void carregarTabelaRateio() {
        modeloRateio = new TabelaModelo_2<RateioGas>(tabelaRateio, "Unidade, Fração Ideal, Leitura Anterior, Leitura Atual, Consumo(M3), Consumo a Cobrar(R$), Consumo Área Comum(R$), Valor Total a Cobrar ".split(",")) {

            @Override
            protected List<RateioGas> getCarregarObjetos() {
                return getUnidadesRateio();
            }

            @Override
            public void setValor(RateioGas rateio, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 3:
                        rateio.setLeituraAtual(verificarValor(valor));
                        break;
                }
            }

            @Override
            public Object getValor(RateioGas rateio, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return rateio.getUnidade().getUnidade();
                    case 1:
                        return FormatadorNumeros.formatarDoubleToString(rateio.getUnidade().getFracaoIdeal(), "0.###");
                    case 2:
                        return rateio.getLeituraAnterior();
                    case 3:
                        return rateio.getLeituraAtual();
                    case 4:
                        return rateio.getConsumoMetroCubico();
                    case 5:
                        return new Moeda(rateio.getConsumoEmReaisUnidade());
                    case 6:
                        return new Moeda(rateio.getConsumoReaisAreaComum());
                    case 7:
                        return new Moeda(rateio.getConsumoTotal());
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaADireita direito = new RenderizadorCelulaADireita();
        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        tabelaRateio.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        modeloRateio.setEditaveis(3);



        for (int i = 0; i <= 7; i++) {
            tabelaRateio.getColumn(modeloRateio.getCampo(i)).setCellRenderer(direito);

        }
//        tabelaRateio.getColumn(modeloRateio.getCampo(8)).setCellRenderer(centralizado);
//        tabelaRateio.getColumn(modeloRateio.getCampo(3)).setCellRenderer(new RenderizadorCelulaCorGenerico());
//
        for (int i = 2; i <= 7; i++) {
            tabelaRateio.getColumn(modeloRateio.getCampo(i)).setMinWidth(150);

        }

        tabelaRateio.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "selectNextCell");


    }

    private List<RateioGas> getUnidadesRateio() {
        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                RateioGas r1 = (RateioGas) o1;
                RateioGas r2 = (RateioGas) o2;
                return (r1.getUnidade().getUnidade()).compareTo(r2.getUnidade().getUnidade());
            }
        };

        Collections.sort(conta.getRateios(), c);

        return conta.getRateios();
    }

    private void calcular() {
        List<RateioGas> rateios = modeloContaGas.getObjetoSelecionado().getRateios();
        if (!rateios.isEmpty()) {
            calcularMetroCubico();
            for (RateioGas rateio : rateios) {
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
                        valorMetroCubicoPorRateio(rateio);
                        
                    }
                } else {
                    System.out.println("valor nulo");
                }


            }


            calcularTotalAreaComum();

            

//            modeloContaAgua.carregarObjetos();
            modeloRateio.carregarObjetos();
////            totalValorConta();

        }
    }

    private void valorMetroCubicoPorRateio(RateioGas rateio) {
        if (rateio != null) {
            rateio.setConsumoEmReaisUnidade(rateio.getConsumoMetroCubico().multiply(conta.getValorUnitarioMetroCubico()).setScale(2, RoundingMode.UP));
        }
    }

    private void calcularTotalAreaComum(){
        Moeda soma = new Moeda();
        for (RateioGas rateio : conta.getRateios()) {
            soma.soma(rateio.getConsumoEmReaisUnidade());
        }

       Moeda valorAtualizado =  new Moeda (conta.getValorTotal().subtract(soma.bigDecimalValue()));

       Moeda valorAreaComum = new Moeda (valorAtualizado.doubleValue() / condominio.getUnidades().size());

       for (RateioGas rateio : conta.getRateios()) {
            rateio.setConsumoReaisAreaComum(valorAreaComum.bigDecimalValue());
            totalValorRateio(rateio);
        }

    }

    private void totalValorRateio(RateioGas rateio) {
        
        Moeda total = new Moeda(rateio.getConsumoEmReaisUnidade()).soma(rateio.getConsumoReaisAreaComum());
        
        rateio.setConsumoTotal(total.bigDecimalValue());

    }

    private void incluirContaGas() {


        conta = new ContaGas();

        List<Unidade> unidades = new DAO().listar("UnidadePorCondominio", condominio.getCodigo());
        List<RateioGas> rateios = new ArrayList<RateioGas>();

        for (Unidade unidade : unidades) {

            RateioGas rateio = new RateioGas(unidade);
            if (modeloContaGas.size() == 0) {
                rateio.setLeituraAnterior(BigDecimal.ZERO);
            } else {
                ContaGas c = modeloContaGas.getObjeto(modeloContaGas.size() - 1);
                for (RateioGas r : c.getRateios()) {
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
        modeloContaGas.adicionar(conta);
        modeloContaGas.selecionar(conta, 0);
        carregarTabelaRateio();
//        modeloContaAgua.setSelecaoMultipla(false);



    }

    private void removerContaGas() {
        if (modeloContaGas.getObjetoSelecionado() != null) {


            modeloContaGas.remover(modeloContaGas.getObjetoSelecionado());

            new DAO().remover(conta);

            conta = null;

            modeloRateio.setObjetos(null);

            carregarTabelaContaGas();

            ApresentacaoUtil.exibirInformacao("Removido com Sucesso!", this);

        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void fechar() {
        doDefaultCloseAction();
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

//        if (conta.getValorProlagos().doubleValue() > 0 && conta.getConsumoProlagos().doubleValue() > 0) {
//            if (conta.getDataVencimento == null) {
//                ApresentacaoUtil.exibirAdvertencia("Por favor entre com o vencimento da conta!", this);
//                return false;
//            } else {
        return true;
//            }


//        }

//        return true;
    }

    private void salvarGas() {
        try {
            new DAO().salvar(conta);

            ApresentacaoUtil.exibirInformacao("Conta Salva com Sucesso!", this);

        } catch (Exception e) {
            e.printStackTrace();
            ApresentacaoUtil.exibirInformacao("Ocorreu um erro ao tentar salvar a conta", this);
        }

    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();

            if (origem == btnIncluir || origem == itemMenuIncluirRegistroContaAgua) {
                incluirContaGas();
            } else if (origem == itemMenuDeletarRegistroContaAgua) {
                removerContaGas();
            } else if (origem == btnCalcular) {
                calcular();
            } else if (origem == btnSalvarAgua || origem == itemMenuGravarAlteracoesContaAgua || origem == itemMenuGravarAlteracoesPipa) {
                salvarGas();
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
            btnIncluir.addActionListener(this);
            tabelaContaGas.addMouseListener(this);
            tabelaContaGas.addKeyListener(this);
            tabelaRateio.addMouseListener(this);
            btnCalcular.addActionListener(this);
            btnSalvarAgua.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabelaContaGas) {
                popupContaAgua.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Object origem = e.getSource();
            if (origem == tabelaContaGas && (e.getKeyCode() == KeyEvent.VK_UP) || origem == tabelaContaGas && (e.getKeyCode() == KeyEvent.VK_DOWN)) {
                modeloRateio.setObjetos(modeloContaGas.getObjetoSelecionado().getRateios());

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
        btnSalvarAgua = new javax.swing.JButton();
        btnCalcular = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaContaGas = new javax.swing.JTable();
        abaPipa = new javax.swing.JTabbedPane();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaRateio = new javax.swing.JTable();

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
        setTitle("Cálculo de Gás");

        btnIncluir.setText("Incluir");

        btnSalvarAgua.setText("Salvar");

        btnCalcular.setText("Calcular");

        btnImprimir.setText("Imprimir");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSalvarAgua)
                .addGap(18, 18, 18)
                .addComponent(btnCalcular)
                .addGap(27, 27, 27)
                .addComponent(btnImprimir)
                .addContainerGap(161, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir, btnSalvarAgua});

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalvarAgua)
                    .addComponent(btnCalcular)
                    .addComponent(btnImprimir)
                    .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir, btnSalvarAgua});

        tabelaContaGas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tabelaContaGas);

        tabelaRateio.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tabelaRateio);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 698, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                .addContainerGap())
        );

        abaPipa.addTab("Rateio", jPanel8);

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
                .addContainerGap(12, Short.MAX_VALUE))
        );

        abaCalculoMensal.addTab("Cálculos Mensais de Àgua", jPanel1);

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane abaCalculoMensal;
    private javax.swing.JTabbedPane abaPipa;
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnSalvarAgua;
    private javax.swing.JMenuItem itemMenuAdicionar;
    private javax.swing.JMenuItem itemMenuDeletarPipa;
    private javax.swing.JMenuItem itemMenuDeletarRegistroContaAgua;
    private javax.swing.JMenuItem itemMenuEditar;
    private javax.swing.JMenuItem itemMenuGravarAlteracoesContaAgua;
    private javax.swing.JMenuItem itemMenuGravarAlteracoesPipa;
    private javax.swing.JMenuItem itemMenuIncluirPipa;
    private javax.swing.JMenuItem itemMenuIncluirRegistroContaAgua;
    private javax.swing.JMenuItem itemMenuRemover;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu popupContaAgua;
    private javax.swing.JPopupMenu popupPipa;
    private javax.swing.JPopupMenu popupTarifaProlagos;
    private javax.swing.JTable tabelaContaGas;
    private javax.swing.JTable tabelaRateio;
    // End of variables declaration//GEN-END:variables
}
