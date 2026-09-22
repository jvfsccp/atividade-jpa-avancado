package br.pucgoias.ads.hotel.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Version;

import java.math.BigDecimal;

/**
 * Raiz da hierarquia de quartos. A estrategia SINGLE_TABLE grava todas as
 * subclasses na mesma tabela; a coluna "tipo" identifica a classe concreta.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
public abstract class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    /** Controle de concorrencia otimista (regra R8): incluida no WHERE de cada UPDATE. */
    @Version
    private Long versao;

    protected Quarto() {
    }

    protected Quarto(String numero, BigDecimal valorDiaria) {
        this.numero = numero;
        this.valorDiaria = valorDiaria;
    }

    /** Calculo polimorfico do valor da hospedagem (regra R3). */
    public abstract BigDecimal calcularValor(Periodo periodo);

    public void reajustarDiaria(BigDecimal novoValor) {
        if (novoValor == null || novoValor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da diaria deve ser positivo.");
        }
        this.valorDiaria = novoValor;
    }

    protected BigDecimal valorDasDiarias(Periodo periodo) {
        return valorDiaria.multiply(BigDecimal.valueOf(periodo.noites()));
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public Long getVersao() {
        return versao;
    }
}
