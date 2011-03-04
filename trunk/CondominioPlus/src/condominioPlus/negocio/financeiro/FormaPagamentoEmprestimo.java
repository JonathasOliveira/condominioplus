/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

/**
 *
 * @author Administrador
 */
public enum FormaPagamentoEmprestimo {

    PAGAMENTO_A_VISTA("À Vista"),
    PARCELADO("Parcelado"),
    CONFORME_DISPONIBILIDADE("Conforme Disponibilidade");

    FormaPagamentoEmprestimo(String nome) {
        this.nome = nome;
    }

    public String toString() {
        return nome;
    }
    private String nome;
}
