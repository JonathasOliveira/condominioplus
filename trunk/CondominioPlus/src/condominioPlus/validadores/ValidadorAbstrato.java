/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.validadores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Administrador
 */
public abstract class ValidadorAbstrato implements Validador {

    protected List<String> erros;

    public ValidadorAbstrato() {
        erros = new ArrayList<String>();
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }
}
