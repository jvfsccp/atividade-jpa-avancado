package br.pucgoias.ads.hotel;

import br.pucgoias.ads.hotel.dominio.Hospede;
import br.pucgoias.ads.hotel.dominio.Quarto;
import br.pucgoias.ads.hotel.dominio.QuartoStandard;
import br.pucgoias.ads.hotel.dominio.QuartoSuite;
import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import br.pucgoias.ads.hotel.excecao.PeriodoInvalidoException;
import br.pucgoias.ads.hotel.excecao.QuartoIndisponivelException;
import br.pucgoias.ads.hotel.servico.ReservaService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ReservaService.class)
class ReservaServiceTest {

    private static final LocalDate DIA_1 = LocalDate.of(2026, 10, 1);

    @Autowired
    private ReservaService service;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Hospede ana;
    private Hospede bruno;
    private Quarto standard;
    private Quarto suite;

    @BeforeEach
    void prepararDados() {
        ana = em.persist(new Hospede("Ana Souza", "11111111111", "ana@email.com"));
        bruno = em.persist(new Hospede("Bruno Lima", "22222222222", "bruno@email.com"));
        standard = em.persist(new QuartoStandard("101", new BigDecimal("200.00")));
        suite = em.persist(new QuartoSuite("501", new BigDecimal("500.00"), new BigDecimal("150.00")));
        em.flush();
    }

    // ---------------------------------------------------------------
    // Casos 1 a 5: fornecidos no projeto inicial
    // ---------------------------------------------------------------

    @Test
    @DisplayName("1. R1 - check-out igual ao check-in lanca PeriodoInvalidoException")
    void periodoInvalido() {
        assertThatThrownBy(() -> service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    @DisplayName("2. R2 - reserva sobreposta no mesmo quarto lanca QuartoIndisponivelException")
    void conflitoDeReserva() {
        service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(3));

        assertThatThrownBy(() ->
                service.reservar(bruno.getId(), standard.getId(), DIA_1.plusDays(2), DIA_1.plusDays(5)))
                .isInstanceOf(QuartoIndisponivelException.class)
                .hasMessageContaining("101");
    }

    @Test
    @DisplayName("3. R2 - reserva cancelada nao bloqueia o periodo")
    void reservaCanceladaNaoBloqueia() {
        Reserva primeira = service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(3));
        service.cancelar(primeira.getId());

        Reserva segunda = service.reservar(bruno.getId(), standard.getId(), DIA_1, DIA_1.plusDays(3));

        assertThat(segunda.getId()).isNotNull();
    }

    @Test
    @DisplayName("4. R3 - quarto standard: diaria x noites")
    void valorQuartoStandard() {
        Reserva reserva = service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(3));

        assertThat(reserva.getValorTotal()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("5. R3 - suite: diaria x noites + taxa de servico")
    void valorSuite() {
        Reserva reserva = service.reservar(ana.getId(), suite.getId(), DIA_1, DIA_1.plusDays(2));

        assertThat(reserva.getValorTotal()).isEqualByComparingTo("1150.00");
    }

    // ---------------------------------------------------------------
    // Casos 6 a 9: escritos pelo aluno (Etapa 5)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("6. R4 - cancelamento persistido por dirty checking, sem remover o registro")
    void cancelamentoPorDirtyChecking() {
        Long id = service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(2)).getId();
        em.flush();
        em.clear();

        service.cancelar(id);
        em.flush();
        em.clear();

        Reserva recarregada = em.find(Reserva.class, id);
        assertThat(recarregada).isNotNull();
        assertThat(recarregada.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(recarregada.getVersao()).isEqualTo(1L);
    }

    @Test
    @DisplayName("7. R5 - listagem paginada e ordenada por check-in decrescente")
    void listagemPaginadaPorHospede() {
        service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(2));
        service.reservar(ana.getId(), standard.getId(), DIA_1.plusDays(20), DIA_1.plusDays(22));
        service.reservar(ana.getId(), suite.getId(), DIA_1.plusDays(10), DIA_1.plusDays(12));
        service.reservar(bruno.getId(), suite.getId(), DIA_1, DIA_1.plusDays(2));
        em.flush();
        em.clear();

        Page<Reserva> pagina = service.listarPorHospede(ana.getId(), 0, 2);

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
        assertThat(pagina.getContent())
                .extracting(r -> r.getPeriodo().getCheckIn())
                .containsExactly(DIA_1.plusDays(20), DIA_1.plusDays(10));
    }

    @Test
    @DisplayName("8. R6 - relatorio de ocupacao por projecao, apenas reservas ativas")
    void relatorioDeOcupacao() {
        service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(2));                    // 400.00
        service.reservar(bruno.getId(), standard.getId(), DIA_1.plusDays(5), DIA_1.plusDays(6));      // 200.00
        Reserva cancelada = service.reservar(ana.getId(), standard.getId(), DIA_1.plusDays(10), DIA_1.plusDays(15));
        service.reservar(ana.getId(), suite.getId(), DIA_1, DIA_1.plusDays(2));                       // 1150.00
        service.cancelar(cancelada.getId());
        em.flush();
        em.clear();

        List<OcupacaoQuarto> relatorio = service.relatorioOcupacao();

        assertThat(relatorio).hasSize(2);

        OcupacaoQuarto linhaStandard = relatorio.get(0);
        assertThat(linhaStandard.numeroQuarto()).isEqualTo("101");
        assertThat(linhaStandard.quantidadeReservas()).isEqualTo(2L);
        assertThat(linhaStandard.receita()).isEqualByComparingTo("600.00");

        OcupacaoQuarto linhaSuite = relatorio.get(1);
        assertThat(linhaSuite.numeroQuarto()).isEqualTo("501");
        assertThat(linhaSuite.quantidadeReservas()).isEqualTo(1L);
        assertThat(linhaSuite.receita()).isEqualByComparingTo("1150.00");
    }

    @Test
    @DisplayName("9. R7 - reservas ativas com hospede e quarto em uma unica instrucao SQL")
    void listagemSemProblemaNMaisUm() {
        service.reservar(ana.getId(), standard.getId(), DIA_1, DIA_1.plusDays(2));
        service.reservar(bruno.getId(), suite.getId(), DIA_1, DIA_1.plusDays(2));
        service.reservar(ana.getId(), suite.getId(), DIA_1.plusDays(10), DIA_1.plusDays(12));
        em.flush();
        em.clear(); // sem o clear, as entidades ja estariam no contexto e o teste nao mediria nada

        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        List<Reserva> ativas = service.listarAtivasComDetalhes();
        for (Reserva r : ativas) {
            assertThat(r.getHospede().getNome()).isNotBlank();
            assertThat(r.getQuarto().getNumero()).isNotBlank();
        }

        assertThat(ativas).hasSize(3);
        assertThat(estatisticas.getPrepareStatementCount()).isEqualTo(1);
    }
}
