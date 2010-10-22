/*
 * TelaCategoriaFuncionario.java
 *
 * Created on 17 de Agosto de 2007, 17:35
 */
package condominioPlus.apresentacao.funcionario;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.funcionario.CategoriaFuncionario;
import condominioPlus.negocio.funcionario.Funcionario;
import condominioPlus.negocio.funcionario.Caracteristica;
import condominioPlus.negocio.funcionario.CaracteristicaAcesso;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ListaModelo;
import logicpoint.util.TabelaModelo;

/**
 *
 * @author  USUARIO
 */
public class TelaCategoriaFuncionario extends javax.swing.JInternalFrame {

    private List<PermissaoSelecionada> permissoes = new ArrayList<PermissaoSelecionada>();
    private List<CategoriaFuncionario> categorias = new ArrayList<CategoriaFuncionario>();
    private List<CategoriaFuncionario> removidos = new ArrayList<CategoriaFuncionario>();
    private ControladorEventos controlador = new ControladorEventos();
    private boolean modificado;

    /** Creates new form TelaCategoriaFuncionario */
    public TelaCategoriaFuncionario() {
        initComponents();
        adicionarOuvintes();
        for (CaracteristicaAcesso permissao : CaracteristicaAcesso.values()) {
            permissoes.add(new PermissaoSelecionada(permissao, false));
        }
        carregarTabela();
        carregarLista();
    }

