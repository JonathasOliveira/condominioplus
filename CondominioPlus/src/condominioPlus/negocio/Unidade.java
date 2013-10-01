/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio;

import condominioPlus.negocio.cobranca.AcordoCobranca;
import condominioPlus.negocio.cobranca.Cobranca;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 *
 * @author Administrador
 */
@Entity
@NamedQueries(value = {
    @NamedQuery(name = "UnidadePorCondominio", query = "SELECT u FROM Unidade u WHERE u.condominio.codigo = ?1 order by u.codigo"),
    @NamedQuery(name = "ConselheirosPorUnidade", query = "SELECT u FROM Unidade u WHERE u.condominio.codigo = ?1 AND u.condomino.conselheiro = true order by u.codigo"),
    @NamedQuery(name = "CondominosPorUnidade", query = "SELECT u FROM Unidade u WHERE u.condominio.codigo = ?1 order by u.condomino.nome"),
    @NamedQuery(name = "CondominosPorUnidadeSemSindico", query = "SELECT u FROM Unidade u WHERE u.condominio.codigo = ?1 AND u.condomino.conselheiro = false and u.sindico = false order by u.condomino.nome")
})
public class Unidade implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int codigo;
    @OneToOne(cascade = CascadeType.ALL)
    private Condomino condomino = new Condomino();
    @ManyToOne
    private Condominio condominio;
    @OneToOne(cascade = CascadeType.ALL)
    private Inquilino inquilino;
    @OneToOne(mappedBy = "unidade", cascade = CascadeType.ALL)
    private ProcessoJudicial processoJudicial;
    @OneToOne(mappedBy = "unidade", cascade = CascadeType.ALL)
    private NotificacaoJudicial notificacaoJudicial;
    private String unidade;
    private String descricao;
    private String iptu;
    @Column(name = "fracao_ideal")
    private double fracaoIdeal;
    private String bloco;
    private String coeficiente;
    @Column(name = "valor_principal", precision = 20, scale = 2)
    private BigDecimal valorPrincipal = new BigDecimal(0);
    private boolean sindico;
    private boolean ativo;
    @Column(name = "bloquear_impressao_certificado")
    private boolean bloquearImpressaoCertificado;
    @Column(name = "bloquear_impressao_cobranca")
    private boolean bloquearImpressaoCobranca;
    @Column(name = "bloquear_impressao_carta_cobranca")
    private boolean bloquearImpressaoCartaCobranca;
    @Column(name = "tem_inquilino")
    private boolean hasInquilino;
    @OneToMany(mappedBy = "unidade", cascade = CascadeType.ALL)
    private List<Cobranca> cobrancas;
    @OneToMany(mappedBy = "unidade", cascade = CascadeType.ALL)
    private List<AcordoCobranca> acordos;
    //informações para o envio de correspondências/boletos
    @Column(name = "balancete_proprietario")
    private boolean balanceteProprietario;
    @Column(name = "balancete_inquilino")
    private boolean balanceteInquilino;
    @Column(name = "balancete_proprietario_inquilino")
    private boolean balanceteProprietarioInquilino;
    @Column(name = "boleto_proprietario")
    private boolean boletoProprietario;
    @Column(name = "boleto_inquilino")
    private boolean boletoInquilino;

    public Unidade() {
    }

    public Unidade(Condominio condominio) {
        this.condominio = condominio;
    }

    public boolean isHasInquilino() {
        return hasInquilino;
    }

    public void setHasInquilino(boolean hasInquilino) {
        this.hasInquilino = hasInquilino;
    }

    public Inquilino getInquilino() {
        return inquilino;
    }

    public void setInquilino(Inquilino inquilino) {
        this.inquilino = inquilino;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getBloco() {
        return bloco;
    }

    public void setBloco(String bloco) {
        this.bloco = bloco;
    }

    public boolean isBloquearImpressaoCartaCobranca() {
        return bloquearImpressaoCartaCobranca;
    }

    public void setBloquearImpressaoCartaCobranca(boolean bloquearImpressaoCartaCobranca) {
        this.bloquearImpressaoCartaCobranca = bloquearImpressaoCartaCobranca;
    }

    public boolean isBloquearImpressaoCertificado() {
        return bloquearImpressaoCertificado;
    }

    public void setBloquearImpressaoCertificado(boolean bloquearImpressaoCertificado) {
        this.bloquearImpressaoCertificado = bloquearImpressaoCertificado;
    }

    public boolean isBloquearImpressaoCobranca() {
        return bloquearImpressaoCobranca;
    }

    public void setBloquearImpressaoCobranca(boolean bloquearImpressaoCobranca) {
        this.bloquearImpressaoCobranca = bloquearImpressaoCobranca;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getCoeficiente() {
        return coeficiente;
    }

    public void setCoeficiente(String coeficiente) {
        this.coeficiente = coeficiente;
    }

    public Condominio getCondominio() {
        return condominio;
    }

    public void setCondominio(Condominio condominio) {
        this.condominio = condominio;
    }

    public Condomino getCondomino() {
        return condomino;
    }

    public void setCondomino(Condomino condomino) {
        this.condomino = condomino;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getFracaoIdeal() {
        return fracaoIdeal;
    }

    public void setFracaoIdeal(double fracaoIdeal) {
        this.fracaoIdeal = fracaoIdeal;
    }

    public String getIptu() {
        return iptu;
    }

    public void setIptu(String iptu) {
        this.iptu = iptu;
    }

    public NotificacaoJudicial getNotificacaoJudicial() {
        return notificacaoJudicial;
    }

    public void setNotificacaoJudicial(NotificacaoJudicial notificacaoJudicial) {
        this.notificacaoJudicial = notificacaoJudicial;
    }

    public ProcessoJudicial getProcessoJudicial() {
        return processoJudicial;
    }

    public void setProcessoJudicial(ProcessoJudicial processoJudicial) {
        this.processoJudicial = processoJudicial;
    }

    public boolean isSindico() {
        return sindico;
    }

    public void setSindico(boolean sindico) {
        this.sindico = sindico;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getValorPrincipal() {
        return valorPrincipal;
    }

    public void setValorPrincipal(BigDecimal valorPrincipal) {
        this.valorPrincipal = valorPrincipal;
    }

    public List<Cobranca> getCobrancas() {
        return cobrancas;
    }

    public void setCobrancas(List<Cobranca> cobrancas) {
        this.cobrancas = cobrancas;
    }

    public List<AcordoCobranca> getAcordos() {
        return acordos;
    }

    public void setAcordos(List<AcordoCobranca> acordos) {
        this.acordos = acordos;
    }

    public boolean isBoletoInquilino() {
        return boletoInquilino;
    }

    public void setBoletoInquilino(boolean boletoInquilino) {
        this.boletoInquilino = boletoInquilino;
    }

    public boolean isBoletoProprietario() {
        return boletoProprietario;
    }

    public void setBoletoProprietario(boolean boletoProprietario) {
        this.boletoProprietario = boletoProprietario;
    }

    public boolean isBalanceteInquilino() {
        return balanceteInquilino;
    }

    public void setBalanceteInquilino(boolean balanceteInquilino) {
        this.balanceteInquilino = balanceteInquilino;
    }

    public boolean isBalanceteProprietario() {
        return balanceteProprietario;
    }

    public void setBalanceteProprietario(boolean balanceteProprietario) {
        this.balanceteProprietario = balanceteProprietario;
    }

    public boolean isBalanceteProprietarioInquilino() {
        return balanceteProprietarioInquilino;
    }

    public void setBalanceteProprietarioInquilino(boolean balanceteProprietarioInquilino) {
        this.balanceteProprietarioInquilino = balanceteProprietarioInquilino;
    }

    @Override
    public String toString() {
        return condomino.getNome() + " - " + unidade;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Unidade other = (Unidade) obj;
        if (this.codigo != other.codigo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.codigo;
        return hash;
    }
}
