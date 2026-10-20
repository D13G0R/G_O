package com.core.G_O.controllers;

import com.core.G_O.models.Technician;
import com.core.G_O.service.JwtService;
import com.core.G_O.service.TechnicianService;
import io.jsonwebtoken.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/technicians")
public class TechnicianApi {

    private static final Logger LOG = LoggerFactory.getLogger(TechnicianApi.class);

    TechnicianService technicianService;

    JwtService jwtService;

    public TechnicianApi(TechnicianService technicianService, JwtService jwtService) {
        this.technicianService = technicianService;
        this.jwtService = jwtService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Technician>> getTechnicians(@RequestHeader("Authorization") String header) {

        LOG.info("@getTechnicians API > Start api to get all technician");

        String token = header.substring(7);

        Long businessId = jwtService.getBusinessId(token);

        List<Technician> technicians = technicianService.getTechnicians(businessId);

        return ResponseEntity.ok(technicians);
    }
}
