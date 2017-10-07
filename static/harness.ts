/// <reference path="./bridge2.d.ts"/>

import * as Bridge from  "bridge";

// Harness
let button = document.createElement('button');
button.textContent = "Ping editor";
button.onclick = function() {
    alert(Bridge.ping());
}

document.body.appendChild(button);
