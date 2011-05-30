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
import condominioPlus.negocio.cobranca.agua.ContaAgua;
import condominioPlus.negocio.cobranca.agua.FormaCalculoMetroCubico;
import condominioPlus.negocio.cobranca.agua.FormaRateioAreaComum;
import condominioPlus.negocio.cobranca.agua.ParametrosCalculoAgua;
import condominioPlus.negocio.cobranca.agua.TarifaProlagos;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.util.RenderizadorCelulaDireita;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.RenderizadorCelulaADireita;
import logicpoint.apresentacao.RenderizadorCelulaCentralizada;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.Moeda;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class TelaAgua extends javax.swing.JInternalFrame {

    private TabelaModelo_2<TarifaProlagos> modelo;
    private TabelaModelo_2<ContaAgua> modeloContaAgua;
    private ControladorEventos controlador;
    private ParametrosCalculoAgua parametros;
    private Condominio condominio;

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
        controlador = new ControladorEventos();

        preencherTela();
    }

    private void carregarComboFormaPrecoMetroCubico() {
        cbFormaCalculoMetroCubico.setModel(new ComboModelo<String>(Util.toList(new String[]{FormaCalculoMetroCubico.SEM_VALOR.toString(), FormaCalculoMetroCubico.DIVIDIR_METROS_CUBICOS.toString(), FormaCalculoMetroCubico.SINDICO_PRECO.toString(), FormaCalculoMetroCubico.TABELA_PROLAGOS.toString()})));
    }

    private void carregarComboFormaRateioAreaComum() {
        cbFormaRateioAreaComum.setModel(new ComboModelo<String>(Util.toList(new String[]{FormaRateioAreaComum.SEM_VALOR.toString(), FormaRateioAreaComum.IGUAL_TODOS.toString(), FormaRateioAreaComum.NAO_COBRAR.toString(), FormaRateioAreaComum.PROPORCIONAL_CONSUMO.toString(), FormaRateioAreaComum.PROPORCIONAL_FRACAO.toString()})));
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
        modeloContaAgua = new TabelaModelo_2<ContaAgua>(tabelaContaAgua, "Data Inicial, Data Final, Valor Prolagos(R$), Consumo Prolagos(M3), Valor Pipa (R$), Consumo Pipa(M3), Preço(M3), Data Vencimento da Conta, Consumo Unidades(M3), Consumo Total Unidades(R$), Total de Despesas".split(",")) {

            @Override
            protected List<ContaAgua> getCarregarObjetos() {
                return getContasAgua();
            }

            @Override
            public Object getValor(ContaAgua conta, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return conta.getDataInicial();
                    case 1:
                        return conta.getDataFinal();
                    case 2:
                        return conta.getValorProlagos();
                    case 3:
                        return conta.getConsumoProlagos();
                    case 4:
                        return conta.getValorPipa();
                    case 5:
                        return conta.getConsumoPipa();
                    case 6:
                        return conta.getPrecoMetroCubico();
                    case 7:
                        return conta.getDataVencimentoConta();
                    case 8:
                        return conta.getConsumoUnidadesMetroCubico();
                    case 9:
                        return conta.getPrecoTotalUnidades();
                    case 10:
                        return conta.getTotalDespesas();
                    default:
                        return null;

                }
            }
        };

        RenderizadorCelulaADireita direito = new RenderizadorCelulaADireita();
        RenderizadorCelulaCentralizada centralizado = new RenderizadorCelulaCentralizada();

        tabelaContaAgua.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i <= 7; i++) {
            tabelaContaAgua.getColumn(modeloContaAgua.getCampo(i)).setCellRenderer(direito);

        }
        tabelaContaAgua.getColumn(modeloContaAgua.getCampo(8)).setCellRenderer(centralizado);

        for (int i = 0; i <= 10; i++) {
            tabelaContaAgua.getColumn(modeloContaAgua.getCampo(i)).setMinWidth(80);

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
        }

        return FormaRateioAreaComum.SEM_VALOR;
    }

    private FormaCalculoMetroCubico selecionarCalculoMetroCubico() {
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
        parametros.setQuantidadeMetrosCubicosNaCota((Integer) spinnerQuantidadeIncluirCota.getValue());
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
        spinnerQuantidadeIncluirCota.setValue(parametros.getQuantidadeMetrosCubicosNaCota());
        checkHidrometroAreaComum.setSelected(parametros.isHidrometroAreaComum());
        checkNaoCobrarPipa.setSelected(parametros.isCobrarPipa());
        txtValorFixoAreaComum.setText(new Moeda(parametros.getValorFixoAreaComum()).toString());
        txtValorSindico.setText(new Moeda(parametros.getValorMetroCubicoSindico()).toString());
    }

    private void fechar() {
        doDefaultCloseAction();
    }

    private void salvar() {

        preencherObjeto();
        try {
            new DAO().salvar(condominio.getParametros());

            ApresentacaoUtil.exibirInformacao("Parâmetros salvos com Sucesso!", this);
            fechar();

        } catch (Exception e) {
            e.printStackTrace();
            ApresentacaoUtil.exibirInformacao("Ocorreu um erro ao tentar salvar os parâmetros", this);
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
            }
        }

        @Override
        public void configurar() {
            itemMenuAdicionar.addActionListener(this);
            itemMenuEditar.addActionListener(this);
            itemMenuRemover.addActionListener(this);
            tabela.addMouseListener(this);
            btnVoltar.addActionListener(this);
            btnSalvar.addActionListener(this);
            btnAdicionar.addActionListener(this);
            btnEditar.addActionListener(this);
            btnRemover.addActionListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
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
        itemMenuAdicionar = new javax.swing.JMenuItem();
        itemMenuEditar = new javax.swing.JMenuItem();
        itemMenuRemover = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
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
        popupMenu.add(itemMenuAdicionar);

        itemMenuEditar.setText("Editar Tarifa");
        popupMenu.add(itemMenuEditar);

        itemMenuRemover.setText("Remover Tarifa");
        popupMenu.add(itemMenuRemover);

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
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tabelaContaAgua);

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
        jScrollPane3.setViewportView(tabelaRateio);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
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

        tabelaPipa.setModel(new javax.swing.table.DefaultTableModel(
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
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
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
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cálculos Mensais de Àgua", jPanel1);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
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

        btnVoltar.setText("Voltar");

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Parâmetros para Cálculo de Consumo de Água", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 748, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JMenuItem itemMenuEditar;
    private javax.swing.JMenuItem itemMenuRemover;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
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
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JSpinner spinnerQuantidadeIncluirCota;
    private javax.swing.JTable tabela;
    private javax.swing.JTable tabelaContaAgua;
    private javax.swing.JTable tabelaPipa;
    private javax.swing.JTable tabelaRateio;
    private javax.swing.JTextField txtValorFixoAreaComum;
    private javax.swing.JTextField txtValorSindico;
    // End of variables declaration//GEN-END:variables
}
