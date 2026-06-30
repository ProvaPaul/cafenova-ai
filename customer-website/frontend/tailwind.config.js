/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        cafe: {
          50:  '#fdf8f0',
          100: '#faefd6',
          200: '#f4daaa',
          300: '#ebbf74',
          400: '#e09d44',
          500: '#d4822a',
          600: '#c06720',
          700: '#9f4e1d',
          800: '#80401f',
          900: '#6a361c',
          950: '#391a0b',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      }
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
  ]
}
