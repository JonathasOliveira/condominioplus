/*
 * Main.java
 *
 * Created on 27/07/2007, 15:50:27
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus;

import java.util.Properties;
import javax.swing.UIManager;
import condominioPlus.apresentacao.TelaPrincipal;
import condominioPlus.negocio.Condominio;
import condominioPlus.negocio.funcionario.FuncionarioUtil;
import condominioPlus.negocio.Configuracao;
import condominioPlus.negocio.NegocioUtil;
import condominioPlus.negocio.funcionario.CaracteristicaAcesso;
import condominioPlus.negocio.funcionario.Funcionario;
import condominioPlus.negocio.funcionario.TipoAcesso;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.persistencia.DAO;
import logicpoint.persistencia.FabricaHSQL;
import logicpoint.persistencia.FabricaPostgreSQL;
import logicpoint.persistencia.Persistencia;
import logicpoint.recursos.Recursos;
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;

/**
 *
 * @author USUARIO
 */
public class Main {

    public static final int LIVRE = 0;
    public static final int DEMONSTRACAO = 1;
    private static Funcionario funcionario;
    private static Condominio condominio;
    private static int tipoAplicacao = LIVRE;

    /** Creates a new instance of Main */
    public Main() {
        carregarRecursos();
        carregarConexao();
        carregarLookAndFeel();
//        carregarImagens();
        login();

//        //__________________________________________________________        
//        Configuracao config = NegocioUtil.getConfiguracao();
//        boolean expirado = config.isExpirado();
//        DateTime dataInstalacao = config.getDataInstalacao();
//        if (expirado) {
//        } else if (dataInstalacao != null) {
//            if (dataInstalacao.plusDays(30).isBefore(new DateTime())) {
//                expirado = true;
//                config.setExpirado(expirado);
//                new DAO().salvar(config);
//            }
//        } else {
//            config.setDataInstalacao(new DateTime());
//            new DAO().salvar(config);
//        }
//        if (expirado) {
//            ApresentacaoUtil.exibirAdvertencia("O prazo de utilização do sistema expirou.\n" +
//                    "Entre em contato com a Logic Point Ltda\n\n" +
//                    "E-mail: contato@logicpoint.com.br\n" +
//                    "Telefone: (22) 2645-5392", null);
//            System.exit(0);
//        }
//        //__________________________________________________________  

        if (tipoAplicacao == DEMONSTRACAO) {
            validarDemonstracao();
        }
    }

    public static int getTipoAplicacao() {
        return tipoAplicacao;
    }

    public static void setTipoAplicacao(int tipoAplicacao) {
        Main.tipoAplicacao = tipoAplicacao;
    }

    public static void login() {
        funcionario = FuncionarioUtil.login(null, NegocioUtil.getConfiguracaoLocal().getTodosLocais());

        if (funcionario == null) {
            fechar();
        } else {
            String categoria = funcionario.getCategoria() != null ? " (" + funcionario.getCategoria().getNome() + ")" : "";
            FuncionarioUtil.registrar(TipoAcesso.LOGON, funcionario.getNome() + categoria + " entrou no sistema.");
            new TelaPrincipal().setVisible(true);
        }
    }

    public static void logoff() {
        Persistencia.fecharConexao();
        login();
    }

    public static Funcionario getFuncionario() {
        return funcionario;
    }

    public static Condominio getCondominio() {
        return condominio;
    }

    public static void recarregarFuncionario() {
        funcionario = new DAO().localizar(Funcionario.class, funcionario.getId());
    }

    public static void recarregarCondominio() {
        condominio = new DAO().localizar(Condominio.class, condominio.getCodigo());
    }

    public static void setCondominio(Condominio c){
        condominio = c;
    }

    public static boolean permite(CaracteristicaAcesso caracteristicaAcesso) {
        return FuncionarioUtil.permite(getFuncionario(), caracteristicaAcesso);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Main();
    }

    private void carregarRecursos() {
        Recursos.inicializar();
    }

