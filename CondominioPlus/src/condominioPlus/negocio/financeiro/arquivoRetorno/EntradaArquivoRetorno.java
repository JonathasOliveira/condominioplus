/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro.arquivoRetorno;

/**
 *
 * @author Administrador
 */
import condominioPlus.Main;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.cobranca.Cobranca;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.persistencia.DAO;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author marano
 */
public class EntradaArquivoRetorno implements Serializable {

    String linha = null;
    ArrayList<RegistroTransacao> registros = new ArrayList<RegistroTransacao>();
    private List<ArquivoAtualizado> listaArquivos;
    private List<Cobranca> listaCobrancasEmAberto;
    private ArquivoAtualizado arquivo;

    public static void main(String[] args) {
        FileDialog fileDialog = new FileDialog((Frame) null);
        fileDialog.setVisible(true);
        File diretorio = new File(fileDialog.getDirectory());
        System.out.println("sad" + diretorio.listFiles());
        new EntradaArquivoRetorno().lerArquivo(diretorio.listFiles());
    }

    public List<RegistroTransacao> getRegistros() {
        return registros;
    }

    public EntradaArquivoRetorno() {
        carregarConexao();
        FileDialog fileDialog = new FileDialog(TelaPrincipal.getInstancia());
        fileDialog.setVisible(true);
        if (fileDialog.getDirectory() != null) {
            File diretorio = new File(fileDialog.getDirectory());
            if (diretorio != null) {
                lerArquivo(diretorio.listFiles());
            }
        } else {
            System.out.println("selecione um arquivo");
        }
    }

    public void lerArquivo(File arquivos[]) {
        ARQUIVOS:
        for (int i = 0; i < arquivos.length; i++) {
            BufferedReader leitor = null;
            if (arquivos[i].getName().charAt(0) == 'C') {
                arquivo = new ArquivoAtualizado();
                arquivo.setNome(arquivos[i].getName());
                if (verificarArquivosAtualizados(arquivo)) {
                    ApresentacaoUtil.exibirInformacao("O arquivo " + arquivo.getNome() + " já foi atualizado!", null);
                    continue ARQUIVOS;
                }
                new DAO().salvar(arquivo);

            }

            try {
                leitor = pegarReader(arquivos[i]);
                while ((linha = leitor.readLine()) != null) {

                    if (linha.equalsIgnoreCase("")) {
                        break;
                    }
                    if (isTransacao(linha) && arquivos[i].getName().charAt(0) == 'C') {
                        if (Main.getCondominio().getContaBancaria().getBanco().getNumeroBanco().equals("033")) {
                            if (obterRegistroSantander(linha) != null) {
                                registros.add(obterRegistroSantander(linha));
                            }
                        } else if (Main.getCondominio().getContaBancaria().getBanco().getNumeroBanco().equals("237")) {
                            if (obterRegistroBradesco(linha) != null) {
                                registros.add(obterRegistroBradesco(linha));
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean verificarArquivosAtualizados(ArquivoAtualizado arquivo) {
        for (ArquivoAtualizado a : listaArquivos) {
            if (listaArquivos.isEmpty()) {
                return false;
            }
            if (a.getNome().equals(arquivo.getNome())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTransacao(String linha) {

        if (linha.charAt(0) == '1') {

            return true;


        } else {
            return false;
        }
    }

    private RegistroTransacao obterRegistroSantander(String linha) {
        RegistroTransacao registro = new RegistroTransacao();

        DateTime data = new DateTime(2000 + Integer.parseInt(linha.substring(299, 301)), Integer.parseInt(linha.substring(297, 299)), Integer.parseInt(linha.substring(295, 297)), 0, 0, 0, 0);
        registro.setData(data);
        registro.setDocumento(linha.substring(46, 59));
        registro.setValorTitulo(new Moeda(editarValor(linha, 152, 165)));
        registro.setValorPago(new Moeda(editarValor(linha, 253, 266)));
        registro.setJuros(new Moeda(editarValor(linha, 266, 279)));

        Cobranca co = new DAO().localizar(Cobranca.class, "CobrancaPorNumeroDocumento", registro.getDocumento());

        if (co != null) {
            registro.setCobranca(co);
        }

        return registro;
    }

    private RegistroTransacao obterRegistroBradesco(String linha) {
        RegistroTransacao registro = new RegistroTransacao();

        DateTime data = new DateTime(2000 + Integer.parseInt(linha.substring(114, 116)), Integer.parseInt(linha.substring(112, 114)), Integer.parseInt(linha.substring(110, 112)), 0, 0, 0, 0);
        registro.setData(data);
        registro.setDocumento(linha.substring(70, 81));
        registro.setValorTitulo(new Moeda(editarValor(linha, 153, 165)));
        registro.setValorPago(new Moeda(editarValor(linha, 254, 266)));
        registro.setJuros(new Moeda(editarValor(linha, 267, 279)));

        Cobranca co = new DAO().localizar(Cobranca.class, "CobrancaPorNumeroDocumento", registro.getDocumento());

        if (co != null) {
            registro.setCobranca(co);
        }

        return registro;
    }

    private double editarValor(String linha, int indiceInicio, int indiceTermino) {
        String valor = linha.substring(indiceInicio, indiceTermino);
        double numero = (Double.parseDouble(valor)) / 100;
        return numero;
    }

    private BufferedReader pegarReader(File arquivo) throws FileNotFoundException {
        File file = arquivo;
        FileReader reader = new FileReader(file);
        return new BufferedReader(reader);
    }

    private void carregarConexao() {
        listaArquivos = new DAO().listar(ArquivoAtualizado.class);
        listaCobrancasEmAberto = new DAO().listar("CobrancasEmAberto");
    }
}
