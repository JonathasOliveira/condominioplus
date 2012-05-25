/*
 * TelaFuncionario.java
 *
 * Created on 17 de Agosto de 2007, 17:30
 */
package condominioPlus.apresentacao.condominio;

import condominioPlus.negocio.funcionario.FuncionarioUtil;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.CaretEvent;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Condominio;
import condominioPlus.util.Relatorios;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.NotificavelClasse;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author Thiago
 */
public class TelaCondominio extends javax.swing.JInternalFrame implements NotificavelClasse<Condominio> {

    private ControladorEventos controlador;
    private TabelaModelo_2<Condominio> modelo;

    /** Creates new form TelaFuncionario */
    public TelaCondominio() {
        initComponents();

        controlador = new ControladorEventos();

        carregarTabela();
    }

    public void notificarClasse(Condominio condominio) {
        modelo.set(condominio);
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<Condominio>(tabela, "Nome, CNPJ, Síndico, Telefone".split(",")) {

            @Override
            protected Condominio getAdicionar() {
                editar(new Condominio());
                return null;
            }

            @Override
            public void editar(Condominio condominio) {
              TelaPrincipal.getInstancia().criarFrame(new TelaDadosCondominio(condominio));
            }

            @Override
            protected List<Condominio> getCarregarObjetos() {
                return new DAO().listar(Condominio.class);
            }

            @Override
            protected List<Condominio> getFiltrar(List<Condominio> condominios) {
                return filtrarListaPorNome(txtNome.getText(), condominios);
            }

            @Override
            public Object getValor(Condominio condominio, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return condominio.getRazaoSocial();
                    case 1:
                        return condominio.getCnpj();
                    case 2:
                        return condominio.getSindico();
                    case 3:
                        return condominio.getTelefones().size() > 0 ? condominio.getTelefones().get(0).getNumero() : "";
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Condominio condominio) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Condomínio - " + condominio.getRazaoSocial() + " ?", TelaCondominio.this)) {
                    return false;
                }

                try {
                    new DAO().remover(condominio);
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Condomínio - " + condominio.getRazaoSocial());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaCondominio.this);
                    return false;
                }
            }
        };

        modelo.setLargura(1, 200, 200, -1);

        btnAdicionar.addActionListener(modelo.listenerAdicao);
        btnEditar.addActionListener(modelo.listenerEdicao);
        btnRemover.addActionListener(modelo.listenerRemocao);
    }

    private List<Condominio> filtrarListaPorNome(String sequencia, List<Condominio> condominios) {
        ArrayList<Condominio> listaFiltrada = new ArrayList<Condominio>();

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        CONDOMINIOS:
        for (Condominio c : condominios) {
            for (String s : sequencias) {
                if (!c.getRazaoSocial().toUpperCase().contains(s)) {
                    continue CONDOMINIOS;
                }
            }

            listaFiltrada.add(c);
        }

        return listaFiltrada;
    }

    private void imprimir() {

        HashMap parametrosRelatorio = new HashMap();

        List<HashMap> lista = new ArrayList<HashMap>();
        for (Condominio c : modelo.getObjetos()) {
            HashMap<String, String> objeto = new HashMap<String, String>();
            objeto.put("nome", c.getRazaoSocial());
            objeto.put("cpf", c.getCnpj());
            objeto.put("dataNascimento", DataUtil.toString(c.getDataCadastro()));
            objeto.put("logradouro", c.getEndereco().getLogradouro());
            objeto.put("numero", c.getEndereco().getNumero());
            objeto.put("complemento", c.getEndereco().getComplemento());
            objeto.put("bairro", c.getEndereco().getBairro());
            objeto.put("cidade", c.getEndereco().getCidade());
            objeto.put("cep", c.getEndereco().getCep());
            objeto.put("estado", c.getEndereco().getEstado());
            objeto.put("referencia", c.getEndereco().getReferencia());

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
                    modelo.editar(new Condominio(txtNome.getText()));
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
        setTitle("Condomínios");
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
