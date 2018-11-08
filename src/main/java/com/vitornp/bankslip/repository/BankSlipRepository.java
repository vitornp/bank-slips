package com.vitornp.bankslip.repository;

import com.google.common.collect.ImmutableMap;
import com.vitornp.bankslip.model.BankSlip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Repository
@Slf4j
public class BankSlipRepository {

    private static final RowMapper<BankSlip> ROW_MAPPER = (rs, rowNum) -> BankSlip.builder()
        .id(rs.getObject("id", UUID.class))
        .dueDate(rs.getObject("due_date", LocalDate.class))
        .totalInCents(rs.getBigDecimal("total_in_cents"))
        .customer(rs.getString("costumer"))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .build();

    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public BankSlipRepository(JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("bank_slip");
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public BankSlip save(BankSlip bankSlip) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder()
            .put("id", bankSlip.getId())
            .put("due_date", bankSlip.getDueDate())
            .put("total_in_cents", bankSlip.getTotalInCents())
            .put("costumer", bankSlip.getCustomer())
            .put("created_at", Timestamp.from(bankSlip.getCreatedAt()))
            .build();

        simpleJdbcInsert.execute(params);

        return bankSlip;
    }

    public List<BankSlip> findAll() {
        return namedJdbcTemplate.query("SELECT * FROM bank_slip ORDER BY created_at DESC", ROW_MAPPER);
    }

    public Optional<BankSlip> findById(UUID id) {
        try {
            return ofNullable(namedJdbcTemplate.queryForObject(
                "SELECT * FROM bank_slip WHERE id = :id",
                of("id", id),
                ROW_MAPPER
            ));
        } catch (EmptyResultDataAccessException e) {
            log.error("Error when find a bank slip by id", e);
            return empty();
        }
    }

}
