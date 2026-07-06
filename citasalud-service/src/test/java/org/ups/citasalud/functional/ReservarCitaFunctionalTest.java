package org.ups.citasalud.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReservarCitaFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PACIENTE_ID_1 = "00000000-0000-0000-0000-000000000001";
    private static final String PACIENTE_ID_2 = "00000000-0000-0000-0000-000000000002";
    private static final String FRANJA_DISPONIBLE = "770e8400-e29b-41d4-a716-446655440001";
    private static final String FRANJA_DISPONIBLE_2 = "770e8400-e29b-41d4-a716-446655440004";
    private static final String FRANJA_REINTENTO = "770e8400-e29b-41d4-a716-446655440005";
    private static final String FRANJA_OCUPADA = "770e8400-e29b-41d4-a716-446655440003";
    private static final String FRANJA_CONCURRENCIA = "770e8400-e29b-41d4-a716-446655440010";

    @Nested
    class DadoPacienteConHeaderXPatientId {

        @Test
        void cuandoPostApiV1Citas_entoncesRetorna201ConCitaConfirmada() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_DISPONIBLE));

            mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                    .andExpect(jsonPath("$.citaId").isNotEmpty())
                    .andExpect(jsonPath("$.medico.nombre").value("Ana"));
        }
    }

    @Nested
    class DadaFranjaYaOcupada {

        @Test
        void cuandoPostApiV1Citas_entoncesRetorna409ConCodigoFranjaOcupada() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_OCUPADA));

            mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("FRANJA_OCUPADA"));
        }
    }

    @Nested
    class DadoCitaConfirmada {

        @Test
        void cuandoGetCita_entoncesRetorna200ConCitaConfirmada() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_DISPONIBLE_2));
            MvcResult createResult = mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            String citaId = new ObjectMapper()
                    .readTree(createResult.getResponse().getContentAsString())
                    .get("citaId").asText();

            mockMvc.perform(get("/api/v1/citas/{citaId}", citaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.citaId").value(citaId))
                    .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
        }
    }

    @Nested
    class DadoCitaInexistente {

        @Test
        void cuandoGetCita_entoncesRetorna404() throws Exception {
            mockMvc.perform(get("/api/v1/citas/{citaId}", "00000000-0000-0000-0000-999999999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
        }
    }

    @Nested
    class DadoSinHeaderXPatientId {

        @Test
        void cuandoPostCitas_entoncesRetorna404() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_DISPONIBLE));
            mockMvc.perform(post("/api/v1/citas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DadoHeaderXPatientIdInvalido {

        @Test
        void cuandoPostCitas_entoncesRetorna404() throws Exception {
            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_DISPONIBLE));
            mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", "not-a-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DadoCuerpoSinFranjaHorariaId {

        @Test
        void cuandoPostCitas_entoncesRetorna400() throws Exception {
            mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
        }
    }

    @Nested
    class DadoPacienteQueReintentalaMismaReserva {

        @Test
        void cuandoReintentaConMismaFranja_entoncesSegundoPedidoRetorna409YCitaNoDuplicada() throws Exception {
            // EC-001: idempotencia — si el paciente reintenta tras perder conexión,
            // el segundo intento falla con FRANJA_OCUPADA; no se crea una segunda cita
            String body = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_REINTENTO));

            mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/citas")
                            .header("X-Patient-Id", PACIENTE_ID_1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("FRANJA_OCUPADA"));
        }
    }

    @Nested
    class DadoDosPacientesConfirmanMismaFranjaConcurrentemente {

        @Test
        void entoncesUnoRetorna201YOtroRetorna409() throws Exception {
            String body1 = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_CONCURRENCIA));
            String body2 = objectMapper.writeValueAsString(Map.of("franjaHorariaId", FRANJA_CONCURRENCIA));

            ExecutorService executor = Executors.newFixedThreadPool(2);
            List<Callable<Integer>> tasks = new ArrayList<>();

            tasks.add(() -> {
                MvcResult result = mockMvc.perform(post("/api/v1/citas")
                                .header("X-Patient-Id", PACIENTE_ID_1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body1))
                        .andReturn();
                return result.getResponse().getStatus();
            });

            tasks.add(() -> {
                MvcResult result = mockMvc.perform(post("/api/v1/citas")
                                .header("X-Patient-Id", PACIENTE_ID_2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body2))
                        .andReturn();
                return result.getResponse().getStatus();
            });

            List<Future<Integer>> futures = executor.invokeAll(tasks);
            executor.shutdown();

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : futures) {
                statuses.add(f.get());
            }

            long created = statuses.stream().filter(s -> s == 201).count();
            long conflict = statuses.stream().filter(s -> s == 409).count();

            assertEquals(1, created, "Exactamente un paciente debe obtener 201");
            assertEquals(1, conflict, "Exactamente un paciente debe obtener 409");
        }
    }
}
