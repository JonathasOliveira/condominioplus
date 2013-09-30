/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.cobranca;

import condominioPlus.negocio.Endereco;
import condominioPlus.negocio.Unidade;
import java.util.List;

/**
 *
 * @author eugenia
 */
public class DadosCorrespondencia {
    
    private String condominio;
    private String unidade;
    private String nome;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private boolean inquilino;

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isInquilino() {
        return inquilino;
    }

    public void setInquilino(boolean inquilino) {
        this.inquilino = inquilino;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getCondominio() {
        return condominio;
    }

    public void setCondominio(String condominio) {
        this.condominio = condominio;
    }
    
    public static List<DadosCorrespondencia> preencherLista(List<Unidade> unidades, List<DadosCorrespondencia> listaDados, boolean imprimirProprietario, boolean imprimirInquilino){
        for (Unidade u : unidades) {

            if (imprimirProprietario) {
                DadosCorrespondencia dados = new DadosCorrespondencia();
                dados.setCondominio(u.getCondominio().getRazaoSocial());
                dados.setUnidade(u.getUnidade());
                dados.setNome(u.getCondomino().getNome());
                for (Endereco e : u.getCondomino().getEnderecos()) {
                    if (e.isPadrao()) {
                        dados.setLogradouro(e.getLogradouro());
                        dados.setNumero(e.getNumero());
                        dados.setComplemento(e.getComplemento());
                        dados.setBairro(e.getBairro());
                        dados.setCidade(e.getCidade());
                        dados.setEstado(e.getEstado());
                        dados.setCep(e.getCep());
                        dados.setInquilino(false);
                    }
                }
                listaDados.add(dados);
            }

            if (imprimirInquilino) {
                if (u.getInquilino() != null) {
                    DadosCorrespondencia dadosInquilino = new DadosCorrespondencia();
                    dadosInquilino.setCondominio(u.getCondominio().getRazaoSocial());
                    dadosInquilino.setUnidade(u.getUnidade());
                    dadosInquilino.setNome(u.getInquilino().getNome());
                    for (Endereco e : u.getInquilino().getEnderecos()) {
                        if (e.isPadrao()) {
                            dadosInquilino.setLogradouro(e.getLogradouro());
                            dadosInquilino.setNumero(e.getNumero());
                            dadosInquilino.setComplemento(e.getComplemento());
                            dadosInquilino.setBairro(e.getBairro());
                            dadosInquilino.setCidade(e.getCidade());
                            dadosInquilino.setEstado(e.getEstado());
                            dadosInquilino.setCep(e.getCep());
                            dadosInquilino.setInquilino(true);
                        }
                    }
                    listaDados.add(dadosInquilino);
                }
            }
        }
        return listaDados;
    }
    
}
