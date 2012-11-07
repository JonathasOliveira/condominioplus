/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DialogoDetalheInquilino.java
 *
 * Created on 05/11/2012, 16:06:22
 */
package condominioPlus.apresentacao.condomino;

import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.Inquilino;
import condominioPlus.negocio.Telefone;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import logicpoint.apresentacao.ControladorEventosGenerico;
import logicpoint.apresentacao.TabelaModelo_2;

/**
 *
 * @author eugenia
 */
public class DialogoDetalheInquilino extends javax.swing.JDialog {

    private ControladorDeEventos controlador;
    private Inquilino inquilino;
    private TabelaModelo_2<Endereco> modeloTabelaEnderecoInquilino;
    private TabelaModelo_2<Telefone> modeloTabelaTelefone;
    private List<Endereco> listaEnderecoInquilino = new ArrayList<Endereco>();
    private List<Telefone> listaTelefoneInquilino = new ArrayList<Telefone>();

    /** Creates new form DialogoDetalheInquilino */
    public DialogoDetalheInquilino(Inquilino inquilino, java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        this.inquilino = inquilino;

        initComponents();
        controlador = new ControladorDeEventos();
        controlador.preencher(inquilino);
        this.setLocationRelativeTo(null);

        carregarTabelaEnderecoInquilino();
        carregarTabelaTelefoneInquilino();
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
        listaEnderecoInquilino = inquilino.getEnderecos();
        return listaEnderecoInquilino;
    }

    private void preencherCamposEndereco(Endereco endereco) {
        checkBoxPadrao.setSelected(endereco.isPadrao());
        txtRua.setText(endereco.getLogradouro());
        txtNumero.setText(endereco.getNumero());
        txtComplemento.setText(endereco.getComplemento());
        txtReferencia.setText(endereco.getReferencia());
        txtBairro.setText(endereco.getBairro());
        txtCidade.setText(endereco.getCidade());
        txtUf.setText(endereco.getEstado());
        txtCep.setText(endereco.getCep());
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
        listaTelefoneInquilino = inquilino.getTelefones();
        return listaTelefoneInquilino;
    }

    private class ControladorDeEventos extends ControladorEventosGenerico {

        Object origem;

