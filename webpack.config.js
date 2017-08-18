var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');

var isProduction = process.env.NODE_ENV === "production";

// noinspection JSUnusedGlobalSymbols
var config = {
    entry: './js/app.js',
    resolve: {
        modules: ['./node_modules', './build/kotlin-javascript-dependencies', './build/classes/main'].map(function (s) {
            return path.resolve(__dirname, s);
        })
    },
    output: {
        path: __dirname + "/output",
        filename: "[name].[hash].bundle.js"
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
                loader: 'url-loader',
                options: {limit: 10000}
            },
            {
                test: /\.(png|jpg|gif|svg|eot|ttf|woff|woff2)$/,
                loader: 'file-loader'
            }
        ]
    },
    plugins: [
        new HtmlWebpackPlugin({template: './index.ejs'})
    ]
};

if (!isProduction) {
    config.plugins.push(
        new webpack.HotModuleReplacementPlugin()
    );

    config.devServer = {hot: true};
}

module.exports = config;
