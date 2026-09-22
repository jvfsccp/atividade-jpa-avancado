package br.pucgoias.ads.hotel.repositorio;

import br.pucgoias.ads.hotel.dominio.Quarto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    /**
     * Desafio complementar: bloqueio pessimista. Exige transacao ativa; a linha
     * permanece travada no banco ate o commit ou rollback.
     *
     * Instrucao SQL exibida no console (H2):
     * <pre>
     * select q1_0.id, q1_0.tipo, q1_0.numero, q1_0.valor_diaria, q1_0.versao, q1_0.taxa_servico
     * from quarto q1_0
     * where q1_0.id=? for update
     * </pre>
     * O sufixo "for update" trava a linha: outra transacao que tente altera-la,
     * ou le-la com bloqueio, aguarda o termino desta.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quarto q where q.id = :id")
    Optional<Quarto> buscarParaAtualizacao(@Param("id") Long id);
}
