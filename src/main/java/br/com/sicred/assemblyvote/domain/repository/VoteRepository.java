package br.com.sicred.assemblyvote.domain.repository;

import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity, UUID> {
    boolean existsByAgendaAndMemberCpf(AgendaEntity agenda, String memberCpf);
}
