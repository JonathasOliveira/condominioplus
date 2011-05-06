/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaExtratoBancario.java
 *
 * Created on 01/04/2011, 10:15:31
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.FormaPagamento;
import condominioPlus.negocio.financeiro.Identificador;
import condominioPlus.negocio.financeiro.Pagamento;
import condominioPlus.negocio.financeiro.arquivoRetorno.EntradaExtratoDiario;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author eugenia
 */
public class TelaExtratoBancario extends javax.swing.JInternalFrame {

    private Condominio condominio;
    private TabelaModelo_2<ExtratoBancario> modeloTabelaExtratoDiario;
    private TabelaModelo_2<ExtratoBancario> modeloTabelaExtratoMensal;
    private TabelaModelo_2<Identificador> modeloTabelaIdentificadores;
    private List<ExtratoBancario> listaExtratoDiario;
    private List<ExtratoBancario> listaExtratoMensal;
    private List<Identificador> listaIdentificadores;

    /** Creates new form TelaExtratoBancario */
    public TelaExtratoBancario(Condominio condominio) {

        this.condominio = condominio;

        initComponents();
        new ControladorEventos();

        carregarTabelas();
        exibirSaldos();

        if (condominio != null) {
            this.setTitle("Extrato Bancário - " + condominio.getRazaoSocial());
        }
    }

    private void carregarTabelas() {
        carregarTabelaExtratoDiario();
        carregarTabelaExtratoMensal();
        carregarTabelaIdentificadores();
    }

    private void exibirSaldos() {
        txtSaldoPoupanca.setText(new Moeda(condominio.getPoupanca().getSaldo()).toString());
        txtSaldoEmprestimo.setText(new Moeda(condominio.getEmprestimo().getSaldo()).toString());
        txtSaldoConsignacao.setText(new Moeda(condominio.getConsignacao().getSaldo()).toString());
        txtSaldoAplicacao.setText(new Moeda(condominio.getAplicacao().getSaldo()).toString());
        txtChequeEspecial.setText(new Moeda(condominio.getContaBancaria().getValor()).toString());

        calcularSaldo();
    }

    private Moeda obterSaldoFinal(List<ExtratoBancario> lista) {
        Moeda valor = new Moeda();

        if (!lista.isEmpty()) {
            valor.soma(lista.get(lista.size() - 1).getValor());
        }

        return valor;
    }

    private Moeda obterChequesACompensar(List<Pagamento> lista) {
        Moeda valor = new Moeda();

        for (Pagamento p : lista) {
            if (p.getForma() == FormaPagamento.CHEQUE) {
                valor.soma(p.getValor());
            }
        }

        return valor;
    }

    private void calcularSaldo() {
        List<ExtratoBancario> listaExtratos = new DAO().listar("ExtratoSaldoFinal", condominio, 2);
        List<Pagamento> listaPagamentos = new DAO().listar("PagamentosContaPagar", condominio.getContaPagar());
        Moeda saldoFinal = new Moeda(obterSaldoFinal(listaExtratos));
        Moeda chequesACompensar = obterChequesACompensar(listaPagamentos);

        Moeda saldo = new Moeda(saldoFinal);
        saldo.soma(chequesACompensar);

        Moeda valorDisponivel = new Moeda(saldo);
        valorDisponivel.soma(condominio.getContaBancaria().getValor());

        txtDisponivel.setText(valorDisponivel.toString());
        txtSaldoBanco.setText(saldoFinal.toString());
        txtChequeACompensar.setText(chequesACompensar.multiplica(-1).toString());
        txtSaldo.setText(saldo.toString());
        txtRecursosDisponiveis.setText(calcularRecursosDisponiveis(valorDisponivel).toString());
    }

    private Moeda calcularRecursosDisponiveis(Moeda valorDisponivel) {
        Moeda valor = new Moeda(valorDisponivel);
        valor.soma(condominio.getPoupanca().getSaldo()).soma(condominio.getAplicacao().getSaldo()).soma(condominio.getConsignacao().getSaldo()).soma(condominio.getEmprestimo().getSaldo());
        return valor;
    }

