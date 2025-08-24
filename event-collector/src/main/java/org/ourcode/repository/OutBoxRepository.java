package org.ourcode.repository;

import org.ourcode.model.OutBoxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutBoxRepository extends JpaRepository<OutBoxEntity, String> {
    @Query("SELECT o FROM OutBoxEntity o WHERE o.isProcessed = false")
    List<OutBoxEntity> findAllByProcessedFalse();
    @Query("SELECT o FROM OutBoxEntity o WHERE o.isProcessed = true")
    List<OutBoxEntity> findAllByProcessedTrue();
    List<OutBoxEntity> findAllByDeviceId(String deviceId);
}
