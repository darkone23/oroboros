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

  function setActive(li, a) {
    return function(event) {
      $nav.find("li").removeClass("active");
      li.addClass("active");
      load(a.text());
    };
  }

  var $nav = $(".nav-sidebar"),
      li = $nav.find("li"), a = $nav.find("a");
  li.on("click", setActive(li, a));

  $.get("/configs").then(function(data){
    $(data).each(function(i, el) {
      var li = $("<li>"), a = $("<a>");
      li.append(a.text(el));
      li.on("click", setActive(li, a));
      $nav.append(li);
      });
  });

  load();

});
