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
# {"cat":"tom","mouse":"jerry","name":"tom & jerry","best":"tom"}
~~~

check out the [examples](examples) for more details

### build

building oroboros requires java7+ and leiningen

~~~sh
lein do clean, ring uberjar
~~~

### run

Once built, there are a few different ways to run oroboros in your environment.

using [fig](http://www.fig.sh/) for development:

~~~sh
fig up
~~~

using [docker](https://docker.io) for deployment:

~~~sh
# TODO: publish to docker registry so this is just `docker run $OPTS egghead/oroboros`
docker build -t oroboros .
docker run -v $PWD/examples:/etc/oroboros/configs -p 3000:3000 oroboros java -jar /etc/oroboros/o.jar
~~~

using [lein](http://leiningen.org/):

~~~sh
lein trampoline ring server-headless
~~~

or just run the jar in some config dir:

~~~sh
cd examples
java -jar ../target/oroboros-0.1.0-SNAPSHOT-standalone.jar
~~~

Once you have the server started, play around with editing the configs, or create some of your own.
