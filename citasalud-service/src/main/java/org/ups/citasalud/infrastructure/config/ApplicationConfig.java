package org.ups.citasalud.infrastructure.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.ups.citasalud.application.usecase.ConsultarDisponibilidadService;
import org.ups.citasalud.application.usecase.ReservarCitaService;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase;
import org.ups.citasalud.domain.port.out.CitaPort;
import org.ups.citasalud.domain.port.out.FranjaHorariaPort;
import org.ups.citasalud.domain.port.out.MedicoPort;
import org.ups.citasalud.domain.port.out.PacientePort;

@Configuration
@EnableAsync
public class ApplicationConfig {

    @Bean
    public ReservarCitaUseCase reservarCitaUseCase(CitaPort citaPort,
                                                     FranjaHorariaPort franjaHorariaPort,
                                                     PacientePort pacientePort,
                                                     MedicoPort medicoPort,
                                                     ApplicationEventPublisher eventPublisher) {
        return new ReservarCitaService(citaPort, franjaHorariaPort, pacientePort, medicoPort, eventPublisher);
    }

    @Bean
    public ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase(MedicoPort medicoPort,
                                                                          FranjaHorariaPort franjaHorariaPort) {
        return new ConsultarDisponibilidadService(medicoPort, franjaHorariaPort);
    }
}
