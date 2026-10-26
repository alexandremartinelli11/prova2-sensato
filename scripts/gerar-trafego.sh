#!/usr/bin/env bash
# Gera trafego de login para alimentar os indicadores do dashboard no Grafana.
# Uso: ./scripts/gerar-trafego.sh [url-base]   (padrao: http://localhost:8080)
set -u
URL="${1:-http://localhost:8080}"

login() {
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$1\",\"senha\":\"$2\"}" "$URL/login")
  echo "POST /login $1/$2 -> $code"
}

echo "== Cadastrando usuarios (manutencao) =="
for u in ana bruno carla diego; do
  curl -s -o /dev/null -w "POST / $u -> %{http_code}\n" -u manutencao:manutencao \
    -H 'Content-Type: application/json' -d "{\"username\":\"$u\",\"senha\":\"${u}123\"}" "$URL/"
done

echo "== Logins validos =="
for i in $(seq 1 3); do
  for u in joao maria ana bruno carla diego; do
    case $u in joao) s=123;; maria) s=456;; *) s="${u}123";; esac
    login "$u" "$s"
    sleep 0.3
  done
  sleep 2
done

echo "== Logins invalidos (bloqueia bruno, carla e diego na 3a falha) =="
for u in bruno carla diego; do
  for i in 1 2 3; do login "$u" errada; sleep 0.5; done
  sleep 2
done
login inexistente qualquer

echo "== Tentativa de usuario bloqueado =="
login bruno bruno123

echo "== Desbloqueio de diego (admin) =="
curl -s -o /dev/null -w "PUT /unlock/diego -> %{http_code}\n" -u admin:admin -X PUT "$URL/unlock/diego"
login diego diego123

echo "== Usuarios cadastrados (manutencao) =="
curl -s -u manutencao:manutencao "$URL/"; echo
