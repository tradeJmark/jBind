# jBind
jBind is a lightweight library for binding HTML Element
text and attributes to live, updatable values that are
supplied by Kotlin/JS code.
## Installing
Until I get around to bothering to figuring out how to publish
my stuff to Maven Central, or else until GitHub gets around to
fixing its dumb package system to allow unauthenticated
downloads, installing this library will unfortunately be pretty
awkward. Here's how you'd do it in a Kotlin DSL Gradle script,
and it should translate pretty easily to whatever similar
system you may use.
```kotlin
repositories {
    //...other ones
    maven {
        url = uri("https://maven.pkg.github.com/tradeJmark/jBind")
        credentials {
            username = githubActor ?: System.getenv("GITHUB_ACTOR")
            password = githubToken ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    //...other ones
    implementation("ca.tradejmark.jbind:jbind:0.0.1")
}
```
And then, of course, you'd have to either set those variables
or use the environment variables, where `GIGHUB_ACTOR` is your
GitHub username and `GITHUB_TOKEN` is a [Personal Access Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token).

## Usage
### Specifying Bindings
In jBind, bound values are each identified by a Bind Location,
subdivided into a Bind Path, Object Name, and Value Name.
The Path may contain multiple dot-separated values, and
the Object Name and Value Name are appended to the Path, also
using dot-separation. So, for example, the location `my.path.element.name`
references the value `name` of an object named `element` located
at `my.path`. In HTML, you can bind the text of an object to a
value by specifying the value's Bind Location in the `data-jbind-text`
attribute, as shown below.
```html
<div data-jbind-text="my.path.element.name"></div>
```
As values are supplied for the `my.path.element.name` location,
the div will be updated accordingly. For example, if the value
"Text" were supplied at that location, the div would become:
```html
<div data-jbind-text="my.path.element.name">Text</div>
```
Any subsequent values at that location would cause the div to
automatically update accordingly.

Attribute binding is done by specifying all attributes to be
bound in `data-jbind-attributes` (comma-separated), then 
giving those attributes Bind Locations as values, as in this
example:
```html
<div data-jbind-attributes="hidden,data-status" 
     hidden="data.div.hidden" 
     data-status="data.div.status">
</div>
```
At bind-time, the locations will be replaced with the supplied values.

#### Kotlin HTML DSL
If you are using `kotlinx.html`, you can specify these bindings
in a type-safe way as shown in the following example:
```kotlin
div {
    val path = BindPath("my").sub("path")
    bindText(path.obj("element").value("name"))
    val divObject = path.obj("div")
    bindAttributes(mapOf(
        "hidden" to divObject.value("hidden"),
        "data-status" to divObject.value("status")
    ))
}
```
### Initiating the binding
In your code, in order to initiate the binding, you need to call
the `JBind::bind` method, supplying it a root element (the
entire subtree of which will be bound) and a `Provider`
which knows where to find the values. Here is an example:
```kotlin
val element = document.body!!.getElementById("bind-root")
JBind.bind(element, WebSocketProvider())
```
### Providers
A `Provider` knows how to supply a flow of values for a provided
location. A `Provider` could be as simple as this:
```kotlin
object StaticProvider: Provider {
    override fun getValue(location: BindValueLocation): Flow<String> {
        if (location == BindPath("mypath").obj("object").value("name")) {
            return flow { emit("The Name") }
        }
        else throw UnavailableError(location)
    }
}
```
In general a `Provider` like that would have fairly limited use,
but you can create a custom `Provider` for almost any use you
could need, like reading from a database or calling back to the 
server. 

`WebSocketProvider` is supplied by the library, and will
kick the can of providing values down to the server over a
WebSocket. By default it will connect to the current host at the
root over a plain `ws://` connection, but you can supply a custom
WebSocket if you want to use a different path or `wss://`:
```kotlin
val ws = WebSocket("wss://somesite.com/jbind")
val provider = WebSocketProvider(ws)
JBind.bind(document.body!!, provider)
```
If you are using Ktor as your server, you can include jBind in
your backend dependencies, and then use the JBind plugin to
accept connections from `WebSocketProvider` instances:
```kotlin
fun Application.main() {
    install(JBind)
    routing {
        route("jbind") {
            val provider: Provider = MyProviderForGettingValuesFromTheDBOrWhatever()
            jBind(provider)
        }
    }
}
```
In tandem with the appropriate `WebSocketProvider` on the
client-end, this should rather seamlessly behave as though
the server-side `Provider` were supplied directly to the client.

## Upcoming
In the near future, I plan to add facilities for arrays of objects,
and also the ability to bind an entire object to an element.
What either of those might look like is still pretty fluid, so
stay tuned.

## HQs
I don't have FAQs on my stuff because I've never made anything
famous enough for people to frequently ask questions about it, but
here are some hypothetical questions.
### Q: Can I consume this from JavaScript/TypeScript?
### A: Not really.
I'd be interested in making that work maybe at some point,
but right now the public interface relies on Kotlin stuff
like `Flow` that doesn't translate. For now and the 
foreseeable future, you'd mostly want to use this library
if you were already making a frontend in Kotlin/JS.
### Q: Is this production ready?
### A: Not really.
I mean, all of the tests pass and everything seems to work,
so if you're interested, I say have at it, but it's never
been used in production up to now, so you'd be on an
adventure.