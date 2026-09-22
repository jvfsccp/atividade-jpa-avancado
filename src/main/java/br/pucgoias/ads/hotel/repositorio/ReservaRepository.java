package br.pucgoias.ads.hotel.repositorio;

import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /** R2: JPQL que verifica sobreposicao com reservas do status informado. */
    @Query("""
            select case when count(r) > 0 then true else false end
            from Reserva r
            where r.quarto.id = :quartoId
              and r.status = :status
              and r.periodo.checkIn < :checkOut
              and r.periodo.checkOut > :checkIn
            """)
    boolean existeConflito(@Param("quartoId") Long quartoId,
                           @Param("status") StatusReserva status,
                           @Param("checkIn") LocalDate checkIn,
                           @Param("checkOut") LocalDate checkOut);

    /** R5: consulta derivada paginada. */
    Page<Reserva> findByHospedeId(Long hospedeId, Pageable pageable);

    /** R6: projecao com expressao construtora (select new ... OcupacaoQuarto). */
    @Query("""
            select new br.pucgoias.ads.hotel.dto.OcupacaoQuarto(
                q.numero, count(r), sum(r.valorTotal))
            from Reserva r join r.quarto q
            where r.status = :status
            group by q.numero
            order by q.numero
            """)
    List<OcupacaoQuarto> relatorioOcupacao(@Param("status") StatusReserva status);

    /** R7: carregar hospede e quarto na mesma instrucao SQL. */
    @EntityGraph(attributePaths = {"hospede", "quarto"})
    List<Reserva> findByStatusOrderById(StatusReserva status);
}
