package org.ups.citasalud.integration.adapter.persistence;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.ups.citasalud.adapter.out.persistence.adapter.MedicoPersistenceAdapter;
import org.ups.citasalud.domain.model.Medico;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import(MedicoPersistenceAdapter.class)
class MedicoPersistenceAdapterIT {

    @Autowired
    private MedicoPersistenceAdapter adapter;

    @Nested
    class DadoMedicoConDisponibleOnlineTrue {

        @Test
        void cuandoListar_entoncesAparececEnLista() {
            List<Medico> medicos = adapter.listarDisponiblesOnline(null);

            assertFalse(medicos.isEmpty(), "Debe haber al menos un médico disponible online");
            assertTrue(medicos.stream().allMatch(Medico::isDisponibleOnline),
                    "Todos los médicos deben tener disponibleOnline=true");
        }
    }
}
