/*
 * TelaFuncionario.java
 *
 * Created on 17 de Agosto de 2007, 17:30
 */
package condominioPlus.apresentacao.financeiro;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.CaretEvent;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.financeiro.Conta;
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
public class TelaConta extends javax.swing.JInternalFrame implements NotificavelClasse<Conta> {

    private ControladorEventos controlador;
    private TabelaModelo_2<Conta> modelo;
    private Conta conta;
    private List<Conta> listaContas;

    /** Creates new form TelaFuncionario */
    public TelaConta() {
        initComponents();

        controlador = new ControladorEventos();

        carregarTabela();



    }

    public void notificarClasse(Conta conta) {
        modelo.set(conta);
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<Conta>(tabela, "Código, Nome, Vinculada, Crédito".split(",")) {

            @Override
            protected Conta getAdicionar() {
                editar(new Conta());
                return null;
            }

            @Override
            public void editar(Conta conta) {
                TelaPrincipal.getInstancia().criarFrame(new TelaDadosConta(conta, modelo));
            }

            @Override
            protected List<Conta> getCarregarObjetos() {
                listaContas = carregarContas();
                return listaContas;
            }

            @Override
            protected List<Conta> getFiltrar(List<Conta> contas) {
                if(radioCodigo.isSelected()){
                    return filtrarListaPorCodigo(txtNome.getText(), contas);
                }
                return filtrarListaPorNome(txtNome.getText(), contas);
            }

            @Override
            public Object getValor(Conta conta, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return conta.getCodigo();
                    case 1:
                        return conta.getNome();
                    case 2:
                        return conta.getNomeVinculo();
                    case 3:
                        return conta.isCredito() ? "Crédito" : "Débito";
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Conta conta) {
                try {
                    if (conta.getContaVinculada() == null) {
                        if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir a conta " + conta.getNome() + " ?", TelaConta.this)) {
                            return false;
                        }
                        new DAO().remover(conta);
                        ApresentacaoUtil.exibirInformacao("Conta removida com sucesso!", tabela);
                        return true;
                    } else {
                        if (ApresentacaoUtil.perguntar("Deseja excluir a conta  " + conta.getNome() + " e a conta associada " + conta.getContaVinculada().getNome() + " ?", TelaConta.this)) {

                            Conta contaVinculada = conta.getContaVinculada();
                            conta.setContaVinculada(null);
                            contaVinculada.setContaVinculada(null);
                            new DAO().salvar(conta);
                            new DAO().salvar(contaVinculada);
                            modelo.remover(contaVinculada);
                            new DAO().remover(contaVinculada);
                            new DAO().remover(conta);
                            return true;

                        } else {
                            if (ApresentacaoUtil.perguntar("Deseja excluir somente a conta " + conta.getNome() + " ?", TelaConta.this)) {
                                Conta contaVinculada = conta.getContaVinculada();
                                conta.setContaVinculada(null);
                                contaVinculada.setContaVinculada(null);
                                new DAO().salvar(conta);
                                new DAO().salvar(contaVinculada);
                                new DAO().remover(conta);
                                ApresentacaoUtil.exibirInformacao("Conta removida com sucesso!", tabela);
                                return true;
                            }


                        }
                    }

                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaConta.this);
                    return false;
                }
                return false;
            }
        };

        modelo.setLargura(1, 200, 200, -1);

        btnAdicionar.addActionListener(modelo.listenerAdicao);
        btnEditar.addActionListener(modelo.listenerEdicao);
        btnRemover.addActionListener(modelo.listenerRemocao);
    }

    private List<Conta> filtrarListaPorNome(String sequencia, List<Conta> contas) {
        ArrayList<Conta> listaFiltrada = new ArrayList<Conta>();

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

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        CONTAS:
        for (Conta c : contas) {
            for (String s : sequencias) {
                String codigo = String.valueOf(c.getCodigo());
//                System.out.println(codigo);
                if (!codigo.toUpperCase().contains(s)) {
                    continue CONTAS;
                }
            }

            listaFiltrada.add(c);
        }

        return listaFiltrada;
    }

    private List<Conta> carregarContas() {

        return new DAO().listar(Conta.class);
    }

    private void imprimir() {

        HashMap parametrosRelatorio = new HashMap();

        List<HashMap> lista = new ArrayList<HashMap>();
        for (Conta c : modelo.getObjetos()) {
            HashMap<String, String> objeto = new HashMap<String, String>();
            objeto.put("nome", c.getNome());
            objeto.put("cpf", c.getNomeVinculo());
            lista.add(objeto);
        }

        new Relatorios().imprimir("RelatorioFuncionarios", parametrosRelatorio, lista, false, true, null);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnImprimir.addActionListener(this);
            txtNome.addCaretListener(this);
            txtNome.addActionListener(this);
            tabela.addMouseListener(this);
            radioCodigo.addActionListener(this);
            radioNome.addActionListener(this);
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
                    modelo.editar(new Conta());
                }
            }
            if (source == radioCodigo || source == radioNome){
                txtNome.grabFocus();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        txtNome = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnRemover = new javax.swing.JButton();
        radioCodigo = new javax.swing.JRadioButton();
        radioNome = new javax.swing.JRadioButton();

        setClosable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Conta");
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

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionar.setToolTipText("Adicionar");
        btnAdicionar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionar.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditar.setToolTipText("Editar");
        btnEditar.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditar.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditar.setPreferredSize(new java.awt.Dimension(32, 32));

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir");
        btnImprimir.setMaximumSize(new java.awt.Dimension(32, 32));
        btnImprimir.setMinimumSize(new java.awt.Dimension(32, 32));
        btnImprimir.setPreferredSize(new java.awt.Dimension(32, 32));

        jLabel1.setText("Pesquisar por:");

        btnRemover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/lixeira.gif"))); // NOI18N
        btnRemover.setToolTipText("Excluir");
        btnRemover.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemover.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemover.setPreferredSize(new java.awt.Dimension(32, 32));

        buttonGroup1.add(radioCodigo);
        radioCodigo.setSelected(true);
        radioCodigo.setText("Código");

        buttonGroup1.add(radioNome);
        radioNome.setText("Nome");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(radioCodigo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(radioNome)
                .addGap(2, 2, 2)
                .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(radioCodigo)
                    .addComponent(radioNome)))
            .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnRemover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnRemover;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radioCodigo;
    private javax.swing.JRadioButton radioNome;
    private javax.swing.JTable tabela;
    private javax.swing.JTextField txtNome;
    // End of variables declaration//GEN-END:variables
}
