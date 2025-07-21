#!/usr/bin/env bash
set -euo pipefail

# Name of your MySQL container (as in docker-compose.yml)
MYSQL_CONTAINER="mysql"

# MySQL creds
DB_NAME="inventory"
DB_USER="debezium"
DB_PASS="dbz"

# A few sample products to pick from
PRODUCTS=(Widget Gadget Thingamajig Doohickey)

i=1
while true; do
  # 1) INSERT a new order
  PROD="${PRODUCTS[RANDOM % ${#PRODUCTS[@]}]}"
  QTY=$((RANDOM % 5 + 1))
  DATE=$(date +%F)   # YYYY-MM-DD
  docker exec -i "$MYSQL_CONTAINER" \
    mysql -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" \
    -e "INSERT INTO orders (product_name,quantity,order_date) \
        VALUES ('$PROD',$QTY,'$DATE');"
  echo "[Sim] Inserted order #$i ($PROD ×$QTY @ $DATE)"

  # 2) Every 3 inserts, do an UPDATE
  if (( i % 3 == 0 )); then
    # pick a random existing order_id between 1 and i
    ID=$((RANDOM % i + 1))
    NEWQTY=$((RANDOM % 5 + 1))
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
      -e "UPDATE orders SET quantity=$NEWQTY WHERE order_id=$ID;"
    echo "[Sim] Updated order #$ID → qty=$NEWQTY"
  fi

  # 3) Every 5 inserts, do a DELETE
  if (( i % 5 == 0 )); then
    ID=$((RANDOM % i + 1))
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
      -e "DELETE FROM orders WHERE order_id=$ID;"
    echo "[Sim] Deleted order #$ID"
  fi

  ((i++))
  sleep 1
done
