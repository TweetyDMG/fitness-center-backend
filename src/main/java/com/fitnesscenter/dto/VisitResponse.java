package com.fitnesscenter.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VisitResponse {
        private Long id;
        private LocalDate visitDate;
        private LocalTime visitTime;
        private String trainerName;
        private String serviceName;
        private String status;

        public VisitResponse() {
        }

        public VisitResponse(Long id, LocalDate visitDate, LocalTime visitTime, String trainerName, String serviceName, String status) {
                this.id = id;
                this.visitDate = visitDate;
                this.visitTime = visitTime;
                this.trainerName = trainerName;
                this.serviceName = serviceName;
                this.status = status;
        }
}