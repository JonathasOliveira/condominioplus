/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaAgua.java
 *
 * Created on 18/05/2011, 16:04:14
 */
package condominioPlus.apresentacao.cobranca.luz;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.gas.RateioGas;
import condominioPlus.negocio.cobranca.luz.ContaLuz;
import condominioPlus.negocio.cobranca.luz.RateioLuz;
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
public class TelaLuz extends javax.swing.JInternalFrame {

    private TabelaModelo_2<ContaLuz> modeloContaLuz;
    private TabelaModelo_2<RateioLuz> modeloRateio;
    private ControladorEventos controlador;
    private Condominio condominio;
    private ContaLuz conta;

    /** Creates new form TelaAgua */
    public TelaLuz(Condominio condominio) {
        initComponents();

        this.condominio = condominio;

        carregarTabelaContaGas();
        tabelaContaLuz.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        controlador = new ControladorEventos();




        if (modeloContaLuz.size() > 0) {
            conta = modeloContaLuz.getObjeto(0);
            carregarTabelaRateio();
            modeloContaLuz.selecionar(conta, 0);
        }
    }

    private void carregarTabelaContaGas() {
        modeloContaLuz = new TabelaModelo_2<ContaLuz>(tabelaContaLuz, "Data Inicial, Data Final, Constante, Quantidade KWH, Valor Unitário KWH, Taxa Pública, Data Vencimento , Total Unidades, Total Area Comum,Total".split(",")) {

            @Override
            protected List<ContaLuz> getCarregarObjetos() {
                return getContasLuz();
            }

            @Override
            public void setValor(ContaLuz conta, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        conta.setDataInicial(DataUtil.getCalendar(valor));
                        break;
                    case 1:
                        conta.setDataFinal(DataUtil.getCalendar(valor));
                        break;
                    case 2:
                        conta.setConstante(((BigDecimal) valor));
                        break;
                    case 3:
                        conta.setQuantidadeWatts((BigDecimal) valor);
                        break;
                    case 4:
                        conta.setValorUnitarioWatts(((BigDecimal) valor));
                        break;
                    case 5:
                        conta.setTaxaPublica(((BigDecimal) valor));
                        break;
                    case 6:
                        conta.setDataVencimento(DataUtil.getCalendar(valor));
                        break;

                }
            }

            @Override
            public Object getValor(ContaLuz conta, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return conta.getDataInicial();
                    case 1:
                        return conta.getDataFinal();
                    case 2:
                        return conta.getConstante();
                    case 3:
                        return conta.getQuantidadeWatts();
                    case 4:
                        return conta.getValorUnitarioWatts();
                    case 5:
                        return conta.getTaxaPublica();
                    case 6:
                        return conta.getDataVencimento();
                    case 7:
                        return new Moeda(conta.getTotalUnidadesDinheiro());
                    case 8:
                        return new Moeda(conta.getTotalConsumoAreaComum());
                    case 9:
                        return new Moeda(conta.getTotalFatura());
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaADireita direito = new RenderizadorCelulaADireita();
//        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        tabelaContaLuz.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//
//
        modeloContaLuz.setEditaveis(0, 1, 2, 3, 4, 5, 6);
//
        tabelaContaLuz.getColumn(modeloContaLuz.getCampo(0)).setCellRenderer(new RenderizadorCelulaData());
        tabelaContaLuz.getColumn(modeloContaLuz.getCampo(1)).setCellRenderer(new RenderizadorCelulaData());
        tabelaContaLuz.getColumn(modeloContaLuz.getCampo(6)).setCellRenderer(new RenderizadorCelulaData());
//        tabelaContaGas.getColumn(modeloContaAgua.getCampo(7)).setCellRenderer(new RenderizadorCelulaData());
        JTableCustomizer.setEditorForRow(tabelaContaLuz, 0);
        JTableCustomizer.setEditorForRow(tabelaContaLuz, 1);
        JTableCustomizer.setEditorForRow(tabelaContaLuz, 6);



        for (int i = 2; i <= 5; i++) {
            tabelaContaLuz.getColumn(modeloContaLuz.getCampo(i)).setCellRenderer(direito);

        }
//        tabelaContaGas.getColumn(modeloContaAgua.getCampo(8)).setCellRenderer(centralizado);
//
        for (int i = 0; i <= 7; i++) {
            tabelaContaLuz.getColumn(modeloContaLuz.getCampo(i)).setMinWidth(100);

        }
        tabelaContaLuz.getColumn(modeloContaLuz.getCampo(8)).setMinWidth(100);
        tabelaContaLuz.getColumn(modeloContaLuz.getCampo(9)).setMinWidth(100);
//
        tabelaContaLuz.getColumn(modeloContaLuz.getCampo(7)).setCellRenderer(new RenderizadorCelulaCorGenerico());

        tabelaContaLuz.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "selectNextColumnCell");
    }

