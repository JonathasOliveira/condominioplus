/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaDadosCondominio.java
 *
 * Created on Aug 6, 2010, 1:06:37 PM
 */
package condominioPlus.apresentacao.advogado;

import condominioPlus.apresentacao.DialogoTelefone;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Advogado;
import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.NotificacaoJudicial;
import condominioPlus.negocio.ProcessoJudicial;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.Identificavel;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.TabelaModelo;

/**
 *
 * @author Administrador
 */
public class TelaDadosAdvogado extends javax.swing.JInternalFrame implements Identificavel<Advogado> {

    private Advogado advogado;
    private ControladorEventos controlador;

    /** Creates new form TelaDadosCondominio */
    public TelaDadosAdvogado(Advogado advogado) {
        this.advogado = advogado;

        initComponents();

        carregarTabelaTelefone();
        carregarTabelaNotificacoes();
        carregarTabelaProcessos();


        controlador = new ControladorEventos();

        controlador.preencher(advogado);

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        return campos;
    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }
            controlador.capturar(advogado);

            TipoAcesso tipo = null;
            if (advogado.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }

            DAO dao = new DAO(false);
            dao.salvar(advogado);
            dao.remover(getModeloTelefone().getObjetosRemovidos());
            dao.concluirTransacao();

            TelaPrincipal.getInstancia().notificarClasse(advogado);

            String descricao = "Cadastro do Advogado " + advogado.getNome() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

            sair();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void sair() {
        this.doDefaultCloseAction();
    }