        @Override
        public void configurar() {
            put(Inquilino.class, DialogoDetalheInquilino.this);

            tblEnderecoInquilino.addMouseListener(this);
            tblEnderecoInquilino.addKeyListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            origem = e.getSource();
            if (origem == tblEnderecoInquilino && e.getClickCount() == 1) {
                preencherCamposEndereco(modeloTabelaEnderecoInquilino.getObjetoSelecionado());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            origem = e.getSource();
            if ((origem == tblEnderecoInquilino) && (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)) {
                preencherCamposEndereco(modeloTabelaEnderecoInquilino.getObjetoSelecionado());
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

        jPanel1 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        txtNomeInquilino = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtCpfInquilino = new javax.swing.JFormattedTextField();
        jLabel7 = new javax.swing.JLabel();
        txtRgInquilino = new javax.swing.JFormattedTextField();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblEnderecoInquilino = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblTelefoneInquilino = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        txtRua = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtNumero = new javax.swing.JTextField();
        checkBoxPadrao = new javax.swing.JCheckBox();
        jLabel25 = new javax.swing.JLabel();
        txtComplemento = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtReferencia = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        txtBairro = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        txtCidade = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        txtUf = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        txtCep = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dados Inquilino");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel21.setText("Nome:");
        jLabel21.setToolTipText("Campo Obrigatório");

        txtNomeInquilino.setBackground(new java.awt.Color(204, 204, 204));
        txtNomeInquilino.setEditable(false);
        txtNomeInquilino.setToolTipText("Digite a Razão Social");
        txtNomeInquilino.setName("nome"); // NOI18N

        jLabel6.setText("CPF*:");

        txtCpfInquilino.setBackground(new java.awt.Color(204, 204, 204));
        txtCpfInquilino.setEditable(false);
        txtCpfInquilino.setName("cpf"); // NOI18N

        jLabel7.setText("RG:");

        txtRgInquilino.setBackground(new java.awt.Color(204, 204, 204));
        txtRgInquilino.setEditable(false);
        txtRgInquilino.setName("rg"); // NOI18N

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Endereços"));

        tblEnderecoInquilino.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tblEnderecoInquilino.setName("null"); // NOI18N
        jScrollPane4.setViewportView(tblEnderecoInquilino);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Telefones"));

        tblTelefoneInquilino.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(tblTelefoneInquilino);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addGap(11, 11, 11))
        );

        jLabel1.setText("Endereço Selecionado:");

        jLabel23.setText("Logradouro: ");

        txtRua.setBackground(new java.awt.Color(204, 204, 204));
        txtRua.setEditable(false);
        txtRua.setToolTipText("Digite o Endereço");
        txtRua.setName("logradouro"); // NOI18N

        jLabel24.setText("Número: ");

        txtNumero.setBackground(new java.awt.Color(204, 204, 204));
        txtNumero.setEditable(false);
        txtNumero.setName("numero"); // NOI18N

        checkBoxPadrao.setText("Atual?");
        checkBoxPadrao.setEnabled(false);
        checkBoxPadrao.setName("padrao"); // NOI18N

        jLabel25.setText("Compl.: ");

        txtComplemento.setBackground(new java.awt.Color(204, 204, 204));
        txtComplemento.setEditable(false);
        txtComplemento.setToolTipText("");
        txtComplemento.setName("complemento"); // NOI18N

        jLabel2.setText("Referência: ");

        txtReferencia.setBackground(new java.awt.Color(204, 204, 204));
        txtReferencia.setEditable(false);
        txtReferencia.setName("referencia"); // NOI18N

        jLabel26.setText("Bairro: ");

        txtBairro.setBackground(new java.awt.Color(204, 204, 204));
        txtBairro.setEditable(false);
        txtBairro.setName("bairro"); // NOI18N

        jLabel27.setText("Cidade: ");

        txtCidade.setBackground(new java.awt.Color(204, 204, 204));
        txtCidade.setEditable(false);
        txtCidade.setName("cidade"); // NOI18N

        jLabel28.setText("UF: ");

        txtUf.setBackground(new java.awt.Color(204, 204, 204));
        txtUf.setEditable(false);
        txtUf.setName("estado"); // NOI18N

        jLabel29.setText("CEP: ");

        txtCep.setBackground(new java.awt.Color(204, 204, 204));
        txtCep.setEditable(false);
        txtCep.setName("cep"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
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
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkBoxPadrao))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel25))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(9, 9, 9)
                                        .addComponent(jLabel2)
                                        .addGap(1, 1, 1)
                                        .addComponent(txtReferencia, javax.swing.GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(txtRua, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel24)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel29)
                                .addGap(18, 18, 18)
                                .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel7)
                    .addComponent(txtRgInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNomeInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtCpfInquilino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel14, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(checkBoxPadrao))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtRua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtComplemento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtReferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBairro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27)
                    .addComponent(jLabel26)
                    .addComponent(txtCidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(txtUf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(txtCep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkBoxPadrao;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable tblEnderecoInquilino;
    private javax.swing.JTable tblTelefoneInquilino;
    private javax.swing.JTextField txtBairro;
    private javax.swing.JFormattedTextField txtCep;
    private javax.swing.JTextField txtCidade;
    private javax.swing.JTextField txtComplemento;
    private javax.swing.JFormattedTextField txtCpfInquilino;
    private javax.swing.JTextField txtNomeInquilino;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtReferencia;
    private javax.swing.JFormattedTextField txtRgInquilino;
    private javax.swing.JTextField txtRua;
    private javax.swing.JTextField txtUf;
    // End of variables declaration//GEN-END:variables
}
