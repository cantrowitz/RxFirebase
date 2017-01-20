package com.cantrowitz.rxfirebase;

import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RxFirebaseAuth}
 */
public class RxFirebaseAuthTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    FirebaseAuth firebaseAuth;

    @Mock
    Task<AuthResult> taskAuthResult;
    @InjectMocks
    RxFirebaseAuth target;
    private OnCompleteListener<AuthResult> onCompleteListener;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Before
    public void setUp() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                authStateListener = invocation.getArgument(0);
                return null;
            }
        }).when(firebaseAuth).addAuthStateListener(any(FirebaseAuth.AuthStateListener.class));
    }

    @Test
    public void testWhenDisposed_unregistersAuthStateListener() throws Exception {
        target.whenAuthStateChanged().test(true);
        verify(firebaseAuth).removeAuthStateListener(authStateListener);
    }

    @Test
    public void testWhenAuthStateChanged() throws Exception {
        final TestObserver<FirebaseAuth> observer = target.whenAuthStateChanged().test();
        authStateListener.onAuthStateChanged(firebaseAuth);
        authStateListener.onAuthStateChanged(firebaseAuth);

        observer.assertValues(firebaseAuth, firebaseAuth);
    }

    @Test
    public void testWhenUserChanged() throws Exception {
        FirebaseUser firebaseUser1 = mock(FirebaseUser.class);
        FirebaseUser firebaseUser2 = mock(FirebaseUser.class);


        final TestObserver<Maybe<FirebaseUser>> observer = target.whenUserChanged().test();
        updateFirebaseUser(firebaseUser1);
        authStateListener.onAuthStateChanged(firebaseAuth);
        updateFirebaseUser(firebaseUser1);
        authStateListener.onAuthStateChanged(firebaseAuth);
        updateFirebaseUser(firebaseUser2);
        authStateListener.onAuthStateChanged(firebaseAuth);
        updateFirebaseUser(null);
        authStateListener.onAuthStateChanged(firebaseAuth);

        observer.assertValueCount(3);
        final List<Maybe<FirebaseUser>> values = observer.values();
        assertEquals(firebaseUser1, values.get(0).get());
        assertEquals(firebaseUser2, values.get(1).get());
        assertFalse(values.get(2).isPresent());
    }

    @Test
    public void testWhenUserLoggedOut() throws Exception {
        FirebaseUser firebaseUser1 = mock(FirebaseUser.class);

        final TestObserver<Maybe<FirebaseUser>> observer = target.whenUserLoggedOut().test();
        updateFirebaseUser(firebaseUser1);
        authStateListener.onAuthStateChanged(firebaseAuth);
        updateFirebaseUser(null);
        authStateListener.onAuthStateChanged(firebaseAuth);

        observer.assertValueCount(1);
        final List<Maybe<FirebaseUser>> values = observer.values();
        assertFalse(values.get(0).isPresent());
    }

    @Test
    public void testWhenUserLoggedIn() throws Exception {
        FirebaseUser firebaseUser1 = mock(FirebaseUser.class);

        final TestObserver<FirebaseUser> observer = target.whenUserLoggedIn().test();
        updateFirebaseUser(null);
        authStateListener.onAuthStateChanged(firebaseAuth);
        updateFirebaseUser(firebaseUser1);
        authStateListener.onAuthStateChanged(firebaseAuth);

        observer.assertValues(firebaseUser1);
    }

    @Test
    public void testSignInAnonymously_andCompletes() throws Exception {
        when(firebaseAuth.signInAnonymously()).thenReturn(taskAuthResult);
        when(taskAuthResult.isSuccessful()).thenReturn(true);

        setCompleteListener();

        final TestObserver<Void> testObserver = target.whenSignedInAnonymously().test();

        onCompleteListener.onComplete(taskAuthResult);
        testObserver.assertComplete();
    }

    private void setCompleteListener() {
        doAnswer(new Answer<Task<AuthResult>>() {
            @Override
            public Task<AuthResult> answer(InvocationOnMock invocation) throws Throwable {
                onCompleteListener = invocation.getArgument(0);
                return taskAuthResult;
            }
        }).when(taskAuthResult)
                .addOnCompleteListener(any(OnCompleteListener.class));
    }

    @Test
    public void testSignInAnonymously_andFails() throws Exception {
        final Exception exception = new Exception();
        when(firebaseAuth.signInAnonymously()).thenReturn(taskAuthResult);
        when(taskAuthResult.isSuccessful()).thenReturn(false);
        when(taskAuthResult.getException()).thenReturn(exception);

        setCompleteListener();

        final TestObserver<Void> testObserver = target.whenSignedInAnonymously().test();

        onCompleteListener.onComplete(taskAuthResult);

        testObserver.assertError(exception);
    }

    @Test
    public void testSignout() throws Exception {
        final TestObserver<Void> testObserver = target.whenSignedOut().test();
        verify(firebaseAuth).signOut();
        testObserver.assertComplete();
    }

    private FirebaseAuth updateFirebaseUser(@Nullable FirebaseUser firebaseUser) {
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        return firebaseAuth;
    }
}