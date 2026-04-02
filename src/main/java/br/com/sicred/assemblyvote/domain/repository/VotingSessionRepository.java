package br.com.sicred.assemblyvote.domain.repository;

import br.com.sicred.assemblyvote.domain.model.VotingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface VotingSessionRepository extends JpaRepository<VotingSessionEntity, UUID> {

    @Modifying
    @Query("""
    UPDATE VotingSessionEntity v
    SET v.status = 'CLOSED'
    WHERE v.status = 'OPEN'
    AND v.endTime <= :now""")
    int closeExpiredSessions(@Param("now") LocalDateTime now);
}
