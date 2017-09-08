require('todomvc-common/base.css');
require('todomvc-app-css/index.css');
// require('todomvc-common/base');

require('../css/app.css');

var kt = require('todomvc');
var model = null;
if (module.hot) {
    module.hot.accept();

    module.hot.dispose(function (data) {
        data.appModel = program.stop();
        while (app.hasChildNodes()) {
            app.removeChild(app.firstChild);
        }
    });

    if (module.hot.data) {
        if (module.hot.data.appModel) {
            model = module.hot.data.appModel;
        }
    }
}

var app = document.querySelector('#app');
var root = document.createElement('div');
app.appendChild(root);
var program = kt.com.minek.kotlin.everywhere.todomvc.main(root, model);
