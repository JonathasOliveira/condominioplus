/*
 * NegocioUtil.java
 *
 * Created on 29/08/2007, 11:27:16
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

import condominioPlus.apresentacao.TelaPrincipal;
import logicpoint.exception.TratadorExcecao;
import logicpoint.persistencia.DAO;
import logicpoint.recursos.Recursos;

/**
 *
 * @author Thiago
 */
public class NegocioUtil {

   private static Configuracao configuracao;

   public static Configuracao getConfiguracaoLocal() {
        Configuracao config = new Configuracao();
        setConfiguracaoLocal(config);
        return config;
    }

    public static void setConfiguracaoLocal(Configuracao config) {
        config.setLocal(Recursos.getLocal());
        config.setLocais(Recursos.getLocais());
    }

    public static void salvarConfiguracaoLocal(Configuracao configuracao) {
        Recursos.setLocal(configuracao.getLocal());
        Recursos.setLocais(configuracao.getLocais());
    }

    public static Configuracao getConfiguracao() {
        return getConfiguracao(false);
    }

    public static Configuracao getConfiguracao(boolean recarregar) {
        if (recarregar || configuracao == null) {
            NegocioUtil.carregarConfiguracao();
        }
        return configuracao;
    }

    public static void carregarConfiguracao() {
        Configuracao config = null;
        try {
            config = new DAO().localizar(Configuracao.class, 0);
            if (config == null) {
                config = getConfiguracaoLocal();
                new DAO().salvar(config);
            } else {
                setConfiguracaoLocal(config);
            }
        } catch (Throwable t) {
            new TratadorExcecao(t, TelaPrincipal.getInstancia());
        } finally {
            if (config == null) {
                config = new Configuracao();
                setConfiguracaoLocal(config);
            }
        }
        configuracao = config;
    }

    
}
