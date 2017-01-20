package com.cantrowitz.rxfirebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * This is the entry point for all {@link Observable} for {@link FirebaseAuth}
 */
public class RxFirebaseAuth {

    private final FirebaseAuth firebaseAuth;

    public RxFirebaseAuth(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    /**
     * Creates an Observable that emits on every notification of the
     * {@link FirebaseAuth.AuthStateListener}
     *
     * @return the {@link Observable}
     */
    public Observable<FirebaseAuth> whenAuthStateChanged() {
        return Observable.create(new ObservableOnSubscribe<FirebaseAuth>() {
            @Override
            public void subscribe(final ObservableEmitter<FirebaseAuth> emitter) throws Exception {
                final FirebaseAuth.AuthStateListener listener = new FirebaseAuth
                        .AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth value) {
                        emitter.onNext(value);
                    }
                };
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        firebaseAuth.removeAuthStateListener(listener);
                    }
                });
                firebaseAuth.addAuthStateListener(listener);
            }
        });
    }

    /**
     * Creates an Observable that only emits when there is a change of logged in user state
     *
     * @return {@link Observable}
     */
    public Observable<Maybe<FirebaseUser>> whenUserChanged() {
        return whenAuthStateChanged()
                .map(new Function<FirebaseAuth, Maybe<FirebaseUser>>() {
                    @Override
                    public Maybe<FirebaseUser> apply(FirebaseAuth newFirebaseAuth) throws
                            Exception {
                        return Maybe.fromNullable(newFirebaseAuth.getCurrentUser());
                    }
                })
                .distinctUntilChanged();
    }

    /**
     * Creates an Observable that only emits when a user has logged out. Only emits an empty
     * {@link Maybe}, not much use interacting with this object
     *
     * @return the {@link Observable}
     */
    public Observable<Maybe<FirebaseUser>> whenUserLoggedOut() {
        return whenUserChanged()
                .filter(new Predicate<Maybe<FirebaseUser>>() {
                    @Override
                    public boolean test(Maybe<FirebaseUser> maybeFirebaseUser) throws
                            Exception {
                        return !maybeFirebaseUser.isPresent();
                    }
                })
                .map(new Function<Maybe<FirebaseUser>, Maybe<FirebaseUser>>() {
                    @Override
                    public Maybe<FirebaseUser> apply(Maybe<FirebaseUser> firebaseUser) throws
                            Exception {
                        return Maybe.absent();
                    }
                });
    }

    /**
     * Creates an Observable that only emits when a user has logged in
     *
     * @return the {@link Observable}
     */
    public Observable<FirebaseUser> whenUserLoggedIn() {
        return whenUserChanged()
                .filter(new Predicate<Maybe<FirebaseUser>>() {
                    @Override
                    public boolean test(Maybe<FirebaseUser> maybeFirebaseUser) throws
                            Exception {
                        return maybeFirebaseUser.isPresent();
                    }
                })
                .map(new Function<Maybe<FirebaseUser>, FirebaseUser>() {
                    @Override
                    public FirebaseUser apply(Maybe<FirebaseUser> maybeFirebaseUser) throws
                            Exception {
                        return maybeFirebaseUser.get();
                    }
                });
    }

    /**
     * Sign in anonymously to the Firebase app
     *
     * @return Completeable for signing in
     */
    public Completable whenSignedInAnonymously() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                firebaseAuth
                        .signInAnonymously()
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    e.onComplete();
                                } else {
                                    e.onError(task.getException());
                                }
                            }
                        });
            }
        });
    }

    /**
     * Signs out of the current session
     *
     * @return Completeable for signing out
     */
    public Completable whenSignedOut() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                firebaseAuth.signOut();
                e.onComplete();
            }
        });
    }
}
