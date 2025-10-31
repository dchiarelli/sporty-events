package com.davidechiarelli.sportyevents.repository;

import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStatusRepository extends JpaRepository<EventStatusEntity, String> {

    List<EventStatusEntity> findAllByStatusIsTrue();
}
