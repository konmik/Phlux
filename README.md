Phlux
=======

Phlux is an Android library which helps to architect applications with immutable data in mind.

It is inspired by Clojure and Facebook's Flux architecture.

### Why Phlux?

1. The original Flux architecture has too many squares and arrows.
All that dispatchers, stores - this is too complex for Android apps.

2. We don't have virtual DOM libraries.
Creating and supporting a such library can be a great pain.

3. Android has troubles with background task continuation, so the architecture
must be ready for any stress situation
(wiping out background tasks and static variables is one of such situations).

4. Immutable data is a key ingredient in building reliable software.

5. We can *update* views instead of re-creating them. Sure, this is not as
"functional" as some of us want, but it is a *very* good compromise. Android XML tools
are quite good and I personally don't want to lose them.

So I decided to implement a simplified version of Flux that
is more Android-friendly and allows to call `update(immutableState, view)`
at the end of all. Looks like adapter's method, isn't it?

The current implementation has about 130 lines of code! Could you imagine such efficiency?

### Library status

The library status is: "Wow, I can do this!".

There should be a strategy about not replacing `EditText` values on update.

Overall, I feel that the library has a great potential. It is clearly better than MVP/C libraries.
It can potentially fit very well into MVVM but I do not care about data binding much).

