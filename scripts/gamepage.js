window.addEventListener('keydown', function(e) {
  var isMeaningfulKey =
    e.keyCode == 32 || // spacebar
    e.keyCode == 37 || // left arrow
    e.keyCode == 38 || // up arrow
    e.keyCode == 39 || // right arrow
    e.keyCode == 40    // down arrow

  if(isMeaningfulKey && e.target == document.body) {
    e.preventDefault();
  }
});
