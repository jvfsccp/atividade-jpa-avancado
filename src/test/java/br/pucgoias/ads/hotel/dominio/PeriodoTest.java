package br.pucgoias.ads.hotel.dominio;

import br.pucgoias.ads.hotel.excecao.PeriodoInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes unitarios do objeto de valor, sem banco de dados. */
class PeriodoTest {

    private static Periodo periodo(int checkIn, int checkOut) {
        return new Periodo(LocalDate.of(2026, 10, checkIn), LocalDate.of(2026, 10, checkOut));
    }

    @Test
    void rejeitaPeriodoVazioOuInvertido() {
        assertThatThrownBy(() -> periodo(5, 5)).isInstanceOf(PeriodoInvalidoException.class);
        assertThatThrownBy(() -> periodo(5, 4)).isInstanceOf(PeriodoInvalidoException.class);
        assertThatThrownBy(() -> new Periodo(null, LocalDate.of(2026, 10, 1)))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    void contaNoites() {
        assertThat(periodo(10, 13).noites()).isEqualTo(3);
    }

    @Test
    void sobreposicaoDeIntervalosSemiabertos() {
        assertThat(periodo(10, 15).sobrepoe(periodo(14, 18))).isTrue();
        assertThat(periodo(10, 15).sobrepoe(periodo(11, 12))).isTrue();
        assertThat(periodo(10, 15).sobrepoe(periodo(15, 18))).isFalse(); // check-out = check-in
        assertThat(periodo(15, 18).sobrepoe(periodo(10, 15))).isFalse();
    }

    @Test
    void igualdadePorValor() {
        assertThat(periodo(10, 12)).isEqualTo(periodo(10, 12)).hasSameHashCodeAs(periodo(10, 12));
        assertThat(periodo(10, 12)).isNotEqualTo(periodo(10, 13));
    }
}
