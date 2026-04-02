package br.com.sicred.assemblyvote.cache.repository;

import br.com.sicred.assemblyvote.cache.model.SessionRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SessionRedisRepository extends CrudRepository<SessionRedis, UUID> {
}
