$(function() {
  "use strict";

  var $main = $ (".main");
  
  function load(config) {
    config = config || "config";
    $.get("/q?config="+config).then(function(data){
      new PrettyJSON.view.Node({
        el: $main.empty(),
        data: data
      }).expandAll();
    });
  }

  load();
  
});



