const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter var', ...defaultTheme.fontFamily.sans],
      },
    },
  },
  plugins: [
    require('@tailwindcss/ui'),
  ],
  purge: {
    content: [
      './src/**/*.html',
      './src/**/*.cljs',
    ],
    options: {
      whitelist: ['save-this-style'],
    }
  },
}
