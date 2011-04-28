/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaIdentificadores.java
 *
 * Created on 21/02/2011, 15:51:51
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.Identificador;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;

/**
 *
 * @author eugenia
 */
public class TelaIdentificadores extends javax.swing.JInternalFrame {

    private Conta conta;
    private Identificador identificador;
    private TabelaModelo_2<Identificador> modeloTabela;
    private List<Identificador> listaIdentificadores;

    /** Creates new form TelaContasIndispensaveis */
    public TelaIdentificadores() {


        initComponents();
        new ControladorEventos();
        desabilitarCampos();
        carregarTabela();

    }

    public void carregarTabela() {

        modeloTabela = new TabelaModelo_2<Identificador>(tabelaIdentificadores, "Cód. Histórico, Palavra Chave, Cód. Conta, Conta, Tipo".split(",")) {

            @Override
            protected List<Identificador> getCarregarObjetos() {
                return getIdentificadores();
            }

            @Override
            protected List<Identificador> getFiltrar(List<Identificador> identificadores) {
                return filtrarLista(txtBusca.getText(), identificadores);
            }

            @Override
            public Object getValor(Identificador identificador, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return identificador.getCodigoHistorico();
                    case 1:
                        return identificador.getPalavraChave();
                    case 2:
                        return identificador.getConta().getCodigo();
                    case 3:
                        return identificador.getConta().getNome();
                    case 4:
                        return identificador.getConta().isCredito() ? "C" : "D";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaIdentificadores.getColumn(modeloTabela.getCampo(1)).setMinWidth(200);
        tabelaIdentificadores.getColumn(modeloTabela.getCampo(3)).setMinWidth(200);
        tabelaIdentificadores.getColumn(modeloTabela.getCampo(4)).setMaxWidth(40);
        tabelaIdentificadores.getColumn(modeloTabela.getCampo(4)).setCellRenderer(centralizado);

    }

    private List<Identificador> getIdentificadores() {
        listaIdentificadores = new DAO().listar(Identificador.class);
        return listaIdentificadores;
    }

    private List<Identificador> filtrarLista(String sequencia, List<Identificador> identificadores) {
        ArrayList<Identificador> listaFiltrada = new ArrayList<Identificador>();

        String[] sequencias = sequencia.toUpperCase().split(" ", 0);

        IDENTIFICADORES:
        for (Identificador i : identificadores) {
            for (String s : sequencias) {
                if (!i.getPalavraChave().toUpperCase().contains(s)) {
                    continue IDENTIFICADORES;
                }
            }

            listaFiltrada.add(i);
        }

        return listaFiltrada;
    }

    private void desabilitarCampos() {
        txtConta.setEnabled(false);
        txtHistorico.setEnabled(false);
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtConta);
        campos.add(txtHistorico);
        campos.add(txtCodigoHistorico);
        return campos;
    }

    private boolean verificarContasIndispensaveis() {
        for (Identificador i : getIdentificadores()) {
            if (i.getCodigoHistorico() == Integer.valueOf(txtCodigoHistorico.getText())) {
                return true;
            }
        }
        return false;
    }

    private void preencherObjeto() {
        identificador = new Identificador();
        identificador.setConta(conta);
        identificador.setPalavraChave(txtPalavraChave.getText());
        identificador.setCodigoHistorico(Integer.valueOf(txtCodigoHistorico.getText()));
        new DAO().salvar(identificador);
        limparCampos();
    }

    private void limparCampos() {
        txtHistorico.setText("");
        txtConta.setText("");
        txtPalavraChave.setText("");
        txtCodigoHistorico.setText("");
    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }

            if (verificarContasIndispensaveis()) {
                ApresentacaoUtil.exibirAdvertencia("Já existe um identificador com as mesmas características.", this);
                return;
            }

            preencherObjeto();

            TipoAcesso tipo = null;
            if (identificador.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }

            String descricao = "Cadastro do Identificador " + identificador.getPalavraChave() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Desejar remover os registros selecionados?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<Identificador> itensRemover = modeloTabela.getObjetosSelecionados();

            for (Identificador i : itensRemover) {
                modeloTabela.remover(i);
                modeloTabela.notificar();
                new DAO().remover(i);
            }

            ApresentacaoUtil.exibirInformacao("Registros removidos com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false, "T");
        c.setVisible(true);

        if (c.getConta() != null) {
            conta = c.getConta();
            txtConta.setText(String.valueOf(conta.getCodigo()));
            txtHistorico.setText(conta.getNome());
        }
    }

    private Conta pesquisarContaPorCodigo(int codigo) {
        Conta c = null;
        try {
            c = (Conta) new DAO().localizar(Conta.class, codigo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void configurar() {
            btnConta.addActionListener(this);
            btnIncluir.addActionListener(this);
            txtConta.addFocusListener(this);
            tabelaIdentificadores.addMouseListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            txtBusca.addCaretListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();
            if (origem == btnConta) {
                pegarConta();
            } else if (origem == btnIncluir) {
                salvar();
                carregarTabela();
            } else if (origem == itemMenuRemoverSelecionados) {
                apagarItensSelecionados();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (e.getSource() == txtConta) {
                Conta resultado = null;
                if (new LimitarCaracteres(10).ValidaNumero(txtConta)) {
                    if (!txtConta.getText().equals("") && txtConta.getText() != null) {
                        resultado = pesquisarContaPorCodigo(Integer.valueOf(txtConta.getText()));
                        if (resultado != null) {
                            conta = resultado;
                            txtConta.setText(String.valueOf(conta.getCodigo()));
                            txtHistorico.setText(conta.getNome());
                        } else {

                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaIdentificadores.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            if (e.getSource() == txtBusca) {
                modeloTabela.filtrar();
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

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaIdentificadores = new javax.swing.JTable();
        painelPesquisa = new javax.swing.JPanel();
        txtBusca = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtPalavraChave = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCodigoHistorico = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        txtConta = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtHistorico = new javax.swing.JTextField();
        btnIncluir = new javax.swing.JButton();

        itemMenuRemoverSelecionados.setText("Deletar Itens Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        setClosable(true);
        setResizable(true);
        setTitle("Identificadores");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tabelaIdentificadores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaIdentificadores);

        painelPesquisa.setBorder(javax.swing.BorderFactory.createTitledBorder("Pesquisa"));

        javax.swing.GroupLayout painelPesquisaLayout = new javax.swing.GroupLayout(painelPesquisa);
        painelPesquisa.setLayout(painelPesquisaLayout);
        painelPesquisaLayout.setHorizontalGroup(
            painelPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPesquisaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtBusca, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(307, Short.MAX_VALUE))
        );
        painelPesquisaLayout.setVerticalGroup(
            painelPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelPesquisaLayout.createSequentialGroup()
                .addComponent(txtBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cadastro"));

        jLabel1.setText("Palavra Chave:");

        jLabel3.setText("Cód. Histórico:");

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtConta.setName("Conta"); // NOI18N

        jLabel2.setText("Descrição Conta:");

        txtHistorico.setName("Histórico"); // NOI18N

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(108, 108, 108))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtPalavraChave, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCodigoHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(26, 26, 26))
                    .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnConta)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCodigoHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPalavraChave, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(1, 1, 1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(painelPesquisa, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(108, 108, 108))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel painelPesquisa;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaIdentificadores;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JTextField txtCodigoHistorico;
    private javax.swing.JTextField txtConta;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtPalavraChave;
    // End of variables declaration//GEN-END:variables
}
