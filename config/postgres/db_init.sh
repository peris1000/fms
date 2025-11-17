#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE DATABASE "zimono_trg_a";
	GRANT ALL PRIVILEGES ON DATABASE "zimono_trg_a" TO "$POSTGRES_USER";
	CREATE DATABASE "zimono_trg_c";
  GRANT ALL PRIVILEGES ON DATABASE "zimono_trg_c" TO "$POSTGRES_USER";
  CREATE DATABASE "zimono_trg_keycloak";
  GRANT ALL PRIVILEGES ON DATABASE "zimono_trg_keycloak" TO "$POSTGRES_USER";
EOSQL
