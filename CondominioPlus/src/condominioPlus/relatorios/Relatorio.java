/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.relatorios;


import condominioPlus.negocio.funcionario.Funcionario;
import condominioPlus.util.Relatorios;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import logicpoint.persistencia.DAO;
import logicpoint.util.DataUtil;

/**
 *
 * @author marano
 */
public class Relatorio {

    public static void imprimirRelatorioFuncionario() {

        List<Funcionario> funcionarios;

        funcionarios = new DAO().listar(Funcionario.class);

        HashMap parametrosRelatorio = new HashMap();

        List<HashMap> lista = new ArrayList<HashMap>();
        for (Funcionario f : funcionarios) {
            HashMap<String, String> objeto = new HashMap<String, String>();
            objeto.put("nome", f.getNome());

            objeto.put("cpf", f.getCpf());
            objeto.put("dataNascimento", DataUtil.toString(f.getDataNascimento()));
            objeto.put("identidade", f.getIdentidade());
            objeto.put("estadoCivil", f.getEstadoCivil());
            objeto.put("logradouro", f.getEndereco().getLogradouro());
            objeto.put("numero", f.getEndereco().getNumero());
            objeto.put("complemento", f.getEndereco().getComplemento());
            objeto.put("bairro", f.getEndereco().getBairro());
            objeto.put("cidade", f.getEndereco().getCidade());
            objeto.put("cep", f.getEndereco().getCep());
            objeto.put("estado", f.getEndereco().getEstado());
            objeto.put("referencia", f.getEndereco().getReferencia());

            lista.add(objeto);
        }

        new Relatorios().imprimir("RelatorioFuncionarios", parametrosRelatorio, lista, false, false, null);
    }
}