    private void carregarTabelaExtratoDiario() {
        modeloTabelaExtratoDiario = new TabelaModelo_2<ExtratoBancario>(tabelaExtratoDiario, "Data, Histórico, Doc, Tipo, Valor".split(",")) {

            @Override
            protected List<ExtratoBancario> getCarregarObjetos() {
                return getExtratoDiario();
            }

            @Override
            public Object getValor(ExtratoBancario extratoBancario, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(extratoBancario.getDataPagamento());
                    case 1:
                        return extratoBancario.getHistorico();
                    case 2:
                        return extratoBancario.getDoc();
                    case 3:
                        return extratoBancario.getTipo();
                    case 4:
                        return new Moeda(extratoBancario.getValor());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(0)).setMaxWidth(80);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(2)).setMaxWidth(150);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(3)).setMaxWidth(40);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(4)).setMaxWidth(80);
        tabelaExtratoDiario.getColumn(modeloTabelaExtratoDiario.getCampo(3)).setCellRenderer(centralizado);
    }

    private List<ExtratoBancario> getExtratoDiario() {
        listaExtratoDiario = new DAO().listar("ExtratosPorDia", condominio, DataUtil.getCalendar(new DateTime(DataUtil.hoje()).minusDays(1)));

        Comparator c1 = null;

        c1 = new Comparator() {

            public int compare(Object o1, Object o2) {
                ExtratoBancario e1 = (ExtratoBancario) o1;
                ExtratoBancario e2 = (ExtratoBancario) o2;
                return Integer.valueOf(e1.getIdentificadorRegistro()).compareTo(Integer.valueOf(e2.getIdentificadorRegistro()));
            }
        };

        Collections.sort(listaExtratoDiario, c1);

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                ExtratoBancario e1 = (ExtratoBancario) o1;
                ExtratoBancario e2 = (ExtratoBancario) o2;
                return e1.getDataPagamento().compareTo(e2.getDataPagamento());
            }
        };

        Collections.sort(listaExtratoDiario, c);

        return listaExtratoDiario;
    }

    private void carregarTabelaExtratoMensal() {
        modeloTabelaExtratoMensal = new TabelaModelo_2<ExtratoBancario>(tabelaExtratoMensal, "Data, Histórico, Discriminação da Conta,Doc, Tipo, Valor".split(",")) {

            @Override
            protected List<ExtratoBancario> getCarregarObjetos() {
                return getExtratoMensal();
            }

            @Override
            public Object getValor(ExtratoBancario extratoBancario, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return DataUtil.getDateTime(extratoBancario.getDataPagamento());
                    case 1:
                        return extratoBancario.getHistorico();
                    case 2:
                        return "";
                    case 3:
                        return extratoBancario.getDoc();
                    case 4:
                        return extratoBancario.getTipo();
                    case 5:
                        return new Moeda(extratoBancario.getValor());
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer centralizado = new DefaultTableCellRenderer();
        centralizado.setHorizontalAlignment(SwingConstants.CENTER);

        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(0)).setMaxWidth(80);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(2)).setMinWidth(120);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(3)).setMaxWidth(100);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(4)).setMaxWidth(40);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(5)).setMaxWidth(80);
        tabelaExtratoMensal.getColumn(modeloTabelaExtratoMensal.getCampo(4)).setCellRenderer(centralizado);
    }

    private Calendar pegarUltimoDiaUtilDoMes(Calendar dia) {

        switch (dia.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return DataUtil.getCalendar(new DateTime(dia).minusDays(2));
            case Calendar.SATURDAY:
                return DataUtil.getCalendar(new DateTime(dia).minusDays(1));
            default:
                return dia;
        }

    }

    private List<ExtratoBancario> getExtratoMensal() {
        Calendar dataInicial = pegarUltimoDiaUtilDoMes(DataUtil.getCalendar(new DateTime(DataUtil.getPrimeiroDiaMes()).minusDays(1)));
        Calendar dataFinal = DataUtil.getCalendar(DataUtil.getUltimoDiaMes());
        listaExtratoMensal = new DAO().listar("ExtratosPorMes", condominio, dataInicial, dataFinal);

        Comparator c1 = null;

        c1 = new Comparator() {

            public int compare(Object o1, Object o2) {
                ExtratoBancario e1 = (ExtratoBancario) o1;
                ExtratoBancario e2 = (ExtratoBancario) o2;
                return Integer.valueOf(e1.getIdentificadorRegistro()).compareTo(Integer.valueOf(e2.getIdentificadorRegistro()));
            }
        };

        Collections.sort(listaExtratoMensal, c1);

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                ExtratoBancario e1 = (ExtratoBancario) o1;
                ExtratoBancario e2 = (ExtratoBancario) o2;
                return e1.getDataPagamento().compareTo(e2.getDataPagamento());
            }
        };

        Collections.sort(listaExtratoMensal, c);

        return verificarSaldoInicial(listaExtratoMensal);
    }

    private List<ExtratoBancario> verificarSaldoInicial(List<ExtratoBancario> lista) {
        List<ExtratoBancario> novaLista = new ArrayList<ExtratoBancario>();
        novaLista.addAll(lista);
        for (ExtratoBancario ex : lista) {
            if (DataUtil.compararData(DataUtil.getDateTime(ex.getDataPagamento()), DataUtil.getDateTime(DataUtil.getPrimeiroDiaMes())) == -1) {
                if (ex.getIdentificadorRegistro() != 0) {
                    novaLista.remove(ex);
                }

            }

        }
        return novaLista;

    }

    private void carregarTabelaIdentificadores() {
        modeloTabelaIdentificadores = new TabelaModelo_2<Identificador>(tabelaIdentificadores, "Palavra Chave, Cód. Conta, Conta".split(",")) {

            @Override
            protected List<Identificador> getCarregarObjetos() {
                return getIdentificadores();
            }

            @Override
            public Object getValor(Identificador identificador, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return identificador.getPalavraChave();
                    case 1:
                        return identificador.getConta().getCodigo();
                    case 2:
                        return identificador.getConta().getNome();
                    default:
                        return null;
                }
            }
        };

        DefaultTableCellRenderer esquerda = new DefaultTableCellRenderer();
        esquerda.setHorizontalAlignment(SwingConstants.LEFT);

        tabelaIdentificadores.getColumn(modeloTabelaIdentificadores.getCampo(1)).setCellRenderer(esquerda);

        tabelaIdentificadores.getColumn(modeloTabelaIdentificadores.getCampo(0)).setMinWidth(200);
        tabelaIdentificadores.getColumn(modeloTabelaIdentificadores.getCampo(2)).setMinWidth(200);
    }

    private List<Identificador> getIdentificadores() {
        listaIdentificadores = new DAO().listar(Identificador.class);
        return listaIdentificadores;
    }

    private void lerArquivoExtrato() {
        new EntradaExtratoDiario();
        carregarTabelaExtratoMensal();
        carregarTabelaExtratoDiario();
        exibirSaldos();
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object origem = e.getSource();
            if (origem == btnLerArquivo) {
                lerArquivoExtrato();
            }
        }

        @Override
        public void configurar() {
            btnLerArquivo.addActionListener(this);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        painelExtratoDiario = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaExtratoDiario = new javax.swing.JTable();
        btnLerArquivo = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtSaldoBanco = new javax.swing.JTextField();
        txtChequeACompensar = new javax.swing.JTextField();
        txtSaldo = new javax.swing.JTextField();
        txtChequeEspecial = new javax.swing.JTextField();
        txtDisponivel = new javax.swing.JTextField();
        txtSaldoPoupanca = new javax.swing.JTextField();
        txtSaldoAplicacao = new javax.swing.JTextField();
        txtSaldoConsignacao = new javax.swing.JTextField();
        txtSaldoEmprestimo = new javax.swing.JTextField();
        txtRecursosDisponiveis = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        painelExtratoMensal = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaExtratoMensal = new javax.swing.JTable();
        painelIdentificadores = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaIdentificadores = new javax.swing.JTable();

        setClosable(true);
        setTitle("Extrato Bancário");

        tabelaExtratoDiario.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tabelaExtratoDiario);

        btnLerArquivo.setText("Ler Arquivo");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Disponível:");

        jLabel10.setText("Recursos Disp.:");

        jLabel4.setText("Limite Ch. Esp.:");

        jLabel9.setText("Empréstimos:");

        jLabel3.setText("Saldo:");

        jLabel8.setText("Consignações:");

        jLabel7.setText("Aplicações:");

        jLabel2.setForeground(new java.awt.Color(255, 0, 0));
        jLabel2.setText("Ch. a Comp.:");

        jLabel1.setText("Saldo Banco:");

        jLabel6.setText("Poupança:");

        txtSaldoBanco.setEditable(false);
        txtSaldoBanco.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtChequeACompensar.setEditable(false);
        txtChequeACompensar.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtSaldo.setEditable(false);
        txtSaldo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtChequeEspecial.setEditable(false);
        txtChequeEspecial.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtDisponivel.setEditable(false);
        txtDisponivel.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtSaldoPoupanca.setEditable(false);
        txtSaldoPoupanca.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtSaldoAplicacao.setEditable(false);
        txtSaldoAplicacao.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtSaldoConsignacao.setEditable(false);
        txtSaldoConsignacao.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtSaldoEmprestimo.setEditable(false);
        txtSaldoEmprestimo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtRecursosDisponiveis.setEditable(false);
        txtRecursosDisponiveis.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel11.setText("-");

        jLabel12.setText("=");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSaldoPoupanca)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSaldoBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtChequeACompensar, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addComponent(txtSaldoAplicacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtSaldoConsignacao, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSaldoEmprestimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtRecursosDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(txtSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(txtChequeEspecial, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtDisponivel, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtChequeACompensar, txtChequeEspecial, txtDisponivel, txtRecursosDisponiveis, txtSaldo, txtSaldoAplicacao, txtSaldoBanco, txtSaldoConsignacao, txtSaldoEmprestimo});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSaldoAplicacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSaldoConsignacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSaldoEmprestimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtRecursosDisponiveis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(4, 4, 4)
                                        .addComponent(txtDisponivel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addGap(24, 24, 24))
                                    .addComponent(txtChequeEspecial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel3))
                                        .addGap(4, 4, 4)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(txtChequeACompensar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel12)
                                            .addComponent(txtSaldo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel7)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(jLabel9)
                                        .addComponent(jLabel10))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(4, 4, 4)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtSaldoBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSaldoPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout painelExtratoDiarioLayout = new javax.swing.GroupLayout(painelExtratoDiario);
        painelExtratoDiario.setLayout(painelExtratoDiarioLayout);
        painelExtratoDiarioLayout.setHorizontalGroup(
            painelExtratoDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoDiarioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelExtratoDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnLerArquivo)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        painelExtratoDiarioLayout.setVerticalGroup(
            painelExtratoDiarioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoDiarioLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(btnLerArquivo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Extrato Diário", painelExtratoDiario);

        tabelaExtratoMensal.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tabelaExtratoMensal);

        javax.swing.GroupLayout painelExtratoMensalLayout = new javax.swing.GroupLayout(painelExtratoMensal);
        painelExtratoMensal.setLayout(painelExtratoMensalLayout);
        painelExtratoMensalLayout.setHorizontalGroup(
            painelExtratoMensalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoMensalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelExtratoMensalLayout.setVerticalGroup(
            painelExtratoMensalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelExtratoMensalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Extrato Mensal", painelExtratoMensal);

        tabelaIdentificadores.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(tabelaIdentificadores);

        javax.swing.GroupLayout painelIdentificadoresLayout = new javax.swing.GroupLayout(painelIdentificadores);
        painelIdentificadores.setLayout(painelIdentificadoresLayout);
        painelIdentificadoresLayout.setHorizontalGroup(
            painelIdentificadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelIdentificadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelIdentificadoresLayout.setVerticalGroup(
            painelIdentificadoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelIdentificadoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Identificadores", painelIdentificadores);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Extrato Diário");
        jTabbedPane1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLerArquivo;
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
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelExtratoDiario;
    private javax.swing.JPanel painelExtratoMensal;
    private javax.swing.JPanel painelIdentificadores;
    private javax.swing.JTable tabelaExtratoDiario;
    private javax.swing.JTable tabelaExtratoMensal;
    private javax.swing.JTable tabelaIdentificadores;
    private javax.swing.JTextField txtChequeACompensar;
    private javax.swing.JTextField txtChequeEspecial;
    private javax.swing.JTextField txtDisponivel;
    private javax.swing.JTextField txtRecursosDisponiveis;
    private javax.swing.JTextField txtSaldo;
    private javax.swing.JTextField txtSaldoAplicacao;
    private javax.swing.JTextField txtSaldoBanco;
    private javax.swing.JTextField txtSaldoConsignacao;
    private javax.swing.JTextField txtSaldoEmprestimo;
    private javax.swing.JTextField txtSaldoPoupanca;
    // End of variables declaration//GEN-END:variables
}