    private void carregarLista() {
        try {
            categorias = new DAO().listar(CategoriaFuncionario.class);
            lista.setModel(new ListaModelo<CategoriaFuncionario>(categorias, true, CategoriaFuncionario.class));
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void carregarTabela() {
        String[] colunas = " , Características".split(", ");
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setModel(new TabelaModelo<PermissaoSelecionada>(permissoes, colunas, false, PermissaoSelecionada.class) {

            @Override
            public Object getCampo(PermissaoSelecionada permissao, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return permissao.getSelecionada();
                    case 1:
                        return permissao.getCaracteristicaAcesso().toString();
                }
                return null;
            }

            @Override
            public boolean isCellEditable(int indiceLinha, int indiceColuna) {
                return indiceColuna == 0 ? true : false;
            }

            @Override
            public void setValueAt(Object valor, int indiceLinha, int indiceColuna) {
                int indice = lista.getSelectedIndex();
                if (indice < 0) {
                    return;
                }
                CategoriaFuncionario categoria = categorias.get(indice);
                if ((Boolean) valor) {
                    categoria.getCaracteristicas().add(new Caracteristica(permissoes.get(indiceLinha).getCaracteristicaAcesso()));
                    permissoes.get(indiceLinha).setSelecionada(true);
                } else {
                    categoria.getCaracteristicas().remove(new Caracteristica(permissoes.get(indiceLinha).getCaracteristicaAcesso()));
                    permissoes.get(indiceLinha).setSelecionada(false);
                }
                modificado = true;
                notificarCampo(indiceLinha, indiceColuna);
            }
        });
        tabela.getColumn(colunas[0]).setMaxWidth(50);
        tabela.getColumn(colunas[1]).setMinWidth(80);
    }

    private boolean isCategoriaExistente(String nome) {
        for (CategoriaFuncionario categoria : categorias) {
            if (categoria.getNome().equalsIgnoreCase(nome)) {
                return true;
            }
        }
        return false;
    }

    private void adicionarOuvintes() {
        btnAdicionar.addActionListener(controlador);
        btnRemover.addActionListener(controlador);
        btnFechar.addActionListener(controlador);
        btnSalvar.addActionListener(controlador);
        txtNome.addActionListener(controlador);
        lista.addListSelectionListener(controlador);
    }

    @Override
    public void doDefaultCloseAction() {
        if (modificado) {
            int opcao = JOptionPane.showOptionDialog(this, "Existem alterações que não foram salvas!", "Fechar", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, "Salvar, Ignorar, Cancelar".split(", "), null);
            if (opcao == JOptionPane.YES_OPTION) {
                salvar();
            } else if (opcao == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        super.doDefaultCloseAction();
    }

    private void salvar() {
        try {
            DAO dao = new DAO(false);
            dao.remover(removidos);
            dao.salvar(categorias);
            dao.concluirTransacao();

            modificado = false;

            TelaPrincipal.getInstancia().recarregarFuncionario();

            super.doDefaultCloseAction();
        } catch (Throwable t) {
            new TratadorExcecao(t, this);
        }
    }

    private void atualizarTabela() {
        int indice = lista.getSelectedIndex();
        desmarcarTabela();
        if (indice < 0) {
            return;
        }
        CategoriaFuncionario categoria = categorias.get(indice);
        for (PermissaoSelecionada permissao : permissoes) {
            for (Caracteristica p : categoria.getCaracteristicas()) {
                if (permissao.getCaracteristicaAcesso().equals(p.getCaracteristicaAcesso())) {
                    permissao.setSelecionada(true);
                }
            }
        }
        getTabelaModelo().fireTableDataChanged();
    }

    private void desmarcarTabela() {
        for (PermissaoSelecionada permissao : permissoes) {
            permissao.setSelecionada(false);
        }
    }

    @SuppressWarnings(value = "unchecked")
    private ListaModelo<CategoriaFuncionario> getListaModelo() {
        return (ListaModelo<CategoriaFuncionario>) lista.getModel();
    }

    private TabelaModelo getTabelaModelo() {
        return (TabelaModelo) tabela.getModel();
    }

    private void adicionar() {
        if (!txtNome.getText().equals("") && !isCategoriaExistente(txtNome.getText())) {
            getListaModelo().adicionar(new CategoriaFuncionario(txtNome.getText()));
            txtNome.setText("");
            lista.setSelectedIndex(lista.getModel().getSize() - 1);
            modificado = true;
        }
    }

    private void remover() {
        if (lista.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione a categoria que deseja remover", "Categoria Funcionário", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CategoriaFuncionario categoria = getListaModelo().getObjeto(lista.getSelectedIndex());
        if (categoria.getId() != 0) {
            List<Funcionario> funcionarios = new DAO().listar(Funcionario.class);
            for (Funcionario funcionario : funcionarios) {
                if (funcionario.getCategoria() != null && funcionario.getCategoria().equals(categoria)) {
                    JOptionPane.showMessageDialog(this, "Existem funcionários relacionados a esta categoria!", "Categoria Funcionário", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (categoria.getId() != 0) {
                removidos.add(categoria);
            }
        }
        getListaModelo().remover(categoria);
        desmarcarTabela();
        getTabelaModelo().fireTableDataChanged();
        modificado = true;
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        @SuppressWarnings(value = "unchecked")
        public void actionPerformed(ActionEvent e) {
            Object s = e.getSource();
            if (s == btnAdicionar) {
                adicionar();
            } else if (s == btnRemover) {
                remover();
            } else if (s == btnFechar) {
                TelaCategoriaFuncionario.this.doDefaultCloseAction();
            } else if (s == btnSalvar) {
                salvar();
            } else if (s == txtNome) {
                adicionar();
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            atualizarTabela();
        }
    }

    private class PermissaoSelecionada extends Caracteristica {

        private boolean selecionada;

        public PermissaoSelecionada(CaracteristicaAcesso permissao, boolean selecionada) {
            super(permissao);
            this.selecionada = selecionada;
        }

        public boolean getSelecionada() {
            return selecionada;
        }

        public void setSelecionada(boolean selecionada) {
            this.selecionada = selecionada;
        }

        @Override
        public String toString() {
            return super.toString() + "; " + selecionada;
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

        jPanel1 = new javax.swing.JPanel();
        txtNome = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        lista = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        btnRemover = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Categorias de Funcionários");
        setVisible(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), " Adicionar / Remover Categorias ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jPanel1.setName("painelAdicionarCategoria"); // NOI18N

        txtNome.setName("nome"); // NOI18N

        btnAdicionar.setText("Adicionar");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtNome, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnAdicionar)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("painelCentral"); // NOI18N

        jScrollPane1.setViewportView(tabela);

        jScrollPane2.setViewportView(lista);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        btnRemover.setText("Remover");
        btnRemover.setPreferredSize(new java.awt.Dimension(85, 26));
        jPanel4.add(btnRemover, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new java.awt.GridBagLayout());

        btnSalvar.setText("Salvar");
        btnSalvar.setPreferredSize(new java.awt.Dimension(85, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(btnSalvar, gridBagConstraints);

        btnFechar.setText("Fechar");
        btnFechar.setPreferredSize(new java.awt.Dimension(85, 26));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel3.add(btnFechar, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnRemover;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lista;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}
