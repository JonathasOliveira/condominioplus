/*
 * TelaFuncionario.java
 *
 * Created on 17 de Agosto de 2007, 17:30
 */
package condominioPlus.apresentacao.condominio;

import condominioPlus.Main;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.apresentacao.financeiro.DialogoDadosCapa;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.CaretEvent;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.DadosTalaoCheque;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.NotificavelClasse;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;

/**
 *
 * @author Thiago
 */
public class TelaSelecionarCondominio extends javax.swing.JInternalFrame implements NotificavelClasse<Condominio> {

    private ControladorEventos controlador;
    private TabelaModelo_2<Condominio> modelo;
    private JDesktopPane desktop;
    private boolean selecionarVarios;

    /** Creates new form TelaFuncionario */
    public TelaSelecionarCondominio(JDesktopPane desktop, boolean selecionarVarios) {
        initComponents();

        this.desktop = desktop;
        this.selecionarVarios = selecionarVarios;
        controlador = new ControladorEventos();

        if (selecionarVarios) {
            this.setTitle("Imprimir Capa Prestação de Contas - Selecionar Condomínios");
            btnSelecionar.setText("Imprimir");
        }

        carregarTabela();
    }

    public void notificarClasse(Condominio condominio) {
        modelo.set(condominio);
    }

    private void sair() {
        if (!modelo.getObjetoSelecionado().equals(Main.getCondominio())) {
            for (JInternalFrame f : desktop.getAllFrames()) {
                f.dispose();
            }
        }
        doDefaultCloseAction();
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<Condominio>(tabela, "Nome, Síndico, Telefone".split(",")) {

            @Override
            protected List<Condominio> getCarregarObjetos() {
                return new DAO().listar(Condominio.class, "CondominioPorOrdemAlfabetica");
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
                        return condominio.getSindico();
                    case 2:
                        return condominio.getTelefones().size() > 0 ? condominio.getTelefones().get(0).getNumero() : "";
                    default:
                        return null;
                }
            }
        };

        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.getColumn(modelo.getCampo(0)).setPreferredWidth(280);
        tabela.getColumn(modelo.getCampo(1)).setPreferredWidth(200);
        tabela.getColumn(modelo.getCampo(2)).setPreferredWidth(130);

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

    private void selecionarCondominio() {
        if (modelo.getLinhaSelecionada() > -1) {
            Main.setCondominio(modelo.getObjetoSelecionado());
            TelaPrincipal.preencherCondominio(Main.getCondominio());
            sair();
            verificarNumeroTaloes();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Você precisa selecionar um condominio para usar o sistema!", this);

        }
    }

    private void selecionarVariosCondominios() {
        if (!modelo.getObjetosSelecionados().isEmpty()) {
            DialogoDadosCapa dialogo = new DialogoDadosCapa(null, false, modelo.getObjetosSelecionados());
            dialogo.setVisible(true);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Você precisa selecionar, ao menos, um condominio para prosseguir!", this);

        }
    }

    private void verificarNumeroTaloes() {
        List<DadosTalaoCheque> listaTaloes = new ArrayList<DadosTalaoCheque>();
        for (DadosTalaoCheque dados : Main.getCondominio().getDadosTalaoCheques()) {
            if (dados.isEmUso() || dados.isNovo()) {
                listaTaloes.add(dados);
            }
        }
        if (listaTaloes.size() < Main.getCondominio().getNumeroMinimoTaloes()) {
            ApresentacaoUtil.exibirAdvertencia("Atenção: O número de talões é menor que o desejado, providencie novos talões!", null);
        }
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            txtNome.addCaretListener(this);
            txtNome.addActionListener(this);
            tabela.addMouseListener(this);
            btnSelecionar.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source = e.getSource();
            if (source == txtNome) {
                if (modelo.size() > 0) {
                    tabela.grabFocus();
                    tabela.changeSelection(0, 0, false, false);
                } else {
                    modelo.editar(new Condominio(txtNome.getText()));
                }
            } else if (source == btnSelecionar) {
                if (selecionarVarios) {
                    selecionarVariosCondominios();
                } else {
                    selecionarCondominio();
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
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnSelecionar = new javax.swing.JButton();

        setClosable(true);
        setTitle("Selecionar Condomínio");
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

        jLabel1.setText("Nome");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 4);
        jPanel1.add(jLabel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        btnSelecionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/ok.GIF"))); // NOI18N
        btnSelecionar.setText("Selecionar");
        btnSelecionar.setToolTipText("Selecione um condomínio!");
        jPanel2.add(btnSelecionar, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSelecionar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}
