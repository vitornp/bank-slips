package com.vitornp.bankslip.repository;

import com.google.common.collect.ImmutableMap;
import com.vitornp.bankslip.dto.BankSlipStatusValue;
import com.vitornp.bankslip.model.BankSlipStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.ImmutableMap.of;

@Repository
@Slf4j
public class BankSlipStatusRepository {

    private static final RowMapper<BankSlipStatus> ROW_MAPPER = (rs, rowNum) -> BankSlipStatus.builder()
        .id(rs.getObject("id", UUID.class))
        .bankSlipId(rs.getObject("bank_slip_id", UUID.class))
        .date(rs.getObject("date", LocalDate.class))
        .status(BankSlipStatusValue.valueOf(rs.getString("status")))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .build();

    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public BankSlipStatusRepository(JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("bank_slip_status");
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public BankSlipStatus save(BankSlipStatus bankSlip) {
        Map<String, Object> params = ImmutableMap.<String, Object>builder()
            .put("id", bankSlip.getId())
            .put("bank_slip_id", bankSlip.getBankSlipId())
            .put("date", bankSlip.getDate())
            .put("status", bankSlip.getStatus().toString())
            .put("created_at", Timestamp.from(bankSlip.getCreatedAt()))
            .build();

        simpleJdbcInsert.execute(params);

        return bankSlip;
    }

    public List<BankSlipStatus> findAllByBankSlipId(UUID bankSlipId) {
        return namedJdbcTemplate.query(
            "SELECT * FROM bank_slip_status WHERE bank_slip_id = :bank_slip_id",
            of("bank_slip_id", bankSlipId),
            ROW_MAPPER
        );
    }

}
