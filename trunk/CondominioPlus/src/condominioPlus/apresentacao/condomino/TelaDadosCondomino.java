/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TelaDadosCondominio.java
 *
 * Created on Aug 6, 2010, 1:06:37 PM
 */
package condominioPlus.apresentacao.condomino;

import condominioPlus.Main;
import condominioPlus.apresentacao.DialogoAnotacao;
import condominioPlus.apresentacao.DialogoEndereco;
import condominioPlus.apresentacao.DialogoTelefone;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Advogado;
import condominioPlus.negocio.Anotacao;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.Inquilino;
import condominioPlus.negocio.NotificacaoJudicial;
import condominioPlus.negocio.ProcessoJudicial;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.relatorios.TipoRelatorio;
import condominioPlus.util.Relatorios;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.usuario.Usuario;
import logicpoint.util.ComboModelo;
import logicpoint.util.DataUtil;
import logicpoint.util.TabelaModelo;

/**
 *
 * @author Administrador
 */
public class TelaDadosCondomino extends javax.swing.JInternalFrame {

    private Unidade unidade;
    private ControladorEventos controlador;
    private Condominio condominio;
    private List<Unidade> unidades;
    private ComboModelo<Advogado> modelo;
    private TabelaModelo_2 modeloTabela;
    private TabelaModelo_2<Anotacao> modeloTabelaAnotacoes;
    private TabelaModelo_2<Endereco> modeloTabelaEnderecoInquilino;
    private TabelaModelo_2<Telefone> modeloTabelaTelefone;
    private TabelaModelo_2<Inquilino> modeloTabelaHistoricoInquilino;
    private List<Anotacao> listaAnotacoes = new ArrayList<Anotacao>();
    private List<Endereco> listaEnderecoInquilino = new ArrayList<Endereco>();
    private List<Telefone> listaTelefoneInquilino = new ArrayList<Telefone>();
    private List<Inquilino> listaInquilinosAntigos = new ArrayList<Inquilino>();

    /** Creates new form TelaDadosCondominio */
    public TelaDadosCondomino(Unidade unidade) {
        this.unidade = unidade;
        this.condominio = unidade.getCondominio();

        initComponents();
        controlador = new ControladorEventos();

        modificarCamposInquilino(false);

        carregarTela();

    }

    private void carregarTela() {
        carregarTabelaTelefone();
        carregarTabelaEndereco();
        carregarTabelaAnotacoes();

        carregarComboAdvogado();

        if (this.unidade != null) {
            verificarCNPJ();
            preencherTela(this.unidade);
        }
    }

    public TelaDadosCondomino(Unidade unidade, TabelaModelo_2 modelo) {
        this.unidade = unidade;
        this.condominio = unidade.getCondominio();
        this.modeloTabela = modelo;

        initComponents();
        controlador = new ControladorEventos();

        boolean inquilino = false;
        if (unidade.getInquilino() != null) {
            inquilino = true;
        }

        modificarCamposInquilino(inquilino);

        carregarTabelaTelefone();
        carregarTabelaEndereco();
        carregarTabelaAnotacoes();
        carregarTabelaHistoricoInquilino();

        carregarComboAdvogado();

        if (this.unidade != null) {
            verificarCNPJ();
            preencherTela(this.unidade);
        }

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtNome);
//        if (!checkBoxCNPJ.isSelected()) {
        campos.add(txtCpf);
//        } else {
//            txtCpf.setName("CNPJ");
//            campos.add(txtCpf);
//        }

        campos.add(txtUnidade);
        campos.add(txtFracaoIdeal);
        campos.add(checkboxAtivo);

        return campos;
    }

    private void verificarSindico() {
//        unidades = new DAO().listar("CondominosPorUnidade", condominio.getCodigo());
        unidades = condominio.getUnidades();
        if (checkBoxSindico.isSelected()) {
            for (Unidade u : unidades) {
                if (u.isSindico() && !u.equals(unidade)) {
                    if (ApresentacaoUtil.perguntar("O atual síndico é: " + u.getCondomino().getNome() + " " + u.getUnidade() + " deseja substituir?", this)) {
                        u.setSindico(false);
                        new DAO().salvar(u);
                        TelaPrincipal.getInstancia().notificarClasse(u);
                        unidade.setSindico(true);
                    } else {
                        checkBoxSindico.setSelected(false);
                    }
                }

            }
        }
    }

    private boolean verificarEnderecoPadrao() {
        boolean value = false;
        for (Endereco e : unidade.getCondomino().getEnderecos()) {
            if (e.isPadrao()) {
                value = true;
            }
        }
        if (!value) {
            ApresentacaoUtil.exibirInformacao("Deve-se marcar um endereço como padrão!", this);
        }

        return value;
    }

    private boolean verificarEnderecoPadraoInquilino() {
        boolean value = false;
        for (Endereco e : unidade.getInquilino().getEnderecos()) {
            if (e.isPadrao()) {
                value = true;
            }
        }
        if (!value) {
            ApresentacaoUtil.exibirInformacao("Deve-se marcar um endereço do inquilino como padrão!", this);
        }
        return value;
    }

    private void carregarComboAdvogado() {
        cmbAdvogado1.setModel(new ComboModelo<Advogado>(new DAO().listar(Advogado.class)));
        modelo = new ComboModelo<Advogado>(new DAO().listar(Advogado.class));
        cmbAdvogado2.setModel(modelo);
    }

    private void salvar() {
        DAO dao = new DAO(false);
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(this);
                return;
            }
            verificarSindico();
            if (!verificarEnderecoPadrao()) {
                return;
            }

            if (!validador.validarDatas(DataUtil.getCalendar(dateInicioJudicial.getValue()), DataUtil.getCalendar(dateFimJudicial.getValue()))) {
                return;
            }

            preencherObjeto();

            if (unidade.getInquilino() != null) {
                if (!verificarEnderecoPadraoInquilino()) {
                    return;
                }
            }

            TipoAcesso tipo = null;
            if (unidade.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }
            if (!checkBoxProcessoJuridico.isSelected() && unidade.getCodigo() != 0) {
                if (unidade.getProcessoJudicial() != null) {
                    dao.remover(unidade.getProcessoJudicial());
                    unidade.setProcessoJudicial(null);
                    ativarProcessoJuridico(false);
                }

            }

            if (!checkboxNotificadoJudicialmente.isSelected() && unidade.getCodigo() != 0) {
                if (unidade.getNotificacaoJudicial() != null) {
                    dao.remover(unidade.getNotificacaoJudicial());
                    unidade.setNotificacaoJudicial(null);
                    ativarNotificacao(false);
                }

            }
            dao.salvar(unidade);
            dao.remover(getModeloTelefone().getObjetosRemovidos());
            dao.remover(getModeloEndereco().getObjetosRemovidos());
            dao.concluirTransacao();

            modeloTabela.carregarObjetos();
            TelaPrincipal.getInstancia().notificarClasse(unidade);
            TelaPrincipal.getInstancia().notificarClasse(condominio);

            String descricao = "Cadastro do Condominio " + unidade.getCondomino().getNome() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

            carregarTela();
