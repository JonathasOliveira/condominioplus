/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoFornecedor.java
 *
 * Created on 01/07/2014, 16:16:06
 */
package condominioPlus.apresentacao.fornecedor;

import condominioPlus.negocio.fornecedor.Fornecedor;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.CaretEvent;
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
public class DialogoFornecedor extends javax.swing.JDialog {

    private TabelaModelo_2<Fornecedor> modelo;
    private Fornecedor fornecedor;
    private boolean selecaoNula;

    /** Creates new form DialogoConta */
    public DialogoFornecedor(java.awt.Frame parent, boolean modal, boolean selecaoNula) {
        super(parent, modal);
        initComponents();
        new ControladorEventos();
        this.setLocationRelativeTo(null);
        this.selecaoNula = selecaoNula;
        carregarTipoBusca();
        carregarTabela();
    }

    private void carregarTipoBusca() {
        cbTipo.setModel(new ComboModelo<String>(Util.toList(new String[]{"Código", "Nome"})));
        cbTipo.setSelectedItem("Nome");
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<Fornecedor>(tabela, "Código, Nome".split(",")) {

            @Override
            protected List<Fornecedor> getCarregarObjetos() {
                return getFornecedores();
            }

            @Override
            protected List<Fornecedor> getFiltrar(List<Fornecedor> fornecedores) {
                if (cbTipo.getSelectedItem().equals("Código")) {
                    return filtrarListaPorCodigo(txtNome.getText(), fornecedores);
                } else {
                    return filtrarListaPorNome(txtNome.getText(), fornecedores);
                }
            }

            @Override
            public Object getValor(Fornecedor fornecedor, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return fornecedor.getCodigo();
                    case 1:
                        return fornecedor.getNome();
                    default:
                        return null;
                }
            }
        };

        tabela.getColumn(modelo.getCampo(0)).setMinWidth(50);
        tabela.getColumn(modelo.getCampo(1)).setMinWidth(400);

//        modelo.setLargura(1, 200, 200, -1);

    }

    private List<Fornecedor> filtrarListaPorNome(String sequencia, List<Fornecedor> fornecedores) {
        ArrayList<Fornecedor> listaFiltrada = new ArrayList<Fornecedor>();

        if (sequencia.equals("")) {
            return fornecedores;
        }

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        FORNECEDORES:
        for (Fornecedor f : fornecedores) {
            for (String s : sequencias) {
                if (!f.getNome().toUpperCase().contains(s)) {
                    continue FORNECEDORES;
                }
            }

            listaFiltrada.add(f);
        }

        return listaFiltrada;
    }

    private List<Fornecedor> filtrarListaPorCodigo(String sequencia, List<Fornecedor> fornecedores) {
        ArrayList<Fornecedor> listaFiltrada = new ArrayList<Fornecedor>();

        if (sequencia.equals("")) {
            return fornecedores;
        }

        FORNECEDORES:
        for (Fornecedor f : fornecedores) {
            if (f.getCodigo() != Integer.parseInt(sequencia)) {
                continue FORNECEDORES;
            }
            listaFiltrada.add(f);
        }

        return listaFiltrada;
    }

    private void selecionarFornecedor() {
        if (modelo.getLinhaSelecionada() > -1) {
            fornecedor = modelo.getObjetoSelecionado();
            sair();
        } else {
            if (!selecaoNula) {
                ApresentacaoUtil.exibirAdvertencia("Você precisa selecionar um fornecedor!", this);
            } else {
                sair();
            }
        }
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    private void sair() {
        dispose();
    }

    private List<Fornecedor> getFornecedores() {
        return new DAO().listar(Fornecedor.class);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            txtNome.addCaretListener(this);
            txtNome.addActionListener(this);
            tabela.addMouseListener(this);
            btnOk.addActionListener(this);
            btnCancelar.addActionListener(this);
            btnCancelar.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == txtNome) {
                if (modelo.size() > 0) {
                    tabela.grabFocus();
                    tabela.changeSelection(0, 0, false, false);
                }
            } else if (source == btnOk) {
                selecionarFornecedor();
            } else if (source == btnCancelar) {
                sair();
            }
            source = null;
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            if (e.getSource() == txtNome) {
                modelo.filtrar();
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

        txtNome = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        cbTipo = new javax.swing.JComboBox();
        btnOk = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Contas");
        setAlwaysOnTop(true);

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabela);

        btnOk.setText("OK");

        btnCancelar.setText("Cancelar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNome, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(btnOk)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancelar)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnCancelar, btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancelar))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName("Fornecedores");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox cbTipo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}
