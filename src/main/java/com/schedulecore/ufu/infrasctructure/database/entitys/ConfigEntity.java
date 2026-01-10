package com.schedulecore.ufu.infrasctructure.database.entitys;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "configuracoes")
public class ConfigEntity {
    @Id
    private String nome;
    private String value;
}
