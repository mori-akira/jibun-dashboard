FROM golang:1.24.4-bookworm

RUN apt-get update && apt-get install -y \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd -m vscode

CMD ["sleep", "infinity"]