//            sair();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }
    }

    private void sair() {
        this.doDefaultCloseAction();
    }

    private void carregarTabelaTelefone() {
        String[] campos = "Tipo, Número".split(",");

        tblTelefone.setModel(new TabelaModelo<Telefone>(unidade.getCondomino().getTelefones(), campos, tblTelefone) {

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

    private void adicionarTelefone() {
        Telefone telefone = DialogoTelefone.getTelefone(new Telefone(unidade.getCondomino()), TelaPrincipal.getInstancia(), true);
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

    private void carregarTabelaEndereco() {
        String[] campos = "Rua, Número, Bairro".split(",");

        tblEndereco.setModel(new TabelaModelo<Endereco>(unidade.getCondomino().getEnderecos(), campos, tblEndereco) {

            @Override
            public Object getCampo(Endereco endereco, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return endereco.getLogradouro();
                    case 1:
                        return endereco.getNumero();
                    case 2:
                        return endereco.getBairro();
                    default:
                        return null;
                }
            }
        });

        tblEndereco.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblEndereco.getColumn(campos[0]).setMinWidth(180);
        tblEndereco.getColumn(campos[2]).setMinWidth(50);
        tblEndereco.getColumn(campos[2]).setMinWidth(150);

        tblEndereco.setFont(new Font("Verdana", Font.PLAIN, 11));


    }

    private TabelaModelo<Endereco> getModeloEndereco() {
        return (TabelaModelo<Endereco>) tblEndereco.getModel();
    }

    private void adicionarEndereco() {
        Endereco endereco = DialogoEndereco.getEndereco(new Endereco(unidade.getCondomino()), TelaPrincipal.getInstancia(), true);
        if (endereco.getLogradouro().equals("")) {
            return;
        }
        getModeloEndereco().adicionar(endereco);
        preencherPainelEndereco();
    }

    private void editarEndereco() {
        Endereco endereco = getModeloEndereco().getObjeto();
        if (endereco == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o endereço a ser editado!", this);
            return;
        }
        DialogoEndereco.getEndereco(endereco, TelaPrincipal.getInstancia(), true);
        getModeloTelefone().notificarLinha(getModeloEndereco().getObjetos().indexOf(endereco));
    }

    private void removerEndereco() {

        if (getModeloEndereco().getObjeto() != null) {
            if (getModeloEndereco().getObjeto().isPadrao()) {
                if (unidade.getCondomino().getEnderecos().size() > 1) {
                    for (Endereco e : unidade.getCondomino().getEnderecos()) {
                        if (!e.equals(getModeloEndereco().getObjeto())) {
                            e.setPadrao(true);
                            getModeloEndereco().remover(getModeloEndereco().getObjeto());
                            preencherTela(unidade);
                        }
                    }

                } else {
                    ApresentacaoUtil.exibirAdvertencia("Adicione um novo endereço padrão ou edite esse!", this);
                }
            }

        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione o endereço a ser removido!", this);
        }

    }

    private void preencherPainelEndereco() {

        for (Endereco e : unidade.getCondomino().getEnderecos()) {
            if (e.isPadrao()) {
                txtRua.setText(e.getLogradouro());
                txtNumero.setText(e.getNumero());
                txtComplemento.setText(e.getComplemento());
                txtReferencia.setText(e.getReferencia());
                txtBairro.setText(e.getBairro());
                txtCidade.setText(e.getCidade());
                txtUf.setText(e.getEstado());
                txtCep.setText(e.getCep());
            }
        }

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
        listaAnotacoes = unidade.getCondomino().getAnotacoes();

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
        Anotacao anotacao = DialogoAnotacao.getAnotacao(new Anotacao(unidade.getCondomino()), TelaPrincipal.getInstancia(), true);
        if (anotacao.getTexto().equals("")) {
            return;
        }

        if (Main.getFuncionario().getUsuario().getUsuario().equals("")) {
            Usuario usuario = new DAO().localizar(Usuario.class, 50452);
            anotacao.setUsuario(usuario);
        } else {
            anotacao.setUsuario(Main.getFuncionario().getUsuario());
        }

        unidade.getCondomino().adicionarAnotacao(anotacao);
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
            if (!ApresentacaoUtil.perguntar("Desejar remover o(s) registro(s)?", this)) {
                return;
            }
            System.out.println("removendo... " + modeloTabelaAnotacoes.getLinhasSelecionadas());
            List<Anotacao> itensRemover = modeloTabelaAnotacoes.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Anotacao a : itensRemover) {
                    modeloTabelaAnotacoes.remover(a);
                    for (Anotacao o : unidade.getCondomino().getAnotacoes()) {
                        if (a.getCodigo() == o.getCodigo()) {
                            unidade.getCondomino().getAnotacoes().remove(a);
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
            if (unidade.getCondomino().getAnotacoes().isEmpty()) {
                ApresentacaoUtil.exibirAdvertencia("Não há registros a serem impressos.", this);
            } else {
                new Relatorios().imprimirAnotacoes(condominio, unidade, unidade.getCondomino().getAnotacoes(), TipoRelatorio.ANOTACOES_CONDOMINO);
            }
        } else {
            new Relatorios().imprimirAnotacoes(condominio, unidade, modeloTabelaAnotacoes.getObjetosSelecionados(), TipoRelatorio.ANOTACOES_CONDOMINO);
        }
    }

    private void carregarTabelaTelefoneInquilino() {
        modeloTabelaTelefone = new TabelaModelo_2<Telefone>(tblTelefoneInquilino, "Tipo, Número".split(",")) {

            @Override
            protected List<Telefone> getCarregarObjetos() {
                return getTelefoneInquilino();
            }

            @Override
            public Object getValor(Telefone telefone, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return telefone.getTipo();
                    case 1:
                        return telefone.getNumero();
                    default:
                        return null;
                }
            }
        };
    }

    private List<Telefone> getTelefoneInquilino() {
        listaTelefoneInquilino = unidade.getInquilino().getTelefones();
        return listaTelefoneInquilino;
    }

    private void adicionarTelefoneInquilino() {
        Telefone telefone = DialogoTelefone.getTelefone(new Telefone(unidade.getInquilino()), TelaPrincipal.getInstancia(), true);
        if (telefone.getNumero().equals("")) {
            return;
        }
        if (unidade.getInquilino() == null) {
            unidade.setInquilino(new Inquilino());
        }
        unidade.getInquilino().adicionarTelefone(telefone);
        carregarTabelaTelefoneInquilino();
    }

    private void editarTelefoneInquilino() {
        Telefone telefone = modeloTabelaTelefone.getObjetoSelecionado();
        if (telefone == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o registro a ser editado!", this);
            return;
        }
        DialogoTelefone.getTelefone(telefone, TelaPrincipal.getInstancia(), true);
        carregarTabelaTelefoneInquilino();
    }

    private void removerTelefoneInquilino() {
        if (modeloTabelaTelefone.getLinhaSelecionada() > -1) {
            if (!ApresentacaoUtil.perguntar("Desejar remover o(s) registro(s)?", this)) {
                return;
            }
            System.out.println("removendo... " + modeloTabelaTelefone.getLinhasSelecionadas());
            List<Telefone> itensRemover = modeloTabelaTelefone.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Telefone t : itensRemover) {
                    modeloTabelaTelefone.remover(t);
                    for (Telefone o : unidade.getInquilino().getTelefones()) {
                        if (t.getCodigo() == o.getCodigo()) {
                            unidade.getInquilino().getTelefones().remove(t);
                        }
                    }
                    new DAO().remover(t);
                }
            }
            ApresentacaoUtil.exibirInformacao("Registro(s) removido(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void carregarTabelaEnderecoInquilino() {
        modeloTabelaEnderecoInquilino = new TabelaModelo_2<Endereco>(tblEnderecoInquilino, "Rua, Número, Bairro".split(",")) {

            @Override
            protected List<Endereco> getCarregarObjetos() {
                return getEnderecoInquilino();
            }

            @Override
            public Object getValor(Endereco endereco, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return endereco.getLogradouro();
                    case 1:
                        return endereco.getNumero();
                    case 2:
                        return endereco.getBairro();
                    default:
                        return null;
                }
            }
        };
    }

    private List<Endereco> getEnderecoInquilino() {
        listaEnderecoInquilino = unidade.getInquilino().getEnderecos();
        return listaEnderecoInquilino;
    }

    private void adicionarEnderecoInquilino() {
        Endereco endereco = DialogoEndereco.getEndereco(new Endereco(unidade.getInquilino()), TelaPrincipal.getInstancia(), true);
        if (endereco.getLogradouro().equals("")) {
            return;
        }
        if (unidade.getInquilino() == null) {
            unidade.setInquilino(new Inquilino());
        }
        unidade.getInquilino().adicionarEndereco(endereco);
        carregarTabelaEnderecoInquilino();
    }

    private void editarEnderecoInquilino() {
        Endereco endereco = modeloTabelaEnderecoInquilino.getObjetoSelecionado();
        if (endereco == null) {
            ApresentacaoUtil.exibirAdvertencia("Selecione o registro a ser editado!", this);
            return;
        }
        DialogoEndereco.getEndereco(endereco, TelaPrincipal.getInstancia(), true);
        carregarTabelaEnderecoInquilino();
    }

    private void removerEnderecoInquilino() {
        if (modeloTabelaEnderecoInquilino.getLinhaSelecionada() > -1) {
            if (!ApresentacaoUtil.perguntar("Desejar remover o(s) registro(s)?", this)) {
                return;
            }
            System.out.println("removendo... " + modeloTabelaEnderecoInquilino.getLinhasSelecionadas());
            List<Endereco> itensRemover = modeloTabelaEnderecoInquilino.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Endereco e : itensRemover) {
                    modeloTabelaEnderecoInquilino.remover(e);
                    for (Endereco o : unidade.getInquilino().getEnderecos()) {
                        if (e.getId() == o.getId()) {
                            unidade.getInquilino().getEnderecos().remove(e);
                        }
                    }
                    new DAO().remover(e);
                }
            }
            ApresentacaoUtil.exibirInformacao("Registro(s) removido(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void carregarTabelaHistoricoInquilino() {
        modeloTabelaHistoricoInquilino = new TabelaModelo_2<Inquilino>(tabelaHistoricoInquilino, "Nome, CPF, RG".split(",")) {

            @Override
            protected List<Inquilino> getCarregarObjetos() {
                return getHistoricoInquilino();
            }

            @Override
            public Object getValor(Inquilino inquilino, int indiceColuna) {
                switch (indiceColuna) {
                    case 0:
                        return inquilino.getNome();
                    case 1:
                        return inquilino.getCpf();
                    case 2:
                        return inquilino.getRg();
                    default:
                        return null;
                }
            }
        };
    }

    private List<Inquilino> getHistoricoInquilino() {
        listaInquilinosAntigos = new DAO().listar(Inquilino.class, "InquilinoPorUnidade", unidade.getCodigo());
        return listaInquilinosAntigos;
    }

//    private void editarTelefoneInquilino() {
//        Telefone telefone = modeloTabelaTelefone.getObjetoSelecionado();
//        if (telefone == null) {
//            ApresentacaoUtil.exibirAdvertencia("Selecione o registro a ser editado!", this);
//            return;
//        }
//        DialogoTelefone.getTelefone(telefone, TelaPrincipal.getInstancia(), true);
//        carregarTabelaTelefoneInquilino();
//    }
    private void removerInquilinoHistorico() {
        if (modeloTabelaHistoricoInquilino.getLinhaSelecionada() > -1) {
            if (!ApresentacaoUtil.perguntar("Desejar remover o(s) registro(s)?", this)) {
                return;
            }
            System.out.println("removendo... " + modeloTabelaHistoricoInquilino.getLinhasSelecionadas());
            List<Inquilino> itensRemover = modeloTabelaHistoricoInquilino.getObjetosSelecionados();
            if (!itensRemover.isEmpty()) {
                for (Inquilino i : itensRemover) {
                    modeloTabelaHistoricoInquilino.remover(i);
                    new DAO().remover(i);
                }
            }
            ApresentacaoUtil.exibirInformacao("Registro(s) removido(s) com sucesso!", this);
        } else {
            ApresentacaoUtil.exibirAdvertencia("Selecione pelo menos um registro para removê-lo!", this);
        }
    }

    private void preencherTela(Unidade unidade) {

        txtCondominio.setText(unidade.getCondominio().getRazaoSocial());
        txtNome.setText(unidade.getCondomino().getNome());
        txtCpf.setText(unidade.getCondomino().getCpf());
        txtRg.setText(unidade.getCondomino().getRg());
        txtUnidade.setText(unidade.getUnidade());
        checkBoxFalecido.setSelected(unidade.getCondomino().isFalecido());

        for (Endereco e : unidade.getCondomino().getEnderecos()) {
            if (e.isPadrao()) {
                txtRua.setText(e.getLogradouro());
                txtNumero.setText(e.getNumero());
                txtComplemento.setText(e.getComplemento());
                txtReferencia.setText(e.getReferencia());
                txtBairro.setText(e.getBairro());
                txtCidade.setText(e.getCidade());
                txtUf.setText(e.getEstado());
                txtCep.setText(e.getCep());
            }
        }
        txtEmail.setText(unidade.getCondomino().getEmail());
//        txtAnotacoes.setText(unidade.getCondomino().getAnotacoes());

        if (unidade.getInquilino() != null) {
            checkBoxInquilino.setSelected(true);
            txtNomeInquilino.setText(unidade.getInquilino().getNome());
            txtCpfInquilino.setText(unidade.getInquilino().getCpf());
            txtRgInquilino.setText(unidade.getInquilino().getRg());
            carregarTabelaTelefoneInquilino();
            carregarTabelaEnderecoInquilino();
        }

        txtFracaoIdeal.setText(String.valueOf(unidade.getFracaoIdeal()));
        txtIptu.setText(unidade.getIptu());
        txtBloco.setText(unidade.getBloco());
        txtCoeficiente.setText(unidade.getCoeficiente());
        txtValorPrincipal.setText(unidade.getValorPrincipal().toString().replace(".", ","));

        checkBoxSindico.setSelected(unidade.isSindico());
        checkboxAtivo.setSelected(unidade.isAtivo());
        checkboxCartaCobranca.setSelected(unidade.isBloquearImpressaoCartaCobranca());
        checkboxImpressaoCertificado.setSelected(unidade.isBloquearImpressaoCertificado());
        checkboxImpressaoCobranca.setSelected(unidade.isBloquearImpressaoCobranca());

        if (unidade.getNotificacaoJudicial() != null) {
            ativarNotificacao(true);
            checkboxNotificadoJudicialmente.setSelected(true);
            modelo.setSelectedItem(unidade.getNotificacaoJudicial().getAdvogado());
            dateInicioJudicial.setValue(DataUtil.getDate(unidade.getNotificacaoJudicial().getData_inicio().getTimeInMillis()));
            dateFimJudicial.setValue(DataUtil.getDate(unidade.getNotificacaoJudicial().getData_termino().getTimeInMillis()));
        }

        if (unidade.getProcessoJudicial() != null) {
            ativarProcessoJuridico(true);
            checkBoxProcessoJuridico.setSelected(true);
            cmbAdvogado1.setSelectedItem(unidade.getProcessoJudicial().getAdvogado());
            dateProcessoJuridico.setValue(DataUtil.getDate(unidade.getProcessoJudicial().getData_processo().getTimeInMillis()));
            txtNumeroProcesso.setText(unidade.getProcessoJudicial().getNumero_processo());

        }

        //processos com datas
    }

    private void ativarProcessoJuridico(boolean valor) {
        cmbAdvogado1.setEnabled(valor);
        dateProcessoJuridico.setEnabled(valor);
    }

    private void ativarNotificacao(boolean valor) {
        cmbAdvogado2.setEnabled(valor);
        dateInicioJudicial.setEnabled(valor);
        dateFimJudicial.setEnabled(valor);
    }

    private void verificarCNPJ() {
        if (unidade.getCondomino().isCnpj()) {
            checkBoxCNPJ.setSelected(true);
            lblCpf.setText("CNPJ*");
            txtRg.setEditable(false);
            txtRg.setBackground(new Color(204, 204, 204));
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }
        } else {
            lblCpf.setText("CPF*");
            txtRg.setEditable(true);
            txtRg.setBackground(new Color(240, 240, 240));
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###.###.###-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void cnpjSelecionado() {
        if (checkBoxCNPJ.isSelected()) {
            lblCpf.setText("CNPJ*");
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }
            txtRg.setEditable(false);
            txtRg.setBackground(new Color(204, 204, 204));
        } else {
            lblCpf.setText("CPF*");
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###.###.###-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }
            txtRg.setEditable(true);
            txtRg.setBackground(new Color(240, 240, 240));
        }
    }

    private void recuperarOriginal() {
        if (checkBoxCNPJ.isSelected() && unidade.getCondomino().isCnpj()) {
            txtCpf.setText(unidade.getCondomino().getCpf());
        } else if (!checkBoxCNPJ.isSelected() && !unidade.getCondomino().isCnpj()) {
            txtCpf.setText(unidade.getCondomino().getCpf());
        }
    }

    private void preencherObjeto() {

        unidade.getCondomino().setNome(txtNome.getText().trim().toUpperCase());
        unidade.getCondomino().setCpf(txtCpf.getText());
        unidade.getCondomino().setRg(txtRg.getText());
        unidade.setUnidade(txtUnidade.getText().trim().toUpperCase());
        unidade.getCondomino().setFalecido(checkBoxFalecido.isSelected());
        unidade.getCondomino().setCnpj(checkBoxCNPJ.isSelected());

        unidade.getCondomino().setEmail(txtEmail.getText());
//        unidade.getCondomino().setAnotacoes(txtAnotacoes.getText());

        if (checkBoxInquilino.isSelected()) {
            if (unidade.getInquilino() == null) {
                unidade.setInquilino(new Inquilino());
            }
            unidade.getInquilino().setNome(txtNomeInquilino.getText());
            unidade.getInquilino().setCpf(txtCpfInquilino.getText());
            unidade.getInquilino().setRg(txtRgInquilino.getText());
        } else {
            if (unidade.getInquilino() != null) {
                Inquilino inquilino = unidade.getInquilino();
                inquilino.setCodigoUnidade(unidade.getCodigo());
                new DAO().salvar(inquilino);
                unidade.setInquilino(null);
            }
        }

        unidade.setFracaoIdeal(Double.parseDouble(txtFracaoIdeal.getText()));
        unidade.setIptu(txtIptu.getText());
        unidade.setBloco(txtBloco.getText());
        unidade.setCoeficiente(txtCoeficiente.getText());
        unidade.setValorPrincipal(new BigDecimal(txtValorPrincipal.getText().replace(",", ".")));

        unidade.setSindico(checkBoxSindico.isSelected());
        unidade.setAtivo(checkboxAtivo.isSelected());
        unidade.setBloquearImpressaoCartaCobranca(checkboxCartaCobranca.isSelected());
        unidade.setBloquearImpressaoCertificado(checkboxImpressaoCertificado.isSelected());
        unidade.setBloquearImpressaoCobranca(checkboxImpressaoCobranca.isSelected());

        if (checkboxNotificadoJudicialmente.isSelected()) {
            if (unidade.getNotificacaoJudicial() == null) {
                NotificacaoJudicial nf = new NotificacaoJudicial();
                nf.setAdvogado(modelo.getSelectedItem());
                nf.setUnidade(unidade);
                nf.setData_inicio(DataUtil.getCalendar(dateInicioJudicial.getValue()));
                nf.setData_termino(DataUtil.getCalendar(dateFimJudicial.getValue()));
                unidade.setNotificacaoJudicial(nf);
            } else {
                NotificacaoJudicial nf = unidade.getNotificacaoJudicial();
                nf.setAdvogado(modelo.getSelectedItem());
                nf.setUnidade(unidade);
                nf.setData_inicio(DataUtil.getCalendar(dateInicioJudicial.getValue()));
                nf.setData_termino(DataUtil.getCalendar(dateFimJudicial.getValue()));

            }
        }

        if (checkBoxProcessoJuridico.isSelected()) {

            if (unidade.getProcessoJudicial() != null) {
                ProcessoJudicial pj = unidade.getProcessoJudicial();
                pj.setAdvogado((Advogado) cmbAdvogado1.getSelectedItem());
                pj.setUnidade(unidade);
                pj.setData_processo(DataUtil.getCalendar(dateProcessoJuridico.getValue()));
                pj.setNumero_processo(txtNumeroProcesso.getText());
            } else {
                ProcessoJudicial pj = new ProcessoJudicial();
                pj.setAdvogado((Advogado) cmbAdvogado1.getSelectedItem());
                pj.setUnidade(unidade);
                pj.setData_processo(DataUtil.getCalendar(dateProcessoJuridico.getValue()));
                pj.setNumero_processo(txtNumeroProcesso.getText());
                unidade.setProcessoJudicial(pj);
            }
        }
    }

    private void teste() {
        String comando = "C:/Arquivos de programas/Internet Explorer/IEXPLORE.EXE http://www.tjrj.jus.br/";
        Clipboard teclado = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selecao = new StringSelection(txtNumeroProcesso.getText());
        teclado.setContents(selecao, null);
        try {
            Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + comando);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Ocorreu um erro ao carregar o Browser", "Internet Explorer", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void modificarCamposInquilino(boolean valor) {
        txtNomeInquilino.setEnabled(valor);
        txtCpfInquilino.setEnabled(valor);
        txtRgInquilino.setEnabled(valor);
        tblEnderecoInquilino.setEnabled(valor);
        btnAdicionarEnderecoInquilino.setEnabled(valor);
        btnEditarEnderecoInquilino.setEnabled(valor);
        btnRemoverEnderecoInquilino.setEnabled(valor);
        tblTelefoneInquilino.setEnabled(valor);
        btnAdicionarTelefoneInquilino.setEnabled(valor);
        btnEditarTelefoneInquilino.setEnabled(valor);
        btnRemoverTelefoneInquilino.setEnabled(valor);
//        txtRuaInquilino.setEnabled(valor);
//        txtNumeroInquilino.setEnabled(valor);
//        txtComplementoInquilino.setEnabled(valor);
//        txtReferenciaInquilino.setEnabled(valor);
//        txtBairroInquilino.setEnabled(valor);
//        txtCidadeInquilino.setEnabled(valor);
//        txtUfInquilino.setEnabled(valor);
//        txtCepInquilino.setEnabled(valor);
        if (valor == false) {
            Color color = new Color(204, 204, 204);
            txtNomeInquilino.setBackground(color);
            txtCpfInquilino.setBackground(color);
            txtRgInquilino.setBackground(color);
//            txtRuaInquilino.setBackground(color);
//            txtNumeroInquilino.setBackground(color);
//            txtComplementoInquilino.setBackground(color);
//            txtReferenciaInquilino.setBackground(color);
//            txtBairroInquilino.setBackground(color);
//            txtCidadeInquilino.setBackground(color);
//            txtUfInquilino.setBackground(color);
//            txtCepInquilino.setBackground(color);
        } else {
            Color color = new Color(240, 240, 240);
            txtNomeInquilino.setBackground(color);
            txtCpfInquilino.setBackground(color);
            txtRgInquilino.setBackground(color);
//            txtRuaInquilino.setBackground(color);
//            txtNumeroInquilino.setBackground(color);
//            txtComplementoInquilino.setBackground(color);
//            txtReferenciaInquilino.setBackground(color);
//            txtBairroInquilino.setBackground(color);
//            txtCidadeInquilino.setBackground(color);
//            txtUfInquilino.setBackground(color);
//            txtCepInquilino.setBackground(color);
        }
    }

    private void exibirDetalheInquilino() {
        DialogoDetalheInquilino dialogo = new DialogoDetalheInquilino(modeloTabelaHistoricoInquilino.getObjetoSelecionado(), null, true);
        dialogo.setVisible(true);
    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSalvar) {
                salvar();
            } else if (e.getSource() == btnVoltar) {
                sair();
            } else if (e.getSource() == btnAdicionarEndereco) {
                adicionarEndereco();
            } else if (e.getSource() == btnEditarEndereco) {
                editarEndereco();
            } else if (e.getSource() == btnRemoverEndereco) {
                removerEndereco();
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
            } else if (e.getSource() == checkBoxInquilino) {
                boolean selecionado = checkBoxInquilino.isSelected();
                if (!selecionado) {
                    modificarCamposInquilino(false);
                } else {
                    modificarCamposInquilino(true);
                }
            } else if (e.getSource() == btnAdicionarEnderecoInquilino) {
                adicionarEnderecoInquilino();
            } else if (e.getSource() == btnEditarEnderecoInquilino) {
                editarEnderecoInquilino();
            } else if (e.getSource() == btnRemoverTelefoneInquilino) {
                removerEnderecoInquilino();
            } else if (e.getSource() == btnAdicionarTelefoneInquilino) {
                adicionarTelefoneInquilino();
            } else if (e.getSource() == btnEditarTelefoneInquilino) {
                editarTelefoneInquilino();
            } else if (e.getSource() == btnRemoverTelefoneInquilino) {
                removerTelefoneInquilino();
            } else if (e.getSource() == btnRemoverInquilino) {
                removerInquilinoHistorico();
            } else if (e.getSource() == checkBoxCNPJ) {
                txtCpf.setValue(null);
                txtCpf.grabFocus();
                cnpjSelecionado();
                recuperarOriginal();
            } else if (e.getSource() == checkboxNotificadoJudicialmente) {
                boolean selecionado = checkboxNotificadoJudicialmente.isSelected();
                ativarNotificacao(selecionado);
            } else if (e.getSource() == checkBoxProcessoJuridico) {
                boolean selecionado = checkBoxProcessoJuridico.isSelected();
                ativarProcessoJuridico(selecionado);
            } else if (e.getSource() == btnTeste) {
                teste();
            } else if (e.getSource() == itemMenuDetalhe) {
                exibirDetalheInquilino();
            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, TelaDadosCondomino.this, JTextField.class, JComboBox.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, TelaDadosCondomino.this, JTextField.class);

            btnSalvar.addActionListener(this);
            btnVoltar.addActionListener(this);
            btnAdicionarEndereco.addActionListener(this);
            btnEditarEndereco.addActionListener(this);
            btnRemoverEndereco.addActionListener(this);
            btnAdicionarTelefone.addActionListener(this);
            btnEditarTelefone.addActionListener(this);
            btnRemoverTelefone.addActionListener(this);
            btnAdicionarAnotacao.addActionListener(this);
            btnEditarAnotacao.addActionListener(this);
            btnRemoverAnotacao.addActionListener(this);
            btnImprimirAnotacoes.addActionListener(this);
            checkBoxInquilino.addActionListener(this);
            btnAdicionarEnderecoInquilino.addActionListener(this);
            btnEditarEnderecoInquilino.addActionListener(this);
            btnRemoverEnderecoInquilino.addActionListener(this);
            btnAdicionarTelefoneInquilino.addActionListener(this);
            btnEditarTelefoneInquilino.addActionListener(this);
            btnRemoverTelefoneInquilino.addActionListener(this);
            btnRemoverInquilino.addActionListener(this);
            checkBoxCNPJ.addActionListener(this);
            checkboxNotificadoJudicialmente.addActionListener(this);
            checkBoxProcessoJuridico.addActionListener(this);
            btnTeste.addActionListener(this);
            itemMenuDetalhe.addActionListener(this);
            tabelaHistoricoInquilino.addMouseListener(this);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (e.getSource() == tabelaHistoricoInquilino) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel11 = new javax.swing.JPanel();
        popupMenu = new javax.swing.JPopupMenu();
        itemMenuDetalhe = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblCpf = new javax.swing.JLabel();
        txtCpf = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        txtRg = new javax.swing.JFormattedTextField();
        checkBoxFalecido = new javax.swing.JCheckBox();
        jLabel22 = new javax.swing.JLabel();
        txtCondominio = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtUnidade = new javax.swing.JTextField();
        checkBoxCNPJ = new javax.swing.JCheckBox();
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
        jPanel12 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        painelContato = new javax.swing.JPanel();
        painelEnderecoInquilino = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblEndereco = new javax.swing.JTable();
        btnAdicionarEndereco = new javax.swing.JButton();
        btnEditarEndereco = new javax.swing.JButton();
        btnRemoverEndereco = new javax.swing.JButton();
        painelTelefoneInquilino = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblTelefone = new javax.swing.JTable();
        btnAdicionarTelefone = new javax.swing.JButton();
        btnEditarTelefone = new javax.swing.JButton();
        btnRemoverTelefone = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        painelInquilino = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtNomeInquilino = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtCpfInquilino = new javax.swing.JFormattedTextField();
        txtRgInquilino = new javax.swing.JFormattedTextField();
        jLabel21 = new javax.swing.JLabel();
        checkBoxInquilino = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblEnderecoInquilino = new javax.swing.JTable();
        btnAdicionarEnderecoInquilino = new javax.swing.JButton();
        btnEditarEnderecoInquilino = new javax.swing.JButton();
        btnRemoverEnderecoInquilino = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblTelefoneInquilino = new javax.swing.JTable();
        btnAdicionarTelefoneInquilino = new javax.swing.JButton();
        btnEditarTelefoneInquilino = new javax.swing.JButton();
        btnRemoverTelefoneInquilino = new javax.swing.JButton();
        painelCorrespondencia = new javax.swing.JPanel();
        painelUnidade = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        Coeficiente = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtIptu = new javax.swing.JTextField();
        txtFracaoIdeal = new javax.swing.JTextField();
        txtBloco = new javax.swing.JTextField();
        txtCoeficiente = new javax.swing.JTextField();
        txtValorPrincipal = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        checkBoxProcessoJuridico = new javax.swing.JCheckBox();
        cmbAdvogado1 = new javax.swing.JComboBox();
        dateProcessoJuridico = new net.sf.nachocalendar.components.DateField();
        btnTeste = new javax.swing.JButton();
        txtNumeroProcesso = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        checkboxNotificadoJudicialmente = new javax.swing.JCheckBox();
        cmbAdvogado2 = new javax.swing.JComboBox();
        dateInicioJudicial = new net.sf.nachocalendar.components.DateField();
        dateFimJudicial = new net.sf.nachocalendar.components.DateField();
        jLabel12 = new javax.swing.JLabel();
        checkBoxSindico = new javax.swing.JCheckBox();
        checkboxAtivo = new javax.swing.JCheckBox();
        checkboxImpressaoCertificado = new javax.swing.JCheckBox();
        checkboxImpressaoCobranca = new javax.swing.JCheckBox();
        checkboxCartaCobranca = new javax.swing.JCheckBox();
        painelAnotacoes = new javax.swing.JPanel();
        btnAdicionarAnotacao = new javax.swing.JButton();
        btnRemoverAnotacao = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaAnotacoes = new javax.swing.JTable();
        btnEditarAnotacao = new javax.swing.JButton();
        btnImprimirAnotacoes = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tabelaHistoricoInquilino = new javax.swing.JTable();
        btnRemoverInquilino = new javax.swing.JButton();

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

        itemMenuDetalhe.setText("Exibir Detalhes");
        popupMenu.add(itemMenuDetalhe);

        setClosable(true);
        setTitle("Cadastro de Condômino");
        setPreferredSize(new java.awt.Dimension(643, 481));

        jPanel1.setPreferredSize(new java.awt.Dimension(679, 439));

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel20.setText("Nome*:");
        jLabel20.setToolTipText("Campo Obrigatório");

        txtNome.setToolTipText("Digite a Razão Social");
        txtNome.setName("Nome"); // NOI18N

        lblCpf.setText("CPF*:");

        try {
            txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        txtCpf.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtCpf.setName("CPF"); // NOI18N

        jLabel3.setText("RG:");

        try {
            txtRg.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###-#")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        checkBoxFalecido.setText("Falecido?");

        jLabel22.setForeground(new java.awt.Color(255, 51, 51));
        jLabel22.setText("Condomínio:");

        txtCondominio.setBackground(new java.awt.Color(204, 204, 204));
        txtCondominio.setEditable(false);

        jLabel4.setText("Unidade*:");

        txtUnidade.setName("unidade"); // NOI18N

        checkBoxCNPJ.setText("CNPJ?");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(lblCpf))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxFalecido))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxCNPJ)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRg, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCondominio, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtUnidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxFalecido)
                    .addComponent(jLabel20)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCpf)
                    .addComponent(txtCondominio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(txtRg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(checkBoxCNPJ))
                .addGap(26, 26, 26))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setPreferredSize(new java.awt.Dimension(650, 97));

        jLabel26.setText("Bairro:");

        txtUf.setBackground(new java.awt.Color(204, 204, 204));
        txtUf.setEditable(false);
        txtUf.setName("estado"); // NOI18N

        jLabel28.setText("UF:");

        txtCidade.setBackground(new java.awt.Color(204, 204, 204));
        txtCidade.setEditable(false);
        txtCidade.setName("cidade"); // NOI18N

        jLabel24.setText("Número:");

        jLabel25.setText("Compl.:");

        txtNumero.setBackground(new java.awt.Color(204, 204, 204));
        txtNumero.setEditable(false);
        txtNumero.setName("numero"); // NOI18N

        txtCep.setBackground(new java.awt.Color(204, 204, 204));
        txtCep.setEditable(false);
        txtCep.setName("cep"); // NOI18N

        txtComplemento.setBackground(new java.awt.Color(204, 204, 204));
        txtComplemento.setEditable(false);
        txtComplemento.setToolTipText("");
        txtComplemento.setName("complemento"); // NOI18N

        jLabel23.setText("Endereço:");

        jLabel29.setText("CEP:");

        txtBairro.setBackground(new java.awt.Color(204, 204, 204));
        txtBairro.setEditable(false);
        txtBairro.setName("bairro"); // NOI18N

        jLabel27.setText("Cidade:");

        txtRua.setBackground(new java.awt.Color(204, 204, 204));
        txtRua.setEditable(false);
        txtRua.setToolTipText("Digite o Endereço");
        txtRua.setName("logradouro"); // NOI18N

        jLabel2.setText("Referência:");

        txtReferencia.setBackground(new java.awt.Color(204, 204, 204));
        txtReferencia.setEditable(false);

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
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtRua)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jLabel2)
                        .addGap(1, 1, 1)
                        .addComponent(txtReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29)
                    .addComponent(jLabel24)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCep, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(txtBairro))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtRua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

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

        painelEnderecoInquilino.setBorder(javax.swing.BorderFactory.createTitledBorder("Endereços"));

        tblEndereco.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblEndereco);

        btnAdicionarEndereco.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarEndereco.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarEndereco.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarEndereco.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarEndereco.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarEndereco.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarEndereco.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarEndereco.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarEndereco.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarEndereco.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverEndereco.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverEndereco.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverEndereco.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverEndereco.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverEndereco.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout painelEnderecoInquilinoLayout = new javax.swing.GroupLayout(painelEnderecoInquilino);
        painelEnderecoInquilino.setLayout(painelEnderecoInquilinoLayout);
        painelEnderecoInquilinoLayout.setHorizontalGroup(
            painelEnderecoInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelEnderecoInquilinoLayout.createSequentialGroup()
                .addContainerGap(251, Short.MAX_VALUE)
                .addGroup(painelEnderecoInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(painelEnderecoInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelEnderecoInquilinoLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(57, Short.MAX_VALUE)))
        );
        painelEnderecoInquilinoLayout.setVerticalGroup(
            painelEnderecoInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelEnderecoInquilinoLayout.createSequentialGroup()
                .addComponent(btnAdicionarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(painelEnderecoInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelEnderecoInquilinoLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        painelTelefoneInquilino.setBorder(javax.swing.BorderFactory.createTitledBorder("Telefones"));

        tblTelefone.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tblTelefone);

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

        javax.swing.GroupLayout painelTelefoneInquilinoLayout = new javax.swing.GroupLayout(painelTelefoneInquilino);
        painelTelefoneInquilino.setLayout(painelTelefoneInquilinoLayout);
        painelTelefoneInquilinoLayout.setHorizontalGroup(
            painelTelefoneInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelTelefoneInquilinoLayout.createSequentialGroup()
                .addContainerGap(217, Short.MAX_VALUE)
                .addGroup(painelTelefoneInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(painelTelefoneInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelTelefoneInquilinoLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(54, Short.MAX_VALUE)))
        );
        painelTelefoneInquilinoLayout.setVerticalGroup(
            painelTelefoneInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelTelefoneInquilinoLayout.createSequentialGroup()
                .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(painelTelefoneInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(painelTelefoneInquilinoLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jLabel13.setText("E-mail:");

        javax.swing.GroupLayout painelContatoLayout = new javax.swing.GroupLayout(painelContato);
        painelContato.setLayout(painelContatoLayout);
        painelContatoLayout.setHorizontalGroup(
            painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContatoLayout.createSequentialGroup()
                .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelContatoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(painelEnderecoInquilino, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(painelTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, painelContatoLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)))
                .addContainerGap())
        );
        painelContatoLayout.setVerticalGroup(
            painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelContatoLayout.createSequentialGroup()
                .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(painelEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(painelTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelContatoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22))
        );

        jTabbedPane1.addTab("Contato", painelContato);

        jLabel6.setText("CPF*:");

        txtNomeInquilino.setToolTipText("Digite a Razão Social");
        txtNomeInquilino.setName(""); // NOI18N

        jLabel7.setText("RG:");

        try {
            txtCpfInquilino.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###.###.###-##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        try {
            txtRgInquilino.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###-#")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        jLabel21.setText("Nome:");
        jLabel21.setToolTipText("Campo Obrigatório");

        checkBoxInquilino.setText("Tem inquilino?");

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Endereços"));

        tblEnderecoInquilino.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(tblEnderecoInquilino);

        btnAdicionarEnderecoInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarEnderecoInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarEnderecoInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarEnderecoInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarEnderecoInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarEnderecoInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarEnderecoInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarEnderecoInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarEnderecoInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarEnderecoInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverEnderecoInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverEnderecoInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverEnderecoInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverEnderecoInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverEnderecoInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(248, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(54, Short.MAX_VALUE)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(btnAdicionarEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverEnderecoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Telefones"));

        tblTelefoneInquilino.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(tblTelefoneInquilino);

        btnAdicionarTelefoneInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarTelefoneInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarTelefoneInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarTelefoneInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarTelefoneInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        btnEditarTelefoneInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnEditarTelefoneInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/atualizar.gif"))); // NOI18N
        btnEditarTelefoneInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnEditarTelefoneInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnEditarTelefoneInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverTelefoneInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverTelefoneInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverTelefoneInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverTelefoneInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverTelefoneInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap(217, Short.MAX_VALUE)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(54, Short.MAX_VALUE)))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(btnAdicionarTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverTelefoneInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel14Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout painelInquilinoLayout = new javax.swing.GroupLayout(painelInquilino);
        painelInquilino.setLayout(painelInquilinoLayout);
        painelInquilinoLayout.setHorizontalGroup(
            painelInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInquilinoLayout.createSequentialGroup()
                .addGroup(painelInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelInquilinoLayout.createSequentialGroup()
                        .addComponent(checkBoxInquilino)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)
                        .addGap(2, 2, 2)
                        .addComponent(txtNomeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCpfInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtRgInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(painelInquilinoLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        painelInquilinoLayout.setVerticalGroup(
            painelInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelInquilinoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxInquilino)
                    .addComponent(jLabel21)
                    .addComponent(jLabel7)
                    .addComponent(txtRgInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNomeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtCpfInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(painelInquilinoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Inquilino", painelInquilino);

        javax.swing.GroupLayout painelCorrespondenciaLayout = new javax.swing.GroupLayout(painelCorrespondencia);
        painelCorrespondencia.setLayout(painelCorrespondenciaLayout);
        painelCorrespondenciaLayout.setHorizontalGroup(
            painelCorrespondenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 602, Short.MAX_VALUE)
        );
        painelCorrespondenciaLayout.setVerticalGroup(
            painelCorrespondenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 178, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Correspondência", painelCorrespondencia);

        jPanel9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));

        jLabel8.setText("IPTU");

        jLabel9.setText("Fração Ideal:");

        jLabel10.setText("Bloco:");

        Coeficiente.setText("Coeficiente:");

        jLabel11.setText("Valor da Cota:");

        txtIptu.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtFracaoIdeal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtFracaoIdeal.setName("Aba Unidade - Fração Ideal"); // NOI18N

        txtBloco.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        txtCoeficiente.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCoeficiente.setName("Aba Unidade - Coeficiente"); // NOI18N

        txtValorPrincipal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(Coeficiente)
                    .addComponent(jLabel10)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtValorPrincipal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(txtCoeficiente, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(txtBloco, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(txtFracaoIdeal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addComponent(txtIptu, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtIptu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(txtFracaoIdeal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBloco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCoeficiente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Coeficiente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtValorPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));

        checkBoxProcessoJuridico.setText("Processo Judicial");

        cmbAdvogado1.setEnabled(false);

        dateProcessoJuridico.setEnabled(false);

        btnTeste.setText("Abrir");

        jLabel1.setText("Número do Processo:");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(checkBoxProcessoJuridico)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateProcessoJuridico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbAdvogado1, 0, 187, Short.MAX_VALUE)
                            .addGroup(jPanel10Layout.createSequentialGroup()
                                .addComponent(txtNumeroProcesso, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnTeste)))
                        .addGap(63, 63, 63))
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dateProcessoJuridico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBoxProcessoJuridico)))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(cmbAdvogado1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(2, 2, 2)
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTeste)
                    .addComponent(txtNumeroProcesso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel13.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));

        checkboxNotificadoJudicialmente.setText("Está Notif. extra Judicial");

        cmbAdvogado2.setEnabled(false);

        dateInicioJudicial.setEnabled(false);

        dateFimJudicial.setEnabled(false);

        jLabel12.setText("À");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkboxNotificadoJudicialmente)
                    .addComponent(cmbAdvogado2, 0, 143, Short.MAX_VALUE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(dateInicioJudicial, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateFimJudicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(checkboxNotificadoJudicialmente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbAdvogado2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(dateFimJudicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateInicioJudicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        checkBoxSindico.setText("É Sindico?");

        checkboxAtivo.setSelected(true);
        checkboxAtivo.setText("Ativa?");
        checkboxAtivo.setName("Unidade Ativa"); // NOI18N

        checkboxImpressaoCertificado.setText("Bloq. Impressão de Certificado");

        checkboxImpressaoCobranca.setText("Bloq Impressão de Cobrança");

        checkboxCartaCobranca.setText("Bloquear Carta Cobrança");

        javax.swing.GroupLayout painelUnidadeLayout = new javax.swing.GroupLayout(painelUnidade);
        painelUnidade.setLayout(painelUnidadeLayout);
        painelUnidadeLayout.setHorizontalGroup(
            painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelUnidadeLayout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelUnidadeLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(painelUnidadeLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(checkboxAtivo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(checkBoxSindico)
                        .addGap(29, 29, 29)))
                .addGroup(painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelUnidadeLayout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(19, Short.MAX_VALUE))
                    .addGroup(painelUnidadeLayout.createSequentialGroup()
                        .addComponent(checkboxImpressaoCertificado)
                        .addContainerGap())
                    .addGroup(painelUnidadeLayout.createSequentialGroup()
                        .addComponent(checkboxCartaCobranca)
                        .addContainerGap())
                    .addGroup(painelUnidadeLayout.createSequentialGroup()
                        .addComponent(checkboxImpressaoCobranca)
                        .addContainerGap())))
        );
        painelUnidadeLayout.setVerticalGroup(
            painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelUnidadeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(painelUnidadeLayout.createSequentialGroup()
                        .addGroup(painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelUnidadeLayout.createSequentialGroup()
                                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, Short.MAX_VALUE)
                                .addComponent(checkboxImpressaoCertificado))
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(painelUnidadeLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkboxCartaCobranca)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkboxImpressaoCobranca))
                            .addGroup(painelUnidadeLayout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(painelUnidadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxSindico)
                                    .addComponent(checkboxAtivo))))
                        .addGap(16, 16, 16))))
        );

        jTabbedPane1.addTab("Unidade", painelUnidade);

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

        tabelaAnotacoes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane3.setViewportView(tabelaAnotacoes);

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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnImprimirAnotacoes, 0, 0, Short.MAX_VALUE)
                    .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEditarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );
        painelAnotacoesLayout.setVerticalGroup(
            painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelAnotacoesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelAnotacoesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(painelAnotacoesLayout.createSequentialGroup()
                        .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImprimirAnotacoes))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Anotações", painelAnotacoes);

        tabelaHistoricoInquilino.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane6.setViewportView(tabelaHistoricoInquilino);

        btnRemoverInquilino.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverInquilino.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
        btnRemoverInquilino.setToolTipText("Remover Registro");
        btnRemoverInquilino.setMaximumSize(new java.awt.Dimension(32, 32));
        btnRemoverInquilino.setMinimumSize(new java.awt.Dimension(32, 32));
        btnRemoverInquilino.setPreferredSize(new java.awt.Dimension(32, 32));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 532, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnRemoverInquilino, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(btnRemoverInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72))))
        );

        jTabbedPane1.addTab("Histórico", jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Coeficiente;
    private javax.swing.JButton btnAdicionarAnotacao;
    private javax.swing.JButton btnAdicionarEndereco;
    private javax.swing.JButton btnAdicionarEnderecoInquilino;
    private javax.swing.JButton btnAdicionarTelefone;
    private javax.swing.JButton btnAdicionarTelefoneInquilino;
    private javax.swing.JButton btnEditarAnotacao;
    private javax.swing.JButton btnEditarEndereco;
    private javax.swing.JButton btnEditarEnderecoInquilino;
    private javax.swing.JButton btnEditarTelefone;
    private javax.swing.JButton btnEditarTelefoneInquilino;
    private javax.swing.JButton btnImprimirAnotacoes;
    private javax.swing.JButton btnRemoverAnotacao;
    private javax.swing.JButton btnRemoverEndereco;
    private javax.swing.JButton btnRemoverEnderecoInquilino;
    private javax.swing.JButton btnRemoverInquilino;
    private javax.swing.JButton btnRemoverTelefone;
    private javax.swing.JButton btnRemoverTelefoneInquilino;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnTeste;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox checkBoxCNPJ;
    private javax.swing.JCheckBox checkBoxFalecido;
    private javax.swing.JCheckBox checkBoxInquilino;
    private javax.swing.JCheckBox checkBoxProcessoJuridico;
    private javax.swing.JCheckBox checkBoxSindico;
    private javax.swing.JCheckBox checkboxAtivo;
    private javax.swing.JCheckBox checkboxCartaCobranca;
    private javax.swing.JCheckBox checkboxImpressaoCertificado;
    private javax.swing.JCheckBox checkboxImpressaoCobranca;
    private javax.swing.JCheckBox checkboxNotificadoJudicialmente;
    private javax.swing.JComboBox cmbAdvogado1;
    private javax.swing.JComboBox cmbAdvogado2;
    private net.sf.nachocalendar.components.DateField dateFimJudicial;
    private net.sf.nachocalendar.components.DateField dateInicioJudicial;
    private net.sf.nachocalendar.components.DateField dateProcessoJuridico;
    private javax.swing.JMenuItem itemMenuDetalhe;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JLabel jLabel4;
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
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblCpf;
    private javax.swing.JPanel painelAnotacoes;
    private javax.swing.JPanel painelContato;
    private javax.swing.JPanel painelCorrespondencia;
    private javax.swing.JPanel painelEnderecoInquilino;
    private javax.swing.JPanel painelInquilino;
    private javax.swing.JPanel painelTelefoneInquilino;
    private javax.swing.JPanel painelUnidade;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTable tabelaAnotacoes;
    private javax.swing.JTable tabelaHistoricoInquilino;
    private javax.swing.JTable tblEndereco;
    private javax.swing.JTable tblEnderecoInquilino;
    private javax.swing.JTable tblTelefone;
    private javax.swing.JTable tblTelefoneInquilino;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JTextField txtBloco;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JTextField txtCoeficiente;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtCondominio;
    private javax.swing.JFormattedTextField txtCpf;
    private javax.swing.JFormattedTextField txtCpfInquilino;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFracaoIdeal;
    private javax.swing.JTextField txtIptu;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtNomeInquilino;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtNumeroProcesso;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JFormattedTextField txtRg;
    private javax.swing.JFormattedTextField txtRgInquilino;
    private javax.swing.JTextField txtRua;
    private javax.swing.JTextField txtUf;
    private javax.swing.JTextField txtUnidade;
    private javax.swing.JTextField txtValorPrincipal;
    // End of variables declaration//GEN-END:variables
}
