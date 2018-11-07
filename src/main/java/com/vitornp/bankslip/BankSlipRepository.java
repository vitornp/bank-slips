package com.vitornp.bankslip;

import com.google.common.collect.ImmutableMap;
import com.vitornp.bankslip.dto.BankSlipStatus;
import com.vitornp.bankslip.model.BankSlip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class BankSlipRepository {

    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public BankSlipRepository(JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("bank_slip");
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public BankSlip save(BankSlip bankSlip) {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
            .put("id", bankSlip.getId())
            .put("due_date", bankSlip.getDueDate())
            .put("total_in_cents", bankSlip.getTotalInCents())
            .put("costumer", bankSlip.getCustomer())
            .put("status", bankSlip.getStatus().toString())
            .put("created_at", Timestamp.from(bankSlip.getCreatedAt()))
            .build();

        simpleJdbcInsert.execute(map);

        return bankSlip;
    }

    public List<BankSlip> findAll() {
        return namedJdbcTemplate.query(
            "SELECT * FROM bank_slip ORDER BY created_at DESC",
            (rs, rowNum) -> BankSlip.builder()
                .id(rs.getObject("id", UUID.class))
                .dueDate(rs.getObject("due_date", LocalDate.class))
                .totalInCents(rs.getBigDecimal("total_in_cents"))
                .customer(rs.getString("costumer"))
                .status(BankSlipStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build()
        );
    }

}
