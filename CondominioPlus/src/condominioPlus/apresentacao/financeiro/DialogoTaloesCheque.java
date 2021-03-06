/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoTaloesCheque.java
 *
 * Created on 10/02/2011, 16:46:50
 */
package condominioPlus.apresentacao.financeiro;

import condominioPlus.Main;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.DadosTalaoCheque;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.funcionario.TipoAcesso;
import condominioPlus.validadores.ValidadorGenerico;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;

/**
 *
 * @author Administrador
 */
public class DialogoTaloesCheque extends javax.swing.JDialog {

    private DadosTalaoCheque dados;
    private ControladorEventos controlador;

    /** Creates new form DialogoTaloesCheque */
    public DialogoTaloesCheque(java.awt.Frame parent, DadosTalaoCheque dados) {
        super(parent, true);
        this.dados = dados;
        initComponents();
        controlador = new ControladorEventos();

        this.setLocationRelativeTo(null);

        preencherTela();
    }

    private List listaCampos() {
        List<Object> campos = new ArrayList<Object>();

        campos.add(txtNumeroFinal);
        campos.add(txtNumeroInicial);

        return campos;
    }

    private void preencherObjeto() {

        if (dados != null) {
            dados.setNovo(false);
            dados.setEmUso(false);
            dados.setUsado(false);
            if (radioEmUso.isSelected()) {
                dados.setEmUso(true);
            }
            if (radioNovo.isSelected()) {
                dados.setNovo(true);
            }
            if (radioUsado.isSelected()) {
                dados.setUsado(true);
            }

            dados.setCondominio(Main.getCondominio());
            dados.setNumeroInicial(txtNumeroInicial.getText());
            dados.setNumeroFinal(txtNumeroFinal.getText());
        }

    }

    private void preencherTela() {

        if (dados != null) {
            radioEmUso.setSelected(dados.isEmUso());
            radioNovo.setSelected(dados.isNovo());
            radioUsado.setSelected(dados.isUsado());
            txtNumeroInicial.setText(dados.getNumeroInicial());
            txtNumeroFinal.setText(dados.getNumeroFinal());
        }
    }

    public static DadosTalaoCheque getDadosTalao(DadosTalaoCheque dados, Frame pai, boolean modal) {
        TelaPrincipal.getInstancia().criarJanela(new DialogoTaloesCheque(pai, dados));
        return dados;
    }
//
//    private boolean validarIntervalo(){
//
//        List<DadosTalaoCheque> lista = Main.getCondominio().getDadosTalaoCheques();
//        for (DadosTalaoCheque d : lista) {
//            if (d.verificarIntervaloChequeSemContaCorrente(txtNumeroInicial.getText()) || d.verificarIntervaloChequeSemContaCorrente(txtNumeroFinal.getText())) {
//                return true;
//            }
//        }
//
//        return false;
//
//    }

    private boolean verificarStatus() {

        List<DadosTalaoCheque> lista = new DAO().listar("TaloesPorCondominio", Main.getCondominio().getCodigo());
        for (DadosTalaoCheque d : lista) {
            if (d.isEmUso() && radioEmUso.isSelected()) {
                return true;
            }
        }

        return false;

    }

    private void salvar() {
        try {

            ValidadorGenerico validador = new ValidadorGenerico();
            if (!validador.validar(listaCampos())) {
                validador.exibirErros(null);
                return;
            }

//            if (validarIntervalo()){
//                ApresentacaoUtil.exibirAdvertencia("Verifique o intervalo de cheques", this);
//                return;
//            }

            if (verificarStatus()) {
                ApresentacaoUtil.exibirAdvertencia("Já existe um talão de cheque em uso!", this);
                return;
            }

            preencherObjeto();

            new DAO().salvar(dados);
            

            TipoAcesso tipo = null;
            if (dados.getCodigo() == 0) {
                tipo = tipo.INSERCAO;
            } else {
                tipo = tipo.EDICAO;
            }

            String descricao = "Cadastro do Talão " + dados.getNumeroInicial() + " - " + dados.getNumeroFinal() + ".";
            FuncionarioUtil.registrar(tipo, descricao);

            dispose();
        } catch (Throwable t) {
            new TratadorExcecao(t, this, true);
        }

    }

    private class ControladorEventos extends ControladorEventosGenerico {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSalvar) {
                salvar();
            } else if (e.getSource() == btnVoltar) {
                dispose();
            }
        }

        @Override
        public void configurar() {
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.transferidorFocoEnter, DialogoTaloesCheque.this, JTextField.class);
            ApresentacaoUtil.adicionarListener(ApresentacaoUtil.selecionadorTexto, DialogoTaloesCheque.this, JTextField.class);

            put(DadosTalaoCheque.class, painelDados);

            btnSalvar.addActionListener(this);
            btnVoltar.addActionListener(this);

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

        grupoBotoes = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        btnSalvar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        painelDados = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtNumeroInicial = new javax.swing.JTextField();
        txtNumeroFinal = new javax.swing.JTextField();
        radioUsado = new javax.swing.JRadioButton();
        radioEmUso = new javax.swing.JRadioButton();
        radioNovo = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cadastro Talões de Cheque");

        btnSalvar.setText("Salvar");

        btnVoltar.setText("Voltar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 313, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(0, 83, Short.MAX_VALUE)
                    .addComponent(btnSalvar)
                    .addGap(18, 18, 18)
                    .addComponent(btnVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 82, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(0, 4, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnSalvar)
                        .addComponent(btnVoltar))
                    .addGap(0, 4, Short.MAX_VALUE)))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setText("Número Inicial:");

        jLabel2.setText("Número Final:");

        txtNumeroInicial.setName("numeroInicial"); // NOI18N

        txtNumeroFinal.setName("numeroFinal"); // NOI18N

        javax.swing.GroupLayout painelDadosLayout = new javax.swing.GroupLayout(painelDados);
        painelDados.setLayout(painelDadosLayout);
        painelDadosLayout.setHorizontalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelDadosLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNumeroFinal))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, painelDadosLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNumeroInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        painelDadosLayout.setVerticalGroup(
            painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(painelDadosLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNumeroInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(painelDadosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNumeroFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        grupoBotoes.add(radioUsado);
        radioUsado.setText("Usado?");
        radioUsado.setName("usado"); // NOI18N

        grupoBotoes.add(radioEmUso);
        radioEmUso.setText("Em Uso?");
        radioEmUso.setName("emUso"); // NOI18N

        grupoBotoes.add(radioNovo);
        radioNovo.setSelected(true);
        radioNovo.setText("Novo?");
        radioNovo.setName("novo"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioNovo)
                    .addComponent(radioUsado)
                    .addComponent(radioEmUso))
                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(painelDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(6, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(radioUsado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioEmUso)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioNovo)
                .addGap(13, 13, 13))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.ButtonGroup grupoBotoes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel painelDados;
    private javax.swing.JRadioButton radioEmUso;
    private javax.swing.JRadioButton radioNovo;
    private javax.swing.JRadioButton radioUsado;
    private javax.swing.JTextField txtNumeroFinal;
    private javax.swing.JTextField txtNumeroInicial;
    // End of variables declaration//GEN-END:variables
}
