package br.com.sicred.assemblyvote.domain.repository;

import br.com.sicred.assemblyvote.domain.model.VoteOption;

public interface VoteCountProjection {
    VoteOption getVote();
    long getTotal();
}