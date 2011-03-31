/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro.arquivoRetorno;

/**
 *
 * @author Administrador
 */
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
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author marano
 */
public class EntradaArquivoRetorno implements Serializable {

    String linha = null;
    ArrayList<RegistroTransacao> registros = new ArrayList<RegistroTransacao>();
    private String codigoRetorno;

    public String getCodigoRetorno() {
        return codigoRetorno;

    }

    public static void main(String[] args) {
        FileDialog fileDialog = new FileDialog((Frame) null);
        fileDialog.setVisible(true);
        File diretorio = new File(fileDialog.getDirectory());
        System.out.println("sad" + diretorio.listFiles());
        new EntradaArquivoRetorno().lerArquivo(diretorio.listFiles());
    }

    public List<RegistroTransacao> lerArquivo(File arquivos[]) {
        for (int i = 0; i < arquivos.length; i++) {
            BufferedReader leitor = null;
            try {
                leitor = pegarReader(arquivos[i]);
                while ((linha = leitor.readLine()) != null) {

                    if (linha.equalsIgnoreCase("")) {
                        break;
                    }

                    if (isTransacao(linha) && arquivos[i].getName().charAt(0) == 'C') {
//                    editarValor(linha);
                        registros.add(obterRegistro(linha));
                    } else if (linha.charAt(0) == '0') {
                        codigoRetorno = linha.substring(108, 113);
                    }

                }
            } catch (IOException ex) {
                ex.printStackTrace();

            }
        }
        return registros;
    }

    private boolean isTransacao(String linha) {

        if (linha.charAt(0) == '1' ) {

            return true;


        } else {
            return false;
        }
    }

    private RegistroTransacao obterRegistro(String linha) {
        RegistroTransacao registro = new RegistroTransacao();

        DateTime data = new DateTime(2000 + Integer.parseInt(linha.substring(299, 301)), Integer.parseInt(linha.substring(297, 299)), Integer.parseInt(linha.substring(295, 297)), 0, 0, 0, 0);
        registro.setData(data);
        registro.setCodigo(linha.substring(46, 60));
        registro.setNumeroPrestacao(Integer.parseInt(linha.substring(67, 69)));
        registro.setValor(new Moeda(editarValor(linha, 253, 266)));
        registro.setValorTitulo(new Moeda(editarValor(linha, 152, 165)));
        registro.setJuros(new Moeda(editarValor(linha, 266, 279)));


        System.out.println(registro);
        System.out.println("data formatada " + DataUtil.toString(data));
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
}

