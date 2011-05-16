/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaContaCorrente.java
 *
 * Created on 29/09/2010, 11:39:09
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conciliacao;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.util.RenderizadorCelulaCor;
import condominioPlus.util.RenderizadorCelulaCorData;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class TelaConciliacao extends javax.swing.JInternalFrame {

    private Conciliacao conciliacao;
    private Condominio condominio;
    private TabelaModelo_2 modeloTabela;
    private List<Pagamento> pagamentos;
    private RenderizadorCelulaCor renderizadorCelulaCor;

    /** Creates new form TelaContaCorrente */
    public TelaConciliacao(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getConciliacao() == null) {
            conciliacao = new Conciliacao();
            condominio.setConciliacao(conciliacao);
            new DAO().salvar(condominio);
        } 

        initComponents();
        new ControladorEventos();


        carregarTabela();


        if (condominio != null) {
            this.setTitle("Conciliação Bancária - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabelaConciliacao, "Data, Documento, Conta, Descrição, Valor, Saldo ".split(",")) {

                   

            @Override
            protected List<Pagamento> getCarregarObjetos() {
                return getPagamentos();
            }

           
            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getDataPagamento());
                    case 1:
                        return pagamento.getForma() == FormaPagamento.CHEQUE ? String.valueOf(((DadosCheque) pagamento.getDadosPagamento()).getNumero()) : String.valueOf(((DadosDOC) pagamento.getDadosPagamento()).getNumeroDocumento());
                    case 2:
                        return pagamento.getConta().getCodigo();
                    case 3:
                        return pagamento.getHistorico().toUpperCase();
                    case 4:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(pagamento.getSaldo().doubleValue());
                    default:
                        return null;
                }
            }

            @Override
            public boolean getRemover(Pagamento pagamento) {
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getHistorico() + " ?", TelaConciliacao.this)) {
                    return false;
                }

                try {
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getHistorico());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaConciliacao.this);
                    return false;
                }
            }
        };

        renderizadorCelulaCor = new RenderizadorCelulaCor(modeloTabela);
        RenderizadorCelulaCorData renderizadorCelula = new RenderizadorCelulaCorData(modeloTabela);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(0)).setCellRenderer(renderizadorCelula);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(1)).setCellRenderer(renderizadorCelulaCor);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(2)).setCellRenderer(renderizadorCelulaCor);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(3)).setCellRenderer(renderizadorCelulaCor);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(4)).setCellRenderer(renderizadorCelulaCor);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(5)).setCellRenderer(renderizadorCelulaCor);




        tabelaConciliacao.getColumn(modeloTabela.getCampo(3)).setMinWidth(300);
        tabelaConciliacao.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);

    }

   

    private List<Pagamento> getPagamentos() {
        pagamentos = new DAO().listar("PagamentosConciliacao", condominio.getConciliacao());
        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();
        Collections.sort(pagamentos, comCod);
        ComparatorPagamento comparator = new ComparatorPagamento();
        Collections.sort(pagamentos, comparator);
        return pagamentos;
    }

  private void apagarItensSelecionados() {
        if (!ApresentacaoUtil.perguntar("Desejar remover os pagamentos?", this)) {
            return;
        }
        if (modeloTabela.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
            List<Pagamento> itensRemoverConciliacao = modeloTabela.getObjetosSelecionados();
            List<Pagamento> itensRelacionadosRemover = new ArrayList<Pagamento>();

            for (Pagamento p : itensRemoverConciliacao) {
                if (p.getTransacaoBancaria() != null) {
                    TransacaoBancaria transacao = p.getTransacaoBancaria();
                    Pagamento pagamentoRelacionado = new Pagamento();
                    for (Pagamento p2 : transacao.getPagamentos()) {
                        if (!p.equals(p2)) {
                            pagamentoRelacionado = p2;
                            pagamentoRelacionado.setDadosPagamento(null);

                            String nome = pagamentoRelacionado.getConta().getNomeVinculo();

                            if (nome.equals("AF")) {
                                condominio.getAplicacao().setSaldo(condominio.getAplicacao().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            } else if (nome.equals("PO")) {
                                condominio.getPoupanca().setSaldo(condominio.getPoupanca().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            } else if (nome.equals("CO")) {
                                condominio.getConsignacao().setSaldo(condominio.getConsignacao().getSaldo().subtract(pagamentoRelacionado.getValor()));
                            } else if (nome.equals("EM")) {
                            }
                            //verificar

                            itensRelacionadosRemover.add(pagamentoRelacionado);
                        }
                    }
                    new DAO().remover(transacao);
                }
                modeloTabela.remover(p);
                modeloTabela.notificar();
            }
            if (!itensRelacionadosRemover.isEmpty()) {
                for (Pagamento p : itensRelacionadosRemover) {

                    String nome = p.getConta().getNomeVinculo();

                    if (nome.equals("AF")) {
                        condominio.getAplicacao().getPagamentos().remove(p);
                    } else if (nome.equals("PO")) {
                        condominio.getPoupanca().getPagamentos().remove(p);
                    } else if (nome.equals("CO")) {
                        condominio.getConsignacao().getPagamentos().remove(p);
                    } else if (nome.equals("EM")) {
                    }
                }
                new DAO().remover(itensRelacionadosRemover);
                //verificar
            }
            new DAO().remover(itensRemoverConciliacao);
            condominio.getConciliacao().getPagamentos().removeAll(itensRemoverConciliacao);
            new DAO().salvar(condominio);

            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void editarPagamento() {
        if (!modeloTabela.getObjetosSelecionados().isEmpty()) {
            DialogoEditarPagamentoContaCorrente tela = new DialogoEditarPagamentoContaCorrente((Pagamento) modeloTabela.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloTabela.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
        }
    }


/** This method is called from within the constructor to
 * initialize the form.
 * WARNING: Do NOT modify this code. The content of this method is
 * always regenerated by the Form Editor.
 */
private class ControladorEventos extends ControladorEventosGenerico {

    int contador;

    @Override
    public void actionPerformed(ActionEvent e) {
        Object origem = e.getSource();
        if (origem == itemMenuApagarSelecionados) {
            apagarItensSelecionados();

        } else if (origem == itemMenuEditarPagamento){
            editarPagamento();
        }
    }

    @Override
    public void configurar() {

        ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaConciliacao.this, JTextField.class);

        tabelaConciliacao.addMouseListener(this);
        itemMenuApagarSelecionados.addActionListener(this);
        itemMenuEditarPagamento.addActionListener(this);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    }

@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        itemMenuApagarSelecionados = new javax.swing.JMenuItem();
        itemMenuEditarPagamento = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaConciliacao = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        itemMenuEditarPagamento.setText("Editar Pagamento Selecionado");
        popupMenu.add(itemMenuEditarPagamento);

        jMenuItem3.setText("jMenuItem3");
        popupMenu.add(jMenuItem3);

        setClosable(true);
        setTitle("Conta Corrente");
        setPreferredSize(new java.awt.Dimension(800, 600));

        tabelaConciliacao.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaConciliacao);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton1.setText("Enviar Todos para Conta Corrente");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jButton1)
                .addContainerGap(509, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JMenuItem itemMenuEditarPagamento;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaConciliacao;
    // End of variables declaration//GEN-END:variables

}

