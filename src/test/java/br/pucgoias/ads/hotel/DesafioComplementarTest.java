package br.pucgoias.ads.hotel;

import br.pucgoias.ads.hotel.dominio.Hospede;
import br.pucgoias.ads.hotel.dominio.Quarto;
import br.pucgoias.ads.hotel.dominio.QuartoFamilia;
import br.pucgoias.ads.hotel.dominio.QuartoStandard;
import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.excecao.ReservaNaoCancelavelException;
import br.pucgoias.ads.hotel.repositorio.QuartoRepository;
import br.pucgoias.ads.hotel.servico.ReservaService;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Desafio complementar: um caso de teste para cada extensao. */
@DataJpaTest
@Import(ReservaService.class)
class DesafioComplementarTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 11, 1);

    @Autowired
    private ReservaService service;

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private TestEntityManager em;

    private Hospede ana;

    @BeforeEach
    void prepararDados() {
        ana = em.persist(new Hospede("Ana Souza", "11111111111", "ana@email.com"));
    }

    @Test
    @DisplayName("QuartoFamilia - desconto de 10% a partir de cinco noites, sem alterar o servico")
    void quartoFamilia() {
        Quarto familia = em.persist(new QuartoFamilia("301", new BigDecimal("250.00")));

        Reserva quatroNoites = service.reservar(ana.getId(), familia.getId(), DIA_1, DIA_1.plusDays(4));
        Reserva cincoNoites = service.reservar(ana.getId(), familia.getId(), DIA_1.plusDays(10), DIA_1.plusDays(15));

        assertThat(quatroNoites.getValorTotal()).isEqualByComparingTo("1000.00");
        assertThat(cincoNoites.getValorTotal()).isEqualByComparingTo("1125.00");

        em.flush();
        em.clear();
        assertThat(quartos.findById(familia.getId())).get().isInstanceOf(QuartoFamilia.class);
    }

    @Test
    @DisplayName("Cancelamento - apenas reservas ATIVAS podem ser canceladas")
    void cancelamentoApenasDeReservaAtiva() {
        Quarto standard = em.persist(new QuartoStandard("101", new BigDecimal("200.00")));
        Long id = service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(2)).getId();
        service.cancelar(id);

        assertThatThrownBy(() -> service.cancelar(id))
                .isInstanceOf(ReservaNaoCancelavelException.class)
                .hasMessageContaining("CANCELADA");
    }

    @Test
    @DisplayName("buscarParaAtualizacao - quarto obtido com bloqueio pessimista (select ... for update)")
    void bloqueioPessimista() {
        Long id = em.persistAndFlush(new QuartoStandard("102", new BigDecimal("200.00"))).getId();
        em.clear();

        Quarto quarto = quartos.buscarParaAtualizacao(id).orElseThrow();

        assertThat(em.getEntityManager().getLockMode(quarto)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
