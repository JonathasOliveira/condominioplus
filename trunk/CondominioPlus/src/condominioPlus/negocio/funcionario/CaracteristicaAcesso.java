/*
 * Permissoes.java
 *
 * Created on 17/08/2007, 16:15:01
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package condominioPlus.negocio.funcionario;

/**
 *
 * @author USUARIO
 */
public enum CaracteristicaAcesso {

    VENDA_HISTORICO("Histórico de Vendas"),
    ORCAMENTO("Orçamento"),
    VENDA("Efetuar Venda"),
    VENDA_TELA_CHEIA("Venda em Tela Cheia"),
    VENDA_AJUSTE("Alterar valor de Ajuste na Venda"),
    VENDA_SEM_ESTOQUE("Efetuar Venda sem Produto em Estoque"),
    VENDA_SEM_CREDITO("Efetuar Venda para Cliente sem crédito"),
    VENDA_DETALHES("Visualizar Detalhes na Tela de Venda"),
    VENDA_VISUALIZAR_ACRESCIMO("Visualizar Acréscimo na Tela de Pagamento da Venda"),
    VENDA_EDITAR_PAGAMENTO("Editar Pagamentos Livremente na Venda"),
    ESTORNO_EFETUAR("Efetuar Estorno de Venda"),
    FORNECEDOR_CADASTRO("Cadastro de Fornecedores"),
    FORNECEDOR_CATEGORIA_CADASTRO("Cadastro de Categoria de Fornecedor"),
    PRODUTO_VISUALIZAR("Visualizar Produtos"),
    PRODUTO_VISUALIZAR_CUSTO("Visualizar Custo de Produtos"),
    PRODUTO_VISUALIZAR_DETALHES("Visualizar Detalhes dos Produtos"),
    PRODUTO_CADASTRO("Cadastro de Produtos"),
    PRODUTO_ALTERAR_QUANTIDADE("Alterar a Quantidade de Produtos"),
    PRODUTO_CATEGORIA_CADASTRO("Cadastro de Categoria de Produto"),
    ENTRADA_DE_PRODUTO("Entrada de Produtos"),
    DEVOLUCAO_EFETUAR("Efetuar Devolução de Entradas"),
    DESFAZER_ENTRADA("Desfazer Entrada"),
    ORCAMENTO_DE_COMPRA("Orçamento de Compra"),
    PATRIMONIO("Patrimônio"),
    BAIXA_DE_PRODUTO("Baixa de Produtos"),
    CLIENTE_CADASTRO("Cadastro de Clientes"),
    CLIENTE_CREDITO("Editar Crédito de Clientes"),
    ENTREGA_PREVISTA("Entregas Previstas"),
    ENTREGA_REALIZADA("Entregas Realizadas"),
    VEICULO_CADASTRO("Cadastro de Veiculos"),
    FUNCIONARIO_CADASTRO("Cadastro de Funcionários"),
    FUNCIONARIO_CATEGORIA_CADASTRO("Cadastro de Categoria de Funcionário"),
    CONTROLE_DE_ACESSO("Controle de Acesso"),
    RELATORIO("Relatórios"),
    PAGAMENTO("Fluxo de Caixa"),
    EDITAR_PAGAMENTO("Editar Pagamentos"),
    EDITAR_JUROS("Editar Juros"),
    PLANO_DE_CONTAS("Plano de Contas"),
    COMISSAO("Comissão"),
    BANCO("Banco"),
    CONFIGURACAO("Configurações"),
    INTEGRACAO("Integração entre Locais");

    CaracteristicaAcesso(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }
    private String nome;
}