    private List<ContaLuz> getContasLuz() {
        List<ContaLuz> contas = new DAO().listar("ContasLuzPorCondominio", condominio);
        return contas;
    }

   private BigDecimal verificarValor(Object valor) {

        if (valor instanceof String) {
            return FormatadorNumeros.casasDecimais(3, new BigDecimal(((String) valor).replaceAll(",", ".")));
        }

        return FormatadorNumeros.casasDecimais(3, new BigDecimal(((BigDecimal) valor).toString().replaceAll(",", ".")));

    }

    private void carregarTabelaRateio() {
        modeloRateio = new TabelaModelo_2<RateioLuz>(tabelaRateio, "Unidade, Fração Ideal, Leitura Anterior, Leitura Atual, Consumo(Kwh), Consumo a Cobrar(R$), Consumo Área Comum(R$), Valor Total a Cobrar ".split(",")) {

            @Override
            protected List<RateioLuz> getCarregarObjetos() {
                return getUnidadesRateio();
            }

            @Override
            public void setValor(RateioLuz rateio, Object valor, int indiceColuna) {
                switch (indiceColuna) {
                    case 3:
                        rateio.setLeituraAtual(verificarValor(valor));
                        break;
                }
            }

            @Override
            public Object getValor(RateioLuz rateio, int indiceColuna) {
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
                        return rateio.getConsumoWatts();
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

    private List<RateioLuz> getUnidadesRateio() {
        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                RateioLuz r1 = (RateioLuz) o1;
                RateioLuz r2 = (RateioLuz) o2;
                return (r1.getUnidade().getUnidade()).compareTo(r2.getUnidade().getUnidade());
            }
        };

        Collections.sort(conta.getRateios(), c);

        return conta.getRateios();
    }
    
    private void somarValoresContaLuz(){
        conta.setTotalFatura(new Moeda((conta.getValorUnitarioWatts().multiply(conta.getQuantidadeWatts())).add(conta.getTaxaPublica())).bigDecimalValue());
    }

    private void calcular() {
        List<RateioLuz> rateios = modeloContaLuz.getObjetoSelecionado().getRateios();
        if (!rateios.isEmpty()) {
            somarValoresContaLuz();
            for (RateioLuz rateio : rateios) {
                if (rateio.getLeituraAtual() != null && rateio.getLeituraAnterior() != null) {
                    if (rateio.getLeituraAnterior().doubleValue() > rateio.getLeituraAtual().doubleValue()) {
                        ApresentacaoUtil.exibirAdvertencia("O valor da leitura atual não pode ser Menor que a anterior!", this);
                        return;
                    } else {
                        BigDecimal valorConsumo = rateio.getLeituraAtual().subtract(rateio.getLeituraAnterior());
                        if (valorConsumo.intValue() < 0) {
                            rateio.setConsumoWatts(BigDecimal.ZERO);
                        } else {
                            rateio.setConsumoWatts(valorConsumo);
                        }
                        valorWattsPorRateio(rateio);

                    }
                } else {
                    System.out.println("valor nulo");
                }


            }

            calcularTotalConsumoUnidades();
            calcularTotalAreaComum();
            calcularTotalConsumoAreaComum();

            modeloRateio.carregarObjetos();
////            totalValorConta();

        }
    }

    private void valorWattsPorRateio(RateioLuz rateio) {
        if (rateio != null) {
            rateio.setConsumoEmReaisUnidade(rateio.getConsumoWatts().multiply(conta.getValorUnitarioWatts()).setScale(2, RoundingMode.UP));
        }
    }

    private void calcularTotalAreaComum() {
 

        Moeda valorAtualizado = new Moeda(conta.getTotalFatura().subtract(conta.getTotalUnidadesDinheiro()));

        Moeda valorAreaComum = new Moeda(valorAtualizado.doubleValue() / condominio.getUnidades().size());
        System.out.println("valor area comum " + valorAreaComum);

        for (RateioLuz rateio : conta.getRateios()) {
            if (valorAreaComum.doubleValue() >= 0) {
                rateio.setConsumoReaisAreaComum(valorAreaComum.bigDecimalValue());
                totalValorRateio(rateio);
            } else {
                rateio.setConsumoReaisAreaComum(BigDecimal.ZERO);
                totalValorRateio(rateio);
            }
        }

    }
    
     private void calcularTotalConsumoUnidades() {
        List<RateioLuz> rateios = modeloContaLuz.getObjetoSelecionado().getRateios();
        Moeda totalDinheiro = new Moeda(0);
        for (RateioLuz rateio : rateios) {
              totalDinheiro.soma(rateio.getConsumoEmReaisUnidade());
        }

        conta.setTotalUnidadesDinheiro(totalDinheiro.bigDecimalValue());
    }
     
       private void calcularTotalConsumoAreaComum() {
        List<RateioLuz> rateios = modeloContaLuz.getObjetoSelecionado().getRateios();
        Moeda totalDinheiro = new Moeda(0);
        for (RateioLuz rateio : rateios) {
              totalDinheiro.soma(rateio.getConsumoReaisAreaComum());
        }

        conta.setTotalConsumoAreaComum(totalDinheiro.bigDecimalValue());
    }


    private void totalValorRateio(RateioLuz rateio) {

        Moeda total = new Moeda(rateio.getConsumoEmReaisUnidade()).soma(rateio.getConsumoReaisAreaComum());

        rateio.setConsumoTotal(total.bigDecimalValue());

    }

    private void incluirContaLuz() {


        conta = new ContaLuz();

        List<Unidade> unidades = new DAO().listar("UnidadePorCondominio", condominio.getCodigo());
        List<RateioLuz> rateios = new ArrayList<RateioLuz>();

        for (Unidade unidade : unidades) {

            RateioLuz rateio = new RateioLuz(unidade);
            if (modeloContaLuz.size() == 0) {
                rateio.setLeituraAnterior(BigDecimal.ZERO);
            } else {
                ContaLuz c = modeloContaLuz.getObjeto(modeloContaLuz.size() - 1);
                for (RateioLuz r : c.getRateios()) {
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
        modeloContaLuz.adicionar(conta);
        modeloContaLuz.selecionar(conta, 0);
        carregarTabelaRateio();
//        modeloContaAgua.setSelecaoMultipla(false);



    }

    private void removerContaLuz() {
        if (modeloContaLuz.getObjetoSelecionado() != null) {
            if (ApresentacaoUtil.perguntar("Tem certeza que deseja excluir? ", this) == true) {


                modeloContaLuz.remover(modeloContaLuz.getObjetoSelecionado());

                new DAO().remover(conta);

                conta = null;

                modeloRateio.setObjetos(null);

                carregarTabelaContaGas();

                ApresentacaoUtil.exibirInformacao("Removido com Sucesso!", this);
            }

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

    private void salvarLuz() {
        try {
            if (!validarCampos()) {
                return;
            }
            new DAO().salvar(conta);

            ApresentacaoUtil.exibirInformacao("Conta Salva com Sucesso!", this);

        } catch (Exception e) {
            ApresentacaoUtil.exibirInformacao("Ocorreu um erro ao tentar salvar a conta", this);
        }

    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();

            if (origem == btnIncluir || origem == itemMenuIncluirRegistroContaAgua) {
                incluirContaLuz();
            } else if (origem == itemMenuDeletarRegistroContaAgua) {
                removerContaLuz();
            } else if (origem == btnCalcular) {
                calcular();
            } else if (origem == btnSalvarLuz || origem == itemMenuGravarAlteracoesContaAgua || origem == itemMenuGravarAlteracoesPipa) {
                salvarLuz();
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
            tabelaContaLuz.addMouseListener(this);
            tabelaContaLuz.addKeyListener(this);
            tabelaRateio.addMouseListener(this);
            btnCalcular.addActionListener(this);
            btnSalvarLuz.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabelaContaLuz) {
                popupContaAgua.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Object origem = e.getSource();
            if (origem == tabelaContaLuz && (e.getKeyCode() == KeyEvent.VK_UP) || origem == tabelaContaLuz && (e.getKeyCode() == KeyEvent.VK_DOWN)) {
                modeloRateio.setObjetos(modeloContaLuz.getObjetoSelecionado().getRateios());

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
        btnSalvarLuz = new javax.swing.JButton();
        btnCalcular = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaContaLuz = new javax.swing.JTable();
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
        setTitle("Cálculo de Luz");

        btnIncluir.setText("Incluir");

        btnSalvarLuz.setText("Salvar");

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
                .addComponent(btnSalvarLuz)
                .addGap(18, 18, 18)
                .addComponent(btnCalcular)
                .addGap(27, 27, 27)
                .addComponent(btnImprimir)
                .addContainerGap(161, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir, btnSalvarLuz});

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalvarLuz)
                    .addComponent(btnCalcular)
                    .addComponent(btnImprimir)
                    .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnCalcular, btnImprimir, btnIncluir, btnSalvarLuz});

        tabelaContaLuz.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tabelaContaLuz);

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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
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
                .addComponent(abaPipa, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        abaCalculoMensal.addTab("Cálculos Mensais de Luz", jPanel1);

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
                .addComponent(abaCalculoMensal, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE)
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
    private javax.swing.JButton btnSalvarLuz;
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
    private javax.swing.JTable tabelaContaLuz;
    private javax.swing.JTable tabelaRateio;
    // End of variables declaration//GEN-END:variables
}
