package com.recargafacil.repository;

import com.recargafacil.model.Wallbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WallboxRepository extends JpaRepository<Wallbox, Long> {
    
    @Query(value = "SELECT w.* FROM Wallbox w " +
           "JOIN Local l ON w.id_local = l.id " +
           "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(l.latitude)) * cos(radians(l.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(l.latitude)))) <= :radius", 
           nativeQuery = true)
    List<Wallbox> findNearby(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius);
}