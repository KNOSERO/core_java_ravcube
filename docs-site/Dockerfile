FROM node:22-alpine

WORKDIR /opt/docs-runtime

COPY docs-site/package.json /tmp/package.json

RUN cp /tmp/package.json package.json \
    && npm install --no-audit --no-fund

ENV PATH="/opt/docs-runtime/node_modules/.bin:${PATH}"
ENV NODE_PATH="/opt/docs-runtime/node_modules"

EXPOSE 3000

CMD ["docusaurus", "--version"]
