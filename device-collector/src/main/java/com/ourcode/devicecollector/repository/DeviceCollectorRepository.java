package com.ourcode.devicecollector.repository;

import com.ourcode.devicecollector.model.entity.DeviceCollectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCollectorRepository extends JpaRepository<DeviceCollectorEntity, Long> {


    @Modifying
    @Query(value = """
            INSERT INTO device_collector (device_id, device_type, created_at, meta)
            VALUES (:deviceId, :deviceType, :createdAt, :meta)
            ON CONFLICT (device_id)
            DO UPDATE SET
                device_type = EXCLUDED.device_type,
                created_at = EXCLUDED.created_at,
                meta = EXCLUDED.meta
            """, nativeQuery = true)
    void upsertDevice(@Param("deviceId") String deviceId,
                      @Param("deviceType") String deviceType,
                      @Param("createdAt") Long createdAt,
                      @Param("meta") String meta);

    DeviceCollectorEntity findByDeviceId(@Param("deviceId") String deviceId);
}
