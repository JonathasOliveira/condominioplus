/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package condominioPlus.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author Administrador
 */
public class DataUtil {

    public static final String modeloPadrao = "dd/MM/yy";

    public static String formatar(DateTime data, String formato) {
        return toString(data, formato);
    }

    /** Creates a new instance of DataUtil */
    public DataUtil() {
    }

    public static DateTime getPrimeiroDiaMes(DateTime hoje) {
        return new DateTime(hoje.getYear(), hoje.getMonthOfYear(), 1, 0, 0, 0, 0);
    }

    public static DateTime getPrimeiroDiaMes() {
        return getPrimeiroDiaMes(hoje());
    }

    public static DateTime getUltimoDiaMes(DateTime hoje) {
        return hoje.getMonthOfYear() < 12 ? new DateTime(hoje.getYear(), hoje.getMonthOfYear() + 1, 1, 0, 0, 0, 0).minusDays(1) : new DateTime(hoje.getYear(), 12, 31, 0, 0, 0, 0);
    }

    public static DateTime getUltimoDiaMes() {
        return getUltimoDiaMes(hoje());
    }

    public static String escreverContagem(int dias) {
        if (dias == 0) {
            return "hoje";
        } else if (dias == 1) {
            return "amanhã";
        } else if (dias == -1) {
            return "ontem";
        } else {
            return dias + " dias";
        }
    }

    public static int getDiferencaEmDias(DateTime data) {
        return getDiferencaEmDias(data, hoje());
    }

    public static int getDiferencaEmDias(DateTime data2, DateTime data1) {
        if (data1 == null) {
            data1 = new DateTime();
        }
        if (data2 == null) {
            data2 = new DateTime();
        }
        return getDiferencaEmDias(data2.getMillis(), data1.getMillis());
    }

    public static int getDiferencaEmDias(Calendar data2, Calendar data1) {
        return getDiferencaEmDias(data2.getTimeInMillis(), data1.getTimeInMillis());
    }

    public static int getDiferencaEmDias(long data2, long data1) {
        return (int) ((data2 - data1) / 1000d / 60d / 60d / 24d);
    }

    public static double getDiferencaEmMeses(DateTime data2, DateTime data1) {
        double diferenca = ((data2.getMillis() - data1.getMillis()) / 1000d / 60d / 60d / 24d / 30d);
        //System.out.println("diferença de " + data1 + " e de " + data2 + " = " + diferenca);
        return diferenca;
    }

    public static Date getDate(Object data) {
        return getDate(getMillis(data));
    }

    public static Date getDate(long millis) {
        return millis < 0 ? null : new Date(millis);
    }

    public static Date getDate(Calendar data) {
        return getDate(getMillis(data));
    }

    public static Date getDate(DateTime data) {
        return getDate(getMillis(data));
    }

    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    public static Calendar getCalendar(Object data) {
        return getCalendar(getMillis(data));
    }

    public static Calendar getCalendar(long millis) {
        Calendar calendar = null;
        if (millis >= 0) {
            calendar = getCalendar();
            calendar.setTimeInMillis(millis);
        }
        return calendar;
    }

    public static Calendar getCalendar(Date data) {
        return getCalendar(getMillis(data));
    }

    public static Calendar getCalendar(DateTime data) {
        return getCalendar(getMillis(data));
    }

    public static DateTime getDateTime(Object data) {
        return getDateTime(getMillis(data));
    }

    public static DateTime getDateTime(long millis) {
        return millis < 0 ? null : new DateTime(millis);
    }

    public static DateTime getDateTime(Calendar data) {
        return getDateTime(getMillis(data));
    }

    public static DateTime getDateTime(Date data) {
        return getDateTime(getMillis(data));
    }

