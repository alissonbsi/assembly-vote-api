package br.com.sicred.assemblyvote.domain.repository;

import br.com.sicred.assemblyvote.domain.model.AgendaEntity;
import br.com.sicred.assemblyvote.domain.model.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<VoteEntity, UUID> {
    boolean existsByAgendaAndMemberCpf(AgendaEntity agenda, String memberCpf);

    @Query("""
    SELECT v.vote as vote, COUNT(v) as total
    FROM VoteEntity v
    WHERE v.agenda = :agenda
    GROUP BY v.vote
""")
    List<VoteCountProjection> countVotesByAgenda(@Param("agenda") AgendaEntity agenda);
}
