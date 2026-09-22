package br.pucgoias.ads.hotel.excecao;

public class QuartoIndisponivelException extends RuntimeException {

    public QuartoIndisponivelException(String numeroQuarto) {
        super("O quarto " + numeroQuarto + " possui reserva ativa no periodo solicitado.");
    }
}
