package com.davidechiarelli.sportyevents.controller;

import com.davidechiarelli.sportyevents.dto.EventStatus;
import com.davidechiarelli.sportyevents.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_CLASS)
class EventControllerTest {

    @MockitoBean
    private EventService eventService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ENDPOINT = "/events/status";

    @Test
    void shouldSaveEventStatus() throws Exception {

        EventStatus eventStatus = new EventStatus("1", true);

        doNothing().when(eventService).saveEventStatus(any());

        mvc.perform(
                        post(ENDPOINT)
                                .content(objectMapper.writeValueAsBytes(eventStatus))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotSaveEventStatus() throws Exception {

        EventStatus eventStatus = new EventStatus("2", true);

        doThrow(new RuntimeException("Database is down !"))
                .when(eventService).saveEventStatus(any());

        mvc.perform(
                        post(ENDPOINT)
                                .content(objectMapper.writeValueAsBytes(eventStatus))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.message", is("Database is down !")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldValidateInputField(String eventId) throws Exception {

        EventStatus eventStatus = new EventStatus(eventId, true);

        mvc.perform(
                        post(ENDPOINT)
                                .content(objectMapper.writeValueAsBytes(eventStatus))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", containsString("Validation failed for argument [0]")));
    }
}
