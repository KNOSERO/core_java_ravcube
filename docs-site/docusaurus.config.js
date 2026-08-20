const path = require('path');

module.exports = {
  title: 'Ravcube Core Java',
  tagline: 'Spring integration libraries with clear module boundaries.',
  url: 'http://localhost:3000',
  baseUrl: '/',
  organizationName: 'ravcube',
  projectName: 'core-java-ravcube',
  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',
  presets: [
    [
      'classic',
      {
        docs: {
          path: path.resolve(__dirname, '..', 'docs'),
          routeBasePath: '/',
          sidebarPath: require.resolve('./sidebars.js'),
        },
        blog: false,
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
  themeConfig: {
    navbar: {
      title: 'Ravcube Core Java',
      items: [
        {to: '/', label: 'Start', position: 'left'},
        {to: '/getting-started/project-map', label: 'Modules', position: 'left'},
        {to: '/test-modules', label: 'Testing', position: 'left'},
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Modules',
          items: [
            {label: 'Production libraries', to: '/libraries/common'},
            {label: 'Test modules', to: '/test-modules'},
          ],
        },
      ],
    },
    prism: {
      additionalLanguages: ['java', 'kotlin', 'powershell'],
    },
  },
};
