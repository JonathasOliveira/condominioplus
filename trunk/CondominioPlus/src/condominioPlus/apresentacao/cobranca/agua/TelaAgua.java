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
import condominioPlus.negocio.cobranca.agua.FormaCalculoMetroCubico;
import condominioPlus.negocio.cobranca.agua.FormaRateioAreaComum;
import condominioPlus.negocio.cobranca.agua.ParametrosCalculoAgua;
import condominioPlus.negocio.cobranca.agua.TarifaProlagos;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.util.RenderizadorCelulaDireita;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class TelaAgua extends javax.swing.JInternalFrame {

    private TabelaModelo_2<TarifaProlagos> modelo;
    private ControladorEventos controlador;
    private ParametrosCalculoAgua parametros;

    /** Creates new form TelaAgua */
    public TelaAgua(Condominio condominio) {
        initComponents();
        carregarComboFormaPrecoMetroCubico();
        carregarComboFormaRateioAreaComum();
        carregarTabela();
        if (condominio.getParametros() != null) {
            this.parametros = condominio.getParametros();
        }else{
            parametros = new ParametrosCalculoAgua();
            condominio.setParametros(parametros);
            new DAO().salvar(condominio);
        }
        controlador = new ControladorEventos();
    }

    private void carregarComboFormaPrecoMetroCubico() {
        cbFormaCalculoMetroCubico.setModel(new ComboModelo<String>(Util.toList(new String[]{"", FormaCalculoMetroCubico.DIVIDIR_METROS_CUBICOS.toString(), FormaCalculoMetroCubico.SINDICO_PRECO.toString(), FormaCalculoMetroCubico.TABELA_PROLAGOS.toString()})));
    }

    private void carregarComboFormaRateioAreaComum() {
        cbFormaRateioAreaComum.setModel(new ComboModelo<String>(Util.toList(new String[]{"", FormaRateioAreaComum.IGUAL_TODOS.toString(), FormaRateioAreaComum.NAO_COBRAR.toString(), FormaRateioAreaComum.PROPORCIONAL_CONSUMO.toString(), FormaRateioAreaComum.PROPORCIONAL_FRACAO.toString()})));
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

    private List<TarifaProlagos> getTarifaProlagos() {
        List<TarifaProlagos> tarifas = new DAO().listar("TarifaPorId");
        return tarifas;
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

    private void preencherObjeto() {
        parametros.setFormaAreaComum((FormaRateioAreaComum) cbFormaRateioAreaComum.getSelectedItem());
        parametros.setFormaMetroCubico((FormaCalculoMetroCubico) cbFormaCalculoMetroCubico.getSelectedItem());
        parametros.setQuantidadeMetrosCubicosNaCota((Integer) spinnerQuantidadeIncluirCota.getValue());
        parametros.setCobrarPipa(checkNaoCobrarPipa.isSelected());
        parametros.setHidrometroAreaComum(checkHidrometroAreaComum.isSelected());
        parametros.setValorFixoAreaComum(new BigDecimal(txtValorFixoAreaComum.getText()));
        parametros.setValorMetroCubicoSindico(new BigDecimal(txtValorSindico.getText()));
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();

            if (origem == itemMenuAdicionar) {
                adicionar();
            } else if (origem == itemMenuEditar) {
                editar();
            } else if (origem == itemMenuRemover) {
                remover();
            }
        }

        @Override
        public void configurar() {
            itemMenuAdicionar.addActionListener(this);
            itemMenuEditar.addActionListener(this);
            itemMenuRemover.addActionListener(this);
            tabela.addMouseListener(this);
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
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 462, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addContainerGap())
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
                    .addComponent(jLabel6)
                    .addComponent(cbFormaRateioAreaComum, 0, 237, Short.MAX_VALUE)
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
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorFixoAreaComum, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                    .addComponent(cbFormaCalculoMetroCubico, 0, 237, Short.MAX_VALUE)
                    .addComponent(checkHidrometroAreaComum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(26, 26, 26)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtValorSindico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtValorFixoAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(checkHidrometroAreaComum)
                .addGap(18, 18, 18)
                .addComponent(checkNaoCobrarPipa)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFormaRateioAreaComum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
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
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))))
        );

        btnSalvar.setText("Salvar");

        btnVoltar.setText("Voltar");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(235, 235, 235)
                .addComponent(btnSalvar)
                .addGap(132, 132, 132)
                .addComponent(btnVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(238, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSalvar)
                    .addComponent(btnVoltar))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Parâmetros para Cálculo de Consumo de Água", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 761, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSalvar;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JSpinner spinnerQuantidadeIncluirCota;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtValorFixoAreaComum;
    private javax.swing.JTextField txtValorSindico;
    // End of variables declaration//GEN-END:variables
}
