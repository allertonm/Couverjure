(I have ceased work on this project, see "Why did I stop doing this?" below for rationale.)

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

Your best bet for now is to take a look at [the BasicCocoa example](https://github.com/allertonm/Couverjure/blob/master/Examples/BasicCocoa/src/clojure/couverjure/examples/basiccocoa.clj)

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

_Updated 06-02-2010: structure arguments are now supported, but the current mechanism for introducing new structures and their ObjC encoding is pretty clunky, and the next phase of work will look at how to improve this._

_Updated 15-02-2010: the mechanisms introduced in this version for structures (and objective-c types in general) probably wrap up how we deal with structure arguments. Structure return values still need to be worked on though._

* Does not support GC-only frameworks

Also, readers of the code should be prepared to be horrified by my lack of idiomatic Clojure and also the fact that the code is all over the map (basically my first priority has been to make it work rather than make it pretty.)

## Areas for future work ##

* Improve interpretation of method signatures to deal with structs and ref/out types - and look at whether keywords or java.lang.reflect.Types are better for defining signatures on the Clojure side of the fence. 

_Updated 06-02-2010: the code now includes a full parser for Objective-C type encodings, but currently this is only employed to interpret return types - but this will support some later phases of the work._

_Updated 15-02-2010: Rather than java types or keywords, I've settled on a third thing - 'octypes'. An octype is a pair of a java type with an objective-c type encoding, and the old keyword based mechanism has been removed in favour of this._

* Read BridgeSupport XML files to automatically generate JNA-based interfaces to non-Objective-C framework interfaces. This will probably involve generating Java source code in the first version, but I'd like to get to a completely dynamic solution, perhaps using clj-native.

_Updated 15-02-2010: Couverjure now includes a tool to read bridgesupport files and generate (for now) java/JNA classes and clojure code for the structures defined in those files. This support will be extended to define enum values, constants, interfaces to native functions and code to improve performance on method invocations. Working through building this leads me to believe that a fully dynamic, runtime only solution for structures etc would not be particularly usable in practice, so I doubt that idea will go much further._

* Investigate using either struct-maps or datatypes to represent IDs, classes, self and super references on the Clojure side. 
I'd like "self" to have some special behaviour associated with it - for example, direct access to object state, but also to be usable like an id - similar to the way (super self) can be used like an id now. This calls for some polymorphism which is currently missing.
* Build a mechanism to introspect on existing Objective-C classes and generate code to speed up message dispatch. Currently this is done completely dynamically and involves reflecting on the object and its class in order to do the right massaging of arguments and return types so we can handle things like refcounting "under the hood".

_Update 15-02-2010: this idea is likely to be dropped in favour of generating the code from bridgesupport files_

## Stuff punted for now

* Handling GC-only frameworks - this is not straightforward, even ignoring the fun involved in having two garbage collectors potentially fight each other.

# Building & running the code #

Building the code requires Xcode to be installed on your machine. With that dealt with, the following should build and run a sample browser application written using WebKit.

	cd <Couverjure-dir>/Examples/BasicCocoa
	ant run
	
# Licensing #

I'm putting this up using the [Simplified BSD license](http://en.wikipedia.org/wiki/BSD_licenses).

# A note on JNA usage

This repo contains a modified version of [JNA](https://jna.dev.java.net/) - and Couverjure now requires this modified version in order to function. 

For those interested in the technical details: the changes support using JNA's callback argument marshalling when using the CallbackProxy interface rather than deriving from Callback, through a new class TypeMappingCallbackProxy. This functionality cannot currently be implemented using the 'official' JNA's public API because it requires access to functionality that is either protected or package private. 

The intention is ultimately to contribute these changes back to JNA, but as things stand the quality is merely 'good enough' for Couverjure's current usage, but missing some features that would make it ready for prime-time (in particular, no context is supplied to TypeMapper implementations).

The JNA changes are (like JNA itself) released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/copyleft/lesser.html)

# Why did I stop doing this

The section on "why is this a terrible idea" hints at some of the reasons I could see that this would not go anywhere, and subsequent events have proven these intuitions to be correct. 

* Apple no longer ships a JVM by default 
* Applications based on the JVM are not accepted in the Mac App Store.
* Apple will never ship a JVM for iOS devices
* Apple's flirtation with garbage collection in Objective-C has given way to Automatic Reference Counting

# What should you use instead?

Those of you interested in building OS X or iOS applications in Clojure would be better off looking at the combination of ClojureScript with JSCocoa.