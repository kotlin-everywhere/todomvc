require('todomvc-common/base.css');
require('todomvc-app-css/index.css');
// require('todomvc-common/base');

require('../css/app.css');

var kt = require('todomvc');
var app = document.querySelector('#app');
var root = document.createElement('div');
app.appendChild(root);
kt.com.minek.kotlin.everywhere.todomvc.main(root);
