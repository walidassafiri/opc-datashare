module.exports = {
  module: {
    rules: [
      {
        test: /\.[jt]s$/,
        enforce: "post",
        exclude: [/node_modules/, /\.spec\.ts$/, /src\/test\.ts$/, /cypress/],
        use: {
          loader: "babel-loader",
          options: {
            plugins: ["babel-plugin-istanbul"],
          },
        },
      },
    ],
  },
};
