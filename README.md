# Laboratório JPA/Hibernate Avançado: Rede Hoteleira

ADS1253 · Programação Orientada a Objetos com Banco de Dados · PUC Goiás

Módulo de reservas de uma rede hoteleira, implementado sobre o projeto inicial
`lab-hotel-starter`, conforme a Parte 2 (atividade prática de laboratório) da aula
de JPA/Hibernate Avançado.

## Tecnologias

Java 21, Spring Boot 3.5.5, Spring Data JPA, Hibernate, H2 em memória, JUnit 5 e AssertJ.

## Execução

```bash
./mvnw test              # suíte de testes
./mvnw spring-boot:run   # inicia a aplicação e exibe o DDL gerado no console
```

No Windows, use `mvnw.cmd`. Para usar MySQL, ative o perfil `mysql`
(`--spring.profiles.active=mysql`) e ajuste as credenciais em `application-mysql.properties`.

## O que foi implementado

| Etapa | Arquivo | Conteúdo |
|-------|---------|----------|
| 2 | `dominio/Periodo` | `@Embeddable`, colunas `check_in`/`check_out`, validação R1, `noites()`, `sobrepoe()`, `equals()`/`hashCode()` |
| 2 | `dominio/Quarto` | atributo `versao` com `@Version` |
| 2 | `dominio/QuartoSuite` | `@DiscriminatorValue("SUITE")`, coluna `taxa_servico`, `calcularValor()` |
| 2 | `dominio/Reserva` | `@ManyToOne` LAZY obrigatórios, `@Embedded`, `@Enumerated(STRING)`, `valor_total`, `criada_em` com `@PrePersist`, `@Version`, `cancelar()` |
| 3 | `repositorio/ReservaRepository` | JPQL de conflito, consulta derivada paginada, projeção `select new`, `@EntityGraph` |
| 4 | `servico/ReservaService` | `cancelar`, `listarPorHospede`, `relatorioOcupacao`, `listarAtivasComDetalhes` |
| 5 | `ReservaServiceTest` | casos 6 a 9 |

## Regras de negócio e testes

| Regra | Descrição | Teste |
|-------|-----------|-------|
| R1 | Check-out posterior ao check-in | Caso 1 |
| R2 | Sem reserva ATIVA sobreposta no mesmo quarto; canceladas não bloqueiam | Casos 2 e 3 |
| R3 | Valor calculado polimorficamente (standard / suíte) | Casos 4 e 5 |
| R4 | Cancelamento por dirty checking, sem remoção e sem `save` | Caso 6 |
| R5 | Listagem paginada por hóspede, check-in decrescente | Caso 7 |
| R6 | Relatório de ocupação por projeção (`OcupacaoQuarto`) | Caso 8 |
| R7 | Reservas ATIVAS com hóspede e quarto em uma única instrução SQL | Caso 9 |
| R8 | Bloqueio otimista com `@Version` | Caso 10 (`ConcorrenciaTest`) |

Sem o `@EntityGraph` em `findByStatusOrderById`, o caso 9 falha com 5 instruções SQL
(1 consulta + 2 hóspedes + 2 quartos). Isso confirma que o teste mede de fato o problema N+1.

## Desafio complementar

As três extensões foram implementadas, com testes em `DesafioComplementarTest`:

1. **`QuartoFamilia`**: desconto de 10% a partir de cinco noites, sem alterar o serviço.
2. **Cancelamento apenas de reserva ATIVA**: `Reserva.cancelar()` lança `ReservaNaoCancelavelException`.
3. **`QuartoRepository.buscarParaAtualizacao`**: `@Lock(PESSIMISTIC_WRITE)`; o SQL gerado
   (`select ... from quarto q1_0 where q1_0.id=? for update`) está descrito em comentário.

Também há testes unitários do objeto de valor em `dominio/PeriodoTest`.
