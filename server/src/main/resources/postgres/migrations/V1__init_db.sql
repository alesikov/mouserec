CREATE SCHEMA IF NOT EXISTS mouserec;

SET search_path TO mouserec,public;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA pg_catalog;

CREATE TABLE IF NOT EXISTS "session"
(
    id         UUID                 DEFAULT uuid_generate_v4(),
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    stopped_at TIMESTAMPTZ          DEFAULT NULL,

    PRIMARY KEY (id)
);
