/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.validadores;

import java.util.List;

/**
 *
 * @author Administrador
 */
public interface Validador {

    public boolean validar(List<Object> campos);

    public List<String> getErros();
}
