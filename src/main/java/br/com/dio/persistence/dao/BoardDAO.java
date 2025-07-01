package br.com.dio.persistence.dao;

import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

@AllArgsConstructor
public class BoardDAO {

    private static final Logger log = LoggerFactory.getLogger(BoardDAO.class);

    private Connection connection;

    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        final String sql = "INSERT INTO BOARDS (name) VALUES (?);";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                } else {
                    throw new SQLException("Falha ao obter ID gerado para o Board.");
                }
            }
            log.info("Board inserido com ID: {}", entity.getId());
        } catch (SQLException e) {
            log.error("Erro ao inserir Board: {}", entity.getName(), e);
            throw e;
        }
        return entity;
    }

    public void delete(final Long id) throws SQLException {
        final String sql = "DELETE FROM BOARDS WHERE id = ?;";
        if (id == null) throw new IllegalArgumentException("O ID não pode ser nulo.");

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                log.warn("Nenhum Board encontrado para deletar com ID: {}", id);
            } else {
                log.info("Board deletado com ID: {}", id);
            }
        }
    }

    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        final String sql = "SELECT id, name FROM BOARDS WHERE id = ?;";
        if (id == null) throw new IllegalArgumentException("O ID não pode ser nulo.");

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    var entity = new BoardEntity();
                    entity.setId(resultSet.getLong("id"));
                    entity.setName(resultSet.getString("name"));
                    return Optional.of(entity);
                }
            }
        }
        log.info("Board com ID {} não encontrado.", id);
        return Optional.empty();
    }

    public boolean exists(final Long id) throws SQLException {
        final String sql = "SELECT 1 FROM BOARDS WHERE id = ?;";
        if (id == null) throw new IllegalArgumentException("O ID não pode ser nulo.");

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean found = resultSet.next();
                log.debug("Existência do Board ID {}: {}", id, found);
                return found;
            }
        }
    }
}
