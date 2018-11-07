package com.vitornp.bankslip;

import com.google.common.collect.ImmutableMap;
import com.vitornp.bankslip.dto.BankSlipStatus;
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
import java.time.Instant;
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
        .paymentDate(rs.getObject("payment_date", LocalDate.class))
        .totalInCents(rs.getBigDecimal("total_in_cents"))
        .customer(rs.getString("costumer"))
        .status(BankSlipStatus.valueOf(rs.getString("status")))
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
            .put("status", bankSlip.getStatus().toString())
            .put("created_at", Timestamp.from(bankSlip.getCreatedAt()))
            .put("updated_at", Timestamp.from(bankSlip.getUpdatedAt()))
            .build();

        simpleJdbcInsert.execute(params);

        return bankSlip;
    }

    public int updatePayment(UUID id, LocalDate paymentDate) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder()
            .put("id", id)
            .put("payment_date", paymentDate)
            .put("status", BankSlipStatus.PAID.toString())
            .put("updated_at", Timestamp.from(Instant.now()))
            .build();

        return namedJdbcTemplate.update(
            "UPDATE bank_slip SET payment_date = :payment_date, status = :status, updated_at = :updated_at WHERE  id = :id",
            params
        );
    }

    public List<BankSlip> findAll() {
        return namedJdbcTemplate.query("SELECT * FROM bank_slip ORDER BY created_at DESC", ROW_MAPPER);
    }

    public Optional<BankSlip> findById(UUID id) {
        try {
            return ofNullable(
                namedJdbcTemplate
                    .queryForObject("SELECT * FROM bank_slip WHERE id = :id", of("id", id), ROW_MAPPER)
            );
        } catch (EmptyResultDataAccessException e) {
            log.error("Error when find a bank slip by id", e);
            return empty();
        }
    }

}
