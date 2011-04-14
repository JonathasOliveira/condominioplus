/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaExtratoBancario.java
 *
 * Created on 01/04/2011, 10:15:31
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.Identificador;
import condominioPlus.negocio.financeiro.arquivoRetorno.EntradaExtratoDiário;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author eugenia
 */
public class TelaExtratoBancario extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TabelaModelo_2<ExtratoBancario> modeloTabelaExtratoDiario;
    private TabelaModelo_2<ExtratoBancario> modeloTabelaExtratoMensal;
    private TabelaModelo_2<Identificador> modeloTabelaIdentificadores;
    private List<ExtratoBancario> listaExtratoDiario;
    private List<ExtratoBancario> listaExtratoMensal;
    private List<Identificador> listaIdentificadores;

    /** Creates new form TelaExtratoBancario */
    public TelaExtratoBancario(Condominio condominio) {

        this.condominio = condominio;

        initComponents();
        new ControladorEventos();

        carregarTabelas();

        if (condominio != null) {
            this.setTitle("Extrato Bancário - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabelas() {
        carregarTabelaExtratoDiario();
        carregarTabelaExtratoMensal();
        carregarTabelaIdentificadores();
    }

    private void carregarTabelaExtratoDiario() {
        modeloTabelaExtratoDiario = new TabelaModelo_2<ExtratoBancario>(tabelaExtratoDiario, "Data, Histórico, Doc, Tipo, Valor".split(",")) {

            @Override
            protected List<ExtratoBancario> getCarregarObjetos() {
                return getExtratoDiario();
            }

            @Override
            public Object getValor(ExtratoBancario extratoBancario, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(extratoBancario.getDataPagamento());
                    case 1:
                        return extratoBancario.getIdentificador() != null ? extratoBancario.getIdentificador().getPalavraChave() : extratoBancario.getHistorico();
                    case 2:
                        return extratoBancario.getDoc();
                    case 3:
                        return extratoBancario.getTipo();
                    case 4:
                        return extratoBancario.getValor();
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(0)).setMaxWidth(80);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(2)).setMaxWidth(150);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(3)).setMaxWidth(40);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(4)).setMaxWidth(80);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(3)).setCellRenderer(centralizado);
    }

    private List<ExtratoBancario> getExtratoDiario() {
        listaExtratoDiario = new DAO().listar("ExtratosPorDia", condominio, DataUtil.getCalendar(DataUtil.hoje()));
        return listaExtratoDiario;
    }

    private void carregarTabelaExtratoMensal() {
        modeloTabelaExtratoMensal = new TabelaModelo_2<ExtratoBancario>(tabelaExtratoMensal, "Data, Histórico, Discrimação da Conta,Doc, Tipo, Valor".split(",")) {

            @Override
            protected List<ExtratoBancario> getCarregarObjetos() {
                return getExtratoMensal();
            }

            @Override
            public Object getValor(ExtratoBancario extratoBancario, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(extratoBancario.getDataPagamento());
                    case 1:
                        return extratoBancario.getIdentificador() != null ? extratoBancario.getIdentificador().getPalavraChave() : extratoBancario.getHistorico();
                    case 2:
                        return extratoBancario.getIdentificador() != null ? extratoBancario.getIdentificador().getConta().getNome() : "";
                    case 3:
                        return extratoBancario.getDoc();
                    case 4:
                        return extratoBancario.getTipo();
                    case 5:
                        return extratoBancario.getValor();
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(0)).setMaxWidth(80);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(2)).setMinWidth(120);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(3)).setMaxWidth(100);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(4)).setMaxWidth(40);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(5)).setMaxWidth(80);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(4)).setCellRenderer(centralizado);
    }


    private List<ExtratoBancario> getExtratoMensal() {
        Calendar dataInicial = DataUtil.getCalendar(DataUtil.getPrimeiroDiaMes());
        Calendar dataFinal = DataUtil.getCalendar(DataUtil.getUltimoDiaMes());
        listaExtratoMensal = new DAO().listar("ExtratosPorMês", condominio, dataInicial, dataFinal);
        return listaExtratoMensal;
    }

    private void carregarTabelaIdentificadores() {
        modeloTabelaIdentificadores = new TabelaModelo_2<Identificador>(tabelaIdentificadores, "Palavra Chave, Cód. Conta, Conta".split(",")) {

            @Override
            protected List<Identificador> getCarregarObjetos() {
                return getIdentificadores();
            }

            @Override
            public Object getValor(Identificador identificador, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return identificador.getPalavraChave();
                    case 1:
                        return identificador.getConta().getCodigo();
                    case 2:
                        return identificador.getConta().getNome();
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        esquerda.setHorizontalAlignment(SwingConstants.LEFT);

        tabelaIdentificadores.getColumn(modeloTabelaIdentificadores.getCampo(1)).setCellRenderer(esquerda);

        tabelaIdentificadores.getColumn(modeloTabelaIdentificadores.getCampo(0)).setMinWidth(200);
        tabelaIdentificadores.getColumn(modeloTabelaIdentificadores.getCampo(2)).setMinWidth(200);
    }

    private List<Identificador> getIdentificadores() {
        listaIdentificadores = new DAO().listar(Identificador.class);
        return listaIdentificadores;
    }

    private void lerArquivoExtrato() {
        FileDialog fileDialog = new FileDialog((Frame) null);
        fileDialog.setVisible(true);
        File diretorio = new File(fileDialog.getDirectory());
        new EntradaExtratoDiário().lerArquivo(diretorio.listFiles());
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();
            if (origem == btnLerArquivo) {
                lerArquivoExtrato();
            }
        }

        @Override
        public void configurar() {
            btnLerArquivo.addActionListener(this);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        painelExtratoDiario = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaExtratoDiario = new javax.swing.JTable();
        btnLerArquivo = new javax.swing.JButton();
        painelExtratoMensal = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaExtratoMensal = new javax.swing.JTable();
        painelIdentificadores = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaIdentificadores = new javax.swing.JTable();

        setClosable(true);
        setTitle("Extrato Bancário");

        tabelaExtratoDiario.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabelaExtratoDiario);

        btnLerArquivo.setText("Ler Arquivo");

        javax.swing.GroupLayout painelExtratoDiarioLayout = new javax.swing.GroupLayout(painelExtratoDiario);
        painelExtratoDiario.setLayout(painelExtratoDiarioLayout);
        painelExtratoDiarioLayout.setHorizontalGroup(
            painelExtratoDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoDiarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelExtratoDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .addComponent(btnLerArquivo))
                .addContainerGap())
        );
        painelExtratoDiarioLayout.setVerticalGroup(
            painelExtratoDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoDiarioLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(btnLerArquivo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Extrato Diário", painelExtratoDiario);

        tabelaExtratoMensal.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tabelaExtratoMensal);

        javax.swing.GroupLayout painelExtratoMensalLayout = new javax.swing.GroupLayout(painelExtratoMensal);
        painelExtratoMensal.setLayout(painelExtratoMensalLayout);
        painelExtratoMensalLayout.setHorizontalGroup(
            painelExtratoMensalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoMensalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelExtratoMensalLayout.setVerticalGroup(
            painelExtratoMensalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoMensalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Extrato Mensal", painelExtratoMensal);

        tabelaIdentificadores.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tabelaIdentificadores);

        javax.swing.GroupLayout painelIdentificadoresLayout = new javax.swing.GroupLayout(painelIdentificadores);
        painelIdentificadores.setLayout(painelIdentificadoresLayout);
        painelIdentificadoresLayout.setHorizontalGroup(
            painelIdentificadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelIdentificadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelIdentificadoresLayout.setVerticalGroup(
            painelIdentificadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelIdentificadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Identificadores", painelIdentificadores);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Extrato Diário");
        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLerArquivo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelExtratoDiario;
    private javax.swing.JPanel painelExtratoMensal;
    private javax.swing.JPanel painelIdentificadores;
    private javax.swing.JTable tabelaExtratoDiario;
    private javax.swing.JTable tabelaExtratoMensal;
    private javax.swing.JTable tabelaIdentificadores;
    // End of variables declaration//GEN-END:variables
}
