#!/usr/bin/env bash

# Establish root directory execution path (run from scripts/ folder or root)
cd "$(dirname "$0")/.."

# ==============================================================================
# 🚀 AI CONTEXT GENERATOR - TOKEN OPTIMIZER
# ==============================================================================
# This script scans your microservices and frontend workspace, filters out
# generated code, external libraries, secrets, and binary assets, and combines
# your actual hand-written source code into a single, clean Markdown file.
#
# Usage:
#   chmod +x generate-ai-context.sh
#   ./generate-ai-context.sh
# ==============================================================================

OUTPUT_FILE="project_codebase_context.md"

# Clear previous output
echo "" > "$OUTPUT_FILE"

echo "===================================================="
echo "🔍 Scanning workspace for source files..."
echo "===================================================="

# Temporary file to store file list
TEMP_LIST=$(mktemp)

# Find relevant files, ignoring bulky dependencies, binary builds, and secrets
find . -type f \
  ! -path "*/node_modules/*" \
  ! -path "*/target/*" \
  ! -path "*/.git/*" \
  ! -path "*/.idea/*" \
  ! -path "*/.vscode/*" \
  ! -path "*/certs/*" \
  ! -path "*/dist/*" \
  ! -path "*/build/*" \
  ! -name "*.p12" \
  ! -name "*.pem" \
  ! -name "*.key" \
  ! -name "*.crt" \
  ! -name "*.jks" \
  ! -name "*.png" \
  ! -name "*.jpg" \
  ! -name "*.jpeg" \
  ! -name "*.gif" \
  ! -name "*.ico" \
  ! -name "*.woff*" \
  ! -name "*.ttf" \
  ! -name "package-lock.json" \
  ! -name "yarn.lock" \
  ! -name "pnpm-lock.yaml" \
  ! -name "$OUTPUT_FILE" \
  \( \
     -name "*.java" \
     -o -name "*.jsx" \
     -o -name "*.js" \
     -o -name "*.ts" \
     -o -name "*.tsx" \
     -o -name "*.css" \
     -o -name "*.html" \
     -o -name "*.xml" \
     -o -name "*.yaml" \
     -o -name "*.yml" \
     -o -name "*.properties" \
     -o -name "*.sh" \
     -o -name "*.env*" \
     -o -name "Dockerfile" \
     -o -name "docker-compose*" \
  \) > "$TEMP_LIST"

TOTAL_FILES=$(wc -l < "$TEMP_LIST" | xargs)
echo "Found $TOTAL_FILES relevant source code files!"
echo "Bundling files into $OUTPUT_FILE..."

# Write Markdown Header
cat << 'EOF' >> "$OUTPUT_FILE"
# 📦 TRANSCENDENCE MICROSERVICES CONTEXT

This single document contains the handwritten source code of the Transcendence Microservices Stack. It is optimized to be highly token-efficient for AI context ingestion.

## 🗂️ Project Structure Summary
EOF

# Append directory layout
echo "Generating directory summary..."
echo '```' >> "$OUTPUT_FILE"
find . -maxdepth 3 \
  ! -path "*/node_modules*" \
  ! -path "*/target*" \
  ! -path "*/.git*" \
  ! -path "*/.idea*" \
  ! -path "*/.vscode*" \
  ! -path "*/certs*" \
  ! -path "*/dist*" \
  ! -path "*/build*" \
  -not -name "." | sort | sed -e 's;[^/]*/;|____;g;s;____|; |;g' >> "$OUTPUT_FILE"
echo '```' >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Process each file
CURRENT_COUNT=0
while IFS= read -r file; do
  ((CURRENT_COUNT++))
  
  # Determine markdown syntax highlighting language
  ext="${file##*.}"
  lang="text"
  case "$ext" in
    java) lang="java" ;;
    jsx|js) lang="javascript" ;;
    tsx|ts) lang="typescript" ;;
    xml) lang="xml" ;;
    yaml|yml) lang="yaml" ;;
    css) lang="css" ;;
    html) lang="html" ;;
    sh) lang="bash" ;;
    properties) lang="properties" ;;
  esac
  
  if [[ "$file" == *"Dockerfile"* ]]; then
    lang="dockerfile"
  elif [[ "$file" == *".env"* ]]; then
    lang="properties"
  fi

  # Append file context
  echo "📄 Adding [$CURRENT_COUNT/$TOTAL_FILES]: $file"
  
  echo "## 📄 File: $file" >> "$OUTPUT_FILE"
  echo '```'"$lang" >> "$OUTPUT_FILE"
  cat "$file" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"
  echo '```' >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"

done < "$TEMP_LIST"

rm "$TEMP_LIST"

echo "===================================================="
echo "🎉 SUCCESS! Single context file generated:"
echo "📂 $OUTPUT_FILE"
echo "===================================================="
