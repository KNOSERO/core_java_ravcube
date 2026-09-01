FROM node:22-alpine AS docs-runtime

WORKDIR /opt/docs-runtime

COPY docs-site/package.json /tmp/package.json

RUN cp /tmp/package.json package.json \
    && npm install --no-audit --no-fund

ENV PATH="/opt/docs-runtime/node_modules/.bin:${PATH}"
ENV NODE_PATH="/opt/docs-runtime/node_modules"

EXPOSE 3000

CMD ["docusaurus", "--version"]

FROM docs-runtime AS docs-build

COPY docs /workspace/docs
COPY docs-site /workspace/docs-site

WORKDIR /workspace/docs-site

RUN docusaurus build

FROM scratch AS docs-export

COPY --from=docs-build /workspace/docs-site/build /

FROM docs-runtime AS docs-dev
