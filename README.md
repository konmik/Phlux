Phlux
=======

Phlux is an Android library which helps to architect applications in a functional way (with immutable data in mind).

It is inspired by Clojure and Facebook Flux.

### Why Phlux?

##### Why not MVP

1. Immutable data is a key ingredient in building reliable software.
All data in Phlux is immutable. Unlike MVP/C/VM libraries,
Phlux requires to use immutable data to manage view state and to implement background tasks.
Reliability of a Phlux-driven application is outstanding.

2. Despite MVP/C libraries reduce the pain of Android lifecycles,
there are still some lifecycles and there is still some pain.
Phlux allows to forget about Android lifecycles even more.

3. Android has troubles with background task continuation, so the application architecture
must be ready for stress conditions
(wiping out background tasks and static variables is one of such conditions).
Phlux handles background tasks much better than any MVP/C implementation.
Forget about memory leaks, lost data and simultaneously running requests.

##### Why not Flux

1. The original Flux architecture has dispatcher we don't need on Android.
And even if we will implement it - it will work really bad because of lifecycles.
If you *really* need a dispatcher it can be easily implemented on top of Phlux.

2. Unlike Flux, we don't have React library for our views.
Creating and supporting a such library requires a great amount of time investment.
Phlux does not require such library.

3. We can *update* views instead of re-creating them. Sure, this is not as
"functional" as some of us want, but it is a good compromise. Android XML tools
are quite good and I personally don't want to lose them.
Phlux allows to use the current workflow and it does not require to hardcode all views.

Phlux is Android-friendly, it allows to call `view.update(viewState)`
at the end of all.

