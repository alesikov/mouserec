INSERT INTO mouserec.session (id, started_at, stopped_at)
VALUES (uuid_generate_v4(), NOW() - INTERVAL '1 MINUTE', NOW()),
       (uuid_generate_v4(), NOW(), NULL);