    private void carregarTabelaTelefone() {
        String[] campos = "Tipo, Número".split(",");

        tblTelefone.setModel(new TabelaModelo<Telefone>(advogado.getTelefones(), campos, tblTelefone) {

            @Override
            public Object getCampo(Telefone telefone, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return telefone.getTipo();
                    case 1:
                        return telefone.getNumero();
                    default:
                        return null;
                }
            }
        });
    }

    private void carregarTabelaNotificacoes() {
        String[] campos = "Condomínio,Unidade,Data Início, Data Término".split(",");

        tblNotificacoes.setModel(new TabelaModelo<NotificacaoJudicial>(advogado.getNotificacaoJudiciais(), campos, tblNotificacoes) {

            @Override
            public Object getCampo(NotificacaoJudicial nf, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return nf.getUnidade().getCondominio().getRazaoSocial();
                    case 1:
                        return nf.getUnidade().getUnidade();
                    case 2:
                        return DataUtil.toString(nf.getData_inicio());
                    case 3:
                        return DataUtil.toString(nf.getData_termino());
                    default:
                        return null;
                }
            }
        });

        tblNotificacoes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblNotificacoes.getColumn(campos[0]).setMinWidth(100);
        tblNotificacoes.getColumn(campos[0]).setPreferredWidth(200);
        tblNotificacoes.getColumn(campos[1]).setMinWidth(50);
        tblNotificacoes.getColumn(campos[1]).setPreferredWidth(50);
     }

    private void carregarTabelaProcessos() {
        String[] campos = "Condomínio,Unidade, Expira em".split(",");

        tblProcessos.setModel(new TabelaModelo<ProcessoJudicial>(advogado.getProcessoJudiciais(), campos, tblProcessos) {

            @Override
            public Object getCampo(ProcessoJudicial pj, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return pj.getUnidade().getCondominio().getRazaoSocial();
                    case 1:
                        return pj.getUnidade().getUnidade();
                    case 2:
                        return DataUtil.toString(pj.getData_processo());
                    default:
                        return null;
                }
            }
        });

        tblNotificacoes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblNotificacoes.getColumn(campos[0]).setMinWidth(100);
        tblNotificacoes.getColumn(campos[0]).setPreferredWidth(200);
        tblNotificacoes.getColumn(campos[1]).setMinWidth(50);
        tblNotificacoes.getColumn(campos[1]).setPreferredWidth(50);
     }

    private TabelaModelo<Telefone> getModeloTelefone() {
        return (TabelaModelo<Telefone>) tblTelefone.getModel();
    }

    private void adicionarTelefone() {
        Telefone telefone = DialogoTelefone.getTelefone(new Telefone(advogado), TelaPrincipal.getInstancia(), true);
        if (telefone == null) {
            return;
        }
        getModeloTelefone().adicionar(telefone);
    }

    private void editarTelefone() {
        Telefone telefone = getModeloTelefone().getObjeto();
        if (telefone == null) {
            return;
        }
        DialogoTelefone.getTelefone(telefone, TelaPrincipal.getInstancia(), true);
        getModeloTelefone().notificarLinha(getModeloTelefone().getObjetos().indexOf(telefone));
    }

    private void removerTelefone() {
        Telefone telefone = getModeloTelefone().getObjeto();
        if (telefone == null) {
            return;
        }
        getModeloTelefone().remover(telefone);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSalvar) {
                salvar();
            } else if (e.getSource() == btnVoltar) {
                sair();
            } else if (e.getSource() == btnAdicionarTelefone) {
                adicionarTelefone();
            } else if (e.getSource() == btnEditarTelefone) {
                editarTelefone();
            } else if (e.getSource() == btnRemoverTelefone) {
                removerTelefone();
            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaDadosAdvogado.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, TelaDadosAdvogado.this, JTextField.class);

            put(Advogado.class, painelDados);
            put(Endereco.class, painelDados);

            btnSalvar.addActionListener(this);
            btnVoltar.addActionListener(this);
            btnAdicionarTelefone.addActionListener(this);
            btnEditarTelefone.addActionListener(this);
            btnRemoverTelefone.addActionListener(this);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel11 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        painelDados = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        txtUf = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtCidade = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        txtNumero1 = new javax.swing.JTextField();
        txtCep = new javax.swing.JFormattedTextField();
        txtComplemento = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        txtBairro = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtRua = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtReferencia = new javax.swing.JTextField();
        txtNumero = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtOab = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTelefone = new javax.swing.JTable();
        btnAdicionarTelefone = new javax.swing.JButton();
        btnEditarTelefone = new javax.swing.JButton();
        btnRemoverTelefone = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblNotificacoes = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblProcessos = new javax.swing.JTable();

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setClosable(true);
        setTitle("Cadastro de Advogados");
        setPreferredSize(new java.awt.Dimension(426, 319));

        jPanel1.setPreferredSize(new java.awt.Dimension(679, 439));

        jPanel12.setLayout(new java.awt.GridBagLayout());

        btnSalvar.setText("Salvar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 262, 11, 0);
        jPanel12.add(btnSalvar, gridBagConstraints);

        btnVoltar.setText("Voltar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 18, 11, 268);
        jPanel12.add(btnVoltar, gridBagConstraints);

        painelDados.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        painelDados.setPreferredSize(new java.awt.Dimension(650, 97));

        jLabel26.setText("Bairro:");

        txtUf.setName("estado"); // NOI18N

        jLabel28.setText("UF:");

        txtCidade.setName("cidade"); // NOI18N

        jLabel24.setText("nº");

        jLabel25.setText("Compl.:");

        txtNumero1.setName("numero"); // NOI18N

        try {
            txtCep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###-###")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtCep.setName("cep"); // NOI18N

        txtComplemento.setToolTipText("");
        txtComplemento.setName("complemento"); // NOI18N

        jLabel23.setText("Endereço:");

        jLabel29.setText("CEP:");

        txtBairro.setName("bairro"); // NOI18N

        jLabel27.setText("Cidade:");

        txtRua.setToolTipText("Digite o Endereço");
        txtRua.setName("logradouro"); // NOI18N

        jLabel2.setText("Referência:");

        txtReferencia.setName("referencia"); // NOI18N

        txtNumero.setName("nome"); // NOI18N

        jLabel1.setText("Nome:");

        jLabel3.setText("OAB:");

        txtOab.setName("numero_ordem"); // NOI18N

        javax.swing.GroupLayout painelDadosLayout = new javax.swing.GroupLayout(painelDados);
        painelDados.setLayout(painelDadosLayout);
        painelDadosLayout.setHorizontalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel27)
                    .addComponent(jLabel2)
                    .addComponent(jLabel25)
                    .addComponent(jLabel23)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelDadosLayout.createSequentialGroup()
                        .addComponent(txtRua, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                        .addComponent(txtNumero1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelDadosLayout.createSequentialGroup()
                            .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jLabel26)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtComplemento))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelDadosLayout.createSequentialGroup()
                            .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtCidade, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtReferencia, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(painelDadosLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel29)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(painelDadosLayout.createSequentialGroup()
                                    .addGap(3, 3, 3)
                                    .addComponent(jLabel28)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(txtNumero, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                    .addComponent(txtOab, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        painelDadosLayout.setVerticalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtOab, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtRua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(txtNumero1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26)
                    .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDados, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addGap(84, 84, 84))
        );

        jTabbedPane1.addTab("Dados", jPanel2);

        tblTelefone.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblTelefone);

        btnAdicionarTelefone.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarTelefone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarTelefone.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarTelefone.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarTelefone.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarTelefone.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarTelefone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarTelefone.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarTelefone.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarTelefone.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverTelefone.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverTelefone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverTelefone.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverTelefone.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverTelefone.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(138, 138, 138)
                        .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Telefones", jPanel3);

        tblNotificacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tblNotificacoes);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Notificações Judiciais", jPanel4);

        tblProcessos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tblProcessos);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Processos Judiciais", jPanel5);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionarTelefone;
    private javax.swing.JButton btnEditarTelefone;
    private javax.swing.JButton btnRemoverTelefone;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel painelDados;
    private javax.swing.JTable tblNotificacoes;
    private javax.swing.JTable tblProcessos;
    private javax.swing.JTable tblTelefone;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtNumero1;
    private javax.swing.JTextField txtOab;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JTextField txtRua;
    private javax.swing.JTextField txtUf;
    // End of variables declaration//GEN-END:variables

    public Advogado getIdentificacao() {
        return advogado;
    }
}
