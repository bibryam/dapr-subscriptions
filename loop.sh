#!/bin/bash

for i in {1..1000}
do
  curl -X POST http://localhost:5001/pubsub/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId": '"$i"'}'
  
  sleep 1
done

