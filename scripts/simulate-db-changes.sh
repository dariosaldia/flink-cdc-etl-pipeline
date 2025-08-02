#!/usr/bin/env bash
set -euo pipefail

# simulate-db-changes.sh
# Usage: ./simulate-db-changes.sh [-r RATE]
# Generates insert/update/delete operations against the 'orders' table
# in the MySQL 'inventory' DB via CDC.

# Default rate: changes per second
RATE="${RATE:-1}"

# Parse arguments
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

# Ensure RATE > 0 (portable)
if ! awk -v r="$RATE" 'BEGIN { exit (r > 0 ? 0 : 1) }'; then
  echo "ERROR: rate must be > 0, got '$RATE'"
  exit 1
fi

# Calculate sleep interval = 1/RATE, to 3 decimal places (portable)
INTERVAL=$(awk -v r="$RATE" 'BEGIN { printf "%.3f", 1/r }')

echo "[Sim] Rate=${RATE} changes/sec => interval=${INTERVAL}s"

# MySQL connection settings
MYSQL_CONTAINER="mysql"
DB_NAME="inventory"
DB_USER="debezium"
DB_PASS="dbz"

# List of product IDs
PRODUCT_IDS=(1 2 3 4 5)

i=1
while true; do
  # Select random product ID and quantity
  PROD_ID=${PRODUCT_IDS[RANDOM % ${#PRODUCT_IDS[@]}]}
  QTY=$(( (RANDOM % 5) + 1 ))
  DATE=$(date +"%Y-%m-%d %H:%M:%S")

  # INSERT
  docker exec -i "$MYSQL_CONTAINER" \
    mysql -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" \
    -e "INSERT INTO orders (product_id, quantity, order_date) \
        VALUES ($PROD_ID, $QTY, '$DATE');"
  echo "[Sim][INSERT] #$i -> productId=${PROD_ID}, qty=${QTY}, date='${DATE}'"

  # UPDATE every 3rd change
  if (( i % 3 == 0 )); then
    ID=$(( (RANDOM % i) + 1 ))
    NEW_QTY=$(( (RANDOM % 5) + 1 ))
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
      -e "UPDATE orders SET quantity=$NEW_QTY \
          WHERE order_id=$ID;"
    echo "[Sim][UPDATE] #$i order_id=${ID} -> new_qty=${NEW_QTY}"
  fi

  # DELETE every 5th change
  if (( i % 5 == 0 )); then
    ID=$(( (RANDOM % i) + 1 ))
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
      -e "DELETE FROM orders WHERE order_id=$ID;"
    echo "[Sim][DELETE] #$i -> order_id=${ID}"
  fi

  ((i++))
  sleep "$INTERVAL"
done
