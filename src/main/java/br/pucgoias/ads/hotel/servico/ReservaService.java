package br.pucgoias.ads.hotel.servico;

import br.pucgoias.ads.hotel.dominio.Hospede;
import br.pucgoias.ads.hotel.dominio.Periodo;
import br.pucgoias.ads.hotel.dominio.Quarto;
import br.pucgoias.ads.hotel.dominio.Reserva;
import br.pucgoias.ads.hotel.dominio.StatusReserva;
import br.pucgoias.ads.hotel.dto.OcupacaoQuarto;
import br.pucgoias.ads.hotel.excecao.QuartoIndisponivelException;
import br.pucgoias.ads.hotel.excecao.RecursoNaoEncontradoException;
import br.pucgoias.ads.hotel.repositorio.HospedeRepository;
import br.pucgoias.ads.hotel.repositorio.QuartoRepository;
import br.pucgoias.ads.hotel.repositorio.ReservaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservaService {

    private final HospedeRepository hospedes;
    private final QuartoRepository quartos;
    private final ReservaRepository reservas;

    public ReservaService(HospedeRepository hospedes, QuartoRepository quartos, ReservaRepository reservas) {
        this.hospedes = hospedes;
        this.quartos = quartos;
        this.reservas = reservas;
    }

    /** R1, R2 e R3: operacao fornecida como referencia. */
    @Transactional
    public Reserva reservar(Long hospedeId, Long quartoId, LocalDate checkIn, LocalDate checkOut) {
        Hospede hospede = hospedes.findById(hospedeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Hospede", hospedeId));
        Quarto quarto = quartos.findById(quartoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", quartoId));

        Periodo periodo = new Periodo(checkIn, checkOut);

        if (reservas.existeConflito(quartoId, StatusReserva.ATIVA, checkIn, checkOut)) {
            throw new QuartoIndisponivelException(quarto.getNumero());
        }
        return reservas.save(new Reserva(hospede, quarto, periodo));
    }

    /** R4: localiza a reserva e a cancela sem chamar save (dirty checking). */
    @Transactional
    public void cancelar(Long reservaId) {
        Reserva reserva = reservas.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva", reservaId));
        reserva.cancelar();
    }

    /** R5: pagina solicitada, ordenada por periodo.checkIn decrescente. */
    public Page<Reserva> listarPorHospede(Long hospedeId, int pagina, int tamanho) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("periodo.checkIn").descending());
        return reservas.findByHospedeId(hospedeId, pageRequest);
    }

    /** R6: somente reservas ATIVAS. */
    public List<OcupacaoQuarto> relatorioOcupacao() {
        return reservas.relatorioOcupacao(StatusReserva.ATIVA);
    }

    /** R7: reservas ATIVAS com hospede e quarto carregados. */
    public List<Reserva> listarAtivasComDetalhes() {
        return reservas.findByStatusOrderById(StatusReserva.ATIVA);
    }

    @Transactional
    public Quarto reajustarDiaria(Long quartoId, BigDecimal novoValor) {
        Quarto quarto = quartos.findById(quartoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", quartoId));
        quarto.reajustarDiaria(novoValor);
        return quarto;
    }
}
