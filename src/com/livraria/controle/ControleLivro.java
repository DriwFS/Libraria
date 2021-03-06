
package com.livraria.controle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.diverproject.util.sql.MySQL;

import com.livraria.Conexao;
import com.livraria.entidades.Autor;
import com.livraria.entidades.Categoria;
import com.livraria.entidades.Editora;
import com.livraria.entidades.Livro;
import com.livraria.entidades.LivroAutores;
import com.livraria.entidades.LivroCategorias;
import com.livraria.util.ComponentUtil;

public class ControleLivro
{
	private static final Connection connection;

	static
	{
		MySQL mysql = Conexao.getMySQL();
		connection = mysql.getConnection();
	}

	public boolean adicionar(Livro livro) throws SQLException
	{
		if (livro.getPublicacao() == null)
			throw new SQLException("publicação nula");

		if (livro.getEditora() == null)
			throw new SQLException("editora nula");

		Date publicacao = new Date(livro.getPublicacao().getTime());

		String sql = "INSERT INTO livros (isbn, titulo, editora, preco, publicacao, paginas, "
					+"capa, resumo, sumario)"
					+" values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, ComponentUtil.isbnClear(livro.getIsbn()));
		ps.setString(2, livro.getTitulo());
		ps.setInt(3, livro.getEditora().getID());
		ps.setFloat(4, livro.getPreco());
		ps.setDate(5, publicacao);
		ps.setInt(6, livro.getPaginas());
		ps.setInt(7, livro.getCapa());
		ps.setString(8, livro.getResumo());
		ps.setString(9, livro.getSumario());

		if (ps.executeUpdate() != Conexao.INSERT_SUCCESSFUL)
			return false;

		sql = "SELECT auto_increment FROM INFORMATION_SCHEMA.TABLES WHERE table_name = 'livros'";
		ps = connection.prepareStatement(sql);

		ResultSet rs = ps.executeQuery();
		rs.next();

		livro.setID(rs.getInt("auto_increment") - 1);

		for (Categoria categoria : livro.getLivroCategorias().listar())
			if (!adicionarCategoria(livro, categoria))
				throw new SQLException("falha ao adicionar a categoria " +categoria.getTema());

		for (Autor autor : livro.getLivroAutores().listar())
			if (!adicionarAutor(livro, autor))
				throw new SQLException("falha ao adicionar o autor " +autor.getNome());

		return true;
	}

	public boolean atualizar(Livro livro) throws SQLException
	{
		if (livro.getPublicacao() == null)
			throw new SQLException("publicação nula");

		if (livro.getEditora() == null)
			throw new SQLException("editora nula");

		Date publicacao = new Date(livro.getPublicacao().getTime());

		String sql = "UPDATE livros SET isbn = ?, titulo = ?, editora = ?, preco = ?, publicacao = ?,"
					+" paginas = ?, capa = ?, resumo = ?, sumario = ?"
					+" WHERE id = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, ComponentUtil.isbnClear(livro.getIsbn()));
		ps.setString(2, livro.getTitulo());
		ps.setInt(3, livro.getEditora().getID());
		ps.setFloat(4, livro.getPreco());
		ps.setDate(5, publicacao);
		ps.setInt(6, livro.getPaginas());
		ps.setInt(7, livro.getCapa());
		ps.setString(8, livro.getResumo());
		ps.setString(9, livro.getSumario());
		ps.setInt(10, livro.getID());

		for (Categoria categoria : livro.getLivroCategorias().listar())
			adicionarCategoria(livro, categoria);

		for (Autor autor : livro.getLivroAutores().listar())
			adicionarAutor(livro, autor);

		return ps.executeUpdate() != PreparedStatement.EXECUTE_FAILED;
	}

	public boolean excluir(Livro livro) throws SQLException
	{
		removerAutor(livro);
		removerCategoria(livro);

		String sql = "DELETE FROM livros WHERE id = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());

		return ps.executeUpdate() != PreparedStatement.EXECUTE_FAILED;
	}

