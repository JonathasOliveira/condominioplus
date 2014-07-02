/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoConta.java
 *
 * Created on 07/10/2010, 14:33:06
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.financeiro.Conta;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
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
public class DialogoConta extends javax.swing.JDialog {

    private TabelaModelo_2<Conta> modelo;
    private Conta conta;
    private boolean credito;
    private boolean selecaoNula;
    private String nomeVinculo;

    /** Creates new form DialogoConta */
    public DialogoConta(java.awt.Frame parent, boolean modal, boolean credito, boolean selecaoNula, String nomeVinculo) {
        super(parent, modal);
        initComponents();
        new ControladorEventos();
        this.setLocationRelativeTo(null);
        this.credito = credito;
        this.selecaoNula = selecaoNula;
        this.nomeVinculo = nomeVinculo;
        carregarTipoBusca();
        carregarTabela();
    }

    private void carregarTipoBusca() {
        cbTipo.setModel(new ComboModelo<String>(Util.toList(new String[]{"Código", "Nome"})));
        cbTipo.setSelectedItem("Nome");
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<Conta>(tabela, "Código, Nome, Crédito".split(",")) {

            @Override
            protected List<Conta> getCarregarObjetos() {
                return getContas();
            }

            @Override
            protected List<Conta> getFiltrar(List<Conta> contas) {
                if (cbTipo.getSelectedItem().equals("Código")) {
                    return filtrarListaPorCodigo(txtNome.getText(), contas);
                } else {
                    return filtrarListaPorNome(txtNome.getText(), contas);
                }
            }

            @Override
            public Object getValor(Conta conta, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return conta.getCodigo();
                    case 1:
                        return conta.getNome();
                    case 2:
                        return conta.isCredito();
                    default:
                        return null;
                }
            }
        };
        
        tabela.getColumn(modelo.getCampo(0)).setMaxWidth(50);
        tabela.getColumn(modelo.getCampo(1)).setMaxWidth(400);
        tabela.getColumn(modelo.getCampo(2)).setMaxWidth(55);

//        modelo.setLargura(1, 200, 200, -1);

    }

    private List<Conta> filtrarListaPorNome(String sequencia, List<Conta> contas) {
        ArrayList<Conta> listaFiltrada = new ArrayList<Conta>();

        if (sequencia.equals("")) {
            return contas;
        }

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        CONTAS:
        for (Conta c : contas) {
            for (String s : sequencias) {
                if (!c.getNome().toUpperCase().contains(s)) {
                    continue CONTAS;
                }
            }

            listaFiltrada.add(c);
        }

        return listaFiltrada;
    }

    private List<Conta> filtrarListaPorCodigo(String sequencia, List<Conta> contas) {
        ArrayList<Conta> listaFiltrada = new ArrayList<Conta>();

        if (sequencia.equals("")) {
            return contas;
        }

        CONTAS:
        for (Conta c : contas) {
            if (c.getCodigo() != Integer.parseInt(sequencia)) {
                continue CONTAS;
            }
            listaFiltrada.add(c);
        }

        return listaFiltrada;
    }

    private void selecionarConta() {
        if (modelo.getLinhaSelecionada() > -1) {
            conta = modelo.getObjetoSelecionado();
            sair();
        } else {
            if (!selecaoNula) {
                ApresentacaoUtil.exibirAdvertencia("Você precisa selecionar uma conta!", this);
            } else {
                sair();
            }
        }
    }

    public Conta getConta() {
        return conta;
    }

    private void sair() {
        dispose();
    }

    private List<Conta> getContas() {
        if (credito && nomeVinculo.equals("T")) {
            return new DAO().listar(Conta.class);
        } else if (nomeVinculo.equals("")) {
            return new DAO().listar("ListarContasTipo", credito);
        } else {
            return new DAO().listar("ListarContasVinculo", nomeVinculo);
        }
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
            tabela.addMouseListener(this);
//            btnPesquisar.addActionListener(this);
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
                selecionarConta();
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
        
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == tabela && e.getClickCount() >= 2) {
                selecionarConta();
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

        tabela.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
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
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNome, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnOk)
                        .addGap(18, 18, 18)
                        .addComponent(btnCancelar)
                        .addGap(162, 162, 162))))
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
