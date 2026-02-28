# Schémas corrigés (architecture + modèle de données)

Ce document fournit une version conforme au code actuel du projet.

## Architecture applicative

```mermaid
flowchart LR
    U["Utilisateur (navigateur)"] --> FE["Frontend Angular"]
    FE -->|"REST JSON + JWT"| BE["Backend Spring Boot"]
    BE --> DB["PostgreSQL"]
    BE --> FS["Stockage local uploads/"]
    BE --> SEC["Spring Security + JWT"]
```

## MCD / ERD conforme au code actuel

```mermaid
erDiagram
    USERS {
      bigint id PK
      string email UK
      string password_hash
      datetime created_at
    }

    FILE_METADATA {
      string token PK
      string filename
      bigint size
      datetime expires_at
      bigint owner_id FK
      string password
      datetime created_at
    }

    FILE_TAGS {
      string file_token FK
      string tag
    }

    USERS ||--o{ FILE_METADATA : "possede"
    FILE_METADATA ||--o{ FILE_TAGS : "contient"
```

## Écarts corrigés par rapport au schéma initial
- `FILE_METADATA.token` est la clé primaire (pas `id`).
- `TAG` n'est pas une table entité autonome; les tags sont en table de collection `file_tags`.
- pas de champ `is_anonymous` dans l'implémentation actuelle.
- pas de `storage_path` persistant en base.
