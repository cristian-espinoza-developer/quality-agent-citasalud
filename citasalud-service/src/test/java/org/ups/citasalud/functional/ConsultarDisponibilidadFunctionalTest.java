package org.ups.citasalud.functional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConsultarDisponibilidadFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String MEDICO_ID = "550e8400-e29b-41d4-a716-446655440001";

    @Nested
    class DadoPacienteAuth {

        @Test
        void cuandoGetApiV1Medicos_entoncesRetorna200ConListaMedicos() throws Exception {
            mockMvc.perform(get("/api/v1/medicos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.medicos").isArray())
                    .andExpect(jsonPath("$.medicos.length()").value(2));
        }
    }

    @Nested
    class DadoMedicoConFranjas {

        @Test
        void cuandoGetDisponibilidad_entoncesRetorna200ConFranjasYCampoDisponible() throws Exception {
            mockMvc.perform(get("/api/v1/medicos/{medicoId}/disponibilidad", MEDICO_ID)
                            .param("fecha", "2026-07-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.medicoId").value(MEDICO_ID))
                    .andExpect(jsonPath("$.fecha").value("2026-07-15"))
                    .andExpect(jsonPath("$.franjas").isArray())
                    .andExpect(jsonPath("$.franjas.length()").value(3))
                    .andExpect(jsonPath("$.franjas[0].disponible").isBoolean());
        }
    }

    @Nested
    class DadoMedicoSinFranjasEnFechaFutura {

        @Test
        void cuandoGetDisponibilidad_entoncesRetorna200ListaVacia() throws Exception {
            mockMvc.perform(get("/api/v1/medicos/{medicoId}/disponibilidad", MEDICO_ID)
                            .param("fecha", "2099-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.franjas").isEmpty());
        }
    }
}
