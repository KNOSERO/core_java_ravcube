module.exports = {
  docs: [
    'index',
    {
      type: 'category',
      label: 'Getting Started',
      items: [
        'getting-started/index',
        'getting-started/project-map',
      ],
    },
    {
      type: 'category',
      label: 'Production Libraries',
      items: [
        'libraries/common',
        'libraries/cache',
        'libraries/idempotency',
        'libraries/data',
        'libraries/search',
        'libraries/event',
        'libraries/stream',
        'libraries/security',
        'libraries/eureka',
        'libraries/fault-tolerance',
      ],
    },
    {
      type: 'category',
      label: 'Test Modules',
      items: [
        'test-modules/index',
        'test-modules/common',
        'test-modules/awaitility',
        'test-modules/redis',
        'test-modules/kafka',
        'test-modules/nats',
        'test-modules/postgresql',
        'test-modules/elasticsearch',
        'test-modules/keycloak',
        'test-modules/eureka',
      ],
    },
  ],
};
