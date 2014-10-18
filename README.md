# oroboros

[![oroboros](http://i.imgur.com/RL7v1G0.jpg)](#)

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
# {"cat":"tom","mouse":"jerry","name":"tom & jerry","best":"tom"}
~~~

check out the [examples](examples) for more details

### build

building requires java7+ and leiningen

~~~sh
lein do clean, ring uberjar
~~~

### run

using fig:

~~~sh
fig up
~~~

or just run the jar:

~~~sh
(cd examples; java -jar ../target/oroboros-0.1.0-SNAPSHOT-standalone.jar)
~~~
