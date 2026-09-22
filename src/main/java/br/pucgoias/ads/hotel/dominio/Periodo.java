package br.pucgoias.ads.hotel.dominio;

import br.pucgoias.ads.hotel.excecao.PeriodoInvalidoException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Objeto de valor que representa o periodo de uma reserva.
 * Nao possui identidade nem tabela propria: as colunas check_in e check_out
 * sao gravadas na tabela da entidade que o contem.
 */
@Embeddable
public class Periodo {

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    /** Construtor exigido pela especificacao JPA. */
    protected Periodo() {
    }

    public Periodo(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new PeriodoInvalidoException("check-in e check-out sao obrigatorios");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new PeriodoInvalidoException("check-out deve ser posterior ao check-in");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public long noites() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Intervalos semiabertos [a, b) e [c, d) se sobrepoem se, e somente se, a < d e c < b. */
    public boolean sobrepoe(Periodo outro) {
        return checkIn.isBefore(outro.checkOut) && outro.checkIn.isBefore(checkOut);
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Periodo outro)) {
            return false;
        }
        return Objects.equals(checkIn, outro.checkIn) && Objects.equals(checkOut, outro.checkOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkIn, checkOut);
    }

    @Override
    public String toString() {
        return "[" + checkIn + ", " + checkOut + ")";
    }
}
