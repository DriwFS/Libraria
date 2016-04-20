
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

public class ControleAutor
{
	private static final Connection connection;

	static
	{
		MySQL mysql = Conexao.getMySQL();
		connection = mysql.getConnection();
	}
	
	public boolean adicionar(Autor autor) throws SQLException
	{
		Date nascimento = null;
		Date falecimento = null;

		if (autor.getNascimento() != null)
			nascimento = new Date(autor.getNascimento().getTime());

		if (autor.getFalecimento() != null)
			falecimento = new Date(autor.getFalecimento().getTime());

		String sql = "INSERT INTO autores (nome, nascimento, falecimento, local_morte, biografia)"
					+" values (?, ?, ?, ?, ?)";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, autor.getNome());
		ps.setDate(2, nascimento);
		ps.setDate(3, falecimento);
		ps.setString(4, autor.getLocalMorte());
		ps.setString(5, autor.getBiografia());

		return ps.executeUpdate() == Conexao.INSERT_SUCCESSFUL;
	}

	public boolean atualizar(Autor autor) throws SQLException
	{
		Date nascimento = new Date(autor.getNascimento().getTime());
		Date falecimento = new Date(autor.getFalecimento().getTime());

		String sql = "UPDATE autores SET nome = ?, nascimento = ?, falecimento = ?,"
					+" local_morte = ?, biografia = ? WHERE id = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setString(1, autor.getNome());
		ps.setDate(2, nascimento);
		ps.setDate(3, falecimento);
		ps.setString(4, autor.getLocalMorte());
		ps.setString(5, autor.getBiografia());
		ps.setInt(6, autor.getID());

		return ps.executeUpdate() == Conexao.UPDATE_SUCCESSFUL;
	}

	public boolean excluir(int id) throws SQLException
	{
		String sql = "DELETE FROM autores WHERE id = ?";

		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, id);

		return ps.executeUpdate() == Conexao.DELETE_SUCCESSFUL;
	}

	public Autor selecionar(int id) throws SQLException
	{
		Autor autor = new Autor();

		String sql = "SELECT * FROM autores WHERE id = ?";
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
			
		autor.setID(id);
		autor.setNome(rs.getString("nome"));
		autor.setNascimento(rs.getDate("nascimento"));
		autor.setFalecimento(rs.getDate("falecimento"));
		autor.setLocalMorte(rs.getString("local_morte"));
		autor.setBiografia(rs.getString("biografia"));

		return autor;
	}

	public List<Autor> listar() throws SQLException
	{
		String sql = "SELECT * FROM autores";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Autor> filtrarPorNome(String nome) throws SQLException
	{
		String sql = "SELECT * FROM autores WHERE nome LIKE '%" +nome+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Autor> filtrarPorLocalMorte(String localMorte) throws SQLException
	{
		String sql = "SELECT * FROM autores WHERE local_morte LIKE '%" +localMorte+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Autor> filtrarPorBiografia(String biografia) throws SQLException
	{
		String sql = "SELECT * FROM autores WHERE biografia LIKE '%" +biografia+ "%'";

		PreparedStatement ps = connection.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		return concluirFiltragem(rs);
	}

	public List<Autor> concluirFiltragem(ResultSet rs) throws SQLException
	{
		List<Autor> autores = new ArrayList<Autor>();

		while (rs.next())
		{
			Autor autor = new Autor();
			autor.setID(rs.getInt("id"));
			autor.setNome(rs.getString("nome"));
			autor.setNascimento(rs.getDate("nascimento"));
			autor.setFalecimento(rs.getDate("falecimento"));
			autor.setLocalMorte(rs.getString("local_morte"));
			autor.setBiografia(rs.getString("biografia"));
			autores.add(autor);
		}

		return autores;
	}
	
	public int getId(String nome) throws SQLException {
		PreparedStatement ps = connection.prepareStatement("SELECT id FROM autores WHERE nome LIKE '%?%'");
		ps.setString(1, nome);
		ResultSet rs = ps.executeQuery();
		return rs.getInt("id");
	}
}