`update` Looks like a ViewHolder's method, isn't it?
A ViewHolder does not have many variables in it
(it must not have any variables at all if you're doing everything properly).
So how about turning your entire activity into a simple ViewHolder?

### Schema

While Phlux has little amount of boxes and arrows, it still has some. It is nice to know them:

![Data Flow](https://raw.githubusercontent.com/konmik/Phlux/resources/doc/data_flow.png)

This schema represents the Phlux data flow. Whenever an action occurs (be it a background task
completion or an UI event) it applies to *State* and then *View* should automatically
be updated from the new *State* instance.

When the application needs to update a view it should ask Phlux to `apply()` a function to the corresponding state.
Phlux manages all the internal stuff to avoid any data modification. The only variable that gets changed after
the called function is *Phlux Root*.

The main idea about this schema is that it is data-centric. And the data is immutable and parcelable,
so you can always rely on it.

"It is better to have 100 functions operate on one data structure than 10 functions on 10 data structures." — Alan Perlis

So now we have this one data structure and our function number does not increase so dramatically, we have even lesser
amount of functions than we normally have. The interesting effect is that all of our functions now have a clear purpose -
they either alter the data structure OR update a specific part of the view.

Normally on Android our views are bloated with
multi-purpose disorganized methods and variables, our background task management is a dark
and unfriendly place that causes tons of troubles.
Our data is partially parcelable and partially not, we save different values
and get NPEs because our data is not in sync with lifecycles.
With Phlux we have more control over methods,
over data and over background tasks.

![Data Model](https://github.com/konmik/Phlux/blob/resources/doc/data_model.png)

This is the Phlux data model. It is simple.

Phlux has root, it is just `Map<String, Scope>`.
Every *Scope* has a state of a corresponding *View*.
Every *Scope* also has a list of current background tasks.
Key is a randomly generated string that binds view to its state.

Whenever a process gets wiped out Phlux automatically restores scopes
and relaunches their tasks that did not complete yet.

The entire architecture is so simple, it is hard to believe that
no one have it implemented like this yet (if you have, then why didn't you release the
damn library so I could just relax and write reliable apps easily?)

### Why functional

Functional programming is a programming style/language that is based on two practices:

- Code composition by passing references to functions (lambdas) into other functions.
- Immutable data.

First point is out of the current interest.

The second point is unbelievable important for good architecture and application simplicity.
Every variable we have needs a special treating. We must constantly care about variables to
have values that correspond to other values in the application. We need to keep eye on the
order of variable initialization and changes. Every variable we have adds complexity.
We've called one method and changes are propagating across the application
without strict control. We've passed a reference to an object that has mutable variables
in it and we've got troubles.
In a multi-threading environment this complexity becomes stunning.

MVP/C architecture does not scale well just because of this single reason - uncontrolled and
unpredictable propagation of changes across application.

But do we really need tons of variables in each application? And, what is a minimum amount
of variables we can afford? How can we architect our apps to get most of the
"minimum variables amout" principle?

A Phlux-driven application can afford to have just one variable.

`AutoParcel` has a great support of the Builder pattern that allows to easily
create copies of modified data structures. `Solid` allows to do the same with collections
using streams. Java alone is not capable of applying this strategy, but with these two
libraries the job can be done.

*"But immutability comes with a price of increased garbage collection!"*

Not so bad, when we need to replace the root application variable we can reference
parts of the previous root variable. What will be GC'ed is only the difference
between two values which is not a problem. Be honest - who cares about reusing
content of previous variables that was allocated for usage in an adapter? No one, we
just drop them. The same is here, but now we can do this in a much more reliable way.

There is also a fact that not everybody consider when thinking about memory consumption.
Java collections internally allocate new arrays and they silently drop old arrays
of inappropriate size, so the memory consumption difference is even smaller than
memory-consumption purists think.

Overall, I only once had a problem caused by excessive memory consumption
(I was allocating a few 4Mb images during scrolling,
applying different graphical filters on them the same time).
But I had tons of problems caused by mutable variables flying everywhere around.

### Lifecycle goals

One of Phlux purposes is to eliminate the need in lifecycle handling.
All excessive lifecycle events have been replaced by the generalization of three.
Once created, the view state will be persisted and restores automatically,
so developers will be freed of dealing with configuration changes
and process restorations. You will rarely need most of lifecycle methods.

Activity will have just `onCreate`, `onScopeCreated` and `onUpdate` methods.
During `onCreate` you will need to initialize the usual activity visual parts and UI callbacks,
`onScopeCreated` is the place where you start background tasks for the first time
(they will be auto-connected and restarted automatically),
and `onUpdate` is where you're updating your activity from the activity state.

### Data requirements

Most data in a Phlux application must implement `Parcelable` interface.
This is because any data can be wiped out by Android and so it must be parceled.

I recommend using
[AutoParcel](https://github.com/frankiesardo/auto-parcel)
and
[Solid](https://github.com/konmik/solid)
libraries because they allow to have both - immutable and parcelable data the same time.

In case if you don't want some data to be parceled (for example you have a large list
of items that you can easily fetch from a database) you may use
[Transient](https://github.com/konmik/Phlux/blob/master/phlux/src/main/java/phlux/Transient.java)
class inside of `AutoParcel` data object.

`Transient` is just a reference, It implements `Parcelable` but it does not put
the referenced object into a parcel during serialization.

### Background tasks

On Android background tasks must save their arguments to be restarted in case of being wiped out.
So they must implement `Parcelable` as well.
[PhluxBackground](https://github.com/konmik/Phlux/blob/master/phlux/src/main/java/phlux/PhluxBackground.java)

There is a problem: we somehow need to save the reference to a function that will start
a background task from restored arguments.
The problem has been solved by bundling the function with it's arguments into a single parcelable class.

### A little note on `apply()` and multithreading

The `apply(function)` call is atomic and thread-safe.
It is implemented by using (simplified)
`rootReference.compareAndSet(originalRoot, originalRoot.withViewState(function(viewState)))`.
If another thread tries to alter the root during a `function` call then `apply()` will be retried.

This technique is similar to "Software Transactional Memory". It provides lock and callback free
multithreading capabilities for maximum performance and reliability.

It is important that your `function` must not change external variables - it can be called multiple
times so having external variables changed can cause unpredictable results.
`apply()` function returns both - previous and applied values, so you can take an action depending on what happened during the transaction.
This is rarely needed, though.

After a successful `apply()` execution a callback will be fired on the main thread.
If several modifications occur the same time then some of callbacks can be skipped, the last one is guaranteed to be fired anyway.
This prevents flooding of the main thread with callbacks that can't be processed fast enough.

### Why no RxJava

My previous architecture solutions was based on RxJava, however Phlux does not have such dependency.
There are two reasons for this:

- It is hard for newcomers to dive into RxJava *and* a new architecture the same time.
- Phlux is so simple that it has only one callback. RxJava will be an overkill here.

While Phlux does not require RxJava, it can be easily used with Phlux.
Normally I use it to process some of UI actions and all background tasks.

RxJava can be naturally used to substitute *dispatcher* on the original Flux diagram.

### Library status

The library in active development.

Currently I'm using the library for one of my home projects
with multiple activities, fragments, dialogs and background tasks.

Overall, I feel that the library has a great potential. It is clearly better than MVP/C libraries.
The library can potentially fit very well into MVVM but I do not care about data binding much.

### TODO

- 100% test coverage, as usual.

- Docs? :D

- Leverage the new AutoValue plugin [with-](https://github.com/google/auto/issues/294) methods in the example when 1.2 will be released.
