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

import condominioPlus.Main;
import condominioPlus.apresentacao.DialogoAnotacao;
import condominioPlus.apresentacao.DialogoConselheiro;
import condominioPlus.apresentacao.DialogoTelefone;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.apresentacao.financeiro.DialogoTaloesCheque;
import condominioPlus.negocio.Anotacao;
import condominioPlus.negocio.Banco;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.DadosTalaoCheque;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.Relatorios;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.Identificavel;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.usuario.Usuario;
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
    private TabelaModelo_2<Anotacao> modeloTabelaAnotacoes;
    private List<Anotacao> listaAnotacoes = new ArrayList<Anotacao>();

    /** Creates new form TelaDadosCondominio */
    public TelaDadosCondominio(Condominio condominio) {
        this.condominio = condominio;

        initComponents();

        carregarTabelaTelefone();

        carregarTabelaAnotacoes();

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

    private void carregarTabelaAnotacoes() {
        modeloTabelaAnotacoes = new TabelaModelo_2<Anotacao>(tabelaAnotacoes, "Assunto, Data, Texto, Usuario".split(",")) {

            @Override
            protected List<Anotacao> getCarregarObjetos() {
                return getAnotacoes();
            }

            @Override
            public Object getValor(Anotacao anotacao, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return anotacao.getAssunto();
                    case 1:
                        return DataUtil.getDateTime(anotacao.getData());
                    case 2:
                        return anotacao.getTexto();
                    case 3:
                        return anotacao.getUsuario().getUsuario();
                    default:
                        return null;
                }
            }
        };
    }

    private List<Anotacao> getAnotacoes() {
        listaAnotacoes.clear();
        for (Anotacao a : condominio.getAnotacoes()) {
            if (!a.isCobranca()) {
                listaAnotacoes.add(a);
            }
        }
//        listaAnotacoes = new DAO().listar(Anotacao.class, "AnotacoesCondominio", condominio, false);

        Comparator c = null;

        c = new Comparator() {

            public int compare(Object o1, Object o2) {
                Anotacao a1 = (Anotacao) o1;
                Anotacao a2 = (Anotacao) o2;
                return a1.getData().compareTo(a2.getData());
            }
        };

        Collections.sort(listaAnotacoes, c);

        return listaAnotacoes;
    }

    private void adicionarAnotacao() {
        Anotacao anotacao = DialogoAnotacao.getAnotacao(new Anotacao(condominio), TelaPrincipal.getInstancia(), true);
        if (anotacao.getTexto().equals("")) {
            return;
        }

        if (Main.getFuncionario().getUsuario().getUsuario().equals("")) {
            Usuario usuario = new DAO().localizar(Usuario.class, 50452);
            anotacao.setUsuario(usuario);
        } else {
            anotacao.setUsuario(Main.getFuncionario().getUsuario());
        }

        condominio.adicionarAnotacao(anotacao, false);
        carregarTabelaAnotacoes();
    }

    private void editarAnotacao() {
        Anotacao anotacao = modeloTabelaAnotacoes.getObjetoSelecionado();
        if (anotacao == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione a anotação a ser editada!", this);
            return;
        }
        DialogoAnotacao.getAnotacao(anotacao, TelaPrincipal.getInstancia(), true);
        carregarTabelaAnotacoes();
    }

    private void removerAnotacao() {
        if (modeloTabelaAnotacoes.getLinhaSelecionada() > -1) {
            if (!ApresentacaoUtil.perguntar("Deseja remover o(s) registro(s)?", this)) {
                return;
            }
            System.out.println("removendo... " + modeloTabelaAnotacoes.getLinhasSelecionadas());
            List<Anotacao> itensRemover = modeloTabelaAnotacoes.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Anotacao a : itensRemover) {
                    modeloTabelaAnotacoes.remover(a);
                    //lista auxiliar para não dar erro ao remover o registro da lista de anotações do condominio
                    List<Anotacao> listaAuxiliar = new ArrayList<Anotacao>();
                    for (Anotacao anotacao : condominio.getAnotacoes()) {
                        listaAuxiliar.add(anotacao);
                    }
                    //fim lista auxiliar para não dar erro ao remover o registro da lista de anotações do condominio
                    for (Anotacao o : listaAuxiliar) {
                        if (a.getCodigo() == o.getCodigo()) {
                            condominio.getAnotacoes().remove(a);
                        }
                    }
                    new DAO().remover(a);
                }
            }
            ApresentacaoUtil.exibirInformacao("Anotação(ões) removida(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void imprimirAnotacoes() {
        if (modeloTabelaAnotacoes.getObjetosSelecionados().isEmpty()) {
            List<Anotacao> lista = new DAO().listar(Anotacao.class, "AnotacoesCondominio", condominio, false);
            if (lista.isEmpty()) {
                ApresentacaoUtil.exibirAdvertencia("Não há registros a serem impressos.", this);
            } else {
                new Relatorios().imprimirAnotacoes(condominio, null, lista, TipoRelatorio.ANOTACOES_CONDOMINIO);
            }
        } else {
            new Relatorios().imprimirAnotacoes(condominio, null, modeloTabelaAnotacoes.getObjetosSelecionados(), TipoRelatorio.ANOTACOES_CONDOMINIO);
        }
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
            ApresentacaoUtil.exibirInformacao("Não há unidades cadastradas!", this);
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
        checkBoxSindicoPaga.setSelected(condominio.isSindicoPaga());
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

        //dados certificacao digital
        txtResponsavelLegal.setText(condominio.getResponsavelLegal());
        txtNomeUsuarioCertificacao.setText(condominio.getNomeUsuarioCertificacao());
        txtCpfCertificacao.setText(condominio.getCpfUsuarioCertificacao());
        txtRgCertificacao.setText(condominio.getIdentidadeUsuarioCertificacao());
        txtPisCertificacao.setText(condominio.getPisCertificacao());
        txtDataCertificacao.setValue(DataUtil.getDate(condominio.getDataCertificacao()));
        txtPrazoCertificacao.setValue(DataUtil.getDate(condominio.getPrazoCertificacao()));
        //fim dados certificacao digital

        if (condominio.getContaBancaria().getBanco() != null) {
            txtNumeroBanco.setText(condominio.getContaBancaria().getBanco().getNumeroBanco());
            txtAgencia.setText(condominio.getContaBancaria().getBanco().getAgencia());
            modelo.setSelectedItem(condominio.getContaBancaria().getBanco().getNomeBanco());
        }
        txtContaCorrente.setText(condominio.getContaBancaria().getContaCorrente());
        txtContaPoupanca.setText(condominio.getContaBancaria().getContaPoupanca());
        txtDigitoContaCorrente.setText(condominio.getContaBancaria().getDigitoCorrente());
        txtDigitoContaPoupanca.setText(condominio.getContaBancaria().getDigitoPoupanca());
        txtCodigoCedente.setText(condominio.getContaBancaria().getCodigoCedente());
        txtDigitoCedente.setText(condominio.getContaBancaria().getDigitoCedente());

        txtUsuarioBanking.setText(condominio.getContaBancaria().getUsuarioBanking());
        txtSenhaBanking.setText(condominio.getContaBancaria().getSenhaBanking());
        txtCpfBanking.setText(condominio.getContaBancaria().getCpfBanking());
        txtLimiteBanking.setText(condominio.getContaBancaria().getValor().toString());

//        txtAreaAnotacoes.setText(condominio.getAnotacoes());

        txtNumeroMinimoTaloes.setText(Util.IntegerToString(condominio.getNumeroMinimoTaloes()));

    }

    private void preencherObjeto() {

        condominio.setRazaoSocial(txtRazaoSocial.getText().toUpperCase().trim());
        condominio.setCnpj(txtCnpj.getText());
        condominio.setDataCadastro(DataUtil.getCalendar(txtDataCadastro.getValue()));

        condominio.setAtivo(checkBoxAtivo.isSelected());
        condominio.setSindicoPaga(checkBoxSindicoPaga.isSelected());

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

        //dados certificado digital
        condominio.setResponsavelLegal(txtResponsavelLegal.getText());
        condominio.setNomeUsuarioCertificacao(txtNomeUsuarioCertificacao.getText());
        condominio.setCpfUsuarioCertificacao(txtCpfCertificacao.getText());
        condominio.setIdentidadeUsuarioCertificacao(txtRgCertificacao.getText());
        condominio.setPisCertificacao(txtPisCertificacao.getText());
        condominio.setDataCertificacao(DataUtil.getCalendar(txtDataCertificacao.getValue()));
        condominio.setPrazoCertificacao(DataUtil.getCalendar(txtPrazoCertificacao.getValue()));
        //dados certificado digital 

        if (cmbBanco.getSelectedIndex() != -1) {
            condominio.getContaBancaria().setBanco(modelo.getSelectedItem());
        }
        condominio.getContaBancaria().setContaCorrente(txtContaCorrente.getText());
        condominio.getContaBancaria().setDigitoCorrente(txtDigitoContaCorrente.getText());
        condominio.getContaBancaria().setDigitoPoupanca(txtDigitoContaPoupanca.getText());
        condominio.getContaBancaria().setContaPoupanca(txtContaPoupanca.getText());
        condominio.getContaBancaria().setCodigoCedente(txtCodigoCedente.getText());
        condominio.getContaBancaria().setDigitoCedente(txtDigitoCedente.getText());
        condominio.getContaBancaria().setUsuarioBanking(txtUsuarioBanking.getText().toUpperCase().trim());
        condominio.getContaBancaria().setSenhaBanking(txtSenhaBanking.getText().toUpperCase().trim());
        condominio.getContaBancaria().setCpfBanking(txtCpfBanking.getText());
        condominio.getContaBancaria().setValor(new BigDecimal(txtLimiteBanking.getText().replace(",", ".")));

//        condominio.setAnotacoes(txtAreaAnotacoes.getText());

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
            } else if (e.getSource() == btnAdicionarAnotacao) {
                adicionarAnotacao();
            } else if (e.getSource() == btnEditarAnotacao) {
                editarAnotacao();
            } else if (e.getSource() == btnRemoverAnotacao) {
                removerAnotacao();
            } else if (e.getSource() == btnImprimirAnotacoes) {
                imprimirAnotacoes();
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
            btnAdicionarAnotacao.addActionListener(this);
            btnEditarAnotacao.addActionListener(this);
            btnRemoverAnotacao.addActionListener(this);
            btnImprimirAnotacoes.addActionListener(this);
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
        checkBoxSindicoPaga = new javax.swing.JCheckBox();
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
        painelGeral = new javax.swing.JTabbedPane();
        painelContato = new javax.swing.JPanel();
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
        painelConselheiros = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblConselheiros = new javax.swing.JTable();
        btnAdicionarConselheiro = new javax.swing.JButton();
        btnRemoverConselheiro = new javax.swing.JButton();
        painelMaisDados = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        txtDataCertificacao = new net.sf.nachocalendar.components.DateField();
        txtPrazoCertificacao = new net.sf.nachocalendar.components.DateField();
        txtResponsavelLegal = new javax.swing.JTextField();
        txtNomeUsuarioCertificacao = new javax.swing.JTextField();
        txtRgCertificacao = new javax.swing.JFormattedTextField();
        txtPisCertificacao = new javax.swing.JTextField();
        txtCpfCertificacao = new javax.swing.JFormattedTextField();
        painelDadosBancarios = new javax.swing.JPanel();
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
        jLabel34 = new javax.swing.JLabel();
        txtCodigoCedente = new javax.swing.JTextField();
        txtDigitoCedente = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        txtUsuarioBanking = new javax.swing.JTextField();
        txtLimiteBanking = new javax.swing.JTextField();
        txtSenhaBanking = new javax.swing.JTextField();
        txtCpfBanking = new javax.swing.JTextField();
        painelCaixa = new javax.swing.JPanel();
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
        painelAnotacoes = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tabelaAnotacoes = new javax.swing.JTable();
        btnAdicionarAnotacao = new javax.swing.JButton();
        btnRemoverAnotacao = new javax.swing.JButton();
        btnEditarAnotacao = new javax.swing.JButton();
        btnImprimirAnotacoes = new javax.swing.JButton();
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

        setClosable(true);
        setTitle("Cadastro de Condomínio");
        setPreferredSize(new java.awt.Dimension(722, 510));

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

        txtSindico.setBackground(new java.awt.Color(204, 204, 204));
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

        checkBoxSindicoPaga.setText("Síndico Paga?");

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
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSindico, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(checkBoxAtivo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxSindicoPaga)))
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
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(checkBoxAtivo)
                        .addComponent(checkBoxSindicoPaga)))
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
                    .addComponent(txtRua, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
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
                    .addComponent(txtBairro, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
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
                    .addComponent(txtSite, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                    .addComponent(txtContato, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                    .addComponent(txtZelador, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
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
                .addContainerGap(52, Short.MAX_VALUE))
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

        javax.swing.GroupLayout painelContatoLayout = new javax.swing.GroupLayout(painelContato);
        painelContato.setLayout(painelContatoLayout);
        painelContatoLayout.setHorizontalGroup(
            painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContatoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelContatoLayout.setVerticalGroup(
            painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContatoLayout.createSequentialGroup()
                .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelContatoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(painelContatoLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelContatoLayout.createSequentialGroup()
                .addContainerGap(66, Short.MAX_VALUE)
                .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );

        painelGeral.addTab("Contatos", painelContato);

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

        javax.swing.GroupLayout painelConselheirosLayout = new javax.swing.GroupLayout(painelConselheiros);
        painelConselheiros.setLayout(painelConselheirosLayout);
        painelConselheirosLayout.setHorizontalGroup(
            painelConselheirosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelConselheirosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 561, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(painelConselheirosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdicionarConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(80, 80, 80))
        );
        painelConselheirosLayout.setVerticalGroup(
            painelConselheirosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelConselheirosLayout.createSequentialGroup()
                .addGroup(painelConselheirosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelConselheirosLayout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(btnAdicionarConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRemoverConselheiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelConselheirosLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)))
                .addContainerGap())
        );

        painelGeral.addTab("Conselheiros", painelConselheiros);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel35.setText("Responsável Legal: ");

        jLabel36.setText("Nome Usuário: ");

        jLabel37.setText("CPF: ");

        jLabel38.setText("RG: ");

        jLabel39.setText("NIS/PIS: ");

        jLabel40.setText("Data: ");

        jLabel41.setText("Prazo: ");

        try {
            txtRgCertificacao.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###-#")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        try {
            txtCpfCertificacao.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###.###.###-##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel37)
                            .addComponent(jLabel36)
                            .addComponent(jLabel35))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(txtCpfCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel38)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRgCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtNomeUsuarioCertificacao)
                                .addComponent(txtResponsavelLegal, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel39)
                            .addComponent(jLabel40))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPisCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(txtDataCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel41)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtPrazoCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(200, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(txtResponsavelLegal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNomeUsuarioCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(txtCpfCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38)
                    .addComponent(txtRgCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPisCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel40)
                    .addComponent(txtDataCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrazoCertificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelMaisDadosLayout = new javax.swing.GroupLayout(painelMaisDados);
        painelMaisDados.setLayout(painelMaisDadosLayout);
        painelMaisDadosLayout.setHorizontalGroup(
            painelMaisDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelMaisDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        painelMaisDadosLayout.setVerticalGroup(
            painelMaisDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelMaisDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        painelGeral.addTab("Certificação Digital", painelMaisDados);

        painelBanco.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

        txtNumeroBanco.setBackground(new java.awt.Color(204, 204, 204));
        txtNumeroBanco.setEditable(false);

        txtAgencia.setBackground(new java.awt.Color(204, 204, 204));
        txtAgencia.setEditable(false);

        txtContaCorrente.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtContaPoupanca.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel10.setText("Número do Banco:");

        jLabel11.setText("Agência:");

        jLabel12.setText("Banco:");

        jLabel13.setText("Conta Corrente:");

        jLabel14.setText("Conta Poupança:");

        jLabel34.setText("Cód. Cedente:");

        txtCodigoCedente.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout painelBancoLayout = new javax.swing.GroupLayout(painelBanco);
        painelBanco.setLayout(painelBancoLayout);
        painelBancoLayout.setHorizontalGroup(
            painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelBancoLayout.createSequentialGroup()
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbBanco, 0, 176, Short.MAX_VALUE))
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelBancoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNumeroBanco)
                                    .addComponent(txtAgencia, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)))
                            .addGroup(painelBancoLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel34)
                                    .addComponent(jLabel14))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCodigoCedente, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                    .addComponent(txtContaPoupanca, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                    .addComponent(txtContaCorrente, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDigitoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDigitoContaPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDigitoCedente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)))
                .addGap(40, 40, 40))
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
                    .addComponent(txtAgencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbBanco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelBancoLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtContaPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14)))
                            .addComponent(txtContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(painelBancoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCodigoCedente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel34)
                            .addComponent(txtDigitoCedente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(painelBancoLayout.createSequentialGroup()
                        .addComponent(txtDigitoContaCorrente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDigitoContaPoupanca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
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
                .addContainerGap(56, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelDadosBancariosLayout = new javax.swing.GroupLayout(painelDadosBancarios);
        painelDadosBancarios.setLayout(painelDadosBancariosLayout);
        painelDadosBancariosLayout.setHorizontalGroup(
            painelDadosBancariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDadosBancariosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelBanco, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelDadosBancariosLayout.setVerticalGroup(
            painelDadosBancariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelDadosBancariosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosBancariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(painelBanco, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        painelGeral.addTab("Dados Bancários", painelDadosBancarios);

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
                .addComponent(txtNumeroMinimoTaloes, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
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
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout painelCaixaLayout = new javax.swing.GroupLayout(painelCaixa);
        painelCaixa.setLayout(painelCaixaLayout);
        painelCaixaLayout.setHorizontalGroup(
            painelCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelCaixaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        painelCaixaLayout.setVerticalGroup(
            painelCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelCaixaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelCaixaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        painelGeral.addTab("Caixa", painelCaixa);

        tabelaAnotacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(tabelaAnotacoes);

        btnAdicionarAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarAnotacao.setToolTipText("Adicionar Anotação");
        btnAdicionarAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverAnotacao.setToolTipText("Remover Anotação");
        btnRemoverAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarAnotacao.setToolTipText("Editar Anotação");
        btnEditarAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnImprimirAnotacoes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/Print24.gif"))); // NOI18N
        btnImprimirAnotacoes.setToolTipText("Imprimir Anotação(ões)");

        javax.swing.GroupLayout painelAnotacoesLayout = new javax.swing.GroupLayout(painelAnotacoes);
        painelAnotacoes.setLayout(painelAnotacoesLayout);
        painelAnotacoesLayout.setHorizontalGroup(
            painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelAnotacoesLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnImprimirAnotacoes, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18))
        );
        painelAnotacoesLayout.setVerticalGroup(
            painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelAnotacoesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelAnotacoesLayout.createSequentialGroup()
                        .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEditarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnImprimirAnotacoes))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addContainerGap())
        );

        painelGeral.addTab("Anotações", painelAnotacoes);

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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
                        .addComponent(painelGeral, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 685, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(painelGeral, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionarAnotacao;
    private javax.swing.JButton btnAdicionarConselheiro;
    private javax.swing.JButton btnAdicionarTaloes;
    private javax.swing.JButton btnAdicionarTelefone;
    private javax.swing.JButton btnEditarAnotacao;
    private javax.swing.JButton btnEditarTaloes;
    private javax.swing.JButton btnEditarTelefone;
    private javax.swing.JButton btnImprimirAnotacoes;
    private javax.swing.JButton btnRemoverAnotacao;
    private javax.swing.JButton btnRemoverConselheiro;
    private javax.swing.JButton btnRemoverTaloes;
    private javax.swing.JButton btnRemoverTelefone;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox checkBoxAtivo;
    private javax.swing.JCheckBox checkBoxSindicoPaga;
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
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JLabel lblSite;
    private javax.swing.JPanel painelAnotacoes;
    private javax.swing.JPanel painelBanco;
    private javax.swing.JPanel painelCaixa;
    private javax.swing.JPanel painelConselheiros;
    private javax.swing.JPanel painelContato;
    private javax.swing.JPanel painelDadosBancarios;
    private javax.swing.JTabbedPane painelGeral;
    private javax.swing.JPanel painelMaisDados;
    private javax.swing.JTable tabelaAnotacoes;
    private javax.swing.JTable tbTaloes;
    private javax.swing.JTable tblConselheiros;
    private javax.swing.JTable tblTelefone;
    private javax.swing.JTextField txtAgencia;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JFormattedTextField txtCnpj;
    private javax.swing.JTextField txtCodigoCedente;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtContaCorrente;
    private javax.swing.JTextField txtContaPoupanca;
    private javax.swing.JTextField txtContato;
    private javax.swing.JTextField txtCpfBanking;
    private javax.swing.JFormattedTextField txtCpfCertificacao;
    private net.sf.nachocalendar.components.DateField txtDataCadastro;
    private net.sf.nachocalendar.components.DateField txtDataCertificacao;
    private net.sf.nachocalendar.components.DateField txtDataFechamentoCaixa;
    private javax.swing.JTextField txtDigitoCedente;
    private javax.swing.JTextField txtDigitoContaCorrente;
    private javax.swing.JTextField txtDigitoContaPoupanca;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtLimiteBanking;
    private javax.swing.JTextField txtNomeUsuarioCertificacao;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtNumeroBanco;
    private javax.swing.JTextField txtNumeroMinimoTaloes;
    private javax.swing.JTextField txtPisCertificacao;
    private net.sf.nachocalendar.components.DateField txtPrazoCertificacao;
    private javax.swing.JTextField txtPrimeiroCheque;
    private javax.swing.JTextField txtRazaoSocial;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JTextField txtResponsaveCNPJ;
    private javax.swing.JTextField txtResponsavelCPF;
    private javax.swing.JTextField txtResponsavelCheque;
    private javax.swing.JTextField txtResponsavelLegal;
    private javax.swing.JFormattedTextField txtRgCertificacao;
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
