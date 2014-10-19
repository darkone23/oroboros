$(function() {
  "use strict";

  function autoLink(text) {
    var entities = twttr.txt.extractEntitiesWithIndices(text, { extractUrlsWithoutProtocol: true });
    text = twttr.txt.autoLinkEntities(text, entities, { targetBlank: true });
    // special case for twitter text missing links on port nums
    var missedPort = text.match(/\:\d+$/)
    if (missedPort) {
      var port = missedPort[0], a = $(text)
      a.attr("href", a.attr("href") + port);
      a.text(a.text() + port);
      text = a.get(0).outerHTML;
    }
    return text;
  }

  var $main = $ (".main");
  
  function load(config) {
    config = config || "config";
    $.get("/q?config="+config).then(function(data){
      new PrettyJSON.view.Node({
        el: $main.empty(),
        data: data
      }).expandAll();
      $main.find(".string").each(function() {
        var text = $(this).text()
        $(this).html(autoLink(text));
      });
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
