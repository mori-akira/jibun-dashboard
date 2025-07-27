# devcontainer用のベースイメージ
FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu

# AWS CLIをインストール
RUN apt-get update
RUN apt-get install -y --no-install-recommends unzip curl groff less ca-certificates
RUN update-ca-certificates
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install
RUN rm -rf awscliv2.zip aws

# ls系のエイリアスを予め使えるようにコメントを外す
RUN sed -i 's/^#alias ll=/alias ll=/; s/^#alias la=/alias la=/; s/^#alias l=/alias l=/' /home/vscode/.bashrc

# node_moduleディレクトリの権限を調整
RUN mkdir -p /workspace/front/node_modules && \
    chown -R vscode:vscode /workspace/front/node_modules

# 環境変数設定
ENV NUXT_PUBLIC_API_BASE_URL="http://localhost:4011/"
ENV NUXT_PUBLIC_REQUIRE_AUTH="off"
