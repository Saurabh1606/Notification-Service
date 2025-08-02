#!/bin/bash

API_BASE="http://localhost:8000/api/v1"
CONCURRENT_USERS=10
REQUESTS_PER_USER=100

echo "🔥 Running load test..."
echo "Concurrent users: $CONCURRENT_USERS"
echo "Requests per user: $REQUESTS_PER_USER"

# Function to send requests
send_requests() {
  local user_id=$1
  local requests=$2
  
  for i in $(seq 1 $requests); do
    curl -s -X POST "$API_BASE/notifications" \
      -H "Content-Type: application/json" \
      -d "{
        \"userId\": \"load_test_user_$user_id\",
        \"title\": \"Load Test $i\",
        \"content\": \"This is load test message $i from user $user_id\",
        \"channels\": [\"EMAIL\"]
      }" > /dev/null
      
    if [ $((i % 10)) -eq 0 ]; then
      echo "User $user_id: Sent $i requests"
    fi
  done
  
  echo "User $user_id: Completed $requests requests"
}

# Start concurrent users
for user in $(seq 1 $CONCURRENT_USERS); do
  send_requests $user $REQUESTS_PER_USER &
done

# Wait for all background jobs to complete
wait

echo "✅ Load test completed!"
echo "Total requests sent: $((CONCURRENT_USERS * REQUESTS_PER_USER))"
echo "Check Prometheus metrics at http://localhost:9090"