	public Livro selecionar(int id) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, id);

		ResultSet rs = ps.executeQuery();

		if (!rs.next())
			return null;

		Livro livro = criar(rs);
		carregarCategorias(livro);
		carregarAutores(livro);

		return livro;
	}

	private boolean adicionarCategoria(Livro livro, Categoria categoria) throws SQLException
	{
		String sql = "SELECT * FROM livros_categorias WHERE livro = ? AND categoria = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());
		ps.setInt(2, categoria.getID());

		if (ps.executeQuery().next())
			return true;

		sql = "INSERT INTO livros_categorias (livro, categoria) VALUES (?, ?)";

		ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());
		ps.setInt(2, categoria.getID());

		return ps.executeUpdate() == Conexao.INSERT_SUCCESSFUL;
	}

	private boolean removerCategoria(Livro livro) throws SQLException
	{
		String sql = "DELETE FROM livros_categorias WHERE livro = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());

		return ps.executeUpdate() == PreparedStatement.EXECUTE_FAILED;
	}

	public void carregarCategorias(Livro livro) throws SQLException
	{
		String sql = "SELECT categorias.id, categorias.codigo, categorias.tema FROM livros_categorias "
					+"INNER JOIN categorias ON categorias.id = livros_categorias.categoria "
					+"WHERE livros_categorias.livro = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());

		LivroCategorias categorias = livro.getLivroCategorias();
		categorias.limpar();

		ResultSet rs = ps.executeQuery();

		while (rs.next())
		{
			Categoria categoria = new Categoria();
			categoria.setID(rs.getInt("id"));
			categoria.setCodigo(rs.getString("codigo"));
			categoria.setTema(rs.getString("tema"));
			categorias.adicionar(categoria);
		}
	}

	private boolean adicionarAutor(Livro livro, Autor autor) throws SQLException
	{
		String sql = "SELECT * FROM livros_autores WHERE livro = ? AND autor = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());
		ps.setInt(2, autor.getID());

		if (ps.executeQuery().next())
			return true;

		sql = "INSERT INTO livros_autores (livro, autor) VALUES (?, ?)";

		ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());
		ps.setInt(2, autor.getID());

		return ps.executeUpdate() == Conexao.INSERT_SUCCESSFUL;
	}

	private boolean removerAutor(Livro livro) throws SQLException
	{
		String sql = "DELETE FROM livros_autores WHERE livro = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());

		return ps.executeUpdate() == PreparedStatement.EXECUTE_FAILED;
	}

	public void carregarAutores(Livro livro) throws SQLException
	{
		String sql = "SELECT autores.id, autores.nome, autores.nascimento, autores.falecimento, "
					+"autores.local_morte, autores.biografia FROM livros_autores "
					+"INNER JOIN autores ON autores.id = livros_autores.autor "
					+"WHERE livros_autores.livro = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, livro.getID());

		LivroAutores autores = livro.getLivroAutores();
		autores.limpar();

		ResultSet rs = ps.executeQuery();

		while (rs.next())
		{
			Autor autor = new Autor();
			autor.setID(rs.getInt("id"));
			autor.setNome(rs.getString("nome"));
			autor.setNascimento(rs.getDate("nascimento"));
			autor.setFalecimento(rs.getDate("falecimento"));
			autor.setLocalMorte(rs.getString("local_morte"));
			autor.setBiografia(rs.getString("biografia"));
			autores.adicionar(autor);
		}
	}

	public List<Livro> listar() throws SQLException
	{
		String sql = "SELECT * FROM livros";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorISBN(String isbn) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE isbn LIKE '" +isbn+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorTitulo(String titulo) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE titulo LIKE '%" +titulo+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorResumo(String resumo) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE resumo LIKE '%" +resumo+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorSumario(String sumario) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE sumario LIKE '%" +sumario+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorPreco(float minimo, float maximo) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE preco >= ? AND preco <= ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setFloat(1, minimo);
		ps.setFloat(2, maximo);

		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorAutor(Autor autor) throws SQLException
	{
		String sql = "SELECT * FROM livros "
					+"INNER JOIN livros_autores ON livros.id = livros_autores.livro "
					+"INNER JOIN autores ON livros_autores.autor = autores.id "
					+ "WHERE autores.id = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, autor.getID());

		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Livro> filtrarPorEditora(Editora editora) throws SQLException
	{
		String sql = "SELECT * FROM livros WHERE editora = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, editora.getID());

		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	private List<Livro> concluirFiltragem(ResultSet rs) throws SQLException
	{
		List<Livro> livros = new ArrayList<Livro>();

		while (rs.next())
		{
			Livro livro = criar(rs);
			livros.add(livro);
		}

		return livros;
	}

	private Livro criar(ResultSet rs) throws SQLException
	{
		ControleEditora controleEditora = new ControleEditora();
		Editora editora = controleEditora.selecionar(rs.getInt("editora"));

		Livro livro = new Livro();
		livro.setID(rs.getInt("id"));
		livro.setIsbn(rs.getString("isbn"));
		livro.setTitulo(rs.getString("titulo"));
		livro.setEditora(editora);
		livro.setPreco(rs.getFloat("preco"));
		livro.setPublicacao(rs.getDate("publicacao"));
		livro.setPaginas(rs.getInt("paginas"));
		livro.setCapa(rs.getInt("capa"));
		livro.setResumo(rs.getString("resumo"));
		livro.setSumario(rs.getString("sumario"));

		return livro;
	}

	public boolean existe(String titulo) throws SQLException
	{
		String sql = "SELECT COUNT(*) as count FROM livros WHERE titulo = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, titulo);

		ResultSet rs = ps.executeQuery();
		rs.next();

		return rs.getInt("count") != 0;
	}

	public void trucate() throws SQLException
	{
		PreparedStatement ps = null;

		ps = connection.prepareStatement("TRUNCATE livros_categorias");	ps.executeUpdate();
		ps = connection.prepareStatement("TRUNCATE livros_autores");	ps.executeUpdate();
		ps = connection.prepareStatement("TRUNCATE livros");			ps.executeUpdate();
	}
}
