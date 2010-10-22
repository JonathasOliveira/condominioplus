/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.funcionario;

import condominioPlus.Main;
import condominioPlus.apresentacao.TelaPrincipal;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.recursos.Local;
import logicpoint.usuario.DialogoLogin;
import logicpoint.usuario.Usuario;
import logicpoint.util.Util;

/**
 *
 * @author marano
 */
public class FuncionarioUtil {

    public static Funcionario getFuncionario(Usuario usuario) {
        Funcionario funcionario = null;
        try {
            funcionario = new DAO().localizar(Funcionario.class, "FuncionarioPorUsuario", usuario);
        } catch (Throwable t) {
            new TratadorExcecao(t, false);
        }
        return funcionario;
    }

    public static Funcionario login() {
        return login(TelaPrincipal.getInstancia(), null);
    }

    public static Funcionario login(Frame pai) {
        return login(pai, null);
    }

    public static Funcionario login(Frame pai, List<Local> locais) {
        Usuario usuario = DialogoLogin.getUsuario(pai, locais, true);

        Funcionario funcionario = null;

        if (usuario == Usuario.usuarioFantasma) {
            funcionario = new Funcionario("Usuário");
            List<CaracteristicaAcesso> caracteristicasAcesso = Util.toList(CaracteristicaAcesso.values());
            List<Caracteristica> caracteristicas = new ArrayList<Caracteristica>(caracteristicasAcesso.size());
            for (CaracteristicaAcesso c : caracteristicasAcesso) {
                caracteristicas.add(new Caracteristica(c));
            }
            CategoriaFuncionario especial = new CategoriaFuncionario("Especial", caracteristicas);
            funcionario.setCategoria(especial);
        } else if (usuario != null) {
            funcionario = getFuncionario(usuario);
        }

        return funcionario;
    }

    public static void registrar(TipoAcesso tipo, String descricao) {
        try {
            if (Main.getFuncionario().getId() > 0) {
                new DAO().salvar(new ControleAcesso(tipo, descricao, Main.getFuncionario()));
            }
        } catch (Throwable t) {
            t.printStackTrace();
            new TratadorExcecao(t, false);
        }
    }

    public static boolean permite(Funcionario funcionario, CaracteristicaAcesso caracteristicaAcesso) {
        if (funcionario != null && funcionario.getCategoria() != null) {
            for (Caracteristica c : funcionario.getCategoria().getCaracteristicas()) {
                if (caracteristicaAcesso.equals(c.getCaracteristicaAcesso())) {
                    return true;
                }
            }
        }
        return false;
    }
}
