/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.relatorios;


/**
 *
 * @author Administrador
 */
public enum TipoRelatorio {

    INADIMPLENCIA_SINTETICA("Inadimplência Sintética"),
    INADIMPLENCIA_ANALITICA("Inadimplência Analítica");

    TipoRelatorio(String nome) {
        this.nome = nome;
    }

    public String toString() {
        return nome;
    }
    private String nome;
}
