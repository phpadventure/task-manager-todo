/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.vue',
    './src/**/*.js',
    './src/**/*.jsx',
    './src/**/*.ts',
    './src/**/*.tsx',
  ],
  theme: {
    extend: {
      screens: {
        tablet: '768px',
      },
    },
  },
};
