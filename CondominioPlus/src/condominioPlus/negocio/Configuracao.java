package condominioPlus.negocio;

/*
 * Configuracao.java
 *
 * Created on 17/10/2007, 14:45:08
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.math.BigDecimal;
import logicpoint.recursos.Local;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.swing.ImageIcon;
import javax.persistence.Transient;
import logicpoint.util.DataUtil;
import logicpoint.util.Util;
import org.joda.time.DateTime;

/**
 *
 * @author Thiago
 */
@Entity
public class Configuracao implements Serializable {

    @Id
    private int id;
    @Column(name = "nome_empresa")
    private String nomeEmpresa = "";
    private String cnpj = "";
    @Lob
    @Column(name = "logo_empresa")
    private byte[] logoEmpresa = new byte[0];
    @Temporal(value = TemporalType.DATE)
    @Column(name = "data_instalacao")
    private Calendar dataInstalacao = null;
    private boolean expirado;
    @Transient
    private Local local = new Local();
    @Transient
    private List<Local> locais = new ArrayList<Local>();
    @Column(name = "telefone_empresa")
    private String telefoneEmpresa = "";
    @Column(name = "endereco_empresa")
    private String enderecoEmpresa = "";
    @Column(name="percentual_juros")
    private BigDecimal percentualJuros;
    @Column(name="percentual_multa")
    private BigDecimal percentualMulta;
    @Column(name="incremento_nosso_numero")
    private int incrementoNumeroDocumento = 10000;

    public boolean isExpirado() {
        return expirado;
    }

    public void setExpirado(boolean expirado) {
        this.expirado = expirado;
    }

    public DateTime getDataInstalacao() {
        return DataUtil.getDateTime(dataInstalacao);
    }

    public void setDataInstalacao(DateTime dataInstalacao) {
        this.dataInstalacao = DataUtil.getCalendar(dataInstalacao);
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public ImageIcon getLogoEmpresa() {
        return logoEmpresa.length > 0 ? new ImageIcon(logoEmpresa) : null;
    }

    public void setLogoEmpresa(ImageIcon logoEmpresa) {
        this.logoEmpresa = logoEmpresa != null ? Util.getBytes(logoEmpresa) : new byte[0];
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public Configuracao() {
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public List<Local> getLocais() {
        return locais;
    }

    public void setLocais(List<Local> locais) {
        this.locais = locais;
    }

    public List<Local> getTodosLocais() {
        List<Local> todosLocais = getLocais();
        todosLocais.add(0, getLocal());
        return todosLocais;
    }

    public String getTelefoneEmpresa() {
        return telefoneEmpresa;
    }

    public void setTelefoneEmpresa(String telefoneEmpresa) {
        this.telefoneEmpresa = telefoneEmpresa;
    }

    public String getEnderecoEmpresa() {
        return enderecoEmpresa;
    }

    public void setEnderecoEmpresa(String enderecoEmpresa) {
        this.enderecoEmpresa = enderecoEmpresa;
    }

    public BigDecimal getPercentualJuros() {
        return percentualJuros;
    }

    public void setPercentualJuros(BigDecimal percentualJuros) {
        this.percentualJuros = percentualJuros;
    }

    public BigDecimal getPercentualMulta() {
        return percentualMulta;
    }

    public void setPercentualMulta(BigDecimal percentualMulta) {
        this.percentualMulta = percentualMulta;
    }

    public int getIncrementoNumeroDocumento() {
        return incrementoNumeroDocumento;
    }

    public void setIncrementoNumeroDocumento(int incrementoNumeroDocumento) {
        this.incrementoNumeroDocumento = incrementoNumeroDocumento;
    }
    
}
