#!/usr/bin/env bash
set -euo pipefail

# Default rate (changes per second)
RATE="${RATE:-1}"

# parse -r|--rate
while [[ $# -gt 0 ]]; do
  case "$1" in
    -r|--rate)
      RATE="$2"
      shift 2
      ;;
    *)
      echo "Usage: $0 [-r|--rate <changes_per_second>]"
      exit 1
      ;;
  esac
done

# Validate RATE is positive number
if ! awk "BEGIN{exit !($RATE > 0)}"; then
  echo "ERROR: rate must be > 0: got '$RATE'"
  exit 1
fi

# Compute sleep interval = 1/RATE, to 3 decimal places
INTERVAL=$(awk "BEGIN{printf \"%.3f\", 1/$RATE}")

# MySQL container & creds
MYSQL_CONTAINER="mysql"
DB_NAME="inventory"
DB_USER="debezium"
DB_PASS="dbz"

PRODUCTS=(Widget Gadget Thingamajig Doohickey)

i=1
echo "[Sim] Starting with RATE=${RATE} changes/sec (interval=${INTERVAL}s)"
while true; do
  # INSERT
  PROD="${PRODUCTS[RANDOM % ${#PRODUCTS[@]}]}"
  QTY=$((RANDOM % 5 + 1))
  DATE=$(date +%F)
  docker exec -i "$MYSQL_CONTAINER" \
    mysql -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" \
    -e "INSERT INTO orders (product_name,quantity,order_date) VALUES ('$PROD',$QTY,'$DATE');"
  echo "[Sim] Inserted #$i (${PROD}×${QTY} @ ${DATE})"

  # UPDATE every 3
  if (( i % 3 == 0 )); then
    ID=$((RANDOM % i + 1))
    NEWQTY=$((RANDOM % 5 + 1))
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
      -e "UPDATE orders SET quantity=$NEWQTY WHERE order_id=$ID;"
    echo "[Sim] Updated #$ID → qty=$NEWQTY"
  fi

  # DELETE every 5
  if (( i % 5 == 0 )); then
    ID=$((RANDOM % i + 1))
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
      -e "DELETE FROM orders WHERE order_id=$ID;"
    echo "[Sim] Deleted #$ID"
  fi

  ((i++))
  sleep "$INTERVAL"
done
