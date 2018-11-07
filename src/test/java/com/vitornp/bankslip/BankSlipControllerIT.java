package com.vitornp.bankslip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
public class BankSlipControllerIT {

    @Autowired
    private MockMvc mvc;

    @Test
    public void createSuccessfully() throws Exception {
        // Given
        String request = "{\n" +
            "  \"due_date\": \"2020-01-01\",\n" +
            "  \"customer\": \"Test\",\n" +
            "  \"total_in_cents\": 0.1\n" +
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
            .andExpect(jsonPath("id", notNullValue()))
            .andExpect(jsonPath("due_date", equalTo("2020-01-01")))
            .andExpect(jsonPath("total_in_cents", equalTo(0.1)))
            .andExpect(jsonPath("customer", equalTo("Test")))
            .andExpect(jsonPath("status", equalTo("PENDING")));
    }

    @Test
    public void createErrorWhenAnyFieldIsNull() throws Exception {
        // Given
        String request = "{\n" +
            "  \"customer\": \"Test\",\n" +
            "  \"total_in_cents\": 0.1\n" +
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
            .andExpect(jsonPath("error", equalTo(422)))
            .andExpect(jsonPath("status", equalTo("Unprocessable Entity")))
            .andExpect(jsonPath("$.errors.due_date", startsWith("must not be null")));
    }
}
