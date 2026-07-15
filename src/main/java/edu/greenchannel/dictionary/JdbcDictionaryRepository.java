package edu.greenchannel.dictionary;

import edu.greenchannel.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDictionaryRepository implements DictionaryRepository {
    private static final RowMapper<DictionaryItem> ROW_MAPPER = (resultSet, rowNum) -> new DictionaryItem(
            resultSet.getLong("id"), resultSet.getString("dict_type_code"),
            resultSet.getString("dict_type_name"), resultSet.getString("item_code"),
            resultSet.getString("item_name"), resultSet.getString("item_value"),
            resultSet.getInt("sort"), resultSet.getInt("status") == 1, resultSet.getString("remark"));

    private final JdbcTemplate jdbcTemplate;

    public JdbcDictionaryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DictionaryItem> findEnabledByType(String dictTypeCode) {
        return jdbcTemplate.query("""
                SELECT id, dict_type_code, dict_type_name, item_code, item_name, item_value,
                       sort, status, remark
                FROM gc_dictionary
                WHERE dict_type_code = ? AND status = 1 AND is_deleted = 0
                ORDER BY sort, id
                """, ROW_MAPPER, dictTypeCode);
    }

    @Override
    public PageResult<DictionaryItem> search(String dictTypeCode, int page, int size) {
        String condition = StringUtils.hasText(dictTypeCode) ? " AND dict_type_code = ?" : "";
        List<Object> parameters = new ArrayList<>();
        if (StringUtils.hasText(dictTypeCode)) {
            parameters.add(dictTypeCode);
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_dictionary WHERE is_deleted = 0" + condition,
                Long.class, parameters.toArray());
        parameters.add(size);
        parameters.add((page - 1) * size);
        List<DictionaryItem> items = jdbcTemplate.query("""
                        SELECT id, dict_type_code, dict_type_name, item_code, item_name, item_value,
                               sort, status, remark
                        FROM gc_dictionary WHERE is_deleted = 0
                        """ + condition + " ORDER BY dict_type_code, sort, id LIMIT ? OFFSET ?",
                ROW_MAPPER, parameters.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    @Override
    public Optional<DictionaryItem> findById(long id) {
        return jdbcTemplate.query("""
                SELECT id, dict_type_code, dict_type_name, item_code, item_name, item_value,
                       sort, status, remark
                FROM gc_dictionary WHERE id = ? AND is_deleted = 0
                """, ROW_MAPPER, id).stream().findFirst();
    }

    @Override
    public DictionaryItem insert(DictionaryItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO gc_dictionary
                      (dict_type_code, dict_type_name, item_code, item_name, item_value, sort, status, remark)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, item.dictTypeCode());
            statement.setString(2, item.dictTypeName());
            statement.setString(3, item.itemCode());
            statement.setString(4, item.itemName());
            statement.setString(5, item.itemValue());
            statement.setInt(6, item.sort());
            statement.setInt(7, item.enabled() ? 1 : 0);
            statement.setString(8, item.remark());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("字典项主键生成失败");
        }
        return new DictionaryItem(key.longValue(), item.dictTypeCode(), item.dictTypeName(), item.itemCode(),
                item.itemName(), item.itemValue(), item.sort(), item.enabled(), item.remark());
    }

    @Override
    public void update(DictionaryItem item) {
        jdbcTemplate.update("""
                UPDATE gc_dictionary
                SET dict_type_code = ?, dict_type_name = ?, item_code = ?, item_name = ?, item_value = ?,
                    sort = ?, status = ?, remark = ?, update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND is_deleted = 0
                """, item.dictTypeCode(), item.dictTypeName(), item.itemCode(), item.itemName(), item.itemValue(),
                item.sort(), item.enabled() ? 1 : 0, item.remark(), item.id());
    }

    @Override
    public void softDelete(long id) {
        jdbcTemplate.update("""
                UPDATE gc_dictionary SET is_deleted = 1, update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND is_deleted = 0
                """, id);
    }
}
