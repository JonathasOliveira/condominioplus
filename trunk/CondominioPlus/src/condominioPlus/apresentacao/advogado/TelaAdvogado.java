/*
 * TelaFuncionario.java
 *
 * Created on 17 de Agosto de 2007, 17:30
 */
package condominioPlus.apresentacao.advogado;

import condominioPlus.negocio.funcionario.FuncionarioUtil;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.CaretEvent;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Advogado;
import condominioPlus.util.Relatorios;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.NotificavelClasse;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;

/**
 *
 * @author Thiago
 */
public class TelaAdvogado extends javax.swing.JInternalFrame implements NotificavelClasse<Advogado> {

    private ControladorEventos controlador;
    private TabelaModelo_2<Advogado> modelo;

    /** Creates new form TelaFuncionario */
    public TelaAdvogado() {
        initComponents();

        controlador = new ControladorEventos();

        carregarTabela();
    }

    public void notificarClasse(Advogado advogado) {
        modelo.set(advogado);
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<Advogado>(tabela, "Ordem, Nome, Telefone".split(",")) {

            @Override
            protected Advogado getAdicionar() {
                editar(new Advogado());
                return null;
            }

            @Override
            public void editar(Advogado advogado) {
              TelaPrincipal.getInstancia().criarFrame(new TelaDadosAdvogado(advogado));
            }

            @Override
            protected List<Advogado> getCarregarObjetos() {
                return new DAO().listar(Advogado.class);
            }

            @Override
            protected List<Advogado> getFiltrar(List<Advogado> advogados) {
                return filtrarListaPorNome(txtNome.getText(), advogados);
            }

            @Override
            public Object getValor(Advogado advogado, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return advogado.getNumero_ordem();
                    case 1:
                        return advogado.getNome();
                    case 2:
                        return advogado.getTelefones().size() > 0 ? advogado.getTelefones().get(0).getNumero() : "";
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Advogado advogado) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Advogado - " + advogado.getNome() + " ?", TelaAdvogado.this)) {
                    return false;
                }

                try {
                    new DAO().remover(advogado);
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Advogado - " + advogado.getNome());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaAdvogado.this);
                    return false;
                }
            }
        };

        modelo.setLargura(1, 200, 200, -1);

        btnAdicionar.addActionListener(modelo.listenerAdicao);
        btnEditar.addActionListener(modelo.listenerEdicao);
        btnRemover.addActionListener(modelo.listenerRemocao);
    }

    private List<Advogado> filtrarListaPorNome(String sequencia, List<Advogado> advogados) {
        ArrayList<Advogado> listaFiltrada = new ArrayList<Advogado>();

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        ADVOGADOS:
        for (Advogado a : advogados) {
            for (String s : sequencias) {
                if (!a.getNome().toUpperCase().contains(s)) {
                    continue ADVOGADOS;
                }
            }

            listaFiltrada.add(a);
        }

        return listaFiltrada;
    }

    private void imprimir() {

        HashMap parametrosRelatorio = new HashMap();

        List<HashMap> lista = new ArrayList<HashMap>();
        for (Advogado a : modelo.getObjetos()) {
            HashMap<String, String> objeto = new HashMap<String, String>();
            objeto.put("nome", a.getNome());
            objeto.put("logradouro", a.getEndereco().getLogradouro());
            objeto.put("numero", a.getEndereco().getNumero());
            objeto.put("complemento", a.getEndereco().getComplemento());
            objeto.put("bairro", a.getEndereco().getBairro());
            objeto.put("cidade", a.getEndereco().getCidade());
            objeto.put("cep", a.getEndereco().getCep());
            objeto.put("estado", a.getEndereco().getEstado());
            objeto.put("referencia", a.getEndereco().getReferencia());

            lista.add(objeto);
        }

        new Relatorios().imprimir("RelatorioFuncionarios", parametrosRelatorio, lista, false, true);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnImprimir.addActionListener(this);
            txtNome.addCaretListener(this);
            txtNome.addActionListener(this);
            tabela.addMouseListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == btnImprimir) {
                imprimir();
            }
            if (source == txtNome) {
                if (modelo.size() > 0) {
                    tabela.grabFocus();
                    tabela.changeSelection(0, 0, false, false);
                } else {
                    modelo.editar(new Advogado(txtNome.getText()));
                }
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        txtNome = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Advogados");
        setVisible(true);

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabela);

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(txtNome, gridBagConstraints);

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionar.setToolTipText("Adicionar");
        btnAdicionar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionar.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(btnAdicionar, gridBagConstraints);

        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditar.setToolTipText("Editar");
        btnEditar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditar.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(btnEditar, gridBagConstraints);

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir");
        btnImprimir.setMaximumSize(new java.awt.Dimension(32, 32));
        btnImprimir.setMinimumSize(new java.awt.Dimension(32, 32));
        btnImprimir.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(btnImprimir, gridBagConstraints);

        jLabel1.setText("Nome");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 4);
        jPanel1.add(jLabel1, gridBagConstraints);

        btnRemover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/lixeira.gif"))); // NOI18N
        btnRemover.setToolTipText("Excluir");
        btnRemover.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemover.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemover.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(btnRemover, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnRemover;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}
