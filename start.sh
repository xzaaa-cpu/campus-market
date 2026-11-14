#!/usr/bin/env bash
# 一键启动：后端 8080 + 前端 5173
# 用法： ./start.sh        （需要本机已安装 JDK 11+、Maven、Node 18+）

set -e
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> 启动后端 (http://localhost:8080)"
cd "$ROOT_DIR/backend"
mvn -q spring-boot:run &
BACKEND_PID=$!

echo "==> 等待后端就绪..."
for i in $(seq 1 60); do
  if curl -s http://localhost:8080/api/categories > /dev/null 2>&1; then
    echo "后端已就绪"
    break
  fi
  sleep 2
done

echo "==> 启动前端 (http://localhost:5173)"
cd "$ROOT_DIR/frontend"
if [ ! -d node_modules ]; then
  npm install
fi
npm run dev

trap "kill $BACKEND_PID 2>/dev/null || true" EXIT