    private void validarDemonstracao() {
        Configuracao config = NegocioUtil.getConfiguracao();
        DateTime dataInstalacao = config.getDataInstalacao();
        if (config.isExpirado() || dataInstalacao != null) {
            if (dataInstalacao.plusDays(30).isBefore(new DateTime())) {
                ApresentacaoUtil.exibirAdvertencia("O prazo de utilização do sistema expirou.\n" +
                        "Entre em contato com Thiago Cifani\n\n" +
                        "E-mail: cifani.thiago@gmail.com\n" +
                        "Telefone: (22) 9945-2774", null);
                if (!config.isExpirado()) {
                    config.setExpirado(true);
                    new DAO().salvar(config);
                }
                System.exit(0);
            }
        } else {
            config.setDataInstalacao(new DateTime());
            new DAO().salvar(config);
        }
        int diasRestantes = Math.abs(DataUtil.getDiferencaEmDias(DataUtil.hoje(new DateTime()), DataUtil.hoje(dataInstalacao.plusDays(10))));
        ApresentacaoUtil.exibirAdvertencia("Faltam " + diasRestantes + " dias para a expiração do sistema.", null);
    }

    private void carregarConexao() {
        if (tipoAplicacao == LIVRE) {
            Persistencia.setFabrica(new FabricaPostgreSQL());
            Properties config = Recursos.getConfig();
            String portaServidor = config.getProperty("porta_servidor") != null ? config.getProperty("porta_servidor") : "5432";
            Persistencia.put(Persistencia.PORTA, portaServidor);
            Persistencia.put(Persistencia.NOME_BANCO, "condominio_plus");
            Persistencia.put(Persistencia.USUARIO, "administrador");
            Persistencia.put(Persistencia.SENHA, "senha");
            Persistencia.put(Persistencia.CACHE_SHARED, "true");
            Persistencia.put(Persistencia.CACHE, oracle.toplink.essentials.config.CacheType.HardWeak);
            Persistencia.put(Persistencia.GERAR, oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider.CREATE_ONLY);
        } else if (tipoAplicacao == DEMONSTRACAO) {
            Persistencia.setFabrica(new FabricaHSQL());
            Persistencia.put(Persistencia.CAMINHO, System.getProperty("user.dir").replace('\\', '/') + "/banco");
            Persistencia.put(Persistencia.NOME_BANCO, "condominio_plus");
            Persistencia.put(Persistencia.USUARIO, "sa");
            Persistencia.put(Persistencia.SENHA, "senha");
            Persistencia.put(Persistencia.CACHE, oracle.toplink.essentials.config.CacheType.NONE);
            Persistencia.put(Persistencia.GERAR, oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider.NONE);
        }
    }

    public static void fechar() {
        Persistencia.fecharConexao();
        System.exit(0);
    }

    /** Carregamento do Look And Feel que afetará todo o sistema */
    private void carregarLookAndFeel() {
        try {
            String laf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(laf);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    private void carregarImagens() {
//        Recursos.putImagem("gradient", "/commerce/recursos/imagens/gradient.jpg");
//        Recursos.putImagem("gradient2", "/commerce/recursos/imagens/gradient2.jpg");
//        Recursos.putImagem("gradient3", "/commerce/recursos/imagens/gradient3.jpg");
//        Recursos.putImagem("gradient4", "/commerce/recursos/imagens/gradient4.jpg");
//        Recursos.putImagem("icone_commerce", "/commerce/recursos/imagens/tray_commerce.gif");
//        Recursos.putImagem("logo_lp", "/commerce/recursos/imagens/logo_lp_grande.jpg");
//        Recursos.putImagem("logo_lp_transparente", "/commerce/recursos/imagens/logo_lp_grande.gif");
//        Recursos.putImagem("logo_lp_pequeno", "/commerce/recursos/imagens/logo_lp_pequeno.gif");
//        Recursos.putImagem("logo_commerce", "/commerce/recursos/imagens/logo_commerce.gif");
//        Recursos.putImagem("logo_commerce_pequeno", "/commerce/recursos/imagens/logo_commerce_pequeno.gif");
//    }
}
