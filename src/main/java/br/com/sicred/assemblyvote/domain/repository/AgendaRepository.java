package br.com.sicred.assemblyvote.domain.repository;

import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgendaRepository extends JpaRepository<AgendaEntity, UUID> {
}