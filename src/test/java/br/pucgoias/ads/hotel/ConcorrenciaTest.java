package br.pucgoias.ads.hotel;

import br.pucgoias.ads.hotel.dominio.Quarto;
import br.pucgoias.ads.hotel.dominio.QuartoStandard;
import br.pucgoias.ads.hotel.repositorio.QuartoRepository;
import br.pucgoias.ads.hotel.repositorio.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cada chamada ao repositorio executa em transacao propria (NOT_SUPPORTED
 * desativa a transacao do teste), simulando dois usuarios que leram o mesmo
 * registro antes de qualquer alteracao. Caso fornecido no projeto inicial.
 */
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ConcorrenciaTest {

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private ReservaRepository reservas;

    @BeforeEach
    void limpar() {
        reservas.deleteAll();
        quartos.deleteAll();
    }

    @Test
    @DisplayName("10. R8 - atualizacao com versao desatualizada e rejeitada")
    void bloqueioOtimista() {
        Long id = quartos.save(new QuartoStandard("201", new BigDecimal("180.00"))).getId();

        Quarto copiaUsuarioA = quartos.findById(id).orElseThrow();
        Quarto copiaUsuarioB = quartos.findById(id).orElseThrow();

        copiaUsuarioA.reajustarDiaria(new BigDecimal("200.00"));
        quartos.save(copiaUsuarioA);                        // versao 0 -> 1

        copiaUsuarioB.reajustarDiaria(new BigDecimal("210.00"));
        assertThatThrownBy(() -> quartos.save(copiaUsuarioB)) // ainda possui versao 0
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        assertThat(quartos.findById(id).orElseThrow().getValorDiaria())
                .isEqualByComparingTo("200.00");
    }
}
