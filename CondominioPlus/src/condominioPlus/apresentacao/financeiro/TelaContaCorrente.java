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
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContaCorrente;
import condominioPlus.negocio.financeiro.DadosCheque;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.ComparadorPagamentoCodigo;
import condominioPlus.util.ComparatorPagamento;
import condominioPlus.util.RenderizadorCelulaCor;
import condominioPlus.util.RenderizadorCelulaCorData;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class TelaContaCorrente extends javax.swing.JInternalFrame {

    private ContaCorrente contaCorrente;
    private Condominio condominio;
    private TabelaModelo_2 modeloTabela;
    private List<Pagamento> pagamentos;
    private RenderizadorCelulaCor renderizadorCelulaCor;

    /** Creates new form TelaContaCorrente */
    public TelaContaCorrente(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getContaCorrente() == null) {
            contaCorrente = new ContaCorrente();
            condominio.setContaCorrente(contaCorrente);
            contaCorrente.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            contaCorrente = condominio.getContaCorrente();
            if (contaCorrente.getCondominio() == null) {
                contaCorrente.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }

        initComponents();
        new ControladorEventos();

        calcularSaldo();

        carregarTabela();

        carregarComboFiltro();

        if (condominio != null) {
            this.setTitle("Conta Corrente - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabela() {
        modeloTabela = new TabelaModelo_2<Pagamento>(tabelaContaCorrente, "Data, Documento, Conta, Descrição, Valor, Saldo ".split(",")) {

            @Override
            protected Pagamento getAdicionar() {
                editar(new Pagamento());
                return null;
            }

            @Override
            public void editar(Pagamento pagamento) {
//              TelaPrincipal.getInstancia().criarFrame(new TelaDadosCondominio(condominio));
            }

            @Override
            protected List<Pagamento> getCarregarObjetos() {
                return getPagamentos();
            }

            @Override
            protected List<Pagamento> getFiltrar(List<Pagamento> pagamentos) {
                return filtrarListaPorCredito(pagamentos);
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
                if (!ApresentacaoUtil.perguntar("Deseja mesmo excluir o Pagamento - " + pagamento.getHistorico() + " ?", TelaContaCorrente.this)) {
                    return false;
                }

                try {
                    FuncionarioUtil.registrar(TipoAcesso.REMOCAO, "Remoção do Pagamento - " + pagamento.getHistorico());
                    return true;
                } catch (Throwable t) {
                    new TratadorExcecao(t, TelaContaCorrente.this);
                    return false;
                }
            }
        };

        renderizadorCelulaCor = new RenderizadorCelulaCor(modeloTabela);
        RenderizadorCelulaCorData renderizadorCelula = new RenderizadorCelulaCorData(modeloTabela);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(0)).setCellRenderer(renderizadorCelula);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(1)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(2)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setCellRenderer(renderizadorCelulaCor);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setCellRenderer(renderizadorCelulaCor);




        tabelaContaCorrente.getColumn(modeloTabela.getCampo(3)).setMinWidth(300);
        tabelaContaCorrente.getColumn(modeloTabela.getCampo(4)).setMinWidth(100);

    }

    private void carregarComboFiltro() {
        cbFiltros.setModel(new ComboModelo<String>(Util.toList(new String[]{" ", "Crédito", "Débito"}), false));
    }

    private List<Pagamento> filtrarListaPorCredito(List<Pagamento> listaGeral) {
        List<Pagamento> lista = new ArrayList<Pagamento>();
        if (cbFiltros.getSelectedIndex() != -1) {
            if (cbFiltros.getSelectedItem().toString().equals("Crédito")) {
                for (Pagamento p : listaGeral) {
                    if (p.getConta().isCredito()) {
                        lista.add(p);
                    }
                }
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(0);
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMaxWidth(0);
                return lista;

            } else if (cbFiltros.getSelectedItem().toString().equals("Débito")) {
                for (Pagamento p : getPagamentos()) {
                    if (!p.getConta().isCredito()) {
                        lista.add(p);
                    }
                }
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(0);
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMaxWidth(0);
                return lista;
            } else if (cbFiltros.getSelectedItem().toString().equals(" ")) {
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMinWidth(100);
                tabelaContaCorrente.getColumn(modeloTabela.getCampo(5)).setMaxWidth(100);

                return getPagamentos();
            }
        }
        return getPagamentos();

    }

    private List<Pagamento> getPagamentos() {
        pagamentos = new DAO().listar("PagamentosContaCorrente", condominio.getContaCorrente());
        ComparadorPagamentoCodigo comCod = new ComparadorPagamentoCodigo();
        Collections.sort(pagamentos, comCod);
        ComparatorPagamento comparator = new ComparatorPagamento();
        Collections.sort(pagamentos, comparator);
        return pagamentos;
    }

    private void verificarLista() {
        if (condominio.getContaCorrente().getPagamentos().size() == 1) {
            for (Pagamento p : getPagamentos()) {
                p.setSaldo(p.getValor());
                condominio.getContaCorrente().setSaldo(p.getValor());

            }
        }
    }

    private void calcularSaldo() {
        verificarLista();
        contaCorrente.calculaSaldo(contaCorrente);
        carregarTabela();
    }

//  private void apagarItensSelecionados() {
//        if (!ApresentacaoUtil.perguntar("Desejar remover os pagamentos?", this)) {
//            return;
//        }
//        if (modeloTabela.getLinhaSelecionada() > -1) {
//            System.out.println("removendo... " + modeloTabela.getLinhasSelecionadas());
//            List<Pagamento> itensRemoverContaCorrente = modeloTabela.getObjetosSelecionados();
//            List<Pagamento> itensRelacionadosRemover = new ArrayList<Pagamento>();
//
//            for (Pagamento p : itensRemoverContaCorrente) {
//                if (p.getTransacaoBancaria() != null) {
//                    TransacaoBancaria transacao = p.getTransacaoBancaria();
//                    Pagamento pagamentoRelacionado = new Pagamento();
//                    for (Pagamento p2 : transacao.getPagamentos()) {
//                        if (!p.equals(p2)) {
//                            pagamentoRelacionado = p2;
//                            pagamentoRelacionado.setDadosPagamento(null);
//
//                            String nome = pagamentoRelacionado.getConta().getNomeVinculo();
//
//                            if (nome.equals("AF")) {
//                                condominio.getAplicacao().setSaldo(condominio.getAplicacao().getSaldo().subtract(pagamentoRelacionado.getValor()));
//                            } else if (nome.equals("PO")) {
//                                condominio.getPoupanca().setSaldo(condominio.getPoupanca().getSaldo().subtract(pagamentoRelacionado.getValor()));
//                            } else if (nome.equals("CO")) {
//                                condominio.getConsignacao().setSaldo(condominio.getConsignacao().getSaldo().subtract(pagamentoRelacionado.getValor()));
//                            } else if (nome.equals("EM")) {
//                            }
//                            //verificar
//
//                            itensRelacionadosRemover.add(pagamentoRelacionado);
//                        }
//                    }
//                    new DAO().remover(transacao);
//                }
//                modeloTabela.remover(p);
//                modeloTabela.notificar();
//                contaCorrente.setSaldo(contaCorrente.getSaldo().subtract(p.getValor()));
//            }
//            if (!itensRelacionadosRemover.isEmpty()) {
//                for (Pagamento p : itensRelacionadosRemover) {
//
//                    String nome = p.getConta().getNomeVinculo();
//
//                    if (nome.equals("AF")) {
//                        condominio.getAplicacao().getPagamentos().remove(p);
//                    } else if (nome.equals("PO")) {
//                        condominio.getPoupanca().getPagamentos().remove(p);
//                    } else if (nome.equals("CO")) {
//                        condominio.getConsignacao().getPagamentos().remove(p);
//                    } else if (nome.equals("EM")) {
//                    }
//                }
//                new DAO().remover(itensRelacionadosRemover);
//                //verificar
//            }
//            new DAO().remover(itensRemoverContaCorrente);
//            condominio.getContaCorrente().getPagamentos().removeAll(itensRemoverContaCorrente);
//            new DAO().salvar(condominio);
//            ApresentacaoUtil.exibirInformacao("Pagamentos removidos com sucesso!", this);
//        } else {
//            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
//        }
//
//    }
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
//                apagarItensSelecionados();
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (cbFiltros.getSelectedIndex() != -1) {
                modeloTabela.filtrar();
            }
        }

        @Override
        public void configurar() {

            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaContaCorrente.this, JTextField.class);

            cbFiltros.addItemListener(this);
            tabelaContaCorrente.addMouseListener(this);
            itemMenuApagarSelecionados.addActionListener(this);
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
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaContaCorrente = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        cbFiltros = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtSaldoAnteriorContaCorrente = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCreditosContaCorrente = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtDebitosContaCorrente = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtSaldoPoupanca = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtSaldoAplicacao = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtSaldoConsignacao = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtSaldoEmprestimo = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtSaldoAnteriorExtratoBancario = new javax.swing.JTextField();
        txtCreditosExtratoBancario = new javax.swing.JTextField();
        txtDebitosExtratoBancario = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txtSaldoContaCorrente = new javax.swing.JTextField();
        txtSaldoExtrato = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();

        itemMenuApagarSelecionados.setText("Apagar Selecionado");
        popupMenu.add(itemMenuApagarSelecionados);

        jMenuItem2.setText("jMenuItem2");
        popupMenu.add(jMenuItem2);

        jMenuItem3.setText("jMenuItem3");
        popupMenu.add(jMenuItem3);

        setClosable(true);
        setTitle("Conta Corrente");

        tabelaContaCorrente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaContaCorrente);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Filtrar por:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(611, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbFiltros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setBackground(new java.awt.Color(0, 102, 102));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Resumo da Conta Corrente:");
        jLabel1.setAlignmentX(1.0F);
        jLabel1.setOpaque(true);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Saldo Anterior");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Créditos");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setText("Débitos");

        jLabel5.setBackground(new java.awt.Color(0, 102, 102));
        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Saldos de Aplicações, Empréstimos e Outros:");
        jLabel5.setOpaque(true);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel7.setText("Poupança");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Aplicações");

        txtSaldoAplicacao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSaldoAplicacaoActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setText("Consignações");

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Empréstimos");

        jLabel11.setBackground(new java.awt.Color(0, 102, 102));
        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Resumo Extrato Bancário");
        jLabel11.setOpaque(true);

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel12.setText("Saldo Anterior");

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel13.setText("Créditos");

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("Débitos");

        jLabel15.setBackground(new java.awt.Color(0, 102, 102));
        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Saldos Atuais");
        jLabel15.setOpaque(true);

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel16.setText("Caixa");

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setText("Extrato");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(txtSaldoAnteriorContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCreditosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtDebitosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtSaldoPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSaldoAplicacao, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtSaldoConsignacao))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(51, 51, 51)
                                .addComponent(jLabel8)
                                .addGap(51, 51, 51)
                                .addComponent(jLabel9)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel10))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSaldoEmprestimo, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))))
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(113, 113, 113)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSaldoAnteriorExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(txtCreditosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDebitosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)))
                    .addComponent(jLabel15)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(89, 89, 89))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtSaldoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(txtSaldoExtrato, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSaldoAnteriorContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtCreditosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDebitosContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSaldoPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoAplicacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoConsignacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoEmprestimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSaldoAnteriorExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCreditosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDebitosExtratoBancario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSaldoExtrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSaldoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtSaldoAplicacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSaldoAplicacaoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSaldoAplicacaoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbFiltros;
    private javax.swing.JMenuItem itemMenuApagarSelecionados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaContaCorrente;
    private javax.swing.JTextField txtCreditosContaCorrente;
    private javax.swing.JTextField txtCreditosExtratoBancario;
    private javax.swing.JTextField txtDebitosContaCorrente;
    private javax.swing.JTextField txtDebitosExtratoBancario;
    private javax.swing.JTextField txtSaldoAnteriorContaCorrente;
    private javax.swing.JTextField txtSaldoAnteriorExtratoBancario;
    private javax.swing.JTextField txtSaldoAplicacao;
    private javax.swing.JTextField txtSaldoConsignacao;
    private javax.swing.JTextField txtSaldoContaCorrente;
    private javax.swing.JTextField txtSaldoEmprestimo;
    private javax.swing.JTextField txtSaldoExtrato;
    private javax.swing.JTextField txtSaldoPoupanca;
    // End of variables declaration//GEN-END:variables
}

