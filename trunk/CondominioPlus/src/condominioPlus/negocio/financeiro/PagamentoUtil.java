/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.financeiro;

import condominioPlus.Main;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author thiagocifani
 * Esta classe visa especificar metodos para evitar repetição dos mesmos
 */
public class PagamentoUtil {

    public static Pagamento pagamentoVinculado(Pagamento pagamento) {
        if (pagamento.getConta().getContaVinculada() != null) {

            TransacaoBancaria transacao = new TransacaoBancaria();
            if (pagamento.getTransacaoBancaria() != null) {
                transacao = pagamento.getTransacaoBancaria();
            }

            Pagamento pagamentoRelacionado = new Pagamento();
            if (transacao.getPagamentos() != null) {
                for (Pagamento p : transacao.getPagamentos()) {
                    if (!p.equals(pagamento)) {
                        pagamentoRelacionado = p;
                    }
                }
            }

            pagamentoRelacionado.setDataPagamento(pagamento.getDataPagamento());
            pagamentoRelacionado.setHistorico(pagamento.getConta().getContaVinculada().getNome());
            pagamentoRelacionado.setConta(pagamento.getConta().getContaVinculada());
            if (pagamentoRelacionado.getConta().isCredito()) {
                pagamentoRelacionado.setValor(pagamento.getValor().negate());
            } else {
                pagamentoRelacionado.setValor(pagamento.getValor().negate());
            }
            pagamentoRelacionado.setSaldo(new BigDecimal(0));
            pagamentoRelacionado.setDadosPagamento(pagamento.getDadosPagamento());


            pagamentoRelacionado.setContaCorrente(Main.getCondominio().getContaCorrente());
            pagamentoRelacionado.setPago(true);


            transacao.adicionarPagamento(pagamento);
            transacao.adicionarPagamento(pagamentoRelacionado);

            verificarListaVaziaContaCorrente(pagamentoRelacionado);
            Main.getCondominio().getContaCorrente().adicionarPagamento(pagamentoRelacionado);
            Main.getCondominio().getContaCorrente().setSaldo(Main.getCondominio().getContaCorrente().getSaldo().add(pagamentoRelacionado.getValor()));

            System.out.println("Transacao Bancária: " + transacao);

            pagamento.setTransacaoBancaria(transacao);
            pagamentoRelacionado.setTransacaoBancaria(transacao);
        }

        return pagamento;

    }

    public static void verificarVinculoPagamento(Pagamento pagamento) {
        if (pagamento.getConta().getContaVinculada() != null) {
            TransacaoBancaria transacao = new TransacaoBancaria();
            if (pagamento.getTransacaoBancaria() != null) {
                transacao = pagamento.getTransacaoBancaria();
            }

            Pagamento pagamentoRelacionado = new Pagamento();
            if (transacao.getPagamentos() != null) {
                for (Pagamento p : transacao.getPagamentos()) {
                    if (!p.equals(pagamento)) {
                        pagamentoRelacionado = p;
                    }
                }
            }

            pagamentoRelacionado.setDataPagamento(pagamento.getDataPagamento());
            pagamentoRelacionado.setHistorico(pagamento.getConta().getContaVinculada().getNome());
            pagamentoRelacionado.setConta(pagamento.getConta().getContaVinculada());
            if (pagamentoRelacionado.getConta().isCredito()) {
                pagamentoRelacionado.setValor(pagamento.getValor().negate());
            } else {
                pagamentoRelacionado.setValor(pagamento.getValor().negate());
            }
            pagamentoRelacionado.setSaldo(new BigDecimal(0));
            pagamentoRelacionado.setDadosPagamento(pagamento.getDadosPagamento());

            String nome = pagamentoRelacionado.getConta().getNomeVinculo();

            if (nome.equals("AF")) {
                pagamentoRelacionado.setAplicacao(Main.getCondominio().getAplicacao());
            } else if (nome.equals("PO")) {
                pagamentoRelacionado.setPoupanca(Main.getCondominio().getPoupanca());
            } else if (nome.equals("CO")) {
                pagamentoRelacionado.setConsignacao(Main.getCondominio().getConsignacao());
            } else if (nome.equals("EM")) {
            }

            pagamentoRelacionado.setPago(true);


            transacao.adicionarPagamento(pagamento);
            transacao.adicionarPagamento(pagamentoRelacionado);

            if (nome.equals("AF")) {

                verificarListaVaziaAplicacao(pagamentoRelacionado);
                Main.getCondominio().getAplicacao().adicionarPagamento(pagamentoRelacionado);
                Main.getCondominio().getAplicacao().setSaldo(Main.getCondominio().getAplicacao().getSaldo().add(pagamentoRelacionado.getValor()));

            } else if (nome.equals("PO")) {

                verificarListaVaziaPoupanca(pagamentoRelacionado);
                Main.getCondominio().getPoupanca().adicionarPagamento(pagamentoRelacionado);
                Main.getCondominio().getPoupanca().setSaldo(Main.getCondominio().getPoupanca().getSaldo().add(pagamentoRelacionado.getValor()));

            } else if (nome.equals("CO")) {

                verificarListaVaziaConsignacao(pagamentoRelacionado);
                Main.getCondominio().getConsignacao().adicionarPagamento(pagamentoRelacionado);
                Main.getCondominio().getConsignacao().setSaldo(Main.getCondominio().getConsignacao().getSaldo().add(pagamentoRelacionado.getValor()));

            } else if (nome.equals("EM")) {
            }

            pagamento.setTransacaoBancaria(transacao);
            pagamentoRelacionado.setTransacaoBancaria(transacao);
        }
    }

    public static void verificarListaVaziaAplicacao(Pagamento p2) {
        if (Main.getCondominio().getAplicacao().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            Main.getCondominio().getAplicacao().setSaldo(p2.getValor());
        }
    }

    public static void verificarListaVaziaPoupanca(Pagamento p2) {
        if (Main.getCondominio().getPoupanca().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            Main.getCondominio().getPoupanca().setSaldo(p2.getValor());
        }
    }

    public static void verificarListaVaziaConsignacao(Pagamento p2) {
        if (Main.getCondominio().getConsignacao().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            Main.getCondominio().getConsignacao().setSaldo(p2.getValor());
        }
    }

    public static void verificarListaVaziaContaCorrente(Pagamento p2) {
        if (Main.getCondominio().getContaCorrente().getPagamentos().isEmpty()) {
            p2.setSaldo(p2.getValor());
            Main.getCondominio().getContaCorrente().setSaldo(p2.getValor());
        }
    }
    
    public static String formatarMoeda(Double valor) {

        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(2);
        String valorFormatado;

        return valorFormatado = format.format(valor);
    }
}




