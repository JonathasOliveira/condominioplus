/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro.arquivoRetorno;

import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.financeiro.ExtratoBancario;
import condominioPlus.negocio.financeiro.Identificador;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;
import logicpoint.util.Moeda;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class EntradaExtratoDiário {

    private String linha = null;
    private ArrayList<ExtratoBancario> registros = new ArrayList<ExtratoBancario>();
    private String codigoRetorno;
    private List<Condominio> condominios;
    private List<Identificador> identificadores;

    public EntradaExtratoDiário() {
        carregarConexao();
    }



    public String getCodigoRetorno() {
        return codigoRetorno;

    }

//    public static void main(String[] args) {
//        FileDialog fileDialog = new FileDialog((Frame) null);
//        fileDialog.setVisible(true);
//        File diretorio = new File(fileDialog.getDirectory());
//        System.out.println("sad" + diretorio.listFiles());
//
//        new EntradaExtratoDiário().lerArquivo(new File("Extrato.TXT"));
//
//    }
    public void lerArquivo(File arquivos[]) {
        for (int i = 0; i < arquivos.length; i++) {
            BufferedReader leitor = null;
            try {
                leitor = pegarReader(arquivos[i]);
                while ((linha = leitor.readLine()) != null) {

                    if (linha.equalsIgnoreCase("")) {
                        break;
                    }

                    if (isTransacao(linha) && arquivos[i].getName().charAt(0) == 'E') {
//                    editarValor(linha);
                        if (obterRegistro(linha) != null) {
                            registros.add(obterRegistro(linha));
                        }

                    } else if (linha.charAt(0) == '0') {
                        codigoRetorno = linha.substring(108, 113);
                    }

                }
            } catch (IOException ex) {
                ex.printStackTrace();

            }
        }
        salvar();
    }

    private boolean isTransacao(String linha) {

        if (linha.charAt(0) == '1') {

            return true;


        } else {
            return false;
        }
    }

    private void salvar() {
        new DAO().salvar(registros);

    }

    private Condominio localizaCondominio(String condominio) {

        for (Condominio c : condominios) {
            if (c.getContaBancaria().getContaCorrente().equals(condominio)) {
                return c;
            }
        }

        return null;

    }

    private Identificador localizaIdentificador(String ident) {

        for (Identificador i : identificadores) {
            if (i.getPalavraChave().equalsIgnoreCase(ident)) {
                System.out.println("i " + i.getPalavraChave());
                return i;
            }
        }

        return null;

    }

    private void carregarConexao() {
        identificadores = new DAO().listar(Identificador.class);
        condominios = new DAO().listar(Condominio.class);

    }

    private ExtratoBancario obterRegistro(String linha) {
        ExtratoBancario registro = new ExtratoBancario();

        if (localizaCondominio(linha.substring(21, 28)) != null) {
            DateTime data = new DateTime(2000 + Integer.parseInt(linha.substring(84, 86)), Integer.parseInt(linha.substring(82, 84)), Integer.parseInt(linha.substring(80, 82)), 0, 0, 0, 0);
            registro.setDataPagamento(DataUtil.getCalendar(data));
            registro.setTipo(linha.substring(104, 105));
            registro.setIdentificadorRegistro(Integer.valueOf(linha.substring(41, 42)));
            registro.setCondominio(localizaCondominio(linha.substring(21, 28)));
            if (registro.getIdentificadorRegistro() == 1) {
                registro.setIdentificador(localizaIdentificador(linha.substring(49, 74).trim()));
                System.out.println("registr identificador " +  registro.getIdentificador());
                System.out.println("fadf " + linha.substring(49, 74).trim());
                System.out.println("fadf " + linha.substring(45, 49).trim());
            }

            registro.setDoc(linha.substring(74, 80));

            if (registro.getIdentificadorRegistro() == 1 && !linha.substring(182, 183).equals(" ")) {
                registro.setNatureza(Integer.valueOf(linha.substring(182, 183)));
            }
            registro.setValor(new Moeda(editarValor(linha, 86, 104)));

            System.out.println(registro);
             return registro;
        } else {
            return null;
        }


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
