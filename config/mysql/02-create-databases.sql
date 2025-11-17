CREATE DATABASE IF NOT EXISTS zimono_trg_a;
GRANT ALL PRIVILEGES ON zimono_trg_a.* TO 'user1'@'%';

CREATE DATABASE IF NOT EXISTS zimono_trg_c;
GRANT ALL PRIVILEGES ON zimono_trg_c.* TO 'user1'@'%';

CREATE DATABASE IF NOT EXISTS zimono_trg_keycloak;
GRANT ALL PRIVILEGES ON zimono_trg_keycloak.* TO 'user1'@'%';
