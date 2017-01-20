# RxFirebase: Reactive Extensions for Firebase

[![Build Status](https://travis-ci.org/cantrowitz/RxFirebase.svg?branch=master)](https://travis-ci.org/cantrowitz/RxFirebase)

[![Code Coverage](https://codecov.io/github/cantrowitz/RxFirebase/coverage.svg?branch=master)](https://codecov.io/gh/cantrowitz/RxFirebase)

Firebase specific bindings for [RxJava2](http://github.com/ReactiveX/RxJava).

This module adds the minimum classes to Firebase that make writing reactive components in Android
applications easy and hassle-free. It exposes the FirebaseAuth object.


## Communication

All communication should be through the following channel:
- [GitHub Issues][issues]


# Binaries

```groovy
compile 'com.cantrowitz:rxfirebase:0.1.4'
// Because RxFirebase releases are few and far between, it is recommended you also
// explicitly depend on RxJava's and Firebase's latest version for bug fixes and new features.
compile 'io.reactivex.rxjava2:rxjava:2.0.4'
compile 'com.google.firebase:firebase-auth:10.0.1'
```




## Bugs and Feedback

For bugs, feature requests, and discussion please use [GitHub Issues][issues].

[issues]: https://github.com/cantrowitz/RxFirebase/issues