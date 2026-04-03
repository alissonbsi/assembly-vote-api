ALTER TABLE voting_session
    ADD CONSTRAINT uk_voting_session_agenda
        UNIQUE (agenda_id);