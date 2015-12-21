Phlux
=======

Phlux is an Android library which helps to architect applications in a functional way (with immutable data in mind).

It is inspired by Clojure and Facebook's Flux architecture.

### Why Phlux?

1. The original Flux architecture has too many squares and arrows.
All that dispatchers, stores - this is too complex for Android apps.

2. We don't have virtual DOM libraries.
Creating and supporting a such library requires a great amount of time investment.

3. Android has troubles with background task continuation, so the architecture
must be ready for any stress situation
(wiping out background tasks and static variables is one of such situations).

4. Immutable data is a key ingredient in building reliable software.

5. We can *update* views instead of re-creating them. Sure, this is not as
"functional" as some of us want, but it is a *very* good compromise. Android XML tools
are quite good and I personally don't want to lose them.

6. Despite MVP/C libraries reduce the pain of Android lifecycles,
there are still some lifecycles and there is still some pain.
Phlux allows to forget about Android lifecycles even more!

7. Get ready for [Hot Code Swapping](https://www.youtube.com/watch?v=YYin_N6xXxQ&feature=youtu.be&t=37m32s)!

So I decided to implement a simplified version of Flux that
is more Android-friendly and allows to call `view.update(immutableState)`
at the end of all.

`update` Looks like a ViewHolder's method, isn't it?
A ViewHolder does not have many variables in it
(it must not have variables at all if you're doing everything properly).
So how about turning your entire activity into a simple ViewHolder?

The current implementation has about 130 lines of code! Could you imagine such efficiency?

### Data requirements

Most data in a Phlux application should implement `Parcelable` interface.
This is because any data can be wiped out by Android and so it must be parceled.

I recommend using
[AutoParcel](https://github.com/frankiesardo/auto-parcel)
and
[Solid](https://github.com/konmik/solid)
libraries because they easily allow to have both - immutable and parcelable data.

In case if you don't want some data to be parceled (for example you have a large list
of items that you can easily fetch from a database) you may use
[Transient](https://github.com/konmik/Phlux/blob/master/phlux/src/main/java/phlux/Transient.java)
class inside of `AutoParcel` data object to mark such data as "temporary".

`Transient` is just a reference, It implements `Parcelable` but it does not put
the referenced object into a parcel during serialization.

### Background tasks

On Android background tasks must save their arguments to be restarted in case of being wiped out.
So they must implement `Parcelable` as well.
[PhluxBackground](https://github.com/konmik/Phlux/blob/master/phlux/src/main/java/phlux/PhluxBackground.java)

Background tasks may have `sticky` property to automatically refresh temporary data when it gets lost during
Android lifecycles.

### Library status

The library status is: "Wow, I can do this!".
I can create an application which has only *one* mutable variable!
(OK, there are *two* mutable variables - one for data and another one for the callback list.)

Overall, I feel that the library has a great potential. It is clearly better than MVP/C libraries.
The library can potentially fit very well into MVVM but I do not care about data binding much).

### TODO

- In some cases we need to call background and UI tasks sequentially.
This is still a pain, so I think about implementing Interactor pattern on top of Phlux.

- 100% test coverage, as usual.
