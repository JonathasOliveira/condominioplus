/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaPoupanca.java
 *
 * Created on 21/02/2011, 15:51:51
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaIndispensavel;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.Poupanca;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author eugenia
 */
public class TelaContasIndispensaveis extends javax.swing.JInternalFrame {

    private Poupanca poupanca;
    private Condominio condominio;
    private Conta conta;
    private ContaIndispensavel contaIndispensavel;
    private TabelaModelo_2 modeloTabela;
    private List<ContaIndispensavel> listaContasIndispensaveis;

    /** Creates new form TelaPoupanca */
    public TelaContasIndispensaveis(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getPoupanca() == null) {
            poupanca = new Poupanca();
            condominio.setPoupanca(poupanca);
            poupanca.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            poupanca = condominio.getPoupanca();
            if (poupanca.getCondominio() == null) {
                poupanca.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }

        initComponents();
        new ControladorEventos();

        poupanca.calculaSaldo(poupanca);
        carregarTabela();

        if (condominio != null) {
            this.setTitle("Poupança - " + condominio.getRazaoSocial());
        }
    }

    public void carregarTabela() {

        modeloTabela = new TabelaModelo_2<ContaIndispensavel>(tabelaContasIndispensaveis, "Conta, Histórico, Data Vencimento".split(",")) {

            @Override
            protected List<ContaIndispensavel> getCarregarObjetos() {
                return getContasIndispensaveis();
            }

            @Override
            public Object getValor(ContaIndispensavel contaIndispensavel, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return contaIndispensavel.getConta().getCodigo();
                    case 1:
                        return contaIndispensavel.getConta().getNome();
                    case 2:
                        return DataUtil.getDateTime(contaIndispensavel.getDataVencimento());
                    default:
                        return null;
                }
            }
        };

    }

    private List<ContaIndispensavel> getContasIndispensaveis() {
        listaContasIndispensaveis = new DAO().listar("PagamentosPoupanca", condominio.getPoupanca());
        return listaContasIndispensaveis;
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtConta);
        campos.add(txtHistorico);
        return campos;
    }

    private void preencherObjeto() {
    }

    private void limparCampos() {
        txtHistorico.setText("");
        txtConta.setText("");
    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }
            preencherObjeto();

            TipoAcesso tipo = null;
            if (condominio.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }


            String descricao = "Cadastro do Condominio " + condominio.getRazaoSocial() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Desejar remover os pagamentos?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<Pagamento> itensRemoverPoupanca = modeloTabela.getObjetosSelecionados();
            List<Pagamento> itensRelacionadosRemover = new ArrayList<Pagamento>();

            for (Pagamento p : itensRemoverPoupanca) {
                if (p.getTransacaoBancaria() != null) {
                    TransacaoBancaria transacao = p.getTransacaoBancaria();
                    Pagamento pagamentoRelacionado = new Pagamento();
                    for (Pagamento p2 : transacao.getPagamentos()) {
                        if (!p.equals(p2)) {
                            pagamentoRelacionado = p2;
                            pagamentoRelacionado.setDadosPagamento(null);
                            condominio.getContaCorrente().setSaldo(condominio.getContaCorrente().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            itensRelacionadosRemover.add(pagamentoRelacionado);
                        }
                    }
                    new DAO().remover(transacao);
                }
                modeloTabela.remover(p);
                modeloTabela.notificar();
                poupanca.setSaldo(poupanca.getSaldo().subtract(p.getValor()));
            }
            if (!itensRelacionadosRemover.isEmpty()) {
                new DAO().remover(itensRelacionadosRemover);
                condominio.getContaCorrente().getPagamentos().removeAll(itensRelacionadosRemover);
            }
            new DAO().remover(itensRemoverPoupanca);
            condominio.getPoupanca().getPagamentos().removeAll(itensRemoverPoupanca);
            new DAO().salvar(condominio);
            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false);
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
            tabelaContasIndispensaveis.addMouseListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
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

                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaContasIndispensaveis.this);
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
        itemMenuImprimirPoupanca = new javax.swing.JMenuItem();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtDataVencimento = new net.sf.nachocalendar.components.DateField();
        btnConta = new javax.swing.JButton();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtConta = new javax.swing.JTextField();
        btnIncluir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContasIndispensaveis = new javax.swing.JTable();

        itemMenuImprimirPoupanca.setText("Imprimir Conta Poupança");
        popupMenu.add(itemMenuImprimirPoupanca);

        itemMenuRemoverSelecionados.setText("Deletar Itens Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        setClosable(true);
        setTitle("Contas Indispensáveis");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Data Vencimento:");

        txtDataVencimento.setFocusable(false);
        txtDataVencimento.setRequestFocusEnabled(false);

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Histórico:");

        txtConta.setName("Conta"); // NOI18N

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConta)
                    .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataVencimento, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnConta)
                                .addComponent(jLabel2))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaContasIndispensaveis.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContasIndispensaveis);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JMenuItem itemMenuImprimirPoupanca;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaContasIndispensaveis;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtDataVencimento;
    private javax.swing.JTextField txtHistorico;
    // End of variables declaration//GEN-END:variables
}
