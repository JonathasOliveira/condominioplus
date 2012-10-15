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
import condominioPlus.negocio.NotificacaoJudicial;
import condominioPlus.negocio.ProcessoJudicial;
import condominioPlus.negocio.Telefone;
import condominioPlus.negocio.Unidade;
import condominioPlus.negocio.funcionario.Funcionario;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
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
    private List<Anotacao> listaAnotacoes = new ArrayList<Anotacao>();

    /** Creates new form TelaDadosCondominio */
    public TelaDadosCondomino(Unidade unidade) {
        this.unidade = unidade;
        this.condominio = unidade.getCondominio();

        initComponents();
        controlador = new ControladorEventos();

        modificarCamposInquilino(false);

        verificarCNPJ();
        carregarTabelaTelefone();
        carregarTabelaEndereco();
        carregarTabelaAnotacoes();

        carregarComboAdvogado();

        if (this.unidade != null) {
            preencherTela(this.unidade);
        }

    }

    public TelaDadosCondomino(Unidade unidade, TabelaModelo_2 modelo) {
        this.unidade = unidade;
        this.condominio = unidade.getCondominio();
        this.modeloTabela = modelo;

        initComponents();
        controlador = new ControladorEventos();

        modificarCamposInquilino(false);

        verificarCNPJ();
        carregarTabelaTelefone();
        carregarTabelaEndereco();
        carregarTabelaAnotacoes();

        carregarComboAdvogado();

        if (this.unidade != null) {
            preencherTela(this.unidade);
        }

    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();
        campos.add(txtNome);
        if (!checkBoxCNPJ.isSelected()) {
            campos.add(txtCpf);
        } else {
            txtCpf.setName("CNPJ");
            campos.add(txtCpf);
        }

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

        if (unidade.isHasInquilino()) {
            txtNomeInquilino.setText(unidade.getInquilino().getNome());
            txtCpfInquilino.setText(unidade.getInquilino().getCpf());
            txtRgInquilino.setText(unidade.getInquilino().getRg());
            txtRuaInquilino.setText(unidade.getInquilino().getEndereco().getLogradouro());
            txtNumeroInquilino.setText(unidade.getInquilino().getEndereco().getNumero());
            txtComplementoInquilino.setText(unidade.getInquilino().getEndereco().getComplemento());
            txtReferenciaInquilino.setText(unidade.getInquilino().getEndereco().getReferencia());
            txtBairroInquilino.setText(unidade.getInquilino().getEndereco().getBairro());
            txtCidadeInquilino.setText(unidade.getInquilino().getEndereco().getCidade());
            txtUfInquilino.setText(unidade.getInquilino().getEndereco().getEstado());
            txtCepInquilino.setText((unidade.getInquilino().getEndereco().getCep()));
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
            lblCpf.setName("CNPJ*");
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void CnpjSelecionado() {
        if (checkBoxCNPJ.isSelected()) {
            lblCpf.setText("CNPJ*");
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("##.###.###/####-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }

        } else {
            lblCpf.setText("CPF*");
            try {
                txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###.###.###-##")));
            } catch (java.text.ParseException ex) {
                ex.printStackTrace();
            }


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

        if (unidade.isHasInquilino()) {
            txtNomeInquilino.setText(unidade.getInquilino().getNome().trim().toUpperCase());
            txtCpfInquilino.setText(unidade.getInquilino().getCpf());
            txtRgInquilino.setText(unidade.getInquilino().getRg());
            txtRuaInquilino.setText(unidade.getInquilino().getEndereco().getLogradouro().trim().toUpperCase());
            txtNumeroInquilino.setText(unidade.getInquilino().getEndereco().getNumero().trim().toUpperCase());
            txtComplementoInquilino.setText(unidade.getInquilino().getEndereco().getComplemento().trim().toUpperCase());
            txtReferenciaInquilino.setText(unidade.getInquilino().getEndereco().getReferencia().trim().toUpperCase());
            txtBairroInquilino.setText(unidade.getInquilino().getEndereco().getBairro().trim().toUpperCase());
            txtCidadeInquilino.setText(unidade.getInquilino().getEndereco().getCidade().trim().toUpperCase());
            txtUfInquilino.setText(unidade.getInquilino().getEndereco().getEstado().trim().toUpperCase());
            txtCepInquilino.setText((unidade.getInquilino().getEndereco().getCep()));
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
        txtRuaInquilino.setEnabled(valor);
        txtNumeroInquilino.setEnabled(valor);
        txtComplementoInquilino.setEnabled(valor);
        txtReferenciaInquilino.setEnabled(valor);
        txtBairroInquilino.setEnabled(valor);
        txtCidadeInquilino.setEnabled(valor);
        txtUfInquilino.setEnabled(valor);
        txtCepInquilino.setEnabled(valor);
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
            } else if (e.getSource() == btnRemoverAnotacao) {
                removerAnotacao();
            } else if (e.getSource() == checkBoxInquilino) {
                boolean selecionado = checkBoxInquilino.isSelected();

                if (!selecionado) {
                    modificarCamposInquilino(false);
                } else {
                    modificarCamposInquilino(true);
                }
            } else if (e.getSource() == checkBoxCNPJ) {
                CnpjSelecionado();
            } else if (e.getSource() == checkboxNotificadoJudicialmente) {
                boolean selecionado = checkboxNotificadoJudicialmente.isSelected();
                ativarNotificacao(selecionado);
            } else if (e.getSource() == checkBoxProcessoJuridico) {
                boolean selecionado = checkBoxProcessoJuridico.isSelected();
                ativarProcessoJuridico(selecionado);
            } else if (e.getSource() == btnTeste) {
                teste();
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
            btnRemoverAnotacao.addActionListener(this);
            checkBoxInquilino.addActionListener(this);
            checkBoxCNPJ.addActionListener(this);
            checkboxNotificadoJudicialmente.addActionListener(this);
            checkBoxProcessoJuridico.addActionListener(this);
            btnTeste.addActionListener(this);
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
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblEndereco = new javax.swing.JTable();
        btnAdicionarEndereco = new javax.swing.JButton();
        btnEditarEndereco = new javax.swing.JButton();
        btnRemoverEndereco = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblTelefone = new javax.swing.JTable();
        btnAdicionarTelefone = new javax.swing.JButton();
        btnEditarTelefone = new javax.swing.JButton();
        btnRemoverTelefone = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtNomeInquilino = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtCpfInquilino = new javax.swing.JFormattedTextField();
        txtRgInquilino = new javax.swing.JFormattedTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        txtNumeroInquilino = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        txtCepInquilino = new javax.swing.JFormattedTextField();
        txtComplementoInquilino = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtBairroInquilino = new javax.swing.JTextField();
        jLabel31 = new javax.swing.JLabel();
        txtReferenciaInquilino = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        txtCidadeInquilino = new javax.swing.JTextField();
        txtRuaInquilino = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        txtUfInquilino = new javax.swing.JTextField();
        checkBoxInquilino = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
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
        jPanel14 = new javax.swing.JPanel();
        btnAdicionarAnotacao = new javax.swing.JButton();
        btnRemoverAnotacao = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaAnotacoes = new javax.swing.JTable();

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
            txtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###.###.###-##")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
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
                        .addComponent(txtCondominio, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        txtUf.setEditable(false);
        txtUf.setName("estado"); // NOI18N

        jLabel28.setText("UF:");

        txtCidade.setEditable(false);
        txtCidade.setName("cidade"); // NOI18N

        jLabel24.setText("Número:");

        jLabel25.setText("Compl.:");

        txtNumero.setEditable(false);
        txtNumero.setName("numero"); // NOI18N

        txtCep.setEditable(false);
        txtCep.setName("cep"); // NOI18N

        txtComplemento.setEditable(false);
        txtComplemento.setToolTipText("");
        txtComplemento.setName("complemento"); // NOI18N

        jLabel23.setText("Endereço:");

        jLabel29.setText("CEP:");

        txtBairro.setEditable(false);
        txtBairro.setName("bairro"); // NOI18N

        jLabel27.setText("Cidade:");

        txtRua.setEditable(false);
        txtRua.setToolTipText("Digite o Endereço");
        txtRua.setName("logradouro"); // NOI18N

        jLabel2.setText("Referência:");

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
                .addContainerGap(25, Short.MAX_VALUE))
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

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Endereços"));

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

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(261, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(67, Short.MAX_VALUE)))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(btnAdicionarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Telefones"));

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

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(217, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(54, Short.MAX_VALUE)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(btnAdicionarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditarTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoverTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jLabel13.setText("E-mail:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6))
        );

        jTabbedPane1.addTab("Contato", jPanel4);

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

        jLabel21.setText("Inquilino:");
        jLabel21.setToolTipText("Campo Obrigatório");

        jLabel32.setText("Número:");

        txtNumeroInquilino.setName("numero"); // NOI18N

        jLabel33.setText("Compl.:");

        txtCepInquilino.setName("cep"); // NOI18N

        txtComplementoInquilino.setToolTipText("");
        txtComplementoInquilino.setName("complemento"); // NOI18N

        jLabel34.setText("Endereço:");

        jLabel35.setText("CEP:");

        jLabel5.setText("Referência:");

        txtBairroInquilino.setName("bairro"); // NOI18N

        jLabel31.setText("UF:");

        jLabel36.setText("Cidade:");

        txtCidadeInquilino.setName("cidade"); // NOI18N

        txtRuaInquilino.setToolTipText("Digite o Endereço");
        txtRuaInquilino.setName("logradouro"); // NOI18N

        jLabel30.setText("Bairro:");

        txtUfInquilino.setName("estado"); // NOI18N

        checkBoxInquilino.setText("Tem inquilino?");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(checkBoxInquilino)
                        .addContainerGap())
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtNomeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtCpfInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtRgInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel34)
                                    .addComponent(jLabel33)
                                    .addComponent(jLabel36))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(txtCidadeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(26, 26, 26)
                                        .addComponent(jLabel31)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtUfInquilino, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(txtComplementoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(9, 9, 9)
                                        .addComponent(jLabel5)
                                        .addGap(1, 1, 1)
                                        .addComponent(txtReferenciaInquilino, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                                    .addComponent(txtRuaInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel30)
                                    .addComponent(jLabel32)
                                    .addComponent(jLabel35))
                                .addGap(10, 10, 10)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNumeroInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCepInquilino)
                                    .addComponent(txtBairroInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(22, 22, 22))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxInquilino)
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(txtCpfInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(txtRgInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNomeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(txtRuaInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumeroInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(txtComplementoInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(txtReferenciaInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBairroInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(txtCidadeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCepInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35)
                    .addComponent(jLabel31)
                    .addComponent(txtUfInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Inquilino", jPanel5);

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
                    .addComponent(cmbAdvogado2, 0, 148, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(checkboxAtivo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(checkBoxSindico)
                        .addGap(29, 29, 29)))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(24, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(checkboxImpressaoCertificado)
                        .addContainerGap())
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(checkboxCartaCobranca)
                        .addContainerGap())
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(checkboxImpressaoCobranca)
                        .addContainerGap())))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, Short.MAX_VALUE)
                                .addComponent(checkboxImpressaoCertificado))
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkboxCartaCobranca)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkboxImpressaoCobranca))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(checkBoxSindico)
                                    .addComponent(checkboxAtivo))))
                        .addGap(16, 16, 16))))
        );

        jTabbedPane1.addTab("Unidade", jPanel8);

        btnAdicionarAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnAdicionarAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/adicionar.gif"))); // NOI18N
        btnAdicionarAnotacao.setMaximumSize(new java.awt.Dimension(32, 32));
        btnAdicionarAnotacao.setMinimumSize(new java.awt.Dimension(32, 32));
        btnAdicionarAnotacao.setPreferredSize(new java.awt.Dimension(32, 32));

        btnRemoverAnotacao.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRemoverAnotacao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/condominioPlus/recursos/imagens/remover.gif"))); // NOI18N
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

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(btnAdicionarAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRemoverAnotacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Anotações", jPanel14);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE))
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
    private javax.swing.JButton btnAdicionarTelefone;
    private javax.swing.JButton btnEditarEndereco;
    private javax.swing.JButton btnEditarTelefone;
    private javax.swing.JButton btnRemoverAnotacao;
    private javax.swing.JButton btnRemoverEndereco;
    private javax.swing.JButton btnRemoverTelefone;
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
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
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
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblCpf;
    private javax.swing.JTable tabelaAnotacoes;
    private javax.swing.JTable tblEndereco;
    private javax.swing.JTable tblTelefone;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JTextField txtBairroInquilino;
    private javax.swing.JTextField txtBloco;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JFormattedTextField txtCepInquilino;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JTextField txtCidadeInquilino;
    private javax.swing.JTextField txtCoeficiente;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JTextField txtComplementoInquilino;
    private javax.swing.JTextField txtCondominio;
    private javax.swing.JFormattedTextField txtCpf;
    private javax.swing.JFormattedTextField txtCpfInquilino;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFracaoIdeal;
    private javax.swing.JTextField txtIptu;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtNomeInquilino;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtNumeroInquilino;
    private javax.swing.JTextField txtNumeroProcesso;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JTextField txtReferenciaInquilino;
    private javax.swing.JFormattedTextField txtRg;
    private javax.swing.JFormattedTextField txtRgInquilino;
    private javax.swing.JTextField txtRua;
    private javax.swing.JTextField txtRuaInquilino;
    private javax.swing.JTextField txtUf;
    private javax.swing.JTextField txtUfInquilino;
    private javax.swing.JTextField txtUnidade;
    private javax.swing.JTextField txtValorPrincipal;
    // End of variables declaration//GEN-END:variables
}
