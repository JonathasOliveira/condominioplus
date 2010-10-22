/*
 * TelaControleAcesso.java
 *
 * Created on 4 de Outubro de 2007, 09:59
 */
package condominioPlus.apresentacao.funcionario;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.event.ChangeEvent;
import condominioPlus.negocio.funcionario.ControleAcesso;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.negocio.funcionario.Funcionario;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.RenderizadorCelulaCentralizada;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.TabelaModelo;
import logicpoint.util.Util;

/**
 *
 * @author Thiago
 */
public class TelaControleAcesso extends javax.swing.JInternalFrame {

    private List<ControleAcesso> acessos;

    /** Creates new form TelaControleAcesso */
    public TelaControleAcesso() {
        initComponents();

        ApresentacaoUtil.setarMes(datInicio, datTermino);

        new ControladorEventos();

        try {
            carregarComboFuncionario();
            carregarComboTipoAcesso();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
        carregarTabela();
    }

    private void carregarComboFuncionario() {
        cmbFuncionario.setModel(new ComboModelo<Funcionario>(new DAO().listar(Funcionario.class)));
        getModeloFuncionario().adicionar(null, 0);
    }

    private void carregarComboTipoAcesso() {
        cmbTipoAcesso.setModel(new ComboModelo<TipoAcesso>(Util.toList(TipoAcesso.values())));
        getModeloTipoAcesso().adicionar(null, 0);
    }

    private void carregarListaPorPeriodo() {
        Calendar inicio = Calendar.getInstance();
        inicio.setTime((Date) datInicio.getValue());
        Calendar termino = Calendar.getInstance();
        termino.setTime((Date) datTermino.getValue());

        try {
            acessos = new DAO().listar(ControleAcesso.class, "ControleAcessoPorPeriodo", inicio, termino);
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void carregarTabela() {
        if (acessos == null) {
            carregarListaPorPeriodo();
        }

        List<ControleAcesso> listaFiltrada = aplicarFiltro(acessos);

        if (tabela.getModel() instanceof TabelaModelo) {
            getModeloTabela().setObjetos(listaFiltrada);
        } else {
            String[] colunas = "Data, Funcionário, Tipo de Acesso, Descrição".split(", ");

            tabela.setModel(new TabelaModelo<ControleAcesso>(listaFiltrada, colunas, true, ControleAcesso.class, tabela) {

                public Object getCampo(ControleAcesso acesso, int indiceColuna) {
                    switch (indiceColuna) {
                        case 0:
                            return DataUtil.toString(acesso.getData(), "dd/MM/yy 'às' HH:mm:ss");
                        case 1:
                            return acesso.getFuncionario();
                        case 2:
                            return acesso.getTipo().toString();
                        case 3:
                            return acesso.getDescricao() != null ? acesso.getDescricao() : "";
                        default:
                            return null;
                    }
                }
            });

            tabela.getColumn(colunas[0]).setMinWidth(130);
            tabela.getColumn(colunas[0]).setMaxWidth(130);
            tabela.getColumn(colunas[0]).setCellRenderer(new RenderizadorCelulaCentralizada());
            tabela.getColumn(colunas[1]).setMinWidth(130);
            tabela.getColumn(colunas[1]).setMaxWidth(130);
            tabela.getColumn(colunas[2]).setMinWidth(130);
            tabela.getColumn(colunas[2]).setMaxWidth(130);
            tabela.getColumn(colunas[3]).setMinWidth(130);
        }
    }

    private List<ControleAcesso> aplicarFiltro(List<ControleAcesso> lista) {
        List<ControleAcesso> listaFiltrada = new ArrayList(lista);

        Funcionario funcionario = getModeloFuncionario().getSelectedItem();
        TipoAcesso tipo = getModeloTipoAcesso().getSelectedItem();

        for (ControleAcesso c : lista) {
            if (funcionario != null) {
                if (!c.getFuncionario().equals(funcionario)) {
                    listaFiltrada.remove(c);
                }
            }
            if (tipo != null) {
                if (!c.getTipo().equals(tipo)) {
                    listaFiltrada.remove(c);
                }
            }
        }

        return listaFiltrada;
    }

    private ComboModelo<Funcionario> getModeloFuncionario() {
        return (ComboModelo<Funcionario>) cmbFuncionario.getModel();
    }

    private ComboModelo<TipoAcesso> getModeloTipoAcesso() {
        return (ComboModelo<TipoAcesso>) cmbTipoAcesso.getModel();
    }

    private TabelaModelo<ControleAcesso> getModeloTabela() {
        return (TabelaModelo<ControleAcesso>) tabela.getModel();
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            datInicio.addChangeListener(this);
            datTermino.addChangeListener(this);

            cmbFuncionario.addItemListener(this);
            cmbTipoAcesso.addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getSource() == cmbFuncionario || e.getSource() == cmbTipoAcesso) {
                carregarTabela();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            source = e.getSource();
            if (source == datInicio || source == datTermino) {
                ApresentacaoUtil.verificarDatas(source, datInicio, datTermino, this);
                carregarListaPorPeriodo();
                carregarTabela();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        datInicio = new net.sf.nachocalendar.components.DateField();
        jLabel3 = new javax.swing.JLabel();
        datTermino = new net.sf.nachocalendar.components.DateField();
        jLabel4 = new javax.swing.JLabel();
        cmbTipoAcesso = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cmbFuncionario = new javax.swing.JComboBox();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Controle de Acesso");
        setVisible(true);

        jScrollPane1.setViewportView(tabela);

        jLabel1.setText("Período:");

        jLabel2.setText("Início");

        jLabel3.setText("Término");

        jLabel4.setText("Tipo de Acesso");

        jLabel5.setText("Funcionário");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(datInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datTermino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbTipoAcesso, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(4, 4, 4))
                    .addComponent(datInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datTermino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cmbFuncionario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(cmbTipoAcesso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbFuncionario;
    private javax.swing.JComboBox cmbTipoAcesso;
    private net.sf.nachocalendar.components.DateField datInicio;
    private net.sf.nachocalendar.components.DateField datTermino;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    // End of variables declaration//GEN-END:variables
}
