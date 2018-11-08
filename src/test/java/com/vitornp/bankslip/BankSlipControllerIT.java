package com.vitornp.bankslip;

import com.vitornp.bankslip.model.BankSlip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class BankSlipControllerIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BankSlipService bankSlipService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM bank_slip");
    }

    @Test
    void createSuccessfully() throws Exception {
        // Given
        String request = "{" +
            "  \"due_date\": \"2020-01-01\"," +
            "  \"customer\": \"Test\"," +
            "  \"total_in_cents\": 0.1" +
            "}";

        // When
        ResultActions resultActions = this.mvc.perform(
            post("/bankslips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        );

        // Then
        resultActions
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").value(notNullValue()))
            .andExpect(jsonPath("due_date").value("2020-01-01"))
            .andExpect(jsonPath("payment_date").doesNotExist())
            .andExpect(jsonPath("total_in_cents").value(0.1))
            .andExpect(jsonPath("customer").value("Test"))
            .andExpect(jsonPath("status").value("PENDING"))
            .andExpect(jsonPath("fine").doesNotExist());
    }

    @Test
    void createErrorWhenAnyFieldIsNull() throws Exception {
        // Given
        String request = "{" +
            "  \"customer\": \"Test\"," +
            "  \"total_in_cents\": 0.1" +
            "}";

        // When
        ResultActions resultActions = this.mvc.perform(
            post("/bankslips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        );

        // Then
        resultActions
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("error").value(422))
            .andExpect(jsonPath("status").value("Unprocessable Entity"))
            .andExpect(jsonPath("$.errors.due_date").value(startsWith("must not be null")));
    }

    @Test
    void findAll() throws Exception {
        // Given
        LocalDate dueDate = LocalDate.now();
        BankSlip bankSlipPayment = givenBankSlip(dueDate.plusDays(3), "Test 3", "3000");
        BankSlip bankSlipCanceled = givenBankSlip(dueDate.plusDays(4), "Test 4", "4000");
        bankSlipService.save(bankSlipPayment);
        bankSlipService.save(bankSlipCanceled);
        bankSlipService.save(givenBankSlip(dueDate.plusDays(2), "Test 1", "1000"));
        bankSlipService.save(givenBankSlip(dueDate.plusDays(1), "Test 2", "2000"));

        bankSlipService.paymentById(bankSlipPayment.getId(), LocalDate.now().plusDays(4));
        bankSlipService.cancelById(bankSlipCanceled.getId());

        // When
        ResultActions resultActions = this.mvc.perform(
            get("/bankslips")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize(4)))

            .andExpect(jsonPath("$[0].id").value(notNullValue()))
            .andExpect(jsonPath("$[0].due_date").value(dueDate.plusDays(1).toString()))
            .andExpect(jsonPath("$[0].payment_date").doesNotExist())
            .andExpect(jsonPath("$[0].total_in_cents").value(2000.0))
            .andExpect(jsonPath("$[0].customer").value("Test 2"))
            .andExpect(jsonPath("$[0].status").value("PENDING"))
            .andExpect(jsonPath("$[0].fine").doesNotExist())

            .andExpect(jsonPath("$[1].id").value(notNullValue()))
            .andExpect(jsonPath("$[1].due_date").value(dueDate.plusDays(2).toString()))
            .andExpect(jsonPath("$[1].payment_date").doesNotExist())
            .andExpect(jsonPath("$[1].total_in_cents").value(1000.0))
            .andExpect(jsonPath("$[1].customer").value("Test 1"))
            .andExpect(jsonPath("$[1].status").value("PENDING"))
            .andExpect(jsonPath("$[1].fine").doesNotExist())

            .andExpect(jsonPath("$[2].id").value(notNullValue()))
            .andExpect(jsonPath("$[2].due_date").value(dueDate.plusDays(3).toString()))
            .andExpect(jsonPath("$[2].payment_date").value(LocalDate.now().plusDays(4).toString()))
            .andExpect(jsonPath("$[2].total_in_cents").value(3000.0))
            .andExpect(jsonPath("$[2].customer").value("Test 3"))
            .andExpect(jsonPath("$[2].status").value("PAID"))
            .andExpect(jsonPath("$[2].fine").doesNotExist())

            .andExpect(jsonPath("$[3].id").value(notNullValue()))
            .andExpect(jsonPath("$[3].due_date").value(dueDate.plusDays(4).toString()))
            .andExpect(jsonPath("$[3].payment_date").doesNotExist())
            .andExpect(jsonPath("$[3].total_in_cents").value(4000.0))
            .andExpect(jsonPath("$[3].customer").value("Test 4"))
            .andExpect(jsonPath("$[3].status").value("CANCELED"))
            .andExpect(jsonPath("$[3].fine").doesNotExist());
    }

    @Test
    void findAllWhenEmpty() throws Exception {
        // Given

        // When
        ResultActions resultActions = this.mvc.perform(
            get("/bankslips")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.*", hasSize(0)));
    }

    @Test
    void findById() throws Exception {
        // Given
        LocalDate dueDate = LocalDate.now();
        LocalDate paymentDate = LocalDate.now().plusDays(1);
        BankSlip bankSlip = givenBankSlip(dueDate, "Test 1", "2000");
        bankSlipService.save(bankSlip);
        bankSlipService.paymentById(bankSlip.getId(), paymentDate);

        // When
        ResultActions resultActions = this.mvc.perform(
            get(String.format("/bankslips/%s", bankSlip.getId()))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        resultActions
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(notNullValue()))
            .andExpect(jsonPath("due_date").value(dueDate.toString()))
            .andExpect(jsonPath("payment_date").value(paymentDate.toString()))
            .andExpect(jsonPath("total_in_cents").value(2000.0))
            .andExpect(jsonPath("customer").value("Test 1"))
            .andExpect(jsonPath("status").value("PAID"))
            .andExpect(jsonPath("fine").value(10.0));
    }

    @Test
    void paymentById() throws Exception {
        // Given
        BankSlip bankSlip = bankSlipService.save(givenBankSlip(LocalDate.now().plusDays(2), "Test 1", "1000"));
        String request = "{" +
            "  \"payment_date\": \"2020-01-01\"" +
            "}";

        // When
        ResultActions resultActions = this.mvc.perform(
            post(String.format("/bankslips/%s/payments", bankSlip.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        );

        // Then
        resultActions
            .andExpect(status().isNoContent());
    }

    @Test
    void paymentByIdWhenNotFound() throws Exception {
        // Given
        String request = "{" +
            "  \"payment_date\": \"2020-01-01\"" +
            "}";

        // When
        ResultActions resultActions = this.mvc.perform(
            post(String.format("/bankslips/%s/payments", UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        );

        // Then
        resultActions
            .andExpect(status().isNotFound());
    }

    @Test
    void cancelById() throws Exception {
        // Given
        BankSlip bankSlip = bankSlipService.save(givenBankSlip(LocalDate.now().plusDays(2), "Test 1", "1000"));

        // When
        ResultActions resultActions = this.mvc.perform(
            delete(String.format("/bankslips/%s", bankSlip.getId()))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        resultActions
            .andExpect(status().isNoContent());
    }

    @Test
    void cancelByIdWhenNotFound() throws Exception {
        // Given

        // When
        ResultActions resultActions = this.mvc.perform(
            delete(String.format("/bankslips/%s", UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        resultActions
            .andExpect(status().isNotFound());
    }

    private BankSlip givenBankSlip(LocalDate dueDate, String customer, String totalInCents) {
        return BankSlip.builder()
            .dueDate(dueDate)
            .customer(customer)
            .totalInCents(new BigDecimal(totalInCents))
            .build();
    }
}
