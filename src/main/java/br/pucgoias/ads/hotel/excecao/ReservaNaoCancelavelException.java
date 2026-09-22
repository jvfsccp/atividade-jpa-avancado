package br.pucgoias.ads.hotel.excecao;

import br.pucgoias.ads.hotel.dominio.StatusReserva;

/** Desafio complementar: uma reserva so pode ser cancelada se estiver ATIVA. */
public class ReservaNaoCancelavelException extends RuntimeException {

    public ReservaNaoCancelavelException(Long reservaId, StatusReserva statusAtual) {
        super("A reserva " + reservaId + " nao pode ser cancelada: status atual " + statusAtual + ".");
    }
}
