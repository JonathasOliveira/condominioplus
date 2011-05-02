/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaEmprestimo.java
 *
 * Created on 28/02/2011, 12:07:28
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.Conta;
import condominioPlus.negocio.financeiro.ContratoEmprestimo;
import condominioPlus.negocio.financeiro.DadosDOC;
import condominioPlus.negocio.financeiro.DadosPagamento;
import condominioPlus.negocio.financeiro.Emprestimo;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.FormaPagamentoEmprestimo;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.PagamentoUtil;
import condominioPlus.negocio.financeiro.TransacaoBancaria;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.util.LimitarCaracteres;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class TelaEmprestimo extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private Emprestimo emprestimo;
    private Pagamento pagamento;
    private ContratoEmprestimo contrato;
    private List<ContratoEmprestimo> contratos;
    private TabelaModelo_2<ContratoEmprestimo> modelo;
    private TabelaModelo_2<Pagamento> modeloTabelaPagamentos;
    private Conta conta;

    /** Creates new form TelaEmprestimo */
    public TelaEmprestimo(Condominio condominio) {

        this.condominio = condominio;
        if (condominio.getEmprestimo() == null) {
            emprestimo = new Emprestimo();
            condominio.setEmprestimo(emprestimo);
            emprestimo.setCondominio(condominio);
            new DAO().salvar(condominio);
        } else {
            emprestimo = condominio.getEmprestimo();
            if (emprestimo.getCondominio() == null) {
                emprestimo.setCondominio(condominio);
                new DAO().salvar(condominio);
            }
        }
        initComponents();

        desabilitarBotoesUmaParcela();

        painelDadosContrato.setVisible(false);

        new ControladorEventos();

        preencherTela();

        carregarTabela();

        if (condominio != null) {
            this.setTitle("Empréstimos - " + condominio.getRazaoSocial());
        }
    }

    private void preencherTela() {
        txtDataPrimeiroPagamento.setValue(DataUtil.toString(new DateTime(DataUtil.hoje().plusMonths(1))));
        if (emprestimo.getSaldo() != null) {
            lblSaldoEmprestimo.setText(emprestimo.getSaldo().toString().replace(".", ","));
        }
    }

    private void desabilitarBotoesUmaParcela() {
        if (radioAVista.isSelected()) {
            txtNumeroParcelas.setText("1");
            txtValorParcelas.setText(txtValor.getText());
            txtNumeroParcelas.setEnabled(false);
            txtValorParcelas.setEnabled(false);
        } else if (radioParcelado.isSelected()) {
            txtValorParcelas.setText("");
            txtNumeroParcelas.setText("");
            txtNumeroParcelas.setEnabled(true);
            txtValorParcelas.setEnabled(true);
        } else if (radioConformeDisponibilidade.isSelected()) {
            txtNumeroParcelas.setText("1");
            txtValorParcelas.setText(txtValor.getText());
            txtNumeroParcelas.setEnabled(false);
            txtValorParcelas.setEnabled(false);
        }
    }

    private String obterFormaPagamento(ContratoEmprestimo c) {
        if (c.getForma() == FormaPagamentoEmprestimo.CONFORME_DISPONIBILIDADE) {
            return "Conforme Disponibilidade Financeira";
        } else if (c.getForma() == FormaPagamentoEmprestimo.PAGAMENTO_A_VISTA) {
            return "À vista";
        } else if (c.getForma() == FormaPagamentoEmprestimo.PARCELADO) {
            return "Parcelado";
        }
        return "";
    }

    private void carregarTabela() {
        modelo = new TabelaModelo_2<ContratoEmprestimo>(tabela, "Data, Descrição, Forma Pgto., Parcelas, Valor, Saldo".split(",")) {

            @Override
            protected List<ContratoEmprestimo> getCarregarObjetos() {
                return getContratos();
            }

            @Override
            public Object getValor(ContratoEmprestimo c, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(c.getDataContrato());
                    case 1:
                        return c.getDescricao();
                    case 2:
                        return obterFormaPagamento(c);
                    case 3:
                        return c.getNumeroParcelas();
                    case 4:
                        return PagamentoUtil.formatarMoeda(c.getValor().doubleValue());
                    case 5:
                        return PagamentoUtil.formatarMoeda(c.getSaldo().doubleValue());
                    default:
                        return null;

                }
            }
        };

                DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

               direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabela.getColumn(modelo.getCampo(4)).setCellRenderer(direita);
        tabela.getColumn(modelo.getCampo(5)).setCellRenderer(direita);

        tabela.getColumn(modelo.getCampo(0)).setMaxWidth(80);
        tabela.getColumn(modelo.getCampo(1)).setMinWidth(150);
        tabela.getColumn(modelo.getCampo(2)).setMinWidth(150);
        tabela.getColumn(modelo.getCampo(3)).setMaxWidth(60);

    }

    private List<ContratoEmprestimo> getContratos() {
        contratos = new DAO().listar("ContratosPorData", emprestimo);
        return contratos;

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtConta);
        campos.add(txtHistorico);
        campos.add(txtNumeroParcelas);
        campos.add(txtValor);
        campos.add(txtValorParcelas);

        return campos;

    }

    private void pegarConta() {
        DialogoConta c = new DialogoConta(null, true, true, false, "EM");
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

    private boolean preencherObjeto() {
        if (conta.getNomeVinculo().equals("EM")) {
            contrato = new ContratoEmprestimo();
            contrato.setDataContrato(DataUtil.getCalendar(txtData.getValue()));
            contrato.setDescricao(txtHistorico.getText());
            contrato.setEmprestimo(emprestimo);
            if (radioAVista.isSelected()) {
                contrato.setForma(FormaPagamentoEmprestimo.PAGAMENTO_A_VISTA);
            } else if (radioConformeDisponibilidade.isSelected()) {
                contrato.setForma(FormaPagamentoEmprestimo.CONFORME_DISPONIBILIDADE);
            } else if (radioParcelado.isSelected()) {
                contrato.setForma(FormaPagamentoEmprestimo.PARCELADO);
            }

            contrato.setNumeroParcelas(Integer.valueOf(txtNumeroParcelas.getText()));

            if (conta != null) {
                if (conta.isCredito()) {
                    contrato.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
                } else {
                    contrato.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
                }
            }


            DadosPagamento dados = new DadosDOC(Long.valueOf(Pagamento.gerarNumeroDocumento()));

            if (contrato.getNumeroParcelas() > 0) {
                String texto = "";
                if (contrato.getForma() == FormaPagamentoEmprestimo.PARCELADO && contrato.getNumeroParcelas() > 1) {
                    for (int i = 0; i < contrato.getNumeroParcelas(); i++) {
                        texto = "PAGAMENTO PARCELA " + (i + 1);
                        pagamento = new Pagamento();
                        if (i == 0) {
                            pagamento.setDataVencimento(DataUtil.getCalendar(txtDataPrimeiroPagamento.getValue()));
                        } else {
                            pagamento.setDataVencimento(DataUtil.getCalendar(new DateTime(txtDataPrimeiroPagamento.getValue()).plusMonths(i)));
                        }
                        pagamento.setConta(conta.getContaVinculada());
                        if (pagamento.getConta().isCredito()) {
                            pagamento.setValor(new BigDecimal(txtValorParcelas.getText().replace(",", ".")));
                        } else {
                            pagamento.setValor(new BigDecimal(txtValorParcelas.getText().replace(",", ".")).negate());
                        }

                        pagamento.setContratoEmprestimo(contrato);
                        pagamento.setHistorico(texto + " " + pagamento.getConta().getNome());
                        System.out.println("pagamento historico " + pagamento.getHistorico());
                        pagamento.setForma(FormaPagamento.DINHEIRO);
                        pagamento.setDadosPagamento(dados.clone());

                        if (conta.isCredito()) {
                            new DAO().salvar(pagamento);
                        } else {
                            verificarVinculo(pagamento, texto);
                        }

                    }

                } else {
                    System.out.println("teste");
                    texto = "PAGAMENTO ";
                    pagamento = new Pagamento();
                    pagamento.setDataVencimento(DataUtil.getCalendar(txtDataPrimeiroPagamento.getValue()));
                    pagamento.setConta(conta.getContaVinculada());
                    if (pagamento.getConta().isCredito()) {
                        pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
                    } else {
                        pagamento.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
                    }

                    pagamento.setContratoEmprestimo(contrato);
                    pagamento.setHistorico(texto + " " + pagamento.getConta().getNome());
                    pagamento.setDadosPagamento(dados);
                    pagamento.setForma(FormaPagamento.DINHEIRO);

                    if (conta.isCredito()) {
                        new DAO().salvar(pagamento);
                    } else {
                        verificarVinculo(pagamento, texto);
                    }
                }

                Pagamento p = new Pagamento();
                p.setDataPagamento(DataUtil.getCalendar(txtData.getValue()));
                p.setHistorico(conta.getContaVinculada().getNome());
                p.setConta(conta.getContaVinculada());
                p.setContratoEmprestimo(contrato);
                if (p.getConta().isCredito()) {
                    p.setValor(new BigDecimal(txtValor.getText().replace(",", ".")));
                } else {
                    p.setValor(new BigDecimal(txtValor.getText().replace(",", ".")).negate());
                }
                p.setSaldo(new BigDecimal(0));
                p.setDadosPagamento(dados);

                p.setContaCorrente(condominio.getContaCorrente());
                p.setPago(true);

                condominio.getContaCorrente().adicionarPagamento(p);
                condominio.getContaCorrente().setSaldo(condominio.getContaCorrente().getSaldo().add(p.getValor()));

                new DAO().salvar(condominio);
                limparCampos();
                return true;

            }
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione uma conta vinculada a Empréstimo!", this);
            return false;
        }

        return false;
    }

    private void verificarVinculo(Pagamento p1, String texto) {
        if (conta.getContaVinculada() != null) {

            TransacaoBancaria transacao = new TransacaoBancaria();
            if (p1.getTransacaoBancaria() != null) {
                transacao = p1.getTransacaoBancaria();
            }

            Pagamento pagamentoRelacionado = new Pagamento();
            if (transacao.getPagamentos() != null) {
                for (Pagamento p : transacao.getPagamentos()) {
                    if (!p.equals(p1)) {
                        pagamentoRelacionado = p;
                    }
                }
            }

            pagamentoRelacionado.setDataVencimento(p1.getDataVencimento());
            pagamentoRelacionado.setConta(conta);
            pagamentoRelacionado.setHistorico(texto + " " + conta.getContaVinculada().getNome());
            pagamentoRelacionado.setContratoEmprestimo(contrato);

            pagamentoRelacionado.setValor(p1.getValor().negate());

            pagamentoRelacionado.setSaldo(new BigDecimal(0));
            pagamentoRelacionado.setDadosPagamento(p1.getDadosPagamento());

            if (!pagamentoRelacionado.getConta().isCredito()) {
                pagamentoRelacionado.setContaPagar(condominio.getContaPagar());
            }
            pagamentoRelacionado.setPago(false);

            transacao.adicionarPagamento(p1);
            transacao.adicionarPagamento(pagamentoRelacionado);

            condominio.getContaPagar().adicionarPagamento(pagamentoRelacionado);

            System.out.println("Transacao Bancária: " + transacao);

            p1.setTransacaoBancaria(transacao);
            pagamentoRelacionado.setTransacaoBancaria(transacao);


        }

    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }
            if (!preencherObjeto()) {
                return;
            }

            TipoAcesso tipo = null;
            if (condominio.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }


            String descricao = "Contrato de Empréstimo adicionado! " + contrato.getDescricao() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void remover() {
        if (!ApresentacaoUtil.perguntar("Desejar remover o(s) contrato(s)?", this)) {
            return;
        }
        if (modelo.getLinhaSelecionada() > -1) {
            System.out.println("removendo... " + modelo.getLinhasSelecionadas());
            List<ContratoEmprestimo> itensRemover = modelo.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (ContratoEmprestimo c : itensRemover) {
                    c.setEmprestimo(null);
                    modelo.remover(c);
                    for (Pagamento p : c.getPagamentos()) {
                        p.setContratoEmprestimo(null);
                        if (p.getTransacaoBancaria() != null) {
                            TransacaoBancaria transacao = p.getTransacaoBancaria();
                            Pagamento pagamentoRelacionado;
                            for (Pagamento p2 : transacao.getPagamentos()) {
                                if (!p.equals(p2)) {
                                    pagamentoRelacionado = p2;
                                    pagamentoRelacionado.setDadosPagamento(null);
                                }
                            }
                            new DAO().remover(transacao);
                        }
                    }
                    new DAO().remover(c);
                }
            }
            condominio.getEmprestimo().getContratos().removeAll(itensRemover);
            new DAO().salvar(condominio);
            painelDadosContrato.setVisible(false);
            ApresentacaoUtil.exibirInformacao("Contrato(s) removido(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }

    }

    private void limparCampos() {
        txtValor.setText("");
        txtConta.setText("");
        txtHistorico.setText("");
        txtNumeroParcelas.setText("");
        txtValorParcelas.setText("");
    }

    public List<Pagamento> listarPagamentos() {
        List<Pagamento> lista = new DAO().listar("PagamentosPorContratoEmprestimo", contrato);
        List<Pagamento> novaLista = new ArrayList<Pagamento>();
        for (Pagamento p : lista) {
            if (p.getContaCorrente() == null && p.getContaPagar() == null) {
                novaLista.add(p);
            }
        }
        return novaLista;
    }

    public void carregarTabelaPagamentos() {

        modeloTabelaPagamentos = new TabelaModelo_2<Pagamento>(tabelaPagamentos, "Data Vencimento, Histórico, Valor, Conta, Tipo, Pago?".split(",")) {

            @Override
            protected List<Pagamento> getCarregarObjetos() {
                return listarPagamentos();
            }

            @Override
            public Object getValor(Pagamento pagamento, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(pagamento.getDataVencimento());
                    case 1:
                        return pagamento.getHistorico();
                    case 2:
                        return PagamentoUtil.formatarMoeda(pagamento.getValor().doubleValue());
                    case 3:
                        return pagamento.getConta().getCodigo();
                    case 4:
                        return pagamento.getConta().isCredito() ? "C" : "D";
                    case 5:
                        return pagamento.isPago() ? DataUtil.toString(DataUtil.getDateTime(pagamento.getDataPagamento())) : "Não pago";
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        DefaultTableCellRenderer direita = new DefaultTableCellRenderer();

        esquerda.setHorizontalAlignment(SwingConstants.LEFT);
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);
        direita.setHorizontalAlignment(SwingConstants.RIGHT);

        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(2)).setCellRenderer(direita);
        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(4)).setCellRenderer(centralizado);
        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(5)).setCellRenderer(centralizado);

        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(1)).setMinWidth(300);
        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(2)).setMaxWidth(140);
        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(3)).setMaxWidth(140);
        tabelaPagamentos.getColumn(modeloTabelaPagamentos.getCampo(4)).setMaxWidth(50);

    }

    private void desabilitarCamposContrato() {
        txtDataContrato.setEnabled(false);
        txtCodigoContrato.setEnabled(false);
        txtParcelasContrato.setEnabled(false);
        txtValorContrato.setEnabled(false);
    }

    private void exibirPainelContrato(ContratoEmprestimo c) {
        if (c != null) {
            painelDadosContrato.setVisible(true);
            contrato = c;
            desabilitarCamposContrato();
            preencherPainelContrato(c);
            carregarTabelaPagamentos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione um contrato!", this);
        }

    }

    private void cancelar() {
        painelDadosContrato.setVisible(false);
        carregarTabela();
    }

    public void preencherPainelContrato(ContratoEmprestimo c) {
        txtDataContrato.setValue(DataUtil.toString(c.getDataContrato()));
        txtCodigoContrato.setText(String.valueOf(c.getCodigo()));
        txtDescricao.setText(c.getDescricao());
        txtParcelasContrato.setText(String.valueOf(c.getNumeroParcelas()));
        txtValorContrato.setText(new Moeda(c.getValor()).toString());
    }

    private void editarPagamentoContrato() {
        if (!modeloTabelaPagamentos.getObjetosSelecionados().isEmpty()) {
            DialogoEditarPagamentoContrato tela = new DialogoEditarPagamentoContrato(modeloTabelaPagamentos.getObjetoSelecionado());
            tela.setLocationRelativeTo(this);
            tela.setVisible(true);
            modeloTabelaPagamentos.carregarObjetos();
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um pagamento!", this);
        }
    }

    private void salvarContrato() {
        contrato.setDescricao(txtDescricao.getText());
        new DAO().salvar(contrato);
        carregarTabela();

        ApresentacaoUtil.exibirInformacao("Informações salvas com sucesso!", this);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void actionPerformed(ActionEvent e) {
            origem = e.getSource();
            if (origem == btnIncluir) {
                salvar();
                carregarTabela();
            } else if (origem == btnCalcular) {
                emprestimo.calculaSaldo(emprestimo);
                new DAO().salvar(emprestimo);
                lblSaldoEmprestimo.setText(emprestimo.getSaldo().toString().replace(".", ","));
            } else if (origem == itemMenuRemoverSelecionados) {
                remover();
            } else if (origem == btnConta) {
                pegarConta();
            } else if (origem == itemMenuVisualizarContrato) {
                exibirPainelContrato(modelo.getObjetoSelecionado());
            } else if (origem == btnVoltar) {
                cancelar();
            } else if (origem == radioAVista) {
                desabilitarBotoesUmaParcela();
            } else if (origem == radioConformeDisponibilidade) {
                desabilitarBotoesUmaParcela();
            } else if (origem == radioParcelado) {
                desabilitarBotoesUmaParcela();
            } else if (origem == btnSalvar) {
                salvarContrato();
            }
        }

        @Override
        public void configurar() {
            btnCalcular.addActionListener(this);
            btnConta.addActionListener(this);
            btnImprimir.addActionListener(this);
            btnIncluir.addActionListener(this);
            tabela.addMouseListener(this);
            txtConta.addFocusListener(this);
            itemMenuRemoverSelecionados.addActionListener(this);
            itemMenuVisualizarContrato.addActionListener(this);
            btnVoltar.addActionListener(this);
            radioAVista.addActionListener(this);
            txtValor.addFocusListener(this);
            radioConformeDisponibilidade.addActionListener(this);
            radioParcelado.addActionListener(this);
            btnSalvar.addActionListener(this);
            tabelaPagamentos.addMouseListener(this);
            txtData.addChangeListener(this);
            tabela.addKeyListener(this);
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
                            ApresentacaoUtil.exibirErro("Código Inexistente!", TelaEmprestimo.this);
                            txtConta.setText("");
                            txtConta.grabFocus();
                            return;
                        }
                    }
                }
            }
            if (e.getSource() == txtValor) {
                if (radioAVista.isSelected() || radioConformeDisponibilidade.isSelected()) {
                    String resultado = null;
                    if (!txtValor.getText().equals("") && txtValor.getText() != null) {
                        resultado = txtValor.getText();
                        if (resultado != null) {
                            txtValorParcelas.setText(resultado);
                        } else {
                            ApresentacaoUtil.exibirErro("Digíte um valor para o empréstimo!", TelaEmprestimo.this);
                            return;
                        }

                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && e.getSource() == tabela) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            origem = e.getSource();
            if (origem == tabela && painelDadosContrato.isVisible()) {
                exibirPainelContrato(modelo.getObjetoSelecionado());
            } else if (origem == tabelaPagamentos && e.getClickCount() == 2) {
                editarPagamentoContrato();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            origem = e.getSource();
            if (origem == txtData) {
                txtDataPrimeiroPagamento.setValue(DataUtil.toString(DataUtil.getDateTime(txtData.getValue()).plusMonths(1)));
            }
        }
//        @Override
//        public void keyPressed(KeyEvent e) {
//            if ((origem == tabela && painelDadosContrato.isVisible()) && (e.getKeyCode() == e.VK_DOWN || e.getKeyCode() == e.VK_UP)) {
//                exibirPainelContrato(modelo.getObjetoSelecionado());
//            }
//        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        popupMenu = new javax.swing.JPopupMenu();
        itemMenuVisualizarContrato = new javax.swing.JMenuItem();
        itemMenuRemoverSelecionados = new javax.swing.JMenuItem();
        itemMenuImprimir = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        txtData = new net.sf.nachocalendar.components.DateField();
        jLabel1 = new javax.swing.JLabel();
        txtConta = new javax.swing.JTextField();
        txtValor = new javax.swing.JTextField();
        btnConta = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtHistorico = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnCalcular = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        btnIncluir = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtNumeroParcelas = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtValorParcelas = new javax.swing.JTextField();
        radioAVista = new javax.swing.JRadioButton();
        radioParcelado = new javax.swing.JRadioButton();
        radioConformeDisponibilidade = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        txtDataPrimeiroPagamento = new net.sf.nachocalendar.components.DateField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabela = new javax.swing.JTable();
        painelDadosContrato = new javax.swing.JPanel();
        txtDataContrato = new net.sf.nachocalendar.components.DateField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaPagamentos = new javax.swing.JTable();
        txtCodigoContrato = new javax.swing.JTextField();
        txtDescricao = new javax.swing.JTextField();
        txtParcelasContrato = new javax.swing.JTextField();
        txtValorContrato = new javax.swing.JTextField();
        btnVoltar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        lblSaldoEmprestimo = new javax.swing.JLabel();

        itemMenuVisualizarContrato.setText("Visualizar Dados Contrato");
        popupMenu.add(itemMenuVisualizarContrato);

        itemMenuRemoverSelecionados.setText("Remover Selecionados");
        popupMenu.add(itemMenuRemoverSelecionados);

        itemMenuImprimir.setText("Imprimir");
        popupMenu.add(itemMenuImprimir);

        setClosable(true);
        setTitle("Empréstimos");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        txtData.setFocusable(false);
        txtData.setRequestFocusEnabled(false);

        jLabel1.setText("Data Lançamento:");

        txtConta.setName("Conta"); // NOI18N

        txtValor.setName("Valor"); // NOI18N

        btnConta.setText("Conta:");
        btnConta.setBorder(null);
        btnConta.setBorderPainted(false);
        btnConta.setContentAreaFilled(false);
        btnConta.setFocusable(false);
        btnConta.setRequestFocusEnabled(false);
        btnConta.setVerifyInputWhenFocusTarget(false);

        jLabel3.setText("Valor:");

        txtHistorico.setName("Histórico"); // NOI18N

        jLabel2.setText("Histórico:");

        btnCalcular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/calculadora.png"))); // NOI18N
        btnCalcular.setToolTipText("Recalcular");

        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimir.setToolTipText("Imprimir Cheque");

        btnIncluir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnIncluir.setToolTipText("Incluir Conta");
        btnIncluir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel4.setText("Nº de Parcelas:");

        txtNumeroParcelas.setName("Nº Parcelas"); // NOI18N

        jLabel5.setText("Valor Parcelas:");

        txtValorParcelas.setName("Valor Parcela"); // NOI18N

        buttonGroup1.add(radioAVista);
        radioAVista.setSelected(true);
        radioAVista.setText("1 Parcela");

        buttonGroup1.add(radioParcelado);
        radioParcelado.setText("Parcelado");

        buttonGroup1.add(radioConformeDisponibilidade);
        radioConformeDisponibilidade.setText("Conforme Disponibilidade");

        jLabel6.setText("Data 1º Pagamento:");

        txtDataPrimeiroPagamento.setName("Data Primeiro Pgto"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtData, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtDataPrimeiroPagamento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                            .addComponent(jLabel4))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtValorParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioAVista)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioParcelado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioConformeDisponibilidade))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnConta)
                            .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addComponent(btnIncluir, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(btnCalcular, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnImprimir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnIncluir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnCalcular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(33, 33, 33)
                            .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(btnConta, javax.swing.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addComponent(jLabel1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtConta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtHistorico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNumeroParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtValorParcelas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(radioAVista)
                            .addComponent(radioParcelado)
                            .addComponent(radioConformeDisponibilidade)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataPrimeiroPagamento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9))
        );

        tabela.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tabela);

        painelDadosContrato.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Data:");

        jLabel8.setText("Valor:");

        jLabel9.setText("Nº Parcelas:");

        jLabel10.setText("Descrição:");

        jLabel11.setText("Código:");

        tabelaPagamentos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tabelaPagamentos.setToolTipText("Clique no pagamento para editá-lo.");
        jScrollPane2.setViewportView(tabelaPagamentos);

        btnVoltar.setText("Voltar");

        btnSalvar.setText("Salvar");

        javax.swing.GroupLayout painelDadosContratoLayout = new javax.swing.GroupLayout(painelDadosContrato);
        painelDadosContrato.setLayout(painelDadosContratoLayout);
        painelDadosContratoLayout.setHorizontalGroup(
            painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosContratoLayout.createSequentialGroup()
                .addGap(273, 273, 273)
                .addComponent(btnSalvar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnVoltar)
                .addGap(12, 12, 12))
            .addGroup(painelDadosContratoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(painelDadosContratoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(txtDataContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCodigoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(painelDadosContratoLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(302, 302, 302))
                    .addGroup(painelDadosContratoLayout.createSequentialGroup()
                        .addComponent(txtDescricao)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(txtParcelasContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtValorContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        painelDadosContratoLayout.setVerticalGroup(
            painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDadosContratoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtDataContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtCodigoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtValorContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtParcelasContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelDadosContratoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVoltar)
                    .addComponent(btnSalvar))
                .addContainerGap())
        );

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setText("Saldo:");

        lblSaldoEmprestimo.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblSaldoEmprestimo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSaldoEmprestimo.setText("0,00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
                    .addComponent(painelDadosContrato, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSaldoEmprestimo, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSaldoEmprestimo)
                    .addComponent(jLabel12))
                .addGap(18, 18, 18)
                .addComponent(painelDadosContrato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCalcular;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnIncluir;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem itemMenuImprimir;
    private javax.swing.JMenuItem itemMenuRemoverSelecionados;
    private javax.swing.JMenuItem itemMenuVisualizarContrato;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblSaldoEmprestimo;
    private javax.swing.JPanel painelDadosContrato;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JRadioButton radioAVista;
    private javax.swing.JRadioButton radioConformeDisponibilidade;
    private javax.swing.JRadioButton radioParcelado;
    private javax.swing.JTable tabela;
    private javax.swing.JTable tabelaPagamentos;
    private javax.swing.JTextField txtCodigoContrato;
    private javax.swing.JTextField txtConta;
    private net.sf.nachocalendar.components.DateField txtData;
    private net.sf.nachocalendar.components.DateField txtDataContrato;
    private net.sf.nachocalendar.components.DateField txtDataPrimeiroPagamento;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtHistorico;
    private javax.swing.JTextField txtNumeroParcelas;
    private javax.swing.JTextField txtParcelasContrato;
    private javax.swing.JTextField txtValor;
    private javax.swing.JTextField txtValorContrato;
    private javax.swing.JTextField txtValorParcelas;
    // End of variables declaration//GEN-END:variables
}
