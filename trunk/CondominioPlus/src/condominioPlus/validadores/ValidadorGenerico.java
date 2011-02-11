/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.validadores;

import avant.view.AvantViewUtil;
import br.com.caelum.stella.MessageProducer;
import br.com.caelum.stella.ResourceBundleMessageProducer;
import br.com.caelum.stella.ValidationMessage;
import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import logicpoint.apresentacao.ApresentacaoUtil;
import logicpoint.util.DataUtil;

/**
 *
 * @author Administrador
 */
public class ValidadorGenerico extends ValidadorAbstrato {

    public boolean validar(List<Object> atributos) {
        boolean eValido = true;
        for (Object o : atributos) {
            if (o instanceof JFormattedTextField) {
                JFormattedTextField txt = (JFormattedTextField) o;
                if ((txt.getName().equals("CNPJ") && !validaCnpj(txt.getText())) ) {
                    eValido = exibirErro(txt, " em branco ou inválido!");
                }else if (txt.getName().equals("CPF") && !validaCpf(txt.getText()) ) {
                    eValido = exibirErro(txt, " em branco ou inválido!");
                }
            } else if (o instanceof JTextField) {
                JTextField txt = (JTextField) o;
                if (txt.getText().trim().equals("") || txt == null) {
                    eValido = exibirErro(txt);
                }
            } else if (o instanceof JCheckBox) {
                JCheckBox txt = (JCheckBox) o;
                if (!txt.isSelected() || txt == null) {
                    if (AvantViewUtil.perguntar(txt.getName() + " está desmarcado. Deseja marcá-lo automaticamente?", null)) {
                        txt.setSelected(true);
                    } else {
                        txt.setSelected(false);
                    }
                }
            }
            else if (o instanceof JRadioButton) {
                JRadioButton txt = (JRadioButton) o;
                if (!txt.isSelected() || txt == null) {
                    if (AvantViewUtil.perguntar(txt.getName() + " está desmarcado. Deseja marcá-lo automaticamente?", null)) {
                        txt.setSelected(true);
                    } else {
                        txt.setSelected(false);
                    }
                }
            }
        }
        return eValido;
    }

    public boolean validarDatas(Calendar Data1, Calendar Data2){
        
        int resultado = DataUtil.compararData(DataUtil.getDateTime(Data1), DataUtil.getDateTime(Data2));
        System.out.println("data1 " + DataUtil.getDateTime(Data1));
        System.out.println("data2 " + DataUtil.getDateTime(Data2) );
        if(resultado == 1){
            System.out.println("Iam here");
            ApresentacaoUtil.exibirAdvertencia("A data de término não pode ser menor que a data de Início!", null);
            return false;
        }
        return true;
    }

    private boolean exibirErro(JTextField txt) {
        txt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 51), 2));
        erros.add(txt.getName() + " não pode ficar em branco!");
        return false;
    }

    private boolean exibirErro(JCheckBox txt) {
        erros.add(txt.getName() + " deve estar ativo!");
        return false;
    }

    private boolean exibirErro(JTextField txt, String mensagem) {
        txt.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 0, 51), 2));
        erros.add(txt.getName() + mensagem);
        return false;
    }

    public boolean validarCampo(Object objeto) {
        boolean eValido = true;
        if (objeto instanceof JFormattedTextField) {
            JFormattedTextField txt = (JFormattedTextField) objeto;
            if (txt.getName().equals("CNPJ") && !validaCnpj(txt.getText())) {
                eValido = exibirErro(txt, " em branco ou inválido!");
            } else if (txt.getValue() == null) {
                eValido = exibirErro(txt);

            }
        } else if (objeto instanceof JTextField) {
            System.out.println("I am here");
            JTextField txt = (JTextField) objeto;
            if (txt.getText().trim().equals("") || txt == null) {
                eValido = exibirErro(txt);
            }
        } else if (objeto instanceof JCheckBox) {
            JCheckBox txt = (JCheckBox) objeto;
            if (!txt.isSelected() || txt == null) {
                if (ApresentacaoUtil.perguntar(txt.getName() + " está desmarcado. Deseja marcá-lo automaticamente?", null)) {
                    txt.setSelected(true);
                } else {
                    txt.setSelected(false);
                }
            }
        }

        return eValido;
    }

    public boolean validaCnpj(String cnpj) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("StellaValidationMessages", new Locale("pt", "BR"));
        MessageProducer messageProducer = new ResourceBundleMessageProducer(resourceBundle);
        boolean isFormatted = true;
        CNPJValidator validator = new CNPJValidator(messageProducer, isFormatted);
        try {
            validator.assertValid(cnpj);
            return true;
        } catch (InvalidStateException e) {
            for (ValidationMessage message : e.getInvalidMessages()) {
                System.out.println(message.getMessage());
            }
            return false;
        }
    }

        public boolean validaCpf(String cpf) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("StellaValidationMessages", new Locale("pt", "BR"));
        MessageProducer messageProducer = new ResourceBundleMessageProducer(resourceBundle);
        boolean isFormatted = true;
        CPFValidator validator = new CPFValidator(messageProducer, isFormatted);
        try {
            validator.assertValid(cpf);
            return true;
        } catch (InvalidStateException e) {
            for (ValidationMessage message : e.getInvalidMessages()) {
                System.out.println(message.getMessage());
            }
            return false;
        }
    }

    public void exibirErros(JComponent objeto) {
        StringBuilder sb = new StringBuilder("Foram encontrados os seguintes erros:");
        List<String> erros = this.getErros();
        for (String erro : erros) {
            System.out.println("erros " + erro);
            sb.append("\n - " + erro);
        }
        AvantViewUtil.exibirAdvertencia(sb.toString(), objeto);
        return;
    }


}

