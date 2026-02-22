# SECURITY

## Objectif
Fournir un scan de sécurité simple, reproductible, et documenté.

## Outils utilisés
- `npm audit` pour les dépendances frontend
- `trivy fs` pour scanner le repository

## Exécution
Depuis la racine du repo:

```bash
./scripts/run-security.sh
```

## Artefacts générés
- `reports/security/scan-date.txt`
- `reports/security/npm-audit.json`
- `reports/security/npm-audit-summary.txt`
- `reports/security/trivy-fs.json`
- `reports/security/trivy-summary.txt`
- `reports/security/summary.txt`

## Lecture des résultats
Fichiers courts à lire en priorité:
- `npm-audit-summary.txt`
- `trivy-summary.txt`

Puis détail complet si besoin:
- `npm-audit.json`
- `trivy-fs.json`

Priorisation:
1. `critical`
2. `high`
3. `medium`

## Actions correctives recommandées
1. Corriger d'abord `critical`, puis `high`.
2. Mettre à jour les dépendances concernées.
3. Relancer `./scripts/run-security.sh`.
4. Documenter les exceptions (faux positifs / risque accepté).
