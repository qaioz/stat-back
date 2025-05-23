package com.gaioz.stats.repository;

import com.gaioz.stats.model.Stat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatRepository extends JpaRepository<Stat, Long> {
}
