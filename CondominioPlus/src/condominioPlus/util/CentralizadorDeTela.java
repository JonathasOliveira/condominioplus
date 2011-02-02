/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.util;

import java.awt.Dimension;
import javax.swing.JInternalFrame;

/**
 *
 * @author Administrador
 */
public class CentralizadorDeTela {

    public static void centralizar(JInternalFrame fr) {
        Dimension d = fr.getDesktopPane().getSize();
        fr.setLocation((d.width - fr.getSize().width) / 2, (d.height - fr.getSize().height) / 2);
    }

 

}
