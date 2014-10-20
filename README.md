# oroboros

<img src="http://i.imgur.com/qZl5BBA.jpg"
 alt="oroboros img" title="oroboros" align="right" />

> "In the age-old image of the uroboros lies the thought of devouring oneself and turning oneself into a circulatory process."
> 
> "They are centers of a mighty force, figures pregnant with an awful power…”

*oroboros is a configuration server that uses itself as the templating context of its string values*

you can use it to enable service oriented systems that fetch remote configuration over http

all configuration is stored in a user defined file hierarchy

## usage

configs use themselves as their own templating context, so we can do things like:

~~~yml
# examples/simple/config.yaml
cat: 'tom'
mouse: 'jerry'
name: '{{ cat }} & {{ mouse }}'
best: '{{ favorite }}'
~~~

By placing a named config next to the default `config.yaml`, we can provide context specific overrides

~~~yml
# examples/simple/tom.yaml
favorite: '{{ cat }}'
~~~

And fetch the rendered json config over http:

~~~sh
curl $oroboros/q?config=tom | python -m json.tool
~~~

~~~json
{
  "cat": "tom",
  "mouse": "jerry",
  "name": "tom & jerry",
  "best": "tom"
}
~~~

check out the [examples](examples) for more details

### web ui

a simple web ui is included for exploring configuration

![ui](http://i.imgur.com/dlRTXUD.png)

### running

if you have [docker](https://docker.io) installed::

~~~sh
docker run -v $PWD/examples:/etc/oroboros/configs -p 8080:80 egghead/oroboros
~~~

Or just [grab a copy of the jar](https://github.com/eggsby/oroboros/releases) and run it in your config directory.

### development

building oroboros requires java7+, leiningen, & bower

~~~sh
bower install
lein do clean, ring uberjar
~~~

Once built, there are a few different ways to run oroboros in your environment.

using [fig](http://www.fig.sh/) for development:

~~~sh
fig up
~~~

using [lein](http://leiningen.org/):

~~~sh
lein trampoline ring server-headless
~~~

Once you have the server started, play around with editing the configs, or create some of your own.
