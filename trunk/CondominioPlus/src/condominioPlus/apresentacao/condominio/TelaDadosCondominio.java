/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaDadosCondominio.java
 *
 * Created on Aug 6, 2010, 1:06:37 PM
 */
package condominioPlus.apresentacao.condominio;

import condominioPlus.apresentacao.DialogoConselheiro;
import condominioPlus.apresentacao.DialogoTelefone;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.apresentacao.financeiro.DialogoTaloesCheque;
import condominioPlus.negocio.Banco;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.DadosTalaoCheque;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.Identificavel;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.TabelaModelo;
import logicpoint.util.Util;

/**
 *
 * @author Administrador
 */
public class TelaDadosCondominio extends javax.swing.JInternalFrame implements Identificavel<Condominio> {

    private Condominio condominio;
    private ControladorEventos controlador;
    private ComboModelo<Banco> modelo;

    /** Creates new form TelaDadosCondominio */
    public TelaDadosCondominio(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        carregarTabelaTelefone();

        carregarComboInstrumento();

        carregarTabelaConselheiros();

        carregarComboBanco();

        carregarTabelaDadosTalao();

        controlador = new ControladorEventos();

        if (this.condominio == null) {
            this.condominio = new Condominio();
        } else {
//            this.setTitle(this.getTitle() + " (" + condominio.getRazaoSocial() + ")");
        }

        if (this.condominio != null) {
            preencherTela(this.condominio);
        } else {
            checkBoxAtivo.setSelected(true);
        }

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtRazaoSocial);
        campos.add(txtCnpj);
        campos.add(checkBoxAtivo);
        return campos;
    }

    private void carregarComboInstrumento() {
        cmbInstrumento.setModel(new ComboModelo<String>(Util.toList(new String[]{"ATA", "PROCURAÇÃO"}), false));
    }

    private void carregarComboBanco() {
        modelo = new ComboModelo<Banco>(new DAO().listar(Banco.class), cmbBanco);
        cmbBanco.setModel(modelo);
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

            DAO dao = new DAO(false);
            dao.salvar(condominio);
            dao.remover(getModeloTelefone().getObjetosRemovidos());
            dao.salvar(getModeloConselheiros().getObjetos());
            dao.remover(getModeloConselheiros().getObjetosRemovidos());
            dao.remover(getModeloTalao().getObjetosRemovidos());
            dao.salvar(getModeloTalao().getObjetos());
            dao.concluirTransacao();

            TelaPrincipal.getInstancia().notificarClasse(condominio);

            String descricao = "Cadastro do Condominio " + condominio.getRazaoSocial() + ".";
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

        tblTelefone.setModel(new TabelaModelo<Telefone>(condominio.getTelefones(), campos, tblTelefone) {

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

    private TabelaModelo<Telefone> getModeloTelefone() {
        return (TabelaModelo<Telefone>) tblTelefone.getModel();
    }

    private TabelaModelo<DadosTalaoCheque> getModeloTalao() {
        return (TabelaModelo<DadosTalaoCheque>) tbTaloes.getModel();
    }

    private void adicionarTelefone() {
        Telefone telefone = DialogoTelefone.getTelefone(new Telefone(condominio), TelaPrincipal.getInstancia(), true);
        if (telefone.getNumero().equals("")) {
            return;
        }
        getModeloTelefone().adicionar(telefone);
    }

    private void editarTelefone() {
        Telefone telefone = getModeloTelefone().getObjeto();
        if (telefone == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o telefone a ser editado!", this);
            return;
        }
        DialogoTelefone.getTelefone(telefone, TelaPrincipal.getInstancia(), true);
        getModeloTelefone().notificarLinha(getModeloTelefone().getObjetos().indexOf(telefone));
    }

    private void removerTelefone() {
        Telefone telefone = getModeloTelefone().getObjeto();
        if (telefone == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o telefone a ser removido!", this);
            return;
        }
        getModeloTelefone().remover(telefone);
    }

    private void carregarTabelaConselheiros() {
        String[] campos = "Unidade, Tipo, Nome".split(",");

        tblConselheiros.setModel(new TabelaModelo<Unidade>(carregarConselheiros(), campos, tblConselheiros) {

            @Override
            public Object getCampo(Unidade u, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return u.getUnidade();
                    case 1:
                        return u.getCondomino().getNome();
                    case 2:
                        return u.getCondomino().getTipoConselheiro();
                    default:
                        return null;
                }
            }
        });

        tblConselheiros.getColumn(campos[0]).setMinWidth(50);
        tblConselheiros.getColumn(campos[1]).setMinWidth(300);
        tblConselheiros.getColumn(campos[2]).setMinWidth(200);

        tblConselheiros.setFont(new Font("Verdana", Font.PLAIN, 11));
    }

    private void carregarTabelaDadosTalao() {
        String[] campos = "Inicial, Final, Status".split(",");

        tbTaloes.setModel(new TabelaModelo<DadosTalaoCheque>(carregarTaloes(), campos, tbTaloes) {

            @Override
            public Object getCampo(DadosTalaoCheque d, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return d.getNumeroInicial();
                    case 1:
                        return d.getNumeroFinal();
                    case 2:
                        return d.getStatus();
                    default:
                        return null;
                }
            }
        });

    }

    private void adicionarTaloes() {
        DadosTalaoCheque dados = DialogoTaloesCheque.getDadosTalao(new DadosTalaoCheque(), null, closable);
        if (dados.getCondominio() == null) {
            return;
        }
        getModeloTalao().adicionar(dados);
    }

    private void editarTaloes() {
        DadosTalaoCheque dados = getModeloTalao().getObjeto();
        if (dados == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o talão a ser editado!", this);
            return;
        }
        DialogoTaloesCheque.getDadosTalao(dados, null, closable);
        getModeloTalao().notificarLinha(getModeloTalao().getObjetos().indexOf(dados));
    }

    private void removerTaloes() {
        DadosTalaoCheque dados = getModeloTalao().getObjeto();
        if (dados == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o talão a ser removido!", this);
            return;
        }
        getModeloTalao().remover(dados);
    }

    private TabelaModelo<Unidade> getModeloConselheiros() {
        return (TabelaModelo<Unidade>) tblConselheiros.getModel();
    }

    private List carregarConselheiros() {
        List<Unidade> unidades = new DAO().listar("ConselheirosPorUnidade", condominio.getCodigo());
        return unidades;
    }

    private List carregarTaloes() {
        List<DadosTalaoCheque> taloes = new DAO().listar("TaloesPorCondominio", condominio.getCodigo());
        return taloes;
    }

    private void adicionarConselheiro() {
        if (condominio.getUnidades().isEmpty()) {
            ApresentacaoUtil.exibirInformacao("Não existem Unidades Cadastradas!", this);
            return;
        }
        boolean ok = DialogoConselheiro.getConselheiro(condominio, TelaPrincipal.getInstancia(), true);
        if (ok) {
            getModeloConselheiros().setObjetos(new DAO().listar("ConselheirosPorUnidade", condominio.getCodigo()));
        }
    }

    private void removerConselheiro() {
        Unidade unidade = getModeloConselheiros().getObjeto();
        if (unidade == null) {
            ApresentacaoUtil.exibirInformacao("Não existe unidade selecionada!", this);
            return;
        }
        unidade.getCondomino().setConselheiro(false);
        new DAO().salvar(unidade);
        getModeloConselheiros().setObjetos(new DAO().listar("ConselheirosPorUnidade", condominio.getCodigo()));
    }

    private void preencherTela(Condominio condominio) {

        txtRazaoSocial.setText(condominio.getRazaoSocial());
        txtCnpj.setText(condominio.getCnpj());
        txtDataCadastro.setValue(DataUtil.getDate(condominio.getDataCadastro().getTimeInMillis()));
        checkBoxAtivo.setSelected(condominio.isAtivo());
        txtSindico.setText(condominio.getSindico());

        if (condominio.getContaCorrente().getDataFechamento() != null) {
            txtDataFechamentoCaixa.setValue(DataUtil.getDate(condominio.getContaCorrente().getDataFechamento()));
        }

        txtRua.setText(condominio.getEndereco().getLogradouro());
        txtNumero.setText(condominio.getEndereco().getNumero());
        txtComplemento.setText(condominio.getEndereco().getComplemento());
        txtReferencia.setText(condominio.getEndereco().getReferencia());
        txtBairro.setText(condominio.getEndereco().getBairro());
        txtCidade.setText(condominio.getEndereco().getCidade());
        txtUf.setText(condominio.getEndereco().getEstado());
        txtCep.setText(condominio.getEndereco().getCep());

        txtContato.setText(condominio.getContato());
        txtZelador.setText(condominio.getZelador());
        txtEmail.setText(condominio.getEmail());
        txtSite.setText(condominio.getSite());

        txtResponsavelCheque.setText(condominio.getResponsavelCheque());
        txtResponsaveCNPJ.setText(condominio.getResponsavelCnpj());
        txtResponsavelCPF.setText(condominio.getResponsavelCpf());
        cmbInstrumento.setSelectedItem(condominio.getInstrumento());

        if (condominio.getContaBancaria().getBanco() != null) {
            txtNumeroBanco.setText(condominio.getContaBancaria().getBanco().getNumeroBanco());
            txtAgencia.setText(condominio.getContaBancaria().getBanco().getAgencia());
            modelo.setSelectedItem(condominio.getContaBancaria().getBanco().getNomeBanco());
        }
        txtContaCorrente.setText(condominio.getContaBancaria().getContaCorrente());
        txtContaPoupanca.setText(condominio.getContaBancaria().getContaPoupanca());
        txtDigitoContaCorrente.setText(condominio.getContaBancaria().getDigitoCorrente());
        txtDigitoContaPoupanca.setText(condominio.getContaBancaria().getDigitoPoupanca());

        txtUsuarioBanking.setText(condominio.getContaBancaria().getUsuarioBanking());
        txtSenhaBanking.setText(condominio.getContaBancaria().getSenhaBanking());
        txtCpfBanking.setText(condominio.getContaBancaria().getCpfBanking());
        txtLimiteBanking.setText(condominio.getContaBancaria().getValor().toString());

        txtAreaAnotacoes.setText(condominio.getAnotacoes());

        txtNumeroMinimoTaloes.setText(Util.IntegerToString(condominio.getNumeroMinimoTaloes()));

    }

    private void preencherObjeto() {

        condominio.setRazaoSocial(txtRazaoSocial.getText().toUpperCase().trim());
        condominio.setCnpj(txtCnpj.getText());
        condominio.setDataCadastro(DataUtil.getCalendar(txtDataCadastro.getValue()));

        condominio.setAtivo(checkBoxAtivo.isSelected());

        condominio.getEndereco().setLogradouro(txtRua.getText().toUpperCase().trim());
        condominio.getEndereco().setNumero(txtNumero.getText().toUpperCase().trim());
        condominio.getEndereco().setComplemento(txtComplemento.getText().toUpperCase().trim());
        condominio.getEndereco().setReferencia(txtReferencia.getText().toUpperCase().trim());
        condominio.getEndereco().setBairro(txtBairro.getText().toUpperCase().trim());
        condominio.getEndereco().setCidade(txtCidade.getText().toUpperCase().trim());
        condominio.getEndereco().setEstado(txtUf.getText().toUpperCase().trim());
        condominio.getEndereco().setCep(txtCep.getText());

        condominio.setContato(txtContato.getText().toUpperCase().trim());
        condominio.setZelador(txtZelador.getText().toUpperCase().trim());
        condominio.setEmail(txtEmail.getText().toUpperCase().trim());
        condominio.setSite(txtSite.getText().toUpperCase().trim());

        condominio.setResponsavelCheque(txtResponsavelCheque.getText().toUpperCase().trim());
        condominio.setResponsavelCnpj(txtResponsaveCNPJ.getText());
        condominio.setResponsavelCpf(txtResponsavelCPF.getText());
        if (cmbInstrumento.getSelectedIndex() < 0) {
            condominio.setInstrumento("");
        } else {
            condominio.setInstrumento(cmbInstrumento.getSelectedItem().toString());
        }

        if (cmbBanco.getSelectedIndex() != -1) {
            condominio.getContaBancaria().setBanco(modelo.getSelectedItem());
        }
        condominio.getContaBancaria().setContaCorrente(txtContaCorrente.getText());
        condominio.getContaBancaria().setDigitoCorrente(txtDigitoContaCorrente.getText());
        condominio.getContaBancaria().setDigitoPoupanca(txtDigitoContaPoupanca.getText());
        condominio.getContaBancaria().setContaPoupanca(txtContaPoupanca.getText());
        condominio.getContaBancaria().setUsuarioBanking(txtUsuarioBanking.getText().toUpperCase().trim());
        condominio.getContaBancaria().setSenhaBanking(txtSenhaBanking.getText().toUpperCase().trim());
        condominio.getContaBancaria().setCpfBanking(txtCpfBanking.getText());
        condominio.getContaBancaria().setValor(new BigDecimal(txtLimiteBanking.getText().replace(",", ".")));

        condominio.setAnotacoes(txtAreaAnotacoes.getText());

        condominio.setNumeroMinimoTaloes(Integer.parseInt(txtNumeroMinimoTaloes.getText()));

        if (condominio.getContaCorrente().getDataFechamento() == null) {
            condominio.getContaCorrente().setDataFechamento(DataUtil.getCalendar(DataUtil.hoje()));
        } else {
            condominio.getContaCorrente().setDataFechamento(DataUtil.getCalendar(txtDataFechamentoCaixa.getValue()));
            System.out.println("data fechamento " + DataUtil.getDateTime(condominio.getContaCorrente().getDataFechamento()));
        }

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
            } else if (e.getSource() == btnAdicionarConselheiro) {
                adicionarConselheiro();
            } else if (e.getSource() == btnRemoverConselheiro) {
                removerConselheiro();
            } else if (e.getSource() == btnAdicionarTaloes) {
                adicionarTaloes();
            } else if (e.getSource() == btnEditarTaloes) {
                editarTaloes();
            } else if (e.getSource() == btnRemoverTaloes) {
                removerTaloes();
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (cmbBanco.getSelectedIndex() != -1) {
                txtAgencia.setText(modelo.getSelectedItem().getAgencia());
                txtNumeroBanco.setText(modelo.getSelectedItem().getNumeroBanco());
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (e.getSource() == painelBanco) {
                if (modelo.getSelectedItem() == null) {
                    carregarComboBanco();
                }


            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaDadosCondominio.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, TelaDadosCondominio.this, JTextField.class);

            btnSalvar.addActionListener(this);
            btnVoltar.addActionListener(this);
            btnAdicionarTelefone.addActionListener(this);
            btnEditarTelefone.addActionListener(this);
            btnRemoverTelefone.addActionListener(this);
            cmbBanco.addItemListener(this);
            btnAdicionarConselheiro.addActionListener(this);
            btnRemoverConselheiro.addActionListener(this);
            painelBanco.addMouseListener(this);
            btnAdicionarTaloes.addActionListener(this);
            btnEditarTaloes.addActionListener(this);
            btnRemoverTaloes.addActionListener(this);
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
        jPanel3 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        checkBoxAtivo = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        txtRazaoSocial = new javax.swing.JTextField();
        txtSindico = new javax.swing.JTextField();
        txtCnpj = new javax.swing.JFormattedTextField();
        txtDataCadastro = new net.sf.nachocalendar.components.DateField();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        txtUf = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtCidade = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        txtCep = new javax.swing.JFormattedTextField();
        txtComplemento = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        txtBairro = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtRua = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtReferencia = new javax.swing.JTextField();
        abas = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        btnAdicionarTelefone = new javax.swing.JButton();
        btnEditarTelefone = new javax.swing.JButton();
        btnRemoverTelefone = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblSite = new javax.swing.JLabel();
        txtContato = new javax.swing.JTextField();
        txtZelador = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtSite = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtResponsavelCheque = new javax.swing.JTextField();
        txtResponsaveCNPJ = new javax.swing.JTextField();
        txtResponsavelCPF = new javax.swing.JTextField();
        cmbInstrumento = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTelefone = new javax.swing.JTable();
        jLabel30 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        painelBanco = new javax.swing.JPanel();
        txtNumeroBanco = new javax.swing.JTextField();
        txtAgencia = new javax.swing.JTextField();
        txtContaCorrente = new javax.swing.JTextField();
        txtContaPoupanca = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        cmbBanco = new javax.swing.JComboBox();
        txtDigitoContaCorrente = new javax.swing.JTextField();
        txtDigitoContaPoupanca = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtUsuarioBanking = new javax.swing.JTextField();
        txtLimiteBanking = new javax.swing.JTextField();
        txtSenhaBanking = new javax.swing.JTextField();
        txtCpfBanking = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblConselheiros = new javax.swing.JTable();
        btnAdicionarConselheiro = new javax.swing.JButton();
        btnRemoverConselheiro = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaAnotacoes = new javax.swing.JTextArea();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        txtDataFechamentoCaixa = new net.sf.nachocalendar.components.DateField();
        jLabel19 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        txtPrimeiroCheque = new javax.swing.JTextField();
        txtUltimoCheque = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        txtNumeroMinimoTaloes = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbTaloes = new javax.swing.JTable();
        btnAdicionarTaloes = new javax.swing.JButton();
        btnEditarTaloes = new javax.swing.JButton();
        btnRemoverTaloes = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();

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

        setTitle("Cadastro de Condomínio");
        setPreferredSize(new java.awt.Dimension(704, 488));

        jPanel1.setPreferredSize(new java.awt.Dimension(679, 439));

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel22.setForeground(new java.awt.Color(255, 51, 51));
        jLabel22.setText("Síndico:");

        jLabel20.setText("Razão Social*");
        jLabel20.setToolTipText("Campo Obrigatório");

        checkBoxAtivo.setText("Condomínio está ativo?");
        checkBoxAtivo.setName("Condomínio ativo"); // NOI18N

        jLabel21.setText("CNPJ:*");
        jLabel21.setToolTipText("Campo Obrigatório");

        txtRazaoSocial.setToolTipText("Digite a Razão Social");
        txtRazaoSocial.setName("Razão Social"); // NOI18N

        txtSindico.setEditable(false);
        txtSindico.setCaretColor(new java.awt.Color(255, 51, 51));
        txtSindico.setName("sindico"); // NOI18N

        try {
            txtCnpj.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtCnpj.setName("CNPJ"); // NOI18N

        jLabel1.setText("Data Cadastro:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtCnpj, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(txtDataCadastro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(txtRazaoSocial, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSindico, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                    .addComponent(checkBoxAtivo))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel21)
                        .addComponent(txtCnpj, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addComponent(txtDataCadastro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxAtivo))
                .addGap(8, 8, 8)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(txtRazaoSocial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(txtSindico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setPreferredSize(new java.awt.Dimension(650, 97));

        jLabel26.setText("Bairro:");

        txtUf.setName("estado"); // NOI18N

        jLabel28.setText("UF:");

        txtCidade.setName("cidade"); // NOI18N

        jLabel24.setText("Número:");

        jLabel25.setText("Compl.:");

        txtNumero.setName("numero"); // NOI18N

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel25)
                    .addComponent(jLabel27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtRua, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel2)
                        .addGap(1, 1, 1)
                        .addComponent(txtReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addComponent(jLabel26)
                    .addComponent(jLabel29))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtBairro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jLabel24)
                    .addComponent(txtRua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(txtReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel27)
                        .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel28)
                        .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel29)
                        .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        jPanel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel3.setText("Contato:");

        jLabel4.setText("Zelador:");

        jLabel5.setText("Email:");

        lblSite.setText("Site:");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(lblSite))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSite, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addComponent(txtContato, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addComponent(txtZelador, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtContato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtZelador, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSite)
                    .addComponent(txtSite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel6.setText("Resp. cheque:");

        jLabel7.setText("Resp. CNPJ:");

        jLabel8.setText("Resp. CPF");

        jLabel9.setText("Instrumento:");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtResponsavelCheque, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtResponsavelCPF, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                                    .addComponent(cmbInstrumento, 0, 131, Short.MAX_VALUE)))))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtResponsaveCNPJ, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtResponsavelCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbInstrumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(9, 9, 9)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtResponsavelCPF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtResponsaveCNPJ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap())
        );

        tblTelefone.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblTelefone);

        jLabel30.setText("Telefones:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        abas.addTab("Contatos", jPanel4);

        painelBanco.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        txtNumeroBanco.setEditable(false);

        txtAgencia.setEditable(false);

        jLabel10.setText("Número do Banco:");

        jLabel11.setText("Agência:");

        jLabel12.setText("Banco:");

        jLabel13.setText("Conta Corrente:");

        jLabel14.setText("Conta Poupança:");

        javax.swing.GroupLayout painelBancoLayout = new javax.swing.GroupLayout(painelBanco);
        painelBanco.setLayout(painelBancoLayout);
        painelBancoLayout.setHorizontalGroup(
            painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBancoLayout.createSequentialGroup()
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel12)
                        .addGap(59, 59, 59)
                        .addComponent(cmbBanco, 0, 176, Short.MAX_VALUE))
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelBancoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNumeroBanco)
                                    .addComponent(txtAgencia, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 54, Short.MAX_VALUE))
                            .addGroup(painelBancoLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(painelBancoLayout.createSequentialGroup()
                                        .addComponent(jLabel14)
                                        .addGap(14, 14, 14)
                                        .addComponent(txtContaPoupanca, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
                                    .addGroup(painelBancoLayout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(18, 18, 18)
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDigitoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDigitoContaPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(55, 55, 55))
        );
        painelBancoLayout.setVerticalGroup(
            painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelBancoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtNumeroBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addComponent(txtDigitoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDigitoContaPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)
                            .addComponent(txtContaPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel13)))
                .addGap(37, 37, 37))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        jLabel15.setText("Usuário Banking:");

        jLabel16.setText("Senha Banking:");

        jLabel17.setText("CPF Banking:");

        jLabel18.setText("Limite:");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel18)
                    .addComponent(jLabel16)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtCpfBanking)
                    .addComponent(txtLimiteBanking)
                    .addComponent(txtSenhaBanking)
                    .addComponent(txtUsuarioBanking, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(txtUsuarioBanking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSenhaBanking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtCpfBanking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtLimiteBanking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelBanco, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        abas.addTab("Dados Bancários", jPanel5);

        tblConselheiros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tblConselheiros);

        btnAdicionarConselheiro.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarConselheiro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarConselheiro.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarConselheiro.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarConselheiro.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverConselheiro.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverConselheiro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverConselheiro.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverConselheiro.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverConselheiro.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdicionarConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(80, 80, 80))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(btnAdicionarConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemoverConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)))
                .addContainerGap())
        );

        abas.addTab("Conselheiros", jPanel6);

        txtAreaAnotacoes.setColumns(20);
        txtAreaAnotacoes.setRows(5);
        jScrollPane2.setViewportView(txtAreaAnotacoes);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 669, Short.MAX_VALUE)
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 649, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        abas.addTab("Anotações", jPanel13);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Dados Caixa"));

        jLabel19.setText("Fechado até:");

        jLabel31.setText("Primeiro Cheque:");

        jLabel32.setText("Último Cheque:");

        jLabel33.setText("Qtde Mínima de Talões:");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel31)
                    .addComponent(jLabel32)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDataFechamentoCaixa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtUltimoCheque)
                    .addComponent(txtPrimeiroCheque, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addGap(34, 34, 34)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNumeroMinimoTaloes, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel33)
                        .addComponent(txtNumeroMinimoTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel19)
                    .addComponent(txtDataFechamentoCaixa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel31)
                    .addComponent(txtPrimeiroCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel32)
                    .addComponent(txtUltimoCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Talões"));

        tbTaloes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(tbTaloes);

        btnAdicionarTaloes.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarTaloes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarTaloes.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarTaloes.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarTaloes.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarTaloes.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarTaloes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarTaloes.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarTaloes.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarTaloes.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverTaloes.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverTaloes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverTaloes.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverTaloes.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverTaloes.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdicionarTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditarTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(btnAdicionarTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditarTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoverTaloes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        abas.addTab("Caixa", jPanel14);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .addComponent(abas, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 664, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(abas, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane abas;
    private javax.swing.JButton btnAdicionarConselheiro;
    private javax.swing.JButton btnAdicionarTaloes;
    private javax.swing.JButton btnAdicionarTelefone;
    private javax.swing.JButton btnEditarTaloes;
    private javax.swing.JButton btnEditarTelefone;
    private javax.swing.JButton btnRemoverConselheiro;
    private javax.swing.JButton btnRemoverTaloes;
    private javax.swing.JButton btnRemoverTelefone;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox checkBoxAtivo;
    private javax.swing.JComboBox cmbBanco;
    private javax.swing.JComboBox cmbInstrumento;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblSite;
    private javax.swing.JPanel painelBanco;
    private javax.swing.JTable tbTaloes;
    private javax.swing.JTable tblConselheiros;
    private javax.swing.JTable tblTelefone;
    private javax.swing.JTextField txtAgencia;
    private javax.swing.JTextArea txtAreaAnotacoes;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JFormattedTextField txtCnpj;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtContaCorrente;
    private javax.swing.JTextField txtContaPoupanca;
    private javax.swing.JTextField txtContato;
    private javax.swing.JTextField txtCpfBanking;
    private net.sf.nachocalendar.components.DateField txtDataCadastro;
    private net.sf.nachocalendar.components.DateField txtDataFechamentoCaixa;
    private javax.swing.JTextField txtDigitoContaCorrente;
    private javax.swing.JTextField txtDigitoContaPoupanca;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtLimiteBanking;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtNumeroBanco;
    private javax.swing.JTextField txtNumeroMinimoTaloes;
    private javax.swing.JTextField txtPrimeiroCheque;
    private javax.swing.JTextField txtRazaoSocial;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JTextField txtResponsaveCNPJ;
    private javax.swing.JTextField txtResponsavelCPF;
    private javax.swing.JTextField txtResponsavelCheque;
    private javax.swing.JTextField txtRua;
    private javax.swing.JTextField txtSenhaBanking;
    private javax.swing.JTextField txtSindico;
    private javax.swing.JTextField txtSite;
    private javax.swing.JTextField txtUf;
    private javax.swing.JTextField txtUltimoCheque;
    private javax.swing.JTextField txtUsuarioBanking;
    private javax.swing.JTextField txtZelador;
    // End of variables declaration//GEN-END:variables

    public Condominio getIdentificacao() {
        return condominio;
    }
}
