"use strict";
/// <reference path="./bridge2.d.ts"/>
exports.__esModule = true;
var Bridge = require("bridge");
// Harness
var button = document.createElement('button');
button.textContent = "Ping editor";
button.onclick = function () {
    alert(Bridge.ping());
};
document.body.appendChild(button);