    public static long getMillis(Object data) {
        if (data != null) {
            if (data instanceof Date) {
                return getMillis((Date) data);
            } else if (data instanceof Calendar) {
                return getMillis((Calendar) data);
            } else if (data instanceof DateTime) {
                return getMillis((DateTime) data);
            } else if (data instanceof String) {
                long milisegundos = -1;
                if (data != null) {
                    String separador = null;
                    String stringData = (String) data;
                    for (int i = 0; i < stringData.toCharArray().length; i++) {
                        char c = stringData.toCharArray()[i];
                        if (c == '-') {
                            separador = "-";
                        } else if (c == '.') {
                            separador = ".";
                        } else if (c == '/') {
                            separador = "/";
                        } else {
                            continue;
                        }
                        break;
                    }
                    if (separador != null) {
                        try {
                            if (stringData.length() <= 8) {
                                milisegundos = new SimpleDateFormat("dd" + separador + "MM" + separador + "yy").parse(stringData).getTime();
                            } else {
                                milisegundos = new SimpleDateFormat("dd" + separador + "MM" + separador + "yyyy").parse(stringData).getTime();
                            }
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                return milisegundos;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        System.out.println(DataUtil.getDateTime("20-05-1990"));
        System.out.println(DataUtil.getDateTime("80.05.2004"));
        System.out.println(DataUtil.getDateTime("08/12/2008"));
        System.out.println(DataUtil.getDateTime("20/14/2008"));
        System.out.println(DataUtil.getDateTime("20/14/10"));
        System.out.println(DataUtil.getDateTime("20/06/08"));
    }

    public static long getMillis(Calendar data) {
        return data == null ? -1 : data.getTimeInMillis();
    }

    public static long getMillis(Date data) {
        return data == null ? -1 : data.getTime();
    }


    public static long getMillis(DateTime data) {
        return data == null ? -1 : data.getMillis();
    }

    public static String toString(Date data) {
        return toString(data, modeloPadrao);
    }

    public static String toString(Date data, String modelo) {
        if (data != null && modelo != null) {
            return new SimpleDateFormat(modelo).format(data);
        } else {
            return "";
        }
    }

    public static String toString(Calendar data) {
        return toString(data, modeloPadrao);
    }

    public static String toString(Calendar data, String modelo) {
        return toString(getDate(data), modelo);
    }

    public static String toString(DateTime data) {
        return toString(data, modeloPadrao);
    }

    public static String toString(DateTime data, String modelo) {
        return toString(getDate(data), modelo);
    }

    public static String escreverMes(Date data) {
        return escreverMes(getCalendar(data));
    }

    public static String escreverMes(Calendar data) {
        switch (data.get(Calendar.MONTH)) {
            case 0:
                return "Janeiro";
            case 1:
                return "Fevereiro";
            case 2:
                return "Março";
            case 3:
                return "Abril";
            case 4:
                return "Maio";
            case 5:
                return "Junho";
            case 6:
                return "Julho";
            case 7:
                return "Agosto";
            case 8:
                return "Setembro";
            case 9:
                return "Outubro";
            case 10:
                return "Novembro";
            case 11:
                return "Dezembro";
            default:
                return null;
        }
    }

    public static String escreverMes(DateTime data) {
        return escreverMes(getCalendar(data));
    }

    public static String escreverDiaDaSemana(Date dia) {
        return escreverDiaDaSemana(getCalendar(dia));
    }

    public static String escreverDiaDaSemana(Calendar dia) {
        switch (dia.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return "Domingo";
            case Calendar.MONDAY:
                return "Segunda-Feira";
            case Calendar.TUESDAY:
                return "Terça-Feira";
            case Calendar.WEDNESDAY:
                return "Quarta-Feira";
            case Calendar.THURSDAY:
                return "Quinta-Feira";
            case Calendar.FRIDAY:
                return "Sexta-Feira";
            case Calendar.SATURDAY:
                return "Sábado";
            default:
                return null;
        }
    }

    public static String escreverDiaDaSemana(DateTime dia) {
        return escreverDiaDaSemana(getDate(dia));
    }

    public static String escreverDiaDaSemana() {
        return escreverDiaDaSemana(new Date());
    }

    public static DateTime agora() {
        return new DateTime();
    }

    public static DateTime hoje(DateTime data) {
        DateTime hoje = new DateTime(data.getYear(), data.getMonthOfYear(), data.getDayOfMonth(), 0, 0, 0, 0);
        return hoje;
    }

    public static DateTime hoje() {
        return hoje(agora());
    }

    public static List<DateTime> getIntervaloDatas(Date dataInicio, Date dataTermino, boolean aplainar) {
        return getIntervaloDatas(new DateTime(dataInicio.getTime()), new DateTime(dataTermino.getTime()), aplainar);
    }

    public static List<DateTime> getIntervaloDatas(Calendar dataInicio, Calendar dataTermino, boolean aplainar) {
        return getIntervaloDatas(new DateTime(dataInicio.getTimeInMillis()), new DateTime(dataTermino.getTimeInMillis()), aplainar);
    }

    public static List<DateTime> getIntervaloDatas(DateTime dataInicio, DateTime dataTermino, boolean aplainar) {
        List<DateTime> intervalo = new ArrayList<DateTime>();
        DateTime data1 = new DateTime(dataInicio.getMillis());
        DateTime data2 = new DateTime(dataTermino.getMillis());
        if (data1 == null || data2 == null) {
            return intervalo;
        }
        while (dataInicio.isBefore(dataTermino)) {
            DateTime dataAux = new DateTime(dataInicio.getMillis());
            intervalo.add(aplainar ? DataUtil.hoje(dataAux) : dataAux);
            dataInicio = dataInicio.plusDays(1);
        }

        return intervalo;
    }

    /**
     *
     * @param data1
     * @param data2
     * @return 1 se a primeira data for maior, -1 se for menor e 0 se forem iguais
     */
    public static int compararData(DateTime data1, DateTime data2) {
        if (data1.getYear() > data2.getYear()) {
            return 1;
        } else if (data1.getYear() < data2.getYear()) {
            return -1;
        } else {
            if (data1.getDayOfYear() > data2.getDayOfYear()) {
                return 1;
            } else if (data1.getDayOfYear() < data2.getDayOfYear()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public static boolean compararDia(DateTime data1, DateTime data2) {
        return data1.getYear() == data2.getYear() && data1.getDayOfYear() == data2.getDayOfYear();
    }

    public static String escreverDataEHora(DateTime data) {
        return toString(data, "dd/MM/yy HH:mm");
    }
}

