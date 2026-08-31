#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: scripts/docs.sh <build|dev> [--port <port>] [--container-command <command>]" >&2
    exit 1
fi

MODE="$1"
shift

PORT="3000"
CONTAINER_COMMAND=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --port)
            PORT="$2"
            shift 2
            ;;
        --container-command)
            CONTAINER_COMMAND="$2"
            shift 2
            ;;
        *)
            echo "Unknown argument: $1" >&2
            exit 1
            ;;
    esac
done

if [[ "$MODE" != "build" && "$MODE" != "dev" ]]; then
    echo "Mode must be either 'build' or 'dev'." >&2
    exit 1
fi

IMAGE="core-java-ravcube-docs:local"
CONTAINER_NAME="core-java-ravcube-docs-dev"
CONTAINER_LABEL="com.ravcube.docs=core-java-ravcube"
WORKSPACE_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)"

test_container_command() {
    local command="$1"

    if [[ -z "$command" ]]; then
        return 1
    fi

    if ! command -v "$command" >/dev/null 2>&1 && [[ ! -x "$command" ]]; then
        return 1
    fi

    "$command" info >/dev/null 2>&1
}

test_podman_command() {
    local command="$1"

    if ! test_container_command "$command"; then
        "$command" machine start >/dev/null 2>&1 || true
    fi

    test_container_command "$command"
}

resolve_container_command() {
    if [[ -n "$CONTAINER_COMMAND" ]]; then
        local executable_name
        executable_name="$(basename "$CONTAINER_COMMAND")"

        if [[ "$executable_name" == podman* ]]; then
            if test_podman_command "$CONTAINER_COMMAND"; then
                echo "$CONTAINER_COMMAND"
                return 0
            fi
        elif test_container_command "$CONTAINER_COMMAND"; then
            echo "$CONTAINER_COMMAND"
            return 0
        fi

        echo "Configured container runtime is not available or not running: $CONTAINER_COMMAND" >&2
        exit 1
    fi

    if test_container_command "docker"; then
        echo "docker"
        return 0
    fi

    if command -v podman >/dev/null 2>&1 && test_podman_command "podman"; then
        echo "podman"
        return 0
    fi

    cat >&2 <<'EOF'
No running container runtime was found.

Start Docker or Podman, or pass the executable explicitly:
./gradlew doc-build -PcontainerCommand=docker
./gradlew doc-dev -PcontainerCommand=podman
EOF
    exit 1
}

container_ids_publishing_port() {
    local command="$1"

    "$command" ps --format "{{.ID}} {{.Ports}}" 2>/dev/null |
        awk -v port="$PORT" '$0 ~ "[:.]" port "->" || $0 ~ "[[:space:]]" port "->" { print $1 }'
}

build_docs_image() {
    local command="$1"

    "$command" build -f docs-site/Dockerfile -t "$IMAGE" .
}

stop_docs_containers() {
    local command="$1"

    echo "Stopping previous documentation containers for port $PORT if they exist."
    "$command" rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

    mapfile -t ids < <(
        {
            "$command" ps -aq --filter "label=$CONTAINER_LABEL" 2>/dev/null || true
            container_ids_publishing_port "$command" || true
        } | awk 'NF' | sort -u
    )

    if [[ ${#ids[@]} -gt 0 ]]; then
        "$command" rm -f "${ids[@]}"
    fi
}

invoke_static_site_build() {
    local command="$1"

    "$command" run \
        --rm \
        --mount "type=bind,source=$WORKSPACE_PATH,target=/workspace" \
        -w /workspace/docs-site \
        "$IMAGE" \
        docusaurus \
        build
}

invoke_docs_dev() {
    local command="$1"

    echo "Documentation will be available at http://127.0.0.1:$PORT"
    echo "Do not open http://0.0.0.0:3000; that address is only the container bind address."

    "$command" run \
        --rm \
        --name "$CONTAINER_NAME" \
        --label "$CONTAINER_LABEL" \
        -p "127.0.0.1:${PORT}:3000" \
        -e BROWSER=none \
        --mount "type=bind,source=$WORKSPACE_PATH,target=/workspace" \
        -w /workspace/docs-site \
        "$IMAGE" \
        docusaurus \
        start \
        --host 0.0.0.0 \
        --port 3000
}

RESOLVED_COMMAND="$(resolve_container_command)"
build_docs_image "$RESOLVED_COMMAND"

if [[ "$MODE" == "build" ]]; then
    invoke_static_site_build "$RESOLVED_COMMAND"
else
    stop_docs_containers "$RESOLVED_COMMAND"
    invoke_docs_dev "$RESOLVED_COMMAND"
fi
