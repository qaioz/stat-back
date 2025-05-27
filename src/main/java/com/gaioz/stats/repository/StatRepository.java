package com.gaioz.stats.repository;

import com.gaioz.stats.config.ProfileManager;
import com.gaioz.stats.model.Stat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StatRepository extends JpaRepository<Stat, Long> {

    @Query("SELECT s FROM Stat s")
    Optional<Stat> findAnyNoDelay();

    @Query(value = "SELECT * FROM stats WHERE pg_sleep(10) IS NOT NULL LIMIT 1", nativeQuery = true)
    Optional<Stat> findAnyDelayed();

    default Optional<Stat> findAny() {
        return ProfileManager.isDevProfileActive()
                ? findAnyDelayed()
                : findAnyNoDelay();
    }
}
