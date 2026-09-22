package br.pucgoias.ads.hotel.dto;

import java.math.BigDecimal;

/**
 * Projecao somente leitura utilizada no relatorio de ocupacao (regra R6).
 * Nao e entidade: e instanciada pela expressao "select new" da JPQL.
 */
public record OcupacaoQuarto(String numeroQuarto, Long quantidadeReservas, BigDecimal receita) {
}
