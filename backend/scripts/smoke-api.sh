#!/usr/bin/env bash
set -euo pipefail

base_url="${BASE_URL:-http://localhost:8080}"
username="${SMOKE_USERNAME:-}"
password="${SMOKE_PASSWORD:-}"
curl_bin="${CURL_BIN:-curl}"

if [[ -z "$username" || -z "$password" ]]; then
  echo "请设置 SMOKE_USERNAME 和 SMOKE_PASSWORD" >&2
  exit 2
fi

assert_success() {
  local label="$1"
  local payload="$2"
  PAYLOAD="$payload" python3 - "$label" <<'PY'
import json
import os
import sys

label = sys.argv[1]
payload = json.loads(os.environ["PAYLOAD"])
if payload.get("code") != 0:
    raise SystemExit(f"{label} 失败: {payload}")
print(f"✓ {label}")
PY
}

health_json="$("$curl_bin" -fsS "$base_url/api/health")"
assert_success "网关与平台健康检查" "$health_json"

login_json="$("$curl_bin" -fsS -X POST "$base_url/api/auth/login" \
  -H 'Content-Type: application/json' \
  --data-binary "$(python3 - "$username" "$password" <<'PY'
import json
import sys
print(json.dumps({"username": sys.argv[1], "password": sys.argv[2]}))
PY
)")"
assert_success "账号登录" "$login_json"

token="$(PAYLOAD="$login_json" python3 - <<'PY'
import json
import os
payload = json.loads(os.environ["PAYLOAD"])
print(payload.get("data", {}).get("token", ""))
PY
)"
if [[ -z "$token" ]]; then
  echo "登录响应中没有 data.token" >&2
  exit 1
fi

me_json="$("$curl_bin" -fsS "$base_url/api/auth/me" -H "Authorization: Bearer $token")"
assert_success "平台令牌解析" "$me_json"

dashboard_json="$("$curl_bin" -fsS "$base_url/api/dashboard/ws/heartbeat" \
  -H "Authorization: Bearer $token")"
assert_success "dashboard 路由与跨服务令牌解析" "$dashboard_json"

echo "全部冒烟检查通过"
