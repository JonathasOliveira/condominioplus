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
    INADIMPLENCIA_ANALITICA("Inadimplência Analítica"),
    ASSEMBLEIA_ORDINARIA("Assembleia Ordinária"),
    ASSEMBLEIA_EXTRAORDINARIA("Assembleia Extraordinária"),
    ENVELOPE_PEQUENO("Envelope Pequeno"),
    RELACAO_PROPRIETARIOS("Relação de Proprietários"),
    RELACAO_PROPRIETARIOS_UNIDADE("Relação de Proprietários - Unidade"),
    RELACAO_PROPRIETARIOS_EMAIL("Relação de Proprietários - Email"),
    RELACAO_POSTAGEM("Relação para Postagem"),
    CERTIFICADO_QUITACAO("Certificado de Quitação"),
    CARTA_SINTETICA("Carta Sintética"),
    CARTA_ANALITICA("Carta Analítica"),
    PAGAMENTOS_EFETUADOS_SINTETICO("Pagamentos Efetuados Sintético"),
    PAGAMENTOS_EFETUADOS_ANALITICO("Pagamentos Efetuados Analítico"),
    COBRANCAS_EXISTENTES_A_VENCER_SINTETICO("Cobranças Existentes à Vencer Sintético"),
    COBRANCAS_EXISTENTES_A_VENCER_ANALITICO("Cobranças Existentes à Vencer Analítico"),
    ANOTACOES_CONDOMINO("Anotações em Condômino"),
    ANOTACOES_CONDOMINIO("Anotações em Condomínio"),
    TAXA_EXTRA_GERENCIAL("Taxa Extra - Gerencial"),
    EXTRATO_CONTA_CORRENTE("Extrato Conta Corrente"),
    EXTRATO_CONFERENCIA_CONTA_CORRENTE("Extrato Conferência da Conta Corrente");

    TipoRelatorio(String nome) {
        this.nome = nome;
    }

    public String toString() {
        return nome;
    }
    private String nome;
}
