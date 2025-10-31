package com.davidechiarelli.sportyevents.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "EVENT_STATUS")
@AllArgsConstructor
@NoArgsConstructor
public class EventStatusEntity {

    @Id
    private String eventId;

    private Boolean status;
}
