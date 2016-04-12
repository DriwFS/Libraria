
CREATE TABLE cdus
(
	id INT NOT NULL AUTO_INCREMENT,
	codigo VARCHAR(9) NOT NULL,
	tema VARCHAR(200) NOT NULL,

	UNIQUE (codigo),
	PRIMARY KEY(id)
);

CREATE TABLE livros
(
	id INT AUTO_INCREMENT,
	isbn CHAR(13),
	titulo VARCHAR(48),
	editora INT,
	preco FLOAT,
	publicacao DATE,
	paginas INT,
	capa INT,
	resumo VARCHAR(4096),
	sumario VARCHAR(1024),
	preco_custo FLOAT,
	margem_lucro FLOAT,

	PRIMARY KEY (id),
	UNIQUE (isbn),
	FOREIGN KEY (editora) REFERENCES editoras (id)
);

CREATE TABLE livros_categorias
(
	livro INT,
	cdu INT,

	PRIMARY KEY (livro, cdu),
	FOREIGN KEY (livro) REFERENCES livros (id),
	FOREIGN KEY (cdu) REFERENCES cdus (id)
)

CREATE TABLE editoras
(
	id INT AUTO_INCREMENT,
	cnpj CHAR(14),
	nome VARCHAR(32),
	endereco VARCHAR(48),
	telefone VARCHAR(11),

	PRIMARY KEY (id),
	UNIQUE (cnpj)
);

CREATE TABLE autores
(
	id INT AUTO_INCREMENT,
	nome VARCHAR(48),
	nascimento DATE,
	falecimento DATE,
	local_morte VARCHAR(32),
	biografia VARCHAR(512),

	PRIMARY KEY (id)
);

CREATE TABLE livros_autores
(
	livro INT,
	autor INT,

	PRIMARY KEY (livro, autor),
	FOREIGN KEY (livro) REFERENCES livros (id),
	FOREIGN KEY (autor) REFERENCES autores (id)
);

CREATE TABLE carrinhos
(
	id INT AUTO_INCREMENT,
	criado DATE,
	concluido DATE,
	estado INT,

	PRIMARY KEY (id)
);

CREATE TABLE carrinhos_itens
(
	carrinho INT,
	livro INT,
	quantidade INT,

	PRIMARY KEY (carrinho, livro),
	FOREIGN KEY (carrinho) REFERENCES carrinhos(id),
	FOREIGN KEY (livro) REFERENCES livros(id)
);
