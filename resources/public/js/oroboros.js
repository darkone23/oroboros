$(function() {
  "use strict";

  function autoLink(text) {
    var entities = twttr.txt.extractEntitiesWithIndices(text, { extractUrlsWithoutProtocol: true });
    text = twttr.txt.autoLinkEntities(text, entities, { targetBlank: true });
    // special case for twitter text missing links on port nums
    var missedPort = text.match(/\:\d+$/)
    if (missedPort) {
      var port = missedPort[0], a = $(text)
      if (a.size()) {
        a.attr("href", a.attr("href") + port);
        a.text(a.text() + port);
        text = a.get(0).outerHTML;
      }
    }
    return text;
  }

  var $main = $ (".main");
  var $nav = $ (".nav-sidebar");

  var App = Backbone.Router.extend({
    routes: {
      "": "home",
      ":config": "config"
    },
    home: function() {
      Backbone.history.loadUrl("config");
    },
    config: function(config) {
      $.get("/q?config="+config).then(function(data){
        var $lis = $nav.find("li");
        $lis.removeClass("active");
        var $match = $lis.filter(function() {
          return $(this).find("a").text() === config;
        });
        $match.addClass("active");
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
  });

  function init() {
    // initialize nav sidebar
    $.get("/configs").then(function(data){
      $(data).each(function(i, el) {
        var li = $("<li>"), a = $("<a>");
        a.attr("href", "#" + el);
        li.append(a.text(el));
        $nav.append(li);
      });
      var app = new App();
      Backbone.history.start();
    });
  }

  init();

});
