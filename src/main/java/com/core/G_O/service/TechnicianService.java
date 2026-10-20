package com.core.G_O.service;

import com.core.G_O.models.Technician;
import com.core.G_O.repositories.TechnicianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnicianService.class);

    TechnicianRepository technicianRepository;

    public TechnicianService (TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    public List<Technician> getTechnicians(Long businessId) {
        LOG.info("@getTechnicians SERV > Start service to get all technicians with de businessID {}", businessId);

        List<Technician> technicians = technicianRepository.findByWorkAt_Id(businessId).orElseThrow(
                () -> new RuntimeException("No hay tecnicos asociados al negocio con id" + businessId));

        LOG.info("@getTechnicians SERV > The service to retrieve all technicians has concluded; the data obtained were {}",
                technicians);

        return technicians;
    }
}
