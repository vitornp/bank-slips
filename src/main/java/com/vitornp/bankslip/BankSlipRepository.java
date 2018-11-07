package com.vitornp.bankslip;

import com.google.common.collect.ImmutableMap;
import com.vitornp.bankslip.model.BankSlip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Map;

@Repository
public class BankSlipRepository {

    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public BankSlipRepository(JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("bank_slip");
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
}
