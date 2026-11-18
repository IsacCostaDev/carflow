CREATE TABLE custos_preparacao (
    id UUID PRIMARY KEY,
    loja_id UUID NOT NULL,
    veiculo_id UUID NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(12,2) NOT NULL,
    data_realizacao DATE NOT NULL,
    fornecedor VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_custos_preparacao_loja FOREIGN KEY (loja_id) REFERENCES lojas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_custos_preparacao_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id) ON DELETE CASCADE
);

CREATE INDEX idx_custos_preparacao_loja_id ON custos_preparacao(loja_id);
CREATE INDEX idx_custos_preparacao_veiculo_id ON custos_preparacao(veiculo_id);
CREATE INDEX idx_custos_preparacao_loja_veiculo ON custos_preparacao(loja_id, veiculo_id);