package br.pucgoias.ads.hotel.dominio;

import br.pucgoias.ads.hotel.excecao.ReservaNaoCancelavelException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospede_id", nullable = false)
    private Hospede hospede;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quarto_id", nullable = false)
    private Quarto quarto;

    /** Colunas check_in e check_out gravadas na propria tabela reserva. */
    @Embedded
    private Periodo periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusReserva status;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Version
    private Long versao;

    protected Reserva() {
    }

    public Reserva(Hospede hospede, Quarto quarto, Periodo periodo) {
        this.hospede = hospede;
        this.quarto = quarto;
        this.periodo = periodo;
        this.status = StatusReserva.ATIVA;
        this.valorTotal = quarto.calcularValor(periodo);
    }

    @PrePersist
    void registrarCriacao() {
        this.criadaEm = LocalDateTime.now();
    }

    /**
     * Regra R4: alterar apenas o estado do objeto. A gravacao ocorre por
     * dirty checking ao final da transacao, sem remover o registro.
     * Desafio complementar: apenas reservas ATIVAS podem ser canceladas.
     */
    public void cancelar() {
        if (status != StatusReserva.ATIVA) {
            throw new ReservaNaoCancelavelException(id, status);
        }
        this.status = StatusReserva.CANCELADA;
    }

    public Long getId() {
        return id;
    }

    public Hospede getHospede() {
        return hospede;
    }

    public Quarto getQuarto() {
        return quarto;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public Long getVersao() {
        return versao;
    }
}
