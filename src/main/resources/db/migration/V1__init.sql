CREATE TABLE agenda (
                        id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
                        title VARCHAR2(50) NOT NULL,
                        description VARCHAR2(50) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE voting_session (
                                id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
                                agenda_id RAW(16) NOT NULL,
                                start_time TIMESTAMP,
                                end_time TIMESTAMP,
                                status VARCHAR2(20) CHECK (status IN ('OPEN', 'CLOSED')),
                                CONSTRAINT fk_session_agenda FOREIGN KEY (agenda_id)
                                    REFERENCES agenda(id)
);

CREATE TABLE vote (
                      id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
                      agenda_id RAW(16) NOT NULL,
                      member_cpf VARCHAR2(11) NOT NULL,
                      vote VARCHAR2(3) CHECK (vote IN ('YES', 'NO')),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_vote_agenda FOREIGN KEY (agenda_id)
                          REFERENCES agenda(id),
                      CONSTRAINT uk_vote UNIQUE (agenda_id, member_cpf)
);