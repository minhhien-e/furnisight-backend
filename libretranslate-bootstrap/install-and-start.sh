#!/bin/bash
set -euo pipefail

PACKAGES_DIR="/home/libretranslate/.local/share/argos-translate/packages"
PYTHON_BIN="/app/venv/bin/python"

if [ -d "$PACKAGES_DIR" ]; then
  shopt -s nullglob
  for model_path in "$PACKAGES_DIR"/*.argosmodel; do
    echo "Installing Argos model: $model_path"
    "$PYTHON_BIN" - "$model_path" <<'PY'
import sys
import argostranslate.package

model_path = sys.argv[1]
argostranslate.package.install_from_path(model_path)
print(f"Installed {model_path}")
PY
  done
fi

cd /app
exec ./scripts/entrypoint.sh
