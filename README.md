# What is Couverjure? #

Couverjure is an attempt at building a direct bridge between Clojure and Objective-C.

By "direct" I mean that while it may involve some Java code, it does not involve first bridging between Objective-C and Java classes (i.e using [Rococoa](https://rococoa.dev.java.net/)), and then using Clojure's Java interop to work with those classes. Instead it attempts to go direct to the Objective-C Runtime as much as possible and does not attempt to cater to Java code at all.

# Why do this? #

I had this crazy idea that Clojure might play nicer with Cocoa than Java did, and wanted to see if I was right. 

Apple's Java-Cocoa bridge was a failure - and in hindsight it's not that hard to see why. Despite being an Ahead-of-Time compiled language based on C, Objective-C's runtime is in fact quite a bit more dynamic than Java's - and this left Java developers at a disadvantage, being made to write more code to work around the impedance mismatch.

So why might Clojure be a better match? As a dynamic language it may be a better fit for the dynamic aspects of the Objective-C runtime. In addition, as a lisp, Clojure has few ties between programming paradigm and syntax, making it easier to adopt Objective-C's paradigms directly in Clojure code.

# Why is this a terrible idea? #

There are some other reasons Java was a bad fit for Cocoa, and Clojure doesn't fix these - namely the memory overhead of the JVM, startup time and lack of application responsiveness due to JIT compilation and garbage collection. While Objective-C has introduced GC, it's worth noting that it doesn't work on the iPhone. And speaking of the iPhone, it's unlikely that the JVM will ever be on it (or on the Tablet/Slate/whatever.)

So Couverjure may ultimately be answering a question no-one is asking. It's a good job I did it for the fun of it.

# About the name #

Couverjure is a deliberate mispelling of _"couverture"_ in the same vein as Clojure/Closure.

Couverture is the French word for "coverage", and is also used to refer to the high quality chocolate used by chocolatiers to make chocolates. Hopefully I need explain no further than this.

# What does the code using Couverjure look like?

Your best bet for now is to take a look at [the BasicCocoa example](Examples/BasicCocoa/src/clojure)

# Status of the code

The best description of the current code status is "demonstration" or "proof of concept" quality. It works well enough to run the unit tests I've written so far and the webkit example in Examples/BasicCocoa but if you attempt to do more or less anything else you will find something that breaks.

## What does/should work

* Instantiating existing Objective-C classes
* Invoking methods on instances (and classes??)
* Refcounting of Objective-C objects handled (mostly) behind the scenes
* Implementing new Objective-C classes  
* GC Lifecycle of java/clojure objects held by Clojure-based classes should be handled (i.e java objects held are freed when the objc object is dealloced.)
* Creating an application bundle based on Clojure code
* Starting AppKit event loop
* Loading and wiring classes (implemented in Clojure) referenced from NIB files

## Examples of stuff known to be broken ##

* Only supports 64-bit Intel architecture
* Cannot define class methods
* Current implementation of "super" will break if you derive from a class implemented in Clojure
* Does not support any kind of property apart from "id" types
* Using copy methods will leak objects
* Does not support any argument or return type that cannot be represented by a single character type encoding (this means structure types such as NSRange and also byref or inout types are not yet supported)
* Does not support GC-only frameworks

Also, readers of the code should be prepared to be horrified by my lack of idiomatic Clojure and also the fact that the code is all over the map (basically my first priority has been to make it work rather than make it pretty.)

## Areas for future work ##

* Clean the code up a bit
* Investigate whether it's possible to hide some of the CPU architecture dependencies in the JNA layer (for example, by creating custom type mapping for ID types that is both architecture sensitive *and* allows us to do all the things we need to be able to do with something typed "id", namely sometimes cast it to int, char, boolean or even a pointer to string.)
* Investigate using either struct-maps or datatypes to represent IDs, classes, self and super references on the Clojure side. 
I'd like "self" to have some special behaviour associated with it - for example, direct access to object state, but also to be usable like an id - similar to the way (super self) can be used like an id now. This calls for some polymorphism which is currently missing.
* Read BridgeSupport XML files to automatically generate JNA-based interfaces to non-Objective-C framework interfaces - the [clj-native library](http://github.com/bagucode/clj-native) looks very promising for this.
* Build a mechanism to introspect on existing Objective-C classes and generate code to speed up message dispatch. Currently this is done completely dynamically and involves reflecting on the object and its class in order to do the right massaging of arguments and return types so we can handle things like refcounting "under the hood".

## Stuff punted for now

* Handling GC-only frameworks - this is not straightforward, even ignoring the fun involved in having two garbage collectors potentially fight each other.

# Building & running the code #

Building the code requires Xcode to be installed on your machine. With that dealt with, the following should build and run a sample browser application written using WebKit.

	cd <Couverjure-dir>/Examples/BasicCocoa
	ant run
	
# Licensing #

I'm putting this up using the [Simplified BSD license](http://en.wikipedia.org/wiki/BSD_licenses).

