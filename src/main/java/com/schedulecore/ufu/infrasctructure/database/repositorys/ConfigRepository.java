package com.schedulecore.ufu.infrasctructure.database.repositorys;

import com.schedulecore.ufu.infrasctructure.database.entitys.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<ConfigEntity, String> {
}
