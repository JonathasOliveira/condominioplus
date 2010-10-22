package condominioPlus.util;

import javax.swing.JTextField;
import javax.swing.text.*;
import logicpoint.apresentacao.ApresentacaoUtil;

public class LimitarCaracteres extends PlainDocument {

    private int iMaxLength;

    public LimitarCaracteres(int maxlen) {
        super();
        iMaxLength = maxlen;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr)
            throws BadLocationException {
        if (str == null) {
            return;
        }

        if (iMaxLength <= 0) // aceitara qualquer no. de caracteres
        {
            super.insertString(offset, str, attr);
            return;
        }

        int ilen = (getLength() + str.length());
        if (ilen <= iMaxLength) // se o comprimento final for menor...
        {
            super.insertString(offset, str, attr); // ...aceita str
        } else {
            if (getLength() == iMaxLength) {
                return; // nada a fazer
            }
            String newStr = str.substring(0, (iMaxLength - getLength()));

            super.insertString(offset, newStr, attr);
        }
    }

    public void ValidaNumero(JTextField Numero) {
        long valor;
        if (Numero.getText().length() != 0) {
            try {
                valor = Long.parseLong(Numero.getText());
            } catch (NumberFormatException ex) {
                ApresentacaoUtil.exibirInformacao("Esse Campo só aceita números", null);
                Numero.grabFocus();
            }
        }
    }
}
