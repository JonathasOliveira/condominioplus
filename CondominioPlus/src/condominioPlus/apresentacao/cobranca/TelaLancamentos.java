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
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.cobranca.Cobranca;
import condominioPlus.negocio.cobranca.CobrancaBase;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.TabelaModelo_2;

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

    /** Creates new form TelaLancamentos */
    public TelaLancamentos(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        carregarTabelaCondominos();
        carregarTabelaCobrancaBase();
        carregarTabelaCobranca();

        if (condominio != null) {
            this.setTitle("Lançamentos - " + condominio.getRazaoSocial());
        }
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
        modeloTabelaBoleto = new TabelaModelo_2<Cobranca>(tabelaCobrancas, "Unidade, Condominio, Documento, Total, Linha Digitável".split(",")) {

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
                        return "Doc";
                    case 3:
                        return PagamentoUtil.formatarMoeda(cobranca.getValorTotal().doubleValue());
                    case 4:
                        return "LD";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer direito = new DefaultTableCellRenderer();
        direito.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(0)).setCellRenderer(direito);

        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(0)).setMaxWidth(60);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(1)).setMaxWidth(80);
        tabelaCobrancas.getColumn(modeloTabelaBoleto.getCampo(3)).setMaxWidth(70);

    }

    private List<Cobranca> getCobrancas() {
        for (Unidade u : condominio.getUnidades()) {
            for (Cobranca c : u.getCobrancas()) {
                if (c.getDataPagamento() == null) {
                    listaCobrancas.add(c);
                }
            }
        }

        return listaCobrancas;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCondominos = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tabelaCondominos = new javax.swing.JTable();
        painelCobrancaBase = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaCobrancasBase = new javax.swing.JTable();
        painelBoletos = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaCobrancas = new javax.swing.JTable();
        dateField1 = new net.sf.nachocalendar.components.DateField();
        jLabel1 = new javax.swing.JLabel();
        btnGerarBoleto = new javax.swing.JButton();

        setClosable(true);
        setTitle("Lançamentos");
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
            .addGroup(painelCondominosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelCondominosLayout.setVerticalGroup(
            painelCondominosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCondominosLayout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelCobrancaBase.setBorder(javax.swing.BorderFactory.createTitledBorder("Cobranças Base"));

        tabelaCobrancasBase.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelCobrancaBaseLayout.setVerticalGroup(
            painelCobrancaBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCobrancaBaseLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        painelBoletos.setBorder(javax.swing.BorderFactory.createTitledBorder("Boletos Gerados"));

        tabelaCobrancas.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelBoletosLayout.setVerticalGroup(
            painelBoletosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBoletosLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.setText("Vencimento");

        btnGerarBoleto.setText("Gerar Cobrança");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelCondominos, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelBoletos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelCobrancaBase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnGerarBoleto)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(painelCondominos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnGerarBoleto)
                            .addComponent(dateField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(painelCobrancaBase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(painelBoletos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGerarBoleto;
    private net.sf.nachocalendar.components.DateField dateField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel painelBoletos;
    private javax.swing.JPanel painelCobrancaBase;
    private javax.swing.JPanel painelCondominos;
    private javax.swing.JTable tabelaCobrancas;
    private javax.swing.JTable tabelaCobrancasBase;
    private javax.swing.JTable tabelaCondominos;
    // End of variables declaration//GEN-END:variables
}
