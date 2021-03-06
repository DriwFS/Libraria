package com.livraria.fronteira.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.DefaultTableModel;

import com.livraria.entidades.Livro;

@SuppressWarnings("serial")
public class ModelPesquisaResultados extends DefaultTableModel
{
	private static final int COLUNA_TITULO = 0;
	private static final int COLUNA_PRECO = 1;
	private static final int COLUNA_PAGINAS = 2;
	private static final int COLUNA_EDITORA = 3;
	private static final int COLUNA_PRIMEIRO_AUTOR = 4;
	private static final int COLUNA_CATEGORIAS = 5;

	private static final String COLUNS[] = new String[]
	{
		"Nome", "Pre�o", "Pag.", "Editora", "Autor(es)", "Categorias"
	};

	private static final Class<?> COLUNS_TYPE[] = new Class[]
	{
		String.class, String.class, Integer.class, String.class, String.class, String.class
	};

	private List<Livro> livros = new ArrayList<Livro>();

	@Override
	public Class<?> getColumnClass(int column)
	{
		return COLUNS_TYPE[column];
	}

	@Override
	public int getColumnCount()
	{
		return COLUNS.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return COLUNS[column];
	}

	@Override
	public int getRowCount()
	{
		return livros == null ? 0 : livros.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		Livro livro = livros.get(row);

		if (livro != null)
			switch (column)
			{
				case COLUNA_TITULO: return livro.getTitulo();
				case COLUNA_PRECO: return String.format(Locale.US, "R$ %3.2f", (float) livro.getPreco());
				case COLUNA_PAGINAS: return livro.getPaginas();
				case COLUNA_EDITORA:
					if (livro.getEditora() == null)
						return "-";
					return livro.getEditora().getNome();

				case COLUNA_PRIMEIRO_AUTOR:
					if (livro.getLivroAutores().tamanho() == 0)
						return "-";
					String autores = livro.getLivroAutores().obter(0).getNome();
						for (int i = 1; i < livro.getLivroAutores().tamanho(); i++)
							autores += ", " +livro.getLivroAutores().obter(i).getNome();
					return autores;

				case COLUNA_CATEGORIAS:
					if (livro.getLivroCategorias().tamanho() == 0)
						return "-";
					String categorias = livro.getLivroCategorias().obter(0).getTema();
						for (int i = 1; i < livro.getLivroCategorias().tamanho(); i++)
							categorias += ", " +livro.getLivroCategorias().obter(i).getTema();
					return categorias;
			}

		return null;
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1)
	{
		return false;
	}

	public Livro getLinha(int linha)
	{
		if (linha >= 0 && linha < livros.size())
			return livros.get(linha);

		return null;
	}

	public void atualizarLista(List<Livro> lista)
	{
		livros = lista;

		fireTableDataChanged();
	}
}
