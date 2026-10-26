CREATE TABLE veiculos (
    id UUID PRIMARY KEY,
    loja_id UUID NOT NULL,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    versao VARCHAR(100),
    ano_fabricacao INT NOT NULL,
    ano_modelo INT NOT NULL,
    placa VARCHAR(10) NOT NULL,
    chassi VARCHAR(17) NOT NULL,
    cor VARCHAR(30) NOT NULL,
    km_atual INT NOT NULL DEFAULT 0,
    combustivel VARCHAR(20) NOT NULL,
    cambio VARCHAR(20) NOT NULL,
    preco_compra NUMERIC(12,2) NOT NULL,
    preco_anuncio NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EM_PREPARACAO',
    descricao TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_veiculos_loja FOREIGN KEY (loja_id) REFERENCES lojas(id) ON DELETE RESTRICT,
    CONSTRAINT uk_veiculos_loja_placa UNIQUE (loja_id, placa),
    CONSTRAINT uk_veiculos_loja_chassi UNIQUE (loja_id, chassi)
);

CREATE INDEX idx_veiculos_loja_id ON veiculos(loja_id);
CREATE INDEX idx_veiculos_loja_status ON veiculos(loja_id, status);

CREATE TABLE opcionais (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT uk_opcionais_nome UNIQUE (nome)
);

CREATE TABLE veiculo_opcionais (
    veiculo_id UUID NOT NULL,
    opcional_id UUID NOT NULL,
    PRIMARY KEY (veiculo_id, opcional_id),
    CONSTRAINT fk_veiculo_opcionais_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id) ON DELETE CASCADE,
    CONSTRAINT fk_veiculo_opcionais_opcional FOREIGN KEY (opcional_id) REFERENCES opcionais(id) ON DELETE RESTRICT
);
