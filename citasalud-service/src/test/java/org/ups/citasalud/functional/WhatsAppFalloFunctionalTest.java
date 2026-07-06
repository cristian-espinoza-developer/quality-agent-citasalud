package org.ups.citasalud.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.ups.citasalud.adapter.out.notification.WhatsAppNotificationAdapter;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WhatsAppFalloFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WhatsAppNotificationAdapter notificationAdapter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PACIENTE_ID_1 = "00000000-0000-0000-0000-000000000001";
    private static final String FRANJA_EC002 = "770e8400-e29b-41d4-a716-446655440006";

    @Nested
    class DadoWhatsAppNoDisponible {

        @Test
        void cuandoNotificacionFalla_entoncesReservaQuedaRegistradaYAccesible() throws Exception {
            // EC-002: WhatsApp no disponible — la notificación falla, pero la reserva persiste.
            // onCitaConfirmada es @Async @TransactionalEventListener(AFTER_COMMIT): corre en
            // hilo separado DESPUÉS del commit de la transacción. Aunque falle, la cita ya
            // está en la BD y el HTTP response ya fue enviado.
            doThrow(new RuntimeException("WhatsApp service unavailable"))
                    .when(notificationAdapter).onCitaConfirmada(any());

            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_EC002));

            MvcResult result = mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                    .andReturn();

            String citaId = new ObjectMapper()
                    .readTree(result.getResponse().getContentAsString())
                    .get("citaId").asText();

            // La cita persiste independientemente del estado de la notificación WhatsApp
            mockMvc.perform(get("/api/v1/citas/{citaId}", citaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
        }
    }
}
