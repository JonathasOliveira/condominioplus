/*
 * ControleAcesso.java
 *
 * Created on 03/10/2007, 16:48:16
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.negocio.funcionario;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import logicpoint.util.DataUtil;
import org.joda.time.DateTime;

/**
 *
 * @author Thiago
 */
@Entity
@Table(name = "controle_acesso")
@NamedQueries(value = {@NamedQuery(name = "ControleAcessoPorPeriodo", query = "SELECT c FROM ControleAcesso c WHERE c.data >= ?1 AND c.data <= ?2")})
public class ControleAcesso implements Serializable {

    @Id
    @GeneratedValue
    private int id;
    private TipoAcesso tipo;
    private String descricao = "";
    @ManyToOne(fetch = FetchType.LAZY)
    private Funcionario funcionario;
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "data_acesso")
    private Date data = new Date();

    public ControleAcesso(TipoAcesso tipo, String descricao, Funcionario funcionario) {
        setTipo(tipo);
        setDescricao(descricao);
        setFuncionario(funcionario);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TipoAcesso getTipo() {
        return tipo;
    }

    public void setTipo(TipoAcesso tipo) {
        this.tipo = tipo;
    }

    public DateTime getData() {
        return DataUtil.getDateTime(data);
    }

    public void setData(DateTime data) {
        this.data = DataUtil.getDate(data);
    }

    public ControleAcesso() {
    }
}