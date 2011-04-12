/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.financeiro.arquivoRetorno;

import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.Identificador;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class EntradaExtratoDiário {
     String linha = null;
    ArrayList<ExtratoBancario> registros = new ArrayList<ExtratoBancario>();
    private String codigoRetorno;

    public String getCodigoRetorno() {
        return codigoRetorno;

    }

    public static void main(String[] args) {
//        FileDialog fileDialog = new FileDialog((Frame) null);
//        fileDialog.setVisible(true);
//        File diretorio = new File(fileDialog.getFile());
//        System.out.println("sad" + diretorio.listFiles());
        new EntradaExtratoDiário().lerArquivo(new File("Extrato.TXT"));
    }

    public List<ExtratoBancario> lerArquivo(File arquivo) {
//        for (int i = 0; i < arquivos.length; i++) {
            BufferedReader leitor = null;
            try {
                leitor = pegarReader(arquivo);
                while ((linha = leitor.readLine()) != null) {

                    if (linha.equalsIgnoreCase("")) {
                        break;
                    }

                    if (isTransacao(linha) && arquivo.getName().charAt(0) == 'E') {
//                    editarValor(linha);
                        registros.add(obterRegistro(linha));
                    } else if (linha.charAt(0) == '0') {
                        codigoRetorno = linha.substring(108, 113);
                    }

                }
            } catch (IOException ex) {
                ex.printStackTrace();

            }
//        }
        return registros;
    }

    private boolean isTransacao(String linha) {

        if (linha.charAt(0) == '1' ) {

            return true;


        } else {
            return false;
        }
    }

    private ExtratoBancario obterRegistro(String linha) {
        ExtratoBancario registro = new ExtratoBancario();

        DateTime data = new DateTime(2000 + Integer.parseInt(linha.substring(84, 86)), Integer.parseInt(linha.substring(82, 84)), Integer.parseInt(linha.substring(80, 82)), 0, 0, 0, 0);
        registro.setDataPagamento(DataUtil.getCalendar(data));
        Identificador ident = new Identificador();
        ident.setCodigoHistorico(Integer.valueOf(linha.substring(45, 49).trim()));
        ident.setPalavraChave(linha.substring(49,74));
        registro.setTipo(linha.substring(104, 105));
        registro.setIdentificador(ident);
        registro.setDoc(linha.substring(74, 80));
//        registro.setNatureza(Integer.valueOf(linha.substring(182, 183)));
        registro.setValor(new Moeda(editarValor(linha, 86, 104)));



        System.out.println(registro);
        return registro;
    }

    private double editarValor(String linha, int indiceInicio, int indiceTermino) {

        String valor = linha.substring(indiceInicio, indiceTermino);
        double numero = (Double.parseDouble(valor))/100;

        return numero;
    }

    private BufferedReader pegarReader(File arquivo) throws FileNotFoundException {
        File file = arquivo;
        FileReader reader = new FileReader(file);
        return new BufferedReader(reader);
    }

}
