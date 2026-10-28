package com.isaccosta.carflow.veiculo;

import com.isaccosta.carflow.common.util.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "veiculos")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class Veiculo {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @TenantId
    @Column(name = "loja_id", nullable = false, updatable = false)
    private UUID lojaId;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    @Column(name = "versao", length = 100)
    private String versao;

    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;

    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;

    @Column(name = "placa", nullable = false, length = 10)
    private String placa;

    @Column(name = "chassi", nullable = false, length = 17)
    private String chassi;

    @Column(name = "cor", nullable = false, length = 30)
    private String cor;

    @Builder.Default
    @Column(name = "km_atual", nullable = false)
    private Integer kmAtual = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "combustivel", nullable = false, length = 20)
    private Combustivel combustivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "cambio", nullable = false, length = 20)
    private Cambio cambio;

    @Column(name = "preco_compra", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoCompra;

    @Column(name = "preco_anuncio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoAnuncio;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusVeiculo status = StatusVeiculo.EM_PREPARACAO;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Builder.Default
    @ManyToMany
    @JoinTable(name = "veiculo_opcionais", joinColumns = @JoinColumn(name = "veiculo_id"), inverseJoinColumns = @JoinColumn(name = "opcional_id"))
    private Set<Opcional> opcionais = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = IdGenerator.v7();
        }
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.status == null) {
            this.status = StatusVeiculo.EM_PREPARACAO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void transitionTo(StatusVeiculo newStatus) {
        if (this.status == null || !this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Transição de status inválida: não é permitido alterar de %s para %s.".formatted(this.status,
                            newStatus));
        }
        this.status = newStatus;
    }

    public void addOpcional(Opcional opcional) {
        this.opcionais.add(opcional);
    }

    public void removeOpcional(Opcional opcional) {
        this.opcionais.remove(opcional);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Veiculo other))
            return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
