package br.com.sicred.assemblyvote.cache.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

@Data
@Builder
@RedisHash("SESSION")
@AllArgsConstructor
public class SessionRedis {

    @Id
    @Indexed
    private UUID agendaId;

    @TimeToLive
    private Long ttl;
}
