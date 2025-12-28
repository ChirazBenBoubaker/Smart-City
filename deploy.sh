#!/bin/bash

echo "===== Déploiement SmartCity ====="

VERSION=$1

if [ -z "$VERSION" ]; then
  echo "❌ Version non fournie"
  exit 1
fi

echo "▶ Version : $VERSION"

# Arrêt des conteneurs existants
docker-compose down

# Construction de la nouvelle image
docker build -t smartcity-app:$VERSION .

# Mise à jour du docker-compose
export APP_VERSION=$VERSION

# Lancement des services
docker-compose up -d

echo "✅ Déploiement terminé avec succès"
