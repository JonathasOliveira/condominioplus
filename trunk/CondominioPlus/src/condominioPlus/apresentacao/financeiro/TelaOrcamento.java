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
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;

/**
 *
 * @author eugenia
 */
public class TelaOrcamento extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private Calendar datInicio = DataUtil.getCalendar(DataUtil.hoje());
    private Calendar datTermino = DataUtil.getCalendar(DataUtil.hoje());
    private TabelaModelo_2 modeloTabela;
    private List<ContaOrcamentaria> contasOrcamentarias = new ArrayList<ContaOrcamentaria>();
    private TabelaModelo_2 modeloTabelaContasExtraordinarias;
    private List<ContaOrcamentaria> contasExtraordinarias = new ArrayList<ContaOrcamentaria>();
    private TabelaModelo_2 modeloTabelaContasExcluidas;
    private List<ContaOrcamentaria> contasExcluidas = new ArrayList<ContaOrcamentaria>();
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
    }

    private void preencherTela() {
        txtNomeCondominio.setText(condominio.getRazaoSocial());
        txtDataInicial.setValue(DataUtil.toString(DataUtil.getPrimeiroDiaMes()));
        txtDataFinal.setValue(DataUtil.toString(DataUtil.getUltimoDiaMes()));
        txtNumeroUnidades.setText(Integer.toString(condominio.getUnidades().size()));
        calcularQuantidadeMeses();
    }

    private List<Unidade> getUnidades() {
        return condominio.getUnidades();
    }

    private int getUnidadesDescartadas() {
        int unidadesADescartar = 0;
        for (Unidade u : getUnidades()) {
            if (u.isSindico() && !condominio.isSindicoPaga()) {
                unidadesADescartar += 1;
            } else {
                int quantidadeCobrancasInadimplentes = 0;
                for (Cobranca c : u.getCobrancas()) {
                    if (c.getDataPagamento() == null && DataUtil.getDiferencaEmDias(DataUtil.hoje(), DataUtil.getDateTime(c.getDataVencimento())) >= 1 && c.isExibir()) {
                        quantidadeCobrancasInadimplentes += 1;
                    }
                }
                System.out.println("Unidade " + u.getUnidade() + " - número cobranças inadimplentes: " + quantidadeCobrancasInadimplentes);
                if (quantidadeCobrancasInadimplentes >= (Integer) spnQtdeDescarte.getValue() && (Integer) spnQtdeDescarte.getValue() != 0) {
                    unidadesADescartar += 1;
                }
            }
        }
        System.out.println("Quantidade Unidades a descartar: " + unidadesADescartar);
        return unidadesADescartar;
    }

    private void periodoParaMeses() {
    }
    
    private void carregarTabelaContasOrcamentarias(){
        
    }

    private List<Pagamento> getContasPorPeriodo() {
        List<Pagamento> pagamentos = new DAO().listar(Pagamento.class, "PagamentosPorPeriodoContaCorrente", condominio.getContaCorrente(), datInicio, datTermino);
        return pagamentos;
    }

    private void getApenasDespesas(List<Pagamento> getContasPorPeriodo) {
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
                contasOrcamentarias.add(c1);
            }
        }       
        
        if(contasOrcamentarias.isEmpty()){
            ApresentacaoUtil.exibirAdvertencia("Não houve custos no período selecionado.", this);
        } else {
            calcularMedias();
        }
    }
    
    private void calcularMedias(){
        getUnidadesDescartadas();
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

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            txtDataInicial.addChangeListener(this);
            txtDataFinal.addChangeListener(this);
            btnCalcular.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnCalcular) {
                if (calcular) {
                    getApenasDespesas(getContasPorPeriodo());
                } else {
                    ApresentacaoUtil.exibirAdvertencia("Não é possível efetuar o cálculo para um período maior que 1 ano!", TelaOrcamento.this);
                }
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
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        TabelaMedias = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
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

        TabelaMedias.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(TabelaMedias);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelTabelas.addTab("Médias Obtidas", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
        );

        painelTabelas.addTab("Contas Extraordinárias", jPanel3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
        );

        painelTabelas.addTab("Contas Excluídas", jPanel4);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
        );

        painelTabelas.addTab("Unidades a Descartar", jPanel6);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 589, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
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
        btnIncluir.setToolTipText("Incluir Conta");
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
                            .addComponent(painelTabelas, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(painelTabelas, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpar)
                    .addComponent(btnCalcular)
                    .addComponent(btnImprimir))
                .addContainerGap())
        );

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
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TabelaMedias;
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnLimpar;
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
    private javax.swing.JTabbedPane painelTabelas;
    private javax.swing.JSpinner spnIncremento1;
    private javax.swing.JSpinner spnIncremento2;
    private javax.swing.JSpinner spnIncremento3;
    private javax.swing.JSpinner spnQtdeDescarte;
    private net.sf.nachocalendar.components.DateField txtDataFinal;
    private net.sf.nachocalendar.components.DateField txtDataInicial;
    private javax.swing.JTextField txtDescricaoDiversos;
    private javax.swing.JTextField txtNomeCondominio;
    private javax.swing.JTextField txtNumeroUnidades;
    private javax.swing.JTextField txtQtdeMeses;
    private javax.swing.JTextField txtTaxaBase;
    private javax.swing.JTextField txtValorDiversos;
    // End of variables declaration//GEN-END:variables
}
