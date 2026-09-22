package br.pucgoias.ads.hotel.dominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Desafio complementar: terceiro tipo de quarto, incluido sem alterar o servico.
 * Familia: valor das diarias, com desconto de 10% a partir de cinco noites.
 */
@Entity
@DiscriminatorValue("FAMILIA")
public class QuartoFamilia extends Quarto {

    private static final long NOITES_PARA_DESCONTO = 5;
    private static final BigDecimal FATOR_DESCONTO = new BigDecimal("0.90");

    protected QuartoFamilia() {
    }

    public QuartoFamilia(String numero, BigDecimal valorDiaria) {
        super(numero, valorDiaria);
    }

    @Override
    public BigDecimal calcularValor(Periodo periodo) {
        BigDecimal valor = valorDasDiarias(periodo);
        if (periodo.noites() >= NOITES_PARA_DESCONTO) {
            valor = valor.multiply(FATOR_DESCONTO);
        }
        return valor.setScale(2, RoundingMode.HALF_EVEN);
    }
}
