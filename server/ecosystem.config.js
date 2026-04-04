module.exports = {
  apps: [
    {
      name: 'breathe',
      script: 'server.js',
      cwd: __dirname,
      env: {
        NODE_ENV: 'production',
        HOST: '0.0.0.0',
        PORT: 3000
      },
      max_memory_restart: '250M',
      autorestart: true,
      watch: false
    }
  ]
};
