module.exports = {
  mode: 'none',
  entry: './src/js/app.js',
  output: {
    filename: './js/bundle.js'
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          'style-loader',
          { loader: 'css-loader', options: { importLoaders: 1 } },
          'postcss-loader'
        ]
      }
    ]
  }
}

